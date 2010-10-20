/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.path;

import java.util.ArrayList ;
import java.util.Collection ;
import java.util.HashSet ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Set ;

import org.openjena.atlas.iterator.Filter ;
import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.iterator.Transform ;
import org.openjena.atlas.logging.Log ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.NodeIterator ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.rdf.model.impl.NodeIteratorImpl ;
import com.hp.hpl.jena.sparql.util.ModelUtils ;

public class PathEval
{
    static private Logger log = LoggerFactory.getLogger(PathEval.class) ; 
    
    // Graph to Model.
    static NodeIterator convertGraphNodeToRDFNode(final Model model, Iterator<Node> iter)
    {
        Transform<Node, RDFNode> conv = new Transform<Node, RDFNode>(){
            public RDFNode convert(Node obj)
            {
                return ModelUtils.convertGraphNodeToRDFNode(obj, model) ;
            }
        } ;
        Iterator<RDFNode> iterRDF = Iter.map(iter, conv) ;
        return new NodeIteratorImpl(iterRDF, null) ;
    }
    
    // Possible API usages.
    static public NodeIterator walkForwards(final Model model, RDFNode rdfNode, Path path)
    {
        Iterator<Node> iter = eval(model.getGraph(), rdfNode.asNode(), path) ;
        return convertGraphNodeToRDFNode(model, iter) ;
    }
    
    static public NodeIterator walkBackwards(final Model model, RDFNode rdfNode, Path path)
    {
        Iterator<Node> iter = evalInverse(model.getGraph(), rdfNode.asNode(), path) ;
        return convertGraphNodeToRDFNode(model, iter) ;
    }
    
    // LinkedHashSet for predictable order - remove later??
    
    /** Evaluate a path in the forward direction */ 
    static public Iterator<Node> eval(Graph graph, Node node, Path path)
    { 
        if ( node == null  )
            Log.fatal(PathEval.class, "PathEval.eval applied to a null node") ;
        if ( node.isVariable() )
            Log.warn(PathEval.class, "PathEval.eval applied to a variable: "+node) ;
        return eval(graph, node, path, true) ;
    }
    
    /** Evaluate a path starting at the end of the path */ 
    static public Iterator<Node> evalInverse(Graph g, Node node, Path path) 
    { 
        if ( node == null  )
            Log.fatal(PathEval.class, "PathEval.eval applied to a null node") ;
        if ( node.isVariable() )
            Log.warn(PathEval.class, "PathEval.eval applied to a variable: "+node) ;
        return eval(g, node, path, false) ; 
    }

    static private Iterator<Node> eval(Graph graph, Node node, Path path, boolean forward)
    {
        Collection<Node> acc = new ArrayList<Node>() ;
        eval(graph, node, path, forward, acc);
        return acc.iterator() ;
    }
    
    static private Iterator<Node> eval(Graph graph, Iterator<Node> input, Path path, boolean forward) 
    {
        Collection<Node> acc = new ArrayList<Node>() ;
        
        for ( ; input.hasNext() ; )
        {
            Node node = input.next() ;
            eval(graph, node, path, forward, acc) ;
        }
        return acc.iterator() ;
    }
    
    // ---- Worker ??
    static private void eval(Graph graph, Node node, Path p, boolean forward, Collection<Node> acc)
    {
        PathEvaluator evaluator = new PathEvaluator(graph, node, acc, forward) ;
        p.visit(evaluator) ;
    }
    // ----
    
    private static class PathEvaluator implements PathVisitor
    {

        private final Graph graph ;
        private final Node node ;
        private final Collection<Node> output ;
        private boolean forwardMode ; 

        public PathEvaluator(Graph g, Node n, Collection<Node> output, boolean forward)
        {
            this.graph = g ; 
            this.node = n ;
            this.output = output ;
            this.forwardMode = forward ;
        }

        //@Override
        public void visit(P_Link pathNode)
        {
            Iterator<Node> nodes = doOne(pathNode.getNode()) ;
            fill(nodes) ;
        }
        
        //@Override
        public void visit(P_ReverseLink pathNode)
        {
            forwardMode = ! forwardMode ;
            Iterator<Node> nodes = doOne(pathNode.getNode()) ;
            forwardMode = ! forwardMode ;
            fill(nodes) ;
        }

        //@Override
        public void visit(P_NegPropSet pathNotOneOf)
        {
            // X !(:a|:b|^:c|^:d) Y = { X !(:a|:b) Y } UNION { Y !(:c|:d) X }
            if ( pathNotOneOf.getFwdNodes().size() > 0 )
            {
                Iterator<Node> nodes1 = doOneExcludeForwards(pathNotOneOf.getFwdNodes()) ;
                fill(nodes1) ;
            }
            if ( pathNotOneOf.getBwdNodes().size() > 0 )
            {
                Iterator<Node> nodes2 = doOneExcludeBackwards(pathNotOneOf.getBwdNodes()) ;
                fill(nodes2) ;
            }
        }
        
        //@Override
        public void visit(P_Inverse inversePath)
        {
            //boolean b = forwardMode ;
            // Flip direction and evaluate
            forwardMode = ! forwardMode ;
            inversePath.getSubPath().visit(this) ;
            forwardMode = ! forwardMode ;
        }

        //@Override
        public void visit(P_Alt pathAlt)
        {
            // Try both sizes, accumulate into output.
            Iterator<Node> iter = eval(graph, node, pathAlt.getLeft(), forwardMode) ;
            fill(iter) ;
            iter = eval(graph, node, pathAlt.getRight(), forwardMode) ;
            fill(iter) ;
        }

        //@Override
        public void visit(P_Seq pathSeq)
        {
            Path part1 = forwardMode ? pathSeq.getLeft() : pathSeq.getRight() ;
            Path part2 = forwardMode ? pathSeq.getRight() : pathSeq.getLeft() ;
            
            // Feed one side into the other
            Iterator<Node> iter = eval(graph, node, part1, forwardMode) ;
            iter = eval(graph, iter, part2, forwardMode) ;
            fill(iter) ;
        }

        //@Override
        public void visit(P_Mod pathMod)
        {
            if ( pathMod.isZeroOrMore() )
            {
                doZeroOrMore(pathMod.getSubPath()) ;
                return ;
            }
            
            if ( pathMod.isOneOrMore() )
            {
                doOneOrMore(pathMod.getSubPath()) ;
                return ;
            }
            
            if ( pathMod.getMin() == 0 )
                output.add(node) ;

            if ( pathMod.getMax() == 0 )
                return ;
            
            // One step.
            Iterator<Node> iter = eval(graph, node, pathMod.getSubPath(), forwardMode) ;

            // The next step
            long min2 = dec(pathMod.getMin()) ;
            long max2 = dec(pathMod.getMax()) ;
            P_Mod nextPath = new P_Mod(pathMod.getSubPath(), min2, max2) ;
            
//            // Debug.
//            Listx = Iter.toList(iter) ;
//            System.out.println(x) ;
//            iter = x.iterator() ;
            
            // Moved on one step - now go and do it again on a new path
            //  Need to do the visited thing?  No.  Exact {N,M}
            
            for ( ; iter.hasNext() ; )
            {
                Node n2 = iter.next() ;
                Iterator<Node> iter2 = eval(graph, n2, nextPath, forwardMode) ;
                fill(iter2) ;
            }
            // If no matches, will not call eval and we drop out.
        }
        
        //@Override
        public void visit(P_FixedLength pFixedLength)
        {
            if ( pFixedLength.getCount() == 0 )
            {
                output.add(node) ;
                return ;
            }
            // P_Mod(path, count, count)
            // One step.
            Iterator<Node> iter = eval(graph, node, pFixedLength.getSubPath(), forwardMode) ;
            long count2 = dec(pFixedLength.getCount()) ;
            P_FixedLength nextPath = new P_FixedLength(pFixedLength.getSubPath(), count2) ;
            for ( ; iter.hasNext() ; )
            {
                Node n2 = iter.next() ;
                Iterator<Node> iter2 = eval(graph, n2, nextPath, forwardMode) ;
                fill(iter2) ;
            }
        }

        //@Override
        public void visit(P_ZeroOrOne path)
        { 
            doZero(path.getSubPath()) ;
            doOne(path.getSubPath()) ;
        }

        //@Override
        public void visit(P_ZeroOrMore path)
        { 
            doZeroOrMore(path.getSubPath()) ;
        }

        //@Override
        public void visit(P_OneOrMore path)
        { 
            doOneOrMore(path.getSubPath()) ;
        }

        private void doZero(Path path)
        {
            // Ignores path.
            output.add(node) ;
        }

        private void doOne(Path path)
        {
            Iterator<Node> iter = eval(graph, node, path, forwardMode) ;
            fill(iter) ;
        }

        private void fill(Iterator<Node> iter)
        {
            for ( ; iter.hasNext() ; )
                output.add(iter.next()) ;
        }

        private static Transform<Triple, Node> selectSubject = new Transform<Triple, Node>()
        {
            public Node convert(Triple triple)
            { return triple.getSubject() ; }
        } ;

        private static Transform<Triple, Node> selectPredicate = new Transform<Triple, Node>()
        {
            public Node convert(Triple triple)
            { return triple.getPredicate() ; }
        } ;

        private static Transform<Triple, Node> selectObject = new Transform<Triple, Node>()
        {
            public Node convert(Triple triple)
            { return triple.getObject() ; }
        } ;
        
        // --- Where we touch the graph
        private final Iterator<Node> doOne(Node property)
        {
            Iterator<Node> iter2 = null ;
            if ( forwardMode )
            {
                Iter<Triple> iter1 = Iter.iter(graph.find(node, property, Node.ANY)) ;
                iter2 = iter1.map(selectObject) ;
            }
            else
            {
                Iter<Triple> iter1 = Iter.iter(graph.find(Node.ANY, property, node)) ;
                iter2 = iter1.map(selectSubject) ;
            }
            
            return iter2 ;
        }

        private static class FilterExclude implements Filter<Triple>
        {
            private Collection<Node> excludes ;
            public FilterExclude(Collection <Node> excludes) { this.excludes = excludes ; }
            public boolean accept(Triple triple)
            {
                return ! excludes.contains(triple.getPredicate()) ;
            }
        }
        
        private final Iterator<Node> doOneExcludeForwards(List<Node> excludedNodes)
        {
            Iter<Triple> iter1 = forwardLinks(node, excludedNodes) ;
            Iter<Node> r1 = iter1.map(selectObject) ;
            return r1 ;
        }
        
        private final Iterator<Node> doOneExcludeBackwards(List<Node> excludedNodes)
        {
            Iter<Triple> iter1 = backwardLinks(node, excludedNodes) ;
            Iter<Node> r1 = iter1.map(selectSubject) ;
            return r1 ;
        }
        
//        private boolean testConnected(Node x, Node z, List<Node> excludeProperties)
//        {
//            Iter<Triple> iter1 = Iter.iter(graph.find(x, Node.ANY, z)) ;
//            if ( excludeProperties != null )
//                iter1 = iter1.filter(new FilterExclude(excludeProperties)) ;
//            return iter1.hasNext() ;
//        }

        private Iter<Triple> between(Node x, Node z)
        {
            Iter<Triple> iter1 = Iter.iter(graph.find(x, Node.ANY, z)) ;
            return iter1 ;
        }
        
        private Iter<Triple> forwardLinks(Node x, Collection<Node> excludeProperties)
        {
            Iter<Triple> iter1 = Iter.iter(graph.find(x, Node.ANY, Node.ANY)) ;
            if ( excludeProperties != null )
                iter1 = iter1.filter(new FilterExclude(excludeProperties)) ;
            return iter1 ;
        }

        private Iter<Triple> backwardLinks(Node x, Collection<Node> excludeProperties)
        {
            Iter<Triple> iter1 = Iter.iter(graph.find(Node.ANY, Node.ANY, x)) ;
            if ( excludeProperties != null )
                iter1 = iter1.filter(new FilterExclude(excludeProperties)) ;
            return iter1 ;
        }

        private static long dec(long x) { return (x<=0) ? x : x-1 ; }

        private void doOneOrMore(Path path)
        {
            // This is the visited node collection - a set is OK
            Set<Node> visited = new HashSet<Node>() ;
            doOneOrMore(node, path, visited) ;
        }

        private void doOneOrMore(Node node, Path path, Set<Node> visited)
        {
            if ( visited.contains(node) ) return ;
            
            visited.add(node) ;
            // Do one step.
            Iterator<Node> iter1 = eval(graph, node, path, forwardMode) ;
            
            // For each step, add to results and recurse.
            for ( ; iter1.hasNext() ; )
            {
                Node n1 = iter1.next();
                output.add(n1) ;
                doOneOrMore(n1, path, visited) ;
            }
            visited.remove(node) ;
            
        }
        
        private void doZeroOrMore(Path path)
        {
            doZero(path) ;
            doOneOrMore(path) ;
        }
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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