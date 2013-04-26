/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.legacy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;

import java.awt.GraphicsEnvironment;

import ij.IJ;

import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;

/**
 * Unit tests for {@link LegacyService}.
 * 
 * @author Johannes Schindelin
 */
public class LegacyServiceTest {

	static {
		/*
		 * We absolutely require that the LegacyInjector did its job
		 * before we use the ImageJ 1.x classes here.
		 */
		DefaultLegacyService.getInstance();
	}

	@Before
	public void cannotRunHeadlesslyYet() {
		assumeTrue(!GraphicsEnvironment.isHeadless());
	}

	@Test
	public void testContext() {
		final Context context = new Context(LegacyService.class);
		final LegacyService legacyService =
			context.getService(LegacyService.class);
		assumeTrue(legacyService != null);

		Context context2 = (Context)IJ.runPlugIn(Context.class.getName(), null);
		assertNotNull(context2);
		assertEquals(context, context2);

		final LegacyService legacyService2 = (LegacyService)
				IJ.runPlugIn(LegacyService.class.getName(), null);
		assertNotNull(legacyService2);
		assertEquals(legacyService, legacyService2);
	}

}

