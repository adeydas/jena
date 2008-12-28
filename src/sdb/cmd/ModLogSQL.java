/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package sdb.cmd;

import java.util.List;

import com.hp.hpl.jena.sdb.sql.SDBConnection;

import arq.cmd.CmdException;
import arq.cmdline.ArgDecl;
import arq.cmdline.CmdArgModule;
import arq.cmdline.CmdGeneral;
import arq.cmdline.ModBase;

public class ModLogSQL extends ModBase
{
    // Logging.
    // query, sql
    protected final ArgDecl argDeclLogSQL          = new ArgDecl(true, "log") ;

    public void registerWith(CmdGeneral cmdLine)
    {
        cmdLine.getUsage().startCategory("DB logging") ;
        cmdLine.add(argDeclLogSQL,         "--log=", "SQL logging [none, all, query, exceptions, statement]") ;
        
    }

    public void processArgs(CmdArgModule cmdLine)
    {
        @SuppressWarnings("unchecked")
        List<String> vals = cmdLine.getValues(argDeclLogSQL) ;
        for ( String v : vals )
        {
            if ( v.equalsIgnoreCase("none") )
            {
                SDBConnection.logSQLExceptions = false ;
                SDBConnection.logSQLQueries = false ;
                SDBConnection.logSQLStatements = false ;
                continue ;
            }
            if ( v.equalsIgnoreCase("query") || v.equalsIgnoreCase("queries") )
            {
                SDBConnection.logSQLQueries = true ;
                continue ;
            }
            if ( v.equalsIgnoreCase("exception") || v.equalsIgnoreCase("exceptions") )
            {
                SDBConnection.logSQLExceptions = true ;
                continue ;
            }
            if ( v.equalsIgnoreCase("statement") || v.equalsIgnoreCase("statements") )
            {
                SDBConnection.logSQLStatements = true ;
                continue ;
            }
            if ( v.equalsIgnoreCase("all") || v.equalsIgnoreCase("sql") )
            {
                SDBConnection.logSQLExceptions = true ;
                SDBConnection.logSQLQueries = true ;
                SDBConnection.logSQLStatements = true ;
                continue ;
            }
            
            throw new CmdException("Not recognized as a log form: "+v) ;
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