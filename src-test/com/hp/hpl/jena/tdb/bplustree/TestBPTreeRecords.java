/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.bplustree;

import junit.TestBase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.tdb.base.block.BlockMgr;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFactory;
import com.hp.hpl.jena.tdb.base.buffer.RecordBuffer;
import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.base.record.RecordLib;
import com.hp.hpl.jena.tdb.base.recordfile.RecordBufferPage;
import com.hp.hpl.jena.tdb.base.recordfile.RecordBufferPageMgr;
import com.hp.hpl.jena.tdb.sys.SystemTDB;

public class TestBPTreeRecords extends TestBase
{
    static private boolean oldNullOut ;
    static private boolean oldCheckingNode ;
    static private boolean oldCheckingBTree ;
    
    static private int blockSize ;
    static private RecordFactory recordFactory ;
    
    static private int bufSizeRecord ;
    static private BlockMgr blkMgrRecords  ;
    static private RecordBufferPageMgr recordBufferPageMgr  ;
    static private BPlusTree bPlusTree ;
    
    @BeforeClass public static void beforeClass()
    {
        oldNullOut = SystemTDB.NullOut ;
        SystemTDB.NullOut = true ;
        
        oldCheckingNode = BPlusTreeParams.CheckingNode ;
        BPlusTreeParams.CheckingNode = true ;
        
        oldCheckingBTree = BPlusTreeParams.CheckingTree ;
        BPlusTreeParams.CheckingTree = true ;
        
        blockSize =  4*8 ;  // Which is 6 int records
        recordFactory = new RecordFactory(4, 0) ;
        
        bufSizeRecord = RecordBufferPage.calcRecordSize(recordFactory, blockSize) ;
        blkMgrRecords = BlockMgrFactory.createMem("BPTreeRecords", blockSize) ;
        recordBufferPageMgr = new RecordBufferPageMgr(recordFactory, blkMgrRecords) ;
        
        // Order does not matter.
        bPlusTree = BPlusTree.dummy(new BPlusTreeParams(3, recordFactory), null, blkMgrRecords) ;
    }
    
    @AfterClass public static void afterClass()
    {
        SystemTDB.NullOut = oldNullOut ;
        BPlusTreeParams.CheckingTree = oldCheckingNode ;
        BPlusTreeParams.CheckingTree = oldCheckingBTree ;
    }

//    @Before public void before() {}
//    @After public void after() {}

    @Test public void bpt_records_1()
    {
        BPTreeRecords bpr = make() ;
        fill(bpr) ;
        check(bpr) ;
    }
    

    @Test public void bpt_records_2()
    {
        BPTreeRecords bpr = make() ;
        fill(bpr) ;
        int s = bpr.getCount();
        assertTrue(bpr.isFull()) ;
        BPTreePage z = bpr.split() ;
        assertTrue(z instanceof BPTreeRecords) ;
        assertEquals(s, z.getCount()+bpr.getCount()) ;
        check(bpr) ;
        check((BPTreeRecords)z) ;
    }

    @Test public void bpt_records_3()
    {
        BPTreeRecords bpr = make() ;
        for ( int i = 0 ; bpr.getCount() < bpr.getMaxSize() ; i++ )
            insert(bpr, (i+0x20)) ;
        check(bpr) ;
    }
    
    @Test public void bpt_records_4()
    {
        BPTreeRecords bpr = make() ;
        for ( int i = bpr.getMaxSize()-1 ; i >= 0 ; i-- )
            insert(bpr, i+0x20) ;
        check(bpr) ;
    }
    
    @Test public void bpt_records_5()
    {
        BPTreeRecords bpr = make() ;
        int N =  bpr.getMaxSize() ;
        
        for ( int i = bpr.getMaxSize()-1 ; i >= 0 ; i-- )
            insert(bpr, (i+0x20)) ;
        
        delete(bpr, (1+0x20)) ;
        assertEquals(N-1, bpr.getCount()) ;
        check(bpr) ;

        delete(bpr, (2+0x20)) ;
        assertEquals(N-2, bpr.getCount()) ;
        check(bpr) ;
        
        delete(bpr, bpr.getLowRecord()) ;
        assertEquals(N-3, bpr.getCount()) ;
        check(bpr) ;

        bpr.internalDelete(bpr.getHighRecord()) ;
        assertEquals(N-4, bpr.getCount()) ;
        check(bpr) ;
    }
    
    @Test public void bpt_records_6()
    {
        BPTreeRecords bpr = make() ;
        fill(bpr) ;
        
        // No match.
        assertNull(bpr.internalSearch(RecordLib.intToRecord(0x20))) ;

        Record r = RecordLib.intToRecord(0x32) ;
        Record r2 = search(bpr, r) ;
        assertTrue(Record.keyEQ(r, r2)) ;

        r = bpr.getLowRecord() ;
        r2 = search(bpr, r) ;
        assertTrue(Record.keyEQ(r, r2)) ;
        
        r = bpr.getHighRecord() ;
        r2 = search(bpr, r) ;
        assertTrue(Record.keyEQ(r, r2)) ;

    }
    
    @Test public void bpt_shift_1()
    {
        BPTreeRecords bpr1 = make() ;
        BPTreeRecords bpr2 = make() ;

        insert(bpr1, 10) ;
        Record r = bpr1.shiftRight(bpr2, null) ;
        assertNull(r) ;
        //assertTrue(Record.keyEQ(r, RecordTestLib.intToRecord(10))) ;
        contains(bpr1) ;
        contains(bpr2, 10) ;
    }
    
    @Test public void bpt_shift_2()
    {
        BPTreeRecords bpr1 = make() ;
        BPTreeRecords bpr2 = make() ;
        
        insert(bpr1, 10) ;
        Record r = bpr2.shiftLeft(bpr1, null) ;
        
        assertTrue(Record.keyEQ(r, RecordLib.intToRecord(10))) ;
        contains(bpr1) ;
        contains(bpr2, 10) ;
    }

    @Test public void bpt_shift_3()
    {
        BPTreeRecords bpr1 = make() ;
        BPTreeRecords bpr2 = make() ;
        
        insert(bpr1, 10, 20) ;
        insert(bpr2, 99) ;
        
        Record r = bpr1.shiftRight(bpr2, null) ;

        assertTrue(r+" != "+RecordLib.intToRecord(10), Record.keyEQ(r, RecordLib.intToRecord(10))) ;
        contains(bpr1, 10) ;
        contains(bpr2, 20, 99) ;
    }

    @Test public void bpt_shift_4()
    {
        BPTreeRecords bpr1 = make() ;
        BPTreeRecords bpr2 = make() ;
        
        insert(bpr1, 10, 20) ;
        insert(bpr2, 5) ;
        
        Record r = bpr2.shiftLeft(bpr1, null) ;
        assertTrue(Record.keyEQ(r, RecordLib.intToRecord(10))) ;
        
        contains(bpr1, 20) ;
        contains(bpr2, 5, 10) ;
    }
    
    @Test public void bpt_merge_1()
    {
        BPTreeRecords bpr1 = make() ;
        BPTreeRecords bpr2 = make() ;
        
        insert(bpr1, 10, 20) ;
        insert(bpr2, 99) ;
        
        BPTreeRecords bpr3 = (BPTreeRecords)bpr1.merge(bpr2, null) ;
        contains(bpr1, 10, 20, 99) ;
        contains(bpr2) ;
        assertSame(bpr1, bpr3) ;
    }

    @Test public void bpt_merge_2()
    {
        BPTreeRecords bpr1 = make() ;
        BPTreeRecords bpr2 = make() ;
        
        insert(bpr1, 10, 20) ;
        insert(bpr2, 5) ;
        
        BPTreeRecords bpr3 = (BPTreeRecords)bpr2.merge(bpr1, null) ;
        contains(bpr1) ;
        contains(bpr2, 5, 10, 20) ;
        assertSame(bpr2, bpr3) ;
    }
    
    private static void check(BPTreeRecords bpr)
    {
        assertTrue(bpr.getCount() >= 0 ) ;
        assertTrue(bpr.getCount() <= bpr.getMaxSize() ) ;
        
        assertEquals(bpr.getRecordBuffer().getLow(), bpr.getLowRecord()) ;
        assertEquals(bpr.getRecordBuffer().getHigh(), bpr.getHighRecord()) ;
        
        for ( int i = 1 ; i < bpr.getCount() ; i++ )
        {
            Record r1 = bpr.getRecordBuffer().get(i-1) ;
            Record r2 = bpr.getRecordBuffer().get(i) ;
            assertTrue(Record.keyLE(r1, r2)) ;
        }
    }

    private static Record search(BPTreeRecords bpr, int x)
    {
        return search(bpr, RecordLib.intToRecord(x)) ;
    }

    
    private static Record search(BPTreeRecords bpr, Record r)
    {
        return bpr.internalSearch(r) ;
    }

    private static void insert(BPTreeRecords bpr, int ... values)
    {
        for ( int i = 0 ; i < values.length ; i++ )
           bpr.internalInsert(RecordLib.intToRecord(values[i])) ;
    }
    
    private static void insert(BPTreeRecords bpr, Record r)
    {
        bpr.internalInsert(r) ;
    }
    

    private static void delete(BPTreeRecords bpr, int ... values)
    {
        for ( int i = 0 ; i < values.length ; i++ )
           delete(bpr, RecordLib.intToRecord(values[i])) ;
    }
    
    private static void delete(BPTreeRecords bpr, Record r)
    {
        bpr.internalDelete(r) ;
    }
    

    private static void contains(BPTreeRecords bpr, int ... values)
    {
        assertEquals(values.length, bpr.getCount() ) ;
        for ( int i = 1 ; i < values.length ; i++ )
            assertTrue(Record.compareByKeyValue(RecordLib.intToRecord(values[i]),bpr.getRecordBuffer().get(i)) == 0 ) ;
    }

    private static BPTreeRecords make()
    {
        int id = recordBufferPageMgr.allocateId() ;
        RecordBufferPage page = recordBufferPageMgr.create(id) ;
        return new BPTreeRecords(bPlusTree, page) ;
    }
    private static void fill(BPTreeRecords bpr)
    {
        RecordBuffer rb = bpr.getRecordBuffer() ;
        for ( int i = 0 ; rb.size() < rb.maxSize() ; i++ )
            insert(bpr, (i+0x30)) ;
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