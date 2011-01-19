/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.workflow;

import imagej.workflow.Workflow;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import imagej.workflow.plugin.annotations.Input;
import imagej.workflow.plugin.annotations.Output;

/**
 *
 * @author aivar
 */
public class FinalizeTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public FinalizeTest(String testName) {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(SerializeDeserializeTest.class);
    }

    public void testFinalize()
    {
        DummyComponent testComponentA = new DummyComponent();
        testComponentA.setName("A");
        testComponentA.setInputNames(new String[] { "ONE", "TWO" });
        testComponentA.setOutputNames(new String[] { Output.DEFAULT });
        DummyComponent testComponentB = new DummyComponent();
        testComponentB.setName("B");
        testComponentB.setInputNames(new String[] { Input.DEFAULT } );
        testComponentB.setOutputNames(new String[] { Output.DEFAULT });


        Workflow workFlow1 = new Workflow();
        workFlow1.setName("workFlow1");
        workFlow1.add(testComponentA);
        workFlow1.add(testComponentB);
        workFlow1.wire(testComponentA, testComponentB);
        workFlow1.finalize();

        assertTrue(workFlow1.getInputNames().length == 2);
        assertTrue(workFlow1.getInputNames()[0].equals("ONE"));
        assertTrue(workFlow1.getInputNames()[1].equals("TWO"));
        assertTrue(workFlow1.getOutputNames().length == 1);
        assertTrue(workFlow1.getOutputNames()[0].equals("OUTPUT"));
    }
}
