/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package tdb.cmdline;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.util.Utils;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.pgraph.PGraphBase;

import arq.cmd.CmdException;
import arq.cmdline.CmdARQ;
import arq.cmdline.ModAssembler;

public abstract class CmdTDB extends CmdARQ
{
    private PGraphBase graph = null ; 
    private Model model = null ;
    
    protected ModAssembler modAssembler =  new ModAssembler() ;
    protected ModLocation modLocation =  new ModLocation() ;
    
    protected CmdTDB(String[] argv)
    {
        super(argv) ;
        TDB.init() ;
        super.addModule(modAssembler) ;
        super.addModule(modLocation) ;
    }
    
    @Override
    protected void processModulesAndArgs()
    {
        super.processModulesAndArgs() ;
        if ( modLocation.getLocation() == null && modAssembler.getAssemblerFile() == null )
            throw new CmdException("No assembler file and no location") ;
             
        if ( modLocation.getLocation() != null && modAssembler.getAssemblerFile() != null )
            throw new CmdException("Both an assembler file and a location") ;
    }
    
    protected Model getModel()
    {
        if ( model != null )
            model = ModelFactory.createModelForGraph(getGraph()) ;
        return model ;
    }
    
    protected PGraphBase getGraph()
    {
        if ( graph != null )
            return graph ;
        
        Model model = null ;
        
        if ( modAssembler.getAssemblerFile() != null )
            model = TDBFactory.assembleModel(modAssembler.getAssemblerFile()) ;
        else
            model = TDBFactory.createModel(modLocation.getLocation()) ;
        graph = (PGraphBase)model.getGraph() ;
        return graph ;
    }
    
    @Override
    protected String getCommandName()
    {
        return Utils.className(this) ;
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