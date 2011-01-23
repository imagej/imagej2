/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.workflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

import imagej.workflow.plugin.annotations.Item;
import imagej.workflow.plugin.annotations.Input;
import imagej.workflow.plugin.annotations.Output;
import imagej.workflow.util.xmllight.XMLException;
import imagej.workflow.util.xmllight.XMLParser;
import imagej.workflow.util.xmllight.XMLTag;
import imagej.workflow.plugin.IPlugin;
import imagej.workflow.plugin.PluginAnnotations;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

/**
 *
 * @author aivar
 */
public class WorkflowManager implements IWorkflowManager {
    private static final String WORKFLOWS = "workflows";
    private static String s_xml = "";
    private static WorkflowManager s_instance = null;
    private enum Type { INPUT, OUTPUT };
    private Preferences m_prefs = Preferences.userNodeForPackage(getClass());
    private Map<String, IWorkflowInfo> m_workflows;
    private Map<String, IModuleInfo> m_plugins;

    /**
     * Gets singleton instance.
     *
     * @return
     */
    public static synchronized WorkflowManager getInstance() {
        if (null == s_instance) {
            s_instance = new WorkflowManager();
        }
        return s_instance;
    }

    /**
     * Private constructor.
     */
    private WorkflowManager() {
        m_workflows = parseWorkflows(m_prefs.get(WORKFLOWS, s_xml));
        m_plugins = discoverPlugins();
    }

    /**
     * Adds a new workflow or replaces previous.
     *
     * @param workflow
     */
    public void addWorkflow(IWorkflow workflow) {
        m_workflows.put(workflow.getName(), new WorkflowInfo(workflow.toXML())); //TODO going to XML and back is odd
    }

    /**
     * Deletes a named workflow.
     *
     * @param name
     */
    public void deleteWorkflow(String name) {
        m_workflows.remove(name);
    }

    /**
     * Saves all workflows.
     */
    public void saveAllWorkflows() {
        StringBuilder builder = new StringBuilder();
        for (IWorkflowInfo workflow : m_workflows.values()) {
            builder.append(workflow.toXML());
        }
        String xml = builder.toString();
        if (xml.length() > Preferences.MAX_VALUE_LENGTH) {
            System.out.println("Workflows string exceeds " + Preferences.MAX_VALUE_LENGTH);
        }
        m_prefs.put(WORKFLOWS, xml);
    }

    /**
     * Gets an array of module infos.  May be workflows and/or plugins.
     * 
     * @return
     */
    public IModuleInfo[] getModuleInfos() {
        List<IModuleInfo> moduleList = new ArrayList<IModuleInfo>();
        moduleList.addAll(m_plugins.values());
        moduleList.addAll(m_workflows.values());
        //Collections.sort(moduleList); //TODO get "cannot find symbol sort(java.util.List<IModuleInfo>)"; should compile but give a generics warning
        return moduleList.toArray(new IModuleInfo[0]);
    }

    /**
     * Gets an array of workflow infos.
     *
     * @return
     */
    public IWorkflowInfo[] getWorkflowInfos() {
        return m_workflows.values().toArray(new IWorkflowInfo[0]);
    }

    /**
     * Given the module information, creates an instance of the
     * module.  Works for workflows and plugins.
     *
     * @param moduleInfo
     * @return
     */
    public IModule createInstance(IModuleInfo moduleInfo) {
        String xml = moduleInfo.toXML();
        IModule module = null;
        try {
            module = ModuleFactory.getInstance().create(xml);
        }
        catch (XMLException e) {
            System.out.println("internal XML problem " + e.getMessage());
        }
        return module;
    }

    /**
     * Parses XML string to get workflow infos.
     *
     * @param xml
     * @return map of name to workflow info
     */
    private Map<String, IWorkflowInfo> parseWorkflows(String xml) {
        System.out.println("PARSE>" + xml + "<");
        Map<String, IWorkflowInfo> map = new HashMap<String, IWorkflowInfo>();
        XMLParser xmlHelper = new XMLParser();

        try {
            while (!xml.isEmpty()) {
                // look for workflow tag
                XMLTag tag = xmlHelper.getNextTag(xml);
                if (!Workflow.WORKFLOW.equals(tag.getName())) {
                    throw new XMLException("Missing <workflow> tag");
                }
                IWorkflowInfo workflowInfo = new WorkflowInfo(xml);
                map.put(workflowInfo.getName(), workflowInfo);
                xml = tag.getRemainder();
            }
        }
        catch (XMLException e) {
            System.out.println("Internal XML Error " + e.getMessage());
        }
        return map;
    }

    /**
     * Plugin discovery mechanism using SezPoz.
     *
     * @return map of name to plugin info
     */
    private Map<String, IModuleInfo> discoverPlugins() {
        Map<String, IModuleInfo> pluginInfos = new HashMap<String, IModuleInfo>();

        // Currently IPlugins are annotated with either Input or Output
        // annotations.  Most will have both but its not required.

        // look for IPlugins with Input annotation
        for (final IndexItem<Input, IPlugin> indexItem : Index.load(Input.class, IPlugin.class)) {
            PluginModuleInfo pluginInfo = getPluginInfo(
                    null, Type.INPUT,
                    indexItem.className(), indexItem.annotation().value());
            pluginInfos.put(pluginInfo.getName(), pluginInfo);
        }

        // look for IPlugins with Output annotation
        for (final IndexItem<Output, IPlugin> indexItem : Index.load(Output.class, IPlugin.class)) {
            String fullName = indexItem.className();
            PluginModuleInfo pluginInfo = getPluginInfo(
                    (PluginModuleInfo) pluginInfos.get(PluginModuleInfo.getName(fullName)),
                    Type.OUTPUT,
                    fullName, indexItem.annotation().value());
            pluginInfos.put(pluginInfo.getName(), pluginInfo);
        };
        return pluginInfos;
    }

    /**
     * Creates plugin info from full name and image names.  This is called for both
     * input and output annotations.
     *
     * @param info existing plugin info or null
     * @param inputOrOutput whether input or output annotation
     * @param fullName full package and class name
     * @param items array of Item annotations
     * @return info
     */
    private PluginModuleInfo getPluginInfo(PluginModuleInfo info, Type inputOrOutput, String fullName, Item[] items) {
        if (null == info) {
            info = new PluginModuleInfo(fullName);
        }

        // build list of image names
        List<String> names = new ArrayList<String>();
        if (0 == items.length) {
            names.add(Type.INPUT == inputOrOutput ? Input.DEFAULT : Output.DEFAULT);
        }
        else {
            for (Item item : items) {
                names.add(item.name());
            }
        }

        if (Type.INPUT == inputOrOutput) {
            info.setInputNames(names.toArray(new String[0]));
        }
        else {
            info.setOutputNames(names.toArray(new String[0]));
        }
        return info;
    }
}
