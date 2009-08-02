package dev;
import java.util.Iterator;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;

public class ReportTDB
{
    public static void main(String[] args) throws Exception
    {
        report() ; System.exit(0) ;
        
        Dataset ds = TDBFactory.createDataset();
        Model m1 = ds.getNamedModel("http://example/g1")  ;
        
        Graph g1 = ds.asDatasetGraph().getGraph(Node.createURI("http://example/g1")) ;
        g1.add(SSE.parseTriple("(<x> <p> 1)")) ;
        
        Graph g2 = ds.asDatasetGraph().getGraph(Node.createURI("http://example/g2")) ;
        g2.add(SSE.parseTriple("(<x> <p> 2)")) ;
        Model m2 = ds.getNamedModel("http://example/g2")  ;
        TDB.sync(ds) ;
        
        execContains(ds) ;
    }
 
    public static void report()
    {
        DataSource ds = DatasetFactory.create() ;
        Model m = ModelFactory.createDefaultModel() ;
        m.getGraph().add(SSE.parseTriple("(<x> <p> 1)")) ;
        ds.addNamedModel("http://example/g1", m) ;

        // Internal debugging code - show there is some data.
//        System.out.println("Dataset ds") ;
//        WriterSSE.out(IndentedWriter.stdout, ds.asDatasetGraph(), null) ;
//        System.out.println("Model") ;
//        WriterSSE.out(IndentedWriter.stdout, ds.getNamedModel("http://example/g1").getGraph(), null) ;

        
        Query query2 = QueryFactory.create("SELECT * FROM NAMED <http://example/g1> { ?s ?p ?o }"); 
        System.out.println("FROM NAMED Datasource, union set true") ;
        System.out.println();
        Dataset ds2 = TDBFactory.createDataset(); 
        for (Iterator<String> iterator = query2.getNamedGraphURIs().iterator(); iterator.hasNext();) { 
            String name = iterator.next(); 
            System.out.println("adding " + name); 
            Model model = ds2.getNamedModel(name); 
            System.out.println("isEmpty => "+model.isEmpty());
            ds.getNamedModel(name).write(System.out, "TTL") ;
            model.add(ds.getNamedModel(name).listStatements()); 
            TDB.sync(ds2); 
            System.out.println("isEmpty => "+model.isEmpty());
            System.out.println();
        } 
        
        QueryExecution qExec = QueryExecutionFactory.create(query2, ds2) ; 
        qExec.getContext().set(TDB.symUnionDefaultGraph, true) ; 
        ResultSetFormatter.out(qExec.execSelect()) ; 
        qExec.close() ; 
    }
    
    public static void execContains(Dataset ds) throws Exception
    {       
        System.out.println(ds.containsNamedModel("http://example/g1")) ;
        System.out.println(ds.asDatasetGraph().containsGraph(Node.createURI("http://example/g1"))) ;
        //g1.getBulkUpdateHandler().removeAll() ;
        //g1.delete(SSE.parseTriple("(<x> <p> 1)")) ;
        Model m1 = ds.getNamedModel("http://example/g1")  ;
        m1.removeAll() ;
        
        System.out.println(ds.containsNamedModel("http://example/g1")) ;
        System.out.println(ds.asDatasetGraph().containsGraph(Node.createURI("http://example/g1"))) ;
    }
        
    public static void execSymUnion(Dataset ds) throws Exception
    {
        Query query = QueryFactory.create("SELECT * FROM NAMED <http://example/named> { ?s ?p ?o }"); 
        
        System.out.println("No union set") ;
        QueryExecution qExec = QueryExecutionFactory.create(query, ds) ;
        ResultSetFormatter.out(qExec.execSelect()) ;
        qExec.close() ;
        
        System.out.println("Per-exec union set true") ;
        qExec = QueryExecutionFactory.create(query, ds) ;
        qExec.getContext().set(TDB.symUnionDefaultGraph, true) ;
        ResultSetFormatter.out(qExec.execSelect()) ;
        qExec.close() ;
        
        System.out.println("No union set") ;
        qExec = QueryExecutionFactory.create(query, ds) ;
        ResultSetFormatter.out(qExec.execSelect()) ;
        qExec.close() ;
        
        System.out.println("Global union set true") ;
        TDB.getContext().set(TDB.symUnionDefaultGraph, true) ;
        qExec = QueryExecutionFactory.create(query, ds) ;
        ResultSetFormatter.out(qExec.execSelect()) ;
        qExec.close() ;
        
        System.out.println("Per-exec union set false") ;
        qExec = QueryExecutionFactory.create(query, ds) ;
        qExec.getContext().set(TDB.symUnionDefaultGraph, false) ;
        ResultSetFormatter.out(qExec.execSelect()) ;
        qExec.close() ;
        
        System.out.println("Global set false") ;
        TDB.getContext().set(TDB.symUnionDefaultGraph, false) ;
        qExec = QueryExecutionFactory.create(query, ds) ;
        ResultSetFormatter.out(qExec.execSelect()) ;
        qExec.close() ;

        System.out.println("Global unset") ;
        TDB.getContext().remove(TDB.symUnionDefaultGraph) ;
        qExec = QueryExecutionFactory.create(query, ds) ;
        ResultSetFormatter.out(qExec.execSelect()) ;
        qExec.close() ;
        
        System.exit(0) ;

    }
}


