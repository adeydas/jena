/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: Pattern.java,v 1.2 2003-01-08 15:31:11 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query;

import com.hp.hpl.jena.graph.*;

/**
	@author hedgehog
*/

public class Pattern 
	{
	private Element S;
	private Element P;
	private Element O;
	
	public Pattern( Element S, Element P, Element O )
		{
		this.S = S; 
		this.P = P; 
		this.O = O;
		}
		
    public TripleMatch asTripleMatch( Domain d )
        { return new StandardTripleMatch( S.asNode( d ), P.asNode( d ), O.asNode( d ) ); }
          
    public Element [] getParts() { return new Element[] {S, P, O}; }
    
	public boolean matches( Domain d, Triple t )
		{
		return 
			S.accepts( d, t.getSubject() ) 
			&& P.accepts( d, t.getPredicate() )
			&& O.accepts( d, t.getObject() )
			; 
		}
	
	public Domain matched( Domain d, Triple t )
		{
		S.matched( d, t.getSubject() );
		P.matched( d, t.getPredicate() );
		O.matched( d, t.getObject() );
		return d;
		}
        
     public String toString()
        { return "<pattern " + S + " @" + P + " " + O + ">"; }
	}

/*
    (c) Copyright Hewlett-Packard Company 2002
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
