/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core;

import java.util.HashMap ;
import java.util.Iterator ;
import java.util.Map ;
import java.util.Map.Entry ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.lib.iterator.Iter ;
import com.hp.hpl.jena.sparql.util.Context ;

/** Implementation of a DatasetGraph as an open set of graphs.
 * New graphs are created (via the policy of a GraphMaker) when a getGraph call is 
 * made to a graph that has not been allocated.
 */
public class DatasetGraphOpen extends DatasetGraphBase
{
    public interface GraphMaker { public Graph create() ; }
    
    private static GraphMaker graphMakerNull = new GraphMaker() {
        public Graph create()
        {
            return null ;
        } } ;
    
    private Context context = new Context() ;
    private Map<Node, Graph> graphs = new HashMap<Node, Graph>() ;
    
    private Graph defaultGraph ;
    private GraphMaker graphMaker ;

    public DatasetGraphOpen(GraphMaker graphMaker)
    {
        this.graphMaker = graphMaker ;
        defaultGraph = graphMaker.create() ;
    }
    
    protected DatasetGraphOpen(Graph graph)
    {
        this.graphMaker = graphMakerNull ;
        defaultGraph = graph ;
    }

    @Override
    public boolean containsGraph(Node graphNode)
    {
        return true ;
    }

    @Override
    public Graph getDefaultGraph()
    {
        return defaultGraph ;
    }

    @Override
    public Graph getGraph(Node graphNode)
    {
        Graph g = graphs.get(graphNode) ;
        if ( g == null )
        {
            g = graphMaker.create() ;
            if ( g != null )
                graphs.put(graphNode, g) ;
        }
        return g ;
    }
    
    @Override
    public void add(Quad quad)
    {
        Graph g = fetchGraph(quad) ;
        g.add(quad.asTriple()) ;
    }
    
    protected Graph fetchGraph(Quad quad)
    {
        return fetchGraph(quad.getGraph()) ; 
    }
    
    protected Graph fetchGraph(Node gn)
    {
        if ( Quad.isDefaultGraph(gn))
            return getDefaultGraph() ;
        else
            return getGraph(gn) ;
    }

    @Override
    public void delete(Quad quad)
    {
        Graph g = fetchGraph(quad) ;
        g.delete(quad.asTriple()) ;
    }
    
//    @Override
//    public Iterator<Quad> find(Quad quad)
//    {
//        return find(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()) ;
//    }
    
    @Override
    public Iterator<Quad> find(final Node g, Node s, Node p , Node o)
    {
        if ( ! isWildcard(g) && ! Quad.isDefaultGraph(g))
        {
            Graph graph = ( Quad.isDefaultGraph(g) ? getDefaultGraph() : getGraph(g) ) ;
            return triples2quadsNamedGraph(g, graph.find(s, p, o)) ;
        }

        // Wildcard
        // Default graph
        Iter<Quad> iter = triples2quadsDftGraph(defaultGraph.find(s, p, o)) ;
        
        // Named graphs
        for ( final Entry<Node, Graph> e : graphs.entrySet() )
        {
            Iter<Quad> qIter = triples2quadsNamedGraph(e.getKey(), e.getValue().find(s, p, o)) ;
            iter = iter.append(qIter) ;
        }
        return iter ;
    }
    
//    @Override
//    public boolean isEmpty()
//    {
//        return contains(Node.ANY, Node.ANY, Node.ANY, Node.ANY) ;
//    }
//    
//    @Override
//    public boolean contains(Quad quad) { return contains(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()) ; }
//
//    @Override
//    public boolean contains(Node g, Node s, Node p , Node o)
//    {
//        Iterator<Quad> iter = find(g, s, p, o) ;
//        boolean b = iter.hasNext() ;
//        Iter.close(iter) ;
//        return b ;
//    }

    //@Override
    public Iterator<Node> listGraphNodes()
    {
        return graphs.keySet().iterator() ;
    }

    //@Override
    public int size()
    {
        return graphs.size() ;
    }

    @Override
    public void close()
    { 
        defaultGraph.close();
        for ( Graph graph : graphs.values() )
            graph.close();
        super.close() ;
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