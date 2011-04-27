/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store.bulkloader;

import com.hp.hpl.jena.sparql.util.Timer ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;

class BuilderSecondaryIndexesInterleaved implements BuilderSecondaryIndexes
{
    private LoadMonitor monitor ;

    BuilderSecondaryIndexesInterleaved(LoadMonitor monitor) { this.monitor = monitor ; } 
    
    // Do as one pass over the SPO index, creating both other indexes at the same time.
    // Can be hugely costly in system resources.
    public void createSecondaryIndexes(TupleIndex   primaryIndex ,
                                       TupleIndex[] secondaryIndexes)
    {
        Timer timer = new Timer() ;
        timer.startTimer() ;

        long time1 = timer.readTimer() ;

        LoaderNodeTupleTable.copyIndex(primaryIndex.all(), secondaryIndexes, "All", monitor) ;

        long time2 = timer.readTimer() ;
        monitor.print("Time for all indexes: %.2fs\n", (time2-time1)/1000.0) ;
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