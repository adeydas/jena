/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import java.util.List ;

import com.hp.hpl.jena.sparql.expr.nodevalue.XSDFuncOp ;
import com.hp.hpl.jena.sparql.sse.Tags ;

public class E_StrSubstring extends ExprFunctionN
{
    private static final String symbol = Tags.tagSubstr ;

    public E_StrSubstring(Expr expr1, Expr expr2, Expr expr3)
    {
        super(symbol, expr1, expr2, expr3) ;
    }
    
    @Override
    protected NodeValue eval(List<NodeValue> args)
    { 
        if ( args.size() == 2 )
            return XSDFuncOp.substring(args.get(0), args.get(1)) ;
        
        //return NodeFunctions.substring(args.get(0), args.get(1), args.get(2)) ;
        return XSDFuncOp.substring(args.get(0), args.get(1), args.get(2)) ;
    }

    @Override
    protected Expr copy(ExprList newArgs)
    {
        if ( newArgs.size() == 2 )
            return new E_StrSubstring(newArgs.get(0), newArgs.get(1), null) ; 
        return new E_StrSubstring(newArgs.get(0), newArgs.get(1), newArgs.get(2)) ;   
    }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
 *  All rights reserved.
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
