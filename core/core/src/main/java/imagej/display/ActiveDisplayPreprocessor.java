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

package imagej.display;

import imagej.module.Module;
import imagej.module.ModuleItem;
import imagej.module.ModuleService;
import imagej.module.process.AbstractPreprocessorPlugin;
import imagej.module.process.PreprocessorPlugin;

import org.scijava.Priority;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Assigns the active {@link Display} when there is one single unresolved
 * {@link Display} parameter. Hence, rather than a dialog prompting the user to
 * choose a {@link Display}, the active {@link Display} is used automatically.
 * <p>
 * In the case of more than one {@link Display} parameter, the active
 * {@link Display} is not used and instead the user must select. This behavior
 * is consistent with ImageJ v1.x.
 * </p>
 * 
 * @author Curtis Rueden
 */
@Plugin(type = PreprocessorPlugin.class,
	priority = Priority.VERY_HIGH_PRIORITY)
public class ActiveDisplayPreprocessor extends AbstractPreprocessorPlugin {

	@Parameter(required = false)
	private DisplayService displayService;

	@Parameter(required = false)
	private ModuleService moduleService;

	// -- ModuleProcessor methods --

	@Override
	public void process(final Module module) {
		if (displayService == null || moduleService == null) return;

		final ModuleItem<?> displayInput =
			moduleService.getSingleInput(module, Display.class);
		if (displayInput == null || !displayInput.isAutoFill()) return;

		@SuppressWarnings("unchecked")
		final Class<? extends Display<?>> displayType =
			(Class<? extends Display<?>>) displayInput.getType();

		final Display<?> activeDisplay =
			displayService.getActiveDisplay(displayType);
		if (activeDisplay == null) return;

		final String name = displayInput.getName();
		module.setInput(name, activeDisplay);
		module.setResolved(name, true);
	}

}
