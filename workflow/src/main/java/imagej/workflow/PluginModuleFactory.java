/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.workflow;

/**
 *
 * @author aivar
 */
public class PluginModuleFactory implements IModuleFactory {
    private static PluginModuleFactory s_instance = null;

    private PluginModuleFactory() {
    }

    public static synchronized PluginModuleFactory getInstance() {
        if (null == s_instance) {
            s_instance = new PluginModuleFactory();
        }
        return s_instance;
    }

    /**
     * Creates a plugin module from XML.
     *
     * @param xml string with XML representation
     * @return the module
     */
    public IModule create(String xml) {
        return create(xml, null);
    }

    /**
     * Creates a plugin module from XML, given a unique instance identifier.
     *
     * @param xml sring with XML representation
     * @param instanceId null or unique instance identifler
     * @return the module
     */
    public IModule create(String xml, String instanceId) {
        PluginModule module = new PluginModule();
        if (null != instanceId) {
            module.setInstanceId(instanceId);
        }
        module.fromXML(xml);
        return module;
    }
}
