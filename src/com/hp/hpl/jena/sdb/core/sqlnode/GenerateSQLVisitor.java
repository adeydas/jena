/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core.sqlnode;

import static com.hp.hpl.jena.sdb.iterator.Stream.map;
import static com.hp.hpl.jena.sdb.iterator.Stream.toSet;

import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.core.Annotations;
import com.hp.hpl.jena.sdb.core.sqlexpr.S_Equal;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExpr;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExprList;
import com.hp.hpl.jena.sdb.iterator.Transform;
import com.hp.hpl.jena.sdb.shared.SDBInternalError;
import com.hp.hpl.jena.sdb.shared.SDBNotImplemented;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.Utils;

// This is not a general purpose SQL writer - it needs only work with the
// SQL node trees that the SDB compiler generate.
// 
// It just writes out the tree - it does not optimize it in anyway (that
// happens before this stage). 

public class GenerateSQLVisitor implements SqlNodeVisitor
{
    // Annotate should ensureEndofLine ?
    private static Log log = LogFactory.getLog(GenerateSQLVisitor.class) ;
    
    protected IndentedWriter out ;
    int levelSelectBlock = 0 ;
    
    // Per Generator
    public boolean outputAnnotations = ARQ.getContext().isTrueOrUndef(SDB.annotateGeneratedSQL) ;
    private static final int annotationColumn = 40 ;
    private static boolean commentSQLStyle = true ;
    
    public GenerateSQLVisitor(IndentedWriter out)
    { this.out = out ; }
    
    public void visit(SqlProject sqlNode)   { shouldNotSee(sqlNode) ; }
    public void visit(SqlDistinct sqlNode)  { shouldNotSee(sqlNode) ; }
    public void visit(SqlRestrict sqlNode)  { shouldNotSee(sqlNode) ; }
    public void visit(SqlSlice sqlNode)     { shouldNotSee(sqlNode) ; }
    public void visit(SqlRename sqlNode)    { shouldNotSee(sqlNode) ; }

    private void shouldNotSee(SqlNode sqlNode)
    { throw new SDBInternalError("Didn't expect: "+Utils.className(sqlNode)) ; }

    // If nested (subquery) 
    
    public void visit(SqlSelectBlock sqlSelectBlock)
    {
        // Need a rename and alias if:
        //   Not top
        //   Not merely a table inside.
        
        levelSelectBlock++ ;
        
        if ( levelSelectBlock > 1 )
        {
            // Alias needed.
//            SqlRename rename = SqlRename.view("X", sqlSelectBlock) ;
//            rename.visit(this) ;
//            levelSelectBlock-- ;
//            return ;
        }
        
        out.print("SELECT ") ;
        if ( sqlSelectBlock.getDistinct() )
            out.print("DISTINCT ") ;
        if ( annotate(sqlSelectBlock) ) 
            out.ensureStartOfLine() ;
        out.incIndent() ;
        print(sqlSelectBlock.getCols()) ;
        out.decIndent() ;
        out.ensureStartOfLine() ;

        // FROM
        out.print("FROM") ;
        if ( ! sqlSelectBlock.getSubNode().isTable() )
            out.println();
        else
            out.print(" ");
        out.incIndent() ;
        outputNode(sqlSelectBlock.getSubNode(), true) ;
        //sqlSelectBlock.getSubNode().visit(this) ;
        out.decIndent() ;
        out.ensureStartOfLine() ;

        // WHERE
        if ( sqlSelectBlock.getConditions().size() > 0 )
            genWHERE(sqlSelectBlock.getConditions()) ;

        // LIMIT/OFFSET
        out.ensureStartOfLine() ;
        genLimitOffset(sqlSelectBlock) ;
        levelSelectBlock-- ;

    }

    protected void genLimitOffset(SqlSelectBlock sqlSelectBlock)
    {
        if ( sqlSelectBlock.getLength() >= 0 )
            out.println("LIMIT "+sqlSelectBlock.getLength()) ;
        if ( sqlSelectBlock.getStart() >= 0 )
            out.println("OFFSET "+sqlSelectBlock.getStart()) ;
        
    }
    
    private void print(List<ColAlias> cols)
    {
        String sep = "" ;
        if ( cols.size() == 0 )
        {
            // Can happen - e.g. query with no variables.
            //log.info("No SELECT columns") ;
            out.print("*") ;
        }

        // Put common prefix on same line
        String currentPrefix = null ; 
        String splitMarker = "." ;

        for ( ColAlias c : cols )
        {
            out.print(sep) ;
            
            // Choose split points.
            String cn = c.getColumn().getFullColumnName() ;
            int j = cn.lastIndexOf(splitMarker) ;
            if ( j == -1 )
                currentPrefix = null ;
            else
            {
                String x = cn.substring(0, j) ;
                if ( currentPrefix != null && ! x.equals(currentPrefix) )
                    out.println() ;

                currentPrefix = x ;
            }

            
            
            sep = ", " ;
            out.print(c.getColumn().getFullColumnName()) ;
          
            if ( c.getAlias() != null )
            {
                out.print(aliasToken()) ;
                out.print(c.getAlias().getColumnName()) ;
            }
        }
    }

    private void genWHERE(SqlExprList conditions)
    {
        out.print("WHERE") ;
        out.print(" ") ;
        out.incIndent() ;
        conditionList(conditions) ;
        out.decIndent() ;
    }
    
    public void visit(SqlTable table)
    {
        out.print(table.getTableName()) ;
        out.print(aliasToken()) ;
        out.print(table.getAliasName()) ;
        annotate(table) ;
    }

    public void visit(SqlJoinInner join)
    {
        join = rewrite(join) ;
        visitJoin(join) ;
    }

    public SqlJoinInner rewrite(SqlJoinInner join)
    {
        if ( ! join.getRight().isInnerJoin() )
            return join ;

        // if ( join(A, join(B, C)) ) rewrite as join(join(A,B),C)
        // this then is written without brackets (and so scope changing)
        // TODO abstract as organiseJoin(List<join elements>)
        // and remember to do top down to find maximal join trees
        
        SqlJoinInner right = join.getRight().asInnerJoin() ;

        String alias1 = join.getAliasName() ;
        String alias2 = right.getAliasName() ;

        SqlNode sn_a = join.getLeft() ;
        SqlNode sn_b = right.getLeft() ;
        SqlNode sn_c = right.getRight() ;

        SqlExprList conditions = new SqlExprList(join.getConditions()) ; 
        conditions.addAll(right.getConditions()) ; 

        Set<SqlTable> tables_ab = sn_a.tablesInvolved() ;
        tables_ab.addAll(sn_b.tablesInvolved()) ;

        SqlExprList newCond_ab = new SqlExprList() ;  // Goes to new join(A,B)
        SqlExprList newCond_c = new SqlExprList() ;   // Goes to new join(,C)
        // Place conditions
        for ( SqlExpr e : conditions )
        {
            Set<SqlColumn> cols = e.getColumnsNeeded() ;
            // columns to tables.
            Set<SqlTable> tables = tables(cols) ;
            // Are the tables contained in tables_ab?
            tables.removeAll(tables_ab) ;

            if ( tables.size() == 0 )
                newCond_ab.add(e) ;
            else
                newCond_c.add(e) ;
        }
        if ( newCond_ab.size()+newCond_c.size() != conditions.size() )
            log.fatal(String.format("Conditions mismatch: (%d,%d,%d)",
                                    newCond_ab.size(), newCond_c.size(), conditions.size())) ;


        SqlJoinInner join2 = new SqlJoinInner(sn_a, sn_b) ;
        join2.addConditions(newCond_ab) ;
        join2 = new SqlJoinInner(join2, sn_c) ;
        join2.addConditions(newCond_c) ;
        return join2 ;
    }
    
    static final Transform<SqlColumn, SqlTable> colToTable = new Transform<SqlColumn, SqlTable>() {
        public SqlTable convert(SqlColumn item) { return item.getTable() ; }
    } ;
    
    private static Set<SqlTable> tables(Set<SqlColumn> cols)
    {
        return toSet(map(cols, colToTable)) ;
    }

    public void visit(SqlJoinLeftOuter join)    { visitJoin(join) ; }

    public void visit(SqlCoalesce sqlNode)
    {
        out.print("SELECT ") ;
        
        boolean first = true ;
        SqlJoin join = sqlNode.getJoinNode() ;
        // Rough draft code.
        for ( Var v : sqlNode.getCoalesceVars() )
        {
            if ( ! first )
                out.print(", ") ;
            SqlColumn col = sqlNode.getIdScope().findScopeForVar(v).getColumn() ;
            SqlColumn leftCol = join.getLeft().getIdScope().findScopeForVar(v).getColumn() ;
            SqlColumn rightCol = join.getRight().getIdScope().findScopeForVar(v).getColumn() ;
            
            out.print("COALESCE(") ;
            out.print(leftCol.getFullColumnName()) ;
            out.print(", ") ;
            out.print(rightCol.getFullColumnName()) ;
            
            out.print(")") ;
            out.print(aliasToken()) ;
            out.print(col.getColumnName()) ;
            first = false ;
        }
        
        // And other vars we want.
        
        for ( Var v : sqlNode.getNonCoalesceVars() )
        {
            if ( ! first )
                out.print(", ") ;
            first = false ;
            
            // Need generated names.
            SqlColumn colSub = join.getIdScope().findScopeForVar(v).getColumn() ;
            SqlColumn col = sqlNode.getIdScope().findScopeForVar(v).getColumn() ;

            out.print(colSub.getFullColumnName()) ;
            out.print(aliasToken()) ;
            out.print(col.getColumnName()) ;

        }
        out.ensureStartOfLine() ;

        out.incIndent() ;       // INC
        out.println("FROM") ;
        join.visit(this) ;
        out.ensureStartOfLine() ;
        // Alias and annotations handled by outputNode
    }

    public void visit(SqlUnion sqlUnion)
    { throw new SDBNotImplemented("SQL generation of SqlUnion") ; }

    protected void visitJoin(SqlJoin join) { visitJoin(join, join.getJoinType().sqlOperator()) ; }
    protected void visitJoin(SqlJoin join, String joinOperatorName)
    {
        // TODO revisit this code.  Is it now needless complex?
        // Check brackets for more general SQL generation (safe mode - i.e. always bracketted?)
        SqlNode left = join.getLeft() ;
        SqlNode right = join.getRight() ;
        
        // Appearance: stop nesting too much.
        // Can we linearise the format? (drop indentation)
        if ( left.isJoin() && left.getAliasName() == null ) 
            outputNode(left, false) ;
        else
        {
            out.incIndent() ;
            outputNode(left, true) ;
            out.decIndent() ;
        }
        
        out.println() ;
        //out.print(" ") ;
        
        out.print(joinOperatorName) ;
        annotate(join) ;
        out.println() ;

        // Aliasing and scoping - may need sub-SELECT - or just don't generate
        // such SqlNode structures, leaving only COALESCE as the sub-SELECT case
        
        boolean bracketsRight = true ;
//        if ( right.isInnerJoin() && join.isInnerJoin() && no conditions )
//            bracketsRight = false ;
        
        if ( bracketsRight )
            // Why?
            out.incIndent() ;
        outputNode(right, bracketsRight) ;
        if ( bracketsRight )
            out.decIndent() ;
        out.println() ;
        out.print("ON ") ;
        if ( join.getConditions().size() > 0 )
            conditionList(join.getConditions()) ;
        else
        {
            out.print(" ( ") ;
            out.print(leftJoinNoConditionsString()) ;
            out.print(" )") ;
        }
    }

    // -------- Extension points for various SQL differences
    
    protected String aliasToken()
    {
        return " AS " ;
    }

    protected String leftJoinNoConditionsString() { return "1 = 1" ; }
    
    // --------
    
    // Interaction with annotations
    static boolean allOnOneLine = false ;
    public void conditionList(SqlExprList conditions)
    {
        if ( conditions.size() == 0 )
            return ;
        
        out.print("( ") ;
        
        String sep = " AND " ;
        
        boolean first = true ;
        boolean lastAnnotated = false ;
            
        for ( SqlExpr c : conditions )
        {
            if ( ! first )
            {
                if ( ! allOnOneLine )
                    out.println();
                out.print(sep) ;
            }
            boolean needsParens = ! ( c instanceof S_Equal ) ;
            
            // TODO Interact with SqlExpr precedence printing
            if ( needsParens ) 
                out.print("( ") ;
            out.print(c.asSQL()) ;
            if ( needsParens ) 
                out.print(" )") ;
            if ( ! allOnOneLine )
                lastAnnotated = annotate(c) ;
            first = false ;
        }
        if ( ! allOnOneLine && lastAnnotated )
            out.println("") ;
        out.print(" )") ;
        first = true ; 
        
        if ( allOnOneLine )
        {            
            for ( SqlExpr c : conditions )
            {
                if ( c.hasNotes() )
                {
                    if ( !first ) out.println() ;
                    annotate(c) ;
                    first = false ;
                }
            }
        }
    }
    
    private void outputNode(SqlNode sqlNode, boolean mayNeedBrackets)
    {
        if ( sqlNode.isTable() )
        {
            sqlNode.visit(this) ;
            return ;
        }
        //boolean brackets = ( mayNeedBrackets && ( sqlNode.isSelectBlock() || sqlNode.isCoalesce() ) ) ;
        
        boolean brackets = false ;
        brackets = brackets || (mayNeedBrackets && sqlNode.isCoalesce()) ;
        
        // Work harder? ready for a better test.
        brackets = brackets || ( mayNeedBrackets && sqlNode.isSelectBlock()) ;
        
        
        // Need brackets if the subpart is a SELECT
        
        if ( brackets )
        {
            out.print("( ") ;
            out.incIndent() ;
        }
        sqlNode.visit(this) ;
        if ( brackets )
        {
            out.decIndent() ;
            out.ensureStartOfLine() ;
            out.print(")") ;
        }
            // Every derived table (SELECT ...) must have an alias.
            // Is there a more principled way to do this? .isDerived?
//            if ( sqlNode.isRestrict() || sqlNode.isProject())
//                out.print(+sqlNode.getAliasName()) ;
        if ( sqlNode.getAliasName() != null )
        {
            out.print(aliasToken()) ;
            out.print(sqlNode.getAliasName()) ;
        }
        annotate(sqlNode) ;
    }

    private boolean annotate(Annotations sqlNode)
    { return annotate(sqlNode, annotationColumn) ; }

    // return true if annotation was output and it runs to end-of-line  
    private boolean annotate(Annotations sqlNode, int indentationColumn)
    {
        if ( ! outputAnnotations )
            return false ;
        
        boolean first = true ;
        for ( String s : sqlNode.getNotes() )
        {
            if ( !first ) out.println();
            first = false; 
            out.pad(indentationColumn, true) ;
            if ( commentSQLStyle )
            {
                out.print(" -- ") ; out.print(s) ;
            }else{
                out.print(" /* ") ; out.print(s) ; out.print(" */") ;
            }
        }
        return !commentSQLStyle || !first ;
    }
}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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