/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.modify;

import static com.hp.hpl.jena.sparql.modify.TemplateLib.template ;

import java.util.ArrayList ;
import java.util.Collection ;
import java.util.List ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.lib.MultiMap ;
import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphWrapper ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.engine.Plan ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot ;
import com.hp.hpl.jena.sparql.graph.NodeTransform ;
import com.hp.hpl.jena.sparql.graph.NodeTransformLib ;
import com.hp.hpl.jena.sparql.modify.request.Target ;
import com.hp.hpl.jena.sparql.modify.request.UpdateAdd ;
import com.hp.hpl.jena.sparql.modify.request.UpdateClear ;
import com.hp.hpl.jena.sparql.modify.request.UpdateCopy ;
import com.hp.hpl.jena.sparql.modify.request.UpdateCreate ;
import com.hp.hpl.jena.sparql.modify.request.UpdateDataDelete ;
import com.hp.hpl.jena.sparql.modify.request.UpdateDataInsert ;
import com.hp.hpl.jena.sparql.modify.request.UpdateDeleteWhere ;
import com.hp.hpl.jena.sparql.modify.request.UpdateDrop ;
import com.hp.hpl.jena.sparql.modify.request.UpdateDropClear ;
import com.hp.hpl.jena.sparql.modify.request.UpdateLoad ;
import com.hp.hpl.jena.sparql.modify.request.UpdateModify ;
import com.hp.hpl.jena.sparql.modify.request.UpdateMove ;
import com.hp.hpl.jena.sparql.modify.request.UpdateVisitor ;
import com.hp.hpl.jena.sparql.syntax.Element ;
import com.hp.hpl.jena.sparql.syntax.ElementGroup ;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph ;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock ;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory ;
import com.hp.hpl.jena.update.GraphStore ;
import com.hp.hpl.jena.update.UpdateException ;
import com.hp.hpl.jena.util.FileManager ;

/** Implementation of general purpose update reuest execution */ 
class UpdateEngineWorker implements UpdateVisitor
{
    private final GraphStore graphStore ;
    
    private final Binding initialBinding ;
    
    private final boolean alwaysSilent = true ;

    UpdateEngineWorker(GraphStore graphStore, Binding initialBinding)
    {
        this.graphStore = graphStore ;
        this.initialBinding = initialBinding ;
    }

    public void visit(UpdateDrop update)
    { execDropClear(update, false) ; }

    public void visit(UpdateClear update)
    { execDropClear(update, true) ; }

    // ReDo with gs* primitives
    private void execDropClear(UpdateDropClear update, boolean isClear)
    {
        if ( update.isAll() )
        {
            execDropClear(update, null, true) ;    // Always clear.
            execDropClearAllNamed(update, isClear) ;
        }
        else if ( update.isAllNamed() )
            execDropClearAllNamed(update, isClear) ;
        else if ( update.isDefault() )
            execDropClear(update, null, true) ;
        else if ( update.isOneGraph() )
            execDropClear(update, update.getGraph(), isClear) ;
        else
            throw new ARQInternalErrorException("Target is undefined: "+update.getTarget()) ;
    }

    private void execDropClear(UpdateDropClear update, Node g, boolean isClear)
    {
        if ( ! alwaysSilent )
        {
            if ( g != null && ! graphStore.containsGraph(g) && ! update.isSilent())
                error("No such graph: "+g) ;
        }
        
        if ( isClear )
            graph(graphStore, g).getBulkUpdateHandler().removeAll() ;
        else
            graphStore.removeGraph(g) ;
    }

    private void execDropClearAllNamed(UpdateDropClear update, boolean isClear)
    {
        // Avoid ConcurrentModificationException
        List<Node> list = Iter.toList(graphStore.listGraphNodes()) ;
        
        for ( Node gn : list )
            execDropClear(update, gn, isClear) ;
    }

    public void visit(UpdateCreate update)
    {
        Node g = update.getGraph() ;
        if ( g == null )
            return ;
        if ( graphStore.containsGraph(g) )
        {
            if ( ! alwaysSilent && ! update.isSilent() )
                error("Graph store already contains graph : "+g) ;
            return ;
        }
        // In-memory specific 
        graphStore.addGraph(g, GraphFactory.createDefaultGraph()) ;
    }

    public void visit(UpdateLoad update)
    {
        String source = update.getSource() ;
        Node dest = update.getDest() ;
        Graph g = graph(graphStore, dest) ;
        Model model = ModelFactory.createModelForGraph(g) ;
        try {
            FileManager.get().readModel(model, source) ;
        } catch (RuntimeException ex)
        {
            if ( ! update.getSilent() )
                throw ex ;
        }
    }

    public void visit(UpdateAdd update)
    { 
        gsCopyTriples(graphStore, update.getSrc(), update.getDest()) ;
    }

    public void visit(UpdateCopy update)
    { 
        gsCopy(graphStore, update.getSrc(), update.getDest()) ;
    }

    public void visit(UpdateMove update)
    { 
        gsCopy(graphStore, update.getSrc(), update.getDest()) ;
        gsDrop(graphStore, update.getSrc(), true) ;
    }

    // ----
    // Core operations
    
    private static void gsCopy(GraphStore gStore, Target src, Target dest)
    {
        gsClear(gStore, dest, true) ;
        gsCopyTriples(gStore, src, dest) ;
    }

    private static void gsCopyTriples(GraphStore gStore, Target src, Target dest)
    {
        Graph gSrc = graph(gStore, src) ;
        Graph gDest = graph(gStore, dest) ;
        // Ugly! but avoids concurrency problems.
        // TODO Revisit graph->graph triple copy.
        List<Triple> list = Iter.toList(gSrc.find(null, null, null)) ;
        gDest.getBulkUpdateHandler().add(list) ;
    }

    private static void gsClear(GraphStore gStore, Target target, boolean isSilent)
    {
        // No create.
        Graph g = graph(gStore, target) ;
        if ( target.isOneNamedGraph() )
        {
            if ( !gStore.containsGraph(target.getGraph()) )
            {
                if ( ! isSilent )
                    error("No such graph: "+g) ;
                return ;
            }
        }
        
        g.getBulkUpdateHandler().removeAll() ;
    }

    private static void gsDrop(GraphStore gStore, Target target, boolean isSilent)
    {
        if ( target.isDefault() )
            gStore.getDefaultGraph().getBulkUpdateHandler().removeAll() ;
        else
            gStore.removeGraph(target.getGraph()) ;
    }
    
    // ----
    
    public void visit(UpdateDataInsert update)
    {
        for ( Quad quad : update.getQuads() )
            graphStore.add(quad) ;
    }

    public void visit(UpdateDataDelete update)
    {
        for ( Quad quad : update.getQuads() )
            graphStore.delete(quad) ;
    }

    public void visit(UpdateDeleteWhere update)
    {
        List<Quad> quads = update.getQuads() ;
        // Convert bNodes to named variables first.
        quads = convertBNodesToVariables(quads) ;
        // Convert quads to a pattern.
        Element el = elementFromQuads(quads) ;
        List<Binding> bindings = evalBindings(el, null) ;
        execDelete(quads, null, bindings) ;
    }
    
    public void visit(UpdateModify update)
    {
        Node graph = update.getWithIRI() ;
        Query query = elementToQuery(update.getWherePattern()) ;
        
        // USING/USING NAMED
        processUsing(update, query) ;
        
        final List<Binding> bindings = evalBindings(query, graph) ;
        
        execDelete(update.getDeleteQuads(), graph, bindings) ;
        execInsert(update.getInsertQuads(), graph, bindings) ;
    }

    // Indirection for subsystems to support USING/USING NAMED.
    protected void processUsing(UpdateModify update, Query query)
    {
        if ( update.getUsing().size() > 0 || update.getUsingNamed().size() > 0 )
            Log.warn(this, "Graph selection from the dataset not supported - ignored") ;
        return ;
//        for ( Node n : update.getUsing() )
//            query.addGraphURI(n.getURI()) ;
//        for ( Node n : update.getUsingNamed() )
//            query.addNamedGraphURI(n.getURI()) ;
    }
    
    private static List<Quad> convertBNodesToVariables(List<Quad> quads)
    {
        NodeTransform bnodesToVariables = new NodeTransformBNodesToVariables() ;
        return NodeTransformLib.transformQuads(bnodesToVariables, quads) ;
    }
    
    private Element elementFromQuads(List<Quad> quads)
    {
        ElementGroup el = new ElementGroup() ;
        ElementTriplesBlock x = new ElementTriplesBlock() ;
        // Maybe empty??
        el.addElement(x) ;
        Node g = Quad.defaultGraphNodeGenerated ;
        
        for ( Quad q : quads )
        {
            if ( q.getGraph() != g )
            {
                g = q.getGraph() ;
                x = new ElementTriplesBlock() ;
                if ( g == null || g == Quad.defaultGraphNodeGenerated )
                    el.addElement(x) ;
                else
                {
                    ElementNamedGraph eng = new ElementNamedGraph(g, x) ;
                    el.addElement(eng) ;
                }
            }
            x.addTriple(q.asTriple()) ;
        }
        return el ;
    }

    private void execDelete(List<Quad> quads, Node dftGraph, List<Binding> bindings)
    {
        MultiMap<Node, Triple> acc = template(quads, dftGraph, bindings) ;
        if ( acc == null ) return ; 
        
        for ( Node gn : acc.keys() )
        {
            Collection<Triple> triples = acc.get(gn) ;
            graph(graphStore, gn).getBulkUpdateHandler().delete(triples.iterator()) ;
        }
    }

    private void execInsert(List<Quad> quads, Node dftGraph, List<Binding> bindings)
    {
        MultiMap<Node, Triple> acc = template(quads, dftGraph, bindings) ;
        if ( acc == null ) return ; 
        
        for ( Node gn : acc.keys() )
        {
            Collection<Triple> triples = acc.get(gn) ;
            graph(graphStore, gn).getBulkUpdateHandler().add(triples.iterator()) ;
        }
    }

    private Query elementToQuery(Element pattern)
    {
        if ( pattern == null )
            return null ;
        Query query = new Query() ;
        query.setQueryPattern(pattern) ;
        query.setQuerySelectType() ;
        query.setQueryResultStar(true) ;
        query.setResultVars() ;
        return query ;
    }
    
    protected List<Binding> evalBindings(Element pattern, Node dftGraph)
    {
        return evalBindings(elementToQuery(pattern), dftGraph) ;
    }
    
    static class DatasetGraphAltDefaultGraph extends DatasetGraphWrapper
    {
        private Graph dftGraph ;
        
        public DatasetGraphAltDefaultGraph(DatasetGraph dsg, Graph dftGraph)
        { super(dsg) ; setDefaultGraph(dftGraph) ; }
        
        @Override
        public Graph getDefaultGraph()
        { return dftGraph; }

        @Override
        public void setDefaultGraph(Graph g)
        { dftGraph = g ; }
    }
    
    protected List<Binding> evalBindings(Query query, Node dftGraph)
    {
        List<Binding> bindings = new ArrayList<Binding>() ;
        
        if ( query != null )
        {
            DatasetGraph dsg = graphStore ;
            if ( dftGraph != null )
            {
                Graph g = dsg.getGraph(dftGraph) ;
                dsg = new DatasetGraphAltDefaultGraph(dsg, g) ;
            }
            
            Plan plan = QueryExecutionFactory.createPlan(query, dsg, initialBinding) ;
            QueryIterator qIter = plan.iterator() ;

            for( ; qIter.hasNext() ; )
            {
                Binding b = qIter.nextBinding() ;
                bindings.add(b) ;
            }
            qIter.close() ;
        }
        else
        {
            if ( initialBinding != null )
                bindings.add(initialBinding) ;
            else
                bindings.add(BindingRoot.create()) ;
        }
        return bindings ;
    }
    
    private static Graph graph(GraphStore graphStore, Node gn)
    {
        if ( gn == null || gn == Quad.defaultGraphNodeGenerated )
            return graphStore.getDefaultGraph() ;
        else
            return graphStore.getGraph(gn) ;
    }

    private static Graph graph(GraphStore graphStore, Target target)
    {
        if ( target.isDefault() )
            return graphStore.getDefaultGraph() ;
        if ( target.isOneNamedGraph() )
            return graph(graphStore, target.getGraph()) ;
        error("Target does not name one graph: "+target) ;
        return null ;
    }

    private static void error(String msg)
    {
        throw new UpdateException(msg) ;
    }
}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
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