//
// FinalizeTest.java
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
        System.out.println("testFinalize");
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
