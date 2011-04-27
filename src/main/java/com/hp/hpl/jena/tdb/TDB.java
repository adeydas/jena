/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb;

import java.util.Iterator ;

import org.openjena.atlas.lib.Sync ;
import org.openjena.riot.SysRIOT ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.SystemARQ ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.assembler.AssemblerUtils ;
import com.hp.hpl.jena.sparql.engine.main.QC ;
import com.hp.hpl.jena.sparql.engine.main.StageBuilder ;
import com.hp.hpl.jena.sparql.engine.main.StageGenerator ;
import com.hp.hpl.jena.sparql.lib.Metadata ;
import com.hp.hpl.jena.sparql.mgt.ARQMgt ;
import com.hp.hpl.jena.sparql.mgt.SystemInfo ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.Symbol ;
import com.hp.hpl.jena.tdb.assembler.AssemblerTDB ;
import com.hp.hpl.jena.tdb.modify.UpdateEngineTDB ;
import com.hp.hpl.jena.tdb.solver.OpExecutorTDB ;
import com.hp.hpl.jena.tdb.solver.QueryEngineTDB ;
import com.hp.hpl.jena.tdb.solver.StageGeneratorDirectTDB ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.sys.EnvTDB ;
import com.hp.hpl.jena.tdb.sys.SetupTDB ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;
import com.hp.hpl.jena.tdb.sys.TDBMaker ;

public class TDB
{
    /** IRI for TDB */  
    public static final String tdbIRI = "http://jena.hpl.hp.com/#tdb" ;
    
    // Internal logging
    private static final Logger log = LoggerFactory.getLogger(TDB.class) ;

    /** Logger for general information */ 
    public static final String logInfoName = "com.hp.hpl.jena.tdb.info" ;
    /** Logger for general information */ 
    public static final Logger logInfo = LoggerFactory.getLogger(logInfoName) ;
    
    /** Logger for loading information */
    public static final String logLoaderName = "com.hp.hpl.jena.tdb.loader" ;
    /** Logger for loading information */
    public static final Logger logLoader = LoggerFactory.getLogger(logLoaderName) ;
    
//    /** Logger for execution information */
//    public static final String logExecName = "com.hp.hpl.jena.tdb.exec" ;
//    /** Logger for execution information */
//    public static final Logger logExec = LoggerFactory.getLogger(logExecName) ;
    
    public final static String namespace = "http://jena.hpl.hp.com/2008/tdb#" ;

    /** Symbol to use the union of named graphs as the default graph of a query */ 
    public static final Symbol symUnionDefaultGraph          = SystemTDB.allocSymbol("unionDefaultGraph") ;
    
    /** Symbol to use the union of named graphs as the default graph of a query
     * @deprecated Use ARQ.symLogExec instead.
     */
    @Deprecated
    public static final Symbol symLogExec          = ARQ.symLogExec ;
    
    public static Context getContext()     { return ARQ.getContext() ; }  
    
    // Called on assembler loading.
    // Real initializtion happnes due to class static blocks.
    /** TDB System initialization - normally, this is not explicitly called because
     * all routes to use TDB will cause initialization to occur.  However, calling it
     * repeatedly is safe and low cost.
     */
    public static void init() { }
    
    /** Release any and all system resources held by TDB.
     *  This does NOT close or release datasets or graphs held by client code. 
     */
    public static void closedown()
    {
        TDBMaker.clearDatasetCache() ;
    }
    
    /** Set the global flag that control the "No BGP optimizer" warning.
     * Set to false to silence the warning
     */
    public static void setOptimizerWarningFlag(boolean b)
    { SetupTDB.setOptimizerWarningFlag(b) ; }
    
    // ** Call SystemARQ.sync 
    
    /** Sync a TDB-backed Model. Do nothing if not TDB-backed. */
    public static void sync(Model model)
    {
        sync(model.getGraph()) ;
    }
    
    /** Sync a TDB-backed Graph. Do nothing if not TDB-backed. */
    public static void sync(Graph graph)
    {
        syncObject(graph) ;
    }

    /** Sync a TDB-backed Dataset. Do nothing if not TDB-backed. */
    public static void sync(Dataset dataset)
    { 
        DatasetGraph ds = dataset.asDatasetGraph() ;
        sync(ds) ;
    }
    
    /** Sync a TDB-backed DatasetGraph. Do nothing if not TDB-backed. */
    public static void sync(DatasetGraph dataset)
    { 
        // Should be: SystemARQ.sync(dataset) ;
        if ( dataset instanceof DatasetGraphTDB )
            syncObject(dataset) ;
        else
        {
            // May be a general purpose dataset with TDB objects in it.
            Iterator<Node> iter = dataset.listGraphNodes() ;
            for ( ; iter.hasNext() ; )
            {
                Node n = iter.next();
                Graph g = dataset.getGraph(n) ;
                sync(g) ;
            }
        }
    }
    
    /** Sync a TDB synchronizable object (model, graph, dataset). 
     *  If force is true, synchronize as much as possible (e.g. file metadata)
     *  else make a reasonable attenpt at synchronization but does not gauarantee disk state. 
     * Do nothing otherwise 
     */
    private static void syncObject(Object object)
    {
        if ( object instanceof Sync )
            ((Sync)object).sync() ;
    }

    private static boolean initialized = false ;
    static { initialization1() ; }
    
    private static synchronized void initialization1()
    {
        // Called at start.
        if ( initialized )
            return ;
        initialized = true ;
     
        SysRIOT.wireIntoJena() ;
        SystemTDB.init() ;
        ARQ.init() ;
        EnvTDB.processGlobalSystemProperties() ;
        
        AssemblerUtils.init() ;
        AssemblerTDB.init();
        QueryEngineTDB.register() ;
        UpdateEngineTDB.register() ;

        wireIntoExecution() ;
        
        // This does not work with the conncurrency policy
        // Instead, assume all open files (direct and memory mapped) are sync'ed by the OS. 
//        // Attempt to sync everything on exit.
//        // This can not be guaranteed.
//        Runnable runnable = new Runnable() {
//            public void run()
//            { try { TDBMaker.syncDatasetCache() ; } catch (Exception ex) {} }
//        } ;
//        Runtime.getRuntime().addShutdownHook(new Thread(runnable)) ;
        
        if ( log.isDebugEnabled() )
            log.debug("\n"+ARQ.getContext()) ;
    }

    private static void wireIntoExecution()
    {
        // TDB does it itself.
        TDB.getContext().set(ARQ.optFilterPlacement, false) ;
        // Globally change the stage generator to intercept BGP on TDB
        StageGenerator orig = (StageGenerator)ARQ.getContext().get(ARQ.stageGenerator) ;
        
        // Wire in the TDB stage generator which will make TDB work whether
        // or not the TDB executor is used. This means that datasets of mixed graph
        // types inside a general purpose dataset work.
        StageGenerator stageGenerator = new StageGeneratorDirectTDB(orig) ;
        StageBuilder.setGenerator(ARQ.getContext(), stageGenerator) ;

        // Wire in the new OpExecutor.  This is normal way to execute with a dataset.
        QC.setFactory(ARQ.getContext(), OpExecutorTDB.OpExecFactoryTDB) ;
    }
    
    // ---- Static constants read by modVersion
    // ---- Must be after initialization.
    
    static private String metadataLocation = "com/hp/hpl/jena/tdb/tdb-properties.xml" ;
    static private Metadata metadata = new Metadata(metadataLocation) ;
    
    /** The root package name for TDB */   
    public static final String PATH = "com.hp.hpl.jena.tdb";

    // The names known to ModVersion : "NAME", "VERSION", "BUILD_DATE"
    
    public static final String NAME = "TDB" ;
    
    /** The full name of the current TDB version */   
    public static final String VERSION = metadata.get(PATH+".version", "DEV") ;

    /** The date and time at which this release was built */   
    public static final String BUILD_DATE = metadata.get(PATH+".build.datetime", "unset") ;
    
    // Final initialization (in case any statics in this file are important). 
    static {
        initialization2() ;
    }

    private static void initialization2()
    { 
        // Set management information.
        // Needs ARQ > 2.8.0
        String NS = TDB.PATH ;
        SystemInfo systemInfo = new SystemInfo(TDB.tdbIRI, TDB.VERSION, TDB.BUILD_DATE) ;
        ARQMgt.register(NS+".system:type=SystemInfo", systemInfo) ;
        SystemARQ.registerSubSystem(systemInfo) ;
    }
    
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
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