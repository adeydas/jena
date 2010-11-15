/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.graph;

import java.util.ArrayList ;
import java.util.List ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.ARQNotImplemented ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.TransformCopy ;
import com.hp.hpl.jena.sparql.algebra.op.OpAssign ;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP ;
import com.hp.hpl.jena.sparql.algebra.op.OpDatasetNames ;
import com.hp.hpl.jena.sparql.algebra.op.OpExtend ;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter ;
import com.hp.hpl.jena.sparql.algebra.op.OpGraph ;
import com.hp.hpl.jena.sparql.algebra.op.OpGroup ;
import com.hp.hpl.jena.sparql.algebra.op.OpPath ;
import com.hp.hpl.jena.sparql.algebra.op.OpProject ;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern ;
import com.hp.hpl.jena.sparql.algebra.op.OpTable ;
import com.hp.hpl.jena.sparql.algebra.op.OpTriple ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.TriplePath ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.core.VarExprList ;
import com.hp.hpl.jena.sparql.expr.ExprAggregator ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.path.Path ;

class NodeTransformOp extends TransformCopy
{
    private final NodeTransform transform ;
    NodeTransformOp(NodeTransform transform)
    {
        this.transform = transform ;
    }

    @Override public Op transform(OpTriple opTriple)
    {
        Triple t2 = NodeTransformLib.transform(transform, opTriple.getTriple()) ;
        if ( t2 == opTriple.getTriple())
            return super.transform(opTriple) ;
        return new OpTriple(t2) ;
    }
    
    @Override public Op transform(OpFilter opFilter, Op subOp)
    { 
        ExprList exprList = opFilter.getExprs() ;
        ExprList exprList2 = NodeTransformLib.transform(transform, exprList) ;
        return OpFilter.filter(exprList2, subOp) ;
    }        
    
    @Override public Op transform(OpBGP opBGP)
    { 
        BasicPattern bgp2 = NodeTransformLib.transform(transform, opBGP.getPattern()) ;
        if ( bgp2 == opBGP.getPattern())
            return super.transform(opBGP) ;
        return new OpBGP(bgp2) ;
    }
    
    @Override public Op transform(OpPath opPath)
    { 
        TriplePath tp = opPath.getTriplePath() ;
        Node s = tp.getSubject() ;
        Node s1 = transform.convert(s) ;
        Node o = tp.getObject() ;
        Node o1 = transform.convert(o) ;
        
        if ( s1 == s || o1 == o )
            // No change.
            return opPath ;
        
        Path path = tp.getPath() ;
        TriplePath tp2 ;

        if ( path != null )
            tp2 = new TriplePath(s1, path, o1) ;
        else
        {
            Triple t = new Triple(s1, tp.getPredicate(), o1) ;
            tp2 = new TriplePath(t) ;
        }
        return new OpPath(tp2) ;
    }
    
    @Override public Op transform(OpQuadPattern opQuadPattern)
    { 
        // The internal representation is (graph, BGP)
        BasicPattern bgp2 = NodeTransformLib.transform(transform, opQuadPattern.getBasicPattern()) ;
        Node g2 = opQuadPattern.getGraphNode() ;
        g2 = transform.convert(g2) ;
        
        if ( g2 == opQuadPattern.getGraphNode() && bgp2 == opQuadPattern.getBasicPattern() )
            return super.transform(opQuadPattern) ;
        return new OpQuadPattern(g2, bgp2) ;
    }
    
    @Override public Op transform(OpGraph opGraph, Op subOp)
    {
        Node g2 = transform.convert(opGraph.getNode()) ;
        if ( g2 == opGraph.getNode() )
            return super.transform(opGraph, subOp) ;
        return new OpGraph(g2, subOp) ;
    }
    
    @Override public Op transform(OpDatasetNames opDatasetNames)
    {
        Node g2 = transform.convert(opDatasetNames.getGraphNode()) ;
        if ( g2 == opDatasetNames.getGraphNode() )
            return super.transform(opDatasetNames) ;
        return new OpDatasetNames(g2) ;
    }
    
    @Override public Op transform(OpTable opTable)
    {
        if ( opTable.isJoinIdentity() )
            return opTable ;
        
        throw new ARQNotImplemented() ;
        //return null ;
    }
    
    @Override public Op transform(OpProject opProject, Op subOp)
    { 
        List<Var> x = opProject.getVars() ;
        List<Var> x2 = NodeTransformLib.transformVars(transform, x) ;
        return new OpProject(subOp, x2) ; 
    }
    
    @Override public Op transform(OpAssign opAssign, Op subOp)
    { 
        VarExprList varExprList = opAssign.getVarExprList() ;
        VarExprList varExprList2 = NodeTransformLib.transform(transform, varExprList) ;
        return OpAssign.assign(subOp, varExprList2) ;
    }
    
    @Override public Op transform(OpExtend opExtend, Op subOp)
    { 
        VarExprList varExprList = opExtend.getVarExprList() ;
        VarExprList varExprList2 = NodeTransformLib.transform(transform, varExprList) ;
        return OpExtend.extend(subOp, varExprList2) ;
    }
    
    @Override public Op transform(OpGroup opGroup, Op subOp)
    {
        VarExprList groupVars = NodeTransformLib.transform(transform, opGroup.getGroupVars()) ;

        // Rename the vars in the expression as well.
        // .e.g max(?y) ==> max(?/y)  
        // These need renaming as well.
        List<ExprAggregator> aggregators = new ArrayList<ExprAggregator>() ;
        for ( ExprAggregator agg : opGroup.getAggregators() )
            aggregators.add(agg.applyNodeTransform(transform)) ;
        return new OpGroup(subOp, groupVars, aggregators) ;
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