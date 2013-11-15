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

package imagej;

import org.scijava.plugin.Plugin;
import org.scijava.plugin.SciJavaPlugin;

/**
 * Top-level interface for ImageJ plugins.
 * <p>
 * Plugins discoverable at runtime must implement this interface (usually a
 * subinterface) and be annotated with @{@link Plugin}.
 * </p>
 * <p>
 * The core types of ImageJ plugins are as follows:
 * </p>
 * <ul>
 * <li>{@link imagej.command.Command} - plugins that are executable. These
 * plugins typically perform a discrete operation, and are accessible via the
 * ImageJ menus.</li>
 * <li>{@link imagej.tool.Tool} - plugins that map user input (e.g., keyboard
 * and mouse actions) to behavior. They are usually rendered as icons in the
 * ImageJ toolbar.</li>
 * <li>{@link imagej.display.Display} - plugins that visualize objects, often
 * used to display module outputs.</li>
 * <li>{@link imagej.widget.InputWidget} - plugins that render UI widgets for
 * the {@link imagej.widget.InputHarvester} preprocessor.</li>
 * <li>{@link imagej.plugin.PreprocessorPlugin} - plugins that perform
 * preprocessing on modules. A preprocessor plugin is a discoverable
 * {@link imagej.module.ModulePreprocessor}.</li>
 * <li>{@link imagej.plugin.PostprocessorPlugin} - plugins that perform
 * postprocessing on modules. A {@link imagej.plugin.PostprocessorPlugin} is a
 * discoverable {@link imagej.module.ModulePostprocessor}.</li>
 * <li>{@link imagej.platform.Platform} - plugins for defining platform-specific
 * behavior.</li>
 * </ul>
 * <p>
 * There is also one very important non-ImageJ-specific plugin type:
 * </p>
 * <ul>
 * <li>{@link org.scijava.service.Service} - plugins that define new API in a
 * particular area.</li>
 * </ul>
 * <p>
 * What all plugins have in common is that they are declared using an annotation
 * (@{@link Plugin}), and discovered if present on the classpath at runtime.
 * </p>
 * 
 * @author Curtis Rueden
 * @see Plugin
 * @see org.scijava.plugin.PluginService
 */
public interface ImageJPlugin extends SciJavaPlugin {
	// NB: Marker interface.
}
