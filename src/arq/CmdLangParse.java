/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package arq;

import java.io.ByteArrayInputStream ;
import java.io.IOException ;
import java.io.InputStream ;
import java.util.HashMap ;
import java.util.Map ;
import java.util.Properties ;

import org.apache.log4j.PropertyConfigurator ;
import org.openjena.atlas.io.IO ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.atlas.lib.SinkCounting ;
import org.openjena.atlas.lib.SinkNull ;
import org.openjena.atlas.lib.StrUtils ;
import org.openjena.riot.ErrorHandler ;
import org.openjena.riot.ErrorHandlerFactory ;
import org.openjena.riot.Lang ;
import org.openjena.riot.RiotException ;
import org.openjena.riot.RiotReader ;
import org.openjena.riot.SysRIOT ;
import org.openjena.riot.lang.LangRDFXML ;
import org.openjena.riot.lang.LangRIOT ;
import org.openjena.riot.out.SinkQuadOutput ;
import org.openjena.riot.out.SinkTripleOutput ;
import org.openjena.riot.pipeline.inf.InfFactory ;
import org.openjena.riot.pipeline.inf.InferenceSetupRDFS ;
import org.openjena.riot.system.RiotLib ;
import org.openjena.riot.tokens.Tokenizer ;
import org.openjena.riot.tokens.TokenizerFactory ;
import arq.cmd.CmdException ;
import arq.cmdline.CmdGeneral ;
import arq.cmdline.ModLangParse ;
import arq.cmdline.ModTime ;

import com.hp.hpl.jena.Jena ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.n3.IRIResolver ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.core.Quad ;

/** Common framework for running RIOT parsers */
public abstract class CmdLangParse extends CmdGeneral
{
    // We are not a TDB command but still set the logging.
    //static { CmdTDB.setLogging() ; }
    // Module.
    protected ModTime modTime                   = new ModTime() ;
    protected ModLangParse modLangParse         = new ModLangParse() ;
    protected InferenceSetupRDFS setup          = null ; 
    
    interface LangHandler {
        String getItemsName() ;
        String getRateName() ;
    }

    static LangHandler langHandlerQuads = new LangHandler()
    {
        public String getItemsName()        { return "quads" ; }
        public String getRateName()         { return "QPS" ; }
    } ;
    static LangHandler langHandlerTriples = new LangHandler()
    {
        public String getItemsName()        { return "triples" ; }
        public String getRateName()         { return "TPS" ; }
    } ;
    static LangHandler langHandlerAny = new LangHandler()
    {
        public String getItemsName()        { return "tuples" ; }
        public String getRateName()         { return "TPS" ; }
    } ;
    
    protected static Map<Lang, LangHandler> dispatch = new HashMap<Lang, LangHandler>() ; 
    static {
        for ( Lang lang : Lang.values())
        {
            if ( lang.isQuads() )
                dispatch.put(lang, langHandlerQuads) ;
            else
                dispatch.put(lang, langHandlerTriples) ;
        }
    }
    
    protected LangHandler langHandlerOverall = null ;

    // This is teh setup for command for their message via the logging in ErrorHandlers
    private static final String log4Jsetup = StrUtils.strjoin("\n"
//                    , "## Plain output to stdout"
//                    , "log4j.appender.riot.plain=org.apache.log4j.ConsoleAppender"
//                    , "log4j.appender.riot.plain.target=System.out"
//                    , "log4j.appender.riot.plain.layout=org.apache.log4j.PatternLayout"
//                    , "log4j.appender.riot.plain.layout.ConversionPattern=%m%n"
                    , "## Plain output to stderr"
                    , "log4j.appender.riot.plainerr=org.apache.log4j.ConsoleAppender"
                    , "log4j.appender.riot.plainerr.target=System.err"
                    , "log4j.appender.riot.plainerr.layout=org.apache.log4j.PatternLayout"
                    , "log4j.appender.riot.plainerr.layout.ConversionPattern=%-5p %m%n"
                    , "## Everything"
                    , "log4j.rootLogger=INFO, riot.plainerr"
                    , "## Parser output"
                    , "log4j.additivity."+SysRIOT.riotLoggerName+"=false"
                    , "log4j.logger."+SysRIOT.riotLoggerName+"=ALL, riot.plainerr "
     ) ;

    /** Reset the logging to be good for command line tools */
    public static void setLogging()
    {
        // Turn off optimizer warning.
        // Use a plain logger for output. 
        Properties p = new Properties() ;
        InputStream in = new ByteArrayInputStream(StrUtils.asUTF8bytes(log4Jsetup)) ;
        try { p.load(in) ; } catch (IOException ex) {}
        PropertyConfigurator.configure(p) ;
        //LogManager.getLogger(SysRIOT.riotLoggerName).setLevel(Level.ALL) ;
        System.setProperty("log4j.configuration", "set") ;
    }
    
    
    protected CmdLangParse(String[] argv)
    {
        super(argv) ;
        // As a command, we take control of logging ourselves. 
        setLogging() ;
        super.addModule(modTime) ;
        super.addModule(modLangParse) ;
        
        super.modVersion.addClass(Jena.class) ;
        super.modVersion.addClass(ARQ.class) ;
        //super.modVersion.addClass(SysRIOT.class) ;
    }

    @Override
    protected String getSummary()
    {
        return getCommandName()+" [--time] [--check|--noCheck] [--sink] [--base=IRI] [--skip | --stopOnError] file ..." ;
    }

    @Override
    protected void processModulesAndArgs()
    { }

    protected long totalMillis = 0 ; 
    protected long totalTuples = 0 ; 
    

    @Override
    protected void exec()
    {
        if ( modLangParse.strictMode() )
        {
            SysRIOT.StrictXSDLexicialForms = true ;
        }
        
        if ( modLangParse.getRDFSVocab() != null )
            setup =  new InferenceSetupRDFS(modLangParse.getRDFSVocab()) ;
        
        try {
            if ( super.getPositional().isEmpty() )
                parseFile("-") ;
            else
            {
                for ( String fn : super.getPositional() )
                    parseFile(fn) ;
            }
        } finally {
            System.err.flush() ;
            System.out.flush() ;
            if ( super.getPositional().size() > 1 && modTime.timingEnabled() )
                output("Total", totalTuples, totalMillis, langHandlerOverall) ;
        }
    }

    public void parseFile(String filename)
    {
        InputStream in = null ;
        if ( filename.equals("-") )
            parseFile("http://base/", "stdin", System.in) ;
        else
        {
            try {
                in = IO.openFile(filename) ;
            } catch (Exception ex)
            {
                System.err.println("Can't open '"+filename+"' "+ex.getMessage()) ;
                return ;
            }
            parseFile(filename, filename, in) ;
            IO.close(in) ;
        }
    }

    public void parseFile(String defaultBaseURI, String filename, InputStream in)
    {   
        String baseURI = modLangParse.getBaseIRI() ;
        if ( baseURI == null )
            baseURI = defaultBaseURI ;
        // Make absolute
        baseURI = IRIResolver.resolveGlobal(baseURI) ;
        parseRIOT(baseURI, filename, in) ;
    }
    
    protected abstract Lang selectLang(String filename, Lang lang) ;

    protected void parseRIOT(String baseURI, String filename, InputStream in)
    {
        boolean checking = true ;
        if ( modLangParse.explicitChecking() )  checking = true ;
        if ( modLangParse.explicitNoChecking() ) checking = false ;
        
        ErrorHandler errHandler = null ;
        if ( checking )
        {
            if ( modLangParse.stopOnBadTerm() )
                errHandler = ErrorHandlerFactory.errorHandlerStd  ;
            else
                // Try to go on if possible.  This is the default behaviour.
                errHandler = ErrorHandlerFactory.errorHandlerWarn ;
        }
        
        if ( modLangParse.skipOnBadTerm() )
        {
            // TODO skipOnBadterm
        }
        
        Lang lang = selectLang(filename, Lang.NQUADS) ;  
        LangHandler handler = dispatch.get(lang) ;
        if ( handler == null )
            throw new CmdException("Undefined language: "+lang) ; 
        
        // If multiple files, choose the overall labels. 
        if ( langHandlerOverall == null )
            langHandlerOverall = handler ;
        else
        {
            if ( langHandlerOverall != langHandlerAny )
            {
                if ( langHandlerOverall != handler )
                    langHandlerOverall = langHandlerAny ;
            }
        }
        
        SinkCounting<?> sink ;
        LangRIOT parser ;
        
        // Uglyness because quads and triples aren't subtype of some Tuple<Node>
        // That would change a lot (Triples came several years before Quads). 
        if ( lang.isTriples() )
        {
            Sink <Triple> s = SinkNull.create() ;
            if ( ! modLangParse.toBitBucket() )
                s = new SinkTripleOutput(System.out) ;
            if ( setup != null )
                s = InfFactory.infTriples(s, setup) ;
            
            SinkCounting<Triple> sink2 = new SinkCounting<Triple>(s) ;
            
            if ( lang.equals(Lang.RDFXML) )
                // Adapter round ARP RDF/XML reader.
                parser = LangRDFXML.create(in, baseURI, filename, errHandler, sink2) ;
            else
                parser = RiotReader.createParserTriples(in, lang, baseURI, sink2) ;
            sink = sink2 ;
        }
        else
        {
            Sink <Quad> s = SinkNull.create() ;
            if ( ! modLangParse.toBitBucket() )
                s = new SinkQuadOutput(System.out) ;
            if ( setup != null )
                s = InfFactory.infQuads(s, setup) ;
            
            SinkCounting<Quad> sink2 = new SinkCounting<Quad>(s) ;
            parser = RiotReader.createParserQuads(in, lang, baseURI, sink2) ;
            sink = sink2 ;
        }
        
        modTime.startTimer() ;
        // Pending log message having he filename in them.
        // output(filename) ;
        try
        {
            // Default behaviour is "check":
            
            if ( checking )
            {
                if ( parser.getLang() == Lang.NTRIPLES ||  parser.getLang() == Lang.NQUADS )
                    parser.setProfile(RiotLib.profile(baseURI, false, true, errHandler)) ;
                else
                    parser.setProfile(RiotLib.profile(baseURI, true, true, errHandler)) ;
            }
            else
                parser.setProfile(RiotLib.profile(baseURI, false, false, errHandler)) ;
            parser.parse() ;
        }
        catch (RiotException ex)
        {
            // Should have handled the exception and logged a message by now.
            //System.err.println("++++"+ex.getMessage()); 
           
            if ( modLangParse.stopOnBadTerm() )
                return ;
        }
        finally {
            IO.close(in) ;
            sink.close() ;
        }
        long x = modTime.endTimer() ;
        long n = sink.getCount() ;
        

        if ( modTime.timingEnabled() )
            output(filename, n, x, handler) ;
        
        totalMillis += x ;
        totalTuples += n ;
    }
    
    protected Tokenizer makeTokenizer(InputStream in)
    {
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerUTF8(in) ;
        return tokenizer ;
    }
    
    protected void output(String label, long numberTriples, long timeMillis, LangHandler handler)
    {
        double timeSec = timeMillis/1000.0 ;
        
        System.out.printf("%s : %,5.2f sec  %,d %s  %,.2f %s\n",
                          label,
                          timeMillis/1000.0, numberTriples,
                          handler.getItemsName(),
                          timeSec == 0 ? 0.0 : numberTriples/timeSec,
                          handler.getRateName()) ;
    }
    
    protected void output(String label)
    {
        System.out.printf("%s : \n", label) ;
    }
}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */