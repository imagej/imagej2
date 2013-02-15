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

package imagej.core.commands.debug;

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Test plugin for verifying that invalid module parameters are dealt with using
 * proper error handling.
 * 
 * @author Curtis Rueden
 */
@Plugin(menuPath = "Plugins>Debug>Invalid Plugin", headless = true)
public class InvalidCommand extends ParameterTester {

	/**
	 * This parameter is invalid because it shadows a private parameter of a
	 * superclass. Such parameters violate the principle of parameter names as
	 * unique keys.
	 */
	@Parameter
	private int q;

	/**
	 * This parameter is invalid because it is declared {@code final} without
	 * being {@link imagej.module.ItemVisibility#MESSAGE} visibility. Java does
	 * not allow such parameter values to be set by the framework.
	 */
	@Parameter
	private final float x = 0;
	
	@Parameter(type = ItemIO.OUTPUT)
	protected String output;

	@Override
	public void run() {
		final StringBuilder sb = new StringBuilder();

		append(sb, "InvalidPlugin results:");

		append(sb, "");
		append(sb, "\tq = " + q);
		append(sb, "\tx = " + x);
		append(sb, "");
		append(sb, "The fact that you are reading this means the test failed!");

		output = sb.toString();
	}

	private void append(final StringBuilder sb, final String s) {
		sb.append(s + "\n");
	}

}
