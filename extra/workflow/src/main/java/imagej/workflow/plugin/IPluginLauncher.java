//
// IPluginLauncher.java
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

package imagej.workflow.plugin;

import java.util.Map;

/**
 * Interface to the plugin launcher.
 *
 * Each ILinkedPlugin is associated with a plugin class and has a
 * IPluginLauncher that is uses internally to chain its plugin.
 *
 * The plugin launcher communicates with the plugin scheduler.
 *
 * @author Aivar Grislis
 */
public interface IPluginLauncher {
    /**
     * Sets input settings for this instance.
     *
     * @param inputs
     */
    public void setInputs(Map<String, Object> inputs);

    /**
     * Chains this plugin to the next one.
     *
     * @param outName output name for this plugin
     * @param next next plugin
     * @param inName input name for next plugin
     */
    public void chainNext(String outName, IPluginLauncher next, String inName);

    /**
     * Chains this plugin to the previous one.
     *
     * @param inName input name for this plugin
     * @param previous previous plugin
     * @param outName output namem for previous plugin
     */
    public void chainPrevious(String inName, IPluginLauncher previous, String outName);

    /**
     * Used to initiate a plugin chain.  This named image becomes the input for
     * this plugin.
     *
     * @param name
     * @param image
     */
    public void externalPut(String name, ItemWrapper image);

    /**
     * Given a name, makes it unique for this plugin launcher instance.
     *
     * @param name
     * @return
     */
    public String uniqueName(String name);

    /**
     * Used for plugin chaining.  Called from scheduler.  Tells launcher to
     * associate this plugin's output name with an input name unique to a
     * plugin launcher instance.
     *
     * @param outName
     * @param fullInName
     */
    public void associate(String outName, String fullInName);

    /**
     * Used to shut down processing.
     */
    public void quit();
}
