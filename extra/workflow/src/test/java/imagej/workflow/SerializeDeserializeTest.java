//
// SerializeDeserializeTest.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
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
        System.out.println("testSerialization");

        DummyComponent testComponentA = new DummyComponent();
        testComponentA.setName("A");
        testComponentA.setInputNames(new String[] { "ONE", "TWO" });
        testComponentA.setOutputNames(new String[] { Output.DEFAULT });
        DummyComponent testComponentB = new DummyComponent();
        testComponentB.setName("B");
        testComponentB.setInputNames(new String[] { Input.DEFAULT } );
        testComponentB.setOutputNames(new String[] { Output.DEFAULT });

        DummyComponentFactory componentFactory = DummyComponentFactory.getInstance();
        ModuleFactory.getInstance().register(DummyComponent.TESTCOMPONENT, componentFactory);

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
