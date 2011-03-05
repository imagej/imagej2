//
// WorkflowInfo.java
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

import java.util.ArrayList;
import java.util.List;

import imagej.workflow.util.xmllight.XMLException;
import imagej.workflow.util.xmllight.XMLParser;
import imagej.workflow.util.xmllight.XMLTag;
/**
 *
 * @author aivar
 */
public class WorkflowInfo implements IWorkflowInfo {
    final String m_xml;
    String m_name;
    String[] m_inputNames = new String[0];
    String[] m_outputNames = new String[0];
    String[] m_moduleNames = new String[0];
    WireInfo[] m_wireInfos;

    /**
     * Constructor
     *
     * @param fullName full package and class name of plugin
     */
    WorkflowInfo(String xml) {
        m_xml = xml;
        fromXML(xml);
    }

    /**
     * Gets name of module.
     *
     * @return
     */
    public String getName() {
        return m_name;
    }

    /**
     * Gets input item information array.
     *
     * @return
     */
    public IItemInfo[] getInputItemInfos() {
        throw new RuntimeException("not implemented");
        //return null;
    }

    /**
     * Gets output item information array.
     *
     * @return
     */
    public IItemInfo[] getOutputItemInfos() {
        throw new RuntimeException("not implemented");
        //return null;
    }

    /**
     * Is this a workflow?
     *
     * @return
     */
    public boolean isWorkflow() {
        return true;
    }

    /**
     * Gets module names.
     *
     * @return
     */
    public String[] getModuleNames() {
        return m_moduleNames;
    }

    /**
     * Gets wires that link modules information.
     *
     * @return
     */
    public WireInfo[] getWireInfos() {
        return m_wireInfos;

    }

    /**
     * Creates XML version of plugin
     *
     * @return
     */
    public String toXML() {
        return m_xml;
    }

    private boolean fromXML(String xml) {
        boolean success = false;
        XMLParser xmlHelper = new XMLParser();

        System.out.println("WorkflowInfo.fromXML >" + xml + "<");

        try {
            // handle workflow tag and name
            //
            // <workflow>
            //   <name>workFlow1</name>

            XMLTag tag = xmlHelper.getNextTag(xml);
            if (!Workflow.WORKFLOW.equals(tag.getName())) {
                throw new XMLException("Missing <workflow> tag");
            }
            xml = tag.getContent();
            tag = xmlHelper.getNextTag(xml);
            if (!Workflow.NAME.equals(tag.getName())) {
                throw new XMLException("Missing <name> for <workflow>");
            }
            m_name = tag.getContent();
            System.out.println("parsed name as " + m_name);
            xml = tag.getRemainder();

            // handle modules
            //
            //  <modules>
            //    <module>
            //      <name>A</name>
            //      <testA>whatever</testA>
            //    </module>
            //    <module>
            //      <name>B</name>
            //      <testB>whatever</testB>
            //    </module>
            //  </modules>

            List<String> modules = new ArrayList<String>();

            tag = xmlHelper.getNextTag(xml);
            if (!Workflow.MODULES.equals(tag.getName())) {
                throw new XMLException("Missing <modules> for <workflow>");
            }
            String modulesXML = tag.getContent();
            xml = tag.getRemainder();
            while (!modulesXML.isEmpty()) {
                tag = xmlHelper.getNextTag(modulesXML);
                modulesXML = tag.getRemainder();

                //if (tag.getName().isEmpty()) {
                //    break;
                //}
                if (!Workflow.MODULE.equals(tag.getName())) {
                    throw new XMLException("Missing <module> within <modules>");
                }
                String moduleXML = tag.getContent();
                tag = xmlHelper.getNextTag(moduleXML);
                if (!Workflow.NAME.equals(tag.getName())) {
                    throw new XMLException("Missing <name> within <module>");
                }
                modules.add(tag.getName());
            }
            m_moduleNames = modules.toArray(new String[0]);
            System.out.println("# mod names is " + m_moduleNames.length);

            // handle wires
            //
            //  <wires>
            //    <wire>
            //      <src>
            //        <module>A</module>
            //        <name>OUTPUT</name>
            //      </src>
            //      <dst>
            //        <module>B</module>
            //        <name>INPUT</name>
            //      </dst>
            //    </wire>
            //  </wires>

            List<WireInfo> wireInfos = new ArrayList<WireInfo>();

            tag = xmlHelper.getNextTag(xml);
            if (!Workflow.WIRES.equals(tag.getName())) {
                throw new XMLException("Missing <wires> within <workflow>");
            }
            String wiresXML = tag.getContent();
            xml = tag.getRemainder();
            while (!wiresXML.isEmpty()) {
                tag = xmlHelper.getNextTag(wiresXML);
                wiresXML = tag.getRemainder();

                //if (tag.getName().isEmpty()) {
                //    break;
                //}
                if (!Workflow.WIRE.equals(tag.getName())) {
                    throw new XMLException("Missing <wire> within <wires>");
                }
                String wireXML = tag.getContent();
                tag = xmlHelper.getNextTag(wireXML);
                wireXML = tag.getRemainder();
                if (!Workflow.SRC.equals(tag.getName())) {
                    throw new XMLException("Missing <src> within <wire>");
                }
                String srcXML = tag.getContent();
                ModuleNameAndName srcMNAN = parseModuleNameAndName(xmlHelper, srcXML);

                tag = xmlHelper.getNextTag(wireXML);
                if (!Workflow.DST.equals(tag.getName())) {
                    throw new XMLException("Missing <dst> within <wire>");
                }
                String dstXML = tag.getContent();
                ModuleNameAndName dstMNAN = parseModuleNameAndName(xmlHelper, dstXML);

                wireInfos.add(new WireInfo(srcMNAN.getModuleName(), srcMNAN.getName(), dstMNAN.getModuleName(), dstMNAN.getName()));
            }
            m_wireInfos = wireInfos.toArray(new WireInfo[0]);

            // handle inputs
            //
            //  <inputs>
            //    <input>
            //      <name>RED</name>
            //      <dst>
            //        <module>A</module>
            //        <name>ONE</name>
            //      </dst>
            //   </input>
            // </inputs>

            List<String> inputs = new ArrayList<String>();

            tag = xmlHelper.getNextTag(xml);
            if (!Workflow.INPUTS.equals(tag.getName())) {
                throw new XMLException("Missing <inputs> within <workflow>");
            }
            String inputsXML = tag.getContent();
            xml = tag.getRemainder();
            while (!inputsXML.isEmpty()) {
                tag = xmlHelper.getNextTag(inputsXML);
                inputsXML = tag.getRemainder();

                if (tag.getName().isEmpty()) { //TODO don't think these are necessary
                    break;
                }

                if (!Workflow.INPUT.equals(tag.getName())) {
                    throw new XMLException("Missing <input> within <inputs>");
                }
                String inputXML = tag.getContent();

                tag = xmlHelper.getNextTag(inputXML);
                inputXML = tag.getRemainder();

                if (!Workflow.NAME.equals(tag.getName())) {
                    throw new XMLException("Missing <name> within <input>");
                }
                String inName = tag.getContent();

                tag = xmlHelper.getNextTag(inputXML);
                if (!Workflow.DST.equals(tag.getName())) {
                    throw new XMLException("Missing <dest> within <input>");
                }
                String destXML = tag.getContent();
                ModuleNameAndName destMNAN = parseModuleNameAndName(xmlHelper, destXML);

                //TODO input is described by:
                //    inName, destMNAN.getModuleName(), destMNAN.getName()
                inputs.add(inName);
            }
            m_inputNames = inputs.toArray(new String[0]);

            // handle outputs
            //  <outputs>
            //    <output>
            //      <name>OUTPUT</name>
            //      <src>
            //        <module>B</module>
            //        <name>OUTPUT</name>
            //      </src>
            //    </output>
            //  </outputs>
            
            List<String> outputs = new ArrayList<String>();

            tag = xmlHelper.getNextTag(xml);
            if (!Workflow.OUTPUTS.equals(tag.getName())) {
                throw new XMLException("Missing <outputs> within <workflow>");
            }
            String outputsXML = tag.getContent();
            xml = tag.getRemainder();
            while (!outputsXML.isEmpty()) {
                tag = xmlHelper.getNextTag(outputsXML);
                outputsXML = tag.getRemainder();

                if (tag.getName().isEmpty()) { //TODO don't think these are necessary
                    break;
                }

                if (!Workflow.OUTPUT.equals(tag.getName())) {
                    throw new XMLException("Missing <output> within <outputs>");
                }
                String outputXML = tag.getContent();

                tag = xmlHelper.getNextTag(outputXML);
                outputXML = tag.getRemainder();

                if (!Workflow.NAME.equals(tag.getName())) {
                    throw new XMLException("Missing <name> within <output>");
                }
                String outName = tag.getContent();

                tag = xmlHelper.getNextTag(outputXML);
                if (!Workflow.SRC.equals(tag.getName())) {
                    throw new XMLException("Missing <src> within <output>");
                }
                String srcXML = tag.getContent();
                ModuleNameAndName srcMNAN = parseModuleNameAndName(xmlHelper, srcXML);

                //TODO input is described by:
                //    outName, srcMAN.getModule(), srcMAN.getName()

                outputs.add(outName);
            }
            m_outputNames = outputs.toArray(new String[0]);

            success = true;
        }
        catch (XMLException e) {
            System.out.println("XML Exception " + e.getMessage());
        }
        return success;
    }

    private ModuleNameAndName parseModuleNameAndName(XMLParser xmlHelper, String xml) throws XMLException {
        XMLTag tag = xmlHelper.getNextTag(xml);
        if (!Workflow.MODULE.equals(tag.getName())) {
            throw new XMLException("Missing <module> tag");
        }
        String moduleName = tag.getContent();
        xml = tag.getRemainder();
        tag = xmlHelper.getNextTag(xml);
        if (!Workflow.NAME.equals(tag.getName())) {
            throw new XMLException("Missing <name> tag");
        }
        String name = tag.getContent();

        return new ModuleNameAndName(moduleName, name);
    }

    /**
     * Data structure that keeps track of module name and name.
     */
    private class ModuleNameAndName {
        final String m_moduleName;
        final String m_name;

        ModuleNameAndName(String moduleName, String name) {
            m_moduleName = moduleName;
            m_name = name;
        }

        public String getModuleName() {
            return m_moduleName;
        }

        public String getName() {
        return m_name;
        }
    }
}
