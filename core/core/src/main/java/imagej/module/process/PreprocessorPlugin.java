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

import imagej.ImageJPlugin;

import org.scijava.Contextual;
import org.scijava.plugin.Plugin;

/**
 * A preprocessor plugin defines a step that occurs just prior to the actual
 * execution of a {@link imagej.module.Module}. Typically, a preprocessor
 * prepares the module for execution in some way, such as populating module
 * inputs or checking prerequisites.
 * <p>
 * Preprocessor plugins discoverable at runtime must implement this interface
 * and be annotated with @{@link Plugin} with attribute {@link Plugin#type()} =
 * {@link PreprocessorPlugin}.class. While it possible to create a preprocessor
 * plugin merely by implementing this interface, it is encouraged to instead
 * extend {@link AbstractPreprocessorPlugin}, for convenience.
 * </p>
 * 
 * @author Curtis Rueden
 * @see ModulePreprocessor
 */
public interface PreprocessorPlugin extends ImageJPlugin, Contextual,
	ModulePreprocessor
{
	// PreprocessorPlugin is a module preprocessor,
	// discoverable via the plugin discovery mechanism.
}
