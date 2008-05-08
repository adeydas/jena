/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import static sdb.SDBCmd.sdbconfig;
import static sdb.SDBCmd.sdbload;
import static sdb.SDBCmd.sdbquery;
import static sdb.SDBCmd.setExitOnError;
import static sdb.SDBCmd.setSDBConfig;
import static sdb.SDBCmd.sparql;

import java.sql.Connection;
import java.util.Iterator;

import sdb.SDBCmd;
import arq.cmd.CmdUtils;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.iterator.Iter;
import com.hp.hpl.jena.sdb.modify.GraphStoreSDB;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBConnectionFactory;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.LayoutType;
import com.hp.hpl.jena.sdb.store.StoreConfig;
import com.hp.hpl.jena.sdb.store.StoreFactory;
import com.hp.hpl.jena.sdb.util.StrUtils;
import com.hp.hpl.jena.sparql.resultset.ResultsFormat;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.QueryExecUtils;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.util.FileManager;

public class RunSDB
{
    static { CmdUtils.setLog4j() ; CmdUtils.setN3Params() ; }
    
    @SuppressWarnings("unchecked")
    public static void main(String ... argv) 
    {
        {
            // SPARQL/Update
            Store store = SDB.createInMemoryStore() ;
            
            GraphStoreSDB gs = new GraphStoreSDB(store) ;
            UpdateAction.readExecute("update.ru", gs) ;
            
            @SuppressWarnings("unchecked")
            Iter<Node> iter = Iter.convert((Iterator<Node>)gs.listGraphNodes()) ;
            System.out.println(">>>");
            for ( Node n : iter)
                System.out.println(n);
            System.out.println("<<<");
            
            // Does not see new graph.
            SSE.write(gs.toDataset()) ;
//            
//            
//            SSE.write(gs.getDefaultGraph()) ;
//            IndentedWriter.stdout.println();
            System.out.println("-- Get named graph");
            SSE.write(gs.getGraph(Node.createURI("http://example/foo"))) ;
            IndentedWriter.stdout.println() ;
//            
            System.out.println("----");
            Dataset ds = SDBFactory.connectDataset(store) ;
            SSE.write(ds) ;
          System.out.println("----");
            System.exit(0) ;
        }
        runPrint() ;
        
        Store store = SDBFactory.connectStore("sdb.ttl") ;
//        Model model = SDBFactory.connectDefaultModel(store) ;
        Dataset ds = SDBFactory.connectDataset(store) ;

        Query query = QueryFactory.read("Q.rq") ;
        
        SDBConnection.logSQLQueries = true ;

        QuerySolutionMap qsol = new QuerySolutionMap() ;
        qsol.add("o", ResourceFactory.createPlainLiteral("abc")) ;
        QueryExecution qexec = QueryExecutionFactory.create(query, ds, qsol) ;
        ResultSetFormatter.out(qexec.execSelect()) ;
        qexec.close() ;
        System.exit(0) ;
    }
    
    public static void update()
    {
        String updateCmd = StrUtils.strjoin("\n",
            "DELETE { <http://en.wikipedia.org/wiki/Tony_Benn> <http://purl.org/dc/elements/1.1/publisher> ?all  }" ,
            "WHERE { <http://en.wikipedia.org/wiki/Tony_Benn>  <http://purl.org/dc/elements/1.1/publisher> ?all . }" ,
            "INSERT { <http://en.wikipedia.org/wiki/Tony_Benn> <http://purl.org/dc/elements/1.1/publisher> 'MyWikipedia'^^<http://www.w3.org/2001/XMLSchema#string>  }");

        Store store = StoreFactory.create("sdb.ttl") ;
        store.getTableFormatter().truncate() ;
        
        Model model = SDBFactory.connectNamedModel(store, "http://test.com/test.rdf") ;
        FileManager.get().readModel(model, "D.rdf") ;
        
        Dataset ds = SDBFactory.connectDataset(store) ;
        
        //Query query = QueryFactory.create("SELECT ?g ?s ?p ?o { GRAPH ?g {?s ?p ?o} }") ;
        
        if ( true )
        {
            System.out.println("After load: dump quads") ;
            Query query = QueryFactory.create("SELECT ?g ?s ?p ?o { GRAPH ?g {?s ?p ?o} }") ;
            QueryExecution qExec = QueryExecutionFactory.create(query, ds) ;
            ResultSetFormatter.out(qExec.execSelect()) ;
            qExec.close();
        }
        if ( true )
        {
            System.out.println("After load: dump model") ;
            Query query = QueryFactory.create("SELECT ?s ?p ?o { ?s ?p ?o }") ;
            QueryExecution qExec = QueryExecutionFactory.create(query, model) ;
            ResultSetFormatter.out(qExec.execSelect()) ;
            qExec.close();
        }
        
        GraphStore graphStore = GraphStoreFactory.create() ;
        graphStore.setDefaultGraph(model.getGraph()) ;
        UpdateAction.parseExecute(updateCmd, graphStore) ;
        
        if ( true )
        {
            System.out.println("After update: dump quads") ;
            Query query = QueryFactory.create("SELECT ?g ?s ?p ?o { GRAPH ?g {?s ?p ?o} }") ;
            QueryExecution qExec = QueryExecutionFactory.create(query, ds) ;
            ResultSetFormatter.out(qExec.execSelect()) ;
            qExec.close();
        }
        if ( true )
        {
            System.out.println("After update: dump model") ;
            Query query = QueryFactory.create("SELECT ?s ?p ?o { ?s ?p ?o }") ;
            QueryExecution qExec = QueryExecutionFactory.create(query, model) ;
            ResultSetFormatter.out(qExec.execSelect()) ;
            qExec.close();
        }
    }
    
    public static void cmds()
    {
        SDBCmd.setSDBConfig("sdb.ttl") ;
        sdbconfig("--create") ;
        sdbload("--graph=file:data1", "D1.ttl") ;
        sdbload("--graph=file:data2", "D2.ttl") ;
        sdbquery("--query=Q.rq") ;
        System.exit(0) ;
    }
    
    public static void devEnsureProject()
    {
        // Hit ensureProject?
        // Constructors to read file and get a model.
        Store store = SDBFactory.connectStore("sdb.ttl") ;
        Model model = SDBFactory.connectDefaultModel(store) ;
        System.out.println(model.contains(null, null, (RDFNode)null)) ;
        System.exit(0) ;
    }
    
//    public static void devSelectBlock()
//    {
//        SDB.getContext().setTrue(SDB.unionDefaultGraph) ;
//        Store store = StoreFactory.create(LayoutType.LayoutTripleNodesHash, DatabaseType.PostgreSQL) ;
//
//        Prologue prologue = new Prologue() ;
//        prologue.setPrefix("u", Quad.unionGraph.getURI()) ;
//        prologue.setPrefix("", "http://example/") ;
//        prologue.setBaseURI("http://example/") ;
//
//        //Op op = SSE.parseOp("(distinct (quadpattern [u: ?s :x ?o] [u: ?o :z ?z]))", prologue.getPrefixMapping()) ;
//        Op op = SSE.parseOp("(distinct (quadpattern [u: ?s :x ?o]))", prologue.getPrefixMapping()) ;
//        //System.out.println(op) ;
//        op = Compile.compile(store, op) ;
//        
//        SqlNode x = ((OpSQL)op).getSqlNode() ;
//        if ( false )
//        {
//            System.out.println(op) ;
//            System.out.println() ;
//        }
//
//        SqlNode x2 = SqlTransformer.transform(x, new TransformSelectBlock()) ;
//        System.out.println(x2) ;
//        System.out.println() ;
//
//        System.out.println(GenerateSQL.toPartSQL(x)) ;
//    }
//    
//    @SuppressWarnings("unchecked")
//    public static void devRename()
//    {
//        Store store = StoreFactory.create(LayoutType.LayoutTripleNodesHash, DatabaseType.PostgreSQL) ;
//        Prologue prologue = new Prologue() ;
//        prologue.setPrefix("u", Quad.unionGraph.getURI()) ;
//        prologue.setPrefix("", "http://example/") ;
//        prologue.setBaseURI("http://example/") ;
//        
//        //String qs = "PREFIX : <http://example/> SELECT * { ?s :x ?o }" ;
//        String qs = "PREFIX : <http://example/> SELECT * { ?s :x ?o . ?o :z ?z }" ;
//        
//        if ( false )
//        {
//            setSDBConfig("sdb.ttl") ;
//            sdbprint(qs) ;
//        }            
//        
//        Query query = QueryFactory.create(qs) ;
//        //Op op = AlgebraGeneratorQuad.compileQuery(query) ;
//        
//        //Op op = SSE.parseOp("(quadpattern [_ ?s :x ?o])", prologue.getPrefixMapping()) ;
//        Op op = SSE.parseOp("(quadpattern [u: ?s :x ?o] [u: ?o :z ?z])", prologue.getPrefixMapping()) ;
//        op = Compile.compile(store, op) ;
//        
//        SqlNode x = ((OpSQL)op).getSqlNode() ;
//        
////        if ( false )
////        {
////            System.out.println(x) ;
////            ScopeRename r1 = SqlRename.calc(x.getIdScope()) ;
////            System.out.println("Id: "+r1) ;
////            ScopeRename r2 = SqlRename.calc(x.getNodeScope()) ;
////            System.out.println("Node: "+r2) ;
////            return ;
////        }
//        
//        // Bug : puts all var/cols into the project
//        x = SqlRename.view("ZZ", x) ;
//        
////        System.out.println(x) ;
////        System.out.println() ;
//        
//        SqlNode x2 = SqlTransformer.transform(x, new TransformSelectBlock()) ;
//        System.out.println(x2) ;
//        System.out.println() ;
//        
//        System.out.println(GenerateSQL.toSQL(x)) ;
//        
//    }
    
    private static void devAssembler()
    {
        Dataset ds = DatasetFactory.assemble("dataset.ttl") ;
        ds.getDefaultModel().write(System.out, "TTL") ;
    }
    

    private static void _runQuery(String queryFile, String dataFile, String sdbFile)
        {

        // SDBConnection.logSQLStatements = false ;
         // SDBConnection.logSQLExceptions = true ;
         
         setSDBConfig(sdbFile) ;
         
         if ( dataFile != null  )
         {
             setExitOnError(true) ;
             sdbconfig("--create") ; 
             sdbload(dataFile) ;
         }
         //sdbprint("--print=plan", "--file=Q.rq") ; 
         sdbquery("--file=Q.rq") ;
     }

     public static void runInMem(String queryFile, String dataFile)
     {
         if ( true )
             sparql("--data="+dataFile, "--query="+queryFile) ;
         else
         {
             Query query = QueryFactory.read(queryFile) ;
             Model model = FileManager.get().loadModel(dataFile) ;
             QueryExecution qExec = QueryExecutionFactory.create(query, model) ;
             QueryExecUtils.executeQuery(query, qExec, ResultsFormat.FMT_TEXT) ;
         }
    }
    
    public static void runPrint()
    {
        runPrint("Q.rq") ;
        System.exit(0) ;
    }
    
    public static void runPrint(String filename)
    {
        //QueryCompilerBasicPattern.printAbstractSQL = true ;
        sdb.sdbprint.main("--print=sql", "--print=query", "--print=op", "--sdb=sdb.ttl", "--query="+filename) ;
        System.exit(0) ;
    }
    
   
    static void runScript()
    {
        String[] a = { } ;
        sdb.sdbscript.main(a) ;
    }
    
    public static void runConf()
    {
        JDBC.loadDriverHSQL() ;
        CmdUtils.setLog4j() ;
        
        String hsql = "jdbc:hsqldb:mem:aname" ;
        //String hsql = "jdbc:hsqldb:file:tmp/db" ;

        SDBConnection sdb = SDBFactory.createConnection(hsql, "sa", "");
        StoreConfig conf = new StoreConfig(sdb) ;
        
        Model m = FileManager.get().loadModel("Data/data2.ttl") ;
        conf.setModel(m) ;
        
        // Unicode: 03 B1"α"
        Model m2 = conf.getModel() ;
        
        if ( ! m.isIsomorphicWith(m2) )
            System.out.println("**** Different") ;
        else
            System.out.println("**** Same") ;
        
        conf.setModel(m) ;
        m2 = conf.getModel() ;
        
        m2.write(System.out, "N-TRIPLES") ;
        m2.write(System.out, "N3") ;
        System.exit(0) ;
    }
    
    public static void run()
    {
        
        
        
        try {
            Class.forName("com.ibm.db2.jcc.DB2Driver") ;
            String url = "jdbc:db2://sweb-sdb-4:50000/TEST" ;
                
            Connection conn = JDBC.createConnection(url, "user", "password") ;
            System.out.println("Happy") ;
        
        } catch (Exception ex)
        {
            ex.printStackTrace(System.err) ;
        }
        
        System.exit(0) ;
//        setSDBConfig("Store/sdb-hsqldb-mem.ttl") ;
//        sdbconfig("--create") ;
//        sdbload("D.ttl") ;

        //sparql("--data="+DIR+"data.ttl","--query="+DIR+"struct-10.rq") ;
        //ARQ.getContext().setFalse(StageBasic.altMatcher) ;
        Store store = SDBFactory.connectStore("Store/sdb-hsqldb-mem.ttl") ;
        store.getTableFormatter().create() ;
        Model model = SDBFactory.connectDefaultModel(store) ;
        FileManager.get().readModel(model, "D.ttl") ;
        Query query = QueryFactory.read("Q.rq") ;
        QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
        QueryExecUtils.executeQuery(query, qexec) ;
        qexec.close() ;
        System.exit(0) ;
        
    }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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