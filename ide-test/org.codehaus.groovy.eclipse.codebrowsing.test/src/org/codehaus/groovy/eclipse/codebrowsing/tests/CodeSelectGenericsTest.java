/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.codehaus.groovy.eclipse.codebrowsing.tests;

import org.eclipse.jdt.core.SourceRange;

/**
 * @author Andrew Eisenberg
 * @created Jun 3, 2009
 *
 */
public class CodeSelectGenericsTest extends BrowsingTestCase {

    public CodeSelectGenericsTest() {
        super(CodeSelectGenericsTest.class.getName());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    // test an array of generic types
    public void testGRECLIPSE1050a() throws Exception {
        String groovyContents = "org.codehaus.groovy.ast.ClassHelper.make(List.class)";
        String toFind = "make";
        String elementName = "make";
        assertCodeSelect(XX, null, groovyContents, toFind, elementName);
    }

    // test an array of generic types
    public void testGRECLIPSE1050b() throws Exception {
        String groovyContents = "org.codehaus.groovy.ast.ClassHelper.make(new Class[0])[0].nameWithoutPackage";
        String toFind = "nameWithoutPackage";
        String elementName = "getNameWithoutPackage";
        assertCodeSelect(XX, null, groovyContents, toFind, elementName);
    }

    public void testCodeSelectGenericField1() throws Exception {
        String structureContents = "class Structure { java.util.List<String> field; }";
        String javaContents = "class Java { { new Structure().field = null;} }";
        String groovyContents = "new Structure().field";
        String toFind = "field";
        assertCodeSelect(structureContents, javaContents, groovyContents,
                toFind);
    }

    public void testCodeSelectGenericField2() throws Exception {
        String structureContents = "class Structure { java.util.Map<String, Integer> field; }";
        String javaContents = "class Java { { new Structure().field = null;} }";
        String groovyContents = "new Structure().field";
        String toFind = "field";
        assertCodeSelect(structureContents, javaContents, groovyContents,
                toFind);
    }

    public void testCodeSelectGenericField3() throws Exception {
        String structureContents = "class Structure { java.util.Map<String[], java.util.List<Integer>> field; }";
        String javaContents = "class Java { { new Structure().field = null;} }";
        String groovyContents = "new Structure().field";
        String toFind = "field";
        assertCodeSelect(structureContents, javaContents, groovyContents,
                toFind);
    }

    public void testCodeSelectGenericMethod1() throws Exception {
        String structureContents = "class Structure { java.util.Map<String[], java.util.List<Integer>> field; }";
        String javaContents = "class Java { { new Structure().field.entrySet();} }";
        String groovyContents = "new Structure().field.entrySet()";
        String toFind = "entrySet";
        assertCodeSelect(structureContents, javaContents, groovyContents,
                toFind);
    }

    public void testCodeSelectGenericMethod2() throws Exception {
        String structureContents = "class Structure { java.util.List<Integer> method() { return null; } }";
        String javaContents = "class Java { { new Structure().method();} }";
        String groovyContents = "new Structure().method()";
        String toFind = "method";
        assertCodeSelect(structureContents, javaContents, groovyContents,
                toFind);
    }

    public void testCodeSelectGenericMethod3() throws Exception {
        String structureContents = "class Structure { java.util.List<Integer> method(java.util.List<Integer> a) { return null; } }";
        String javaContents = "class Java { { new Structure().method(null);} }";
        String groovyContents = "new Structure().method(null)";
        String toFind = "method";
        assertCodeSelect(structureContents, javaContents, groovyContents,
                toFind);
    }

    public void testCodeSelectGenericMethod4() throws Exception {
        String structureContents = "class Structure { java.util.List<Integer> method(java.util.List<Integer> a, java.util.List<String> b) { return null; } }";
        String javaContents = "class Java { { new Structure().method(null, null);} }";
        String groovyContents = "new Structure().method(null, null)";
        String toFind = "method";
        assertCodeSelect(structureContents, javaContents, groovyContents,
                toFind);
    }

    public void testCodeSelectGenericMethod5() throws Exception {
        String structureContents = "class Structure { java.util.List<Integer> method(int a, int b, char x) { return null; } }";
        String javaContents = "class Java { { new Structure().method(1, 2, 'c');} }";
        String groovyContents = "new Structure().method(1, 2, 'c')";
        String toFind = "method";
        assertCodeSelect(structureContents, javaContents, groovyContents,
                toFind);
    }

    public void testCodeSelectGenericType1() throws Exception {
        String structureContents = "class Structure<T> { }";
        String javaContents = "class Java { { Structure<Integer> x = null;  if (x instanceof Object) { x.toString(); } } }";
        String groovyContents = "Structure<Integer> obj = new Structure<Integer>()\nobj";
        String toFind = "Structure";
        assertCodeSelect(structureContents, javaContents, groovyContents,
                toFind);
    }

    public void testCodeSelectGenericTypeAndField1() throws Exception {
        String structureContents = "class Structure<T> { java.util.List<T> field; }";
        String javaContents = "class Java { { new Structure<Integer>().field = null;} }";
        String groovyContents = "new Structure<Integer>().field";
        String toFind = "field";
        assertCodeSelect(structureContents, javaContents, groovyContents,
                toFind);
    }

    public void testCodeSelectGenericTypeAndField2() throws Exception {
        String structureContents = "class Structure<T,U> { java.util.Map<T,U> field; }";
        String javaContents = "class Java { { new Structure<String, Integer>().field = null;} }";
        String groovyContents = "new Structure<String, Integer>().field";
        String toFind = "field";
        assertCodeSelect(structureContents, javaContents, groovyContents,
                toFind);
    }

    public void testCodeSelectGenericTypeAndField3() throws Exception {
        String structureContents = "class Structure<T,U> { java.util.Map<T,U> field; }";
        String javaContents = "import java.util.List;\nclass Java { { new Structure<String[], List<Integer>> ().field = null;} }";
        String groovyContents = "new Structure<String[], List<Integer>>().field";
        String toFind = "field";
        assertCodeSelect(structureContents, javaContents, groovyContents,
                toFind);
    }

    public void testCodeSelectGenericTypeAndMethod1() throws Exception {
        String structureContents = "class Structure<T,U> { java.util.Map<T,U> field; }";
        String javaContents = "import java.util.List;\nclass Java { { new Structure<String[], List<Integer>> ().field.entrySet();} }";
        String groovyContents = "new Structure<String[], List<Integer>>().field.entrySet()";
        String toFind = "entrySet";
        assertCodeSelect(structureContents, javaContents, groovyContents,
                toFind);
    }

    public void testCodeSelectGenericTypeAndMethod2() throws Exception {
        String structureContents = "class Structure<T> { java.util.List<T> method() { return null; } }";
        String javaContents = "class Java { { new Structure<Integer>().method();} }";
        String groovyContents = "new Structure<Integer>().method()";
        String toFind = "method";
        assertCodeSelect(structureContents, javaContents, groovyContents,
                toFind);
    }

    public void testCodeSelectGenericTypeAndMethod3() throws Exception {
        String structureContents = "class Structure<T> { java.util.List<T> method(java.util.List<T> a) { return null; } }";
        String javaContents = "class Java { { new Structure<Integer>().method(null);} }";
        String groovyContents = "new Structure<Integer>().method(null)";
        String toFind = "method";
        assertCodeSelect(structureContents, javaContents, groovyContents,
                toFind);
    }

    public void testCodeSelectGenericTypeAndMethod4() throws Exception {
        String structureContents = "class Structure<T> { java.util.List<T> method(T a) { return null; } }";
        String javaContents = "import java.util.List;\nclass Java { { new Structure<List<Integer>>().method(null);} }";
        String groovyContents = "new Structure<List<Integer>>().method(null)";
        String toFind = "method";
        assertCodeSelect(structureContents, javaContents, groovyContents,
                toFind);
    }

    public void testCodeSelectGenericTypeAndMethod5() throws Exception {
        String structureContents = "import java.util.List;\nclass Structure<T,U> { T method(T a, List<U> b) { return null; } }";
        String javaContents = "import java.util.List;\nclass Java { { new Structure<List<String>, Integer>().method(null, null);} }";
        String groovyContents = "new Structure<List<String>, Integer>().method(null, null)";
        String toFind = "method";
        assertCodeSelect(structureContents, javaContents, groovyContents,
                toFind);
    }

    private static final String XX = "class XX { public XX[] getXx() { return null; }\npublic XX getYy() { return null; }\n }";

    public void testCodeSelectArray1() throws Exception {
        String groovyContents = "new XX().xx[0].xx";
        String toFind = "xx";
        String elementName = "getXx";
        assertCodeSelect(XX, null, groovyContents, toFind, elementName);
    }

    public void testCodeSelectArray2() throws Exception {
        String groovyContents = "new XX().xx[0].yy";
        String toFind = "yy";
        String elementName = "getYy";
        assertCodeSelect(XX, null, groovyContents, toFind, elementName);
    }

    public void testCodeSelectArray3() throws Exception {
        String groovyContents = "new XX().xx[0].getXx()";
        String toFind = "getXx";
        assertCodeSelect(XX, groovyContents, toFind);
    }

    public void testCodeSelectArray4() throws Exception {
        String groovyContents = "new XX().xx[0].getYy()";
        String toFind = "getYy";
        assertCodeSelect(XX, groovyContents, toFind);
    }

    public void testCodeSelectArray5() throws Exception {
        String groovyContents = "class YY { YY[] xx \nYY yy }\n"
                + "new YY().xx[0].setXx()";
        String toFind = "setXx";
        String elementName = "xx";
        assertCodeSelect(XX, null, groovyContents, toFind, elementName);
    }

    public void testCodeSelectArray6() throws Exception {
        String groovyContents = "class YY { YY[] xx \nYY yy }\n"
                + "new YY().xx[0].setYy()";
        String toFind = "setYy";
        String elementName = "yy";
        assertCodeSelect(XX, null, groovyContents, toFind, elementName);
    }

    public void testCodeSelectArray7() throws Exception {
        String groovyContents = "class YY { YY[] xx \nYY yy }\n"
                + "new YY().xx[0].getXx()";
        String toFind = "getXx";
        String elementName = "xx";
        assertCodeSelect(XX, null, groovyContents, toFind, elementName);
    }

    public void testCodeSelectArray8() throws Exception {
        String groovyContents = "class YY { YY[] xx \nYY yy }\n"
                + "new YY().xx[0].getYy()";
        String toFind = "getYy";
        String elementName = "yy";
        assertCodeSelect(XX, null, groovyContents, toFind, elementName);
    }

    private static final String GENERICS_CLASS = "interface SomeInterface<T> { }\n" + "interface SomeInterface2<T,U> { }\n"
            + "class SomeClass { }\n"
            + "class Bart implements /*0*/SomeInterface</*1*/SomeInterface2</*2*/SomeClass,/*3*/SomeInterface<? extends /*4*/SomeClass>>> {\n"
            + "    /*12*/SomeInterface</*5*/SomeClass> ff\n"
            + "    /*6*/SomeInterface</*7*/SomeClass> yy(/*8*/SomeInterface</*9*/SomeClass> yyy) { }\n"
            + "  def c = { /*10*/SomeInterface2</*11*/SomeClass> a -> }\n" + "}";

    private int find(int toFind) {
        String lookFor = "/*" + toFind + "*/";
        int index = GENERICS_CLASS.indexOf(lookFor);
        return index < 0 ? index : index + lookFor.length();
    }

    // See GRECLIPSE-1238
    public void testCodeSelectTypeParam0() throws Exception {
        String name = "SomeInterface";
        int len = name.length();
        assertCodeSelect(GENERICS_CLASS, new SourceRange(find(0), len), name);
    }

    // See GRECLIPSE-1238
    public void testCodeSelectTypeParam1() throws Exception {
        String name = "SomeInterface2";
        int len = name.length();
        assertCodeSelect(GENERICS_CLASS, new SourceRange(find(1), len), name);
    }

    // See GRECLIPSE-1238
    public void testCodeSelectTypeParam2() throws Exception {
        String name = "SomeClass";
        int len = name.length();
        assertCodeSelect(GENERICS_CLASS, new SourceRange(find(2), len), name);
    }

    // See GRECLIPSE-1238
    public void testCodeSelectTypeParam3() throws Exception {
        String name = "SomeInterface";
        int len = name.length();
        assertCodeSelect(GENERICS_CLASS, new SourceRange(find(3), len), name);
    }

    // See GRECLIPSE-1238
    public void testCodeSelectTypeParam4() throws Exception {
        String name = "SomeClass";
        int len = name.length();
        assertCodeSelect(GENERICS_CLASS, new SourceRange(find(4), len), name);
    }

    // See GRECLIPSE-1238
    public void testCodeSelectTypeParam5() throws Exception {
        String name = "SomeClass";
        int len = name.length();
        assertCodeSelect(GENERICS_CLASS, new SourceRange(find(5), len), name);
    }

    // See GRECLIPSE-1238
    public void testCodeSelectTypeParam6() throws Exception {
        String name = "SomeInterface";
        int len = name.length();
        assertCodeSelect(GENERICS_CLASS, new SourceRange(find(6), len), name);
    }

    // See GRECLIPSE-1238
    public void testCodeSelectTypeParam7() throws Exception {
        String name = "SomeClass";
        int len = name.length();
        assertCodeSelect(GENERICS_CLASS, new SourceRange(find(7), len), name);
    }

    // See GRECLIPSE-1238
    public void testCodeSelectTypeParam8() throws Exception {
        String name = "SomeInterface";
        int len = name.length();
        assertCodeSelect(GENERICS_CLASS, new SourceRange(find(8), len), name);
    }

    // See GRECLIPSE-1238
    public void testCodeSelectTypeParam9() throws Exception {
        String name = "SomeClass";
        int len = name.length();
        assertCodeSelect(GENERICS_CLASS, new SourceRange(find(9), len), name);
    }

    // See GRECLIPSE-1238
    public void testCodeSelectTypeParam10() throws Exception {
        String name = "SomeInterface2";
        int len = name.length();
        assertCodeSelect(GENERICS_CLASS, new SourceRange(find(10), len), name);
    }

    // See GRECLIPSE-1238
    public void testCodeSelectTypeParam11() throws Exception {
        String name = "SomeClass";
        int len = name.length();
        assertCodeSelect(GENERICS_CLASS, new SourceRange(find(11), len), name);
    }

    // See GRECLIPSE-1238
    public void testCodeSelectTypeParam12() throws Exception {
        String name = "SomeInterface";
        int len = name.length();
        assertCodeSelect(GENERICS_CLASS, new SourceRange(find(12), len), name);
    }

}