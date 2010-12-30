/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.modify;

import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.iterator.Transform ;
import org.openjena.atlas.lib.MultiMap ;
import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.core.Substitute ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;

public class TemplateLib
{
    // See also Substitute -- combine?
    // Or is this specifc enough to CONSTRUCT/Update template processing? 
    
    /** Take a template, as a list of quad patterns, a default graph, and a list of bindings,
     *  and produce a map of graph name to lists of triples.
     */
    public static MultiMap<Node, Triple> template(List<Quad> quads, final Node dftGraph, List<Binding> bindings)
    {
        if ( quads == null || quads.isEmpty() ) return null ; 
        // The default graph has been set to something else.
        if ( dftGraph != null )
        {
            Transform<Quad, Quad> nt = new Transform<Quad, Quad>() {
                public Quad convert(Quad quad)
                {
                    if ( ! quad.isDefaultGraph() ) return quad ;
                    
                    return new Quad(dftGraph, quad.getSubject(), quad.getPredicate(), quad.getObject()) ;
                }
            };
            quads = Iter.map(quads, nt) ;
        }
        
        MultiMap<Node, Triple> acc = calcTriples(quads, bindings) ;
        return acc ;
    }
    
//    /** Take a template, as a list of quad patterns, but only on one graph,  
//     *  a default graph, and a list of bindings,
//     *  and produce a lists of triples.
//     */
//    public static List<Triple> templateTriples(List<Quad> quads, List<Binding> bindings)
//    {
//        if ( quads == null || quads.isEmpty() ) return null ; 
//        MultiMap<Node, Triple> acc = calcTriples(quads, bindings) ;
//        return acc ;
//    }

    /** Substitute into quad patterns, and build a map of graph name to lists of triples */
    public static MultiMap<Node, Triple> calcTriples(List<Quad> quads, List<Binding> bindings)
    {
        QueryIterator qIter = new QueryIterPlainWrapper(bindings.iterator()) ;
        return subst(quads, qIter) ;
    }

    /** Substitute into quad patterns, and build a map of graph name to lists of triples */
    protected static MultiMap<Node, Triple> subst(List<Quad> quads, QueryIterator qIter)
    {
        MultiMap<Node, Triple> acc = MultiMap.createMapList() ;
    
        for ( ; qIter.hasNext() ; )
        {
            Map<Node, Node> bNodeMap = new HashMap<Node, Node>() ;
            Binding b = qIter.nextBinding() ;
            for ( Quad quad : quads )
                subst(acc, quad, b, bNodeMap) ;
        }
        return acc ;
    }

    static void subst(MultiMap<Node, Triple> acc, Quad quad, Binding b, Map<Node, Node> bNodeMap)
    {
        Quad q = subst(quad, b, bNodeMap) ;
        if ( ! q.isConcrete() )
        {
            Log.warn(UpdateEngine.class, "Unbound quad: "+FmtUtils.stringForQuad(quad)) ;
            return ;
        }
        acc.put(q.getGraph(), q.asTriple()) ;
    }

    /** Substitute into a quad, with rewriting of bNodes */ 
    public static Quad subst(Quad quad, Binding b, Map<Node, Node> bNodeMap)
    {
        Node g = quad.getGraph() ;
        Node s = quad.getSubject() ; 
        Node p = quad.getPredicate() ;
        Node o = quad.getObject() ;
    
        Node g1 = g ;
        Node s1 = s ; 
        Node p1 = p ;
        Node o1 = o ;
        
        // replace blank nodes. 
        if ( g1.isBlank() || Var.isBlankNodeVar(g1) )
            g1 = newBlank(g1, bNodeMap) ;
        
        if ( s1.isBlank() || Var.isBlankNodeVar(s1) )
            s1 = newBlank(s1, bNodeMap) ;
    
        if ( p1.isBlank() || Var.isBlankNodeVar(p1) )
            p1 = newBlank(p1, bNodeMap) ;
    
        if ( o1.isBlank() || Var.isBlankNodeVar(o1) )
            o1 = newBlank(o1, bNodeMap) ;
    
        Quad q = quad ;
        if ( s1 != s || p1 != p || o1 != o || g1 != g )
            q = new Quad(g1, s1, p1, o1) ;
        
        Quad q2 = Substitute.substitute(q, b) ;
        return q2 ;
    }
    
    /** Substitute into a triple, with rewriting of bNodes */ 
    public static Triple subst(Triple triple, Binding b, Map<Node, Node> bNodeMap)
    {
        Node s = triple.getSubject() ; 
        Node p = triple.getPredicate() ;
        Node o = triple.getObject() ;
    
        Node s1 = s ; 
        Node p1 = p ;
        Node o1 = o ;
        
        if ( s1.isBlank() || Var.isBlankNodeVar(s1) )
            s1 = newBlank(s1, bNodeMap) ;
    
        if ( p1.isBlank() || Var.isBlankNodeVar(p1) )
            p1 = newBlank(p1, bNodeMap) ;
    
        if ( o1.isBlank() || Var.isBlankNodeVar(o1) )
            o1 = newBlank(o1, bNodeMap) ;
    
        Triple t = triple ;
        if ( s1 != s || p1 != p || o1 != o )
            t = new Triple(s1, p1, o1) ;
        
        Triple t2 = Substitute.substitute(t, b) ;
        return t2 ;
    }

    /** generate a blank node consistently */
    private static Node newBlank(Node n, Map<Node, Node> bNodeMap)
    {
        if ( ! bNodeMap.containsKey(n) ) 
            bNodeMap.put(n, Node.createAnon() );
        return bNodeMap.get(n) ;
    }
}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
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