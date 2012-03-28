/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
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

package imagej.workflow;

import java.util.Map;

import imagej.workflow.plugin.ItemWrapper;
import imagej.workflow.plugin.IPluginLauncher;

/**
 *
 * @author Aivar Grislis
 */
public interface IModule {

    /**
     * Gets name of module.
     *
     * @return
     */
    public String getName();

    /**
     * Sets name of module.
     *
     * @param name
     */
    public void setName(String name);

    /**
     * Gets associated launcher
     *
     * @return launcher
     */
    public IPluginLauncher getLauncher();

    /**
     * Saves module as XML string representation.
     *
     * @return
     */
    String toXML();

    /**
     * Restores module from XML string representation.
     *
     * @param xml
     * @return whether successfully parsed
     */
    boolean fromXML(String xml);

    /**
     * Gets input image names.
     *
     * @return
     */
    public String[] getInputNames();

    /**
     * Gets output names.
     *
     * @return
     */
    public String[] getOutputNames();

    /**
     * Sets input settings.
     *
     * @param inputs
     */
    public void setInputs(Map<String, Object> inputs);

    /**
     * Furnish default input image
     *
     * @param image
     * @param name
     */
    public void input(ItemWrapper image);

    /**
     * Furnish named input image
     *
     * @param image
     * @param name
     */
    public void input(ItemWrapper image, String name);

    /**
     * Listen for default output image.
     *
     * @param name
     * @param listener
     */
    public void setOutputListener(IOutputListener listener);

    /**
     * Listen for named output image.
     *
     * @param name
     * @param listener
     */
    public void setOutputListener(String name, IOutputListener listener);
}
