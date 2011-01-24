/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
