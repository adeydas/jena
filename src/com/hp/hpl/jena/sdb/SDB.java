/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb;

import com.hp.hpl.jena.assembler.assemblers.AssemblerGroup;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.sdb.assembler.AssemblerVocab;
import com.hp.hpl.jena.sdb.core.SDBConstants;
import com.hp.hpl.jena.sdb.engine.QueryEngineSDB;
import com.hp.hpl.jena.sdb.modify.UpdateProcessorSDB;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.LayoutType;
import com.hp.hpl.jena.sdb.util.DerbyUtils;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.core.assembler.AssemblerUtils;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.util.Symbol;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;


public class SDB
{
    /** SDB namespace */
    
    public final static String namespace = "http://jena.hpl.hp.com/2007/sdb#" ;
    public final static String symbolPrefix = "sdb" ;
    public final static String symbolSpace = "http://jena.hpl.hp.com/SDB/symbol#" ;

    
    // ----------------------------------
    public static final Symbol useQuadRewrite           = SDBConstants.allocSymbol("useQuadRewrite") ;
    public static final Symbol unionDefaultGraph        = SDBConstants.allocSymbol("unionDefaultGraph") ;

    /** Control whether GraphSDB streams results to find - must ensure all Jena iterators are closed if this is set true */
    public static final Symbol streamGraphAPI           = SDBConstants.allocSymbol("streamGraphAPI") ;
    
    /** Control/attempt  JDBC streaming - mosty databases allow only one outstanding streaming request */  
    public static final Symbol jdbcStream               = SDBConstants.allocSymbol("jdbcStream") ;
    public static final Symbol jdbcFetchSize            = SDBConstants.allocSymbol("jdbcFetchSize") ;
    // See also SDBConstants.jdbcFetchSizeOff
    
    public static final Symbol annotateGeneratedSQL     = SDBConstants.allocSymbol("annotateGeneratedSQL") ;
    // ----------------------------------
    
    // Global context is the ARQ context.
    public static Context getContext() { return ARQ.getContext() ; }
    
    static { initWorker() ; }
    public static void init() { }
    
    /** Used by Jena assemblers for registration */ 
    public static void whenRequiredByAssembler( AssemblerGroup g )
    {
        AssemblerUtils.init() ;         // ARQ 
        AssemblerVocab.register(g) ;    // SDB
    }
    
    private static boolean initialized = false ;
    private static synchronized void initWorker()
    {
        // Called from 
        // + StoreFactory
        // + DatasetStore
        // Commands call AssemblerVocab.init() ;

        if ( initialized )
            return ;
        
        // Set this immediately in case code below causes init() to be called.
        // (It's better if there are no dependences but ...)
        initialized = true ;
        
        // Better not to break up BGPs too much.
        ARQ.getContext().set(ARQ.filterPlacement, false) ;
        
        // Default is 1000 4Kpages.
        DerbyUtils.setDerbyPageCacheSize(10000) ;
        
        // Wire in the SDB query engine
        QueryEngineSDB.register() ;
        // Wire in the SDB update engine 
        UpdateProcessorSDB.register() ;
        
        SDB.getContext().setIfUndef(useQuadRewrite,        false) ;
        SDB.getContext().setIfUndef(streamGraphAPI,        false) ;
        SDB.getContext().setIfUndef(jdbcStream,            true) ;
        //SDB.getContext().setIfUndef(jdbcFetchSize,         ???) ;
        SDB.getContext().setIfUndef(annotateGeneratedSQL,  true) ;
        //SDB.getContext().setIfUndef(unionDefaultGraph,     false) ;
    }
    
    /** RDF namespace prefix */
    private static final String rdfPrefix = RDF.getURI() ;

    /** RDFS namespace prefix */
    private static final String rdfsPrefix = RDFS.getURI() ;

    /** OWL namespace prefix */
    private static final String owlPrefix = OWL.getURI() ;
    
    /** XSD namespace prefix */
    private static final String xsdPrefix = XSDDatatype.XSD+"#" ;
    
//    /** The namespace of the XML results format */ 
//    private static final String srxPrefix = "http://www.w3.org/2005/sparql-results#" ;
    
    protected static PrefixMapping globalPrefixMap = new PrefixMappingImpl() ;
    static {
        globalPrefixMap.setNsPrefix("rdf",  rdfPrefix) ;
        globalPrefixMap.setNsPrefix("rdfs", rdfsPrefix) ;
        globalPrefixMap.setNsPrefix("xsd",  xsdPrefix) ;
        globalPrefixMap.setNsPrefix("owl" , owlPrefix) ;
        globalPrefixMap.setNsPrefix("sdb" , namespace) ;
    }
    public static PrefixMapping getGlobalPrefixMapping() { return globalPrefixMap ; }
    
    
    public static Symbol allocSymbol(String shortName)
    { 
        if ( shortName.startsWith(ARQ.arqNS)) 
            throw new ARQInternalErrorException("Symbol short name begins with the ARQ namespace name: "+shortName) ;
        return Symbol.create(ARQ.arqNS+shortName) ;
    }
    

    /** Create an H2-backed in-memory store for testing. */
    public static Store createInMemoryStore()
    {
        // Create an H2-backed in-memory store for testing.
        SDBConnection conn = SDBFactory.createConnection("jdbc:h2:mem:", "", "") ;
        StoreDesc desc = new StoreDesc(LayoutType.LayoutTripleNodesHash, DatabaseType.H2) ;
        Store store = SDBFactory.connectStore(conn, desc) ;
        store.getTableFormatter().create() ;
        return  store ;
    }
    
    // ----------------------------------
    
    /** The root package name for SDB */   
    public static final String PATH = "com.hp.hpl.jena.sdb";
   
    /** The product name */   
    public static final String NAME = "@name@";
   
    /** The SDB web site : see also http://jena.sourceforge.net*/   
    public static final String WEBSITE = "@website@";
   
    /** The full name of the current SDB version */   
    public static final String VERSION = "@version@";
   
    /** The major version number for this release of SDB (ie '2' for SDB 2.0) */
    public static final String MAJOR_VERSION = "@version-major@";
   
    /** The minor version number for this release of SDB (ie '0' for SDB 2.0) */
    public static final String MINOR_VERSION = "@version-minor@";
   
    /** The version status for this release of SDB (eg '-beta1' or the empty string) */
    public static final String VERSION_STATUS = "@version-status@";
   
    /** The date and time at which this release was built */   
    public static final String BUILD_DATE = "@build-time@";
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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