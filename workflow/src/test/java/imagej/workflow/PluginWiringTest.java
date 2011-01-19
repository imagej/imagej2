/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.workflow;

import imagej.workflow.ModuleFactory;
import imagej.workflow.PluginModule;
import imagej.workflow.IModule;
import imagej.workflow.Workflow;
import imagej.workflow.IWorkflow;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import imagej.workflow.util.xmllight.XMLException;
import imagej.workflow.plugin.ItemWrapper;

/**
 *
 * @author aivar
 */
public class PluginWiringTest extends TestCase {
    String m_xml;

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public PluginWiringTest(String testName) {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(PluginWiringTest.class);
    }

    public void testPluginWiring()
    {
        // create some test plugin modules
        PluginModule module1 = new PluginModule("loci.workflow.TestPlugin");
        PluginModule module2 = new PluginModule("loci.workflow.TestPlugin2");

        // create workflow, add & wire modules
        IWorkflow workflow = new Workflow();
        workflow.setName("My Workflow");
        workflow.add(module1);
        workflow.add(module2);
        workflow.wire(module1, TestPlugin.LOWER, module2, TestPlugin2.SECOND);
        workflow.wire(module1, TestPlugin.UPPER, module2, TestPlugin2.FIRST);
        workflow.finalize();

        // roundtrip workflow to/from XML
        String xml = workflow.toXML();
        IModule workflow2 = null;
        try {
            workflow2 = ModuleFactory.getInstance().create(xml);
        }
        catch (XMLException e) {
            System.out.println("XML problem " + e.getMessage());
        }

        // create input item, start workflow
        ItemWrapper item = new ItemWrapper("HELLO");
        workflow2.input(item);

        System.out.println("workflow [" + workflow.toXML() + "]");
    }
}
