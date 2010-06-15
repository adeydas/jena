/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core;

import java.util.Set ;

import org.openjena.atlas.iterator.Iter ;

import com.hp.hpl.jena.sparql.core.Var ;

public class ScopeOptional implements Scope
{
    private Scope scope ;
    private ScopeStatus scopeStatus = ScopeStatus.OPTIONAL ;

    // May be better to copy this and mutate the scopen status 
    public ScopeOptional(Scope subScope)
    { this.scope = subScope ; }
    
    public ScopeEntry findScopeForVar(Var var)
    {
        ScopeEntry e = scope.findScopeForVar(var) ;
        if ( e == null )
            return null ;
        e = e.duplicate() ; // Copy - we're going to mutate it.
        e.setStatus(scopeStatus) ;
        return e ;
    }

    public Set<Var> getVars()
    {
        return scope.getVars() ;
    }

    public boolean isEmpty()
    { return scope.isEmpty() ; }
    
    public Set<ScopeEntry> findScopes()
    {
        Set<ScopeEntry> x = scope.findScopes() ;
        Iter.apply(x, ScopeEntry.SetOpt) ;
        return x ;
    }
    
    public boolean hasColumnForVar(Var var)
    {
        return scope.hasColumnForVar(var) ;
    }

    @Override
    public String toString()
    {
        return "Opt("+scope.toString()+")" ;
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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