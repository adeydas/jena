/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver;

import iterator.Iter;
import iterator.NullIterator;
import iterator.RepeatApplyIterator;
import iterator.Transform;

import java.util.Iterator;

import lib.Tuple;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.tdb.pgraph.GraphTDB;
import com.hp.hpl.jena.tdb.pgraph.NodeId;

class StageMatchTriple extends RepeatApplyIterator<BindingNodeId>
{
    private GraphTDB graph ;
    private Triple triple ;
    private ExecutionContext execCxt ;

    StageMatchTriple(GraphTDB graph, Iterator<BindingNodeId> input, Triple triple, ExecutionContext execCxt)
    {
        super(input) ;
        this.graph = graph ; 
        this.triple = triple ;
        this.execCxt = execCxt ;
    }

    @Override
    protected Iterator<BindingNodeId> makeNextStage(final BindingNodeId input)
    {
        NodeId s = idFor(graph, input, triple.getSubject()) ;
        if ( s == NodeId.NodeDoesNotExist ) return new NullIterator<BindingNodeId>() ;
        
        NodeId p = idFor(graph, input, triple.getPredicate()) ;
        if ( p == NodeId.NodeDoesNotExist ) return new NullIterator<BindingNodeId>() ;
        
        NodeId o = idFor(graph, input, triple.getObject()) ;
        if ( o == NodeId.NodeDoesNotExist ) return new NullIterator<BindingNodeId>() ;
        
        // s etc is a null if the triple has a variable in that slot.
        final Var var_s = (s == null) ? asVar(triple.getSubject()) : null ;
        final Var var_p = (p == null) ? asVar(triple.getPredicate()) : null ;
        final Var var_o = (o == null) ? asVar(triple.getObject()) : null ;

        Iterator<Tuple<NodeId>> tuples = graph.find(s,p,o);

        Transform<Tuple<NodeId>, BindingNodeId> binder = new Transform<Tuple<NodeId>, BindingNodeId>()
        {
            @Override
            public BindingNodeId convert(Tuple<NodeId> tuple)
            {
                BindingNodeId output = new BindingNodeId(input) ;

                if ( var_s != null )
                {
                    if ( reject(output, var_s, tuple.get(0)) )
                        return null ;
                    output.put(var_s, tuple.get(0)) ;
                }

                if ( var_p != null )
                {
                    if ( reject(output, var_p, tuple.get(1)) )
                        return null ;
                    output.put(var_p, tuple.get(1)) ;
                }

                if ( var_o != null )
                {
                    if ( reject(output, var_o, tuple.get(2)) )
                        return null ;
                    output.put(var_o, tuple.get(2)) ;
                }

                return output ;
            }
        } ;
        return Iter.iter(tuples).map(binder).removeNulls() ;
    }
    
    private static boolean reject(BindingNodeId output , Var var, NodeId value)
    {
        if ( ! output.containsKey(var) )
            return false ;
        
        if ( output.get(var).equals(value) )
            return false ;

        return true ;
    }
    
    private static Var asVar(Node node)
    {
        if ( Var.isVar(node) )
            return Var.alloc(node) ;
        return null ;
    }

    private static NodeId idFor(GraphTDB graph, BindingNodeId input, Node node)
    {
        if ( Var.isVar(node) )
        {
            NodeId n = input.get((Var.alloc(node))) ;
            // Bound to NodeId or null. 
            return n ;
        } 
        // May return NodeId.NodeDoesNotExist which must not be null. 
        return graph.getNodeTable().nodeIdForNode(node) ;
    }
}
/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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