/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store;

import static com.hp.hpl.jena.tdb.TDB.logExec;
import static com.hp.hpl.jena.tdb.TDB.logInfo;
import static com.hp.hpl.jena.tdb.sys.Names.primaryIndexQuads;
import static com.hp.hpl.jena.tdb.sys.Names.primaryIndexTriples;
import static com.hp.hpl.jena.tdb.sys.Names.quadIndexes;
import static com.hp.hpl.jena.tdb.sys.Names.tripleIndexes;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.LenIndexQuadRecord;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.LenIndexTripleRecord;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.LenNodeHash;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.SizeOfNodeId;
import lib.ColumnMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetImpl;
import com.hp.hpl.jena.sparql.sse.SSEParseException;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.index.IndexBuilder;
import com.hp.hpl.jena.tdb.index.RangeIndex;
import com.hp.hpl.jena.tdb.index.TupleIndex;
import com.hp.hpl.jena.tdb.index.TupleIndexRecord;
import com.hp.hpl.jena.tdb.nodetable.NodeTable;
import com.hp.hpl.jena.tdb.nodetable.NodeTableFactory;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderLib;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderTransformation;
import com.hp.hpl.jena.tdb.sys.Names;
import com.hp.hpl.jena.tdb.sys.SystemTDB;

/** Low-level factory for things TDB. See {@link com.hp.hpl.jena.tdb.TDBFactory} for the usual application API */
public class FactoryGraphTDB
{
    // For this class
    private static Logger log = LoggerFactory.getLogger(FactoryGraphTDB.class) ;

    // ---- Record factories
    public final static RecordFactory indexRecordTripleFactory = new RecordFactory(LenIndexTripleRecord, 0) ; 
    public final static RecordFactory indexRecordQuadFactory = new RecordFactory(LenIndexQuadRecord, 0) ; 
    public final static RecordFactory nodeRecordFactory = new RecordFactory(LenNodeHash, SizeOfNodeId) ;
    
    /** Create a TDB graph at the location.  Any existing persistent files are reconnected.
     * If there no index or node files at the location, an empty graph is setup.
     * 
     * @param location      The location containg the file system resources.
     */
    public static GraphTriplesTDB createGraph(Location location)
    {
        return createGraph(IndexBuilder.get(), location) ;
    }  
    
    // Detect existsing (other technology) indexes.
//    public static PGraph create(Location location)
//    { 
//        if ( location == null )
//            throw new TDBException("Location is null") ;
//
//        if ( location.exists(Names.indexNode2Id, Names.extHashExt) )
//        {
//            log.info("Existing extendible hash index for nodes found - using ExtHash/B+Tree indexing") ;
//            return create(IndexBuilder.getExtHash(), location) ;
//        }
//        
//        if ( location.exists(indexSPO, Names.btExt) )
//        {
//            log.info("Existing BTree index found - using BTree indexing") ;
//            return create(IndexBuilder.getBTree(), location) ;
//        }
//        
//        if ( location.exists(indexSPO, Names.bptExt1) )
//        {
//            log.debug("Existing B+Tree index found - using B+Tree indexing") ;
//            return create(IndexBuilder.getBPlusTree(), location) ;
//        }   
//        
//        return create(IndexBuilder.get(), location) ;
//    }

    
    /** Create a TDB graph using a specifc index builder - mainly for testing */
    public static GraphTriplesTDB createGraph(IndexBuilder indexBuilder, Location location)
    {
        return _createGraph(indexBuilder, location) ;
    }  
    
    /** Create a TDB graph in-memory - for testing */
    public static GraphTriplesTDB createGraphMem()
    {
        return createGraphMem(IndexBuilder.mem()) ;
    }

    /** Create a TDB graph in-memory - for testing */
    public static GraphTriplesTDB createGraphMem(IndexBuilder indexBuilder)
    {
        return createGraph(indexBuilder, null) ;
    }

    /** Create a TDB graph at the location and wrap it up as a Model.  
     * Any existing persistent files are reconnected.
     * If there no index or node files at the location, an empty graph is setup.
     * 
     * @param location      The location containg the file system resources.
     */
    public static Model createModel(Location location)
    {
        return ModelFactory.createModelForGraph(createGraph(location)) ;
    }
    
    
    /** Create or connect a TDB dataset */ 
    public static DatasetGraph createDatasetGraph(Location location)
    {
        return createDatasetGraph(IndexBuilder.get(), location, tripleIndexes, quadIndexes) ;
    }

    /** Create or connect a TDB dataset in-memory - for testing */ 
    public static DatasetGraph createDatasetGraphMem()
    {
        return createDatasetGraph(IndexBuilder.mem(), null, tripleIndexes, quadIndexes) ;
    }

    /** Create or connect a TDB dataset (graph-level) */
    public static DatasetGraph createDatasetGraph(IndexBuilder indexBuilder, Location location, String[] graphDesc, String[] quadDesc)
    {
        return _createDatasetGraph(indexBuilder, location, graphDesc, quadDesc) ;
    }

    /** Create or connect a TDB dataset */
    public static Dataset createDataset(Location location)
    {
        return new DatasetImpl(createDatasetGraph(location)) ;
    }

    /** Create or connect a TDB dataset (in-memory - for testing) */
    public static Dataset createDatasetMem()
    {
        return new DatasetImpl(createDatasetGraphMem()) ;
    }

    
    // ---- Process
    public static TupleIndex[] indexes(IndexBuilder indexBuilder, RecordFactory recordFactory, 
                                        Location location, String primary, String...descs)
    {
        TupleIndex indexes[] = new TupleIndex[descs.length] ;
        int i = 0 ;
        for ( String desc : descs )
        {
            indexes[i] = createTupleIndex(indexBuilder, recordFactory, location, primary, desc) ;
            i++ ;
        }
        return indexes ;
    }
    
    /** Testing */
    static TripleTable createTripleTableMem()
    {
        NodeTable nodeTable = NodeTableFactory.createMem(IndexBuilder.mem()) ;
        return createTripleTable(IndexBuilder.mem(), nodeTable, null, tripleIndexes) ;
    }

    /** Testing */
    public static TripleTable createTripleTable(IndexBuilder indexBuilder, NodeTable nodeTable, Location location, String...descs)
    {
        TupleIndex indexes[] = indexes(indexBuilder, indexRecordTripleFactory, location, primaryIndexTriples, descs) ;
        return new TripleTable(indexes, indexRecordTripleFactory, nodeTable, location) ;
    }

    // ---- All creation happnes here
    
    private static GraphTriplesTDB _createGraph(IndexBuilder indexBuilder, Location location)
    {
        NodeTable nodeTable = NodeTableFactory.create(indexBuilder, location) ;
        TripleTable table = createTripleTable(indexBuilder, nodeTable, location, tripleIndexes) ;
        ReorderTransformation transform = chooseOptimizer(location) ;
        DatasetPrefixes prefixes = new DatasetPrefixes(indexBuilder, location) ;
        return new GraphTriplesTDB(table, prefixes, transform, location) ;
    }
    
    /** Create or connect a TDB dataset (graph-level) */
    private static DatasetGraph _createDatasetGraph(IndexBuilder indexBuilder, Location location, String[] graphDesc, String[] quadDesc)
    {
        NodeTable nodeTable = NodeTableFactory.create(indexBuilder, location) ;
        TripleTable triples = createTripleTable(indexBuilder, nodeTable, location, graphDesc) ;
        QuadTable quads = createQuadTable(indexBuilder, nodeTable, location, quadDesc) ;
        DatasetPrefixes prefixes = new DatasetPrefixes(indexBuilder, location) ;
        //return new DatasetImpl(new DatasetGraphTDB(triples, quads, null, location)) ;
        return new DatasetGraphTDB(triples, quads, prefixes, chooseOptimizer(location), location) ;
    }

    // ----
    

    /** Testing */
    static QuadTable createQuadTableMem()
    {
        NodeTable nodeTable = NodeTableFactory.createMem(IndexBuilder.mem()) ;
        return createQuadTable(IndexBuilder.mem(), nodeTable, null, tripleIndexes) ;
    }
    
    private static QuadTable createQuadTable(IndexBuilder indexBuilder, NodeTable nodeTable,
                                             Location location, String...descs)
    {
        TupleIndex indexes[] = indexes(indexBuilder, indexRecordQuadFactory, location, primaryIndexQuads, descs) ;
        return new QuadTable(indexes, indexRecordQuadFactory, nodeTable, location) ;
    }
    
    private static TupleIndex createTupleIndex(IndexBuilder indexBuilder, RecordFactory recordFactory, Location location, String primary, String desc)
    {
        RangeIndex rIdx1 = indexBuilder.newRangeIndex(location, recordFactory, desc) ;
        TupleIndex tupleIndex = new TupleIndexRecord(desc.length(), new ColumnMap(primary, desc), recordFactory, rIdx1) ; 
        return tupleIndex ;
    }
    
    private static ReorderTransformation chooseOptimizer(Location location)
    {
        if ( location == null )
            return ReorderLib.identity() ;
        
        ReorderTransformation reorder = null ;
        if ( location.exists(Names.optStats) )
        {
            try {
                reorder = ReorderLib.weighted(location.getPath(Names.optStats)) ;
                logInfo.info("Statistics-based BGP optimizer") ;  
            } catch (SSEParseException ex) { 
                log.warn("Error in stats file: "+ex.getMessage()) ;
                reorder = null ;
            }
        }
        
        if ( reorder == null && location.exists(Names.optDefault) )
        {
            // Not as good but better than nothing.
            reorder = ReorderLib.fixed() ;
            logInfo.info("Fixed pattern BGP optimizer") ;  
        }
        
        if ( location.exists(Names.optNone) )
        {
            reorder = ReorderLib.identity() ;
            logInfo.info("Optimizer explicitly turned off") ;
        }

        if ( reorder == null )
            reorder = SystemTDB.defaultOptimizer ;
        
        if ( reorder == null )
            logExec.warn("No BGP optimizer") ;
        
        return reorder ; 
    }
    
//    // ----------
//    public static void enable()
//    {
//        TDBFactory.ImplFactory f = new TDBFactory.ImplFactory()
//        {
//            @Override
//            public Graph createGraph()
//            {
//                return FactoryGraphTDB.createGraphMem() ;
//            }
//    
//            @Override
//            public Graph createGraph(Location loc)
//            {
//                return FactoryGraphTDB.createGraph(loc) ;
//            }
//        } ;
//        TDBFactory.setImplFactory(f) ;
//    }
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