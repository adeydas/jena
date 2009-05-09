/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.graph;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.AllCapabilities;
import com.hp.hpl.jena.graph.impl.GraphBase;
import com.hp.hpl.jena.graph.query.QueryHandler;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.engine.QueryEngineSDB;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.DatasetStoreGraph;
import com.hp.hpl.jena.sdb.store.StoreLoader;
import com.hp.hpl.jena.sdb.store.StoreLoaderPlus;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.Plan;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.NiceIterator;



public class GraphSDB extends GraphBase implements Graph
{
    private static Logger log = LoggerFactory.getLogger(GraphSDB.class) ;

    protected PrefixMapping pmap = null ;
    protected Store store = null ;
    
    // ARP buffers, which results in nested updates from our prespective
    protected int inBulkUpdate = 0 ;
    
    protected Node graphNode = Quad.defaultGraphNodeGenerated ;
    protected DatasetStoreGraph datasetStore = null ;
    
    public GraphSDB(Store store, String uri)
    { 
        this(store, Node.createURI(uri)) ;
        
    }
    
    public GraphSDB(Store store)
    { 
        this(store, (Node)null) ;
    }

    public GraphSDB(Store store, Node graphNode)
    {
        if ( graphNode == null )
            graphNode = Quad.defaultGraphNodeGenerated ;

        this.store = store ;
        this.graphNode = graphNode ;
        
        // Avoid looping here : DatasetStoreGraph can make GraphSDB's
        datasetStore = new DatasetStoreGraph(store, this) ;
        
        //readPrefixMapping() ;
    }
    
    /* We don't support value tests, hence handlesLiteralTyping is false */
    @Override
    public Capabilities getCapabilities()
    { 
    	if (capabilities == null)
            capabilities = new AllCapabilities()
        	  { @Override public boolean handlesLiteralTyping() { return false; } };
        return capabilities;
    }
    
    public Store getStore() { return store ; } 

    public SDBConnection getConnection() { return store.getConnection() ; }
    
    @Override
    public PrefixMapping getPrefixMapping()
    { 
        if ( pmap == null )
            try {
                String graphURI = null ;
                if ( Quad.isQuadDefaultGraphNode(graphNode) )
                    graphURI = "" ;
                else if ( graphNode.isURI() )
                    graphURI = graphNode.getURI() ; 
                else
                {
                    log.warn("Not a URI for graph name") ;
                    graphURI = graphNode.toString() ;
                }
                
                pmap = new PrefixMappingSDB(graphURI, store.getConnection()) ;
            } catch (Exception ex)
            { log.warn("Failed to get prefixes: "+ex.getMessage()) ; }
        return pmap ;
    }
    
    private Quad quad(TripleMatch m)
    {
        Node s = m.getMatchSubject() ;
        Var sVar = null ;
        if ( s == null )
        {
            sVar = Var.alloc("s") ;
            s = sVar ;
        }
        
        Node p = m.getMatchPredicate() ;
        Var pVar = null ;
        if ( p == null )
        {
            pVar = Var.alloc("p") ;
            p = pVar ;
        }
        
        Node o = m.getMatchObject() ;
        Var oVar = null ;
        if ( o == null )
        {
            oVar = Var.alloc("o") ;
            o = oVar ;
        }
        
        return new Quad(graphNode, s, p ,o) ;
    }

    @Override
    protected ExtendedIterator<Triple> graphBaseFind(TripleMatch m)
    {
        // Fake a query.
        SDBRequest cxt = new SDBRequest(getStore(), new Query()) ;
        
        // If null, create and remember a variable, else use the node.
        final Node s = (m.getMatchSubject()==null)   ? Var.alloc("s")   :  m.getMatchSubject() ;
        
        final Node p = (m.getMatchPredicate()==null) ? Var.alloc("p")   :  m.getMatchPredicate() ;
        final Node o = (m.getMatchObject()==null)    ? Var.alloc("o")   :  m.getMatchObject() ;

        Triple triple = new Triple(s, p ,o) ;
        
        // Evaluate as an algebra expression
        BasicPattern pattern = new BasicPattern() ;
        pattern.add(triple) ;
        Op op = new OpQuadPattern(graphNode, pattern) ;
        Plan plan = QueryEngineSDB.getFactory().create(op, datasetStore, BindingRoot.create(), null) ;
        
        QueryIterator qIter = plan.iterator() ;
        
        if ( SDB.getContext().isTrue(SDB.streamGraphAPI) )
        {
            // ---- Safe version: 
            List<Binding> bindings = new ArrayList<Binding>() ;
            while ( qIter.hasNext() ) bindings.add(qIter.nextBinding()) ;
            qIter.close();
            
            // QueryIterPlainWrapper is just to make it ia QuyerIterator again.
            return new GraphIterator(triple, new QueryIterPlainWrapper(bindings.iterator())) ;
        }
        else
        {
            // Dangerous version -- application must close iterator.
            return new GraphIterator(triple, qIter) ;
        }
    }

    // Collect ugliness together.
    private static Triple bindingToTriple(Triple pattern,  
                                          Binding binding)
    {
        Node s = pattern.getSubject() ;
        Node p = pattern.getPredicate() ;
        Node o = pattern.getObject() ;
        
        Node sResult = s ;
        Node pResult = p ;
        Node oResult = o ;
        
        if ( Var.isVar(s) )
            sResult = binding.get(Var.alloc(s)) ;
        if ( Var.isVar(p) )
            pResult = binding.get(Var.alloc(p)) ;
        if ( Var.isVar(o) )
            oResult = binding.get(Var.alloc(o)) ;
        
        Triple resultTriple = new Triple(sResult, pResult, oResult) ;
        return resultTriple ;
    }

    class GraphIterator extends NiceIterator<Triple>
    {
        QueryIterator qIter ; 
        Triple current = null ;
        Triple pattern ;
        
        GraphIterator(Triple pattern, QueryIterator qIter)
        { 
            this.qIter = qIter ;
            this.pattern = pattern ;
        }
        
        @Override
        public void close()
        {
            qIter.close() ;
        }
        
        @Override
        public boolean hasNext()
        {
            return qIter.hasNext() ;
        }
        
        @Override
        public Triple next()
        {
            return ( current = bindingToTriple(pattern, qIter.nextBinding() ) ) ;
        }

        @Override
        public void remove()
        { 
            if ( current != null )
                delete(current) ;
        }
    }
    
    public StoreLoader getBulkLoader() { return store.getLoader() ; }
    
    @Override
    public BulkUpdateHandler getBulkUpdateHandler()
    {
    	if (bulkHandler == null) bulkHandler = new UpdateHandlerSDB(this);
    	return bulkHandler;
    }
    
    @Override
    public QueryHandler queryHandler()
    {
        return new GraphQueryHandlerSDB(this, graphNode, datasetStore) ;
    }
    
    @Override
    public GraphEventManager getEventManager()
    {
    	if (gem == null) gem = new EventManagerSDB( this );
        return gem;
    }
    
    @Override
    public void performAdd( Triple triple )
    {
    	if (inBulkUpdate == 0) store.getLoader().startBulkUpdate();
        
        if ( Quad.isQuadDefaultGraphNode(graphNode) )
            store.getLoader().addTriple(triple) ;
        else
        {
            // XXX
            StoreLoaderPlus x = (StoreLoaderPlus)store.getLoader() ;
            x.addQuad(graphNode, triple.getSubject(), triple.getPredicate(), triple.getObject()) ;
        }
        if (inBulkUpdate == 0) store.getLoader().finishBulkUpdate();
    }
    
    @Override
    public void performDelete( Triple triple ) 
    {
    	if (inBulkUpdate == 0) store.getLoader().startBulkUpdate();
        if ( Quad.isQuadDefaultGraphNode(graphNode) )
            store.getLoader().deleteTriple(triple) ;
        else
        {
            // XXX
            StoreLoaderPlus x = (StoreLoaderPlus)store.getLoader() ;
            x.deleteQuad(graphNode, triple.getSubject(), triple.getPredicate(), triple.getObject()) ;
        }
        if (inBulkUpdate == 0) store.getLoader().finishBulkUpdate();
    }
    
    public void startBulkUpdate()  { inBulkUpdate += 1 ; if (inBulkUpdate == 1) store.getLoader().startBulkUpdate();}
    public void finishBulkUpdate() { inBulkUpdate -= 1 ; if (inBulkUpdate == 0) store.getLoader().finishBulkUpdate();}
    
    @Override
    public TransactionHandler getTransactionHandler() { return store.getConnection().getTransactionHandler() ; }
    
    @Override
    public int graphBaseSize() { return (int) (Quad.isQuadDefaultGraphNode(graphNode) ? store.getSize() : store.getSize(graphNode)); }
    
	public void deleteAll() {
		if (inBulkUpdate == 0) store.getLoader().startBulkUpdate();
		if ( Quad.isQuadDefaultGraphNode(graphNode) )
			store.getLoader().deleteAll();
		else
			((StoreLoaderPlus) store.getLoader()).deleteAll(graphNode);
		if (inBulkUpdate == 0) store.getLoader().finishBulkUpdate();
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