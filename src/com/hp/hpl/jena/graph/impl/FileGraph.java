/*
  (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: FileGraph.java,v 1.25 2005-03-10 14:33:48 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.impl;

import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.graph.TransactionHandler;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import com.hp.hpl.jena.shared.*;

import java.io.*;

/**
    A FileGraph is a memory-based graph that is optionally read in from a file
    when it is created, and is written back when it is closed. It is not 
    particularly robust, alas. 
    
    TODO: consider a version which saves "every now and then"
    
 	@author hedgehog
*/
public class FileGraph extends GraphMem
    {
    /**
        See FileGraph( f, create, strict, Reifier.ReificationStyle ).
    */
    public FileGraph( File f, boolean create, boolean strict )
        { this( f, create, strict, ReificationStyle.Minimal ); }
    
    /**
        The File-name of this graph, used to name it in the filing system 
    */
    protected File name;
    
    /**
        A model used to wrap the graph for the IO operations (since these are not
        yet available at the graph level).
    */
    protected Model model;
    
    /**
        The language used to read and write the graph, guessed from the filename's
        suffix.
    */
    protected String lang;
    
    /**
        Construct a new FileGraph who's name is given by the specified File,
        If create is true, this is a new file, and any existing file will be destroyed;
        if create is false, this is an existing file, and its current contents will
        be loaded. The language code for the file is guessed from its suffix.
        
     	@param f the File naming the associated file-system file
     	@param create true to create a new one, false to read an existing one
        @param strict true to throw exceptions for create: existing, open: not found
        @param style the reification style for the graph
    */
    public FileGraph( File f, boolean create, boolean strict, ReificationStyle style )
        {
        super( style );
        this.name = f;
        this.model = new ModelCom( this );
        this.lang = FileUtils.guessLang( this.name.toString() );
        if (create)
            { 
            if (f.exists() && strict) throw new AlreadyExistsException( f.toString() );
            }
        else
            readModel( this.model, strict );
        }
        
    protected void readModel( Model m, boolean strict )
        { readModelFrom( m, strict, name ); }
    
    protected void readModelFrom( Model m, boolean strict, File name )
        {
        FileInputStream in = null;
        try
            {
            in = new FileInputStream( name );
            model.read( in, "", this.lang );
            }
        catch (FileNotFoundException f)
            { if (strict) throw new DoesNotExistException( name.toString() ); }
        finally 
            {
            if (in != null) try {in.close();} catch (IOException ignore) {}
            }
        }
        
    /**
        As for FileGraph(File,boolean), except the name is given as a String.
     */
    public FileGraph( String s, boolean create )
        { this( new File( s ), create, true ); }
        
    public static FileGraph create()
        { return new FileGraph( FileUtils.tempFileName( "xxx", ".rdf" ), true, true ); }
        
    /**
        Answer true iff the filename string given is plausibly the name of a 
        graph, ie, may have RDF content. We appeal to FileUtils - if it can 
        guess an RDF language name, we deliver true, otherwise false.
        
     	@param name the leaf component of a filename
     	@return true if it is likely to be an RDF file
    */
    public static boolean isPlausibleGraphName( String name )
        { return FileUtils.guessLang( name, null ) != null; }
        
    /**
        Write out and then close this FileGraph. 
    */
    public void close()
        {
        saveContents( name );
        super.close();
        }

    /**
        The graph is written out to the 
        named file in the language guessed from the suffix, and then the 
        parent close is invoked. The write-out goes to an intermediate file
        first, which is then renamed to the correct name, to try and ensure
        that the output is either done completely or not at all.
    */
    protected void saveContents( File targetName ) 
        {
        try
            {
            File intermediate = new File( targetName.getPath() + ".new" );
            FileOutputStream out = new FileOutputStream( intermediate );
            model.write( out, lang );
            out.close();
            updateFrom( targetName, intermediate );
            }
        catch (Exception e)
            { throw new JenaException( e ); }
        }
        
    /**
        The file intermediate has the new file contents. We want to move
        them to the current file. renameTo doesn't have a powerful enough
        semantics, so we anticipate failure and attempt to bypass it ...
    <p>
        If the rename works, that's fine. If it fails, we delete the old file if it
        exists, and try again.
    */
    protected void updateFrom( File targetName, File intermediate )
        {
        if (intermediate.renameTo( targetName ) == false)
            {
            if (targetName.exists()) mustDelete( targetName );
            mustRename( intermediate, targetName );
            }
        }    
        
    private void mustDelete( File f )
        { if (f.delete() == false) throw new JenaException( "could not delete " + f ); }
        
    private void mustRename( File from, File to )
        { 
        if (from.renameTo( to ) == false) 
            throw new JenaException( "could not rename " + from + " to " + to ); 
        }

    public TransactionHandler getTransactionHandler()
        { if (th == null) th = new FileGraphTransactionHandler( this ); 
        return th; }
    
    protected TransactionHandler th;
    }

/*
    (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/