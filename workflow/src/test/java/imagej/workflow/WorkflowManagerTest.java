/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.workflow;

import imagej.workflow.IWorkflowInfo;
import imagej.workflow.ModuleFactory;
import imagej.workflow.IModuleInfo;
import imagej.workflow.WireInfo;
import imagej.workflow.WorkflowManager;
import imagej.workflow.IWorkflow;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import imagej.workflow.util.xmllight.XMLException;

/**
 *
 * @author aivar
 */
public class WorkflowManagerTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public WorkflowManagerTest(String testName) {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(WorkflowManagerTest.class);
    }

    public void testDiscovery()
    {
        System.out.println("testDiscovery");
        
        String xml1 = "<workflow>"
                    + "  <name>My Workflow1</name>"
                    + "  <modules>"
                    + "    <module>"
                    + "      <name>DummyPlugin2</name>"
                    + "      <plugin>"
                    + "        <name>DummyPlugin2</name>"
                    + "        <classname>imagej.workflow.DummyPlugin2</classname>"
                    + "        <inputs>"
                    + "        </inputs>"
                    + "        <outputs>"
                    + "        </outputs>"
                    + "      </plugin>"
                    + "    </module>"
                    + "    <module>"
                    + "      <name>DummyPlugin</name>"
                    + "      <plugin>"
                    + "        <name>DummyPlugin</name>"
                    + "        <classname>imagej.workflow.DummyPlugin</classname>"
                    + "        <inputs>"
                    + "        </inputs>"
                    + "        <outputs>"
                    + "        </outputs>"
                    + "      </plugin>"
                    + "    </module>"
                    + "  </modules>"
                    + "  <wires>"
                    + "    <wire>"
                    + "      <src>"
                    + "        <module>DummyPlugin</module>"
                    + "        <name>LOWER</name>"
                    + "      </src>"
                    + "      <dst>"
                    + "        <module>DummyPlugin2</module>"
                    + "        <name>SECOND</name>"
                    + "      </dst>"
                    + "    </wire>"
                    + "    <wire>"
                    + "      <src>"
                    + "        <module>DummyPlugin</module>"
                    + "        <name>UPPER</name>"
                    + "      </src>"
                    + "      <dst>"
                    + "        <module>DummyPlugin2</module>"
                    + "        <name>FIRST</name>"
                    + "      </dst>"
                    + "    </wire>"
                    + "  </wires>"
                    + "  <inputs>"
                    + "  </inputs>"
                    + "  <outputs>"
                    + "  </outputs>"
                    + "</workflow>";

        String xml2 = "<workflow>"
                    + "  <name>My Workflow2</name>"
                    + "  <modules>"
                    + "    <module>"
                    + "      <name>DummyPlugin2</name>"
                    + "      <plugin>"
                    + "        <name>DummyPlugin2</name>"
                    + "        <classname>imagej.workflow.DummyPlugin2</classname>"
                    + "        <inputs>"
                    + "        </inputs>"
                    + "        <outputs>"
                    + "        </outputs>"
                    + "      </plugin>"
                    + "    </module>"
                    + "    <module>"
                    + "      <name>DummyPlugin</name>"
                    + "      <plugin>"
                    + "        <name>DummyPlugin</name>"
                    + "        <classname>imagej.workflow.DummyPlugin</classname>"
                    + "        <inputs>"
                    + "        </inputs>"
                    + "        <outputs>"
                    + "        </outputs>"
                    + "      </plugin>"
                    + "    </module>"
                    + "  </modules>"
                    + "  <wires>"
                    + "    <wire>"
                    + "      <src>"
                    + "        <module>DummyPlugin</module>"
                    + "        <name>LOWER</name>"
                    + "      </src>"
                    + "      <dst>"
                    + "        <module>DummyPlugin2</module>"
                    + "        <name>SECOND</name>"
                    + "      </dst>"
                    + "    </wire>"
                    + "    <wire>"
                    + "      <src>"
                    + "        <module>DummyPlugin</module>"
                    + "        <name>UPPER</name>"
                    + "      </src>"
                    + "      <dst>"
                    + "        <module>DummyPlugin2</module>"
                    + "        <name>FIRST</name>"
                    + "      </dst>"
                    + "    </wire>"
                    + "  </wires>"
                    + "  <inputs>"
                    + "  </inputs>"
                    + "  <outputs>"
                    + "  </outputs>"
                    + "</workflow>";


        System.out.println("GET MODULES");
        IModuleInfo[] modules = WorkflowManager.getInstance().getModuleInfos();
        for (IModuleInfo module : modules) {
            System.out.println("name " + module.getName());
            System.out.println("isWorkflow " + module.isWorkflow());
            System.out.println("inputs");
            showNames(module.getInputNames());
            System.out.println("outputs");
            showNames(module.getOutputNames());
            System.out.println("XML[" + module.toXML()  + "]");
            if (module.isWorkflow()) {
                IWorkflowInfo workflow = (IWorkflowInfo) module;
                WireInfo wireInfos[] = workflow.getWireInfos();
                for (WireInfo wireInfo : wireInfos) {
                    System.out.println("WIRE:");
                    System.out.println(" " + wireInfo.getSourceModuleName());
                    System.out.println("   " + wireInfo.getSourceName());
                    System.out.println(" -->");
                    System.out.println(" " + wireInfo.getDestModuleName());
                    System.out.println("   " + wireInfo.getDestName());
                }
            }
        }

        System.out.println("ADDS");
        IWorkflow workflow1 = null;
        IWorkflow workflow2 = null;
        try {
            workflow1 = (IWorkflow) ModuleFactory.getInstance().create(xml1);
            workflow2 = (IWorkflow) ModuleFactory.getInstance().create(xml2);
            WorkflowManager.getInstance().addWorkflow(workflow1);
            WorkflowManager.getInstance().addWorkflow(workflow2);
        }
        catch (XMLException e) {
            System.out.println("TEST XML PROBLEM " + e.getMessage());
        }

        modules = WorkflowManager.getInstance().getModuleInfos();
        for (IModuleInfo module : modules) {
            System.out.println("name " + module.getName());
            System.out.println("isWorkflow " + module.isWorkflow());
            System.out.println("inputs");
            showNames(module.getInputNames());
            System.out.println("outputs");
            showNames(module.getOutputNames());
            System.out.println("XML[" + module.toXML()  + "]");
            if (module.isWorkflow()) {
                IWorkflowInfo workflow = (IWorkflowInfo) module;
                WireInfo wireInfos[] = workflow.getWireInfos();
                for (WireInfo wireInfo : wireInfos) {
                    System.out.println("WIRE:");
                    System.out.println(" " + wireInfo.getSourceModuleName());
                    System.out.println("   " + wireInfo.getSourceName());
                    System.out.println(" -->");
                    System.out.println(" " + wireInfo.getDestModuleName());
                    System.out.println("   " + wireInfo.getDestName());
                }
            }
        }

        assertTrue(true);
    }

    private void showNames(String names[]) {
        for (String name : names) {
            System.out.println("  " + name);
        }
    }
}