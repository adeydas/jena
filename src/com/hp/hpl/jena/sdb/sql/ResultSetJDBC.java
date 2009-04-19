/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/* Indirect for possible statement management later.
 * Currectly, one statement - one result set so close both together. 
 * Later, prepared statements and or statement reference counting.
 * But the one result set per statement rule of JDBC may limit gains somewhat.
 * Also, it hides java.sql declarations. 
 */

public class ResultSetJDBC
{
    //static private Logger log = LoggerFactory.getLogger(ResultSetJDBC.class) ;
    
    private Statement statement ;
    private ResultSet resultSet ;
    public ResultSetJDBC(Statement s, ResultSet rs)
    {
        this.statement = s ;
        this.resultSet = rs ;
    }
    
    public ResultSet get() { return resultSet ; }
    
    public void close()
    {
        try {
            resultSet.close() ;
            statement.close() ;
        }
        catch (SQLException ex)
        {
            throw new SDBExceptionSQL("ResultSetJDBC.close", ex) ;
        }
        
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