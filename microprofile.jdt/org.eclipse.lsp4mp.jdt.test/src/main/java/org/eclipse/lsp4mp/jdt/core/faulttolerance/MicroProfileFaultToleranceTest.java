/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.jdt.core.faulttolerance;

import static org.eclipse.lsp4mp.jdt.internal.core.MicroProfileAssert.assertHints;
import static org.eclipse.lsp4mp.jdt.internal.core.MicroProfileAssert.assertHintsDuplicate;
import static org.eclipse.lsp4mp.jdt.internal.core.MicroProfileAssert.assertProperties;
import static org.eclipse.lsp4mp.jdt.internal.core.MicroProfileAssert.assertPropertiesDuplicate;
import static org.eclipse.lsp4mp.jdt.internal.core.MicroProfileAssert.h;
import static org.eclipse.lsp4mp.jdt.internal.core.MicroProfileAssert.p;
import static org.eclipse.lsp4mp.jdt.internal.core.MicroProfileAssert.vh;
import static org.eclipse.lsp4mp.jdt.internal.core.java.MicroProfileForJavaAssert.assertJavaDiagnostics;
import static org.eclipse.lsp4mp.jdt.internal.core.java.MicroProfileForJavaAssert.d;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4mp.commons.DocumentFormat;
import org.eclipse.lsp4mp.commons.MicroProfileJavaDiagnosticsParams;
import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;
import org.eclipse.lsp4mp.commons.MicroProfilePropertiesScope;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4mp.jdt.internal.faulttolerance.MicroProfileFaultToleranceConstants;
import org.eclipse.lsp4mp.jdt.internal.faulttolerance.java.MicroProfileFaultToleranceErrorCode;
import org.junit.Test;

/**
 * Test collection of MicroProfile properties for MicroProfile Fault Tolerance
 * annotations
 *
 * @author Angelo ZERR
 *
 */
public class MicroProfileFaultToleranceTest extends BasePropertiesManagerTest {

	@Test
	public void microprofileFaultTolerancePropertiesTest() throws Exception {

		MicroProfileProjectInfo infoFromClasspath = getMicroProfileProjectInfoFromMavenProject(
				MavenProjectName.microprofile_fault_tolerance, MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES);

		assertProperties(infoFromClasspath,

				// <classname>/<annotation>/<parameter>
				p(null, "org.acme.MyClient/Retry/maxRetries", "int", " *  **Returns:**" + System.lineSeparator() + //
						"    " + System.lineSeparator() + //
						"     *  The max number of retries. -1 means retry forever. The value must be greater than or equal to -1.",
						false, "org.acme.MyClient", null, null, 0, "3"),

				// <classname>/<methodname>/<annotation>/<parameter>
				p(null, "org.acme.MyClient/serviceA/Retry/maxRetries", "int",
						" *  **Returns:**" + System.lineSeparator() + //
								"    " + System.lineSeparator() + //
								"     *  The max number of retries. -1 means retry forever. The value must be greater than or equal to -1.",
						false, "org.acme.MyClient", null, "serviceA()V", 0, "90"),

				p(null, "org.acme.MyClient/serviceA/Retry/delay", "long",
						"The delay between retries. Defaults to 0. The value must be greater than or equal to 0."
								+ System.lineSeparator() + //
								"" + System.lineSeparator() + //
								" *  **Returns:**" + System.lineSeparator() + //
								"    " + System.lineSeparator() + //
								"     *  the delay time",
						false, "org.acme.MyClient", null, "serviceA()V", 0, "0"),

				// <annotation>
				// -> <annotation>/enabled
				p(null, "Asynchronous/enabled", "boolean", "Enabling the policy", false,
						"org.eclipse.microprofile.faulttolerance.Asynchronous", null, null, 0, "true"),

				// <annotation>/<parameter>
				p(null, "Bulkhead/value", "int",
						"Specify the maximum number of concurrent calls to an instance. The value must be greater than 0. Otherwise, `org.eclipse.microprofile.faulttolerance.exceptions.FaultToleranceDefinitionException` occurs."
								+ System.lineSeparator() + //
								"" + System.lineSeparator() + //
								" *  **Returns:**" + System.lineSeparator() + //
								"    " + System.lineSeparator() + //
								"     *  the limit of the concurrent calls",
						false, "org.eclipse.microprofile.faulttolerance.Bulkhead", null, "value()I", 0, "10"),

				p(null, "MP_Fault_Tolerance_NonFallback_Enabled", "boolean",
						MicroProfileFaultToleranceConstants.MP_FAULT_TOLERANCE_NONFALLBACK_ENABLED_DESCRIPTION, false,
						null, null, null, 0, "false")

		);

		assertPropertiesDuplicate(infoFromClasspath);

		assertHints(infoFromClasspath, h("java.time.temporal.ChronoUnit", null, true, "java.time.temporal.ChronoUnit", //
				vh("NANOS", null, null), //
				vh("MICROS", null, null), //
				vh("MILLIS", null, null), //
				vh("SECONDS", null, null), //
				vh("MINUTES", null, null), //
				vh("HALF_DAYS", null, null), //
				vh("DAYS", null, null), //
				vh("WEEKS", null, null), //
				vh("MONTHS", null, null), //
				vh("YEARS", null, null), //
				vh("DECADES", null, null), //
				vh("CENTURIES", null, null), //
				vh("MILLENNIA", null, null), //
				vh("ERAS", null, null), //
				vh("FOREVER", null, null)) //
		);

		assertHintsDuplicate(infoFromClasspath);
	}

	@Test
	public void fallbackMethodsMissing() throws Exception {
		IJavaProject javaProject = loadMavenProject(MavenProjectName.microprofile_fault_tolerance);
		IJDTUtils utils = JDT_UTILS;

		MicroProfileJavaDiagnosticsParams diagnosticsParams = new MicroProfileJavaDiagnosticsParams();
		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/FaultTolerantResource.java"));
		diagnosticsParams.setUris(Arrays.asList(javaFile.getLocation().toFile().toURI().toString()));
		diagnosticsParams.setDocumentFormat(DocumentFormat.Markdown);

		Diagnostic d = d(14, 31, 36,
				"The referenced fallback method 'aaa' does not exist",
				DiagnosticSeverity.Error, MicroProfileFaultToleranceConstants.DIAGNOSTIC_SOURCE,
				MicroProfileFaultToleranceErrorCode.FALLBACK_METHOD_DOES_NOT_EXIST);
		assertJavaDiagnostics(diagnosticsParams, utils, //
				d);
	}
	
	@Test
	public void fallbackMethodValidationFaultTolerant() throws Exception {
		IJavaProject javaProject = loadMavenProject(MavenProjectName.microprofile_fault_tolerance);
		IJDTUtils utils = JDT_UTILS;

		MicroProfileJavaDiagnosticsParams diagnosticsParams = new MicroProfileJavaDiagnosticsParams();
		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/OtherFaultTolerantResource.java"));
		diagnosticsParams.setUris(Arrays.asList(javaFile.getLocation().toFile().toURI().toString()));
		diagnosticsParams.setDocumentFormat(DocumentFormat.Markdown);
		assertJavaDiagnostics(diagnosticsParams, utils);
	}

}
