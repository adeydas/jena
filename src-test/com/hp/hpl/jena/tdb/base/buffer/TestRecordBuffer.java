/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.buffer;

import static com.hp.hpl.jena.tdb.btree.BTreeTestBase.r;
import org.junit.BeforeClass;
import org.junit.Test;
import test.BaseTest;

import com.hp.hpl.jena.tdb.base.BaseConfig;
import com.hp.hpl.jena.tdb.base.ConfigTest;
import com.hp.hpl.jena.tdb.base.record.R;
import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;

public class TestRecordBuffer extends BaseTest
{
    static RecordFactory recordFactory  ;
    
    @BeforeClass static public void before()
    {
        recordFactory = new RecordFactory(ConfigTest.TestRecordLength, 0) ;
        BaseConfig.NullOut = true ;    
    }
    
    @Test public void recBuffer01()
    {
        RecordBuffer rb = make(4, 4) ;
        contains(rb, 2, 4, 6, 8) ;
    }
    
    @Test public void recBuffer02()
    {
        RecordBuffer rb = make(4, 4) ;
        int idx = -1 ;
        idx = find(rb, 6) ;
        assertEquals(2, idx) ;
        idx = find(rb, 8) ;
        
        assertEquals(3, idx) ;
        idx = find(rb, 4) ;
        assertEquals(1, idx) ;
        idx = find(rb, 2) ;
        assertEquals(0, idx) ;

        idx = find(rb, 3) ;
        assertEquals(-2, idx) ;
        idx = find(rb, 0) ;
        assertEquals(-1, idx) ;
        idx = find(rb, 10) ;
        assertEquals(-5, idx) ;
    }

    // Shift at LHS
    @Test public void recBuffer03()
    {
        RecordBuffer rb = make(4,5) ;
        contains(rb, 2, 4, 6, 8) ;
        rb.shiftUp(0) ;
        rb.set(0, r(0)) ;
        contains(rb, 0, 2, 4, 6, 8) ;
        rb.shiftDown(0) ;
        contains(rb, 2, 4, 6, 8) ;
    }    
    
    @Test public void recBuffer04()
    {
        RecordBuffer rb = make(4,5) ;
        contains(rb, 2, 4, 6, 8) ;
        rb.shiftDown(0) ;
        
        contains(rb, 4, 6, 8) ;
        rb.shiftUp(0) ;

        rb.set(0,r(1)) ;
        contains(rb, 1, 4, 6, 8) ;
    }    
    

    // Shift at middle
    @Test public void recBuffer05()
    {
        RecordBuffer rb = make(4,5) ;
        contains(rb, 2, 4, 6, 8) ;
        rb.shiftUp(2) ;
        rb.set(2, r(0)) ;
        contains(rb, 2, 4, 0, 6, 8) ;
        rb.shiftDown(2) ;
        contains(rb, 2, 4, 6, 8) ;
    }    

    @Test public void recBuffer06()
    {
        RecordBuffer rb = make(4,5) ;
        contains(rb, 2, 4, 6, 8) ;
        rb.shiftDown(2) ;
        contains(rb, 2, 4, 8) ;
        rb.shiftUp(2) ;
        contains(rb, 2, 4, -1, 8) ;
    }    

    // Shift RHS - out of bounds
    @Test public void recBuffer07()
    {
        RecordBuffer rb = make(4,5) ;
        contains(rb, 2, 4, 6, 8) ;
        rb.shiftUp(3) ;
        rb.set(3, r(1)) ;
        contains(rb, 2, 4, 6, 1, 8) ;
        rb.shiftDown(3) ;
        contains(rb, 2, 4, 6, 8) ;
    }    

    @Test public void recBuffer08()
    {
        RecordBuffer rb = make(4,5) ;
        contains(rb, 2, 4, 6, 8) ;
        rb.shiftDown(3) ;
        contains(rb, 2, 4, 6) ;
        rb.shiftUp(2) ;
        contains(rb, 2, 4, -1, 6) ;
    }    

    // Errors
    
    @Test(expected=IllegalArgumentException.class) 
    public void recBuffer09()
    {
        RecordBuffer rb = make(4,5) ;
        contains(rb, 2, 4, 6, 8) ;
        rb.shiftDown(4) ;
    }  
    
    @Test(expected=IllegalArgumentException.class) 
    public void recBuffer10()
    {
        RecordBuffer rb = make(4,5) ;
        contains(rb, 2, 4, 6, 8) ;
        rb.shiftUp(4) ;
    }  

    @Test(expected=IllegalArgumentException.class) 
    public void recBuffer11()
    {
        RecordBuffer rb = make(5,5) ;
        contains(rb, 2, 4, 6, 8, 10) ;
        rb.add(r(12)) ;
    }  
    
    // Copy, duplicate, clear
    @Test public void recBuffer12()
    {
        RecordBuffer rb = make(5,5) ;
        contains(rb, 2, 4, 6, 8, 10) ;
        RecordBuffer rb2 = rb.duplicate() ;
        rb2.set(1, r(99)) ;
        contains(rb, 2, 4, 6, 8, 10) ;
        contains(rb2, 2, 99, 6, 8, 10) ;
    }
    
    @Test public void recBuffer13()
    {
        RecordBuffer rb = make(5,5) ;
        contains(rb, 2, 4, 6, 8, 10) ;
        rb.clear(1, 3) ;
        contains(rb, 2, -1, -1, -1, 10) ;
    }
    
    @Test public void recBuffer14()
    {
        RecordBuffer rb = make(5,5) ;
        contains(rb, 2, 4, 6, 8, 10) ;
        RecordBuffer rb2 = make(5,5) ;
        contains(rb2, 2, 4, 6, 8, 10) ;
        rb.copy(0, rb2, 1, 4) ;
        contains(rb2, 2, 2, 4, 6, 8) ;
    }

    // Remove tests
    
    @Test public void recBuffer15()
    {
        RecordBuffer rb = make(5,5) ;
        contains(rb, 2, 4, 6, 8, 10) ;
        rb.removeTop() ;
        contains(rb, 2, 4, 6, 8) ;
        rb.remove(1) ;
        contains(rb, 2, 6, 8) ;
        rb.remove(2) ;
        contains(rb, 2, 6) ;
        rb.remove(0) ;
        contains(rb, 6) ;
        rb.remove(0) ;
        contains(rb) ;
    }


    // ---- Support
    private static void contains(RecordBuffer rb, int... vals)
    {
        assertEquals("Length mismatch: ", vals.length, rb.size()) ;
        
        for ( int i = 0 ; i < vals.length ; i++ )
            if ( vals[i] == -1 )
                assertTrue(rb.isClear(i))  ;
            else
            {
                Record r = R.intToRecord(vals[i]) ;
                Record r2 = rb.get(i) ;
                int x = R.recordToInt(r2) ;
                assertEquals("Value mismatch: ", vals[i], x) ;
            }
    }

    
    public int find(RecordBuffer rb, int v)
    {
        return rb.find(r(v)) ;
    }

    private static RecordBuffer make(int n, int len)
    { 
        RecordBuffer rb = new RecordBuffer(recordFactory, len) ;
        for ( int i = 0 ; i < n ; i++ )
        {
            Record r = R.intToRecord(2*i+2) ;  
            rb.add(r) ;
        }
        return rb ;
    }
}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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