/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.workflow;

import java.util.HashMap;
import java.util.Map;

import imagej.workflow.util.xmllight.XMLParser;
import imagej.workflow.util.xmllight.XMLException;
import imagej.workflow.util.xmllight.XMLTag;

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
     * @throws XMLException
     */
    public IModule create(String xml) throws XMLException {
        return create(xml, null);
    }

    /**
     * Creates a component from XML, given the unique instance identifier.
     *
     * @param xml
     * @param instanceId
     * @return
     * @throws XMLException
     */
    public IModule create(String xml, String instanceId) throws XMLException {
        IModule module = null;
        XMLParser xmlHelper = new XMLParser();
        XMLTag tag = xmlHelper.getNextTag(xml);
        IModuleFactory factory = m_factories.get(tag.getName());
        if (null != factory) {
            module = factory.create(xml, instanceId);
        }
        else {
            throw new XMLException("Invalid tag " + tag.getName());
        }
        return module;
    }
}
