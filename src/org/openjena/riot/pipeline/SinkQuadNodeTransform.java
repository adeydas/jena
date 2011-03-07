/*
 * (c) Copyright 2010, 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot.pipeline;

import org.openjena.atlas.lib.Sink ;
import org.openjena.atlas.lib.SinkWrapper ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.graph.NodeTransform ;

/** Apply a node transform to each node in a quad */ 
public class SinkQuadNodeTransform extends SinkWrapper<Quad>
{
    private final NodeTransform graphNodeTransform ;
    private final NodeTransform subjTransform ;
    private final NodeTransform predTransform ;
    private final NodeTransform objTransform ;

    /** Apply the nodeTransform to each of G, S, P and O */
    public SinkQuadNodeTransform(Sink<Quad> sink, NodeTransform nodeTransform)
    {
        this(sink, nodeTransform, nodeTransform, nodeTransform, nodeTransform) ;
    }
    
    /** Apply the respective nodeTransform to the slot in the triple */
    public SinkQuadNodeTransform(Sink<Quad> sink, NodeTransform graphNodeTransform, NodeTransform subjTransform, NodeTransform predTransform, NodeTransform objTransform)
    {
        super(sink) ;
        this.graphNodeTransform = graphNodeTransform ;
        this.subjTransform = subjTransform ;
        this.predTransform = predTransform ;
        this.objTransform = objTransform ;
    }

    @Override
    public void send(Quad quad)
    {
        Node g = quad.getGraph() ;
        Node s = quad.getSubject() ;
        Node p = quad.getPredicate() ;
        Node o = quad.getObject() ;
        
        Node g1 = apply(graphNodeTransform, g) ;
        Node s1 = apply(subjTransform, s) ;
        Node p1 = apply(predTransform, p) ;
        Node o1 = apply(objTransform, o) ;

        if ( o != o1 || p != p1 || s != s1 || g != g1 )
            quad = new Quad(g1, s1, p1, o1) ;
        
        super.send(quad) ;
    }
    
    private static Node apply(NodeTransform nodeTransform, Node node)
    {
        if ( nodeTransform == null ) return node ;
        Node n2 = nodeTransform.convert(node) ;
        if ( n2 == null ) return node ;
        return n2 ;
    }
}


/*
 * (c) Copyright 2010, 2011 Epimorphics Ltd.
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