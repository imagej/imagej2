//
// PluginWiringTest.java
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
        System.out.println("testPluginWiring");

        // create some test plugin modules
        PluginModule module1 = new PluginModule("imagej.workflow.DummyPlugin");
        PluginModule module2 = new PluginModule("imagej.workflow.DummyPlugin2");

        // create workflow, add & wire modules
        IWorkflow workflow = new Workflow();
        workflow.setName("My Workflow");
        workflow.add(module1);
        workflow.add(module2);
        workflow.wire(module1, DummyPlugin.LOWER, module2, DummyPlugin2.SECOND);
        workflow.wire(module1, DummyPlugin.UPPER, module2, DummyPlugin2.FIRST);
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
