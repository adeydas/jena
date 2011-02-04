/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot;

import java.io.InputStream ;

import org.openjena.atlas.io.IO ;
import org.openjena.atlas.lib.IRILib ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.riot.lang.LangRIOT ;
import org.openjena.riot.lang.SinkQuadsToDataset ;
import org.openjena.riot.lang.SinkTriplesToGraph ;
import org.openjena.riot.tokens.Tokenizer ;
import org.openjena.riot.tokens.TokenizerFactory ;

import com.hp.hpl.jena.graph.Factory ;
import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.lib.DatasetLib ;

/** Convenience operations to read RDF into graphs and datasets, 
 * optionally creating an in-memory object as container.
 * Methods named "<code>load</code>" create containers, methods,
 * called "<code>read</code>" take a container as argument.  
 */

public class RiotLoader
{

    /** Parse a file and return the quads in a dataset (in-memory) */ 
    public static DatasetGraph load(String filename)
    {
        return load(filename, null) ;
    }
    
    /** Parse a file and return the quads in a dataset (in-memory) */ 
    public static DatasetGraph load(String filename, Lang lang)
    {
        return load(filename, lang, null) ; 
    }
    
    /** Parse a file and return the quads in a dataset (in-memory) */ 
    public static DatasetGraph load(String filename, Lang lang, String baseURI)
    {
        if ( lang == null )
            lang = Lang.guess(filename, Lang.NQUADS) ;
        DatasetGraph dsg = DatasetLib.createDatasetGraphMem() ;
        read(filename, dsg, lang, baseURI) ;
        return dsg ;
    }
    
    /** Parse a string and return the quads in a dataset (in-memory) (convenience operation)*/ 
    public static DatasetGraph datasetFromString(String string, Lang language, String baseURI)
    {
        DatasetGraph dsg = DatasetLib.createDatasetGraphMem() ;
        Sink<Quad> sink = RiotLoader.datasetSink(dsg) ;
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerString(string) ;

        LangRIOT parser = RiotReader.createParserQuads(tokenizer, language, baseURI, sink) ;
        parser.parse() ;
        sink.flush();
        return dsg;
    }

    /** Parse a file and return a graph */ 
    public static Graph loadGraph(String filename)
    {
        return loadGraph(filename, null) ;
    }
    
    /** Parse a file and return a graph */ 
    public static Graph loadGraph(String filename, Lang lang)
    {
        return loadGraph(filename, lang, null) ; 
    }
    
    /** Parse a file and return a graph */ 
    public static Graph loadGraph(String filename, Lang lang, String baseURI)
    {
        if ( lang == null )
            lang = Lang.guess(filename, Lang.NTRIPLES) ;
        Graph g = Factory.createDefaultGraph() ;
        read(filename, g, lang, baseURI) ;
        return g ;
    }

    /** Parse a string and return the triples in a graph (in-memory) (convenience operation)*/ 
    public static Graph graphFromString(String string, Lang language, String baseURI)
    {
        Graph g = Factory.createDefaultGraph() ;
        Sink<Triple> sink = graphSink(g) ;
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerString(string) ;
        LangRIOT parser = RiotReader.createParserTriples(tokenizer, language, baseURI, sink) ;
        parser.parse() ;
        sink.flush();
        return g ;
    }
    
    /** Parse a file into a dataset graph */ 
    public static void read(String filename, DatasetGraph dataset)
    {
        Lang lang = Lang.guess(filename) ;
        if ( lang == null )
            throw new RiotException("Can't guess language for "+filename) ; 
        String baseURI = IRILib.filenameToIRI(filename) ;
        InputStream input = IO.openFile(filename) ;
        read(input, dataset, lang, baseURI) ;
    }
    
    /** Parse a file to a dataset */ 
    public static void read(String filename, DatasetGraph dataset, Lang lang)
    {
        read(filename, dataset, lang, null) ;
    }
    
    /** Parse a file to a dataset */ 
    public static void read(String filename, DatasetGraph dataset, Lang lang, String baseURI)
    {
        // All filename/DatasetGraph calls come through here.
        // ILLEGAL BASE NOT AN OPTION.
        baseURI = chooseBaseIRI(baseURI, filename) ;
        InputStream input = IO.openFile(filename) ;
        read(input, dataset, lang, baseURI) ;
    }
    
    /** Parse an input stream and send the quads to a dataset */ 
    public static void read(InputStream input, DatasetGraph dataset, Lang language, String baseURI)
    {
        if ( language.isQuads() )
        {
            Sink<Quad> sink = datasetSink(dataset) ;
            readQuads(input, language, baseURI, sink) ;
        }
        else
        {
            Sink<Triple> sink = graphSink(dataset.getDefaultGraph()) ;
            readTriples(input, language, baseURI, sink) ;
        }
    }
    

    /* Parse a file into a graph */
    public static void read(String filename, Graph graph)
    {
        read(filename, graph, null) ;
    }

    /* Parse a file into a graph */
    public static void read(String filename, Graph graph, Lang lang)
    {
        read(filename, graph, lang, null) ;
        
    }

    /* Parse a file into a graph */
    public static void read(String filename, Graph graph, Lang lang, String baseURI)
    {
        if ( lang == null )
            lang = Lang.guess(filename, Lang.NTRIPLES) ;
        baseURI = chooseBaseIRI(baseURI, filename) ;
        InputStream input = IO.openFile(filename) ;
        read(input, graph, lang, baseURI) ;
    }

    /* Parse a file into a graph */
    public static void read(InputStream input, Graph graph, Lang lang, String baseURI)
    {
        Sink<Triple> sink = graphSink(graph) ;
        readTriples(input, lang, baseURI, sink) ;
    }

    /** Parse an input stream and send the quads to the sink */ 
    public static void readQuads(InputStream input, Lang language, String baseURI, Sink<Quad> sink)
    {
        if ( ! language.isQuads() )
        //if ( language != Lang.NQUADS && language != Lang.TRIG )
            throw new RiotException("Language not supported for quads: "+language) ;
        
        LangRIOT parser = RiotReader.createParserQuads(input, language, baseURI, sink) ;
        parser.parse() ;
        sink.flush() ;
    }

    /** Parse an input stream and send the triples to the sink */ 
    public static void readTriples(InputStream input, Lang language, String baseURI, Sink<Triple> sink)
    {
        if ( ! language.isTriples() )
            throw new RiotException("Language not supported for triples: "+language) ;
        LangRIOT parser = RiotReader.createParserTriples(input, language, baseURI, sink) ;
        parser.parse() ;
        sink.flush();
//        LangRIOT parser ;
//        switch (language)
//        { case NTRIPLES :
//            parser = RiotReader.createParserNTriples(input, sink) ;
//            break ;
//        case TURTLE:
//            parser = RiotReader.createParserTurtle(input, baseURI, sink) ;
//            break ;
//        default:
//            throw new RiotException("Language not supported for triples: "+language) ;
//        }
//        parser.parse() ;
//        sink.flush();
//        return ;
    }

    // Better place?
    // DatasetLoader + "ModelLib" with model graph versions
    public static Sink<Triple> graphSink(Graph graph)
    {
        return new SinkTriplesToGraph(graph) ;
    }


    public static Sink<Quad> datasetSink(DatasetGraph dataset)
    {
        return new SinkQuadsToDataset(dataset) ;
    }
    
    private static String chooseBaseIRI(String baseURI, String filename)
    {
        return RiotReader.chooseBaseIRI(baseURI, filename) ;
    }
    
}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
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