/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.workflow;

import imagej.workflow.IModuleFactory;
import imagej.workflow.IModule;

/**
 *
 * @author Aivar Grislis
 */
public class DummyComponentFactory implements IModuleFactory {
    private static DummyComponentFactory s_instance = null;
    
    private DummyComponentFactory() {
    }

    public static synchronized DummyComponentFactory getInstance() {
        if (null == s_instance) {
            s_instance = new DummyComponentFactory();
        }
        return s_instance;
    }

    public IModule create(String xml) {
        IModule component = new DummyComponent();
        component.fromXML(xml);
        return component;
    }
}
