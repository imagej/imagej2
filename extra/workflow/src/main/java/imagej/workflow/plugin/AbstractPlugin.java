//
// AbstractPlugin.java
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

import imagej.workflow.plugin.annotations.Input;
import imagej.workflow.plugin.annotations.Output;
import imagej.workflow.plugin.ItemWrapper;

/**
 * Abstract base class for plugin.  Starts up plugin processing, gets and
 * puts images for the plugin.
 *
 * @author Aivar Grislis
 */
public abstract class AbstractPlugin implements IPluginInternal, IPlugin {
    String m_uniqueId;
    Map<String, ItemWrapper> m_inputImages;
    Map<String, String> m_outputNames;

    /**
     * Starts up processing.  Called from plugin launcher.
     *
     * @param inputImages maps each input name to an image
     * @param outputNames maps each output name to a unique input name for
     *   the next chained plugin.
     */
    public void start(
            String uniqueId,
            Map<String, ItemWrapper> inputImages,
            Map<String, String> outputNames) {
        m_uniqueId = uniqueId;
        m_inputImages = inputImages;
        m_outputNames = outputNames;

        try {
            // do the actual work of the plugin
            process();
        }
        catch (Exception e) {
            System.out.println("Plugin exception " + e.getMessage());
        }

        m_inputImages = null;
    }

    /**
     * Gets the default input object from previous in chain.  Called from subclass.
     *
     * @return object
     */
    public Object get() {
        return get(Input.DEFAULT);
    }

    /**
     * Gets a named input object from previous in chain.  Called from subclass.
     *
     * @param inName
     * @return object
     */
    public Object get(String inName) {
        ItemWrapper input = m_inputImages.get(inName);
        if (null == input) {
            // run-time request disagrees with annotation
            PluginAnnotations.nameNotAnnotated(PluginAnnotations.InputOutput.INPUT, inName);
        }
        return input.getItem();
    }

    /**
     * Puts the default output object to next in chain (if any).  Called from subclass.
     *
     * @param object
     */
    public void put(Object object) {
        put(Output.DEFAULT, object);
    }

    /**
     * Puts named output object to next in chain (if any).  Called from subclass.
     *
     * @param outName
     * @param object
     */
    public void put(String outName, Object object) {
        //TODO how to check annotation?  No longer visible from here.
        /*
        if (isAnnotatedName(InputOutput.OUTPUT, outName)) {
            System.out.println("was annotated");
            // anyone interested in this output data?
            String fullName = m_map.get(outName);
            System.out.println("full name is " + fullName);
            if (null != fullName) {
                // yes, pass it on
                NodeScheduler.getInstance().put(fullName, data);
            }
        }
        */
        ItemWrapper item = new ItemWrapper(object);
        String fullInName = m_outputNames.get(outName);
        PluginScheduler.getInstance().put(m_uniqueId, outName, fullInName, item);
    }
}
