//
// IPlugin.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.ext.plugin;

import imagej.ext.module.process.ModulePostprocessor;
import imagej.ext.module.process.ModulePreprocessor;
import imagej.ext.module.process.ModuleProcessor;

/**
 * Top-level interface for plugins. Plugins discoverable at runtime must
 * implement this interface and be annotated with @{@link Plugin}.
 * <p>
 * There are several different kinds of plugins:
 * <ul>
 * <li>{@link RunnablePlugin} - plugins that are executable as modules.</li>
 * <li>{@link ImageJPlugin} - plugins defined for use within ImageJ. All
 * {@link ImageJPlugin}s are executable.</li>
 * <li><code>Display</code> - plugins that display data.</li>
 * <li>{@link ModuleProcessor} - plugins that perform pre- or post-processing on
 * other plugins ({@link ModulePreprocessor} and {@link ModulePostprocessor}
 * respectively).</li>
 * </ul>
 * What all plugins have in common is that they can be annotated using the
 * @{@link Plugin} annotation, and discovered if present on the classpath at
 * runtime.
 * </p>
 * 
 * @author Curtis Rueden
 * @see Plugin
 * @see PluginService
 */
public interface IPlugin {
	// top-level marker interface for discovery via SezPoz
}
