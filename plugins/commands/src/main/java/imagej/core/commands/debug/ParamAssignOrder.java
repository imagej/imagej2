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

import imagej.Previewable;
import imagej.command.Command;
import imagej.command.ContextCommand;

import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * An illustration of the order in which command parameter assignment occurs.
 * <p>
 * This is useful since there are several different ways a command's inputs can
 * be assigned a value.
 * </p>
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Command.class, menuPath = "Plugins>Sandbox>Parameter Assignment Order",
	headless = true, initializer = "initParams")
public class ParamAssignOrder extends ContextCommand implements Previewable {

	@Parameter
	private LogService log;

	@Parameter(initializer = "initInput1")
	private String input1 = "input1: initial value";

	@Parameter(initializer = "initInput2")
	private String input2 = "input2: initial value";

	// -- Constructor --

	public ParamAssignOrder() {
		displayValues("constructor start");
		input1 = "input1: constructor";
		input2 = "input2: constructor";
		displayValues("constructor end");
	}

	// -- Runnable methods --

	@Override
	public void run() {
		input1 = "input1: run called";
		input2 = "input2: run called";
		displayValues("run");
	}

	// -- Previewable methods --

	@Override
	public void preview() {
		displayValues("preview");
	}

	@Override
	public void cancel() {
		input1 = "input1: cancel called";
		input2 = "input2: cancel called";
		displayValues("cancel");
	}

	// -- Initializers --

	/** Global initializer. */
	protected void initParams() {
		input1 = "input1: initParams called";
		input2 = "input2: initParams called";
		displayValues("initParams");
	}

	/** Initializer for input1 parameter. */
	protected void initInput1() {
		input1 = "input1: initInput1 called";
		displayValues("initInput1");
	}

	/** Initializer for input2 parameter. */
	protected void initInput2() {
		input2 = "input2: initInput2 called";
		displayValues("initInput2");
	}

	// -- Helper methods --

	private void displayValues(final String header) {
		if (log == null) {
			// NB: Log parameter not yet populated.
			System.err.println();
			System.err.println("-- " + header + " --");
			System.err.println("input1 = " + input1);
			System.err.println("input2 = " + input2);
		}
		else {
			log.info("");
			log.info("-- " + header + " --");
			log.info("input1 = " + input1);
			log.info("input2 = " + input2);
		}
	}

}
