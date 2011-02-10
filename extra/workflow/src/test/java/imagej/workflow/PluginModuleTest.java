/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.workflow;

import imagej.workflow.ModuleFactory;
import imagej.workflow.PluginModule;
import imagej.workflow.IModule;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author aivar
 */
public class PluginModuleTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public PluginModuleTest(String testName) {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(PluginModuleTest.class);
    }

    public void testPluginModule()
    {
        System.out.println("testPluginModule");
        PluginModule module1 = new PluginModule("imagej.workflow.DummyPlugin");
        String xml1 = module1.toXML();

        String xml2 = null;
        try {
            IModule module2 = ModuleFactory.getInstance().create(xml1);
            xml2 = module2.toXML();
        }
        catch (Exception e) {
            System.out.println("exception creating plugin from XML " + e.getMessage());
        }
        assertTrue(xml1.equals(xml2));

        System.out.println("XML is [[" + xml2 + "]]");
    }
}
