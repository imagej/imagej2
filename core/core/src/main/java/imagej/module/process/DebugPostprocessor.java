/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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

package imagej.module.process;

import imagej.module.Module;

import java.util.Map;

import org.scijava.Priority;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * A postprocessor plugin that dumps parameter values to the log.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = PostprocessorPlugin.class, priority = Priority.FIRST_PRIORITY)
public class DebugPostprocessor extends AbstractPostprocessorPlugin {

	@Parameter(required = false)
	private LogService log;

	// -- ModuleProcessor methods --

	@Override
	public void process(final Module module) {
		if (log == null || !log.isDebug()) return;

		// dump input values to log
		log.debug("INPUTS:");
		final Map<String, Object> inputs = module.getInputs();
		for (final String key : inputs.keySet()) {
			log.debug("\t" + key + " = " + inputs.get(key));
		}

		// dump output values to log
		log.debug("OUTPUTS:");
		final Map<String, Object> outputs = module.getOutputs();
		for (final String key : outputs.keySet()) {
			log.debug("\t" + key + " = " + outputs.get(key));
		}
	}

}
