/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.function;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.function.library.* ;

/** Standard function library. */

public class StandardFunctions
{
    public static void loadStdDefs(FunctionRegistry registry)
    {
        String xfn = ARQConstants.fnPrefix ;
        
        String sparqlfn = "http://www.w3.org/sparqfn/" ;    // Not fixed yet.
        
        // See http://www.w3.org/TR/xpath-datamodel/#types-hierarchy
        // No durations here
        
        addCast(registry, XSDDatatype.XSDdecimal) ;
        addCast(registry, XSDDatatype.XSDinteger) ;

        addCast(registry, XSDDatatype.XSDlong) ;
        addCast(registry, XSDDatatype.XSDint) ;
        addCast(registry, XSDDatatype.XSDshort) ;
        addCast(registry, XSDDatatype.XSDbyte) ;
        
        addCast(registry, XSDDatatype.XSDnonPositiveInteger) ;
        addCast(registry, XSDDatatype.XSDnegativeInteger) ;

        addCast(registry, XSDDatatype.XSDnonNegativeInteger) ;
        addCast(registry, XSDDatatype.XSDpositiveInteger) ;
        addCast(registry, XSDDatatype.XSDunsignedLong) ;
        addCast(registry, XSDDatatype.XSDunsignedInt) ;
        addCast(registry, XSDDatatype.XSDunsignedShort) ;

        addCast(registry, XSDDatatype.XSDdouble) ;
        addCast(registry, XSDDatatype.XSDfloat) ;
        
        addCast(registry, XSDDatatype.XSDduration) ;
        
        addCast(registry, XSDDatatype.XSDboolean) ;
        addCast(registry, XSDDatatype.XSDstring) ;

        addCast(registry, XSDDatatype.XSDanyURI) ;
        
        // Specialized casting rules
        addCastDT(registry, XSDDatatype.XSDdateTime) ;
        addCastDT(registry, XSDDatatype.XSDdate) ;
        addCastDT(registry, XSDDatatype.XSDtime) ;
        addCastDT(registry, XSDDatatype.XSDgYear) ;
        addCastDT(registry, XSDDatatype.XSDgYearMonth) ;
        addCastDT(registry, XSDDatatype.XSDgMonth) ;
        addCastDT(registry, XSDDatatype.XSDgMonthDay) ;
        addCastDT(registry, XSDDatatype.XSDgDay) ;

        //TODO op:numeric-greater-than etc.
        
        add(registry, xfn+"boolean",        FN_BEV.class) ;
        add(registry, xfn+"not",            FN_Not.class) ;

        add(registry, xfn+"matches",        FN_Matches.class) ;
        add(registry, xfn+"string-length",  FN_StrLength.class) ;
        //add(registry, xfn+"string-join",   FN_StrJoin.class) ;    // Works fn:string-join works on a sequence.
        add(registry, xfn+"concat",         FN_StrConcat.class) ;
        add(registry, xfn+"substring",      FN_StrSubstring.class) ;
        add(registry, xfn+"starts-with",    FN_StrStartsWith.class) ;
        
        add(registry, xfn+"lower-case",     FN_StrLowerCase.class) ;
        add(registry, xfn+"upper-case",     FN_StrUpperCase.class) ;
        
        add(registry, xfn+"contains",       FN_StrContains.class) ;
        add(registry, xfn+"ends-with",      FN_StrEndsWith.class) ;
        
        add(registry, xfn+"abs",            FN_Abs.class) ;
        add(registry, xfn+"ceiling",        FN_Ceiling.class) ;
        add(registry, xfn+"floor",          FN_floor.class) ;
        add(registry, xfn+"round",          FN_Round.class) ;
        
        // SPARQL functions.
        // Check the exact URI.

//        add(registry, sparqlfn+"boolean",        FN_BEV.class) ;
//        add(registry, sparqlfn+"not",            FN_Not.class) ;
//
//        add(registry, sparqlfn+"matches",        FN_Matches.class) ;
//        add(registry, sparqlfn+"string-length",  FN_StrLength.class) ;
//        add(registry, sparqlfn+"concat",         FN_StrConcat.class) ;
//        add(registry, sparqlfn+"substring",      FN_StrSubstring.class) ;
//        add(registry, sparqlfn+"starts-with",    FN_StrStartsWith.class) ;
//        
//        add(registry, sparqlfn+"lower-case",     FN_StrLowerCase.class) ;
//        add(registry, sparqlfn+"upper-case",     FN_StrUpperCase.class) ;
//        
//        add(registry, sparqlfn+"contains",       FN_StrContains.class) ;
//        add(registry, sparqlfn+"ends-with",      FN_StrEndsWith.class) ;
//        
//        add(registry, sparqlfn+"abs",            FN_Abs.class) ;
//        add(registry, sparqlfn+"ceiling",        FN_Ceiling.class) ;
//        add(registry, sparqlfn+"floor",          FN_floor.class) ;
//        add(registry, sparqlfn+"round",          FN_Round.class) ;

//        add(registry, sparqlfn+"concat",         FN_Concat.class) ;
//        add(registry, sparqlfn+"upper-case",     FN_UpperCase.class) ;
//        add(registry, sparqlfn+"lower-case",     FN_LowerCase.class) ;
//        add(registry, sparqlfn+"encode-for-uri", FN_EncodeForURI.class) ;
//        add(registry, sparqlfn+"contains",       FN_Contains.class) ;
//
//        add(registry, sparqlfn+"starts-with",    FN_StartsWith.class) ;
//        add(registry, sparqlfn+"ends-with",      FN_EndsWith.class) ;

//        add(registry, xfn+"year-from-dateTime",     FN_YearFromDateTime.class) ;
//        add(registry, xfn+"month-from-dateTime",    FN_MonthFromDateTime.class) ;
//        add(registry, xfn+"day-from-dateTime",      FN_DayFromDateTime.class) ;
//
//        
//        add(registry, xfn+"hours-from-dateTime",    FN_HoursFromDateTime.class) ;
//        add(registry, xfn+"minutes-from-dateTime",  FN_MinutesFromDateTime.class) ;
//        add(registry, xfn+"seconds-from-dateTime",  FN_SecondsFromDateTime.class) ;
        
//        add(registry, xfn+"timezone-from-dateTime",  FN_TimezoneFromDateTime.class) ;
        
        
        // fn:compare/2 and /3 and provide collation argument
        //    Locale locale = new Locale(String language, String country)
        //      language is two letter lower case, county is uppercase.
        //        http://www.loc.gov/standards/iso639-2/englangn.html
        //      Check in Locale.getISOCountries()
        //        http://www.iso.ch/iso/en/prods-services/iso3166ma/02iso-3166-code-lists/list-en1.html
        //      Check in Locale.getISOLanguages
        //    Collator.getInstance(Locale)
        // fn:current-date() as xs:date = xs:date(fn:current-dateTime()).
        // fn:current-dateTime as xs:dateTime
        // fn:current-time() as xs:time
        
        // WRONG: fn:max/fn:min are aggregate functions that take a sequence
        //add(registry, xfn+"max", max.class) ;
        //add(registry, xfn+"min", min.class) ;
    }
    
    private static void addCast(FunctionRegistry registry, XSDDatatype dt)
    {
        registry.put(dt.getURI(), new CastXSD(dt) ) ;
    }

    private static void addCastDT(FunctionRegistry registry, XSDDatatype dt)
    {
        registry.put(dt.getURI(), new CastXSD_DT(dt) ) ;
    }

    private static void add(FunctionRegistry registry, String uri, Class<?> funcClass)
    {
        registry.put(uri, funcClass) ;
    }

}


/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
