//
// PluginModuleInfo.java
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

package imagej.workflow;

import imagej.workflow.util.xmllight.XMLWriter;

/**
 * Provides information about a plugin module.
 *
 * @author aivar
 */
public class PluginModuleInfo implements IModuleInfo {
    final String m_name;
    final String m_fullName;
    IItemInfo[] m_inputItemInfos = new IItemInfo[0];
    IItemInfo[] m_outputItemInfos = new IItemInfo[0];

    /**
     * Static method call used within the package to derive the plugin name
     * from the full name consistently.
     *
     * @param fullName
     * @return
     */
    static String getName(String fullName) {
        return fullName.substring(fullName.lastIndexOf('.') + 1, fullName.length());
    }

    /**
     * Constructor
     *
     * @param fullName full package and class name of plugin
     */
    PluginModuleInfo(String fullName) {
        m_fullName = fullName;
        m_name = getName(fullName);
    }

    /**
     * Gets name of module.
     *
     * @return
     */
    public String getName() {
        return m_name;
    }

    /**
     * Gets input item information array.
     *
     * @return
     */
    public IItemInfo[] getInputItemInfos() {
        return m_inputItemInfos;
    }

    /**
     * Used within the package to set input item information array.
     *
     * @param inputNames
     */
    void setInputItemInfos(IItemInfo inputItemInfos[]) {
        m_inputItemInfos = inputItemInfos;
    }

    /**
     * Gets output item information array.
     *
     * @return
     */
    public IItemInfo[] getOutputItemInfos() {
        return m_outputItemInfos;
    }

    /**
     * Used within the package to set output item information array.
     *
     * @param outputNames
     */
    void setOutputItemInfos(IItemInfo outputItemInfos[]) {
        m_outputItemInfos = outputItemInfos;
    }

    /**
     * Is this a workflow?
     *
     * @return
     */
    public boolean isWorkflow() {
        return false;
    }

    /**
     * Creates XML version of plugin
     *
     * @return
     */

    //TODO this code is currently in two places.
    //  copied from PluginModule.java

    public String toXML() {
        StringBuilder xmlBuilder = new StringBuilder();
        XMLWriter xmlHelper = new XMLWriter(xmlBuilder);

        // add workflow tag, name, and class name
        xmlHelper.addTag(PluginModule.PLUGIN);
        xmlHelper.addTagWithContent(Workflow.NAME, getName());
        xmlHelper.addTagWithContent(PluginModule.CLASSNAME, m_fullName);

        // add inputs
        xmlHelper.addTag(Workflow.INPUTS);
        for (IItemInfo itemInfo : m_inputItemInfos) {
            xmlHelper.addTag(Workflow.INPUT);
            xmlHelper.addTagWithContent(Workflow.NAME, itemInfo.getName());
            xmlHelper.addEndTag(Workflow.INPUT);
        }
        xmlHelper.addEndTag(Workflow.INPUTS);

        // add outputs
        xmlHelper.addTag(Workflow.OUTPUTS);
        for (IItemInfo itemInfo : m_outputItemInfos) {
            xmlHelper.addTag(Workflow.OUTPUT);
            xmlHelper.addTagWithContent(Workflow.NAME, itemInfo.getName());
            xmlHelper.addEndTag(Workflow.OUTPUT);
        }
        xmlHelper.addEndTag(Workflow.OUTPUTS);

        // end workflow
        xmlHelper.addEndTag(PluginModule.PLUGIN);

        return xmlBuilder.toString();
    }
}
