/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package lib;

import java.io.File;
import java.io.FilenameFilter;

public class FileOps
{
    private FileOps() {}
    
    /** Delete a file
     * 
     * @param filename
     */
    public static void delete(String filename)
    {
        delete(new File(filename), true) ;
    }
    
    /* Delete a file - don't check it worked */
    
    public static void deleteSilent(String filename)
    {
        delete(new File(filename), false) ;
    }
    
    public static void delete(File f, boolean reportExistsAfter)
    {
        try {
            /* Note: On windows, deleting a file which has been memory
             * mapped does not delete the file.
             */ 
            f.delete() ;
            if ( reportExistsAfter && f.exists() )
                System.err.println("*** Still exists: "+f) ;
            
        } catch (Exception ex)
        {
            System.err.println("Exception: "+ex) ;
            ex.printStackTrace(System.err) ;
            System.exit(1) ;
        }
    }
    
    public static void clearDirectory(String dir)
    {
        try {
            File d = new File(dir) ;
            
            FilenameFilter filter = new FilenameFilter(){
                @Override
                public boolean accept(File dir, String name)
                {
                    return name.matches(".*\\..*") ;
                }} ;
                
            for ( File f : d.listFiles(filter))
                delete(f, false) ;
           
        } catch (Exception ex)
        {
            System.err.println("Exception: "+ex) ;
            ex.printStackTrace(System.err) ;
            System.exit(1) ;
        }
    }

    public static Tuple<String> parse(String filename)
    {
        String path = null ;
        String basename = filename ;
        String ext = null ;
        
        // split on '/'?
        
        int j = filename.lastIndexOf('/') ;
        if ( j >= 0 )
        {
            path = filename.substring(0, j) ;
            basename = filename.substring(j+1) ;
        }
        
        int i = basename.lastIndexOf('.') ;
        
        if ( i > -1 )
        {
            ext = basename.substring(i+1) ;
            basename = basename.substring(0, i) ;
        }
        
        return Tuple.create(path, basename, ext) ;
    }
    
    
    
    public static String getExt(String filename)
    {
        int i = filename.lastIndexOf('.') ;
        int j = filename.lastIndexOf('/') ;
        if ( i > j )
            return filename.substring(i+1) ;
        return null ;
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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