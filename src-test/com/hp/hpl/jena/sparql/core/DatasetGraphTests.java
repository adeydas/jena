/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core;
import static org.junit.Assert.assertEquals ;
import static org.junit.Assert.assertFalse ;
import static org.junit.Assert.assertNotNull ;
import static org.junit.Assert.assertTrue ;

import java.util.Iterator ;
import java.util.Set ;

import org.junit.Test ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.lib.iterator.Iter ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory ;

public abstract class DatasetGraphTests
{
    abstract DatasetGraph emptyDataset() ;
    
    @Test public void create_1()
    {
        DatasetGraph dsg = emptyDataset() ;
        assertNotNull(dsg) ;
        assertNotNull(dsg.getDefaultGraph()) ;
        assertTrue(dsg.getDefaultGraph().isEmpty()) ;
    }
    
    // Quad operations 
    /*
     * void add(Quad quad)
     * void delete(Quad quad)
     * void deleteAny(Node g, Node s, Node p, Node o)
     * Iterator<Quad> find(Quad quad)
     * Iterator<Quad> find(Node g, Node s, Node p , Node o)
     * boolean contains(Node g, Node s, Node p , Node o)
     * boolean contains(Quad quad)
     */
    @Test public void quad_01()
    {
        DatasetGraph dsg = emptyDataset() ;
        assertNotNull(dsg) ;
        Quad quad = SSE.parseQuad("(quad <g> <s> <p> <o>)") ;
        dsg.add(quad) ;
        assertTrue(dsg.contains(quad)) ;
        
        Iterator<Quad> iter = dsg.find(quad) ;
        assertTrue(iter.hasNext()) ;
        Quad quad2 = iter.next();
        assertFalse(iter.hasNext()) ;
        assertEquals(quad, quad2) ;
        
        // and the graph view.
        assertTrue(dsg.getDefaultGraph().isEmpty()) ;
        assertFalse(dsg.getGraph(Node.createURI("g")).isEmpty()) ;
    }
    
    @Test public void quad_02()
    {
        DatasetGraph dsg = emptyDataset() ;
        assertNotNull(dsg) ;
        Quad quad = SSE.parseQuad("(quad <g> <s> <p> <o>)") ;
        dsg.add(quad) ;
        dsg.containsGraph(Node.createURI("g")) ;
        
        dsg.delete(quad) ;
        assertTrue(dsg.isEmpty()) ;
        assertTrue(dsg.getDefaultGraph().isEmpty()) ;
        assertTrue(dsg.getGraph(Node.createURI("g")).isEmpty()) ;
    }
    
    @Test public void quad_03()
    {
        DatasetGraph dsg = emptyDataset() ;
        assertNotNull(dsg) ;
        Quad quad1 = SSE.parseQuad("(quad <g> <s> <p> <o1>)") ;
        Quad quad2 = SSE.parseQuad("(quad <g> <s> <p> <o2>)") ;
        dsg.add(quad1) ; 
        dsg.add(quad2) ; 
        
        dsg.deleteAny(Node.createURI("g"), Node.createURI("s"), null, null) ;
        assertFalse(dsg.contains(quad1)) ; 
        assertFalse(dsg.contains(quad2)) ; 
    }
    
    @Test public void quad_04()
    {
        DatasetGraph dsg = emptyDataset() ;
        assertNotNull(dsg) ;
        Quad quad1 = SSE.parseQuad("(quad <g> <s> <p> <o1>)") ;
        Quad quad2 = SSE.parseQuad("(quad <g> <s> <p> <o2>)") ;
        dsg.add(quad1) ; 
        dsg.add(quad2) ; 
        Iterator<Quad> iter = dsg.find(Node.createURI("g"), Node.createURI("s"), null, null) ;

        assertTrue(iter.hasNext()) ;
        Set<Quad> x = Iter.iter(iter).toSet() ;
        assertEquals(2, x.size()) ;
        assertTrue(x.contains(quad1)) ;
        assertTrue(x.contains(quad2)) ;
    }

    /*
     * getDefaultGraph()
     * getGraph(Node) 
     * containsGraph(Node)
     * ???? setDefaultGraph(Graph) 
     * addGraph(Node, Graph)
     * removeGraph(Node)
     * listGraphNodes()
     */
    // Graph centric operations
    @Test public void graph_01()
    {
        DatasetGraph dsg = emptyDataset() ;
        assertNotNull(dsg) ;
        Node g = Node.createURI("g") ;
        
        dsg.addGraph(g, GraphFactory.createDefaultGraph());
        assertTrue(dsg.containsGraph(g)) ;
        Triple t = SSE.parseTriple("(<s> <p> <o>)") ;
        
        dsg.getGraph(g).add(t) ;
        assertTrue(dsg.getGraph(g).contains(t)) ;

        
        Quad quad = SSE.parseQuad("(quad <g> <s> <p> <o>)") ;
        Iterator<Quad> iter = dsg.find(null, null, null, null) ;
        
        assertTrue(iter.hasNext()) ;
        Quad quad2 = iter.next();
        assertFalse(iter.hasNext()) ;
        assertEquals(quad, quad2) ;
        
        assertTrue(dsg.getDefaultGraph().isEmpty()) ;
        assertFalse(dsg.getGraph(Node.createURI("g")).isEmpty()) ;
    }

    @Test public void graph_02()
    {
        Node g = Node.createURI("g") ;
        DatasetGraph dsg = emptyDataset() ;
        assertNotNull(dsg) ;
        Quad quad = SSE.parseQuad("(quad <g> <s> <p> <o>)") ;
        dsg.add(quad) ;
        
        Triple t = SSE.parseTriple("(<s> <p> <o>)") ;
        dsg.getGraph(g).delete(t) ;
        assertTrue(dsg.getDefaultGraph().isEmpty()) ;
        assertTrue(dsg.getGraph(Node.createURI("g")).isEmpty()) ;
        assertFalse(dsg.find(Node.ANY, Node.ANY, Node.ANY, Node.ANY).hasNext()) ; 
    }
    
    @Test public void graph_03()
    {
        Node g = Node.createURI("g") ;
        DatasetGraph dsg = emptyDataset() ;
        Graph data = SSE.parseGraph("(graph (<s> <p> <o>))") ;
        dsg.addGraph(g, data) ;
        Quad quad = SSE.parseQuad("(quad <g> <s> <p> <o>)") ;
        assertTrue(dsg.contains(quad)) ;
    }
    
    // ---- Dataset level tests
    
//    @Test public void x()
//    {
//        DatasetGraph dsg = emptyDataset() ;
//        Dataset ds = new DatasetImpl(dsg) ; // DataSourceImpl
//    }
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