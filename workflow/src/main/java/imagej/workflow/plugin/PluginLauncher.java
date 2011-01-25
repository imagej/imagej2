//
// PluginLauncher.java
//

/*
Multiple instance chainable plugin framework.

Copyright (c) 2010, UW-Madison LOCI
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
  * Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
  * Neither the name of the UW-Madison LOCI nor the
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The PluginLauncher talks to the PluginScheduler and launches new instances of
 * the plugin as needed.
 *
 * @author Aivar Grislis
 */
public class PluginLauncher implements IPluginLauncher {
    public static boolean s_singleInstance = false;
    private Class m_pluginClass;
    private String m_uniqueId;
    private PluginAnnotations m_annotations;
    private Thread m_thread;
    private volatile boolean m_quit = false;
    private Map<String, String> m_outputNames = new HashMap<String, String>();
    private Map<String, Object> m_inputs = new HashMap<String, Object>();

    /**
     * Creates a launcher for a given class, that has the given input and
     * output image name annotations.
     *
     * @param pluginClass
     * @param annotations
     */
    public PluginLauncher(Class pluginClass, String uniqueId, PluginAnnotations annotations) {
        m_pluginClass = pluginClass;
        m_uniqueId = uniqueId;
        //TODO
        if (null == m_uniqueId) {
            System.out.println("Creating PluginLauncher w/o uniqueId!!");
        }
        m_annotations = annotations;
        m_thread = new LauncherThread();
        m_thread.setDaemon(true);
        m_thread.start();
    }


    /**
     * Sets input settings for this instance.
     *
     * @param inputs
     */
    public void setInputs(Map<String, Object> inputs) {
        m_inputs = inputs;
    }

    /**
     * Chains this launcher to next one.
     *
     * @param outName
     * @param next
     * @param inName
     */
    public void chainNext(String outName, IPluginLauncher next, String inName) {
        PluginScheduler.getInstance().chain(this, outName, next, inName);
    }

    /**
     * Chains this launcher to previous one.
     *
     * @param inName
     * @param previous
     * @param outName
     */
    public void chainPrevious(String inName, IPluginLauncher previous, String outName) {
        PluginScheduler.getInstance().chain(previous, outName, this, inName);
    }

    /**
     * Initiates a plugin chain by feeding a named image to this launcher's plugin.
     *
     * @param name
     * @param image
     */
    public void externalPut(String name, ItemWrapper image) {
        String fullInName = uniqueName(name);
        PluginScheduler.getInstance().put(m_uniqueId, name, fullInName, image);
    }

    /**
     * Generates a unique input image name for this launcher.
     *
     * @param name
     * @return
     */
    public String uniqueName(String name) {
        return m_uniqueId + '.' + name;
    }

    /**
     * Associates a unique input image name for some other launcher to our
     * output image name.
     *
     * @param outName
     * @param fullInName
     */
    public void associate(String outName, String fullInName) {
        m_outputNames.put(outName, fullInName);
    }

    /**
     * Quits processing the chain.
     */
    public void quit() {
        m_quit = true;
    }

    /**
     * Processing thread for launcher.  Waits for a complete set of input
     * images, then spawns a thread with a new instance of the plugin to
     * process them.
     */
    private class LauncherThread extends Thread {

        @Override
        public void run() {
            Set<String> inputNames = m_annotations.getInputNames();

            //TODO
            System.out.println("INPUT NAMES");
            for (String name: inputNames) {
                System.out.println("INPUT NAME IS " + name);
            }
            while (!m_quit) {
                // assemble a set of input images
                Map<String, ItemWrapper> inputImages = new HashMap();
                for (String inputName : inputNames) {
                    ItemWrapper item = null;
                    if (m_inputs.containsKey(inputName)) {
                        // already specified for this instance
                        item = new ItemWrapper(m_inputs.get(inputName));
                    }
                    else {
                        // get from pipes
                        String fullInName = uniqueName(inputName);
                        item = PluginScheduler.getInstance().get(fullInName);
                    }
                    inputImages.put(inputName, item);
                }

                // if we didn't actually wait for any inputs, run once only.
                if (inputNames.size() == inputNames.size()) {
                    m_quit = true;
                }

                //TODO Good place to throttle thread creation here
                PluginScheduler.getInstance().reportNewPlugin(m_pluginClass.getSimpleName());

                // launch the plugin for this set of images
                Thread pluginThread = new PluginThread(inputImages);
                pluginThread.start();

                // Only run one plugin instance at a time?
                if (s_singleInstance) { //TODO implemented in a quick & dirty way
                    // wait for plugin to finish
                    // (Note: this is all a kludge for now:  you might as well just run the plugin
                    //  on this launcher thread.)
                    try {
                        pluginThread.join();
                    }
                    catch (InterruptedException e) {
                        System.out.println("LauncherThread.run() insterrupted on join");
                    }
                }
            }
        }
    }

    /**
     * Processing thread for a plugin instance.  Instantiates and runs the plugin.
     */
    private class PluginThread extends Thread {
        Map<String, ItemWrapper> m_inputImages;
        IPluginInternal m_pluginInstance;

        PluginThread(Map<String, ItemWrapper> inputImages) {
            m_inputImages = inputImages;
            m_pluginInstance = null;
            try {
                m_pluginInstance = (IPluginInternal) m_pluginClass.newInstance();
            }
            catch (InstantiationException e) {
                System.out.println("Problem instantiating plugin " + m_pluginClass.getSimpleName() + ' ' + e.getMessage());
            }
            catch (IllegalAccessException e) {
                System.out.println("Illegal access instantiating plugin " + m_pluginClass.getSimpleName() + ' ' + e.getMessage());
            }
        }

        @Override
        public void run() {
            if (null != m_pluginInstance) {
                m_pluginInstance.start(m_uniqueId, m_inputImages, m_outputNames);
            }
        }
    }
}
