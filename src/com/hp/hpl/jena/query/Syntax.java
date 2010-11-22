/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query;

import com.hp.hpl.jena.sparql.util.Symbol ;
import com.hp.hpl.jena.sparql.util.TranslationTable ;

/** Symbols for query language syntaxes */

public class Syntax extends Symbol
{
    /** The syntax that the DAWG working group defined */
    public static final Syntax syntaxSPARQL_10
                = new Syntax("http://jena.hpl.hp.com/2003/07/query/SPARQL_10") ;

    /** The syntax that the SPARQL working group has defined */
    public static final Syntax syntaxSPARQL_11
                = new Syntax("http://jena.hpl.hp.com/2003/07/query/SPARQL_11") ;
    
//    /** The update syntax that the SPARQL working group has defined */
//    private static final Syntax syntaxSPARQL_11_Update
//                = syntaxSPARQL_11 ;
//                //= new Syntax("http://jena.hpl.hp.com/2003/07/update/SPARQL_11") ;

    /** The query syntax for extended SPARQL */ 
    public static final Syntax syntaxARQ
                = new Syntax("http://jena.hpl.hp.com/2003/07/query/ARQ") ;

//    /** The update syntax for SPARQL Update, with extensions to help migrate the update language in the W3C submission */  
//    private static final Syntax syntaxARQ_Update
//                = syntaxARQ ;
//                //= new Syntax("http://jena.hpl.hp.com/2003/07/update/ARQ") ;
    
    public static final Syntax syntaxRDQL
                = new Syntax("http://jena.hpl.hp.com/2003/07/query/RDQL") ;

    public static final Syntax syntaxAlgebra
                = new Syntax("http://jena.hpl.hp.com/2003/07/query/SPARQL_Algebra") ;
    
    /** The system default syntax */ 
    public static Syntax defaultSyntax = syntaxSPARQL_11 ;
    
    /** The name of the default query language for query parsing.
     *  The default query language syntax must be capable of accepting
     *  any SPARQL query but may also accept extensions. 
     */
    public static Syntax defaultQuerySyntax = syntaxSPARQL_11 ;
    
    /** The name of the default update language for update parsing.
     *  The default update language syntax must be capable of accepting
     *  any SPARQL query but may also accept extensions. 
     */
    public static Syntax defaultUpdateSyntax = syntaxARQ ;
    
    /** The query syntax currently that is standardized, published, SPARQL - the "default SPARQL Query" */ 
    public static final Syntax syntaxSPARQL = syntaxSPARQL_11 ;

//    /** The update syntax currently that is standardized, published, SPARQL - the "default SPARQL Update" */ 
//    private static final Syntax syntaxSPARQL_Update = syntaxSPARQL_11_Update ;

    public static TranslationTable<Syntax> querySyntaxNames = new TranslationTable<Syntax>(true) ;
    static {
        querySyntaxNames.put("sparql",      syntaxSPARQL) ;
        querySyntaxNames.put("sparql_10",   syntaxSPARQL_10) ;
        querySyntaxNames.put("sparql_11",   syntaxSPARQL_11) ;
        querySyntaxNames.put("arq",         syntaxARQ) ;
        querySyntaxNames.put("rdql",        syntaxRDQL) ;
        querySyntaxNames.put("alg",         syntaxAlgebra) ;
        querySyntaxNames.put("op",          syntaxAlgebra) ;
    }

    public static TranslationTable<Syntax> updateSyntaxNames = new TranslationTable<Syntax>(true) ;
    static {
        querySyntaxNames.put("sparql",      syntaxSPARQL) ;
        querySyntaxNames.put("sparql_11",   syntaxSPARQL_11) ;
        querySyntaxNames.put("arq",         syntaxARQ) ;
    }

    protected Syntax(String s) { super(s) ; }
	protected Syntax(Syntax s) { super(s) ; }
    
    public static Syntax make(String uri)
    {
        if ( uri == null )
            return null ;
        
        Symbol sym = Symbol.create(uri) ;
        
        if ( sym.equals(syntaxARQ) )         return syntaxARQ ;
        
        if ( sym.equals(syntaxRDQL) )        return syntaxRDQL ;
        if ( sym.equals(syntaxSPARQL) )      return syntaxSPARQL ;
        if ( sym.equals(syntaxSPARQL_10) )   return syntaxSPARQL_10 ;
        if ( sym.equals(syntaxSPARQL_11) )   return syntaxSPARQL_11 ;
        if ( sym.equals(syntaxAlgebra) )     return syntaxAlgebra ;
        return null ;
    }
    
    
    public static Syntax guessFileSyntax(String url) 
    {
        return guessFileSyntax(url, syntaxSPARQL) ;
    }

    /** Gues the synatx (query and update) based on filename */
    public static Syntax guessFileSyntax(String url, Syntax defaultSyntax)
    {
        if ( url.endsWith(".arq") )     return syntaxARQ ;
        if ( url.endsWith(".rq") )      return syntaxSPARQL ;

        if ( url.endsWith(".aru") )     return syntaxARQ ;
        if ( url.endsWith(".ru") )      return syntaxSPARQL_11 ;
        
        if ( url.endsWith(".rdql") )    return syntaxRDQL ;
        if ( url.endsWith(".sse") )     return syntaxAlgebra ;
        
        // Default
        return defaultSyntax ;
    }
    
    /** Guess the query syntax based on file name */
    public static Syntax guessQueryFileSyntax(String url) 
    {
        return guessFileSyntax(url, defaultQuerySyntax) ;
    }
    

    /** Guess the query syntax based on file name */
    public static Syntax guessQueryFileSyntax(String url, Syntax defaultSyntax)
    {
        if ( url.endsWith(".arq") )     return syntaxARQ ;
        if ( url.endsWith(".rq") )      return syntaxSPARQL ;

        if ( url.endsWith(".rdql") )    return syntaxRDQL ;
        if ( url.endsWith(".sse") )     return syntaxAlgebra ;
        return defaultSyntax ;
    }

    /** Guess the update syntax based on file name */
    public static Syntax guessUpdateFileSyntax(String url)
    {
        return guessUpdateFileSyntax(url, defaultUpdateSyntax) ;
    }

    
    /** Guess the update syntax based on file name */
    public static Syntax guessUpdateFileSyntax(String url, Syntax defaultSyntax)
    {
        if ( url.endsWith(".aru") )     return syntaxARQ ;
        if ( url.endsWith(".ru") )      return syntaxSPARQL_11 ;
        if ( url.endsWith(".sse") )     return syntaxAlgebra ;
        return defaultSyntax ;
    }
    
    
    public static Syntax lookup(String s)
    {
        return querySyntaxNames.lookup(s) ;
    }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
