/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot.lang;

import java.io.Reader ;
import java.io.StringReader ;

import org.junit.Test ;
import atlas.test.BaseTest ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.RDFReader ;
import com.hp.hpl.jena.riot.JenaReaderTurtle2 ;
import com.hp.hpl.jena.shared.JenaException ;

public class TestLangTurtle extends BaseTest
{
    @Test public void blankNodes1()
    {
        String s = "_:a <http://example/p> 'foo' . " ;
        StringReader r = new StringReader(s) ;
        Model m = ModelFactory.createDefaultModel() ;
        
        RDFReader reader = new JenaReaderTurtle2() ;
        reader.read(m, r, null) ;
        assertEquals(1, m.size()) ;
        
        String x = m.listStatements().next().getSubject().getId().getLabelString() ;
        assertNotEquals(x, "a") ;

        // reset - reread -  new bNode.
        r = new StringReader(s) ;
        reader.read(m, r, null) ;
        assertEquals(2, m.size()) ;
    }
    
    @Test public void blankNodes2()
    {
        // Duplicate.
        String s = "_:a <http://example/p> 'foo' . _:a <http://example/p> 'foo' ." ;
        StringReader r = new StringReader(s) ;
        Model m = ModelFactory.createDefaultModel() ;
        
        RDFReader reader = new JenaReaderTurtle2() ;
        reader.read(m, r, null) ;
        assertEquals(1, m.size()) ;
    }

    
    @Test public void updatePrefixMapping()
    {
        JenaReaderTurtle2 parser = new JenaReaderTurtle2() ;
        Model model = ModelFactory.createDefaultModel() ;
        Reader reader = new StringReader("@prefix x: <http://example/x>.") ;
        parser.read(model, reader, "http://example/base/") ;
        
        assertEquals(1, model.getNsPrefixMap().size()) ;
        assertEquals("http://example/x", model.getNsPrefixURI("x")) ;
    }
    
    @Test(expected=JenaException.class)
    public void errorJunk()
    {
        JenaReaderTurtle2 parser = new JenaReaderTurtle2() ;
        Model model = ModelFactory.createDefaultModel() ;
        Reader reader = new StringReader("<p>") ;
        parser.read(model, reader, "http://example/base/") ;
    }
    
    @Test(expected=JenaException.class)
    public void errorNoPrefixDef()
    {
        JenaReaderTurtle2 parser = new JenaReaderTurtle2() ;
        Model model = ModelFactory.createDefaultModel() ;
        Reader reader = new StringReader("x:p <p> 'q' .") ;
        parser.read(model, reader, "http://example/base/") ;
    }
    
    
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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