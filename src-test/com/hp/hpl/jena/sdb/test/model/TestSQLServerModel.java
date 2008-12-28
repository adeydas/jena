/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.test.model;

import junit.framework.TestSuite;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.test.StoreCreator;

public class TestSQLServerModel {
	
	public static junit.framework.Test suite() {
    	TestSuite ts = new TestSuite();
    	
    	ts.addTestSuite(TestSQLServerIndexModel.class);
    	ts.addTestSuite(TestSQLServerIndexQuadModel.class);
    	ts.addTestSuite(TestSQLServerHashModel.class);
    	ts.addTestSuite(TestSQLServerHashQuadModel.class);
    	
    	return ts;
	}
	
	public static class TestSQLServerIndexModel extends AbstractTestModelSDB {

		public TestSQLServerIndexModel(String name) {
			super(name);
		}
		
		@Override
		public Model getModel() {
			Store store = StoreCreator.getIndexSQLServer();
			return SDBFactory.connectDefaultModel(store);
		}
		
	}
	
	public static class TestSQLServerIndexQuadModel extends AbstractTestModelSDB {

		public TestSQLServerIndexQuadModel(String name) {
			super(name);
		}
		
		@Override
		public Model getModel() {
			Store store = StoreCreator.getIndexSQLServer();
			return SDBFactory.connectNamedModel(store, "http://example.com/graph");
		}
		
	}
	
	public static class TestSQLServerHashModel extends AbstractTestModelSDB {

		public TestSQLServerHashModel(String name) {
			super(name);
		}
		
		@Override
		public Model getModel() {
			Store store = StoreCreator.getHashSQLServer();
			return SDBFactory.connectDefaultModel(store);
		}
		
	}
	
	public static class TestSQLServerHashQuadModel extends AbstractTestModelSDB {

		public TestSQLServerHashQuadModel(String name) {
			super(name);
		}
		
		@Override
		public Model getModel() {
			Store store = StoreCreator.getHashSQLServer();
			return SDBFactory.connectNamedModel(store, "http://example.com/graph");
		}
		
	}

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