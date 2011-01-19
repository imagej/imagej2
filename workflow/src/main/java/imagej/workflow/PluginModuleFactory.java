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
     * @param xml
     * @return
     */
    public IModule create(String xml) {
        PluginModule module = new PluginModule();
        module.fromXML(xml);
        return module;
    }
}
