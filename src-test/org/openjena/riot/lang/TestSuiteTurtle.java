/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot.lang;


import junit.framework.TestSuite ;
import org.junit.runner.RunWith ;
import org.junit.runners.AllTests ;
import org.openjena.riot.TestVocabRIOT ;

@RunWith(AllTests.class)
public class TestSuiteTurtle extends TestSuite
{
    // The base URI of the test directory in the submission
    // NB The test results use http://www.w3.org/2001/sw/DataAccess/df1/tests/ in N-Triples (??!!)
    private static final String manifest1 = "testing/RIOT/TurtleStd/manifest.ttl" ;
    private static final String manifest2 = "testing/RIOT/TurtleStd/manifest-bad.ttl" ;

    static public TestSuite suite()
    {
        TestSuite ts = new TestSuite("Turtle") ;
        // The good ..
        ts.addTest(FactoryTestRiotTurtle.make(manifest1, TestVocabRIOT.TestInOut, "Turtle-")) ;
        // .. the bad ...
        ts.addTest(FactoryTestRiotTurtle.make(manifest2, TestVocabRIOT.TestInOut, "Turtle-")) ;
        return ts ;
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