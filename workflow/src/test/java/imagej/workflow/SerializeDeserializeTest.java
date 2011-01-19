/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.workflow;

import imagej.workflow.ModuleFactory;
import imagej.workflow.Workflow;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import imagej.workflow.plugin.annotations.Input;
import imagej.workflow.plugin.annotations.Output;

/**
 * Unit test for save/restore to/from XML.
 */
public class SerializeDeserializeTest extends TestCase
{
    private static final String XML_A = "<testA>whatever</testA>";
    private static final String XML_B = "<testB>whatever</testB>";

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public SerializeDeserializeTest(String testName) {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(SerializeDeserializeTest.class);
    }

    /**
     * Round trip to/from XML.
     */
    public void testSerialization()
    {
        TestComponent testComponentA = new TestComponent();
        testComponentA.setName("A");
        testComponentA.setInputNames(new String[] { "ONE", "TWO" });
        testComponentA.setOutputNames(new String[] { Output.DEFAULT });
        TestComponent testComponentB = new TestComponent();
        testComponentB.setName("B");
        testComponentB.setInputNames(new String[] { Input.DEFAULT } );
        testComponentB.setOutputNames(new String[] { Output.DEFAULT });

        TestComponentFactory componentFactory = TestComponentFactory.getInstance();
        ModuleFactory.getInstance().register(TestComponent.TESTCOMPONENT, componentFactory);

        Workflow workFlow1 = new Workflow();
        workFlow1.setName("workFlow1");
       // workFlow1.setModuleFactory(componentFactory);
        workFlow1.add(testComponentA);
        workFlow1.add(testComponentB);
        workFlow1.wire(testComponentA, testComponentB);
        workFlow1.wireInput("RED", testComponentA, "ONE");
        workFlow1.wireInput("BLUE", testComponentA, "TWO");
        workFlow1.wireOutput(testComponentB);

        String xml1 = workFlow1.toXML();
        System.out.println("workFlow1 XML [\n" + xml1 + "]");

        Workflow workFlow2 = new Workflow();
        workFlow2.fromXML(xml1);
        String xml2 = workFlow2.toXML();

        System.out.println("workFlow2 XML [\n" + xml2 + "]");

        assertTrue(xml1.equals(xml2));
    }

}
