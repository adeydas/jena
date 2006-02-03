/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.iri.impl;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIComponents;
import com.hp.hpl.jena.iri.MalformedIDNException;
import com.hp.hpl.jena.iri.Violation;
import com.hp.hpl.jena.iri.ViolationCodes;
import com.ibm.icu.text.IDNA;
import com.ibm.icu.text.StringPrepParseException;
/*
import com.vgrs.xcode.idna.Idna;
import com.vgrs.xcode.idna.Nameprep;
import com.vgrs.xcode.idna.Punycode;
import com.vgrs.xcode.util.XcodeException;
*/
abstract public class AbsIRIImpl extends  IRI implements 
        ViolationCodes, IRIComponents {

    private static final int defaultRelative = ABSOLUTE | SAMEDOCUMENT | CHILD
            | PARENT | GRANDPARENT;

    static String removeDotSegments(String path) {
        // 5.2.4 step 1.
        int inputBufferStart = 0;
        int inputBufferEnd = path.length();
        StringBuffer output = new StringBuffer();
        // 5.2.4 step 2.
        while (inputBufferStart < inputBufferEnd) {
            String in = path.substring(inputBufferStart);
            // 5.2.4 step 2A
            if (in.startsWith("./")) {
                inputBufferStart += 2;
                continue;
            }
            if (in.startsWith("../")) {
                inputBufferStart += 3;
                continue;
            }
            // 5.2.4 2 B.
            if (in.startsWith("/./")) {
                inputBufferStart += 2;
                continue;
            }
            if (in.equals("/.")) {
                in = "/"; // don't continue, process below.
                inputBufferStart += 2; // force end of loop
            }
            // 5.2.4 2 C.
            if (in.startsWith("/../")) {
                inputBufferStart += 3;
                removeLastSeqment(output);
                continue;
            }
            if (in.equals("/..")) {
                in = "/"; // don't continue, process below.
                inputBufferStart += 3; // force end of loop
                removeLastSeqment(output);
            }
            // 5.2.4 2 D.
            if (in.equals(".")) {
                inputBufferStart += 1;
                continue;
            }
            if (in.equals("..")) {
                inputBufferStart += 2;
                continue;
            }
            // 5.2.4 2 E.
            int nextSlash = in.indexOf('/', 1);
            if (nextSlash == -1)
                nextSlash = in.length();
            inputBufferStart += nextSlash;
            output.append(in.substring(0, nextSlash));
        }
        // 5.2.4 3
        return output.toString();
    }

    private static void removeLastSeqment(StringBuffer output) {
        int ix = output.length();
        while (ix > 0) {
            ix--;
            if (output.charAt(ix) == '/')
                break;
        }
        output.setLength(ix);
    }

    private long foundExceptionMask;

    long allErrors;

    abstract long errors(int field);

    abstract SchemeSpecificPart getSchemeSpec();
    
    // void throwExceptions(IRIFactoryImpl f, boolean includeRelative) {
    // long mask = f.exceptions;
    // if (!includeRelative)
    // mask &= ~(1l << RELATIVE_URI);
    // if (hasExceptionMask(mask)) {
    // throw (IRIImplUncheckedException) exceptionsMask(mask).next();
    // }
    // }

    boolean hasExceptionMask(long mask) {
        return (allErrors & mask) != 0;
    }

    private ArrayList foundExceptions;

    protected String path;
/*
    static private Idna idna;
    static {
        try {
            idna = new Idna(new Punycode(), new Nameprep());
        } catch (XcodeException e) {
            System.err.println("Internal error in IDN setup");
            e.printStackTrace();
        }
    }
*/
    static final private char hex[] = "0123456789ABCDEF".toCharArray();

    static final Iterator nullIterator = new ArrayList(0).iterator();

    protected static final int NO_EXCEPTIONS = 1;

    protected static final int ALL_EXCEPTIONS = 2;

    protected static final int NOT_RELATIVE_EXCEPTIONS = 3;

    protected static final int PATH_INDEX = Parser.invFields[PATH];

    public AbsIRIImpl() {
        super();
    }

    Iterator exceptionsMask(final long mask) {
        createExceptions(mask);
        return foundExceptions == null ? nullIterator : 
            new Iterator() {
               private Iterator underlying = foundExceptions.iterator();
  
                private Object next;
                public void remove() {
                    throw new UnsupportedOperationException();
                }

                public boolean hasNext() {
                    if (next==null) {
                        while (underlying.hasNext()) {
                            next = underlying.next();
                            if (((1l << ((Violation) next).getViolationCode()) 
                                    & mask) != 0) 
                                return true;
                        }
                        next = null;
                        return false;
                    }
                    return true;
                }

                public Object next() {
                    if (hasNext()) {
                        Object rslt = next;
                        next = null;
                        return rslt;
                    }
                    throw new NoSuchElementException();
                }
            
        };
    }

    private void createExceptions(long m) {
        m &= ~foundExceptionMask;
        if ((allErrors & m) != 0) {
            if (foundExceptions == null) {
                foundExceptions = new ArrayList();
            }
            for (int i = 0; i < Parser.fields.length; i++) {
                int f = Parser.fields[i];
                if ((errors(f) & m) != 0) {
                    for (int e = 0; e < 64; e++)
                        if (((1l << e) & m & errors(f)) != 0) {
                            foundExceptions.add(new ViolationImpl(this, f, e));
                        }

                }
            }

        }
        foundExceptionMask |= m;
    }

    public boolean isAbsolute() {
        return has(SCHEME);
    }

    abstract boolean has(int component);

    public boolean isRelative() {
        return !has(SCHEME);
    }

    /*
     * public boolean isRDFURIReference() { return !hasException(RDF); }
     * 
     * public boolean isIRI() { return !hasException(IRI); }
     * 
     * public boolean isURIinASCII() { return !hasException(URI); }
     */
    // public boolean isVeryBad() {
    // return false;
    // }
    // public boolean isXSanyURI() {
    // return !hasException(XMLSchema);
    // }
    /*
    public boolean hasException(int conformance) {
        return hasExceptionMask(getFactory().recsToMask(conformance));
    }

    public Iterator exceptions(int conformance) {
        return exceptionsMask(getFactory().recsToMask(conformance));
    }
    */

    public boolean hasViolation(boolean includeWarnings) {
        return hasExceptionMask(getSchemeSpec().getMask(includeWarnings));
    }

    public Iterator violations(boolean includeWarnings) {
        return exceptionsMask(getSchemeSpec().getMask(includeWarnings));
    }

    public URL toURL() throws MalformedURLException {
        return new URL(toASCIIString());
    }

    // TODO ToAsciiMask
    static long ToAsciiMask = 
        ~0l;
        /*
        (1l << LTR_CHAR) | (1l << ILLEGAL_CHAR)
            | (1l << IRI_CHAR) | (1l << UNWISE_CHAR) | (1l << WHITESPACE)
            | (1l << NOT_XML_SCHEMA_WHITESPACE) | (1l << NON_XML_CHARACTER)
            | (1l << DOUBLE_DASH_IN_REG_NAME);
*/
    public String toASCIIString() throws MalformedURLException {
        if (hasExceptionMask(ToAsciiMask)) {
            return createASCIIString();
        }
        return toString();
    }

    private String createASCIIString() throws MalformedURLException {
        StringBuffer asciiString = new StringBuffer();

        if (has(SCHEME)) {
            toAscii(asciiString, getScheme(), errors(SCHEME));
            asciiString.append(':');
        }
        if (has(AUTHORITY)) {
            asciiString.append("//");
            if (has(USER)) {
                toAscii(asciiString, getRawUserinfo(), errors(USER));
                asciiString.append('@');
            }

            String host = getRawHost();
            regNameToAscii(asciiString,host);
            if (has(PORT)) {
                asciiString.append(':');
                toAscii(asciiString, get(PORT), errors(USER));
            }
        }
        toAscii(asciiString, getRawPath(), errors(PATH));
        if (has(QUERY)) {
            asciiString.append('?');
            toAscii(asciiString, getRawQuery(), errors(QUERY));
        }
        if (has(FRAGMENT)) {
            asciiString.append('#');
            toAscii(asciiString, getRawFragment(), errors(FRAGMENT));
        }
        return asciiString.toString();
    }

    private void regNameToAscii(StringBuffer asciiString, String host)
            throws MalformedURLException {
        if ((errors(HOST) & ToAsciiMask) == 0) {
            asciiString.append(host);
            return;
        }
       
        asciiString.append(domainToAscii(host));

    }

    static StringBuffer domainToAscii(String host) throws MalformedIDNException {
        
       try {
        
        return IDNA.convertIDNToASCII(host,IDNA.USE_STD3_RULES|IDNA.ALLOW_UNASSIGNED);
    } catch (StringPrepParseException e) {
        throw new MalformedIDNException(e);
    } catch (IndexOutOfBoundsException e) {
        throw new MalformedIDNException(
                new StringPrepParseException("The labels in the input are too long. Length > 64.", 
                        StringPrepParseException.LABEL_TOO_LONG_ERROR,host,0)
                );
    }
        /*
        int u[] = new int[host.length()];
        for (int i = 0; i < host.length(); i++)
            u[i] = host.charAt(i);

        try {
            return idna.domainToAscii(u);
        } catch (XcodeException e) {
            throw new MalformedIDNException(e);
        }
        */
    }

    private void toAscii(StringBuffer asciiString, String field, long errs) {
        if ((errs & ToAsciiMask) == 0) {
            asciiString.append(field);
            return;
        }
        // 3.1 RFC 3987
        // Step 1c

        // nothing

        // Step 2a
        /*
         * Step 2. For each character in 'ucschar' or 'iprivate', apply steps
         * 2.1 through 2.3 below.
         * 
         * We interpret this as any charcater above 127, below 32 and the unwise
         * chars
         * 
         * Systems accepting IRIs MAY also deal with the printable characters in
         * US-ASCII that are not allowed in URIs, namely "<", ">", '"', space,
         * "{", "}", "|", "\", "^", and "`", in step 2 above.
         */
        for (int i = 0; i < field.length(); i++) {
            // TODO Java 1.4/1.5 issue
            int ch = field.charAt(i);
            if (ch > 127 || "<>\" {}|\\^`".indexOf(ch) != -1 || ch < 32) {
                // 2.1
                byte b[];
                try {
                    b = field.substring(i, i + 1).getBytes("utf-8");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException("Impossible - utf-8 unsupported");
                }
                // 2.2, 2.3
                for (int j = 0; j < b.length; j++) {
                    char buf[] = { '%', hex[(b[j] & (255 & ~15)) >> 4],
                            hex[b[j] & 15] };

                    asciiString.append(buf);
                }
            } else {
                asciiString.append(new char[] { (char) ch });
            }
        }
    }

    public IRI relativize(String abs, int flags) {
        return relativize(new IRIImpl(getFactory(), abs), flags);
    }

    public IRI relativize(String abs) {
        return relativize(abs, defaultRelative);
    }

    public IRI relativize(IRI abs) {
        return relativize(abs, defaultRelative);
    }

    static private int prefs[][] = { { CHILD, CHILD | PARENT | GRANDPARENT },
            { PARENT, PARENT | GRANDPARENT }, { GRANDPARENT, GRANDPARENT } };

    static String exact[] = { ".", "..", "../.." };

    static String sub[] = { "", "../", "../../" };

    /*
     * public String relativize(String abs, int flags) { return
     * relativize(factory.create(abs),abs,flags); }
     */
    public IRI relativize(IRI abs, int flags) {
        String rslt = relativize(abs, null, flags);
        return rslt == null ? abs : getFactory().create(rslt);
    }

    /**
     * 
     * @param r
     * @param def
     *            Default result if can't make this relative.
     * @param flags
     */
    private String relativize(IRI r, String def, int flags) {
        if (!has(AUTHORITY))   // we could use the new rules for relative URIs for rootless, but I don't like them
            return def;
        if (!((AbsIRIImpl)r).has(AUTHORITY))
            return def;
        // logger.info("<"+Util.substituteStandardEntities(abs)+">");
        // logger.info("<"+Util.substituteStandardEntities(r.m_path)+">");
        boolean net = equal(r.getScheme(), getScheme());
        boolean absl = net && equal(r.getRawHost(), getRawHost())
                && equal(getRawUserinfo(), r.getRawUserinfo())
                && equal(getPort(), r.getPort());
        boolean same = absl && equal(getRawPath(), r.getRawPath())
                && equal(getRawQuery(), r.getRawQuery());

        String rslt = r.getRawFragment() == null ? "" : ("#" + r
                .getRawFragment());

        if (same && (flags & SAMEDOCUMENT) != 0)
            return rslt;
        if (r.getRawQuery() != null) {
            rslt = "?" + r.getRawQuery() + rslt;
        }
        if (absl) {
            // TODO: pretty disgusting code, should be rewritten.
            // this array is stupid ...
            String m_subPaths[] = new String[] {
                    getRawPath() == null ? null : (getRawPath() + "a"), null,
                    null, null };

            if (m_subPaths[0] != null)
                for (int i = 0; i < 3; i++) {
                    if ((flags & prefs[i][1]) == 0)
                        break;
                    if (m_subPaths[i + 1] == null)
                        m_subPaths[i + 1] = getLastSlash(m_subPaths[i]);
                    if (m_subPaths[i + 1].length() == 0)
                        break;
                    if ((flags & prefs[i][0]) == 0)
                        continue;
                    if (!r.getRawPath().startsWith(m_subPaths[i + 1]))
                        continue;
                    // A relative path can be constructed.
                    int lg = m_subPaths[i + 1].length();
                    if (lg == r.getRawPath().length()) {
                        return exact[i] + rslt;
                    }
                    rslt = maybeDotSlash(sub[i] + r.getRawPath().substring(lg) + rslt);

                    // logger.info("<"+Util.substituteStandardEntities(rslt)+">["+i+"]");
                    return rslt;
                }
        }
        rslt = r.getRawPath() + rslt;
        if (absl && (flags & ABSOLUTE) != 0) {
            return rslt;
        }
        if (net && (flags & NETWORK) != 0) {
            return "//"
                    + (r.getRawUserinfo() == null ? ""
                            : (r.getRawUserinfo() + "@")) + r.getRawHost()
                    + (r.getPort() == IRI.NO_PORT ? "" : (":" + ((AbsIRIImpl)r).get(PORT))) + rslt;

        }
        return def;
    }

    static private String maybeDotSlash(String path) {
        int colon = path.indexOf(':');
        if (colon == -1)
            return path;
        int slash = path.indexOf('/');
        if (slash==-1 || slash>colon)
            return "./"+path;
        return path;
    }

    static private String getLastSlash(String s) {
        int ix = s.lastIndexOf('/', s.length() - 2);
        return s.substring(0, ix + 1);
    }

    private boolean equal(String s1, String s2) {
        return s1 == null ? s2 == null : s1.equals(s2);
    }

    private boolean equal(int s1, int s2) {
        return s1 == s2;
    }

    public Iterator allViolations() {
        return exceptionsMask(~0l);
    }

    public String getRawUserinfo() {
        return get(USER);
    }

    public int getPort() {
        String port = get(PORT);
        if (port == null)
            return IRI.NO_PORT;
        try {
            int v = Integer.parseInt(port);
            if (v<0)
                return IRI.ILLFORMED_PORT;
            return v;
        } catch (Exception e) {
            return IRI.ILLFORMED_PORT;
        }
    }

    public String getRawQuery() {
        return get(QUERY);
    }

    public String getRawFragment() {
        return get(FRAGMENT);
    }

    public String getRawHost() {
        return get(HOST);
    }

    public String getScheme() {
        return get(SCHEME);
    }

    abstract String get(int comp);

    public String getRawPath() {
        return path;
    }

    public boolean isRootless() {
        if (!has(SCHEME))
            return false;
        if (has(AUTHORITY))
            return false;
        if (path.equals(""))
            return false;
        if (path.charAt(0) == '/')
            return false;
        return true;
    }

    abstract String pathRemoveDots();

    abstract boolean dotsOK();

    public String getRawAuthority() {
        return get(AUTHORITY);
    }

    public IRI create(IRI i) {
        return new ResolvedRelativeIRI(this, (AbsIRIImpl) getFactory()
                .create(i));
    }

    public IRI create(String s) {
        return create(new IRIImpl(getFactory(), s) );
    }

    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (!(o instanceof IRI))
            return false;
        return toString().equals(o.toString());
    }

    public int hashCode() {
        return toString().hashCode();
    }

    public String getAuthority() {
        return getCooked(AUTHORITY);
    }

    public String getFragment() {
        return getCooked(FRAGMENT);
    }

    public String getHost() {
        return getCooked(HOST);
    }

    public String getPath() {
        return getCooked(PATH);
    }

    public String getQuery() {
        return getCooked(QUERY);
    }

    public String getUserinfo() {
        return getCooked(USER);
    }

    private String getCooked(int component) {
        // TODO Auto-generated method stub
        return null;
    }

    public IRI normalize(boolean useDns) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * Bidirectional IRIs MUST be rendered in the same way as they would be if
     * they were in a left-to-right embedding; i.e., as if they were preceded by
     * U+202A, LEFT-TO-RIGHT EMBEDDING (LRE), and followed by U+202C, POP
     * DIRECTIONAL FORMATTING (PDF).
     * 
     */
    public String toDisplayString() {
        return "\u202A" + toString() + "\u202C";
    }

    // TODO http://example.com/&#x10300;&#x10301;&#x10302 =>
    // http://example.com/%F0%90%8C%80%F0%90%8C%81%F0%90%8C%82

    public String getASCIIHost() throws MalformedURLException {
        StringBuffer asciiString = new StringBuffer();

        String host = getRawHost();
        if (host==null)
            return null;
        regNameToAscii(asciiString,host);
        return asciiString.toString();
    }

    public boolean ladderEquals(IRI iri, int other) {
        // TODO Auto-generated method stub
        return false;
    }

    public int ladderEquals(IRI iri) {
        // TODO Auto-generated method stub
        return 0;
    }
}

/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

