/*******************************************************************************
 * Copyright (c) 2011 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Andrew Eisenberg - Initial implemenation
 *******************************************************************************/
package org.codehaus.groovy.eclipse.dsl.tests;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.codehaus.groovy.eclipse.core.util.ReflectionUtils;
import org.codehaus.groovy.eclipse.dsl.DSLDStore;
import org.codehaus.groovy.eclipse.dsl.DSLDStoreManager;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.groovy.eclipse.dsl.contributions.IContributionGroup;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.CurrentTypePointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.FindFieldPointcut;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.groovy.tests.search.AbstractInferencingTest;

/**
 * 
 * @author Andrew Eisenberg
 * @created Feb 18, 2011
 */
public class DSLStoreTests extends AbstractDSLInferencingTest {
    public static Test suite() {
        return new TestSuite(DSLStoreTests.class);
    }

    public DSLStoreTests(String name) {
        super(name);
    }
    
    public void testNothing() throws Exception {
        assertDSLStore(0, Collections.EMPTY_MAP, Collections.EMPTY_MAP);
    }
    public void testSingleSimple() throws Exception {
        createDsls("currentType().accept { }");
        assertDSLStore(1, 
                createExpectedPointcuts(
                        new String[] { createSemiUniqueName(CurrentTypePointcut.class, 0) }),
        
                createExpectedContributionCount(
                        new String[] {createSemiUniqueName(CurrentTypePointcut.class, 0) },
                        new Integer[] { 1 }
        ));
    }
    
    // the same pointcut is used twice
    public void testSingleTwice() throws Exception {
        createDsls("def g = currentType()\n" +
        		"g.accept { }\n" +
        		"g.accept { }");
        assertDSLStore(1, 
                createExpectedPointcuts(
                        new String[] { createSemiUniqueName(CurrentTypePointcut.class, 0) }),
                        
                        createExpectedContributionCount(
                                new String[] {createSemiUniqueName(CurrentTypePointcut.class, 0) },
                                new Integer[] { 2 }
                        ));
    }
    
    // two pointcuts in same file
    public void testTwoPointcuts() throws Exception {
        createDsls("currentType().accept { }\n" +
                   "findField().accept { }");
        assertDSLStore(1, 
                createExpectedPointcuts(
                        new String[] { createSemiUniqueName(CurrentTypePointcut.class, 0),
                                createSemiUniqueName(FindFieldPointcut.class, 0)}),
                        
                        createExpectedContributionCount(
                                new String[] {createSemiUniqueName(CurrentTypePointcut.class, 0),
                                        createSemiUniqueName(FindFieldPointcut.class, 0)},
                                new Integer[] { 1, 1 }
                        ));
    }
    
    // two pointcuts two files
    public void testTwoPointcutsTwoFiles() throws Exception {
        createDsls("currentType().accept { }",
                   "findField().accept { }");
        assertDSLStore(2, 
                createExpectedPointcuts(
                        new String[] { createSemiUniqueName(CurrentTypePointcut.class, 0) },
                        new String[] { createSemiUniqueName(FindFieldPointcut.class, 1)}),
                        
                        createExpectedContributionCount(
                                new String[] {createSemiUniqueName(CurrentTypePointcut.class, 0),
                                        createSemiUniqueName(FindFieldPointcut.class, 1)},
                                new Integer[] { 1, 1 }
                        ));
    }
    
    public void testTwoFilesEachWith2Pointcuts() throws Exception {
        createDsls("currentType().accept { }\nfindField().accept { }",
                "currentType().accept { }\nfindField().accept { }");
        assertDSLStore(
                2,
                createExpectedPointcuts(
                        new String[] {
                                createSemiUniqueName(CurrentTypePointcut.class, 0),
                                createSemiUniqueName(FindFieldPointcut.class, 0) },
                        new String[] {
                                createSemiUniqueName(CurrentTypePointcut.class, 1),
                                createSemiUniqueName(FindFieldPointcut.class, 1) }),

                createExpectedContributionCount(new String[] {
                        createSemiUniqueName(CurrentTypePointcut.class, 0),
                        createSemiUniqueName(FindFieldPointcut.class, 0),
                        createSemiUniqueName(CurrentTypePointcut.class, 1),
                        createSemiUniqueName(FindFieldPointcut.class, 1) },
                        new Integer[] { 1, 1, 1, 1 }));
    }

    public void testTwoFilesEachWith2PointcutsEachUsedTwice() throws Exception {
        createDsls("def a = currentType()\ndef b = findField()\na.accept { }\na.accept { }\nb.accept { }\nb.accept { }",
                   "def a = currentType()\ndef b = findField()\na.accept { }\na.accept { }\nb.accept { }\nb.accept { }");
        assertDSLStore(
                2,
                createExpectedPointcuts(
                        new String[] {
                                createSemiUniqueName(CurrentTypePointcut.class, 0),
                                createSemiUniqueName(FindFieldPointcut.class, 0) },
                                new String[] {
                                createSemiUniqueName(CurrentTypePointcut.class, 1),
                                createSemiUniqueName(FindFieldPointcut.class, 1) }),
                                
                                createExpectedContributionCount(new String[] {
                                        createSemiUniqueName(CurrentTypePointcut.class, 0),
                                        createSemiUniqueName(FindFieldPointcut.class, 0),
                                        createSemiUniqueName(CurrentTypePointcut.class, 1),
                                        createSemiUniqueName(FindFieldPointcut.class, 1) },
                                        new Integer[] { 2, 2, 2, 2 }));
    }
    
    public void testCraziness() throws Exception {
        createDsls("def a = currentType()\ndef b = findField()\na.accept { }\na.accept { }\nb.accept { }\nb.accept { }",
                   "def a = currentType()\ndef b = findField()\na.accept { }\na.accept { }\nb.accept { }\nb.accept { }",
                   "def a = currentType()\na.accept { }\na.accept { }\na.accept { }\na.accept { }\na.accept { }",
                   ""); // not in store
        
        assertDSLStore(
                3,
                createExpectedPointcuts(
                        new String[] {
                                createSemiUniqueName(CurrentTypePointcut.class, 0),
                                createSemiUniqueName(FindFieldPointcut.class, 0) },
                        new String[] {
                                createSemiUniqueName(CurrentTypePointcut.class, 1),
                                createSemiUniqueName(FindFieldPointcut.class, 1) },
                        new String[] { createSemiUniqueName(
                                CurrentTypePointcut.class, 2) },
                        new String[] { }
                ),

                createExpectedContributionCount(new String[] {
                        createSemiUniqueName(CurrentTypePointcut.class, 0),
                        createSemiUniqueName(FindFieldPointcut.class, 0),
                        createSemiUniqueName(CurrentTypePointcut.class, 1),
                        createSemiUniqueName(FindFieldPointcut.class, 1),
                        createSemiUniqueName(CurrentTypePointcut.class, 2) },
                        new Integer[] { 2, 2, 2, 2, 5 }));
    }
    
    
    public void testAddAndRemove() throws Exception {
        assertDSLStore(0, Collections.EMPTY_MAP, Collections.EMPTY_MAP);
        
        // add one
        createDsls("currentType().accept { }");
        assertDSLStore(1, 
                createExpectedPointcuts(
                        new String[] { createSemiUniqueName(CurrentTypePointcut.class, 0) }),
        
                createExpectedContributionCount(
                        new String[] {createSemiUniqueName(CurrentTypePointcut.class, 0) },
                        new Integer[] { 1 }
        ));
        
        // add another
        createDsls(1, "currentType().accept { }");
        assertDSLStore(2, 
                createExpectedPointcuts(
                        new String[] { createSemiUniqueName(CurrentTypePointcut.class, 0) },
                        new String[] { createSemiUniqueName(CurrentTypePointcut.class, 1) }),
        
                createExpectedContributionCount(
                        new String[] {createSemiUniqueName(CurrentTypePointcut.class, 0), 
                                      createSemiUniqueName(CurrentTypePointcut.class, 1)},
                        new Integer[] { 1, 1 }
        ));
        
        // remove second
        deleteDslFile(1);
        assertDSLStore(1, 
                createExpectedPointcuts(
                        new String[] { createSemiUniqueName(CurrentTypePointcut.class, 0) }),
        
                createExpectedContributionCount(
                        new String[] {createSemiUniqueName(CurrentTypePointcut.class, 0) },
                        new Integer[] { 1 }
        ));

        // remove first
        deleteDslFile(0);
        assertDSLStore(0, Collections.EMPTY_MAP, Collections.EMPTY_MAP);
    }

    
    public void testChange() throws Exception {
        createDsls("currentType().accept { }");
        assertDSLStore(1, 
                createExpectedPointcuts(
                        new String[] { createSemiUniqueName(CurrentTypePointcut.class, 0) }),
        
                createExpectedContributionCount(
                        new String[] {createSemiUniqueName(CurrentTypePointcut.class, 0) },
                        new Integer[] { 1 }
        ));
        
        // overwrite the original
        createDsls("currentType().accept { }\n" + "findField().accept { }");
        assertDSLStore(
                1,
                createExpectedPointcuts(new String[] {
                        createSemiUniqueName(CurrentTypePointcut.class, 0),
                        createSemiUniqueName(FindFieldPointcut.class, 0) }),

                createExpectedContributionCount(new String[] {
                        createSemiUniqueName(CurrentTypePointcut.class, 0),
                        createSemiUniqueName(FindFieldPointcut.class, 0) },
                        new Integer[] { 1, 1 }));

    }
}
