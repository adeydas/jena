/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package sdb;

import java.util.ArrayList;
import java.util.List;

import sdb.cmd.CmdArgsDB;
import arq.cmdline.ArgDecl;
import arq.cmdline.CmdArgModule;
import arq.cmdline.CmdGeneral;
import arq.cmdline.ModBase;

import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sparql.util.Utils;

import dev.inf.TupleTable;

public class sdbtuple extends CmdArgsDB
{
    static class ModTuple extends ModBase
    {
        
        public void registerWith(CmdGeneral cmdLine)
        {}

        public void processArgs(CmdArgModule cmdLine)
        {}
        
    }
    
    private static ModTuple modTuple = new ModTuple() ;
    
    // Commands
    private static ArgDecl argDeclCmdPrint = new ArgDecl(false, "print") ;
    private static ArgDecl argDeclCmdLoad = new ArgDecl(true, "load") ;
    private static ArgDecl argDeclCmdCreate = new ArgDecl(true, "create") ;
    private static ArgDecl argDeclCmdDrop = new ArgDecl(true, "drop") ;

    // Indexes?
    
    private static ArgDecl argDeclCmdTable= new ArgDecl(true, "table") ;
    
    boolean cmdPrint = false ;
    boolean cmdLoad = false ;
    boolean cmdCreate = false ;
    boolean cmdDrop = false ;
    
    String loadFile = null ;
    
    public static void main(String ... args) { new sdbtuple(args).main() ; }
    
    public List<String> tables = new ArrayList<String>() ;
    public sdbtuple(String... argv)
    {
        super(argv) ;
        getUsage().startCategory("Tuple") ;
        add(argDeclCmdTable, "--table=TableName", "Tuple table to operate on (incldues positional arguments as well)") ;
        
        add(argDeclCmdPrint, "--print", "Print a tuple table") ;
        add(argDeclCmdPrint, "--load", "Load a tuple table") ;
        add(argDeclCmdPrint, "--create", "Create a tuple table") ;
        add(argDeclCmdPrint, "--drop", "Drop a tuple table") ;
    }
    
    @Override
    protected void execCmd(List<String> positionalArgs)
    {
        int count = countBool(cmdPrint, cmdLoad, cmdCreate, cmdDrop) ;

        if ( count == 0 )
            cmdError("Nothing to do!", true) ;
        if ( count > 1 )
            cmdError("Too much to do!", true) ;
        
        for ( String tableName : tables )
            execOne(tableName) ;
    }
    
    private int countBool(boolean...bools)
    {
        int count = 0 ; 
        for ( int i = 0 ; i < bools.length ; i++ )
            if ( bools[i] ) count++ ;
        return count ;
    }
    
    @Override
    protected void processModulesAndArgs()
    {
        if ( !contains(argDeclCmdTable) && getNumPositional() == 0 )
            cmdError("No tables specified", true) ;
        
        @SuppressWarnings("unchecked")
        List<String>x = (List<String>)getPositional() ;
        tables.addAll(x) ;
        
        @SuppressWarnings("unchecked")
        List<String>y = (List<String>)getValues(argDeclCmdTable) ;
        tables.addAll(y) ;
        
        cmdPrint = contains(argDeclCmdPrint) ;

        cmdLoad = contains(argDeclCmdLoad) ;
        if ( cmdLoad )
            loadFile = getValue(argDeclCmdLoad) ;
        
        cmdCreate = contains(argDeclCmdCreate) ;
        cmdDrop = contains(argDeclCmdDrop) ;
    }

    @Override
    protected String getSummary()
    { return getCommandName()+" --sdb <SPEC> [--print|--??] [--table TableName] TableName..." ; }

    @Override
    protected String getCommandName() { return Utils.className(this) ; }

    static final String divider = "- - - - - - - - - - - - - -" ;
    boolean needDivider = false ;
    private void divider()
    {
        if ( needDivider ) System.out.println(divider) ;
        needDivider = true ;
    }
    
    // ---- Execution
    
    private void execOne(String tableName)
    {
        if ( cmdPrint ) execPrint(tableName) ;
        if ( cmdLoad ) execLoad(tableName) ;
    }

    private void execPrint(String tableName)
    {
        Store store = getStore() ;
        TupleTable table = new TupleTable(store, tableName) ;
        divider() ;
        table.dump() ;
    }

    private void execLoad(String tableName)
    {
        cmdError("Tuple load - not implemented (yet)", true) ;
        Store store = getStore() ;
        TupleTable table = new TupleTable(store, tableName) ;
        
    }

}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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