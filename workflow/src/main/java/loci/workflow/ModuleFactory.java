/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.workflow;

import java.util.HashMap;
import java.util.Map;

import loci.util.xmllight.XMLParser;
import loci.util.xmllight.XMLException;
import loci.util.xmllight.XMLTag;

/**
 *
 * @author Aivar Grislis
 */
public class ModuleFactory implements IModuleFactory {
    private static ModuleFactory s_instance;
    private Map<String, IModuleFactory> m_factories = new HashMap<String, IModuleFactory>();
    
    private ModuleFactory() {
        register(Workflow.WORKFLOW, WorkflowFactory.getInstance());
        //register(Component.COMPONENT, ComponentFactory.getInstance());
        register(PluginModule.PLUGIN, PluginModuleFactory.getInstance());
    }

    /**
     * Gets singleton instance.
     *
     * @return instance
     */
    public static synchronized ModuleFactory getInstance() {
        if (null == s_instance) {
            s_instance = new ModuleFactory();
        }
        return s_instance;
    }

    public void register(String tagName, IModuleFactory factory) {
        m_factories.put(tagName, factory);
    }

    /**
     * Creates a component from XML.
     *
     * @param xml
     * @return
     */
    public IModule create(String xml) throws XMLException {
        IModule module = null;
        XMLParser xmlHelper = new XMLParser();
        XMLTag tag = xmlHelper.getNextTag(xml);
        IModuleFactory factory = m_factories.get(tag.getName());
        if (null != factory) {
            module = factory.create(xml);
        }
        else {
            throw new XMLException("Invalid tag " + tag.getName());
        }
        return module;
    }
}
