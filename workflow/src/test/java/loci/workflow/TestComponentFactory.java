/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.workflow;

/**
 *
 * @author Aivar Grislis
 */
public class TestComponentFactory implements IModuleFactory {
    private static TestComponentFactory s_instance = null;
    
    private TestComponentFactory() {
    }

    public static synchronized TestComponentFactory getInstance() {
        if (null == s_instance) {
            s_instance = new TestComponentFactory();
        }
        return s_instance;
    }

    public IModule create(String xml) {
        IModule component = new TestComponent();
        component.fromXML(xml);
        return component;
    }
}
