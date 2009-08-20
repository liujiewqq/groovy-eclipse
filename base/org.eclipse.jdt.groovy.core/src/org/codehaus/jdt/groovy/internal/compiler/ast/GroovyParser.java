/*******************************************************************************
 * Copyright (c) 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement        - Initial API and implementation
 *     Andrew Eisenberg - Additional work
 *******************************************************************************/
package org.codehaus.jdt.groovy.internal.compiler.ast;

import java.io.File;

import groovy.lang.GroovyClassLoader;

import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

/**
 * The mapping layer between the groovy parser and the JDT. This class communicates with the groovy parser and translates results
 * back for JDT to consume.
 * 
 * @author Andy Clement
 */
@SuppressWarnings("restriction")
public class GroovyParser {

	// LookupEnvironment lookupEnvironment;
	public ProblemReporter problemReporter;
	CompilationUnit groovyCompilationUnit;
	private JDTResolver resolver;
	private String gclClasspath;
	public static IGroovyDebugRequestor debugRequestor;
	private CompilerOptions compilerOptions;

	// FIXASC (RC1) review callers who pass null for options
	public GroovyParser(CompilerOptions options, ProblemReporter problemReporter) {
		String path = (options == null ? null : options.groovyClassLoaderPath);
//		if (options == null) {
//			throw new RuntimeException("Dont do that");
//		}
		// FIXASC (M2) set parent of the loader to system or context class loader?
		GroovyClassLoader gcl = new GroovyClassLoader();
		this.gclClasspath = path;
		this.compilerOptions = options;
		configureClasspath(gcl, path);
		this.groovyCompilationUnit = new CompilationUnit(gcl);
		this.groovyCompilationUnit.removeOutputPhaseOperation();
		// this.lookupEnvironment = lookupEnvironment;
		this.problemReporter = problemReporter;
		this.resolver = new JDTResolver(groovyCompilationUnit);
		groovyCompilationUnit.setClassLoader(gcl);
		groovyCompilationUnit.setResolveVisitor(resolver);
	}

	// FIXASC (RC1) perf ok?
	private void configureClasspath(GroovyClassLoader gcl, String path) {
		if (path != null) {
			if (path.indexOf(File.pathSeparator) != -1) {
				int pos = 0;
				while (pos != -1) {
					int nextSep = path.indexOf(File.pathSeparator, pos);
					if (nextSep == -1) {
						// last piece
						String p = path.substring(pos);
						gcl.addClasspath(p);
						pos = -1;
					} else {
						String p = path.substring(pos, nextSep);
						gcl.addClasspath(p);
						pos = nextSep + 1;
					}
				}
			} else {
				gcl.addClasspath(path);
			}
		}
	}

	/**
	 * Call the groovy parser to drive the first few phases of
	 */
	public CompilationUnitDeclaration dietParse(ICompilationUnit sourceUnit, CompilationResult compilationResult) {
		char[] sourceCode = sourceUnit.getContents();
		// FIXASC (M3) need our own tweaked subclass of CompilerConfiguration?
		CompilerConfiguration groovyCompilerConfig = new CompilerConfiguration();
		// groovyCompilerConfig.setPluginFactory(new ErrorRecoveredCSTParserPluginFactory(null));
		ErrorCollector errorCollector = new GroovyErrorCollectorForJDT(groovyCompilerConfig);
		SourceUnit groovySourceUnit = new SourceUnit(new String(sourceUnit.getFileName()), new String(sourceCode),
				groovyCompilerConfig, null, errorCollector);
		GroovyCompilationUnitDeclaration gcuDeclaration = new GroovyCompilationUnitDeclaration(problemReporter, compilationResult,
				sourceCode.length, groovyCompilationUnit, groovySourceUnit);
		// FIXASC (M2) get this from the Antlr parser
		compilationResult.lineSeparatorPositions = GroovyUtils.getSourceLineSeparatorsIn(sourceCode);
		groovyCompilationUnit.addSource(groovySourceUnit);

		// boolean success =
		gcuDeclaration.processToPhase(Phases.CONVERSION);

		// Groovy moduleNode is null when there is a fatal error
		// Otherwise, recover what we can
		if (gcuDeclaration.getModuleNode() != null) {
			gcuDeclaration.populateCompilationUnitDeclaration();
			for (TypeDeclaration decl : gcuDeclaration.types) {
				GroovyTypeDeclaration gtDeclaration = (GroovyTypeDeclaration) decl;
				resolver.record(gtDeclaration);
			}
		}

		if (debugRequestor != null) {
			debugRequestor.acceptCompilationUnitDeclaration(gcuDeclaration);
		}
		return gcuDeclaration;
	}

	public void reset() {
		GroovyClassLoader gcl = new GroovyClassLoader();
		configureClasspath(gcl, gclClasspath);
		this.groovyCompilationUnit = new CompilationUnit(gcl);
		this.resolver = new JDTResolver(groovyCompilationUnit);
		this.groovyCompilationUnit.setResolveVisitor(resolver);
	}

	public CompilerOptions getCompilerOptions() {
		return compilerOptions;
	}
}
