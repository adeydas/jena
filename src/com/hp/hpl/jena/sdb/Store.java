/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb;

import com.hp.hpl.jena.graph.Node;

import com.hp.hpl.jena.sdb.compiler.QueryCompilerFactory;
import com.hp.hpl.jena.sdb.layout2.TableDescNodes;
import com.hp.hpl.jena.sdb.layout2.TableDescQuads;
import com.hp.hpl.jena.sdb.layout2.TableDescTriples;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.*;


/** A Store is one RDF dataset instance - it is the unit of query.
 *  The Store class is the SDB specific mechanisms need to implement
 *  an RDF Dataset.  DatasetStore provide the dataset interface.
 *  
 *  A store consists of a number of handlers for different aspects of
 *  the process of setting up and querying a database-backed Store.  This
 *  means Store for new databases can be assmelbed from those standard
 *  components that work, with database-specific code only where necessary.
 *  A common case if the formatting of the database - this is often DB-specific
 *  yet SQL generation of query is more standard.       
 * 
 * @author Andy Seaborne
 */

public interface Store
{
    /** Return the connection to the implementing database */
    public SDBConnection getConnection() ;
    
    /** Return the producer of processors that turn SPARQL queries into SQL expressions */
    public QueryCompilerFactory    getQueryCompilerFactory() ; 
    
    /** Factory for SQL bridges for this store */ 
    public SQLBridgeFactory getSQLBridgeFactory() ;
    
    /** Get the SQL-from-relational algebra generator */ 
    public SQLGenerator     getSQLGenerator() ;
    
    /** Return the processor that creates the database tables */
    public StoreFormatter   getTableFormatter() ;
    
    /** Return the (bulk) loader */
    public StoreLoader      getLoader() ;
    
    /** Return the configuration of this Store */
    public StoreConfig      getConfiguration() ;
    
    /** Return the database type of the store */
    public DatabaseType     getDatabaseType() ;
    
    /** Return the layout type of the store */
    public LayoutType       getLayoutType() ;
    
    /** Stores should be closed explicitly. 
     *  Some stores may require specific finalization actions (e.g. embedded databases),
     *  and some stores may be able to release system resources.
     */  
    public void  close() ;

    /** Has this store been closed? **/
    public boolean isClosed();
    
    /** Get the size of this store **/
    public long  getSize() ;
    
    /** Get the size of the graph corresponding to graphNode **/
    public long getSize(Node graphNode);
    
    /** Where the default graph is store */ 
    public TableDescTriples     getTripleTableDesc() ;
    
    /** Where the named graphs are in is store */ 
    public TableDescQuads       getQuadTableDesc() ;
    
    /** Location of the nodes in the store (if meaningful) */  
    public TableDescNodes       getNodeTableDesc() ;

    // Use the SPARQL query : SELECT ?g {GRAPH ?g {}}
//    /** List the Nodes of the named graphs */
//    public Iterator<Node> listNamedGraphs() ;
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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