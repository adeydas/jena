/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot.lang;

import org.openjena.atlas.lib.Sink ;
import org.openjena.riot.Lang ;
import org.openjena.riot.system.ParserProfile ;
import org.openjena.riot.tokens.Token ;
import org.openjena.riot.tokens.TokenType ;
import org.openjena.riot.tokens.Tokenizer ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;

public final class LangNTriples extends LangNTuple<Triple>
{
    private static Logger messageLog = LoggerFactory.getLogger("N-Triples") ;
    
    public LangNTriples(Tokenizer tokens,
                        ParserProfile profile,
                        Sink<Triple> sink)
    {
        super(tokens, profile, sink) ;
    }
    
    //@Override
    public Lang getLang()   { return Lang.NTRIPLES ; }

//    static final Node X = Node.createURI("http://example") ;
//    static final Triple T = new Triple(X, X, X) ;
    
    @Override
    protected final Triple parseOne() 
    { 
        Token sToken = nextToken() ;
        if ( sToken.isEOF() )
            exception(sToken, "Premature end of file: %s", sToken) ;
        
        Token pToken = nextToken() ;
        if ( pToken.isEOF() )
            exception(pToken, "Premature end of file: %s", pToken) ;
        
        Token oToken = nextToken() ;
        if ( oToken.isEOF() )
            exception(oToken, "Premature end of file: %s", oToken) ;

        // Check in createTriple - but this is cheap so do it anyway.
        checkIRIOrBNode(sToken) ;
        checkIRI(pToken) ;
        checkRDFTerm(oToken) ;
        Token x = nextToken() ;
        
        if ( x.getType() != TokenType.DOT )
            exception(x, "Triple not terminated by DOT: %s", x) ;
//        Node s = X ;
//        Node p = X ;
//        Node o = X ;
//        return T ;
        
        Node s = tokenAsNode(sToken) ;
        Node p = tokenAsNode(pToken) ;
        Node o = tokenAsNode(oToken) ;
        return profile.createTriple(s, p, o, sToken.getLine(), sToken.getColumn()) ;
    }
    
    @Override
    protected final Node tokenAsNode(Token token)
    {
        return profile.create(null, token) ;
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