/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2015 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package net.imagej.app;

import static org.junit.Assert.assertEquals;

import net.imagej.ImageJService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.ValidityProblem;
import org.scijava.log.LogService;
import org.scijava.module.ModuleInfo;
import org.scijava.module.ModuleService;

/**
 * Tests that all SciJava modules are valid.
 *
 * @author Curtis Rueden
 */
public class ModuleValidityTest {

	private Context ctx;
	private LogService log;

	@Before
	public void setUp() {
		ctx = new Context(ImageJService.class);
		log = ctx.getService(LogService.class);
	}

	@After
	public void tearDown() {
		ctx.dispose();
	}

	@Test
	public void testModules() {
		final ModuleService moduleService = ctx.service(ModuleService.class);
		int fail = 0;
		for (final ModuleInfo info : moduleService.getModules()) {
			if (!info.isValid()) {
				fail++;
				if (log != null) {
					log.error("Invalid module: " + info.getIdentifier());
					for (final ValidityProblem problem : info.getProblems()) {
						log.error("- " + problem.getMessage());
						final Throwable cause = problem.getCause();
						if (cause != null) log.debug(cause);
					}
				}
			}
		}
		assertEquals(fail + " modules are invalid", 0, fail);
	}
}
