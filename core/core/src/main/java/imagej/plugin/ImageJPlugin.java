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

package imagej.plugin;

import imagej.command.Command;
import imagej.display.Display;
import imagej.module.Module;
import imagej.module.ModulePostprocessor;
import imagej.module.ModulePreprocessor;
import imagej.platform.Platform;
import imagej.tool.Tool;
import imagej.widget.InputHarvester;
import imagej.widget.InputWidget;

import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.scijava.plugin.SciJavaPlugin;
import org.scijava.service.Service;

/**
 * Top-level interface for plugins. Plugins discoverable at runtime must
 * implement this interface and be annotated with @{@link Plugin}.
 * <p>
 * The core types of plugins are as follows:
 * </p>
 * <ul>
 * <li>{@link Command} - plugins that are executable as {@link Module}s. These
 * plugins typically perform a discrete operation, and are accessible via the
 * ImageJ menus.</li>
 * <li>{@link Service} - plugins that define new API in a particular area.</li>
 * <li>{@link Tool} - plugins that map user input (e.g., keyboard and mouse
 * actions) to behavior. They are usually rendered as icons in the ImageJ
 * toolbar.</li>
 * <li>{@link Display} - plugins that visualize objects, often used to display
 * module outputs.</li>
 * <li>{@link InputWidget} - plugins that render UI widgets for the
 * {@link InputHarvester} preprocessor.</li>
 * <li>{@link PreprocessorPlugin} - plugins that perform preprocessing on
 * modules. A {@link PreprocessorPlugin} is a discoverable
 * {@link ModulePreprocessor}.</li>
 * <li>{@link PostprocessorPlugin} - plugins that perform postprocessing on
 * modules. A {@link PostprocessorPlugin} is a discoverable
 * {@link ModulePostprocessor}.</li>
 * <li>{@link Platform} - plugins for defining platform-specific behavior.</li>
 * </ul>
 * <p>
 * What all plugins have in common is that they are declared using an annotation
 * (@{@link Plugin}), and discovered if present on the classpath at runtime.
 * </p>
 * 
 * @author Curtis Rueden
 * @see Plugin
 * @see PluginService
 */
public interface ImageJPlugin extends SciJavaPlugin {
	// top-level marker interface for discovery via SezPoz
}
