/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.workflow;

import loci.util.xmllight.XMLWriter;

/**
 * Provides information about a plugin module.
 *
 * @author aivar
 */
public class PluginModuleInfo implements IModuleInfo {
    final String m_name;
    final String m_fullName;
    String[] m_inputNames = new String[0];
    String[] m_outputNames = new String[0];

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
     * Gets input image names.
     *
     * @return
     */
    public String[] getInputNames() {
        return m_inputNames;
    }

    /**
     * Used within the package to set input names.
     *
     * @param inputNames
     */
    void setInputNames(String inputNames[]) {
        m_inputNames = inputNames;
    }

    /**
     * Gets output names.
     *
     * @return
     */
    public String[] getOutputNames() {
        return m_outputNames;
    }

    /**
     * Used within the package to set output names.
     *
     * @param outputNames
     */
    void setOutputNames(String outputNames[]) {
        m_outputNames = outputNames;
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
        for (String name : m_inputNames) {
            xmlHelper.addTag(Workflow.INPUT);
            xmlHelper.addTagWithContent(Workflow.NAME, name);
            xmlHelper.addEndTag(Workflow.INPUT);
        }
        xmlHelper.addEndTag(Workflow.INPUTS);

        // add outputs
        xmlHelper.addTag(Workflow.OUTPUTS);
        for (String name : m_outputNames) {
            xmlHelper.addTag(Workflow.OUTPUT);
            xmlHelper.addTagWithContent(Workflow.NAME, name);
            xmlHelper.addEndTag(Workflow.OUTPUT);
        }
        xmlHelper.addEndTag(Workflow.OUTPUTS);

        // end workflow
        xmlHelper.addEndTag(PluginModule.PLUGIN);

        return xmlBuilder.toString();
    }
}
