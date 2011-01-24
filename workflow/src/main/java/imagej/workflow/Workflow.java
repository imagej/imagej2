/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.workflow;

import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import imagej.workflow.debug.PreviewInfo;
import imagej.workflow.debug.WorkflowDebugger;

import imagej.workflow.util.xmllight.XMLParser;
import imagej.workflow.util.xmllight.XMLWriter;
import imagej.workflow.util.xmllight.XMLException;
import imagej.workflow.util.xmllight.XMLTag;

import imagej.workflow.plugin.annotations.Input;
import imagej.workflow.plugin.annotations.Output;
import imagej.workflow.plugin.ItemWrapper;
import imagej.workflow.plugin.IPluginLauncher;
import imagej.workflow.plugin.PluginScheduler;

/**
 * Builds a workflow consisting of chained components.  A component could also
 * be another workflow.  Saves setup as XML file and restores from XML file.
 *
 * @author Aivar Grislis
 */
public class Workflow implements IModule, IWorkflow, IWorkflowDebug {
    public static final String WORKFLOW = "workflow";
    public static final String NAME = "name";
    public static final String MODULES = "modules";
    public static final String MODULE = "module";
    public static final String WIRES = "wires";
    public static final String WIRE = "wire";
    public static final String DST = "dst";
    public static final String SRC = "src";
    public static final String INPUTS = "inputs";
    public static final String INPUT = "input";
    public static final String OUTPUTS = "outputs";
    public static final String OUTPUT = "output";

    private IModuleFactory m_moduleFactory = ModuleFactory.getInstance();
    private String m_name;
    private String m_instanceId;
    private Map<String, IModule> m_moduleMap = new HashMap<String, IModule>();
    private List<String> m_inputNames = new ArrayList<String>();
    private List<String> m_outputNames = new ArrayList<String>();
    private List<Wire> m_wires = new ArrayList<Wire>();
    private Map<String, IModule> m_inputModules = new HashMap<String, IModule>();
    private Map<String, String> m_inputModuleNames = new HashMap<String, String>();
    private Map<String, IOutputListener> m_listeners = new HashMap<String, IOutputListener>();
    private Map<String, IModule> m_outputModules = new HashMap<String, IModule>();
    private Map<String, String> m_outputModuleNames = new HashMap<String, String>();
    private IOutputListener m_listener = new OutputListener();
    private Object m_synchObject = new Object();
    private WorkflowDebugger m_workflowDebugger = null;
    
    public Workflow() {
        m_instanceId = UUID.randomUUID().toString();
    }

    public Workflow(String instanceId) {
        m_instanceId = instanceId;
    }

    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    /**
     * Gets launcher.
     *
     * @param launcher
     */
    //TODO shouldn't a workflow have a launcher?  Perhaps we just wire all the
    //  PluginModules together.
    public IPluginLauncher getLauncher() {
        return null;
    }

    public String[] getInputNames() {
        return m_inputNames.toArray(new String[0]);
    }

    public String[] getOutputNames() {
        return m_outputNames.toArray(new String[0]);
    }

    public boolean fromXML(String xml) {
        boolean success = false;
        XMLParser xmlHelper = new XMLParser();

        try {
            // handle workflow tag and name
            //
            // <workflow>
            //   <name>workFlow1</name>
        
            XMLTag tag = xmlHelper.getNextTag(xml);
            if (!WORKFLOW.equals(tag.getName())) {
                throw new XMLException("Missing <workflow> tag");
            }
            xml = tag.getContent();
            tag = xmlHelper.getNextTag(xml);
            if (!NAME.equals(tag.getName())) {
                throw new XMLException("Missing <name> for <workflow>");
            }
            setName(tag.getContent());
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

            tag = xmlHelper.getNextTag(xml);
            if (!MODULES.equals(tag.getName())) {
                throw new XMLException("Missing <modules> for <workflow>");
            }
            String modulesXML = tag.getContent();
            xml = tag.getRemainder();
            while (!modulesXML.isEmpty()) {
                tag = xmlHelper.getNextTag(modulesXML);
                modulesXML = tag.getRemainder();

                if (tag.getName().isEmpty()) {
                    break;
                }
                if (!MODULE.equals(tag.getName())) {
                    throw new XMLException("Missing <module> within <modules>");
                }
                String moduleXML = tag.getContent();
                tag = xmlHelper.getNextTag(moduleXML);
                if (!NAME.equals(tag.getName())) {
                    throw new XMLException("Missing <name> within <module>");
                }
                IModule module = m_moduleFactory.create(tag.getRemainder());
                add(module);
            }

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

            tag = xmlHelper.getNextTag(xml);
            if (!WIRES.equals(tag.getName())) {
                throw new XMLException("Missing <wires> within <workflow>");
            }
            String wiresXML = tag.getContent();
            xml = tag.getRemainder();
            while (!wiresXML.isEmpty()) {
                tag = xmlHelper.getNextTag(wiresXML);
                wiresXML = tag.getRemainder();

                if (tag.getName().isEmpty()) {
                    break;
                }
                if (!WIRE.equals(tag.getName())) {
                    throw new XMLException("Missing <wire> within <wires>");
                }
                String wireXML = tag.getContent();
                tag = xmlHelper.getNextTag(wireXML);
                wireXML = tag.getRemainder();
                if (!SRC.equals(tag.getName())) {
                    throw new XMLException("Missing <src> within <wire>");
                }
                String srcXML = tag.getContent();
                ModuleAndName srcMAN = parseModuleAndName(xmlHelper, srcXML);
                
                tag = xmlHelper.getNextTag(wireXML);
                if (!DST.equals(tag.getName())) {
                    throw new XMLException("Missing <dst> within <wire>");
                }
                String dstXML = tag.getContent();
                ModuleAndName dstMAN = parseModuleAndName(xmlHelper, dstXML);

                // do the wiring
                wire(srcMAN.getModule(), srcMAN.getName(), dstMAN.getModule(), dstMAN.getName());
            }
            
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

            tag = xmlHelper.getNextTag(xml);
            if (!INPUTS.equals(tag.getName())) {
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

                if (!INPUT.equals(tag.getName())) {
                    throw new XMLException("Missing <input> within <inputs>");
                }
                String inputXML = tag.getContent();

                tag = xmlHelper.getNextTag(inputXML);
                inputXML = tag.getRemainder();

                if (!NAME.equals(tag.getName())) {
                    throw new XMLException("Missing <name> within <input>");
                }
                String inName = tag.getContent();

                tag = xmlHelper.getNextTag(inputXML);
                if (!DST.equals(tag.getName())) {
                    throw new XMLException("Missing <dest> within <input>");
                }
                String destXML = tag.getContent();
                ModuleAndName destMAN = parseModuleAndName(xmlHelper, destXML);

                wireInput(inName, destMAN.getModule(), destMAN.getName());
            }


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
            tag = xmlHelper.getNextTag(xml);
            if (!OUTPUTS.equals(tag.getName())) {
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

                if (!OUTPUT.equals(tag.getName())) {
                    throw new XMLException("Missing <output> within <outputs>");
                }
                String outputXML = tag.getContent();

                tag = xmlHelper.getNextTag(outputXML);
                outputXML = tag.getRemainder();

                if (!NAME.equals(tag.getName())) {
                    throw new XMLException("Missing <name> within <output>");
                }
                String outName = tag.getContent();

                tag = xmlHelper.getNextTag(outputXML);
                if (!SRC.equals(tag.getName())) {
                    throw new XMLException("Missing <src> within <output>");
                }
                String srcXML = tag.getContent();
                ModuleAndName srcMAN = parseModuleAndName(xmlHelper, srcXML);

                wireOutput(outName, srcMAN.getModule(), srcMAN.getName());
            }
            
            // finish the wiring
            finalize();
            
            success = true;
        }
        catch (XMLException e) {
            System.out.println("XML Exception " + e.getMessage());
        }
        return success;
    }
    
    private ModuleAndName parseModuleAndName(XMLParser xmlHelper, String xml) throws XMLException {
        XMLTag tag = xmlHelper.getNextTag(xml);
        if (!MODULE.equals(tag.getName())) {
            throw new XMLException("Missing <module> tag");
        }
        String moduleName = tag.getContent();
        xml = tag.getRemainder();
        tag = xmlHelper.getNextTag(xml);
        if (!NAME.equals(tag.getName())) {
            throw new XMLException("Missing <name> tag");
        }
        String name = tag.getContent();

        return new ModuleAndName(m_moduleMap.get(moduleName), name);
    }

    public String toXML() {
        StringBuilder xmlBuilder = new StringBuilder();
        XMLWriter xmlHelper = new XMLWriter(xmlBuilder);

        // add workflow tag and name
        xmlHelper.addTag(WORKFLOW);
        xmlHelper.addTagWithContent(NAME, getName());

        // add modules
        xmlHelper.addTag(MODULES);
        for (String name: m_moduleMap.keySet()) {
            xmlHelper.addTag(MODULE);
            xmlHelper.addTagWithContent(NAME, name);
            String moduleXML = m_moduleMap.get(name).toXML();
            xmlHelper.add(moduleXML);
            xmlHelper.addEndTag(MODULE);
        }
        xmlHelper.addEndTag(MODULES);

        // add wires
        xmlHelper.addTag(WIRES);
        for (Wire wire: m_wires) {
            xmlHelper.addTag(WIRE);
            xmlHelper.addTag(SRC);
            xmlHelper.addTagWithContent(MODULE, wire.getSource().getName());
            xmlHelper.addTagWithContent(NAME, wire.getSourceName());
            xmlHelper.addEndTag(SRC);
            xmlHelper.addTag(DST);
            xmlHelper.addTagWithContent(MODULE, wire.getDest().getName());
            xmlHelper.addTagWithContent(NAME, wire.getDestName());
            xmlHelper.addEndTag(DST);
            xmlHelper.addEndTag(WIRE);
        }
        xmlHelper.addEndTag(WIRES);

        // add inputs
        xmlHelper.addTag(INPUTS);
        for (String name : m_inputNames) {
            xmlHelper.addTag(INPUT);
            xmlHelper.addTagWithContent(NAME, name);
            xmlHelper.addTag(DST);
            xmlHelper.addTagWithContent(MODULE, m_inputModules.get(name).getName());
            xmlHelper.addTagWithContent(NAME, m_inputModuleNames.get(name));
            xmlHelper.addEndTag(DST);
            xmlHelper.addEndTag(INPUT);
        }
        xmlHelper.addEndTag(INPUTS);

        // add outputs
        xmlHelper.addTag(OUTPUTS);
        for (String name : m_outputNames) {
            xmlHelper.addTag(OUTPUT);
            xmlHelper.addTagWithContent(NAME, name);
            xmlHelper.addTag(SRC);
            xmlHelper.addTagWithContent(MODULE, m_outputModules.get(name).getName());
            xmlHelper.addTagWithContent(NAME, m_outputModuleNames.get(name));
            xmlHelper.addEndTag(SRC);
            xmlHelper.addEndTag(OUTPUT);
        }
        xmlHelper.addEndTag(OUTPUTS);

        // end workflow
        xmlHelper.addEndTag(WORKFLOW);

        return xmlBuilder.toString();
    }

    public void add(IModule component) {
        m_moduleMap.put(component.getName(), component);
    }

    public void wire(IModule source, IModule dest) {
        wire(source, Output.DEFAULT, dest, Input.DEFAULT);
    }

    public void wire(IModule source, String sourceName, IModule dest) {
        wire(source, sourceName, dest, Input.DEFAULT);
    }

    public void wire(IModule source, IModule dest, String destName) {
        wire(source, Output.DEFAULT, dest, destName);
    }

    public void wire(IModule source, String sourceName, IModule dest, String destName) {
        Wire wire = new Wire(source, sourceName, dest, destName);
        m_wires.add(wire);
    }

    public Wire[] getWires() {
        return m_wires.toArray(new Wire[0]);
    }

    public void finalize() {
        // do the wiring
        for (Wire wire: m_wires) {
            IPluginLauncher out = wire.getSource().getLauncher();
            String outName = wire.getSourceName();
            IPluginLauncher in = wire.getDest().getLauncher();
            String inName = wire.getDestName();
            PluginScheduler.getInstance().chain(out, outName, in, inName);
        }

        // promote leftover inputs and outputs to workflow inputs and outputs
        for (IModule module: m_moduleMap.values()) {
            for (String name : module.getInputNames()) {
                if (!isWiredAsInput(module, name)) {
                    wireInput(name, module, name);
                }
            }
            for (String name : module.getOutputNames()) {
                if (!isWiredAsOutput(module, name)) {
                    wireOutput(name, module, name);
                }
            }
        }
    }

    private boolean isWiredAsInput(IModule module, String name) {
        boolean found = false;

        // is this already an input?
        for (String inName : m_inputNames) {
            if (m_inputModules.get(inName).equals(module)
                    && m_inputModuleNames.get(inName).equals(name)) {
                found = true;
            }
        }

        if (!found) {
            // is this the destination of some internal wire?
            for (Wire wire: m_wires) {
                if (wire.getDest().equals(module) && wire.getDestName().equals(name)) {
                    found = true;
                }
            }
        }
        return found;
    }

    private boolean isWiredAsOutput(IModule module, String name) {
        boolean found = false;

        // is this already an output?
        if (null != m_outputModuleNames.get(name)) { //TODO see wireOutput; this is inadequate
            found = true;
        }

        if (!found) {
            // is this the source of some internal wire?
            for (Wire wire: m_wires) {
                if (wire.getSource().equals(module) && wire.getSourceName().equals(name)) {
                    found = true;
                }
            }
        }
        return found;
    }

    public void wireInput(IModule dest) {
        wireInput(Input.DEFAULT, dest, Input.DEFAULT);
    }

    public void wireInput(IModule dest, String destName) {
        wireInput(Input.DEFAULT, dest, destName);
    }

    public void wireInput(String inName, IModule dest) {
        wireInput(inName, dest, Input.DEFAULT);
    }

    public void wireInput(String inName, IModule dest, String destName) {
        // note new input name
        m_inputNames.add(inName);

        // save associated module
        m_inputModules.put(inName, dest);

        // associate dest name with input name
        m_inputModuleNames.put(inName, destName);
    }

    public void wireOutput(IModule source) {
        wireOutput(Output.DEFAULT, source, Output.DEFAULT);
    }

    public void wireOutput(IModule source, String sourceName) {
        wireOutput(Output.DEFAULT, source, sourceName);
    }

    public void wireOutput(String outName, IModule source) {
        wireOutput(Output.DEFAULT, source, outName);
    }

    public void wireOutput(String outName, IModule source, String sourceName) {
        // note new output name
        m_outputNames.add(outName);

        // save associated module
        m_outputModules.put(outName, source);

        // associate source name with output name
        m_outputModuleNames.put(sourceName, outName); //TODO WRONG!!! sourceName is not unique for all modules

        // listen for source name from source module
        source.setOutputListener(sourceName, m_listener);
    }

    public void input(ItemWrapper image) {
        input(image, Input.DEFAULT);
    }
    
    public void input(ItemWrapper image, String name) {
        if (m_inputNames.contains(name)) {
            IModule dest = m_inputModules.get(name);
            String destName = m_inputModuleNames.get(name);
            dest.input(image, destName);
        }
        else {
            System.out.println("input name not found: " + name);
        }
    }

    public void setOutputListener(IOutputListener listener) {
        synchronized (m_synchObject) {
            setOutputListener(Output.DEFAULT, listener);
        }
    }

    public void setOutputListener(String name, IOutputListener listener) {
        synchronized (m_synchObject) {
            m_listeners.put(name, listener);
        }
    }
    
    public void quit() {
        PluginScheduler.getInstance().quit();
    }

    public void clear() {
        //TODO more
        m_wires.clear();
        m_inputNames.clear();
        m_outputNames.clear();
        synchronized (m_synchObject) {
            m_listeners.clear();
        }
        if (null != m_workflowDebugger) {
            m_workflowDebugger.clear();
        }
    }

    /**
     * Starts debugging.
     */
    public void setDebug(boolean debug) {
        if (debug) {
            m_workflowDebugger = new WorkflowDebugger();
        }
        else {
            m_workflowDebugger = null;
        }
        PluginScheduler.getInstance().setDebugger(m_workflowDebugger);
    }

    /**
     * Gets debugger, if any.
     *
     * @return null or workflow debugger
     */
    public WorkflowDebugger getDebugger() {
        return m_workflowDebugger;
    }

    /**
     * Gets a snapshot of the preview information list.  Processes the debugging
     * information list.
     * <p>
     * Called during or after workflow execution.
     *
     * @return list of preview information.
     */
    public List<PreviewInfo> getPreviewInfoList() {
        List<PreviewInfo> previewInfoList = null;
        if (null != m_workflowDebugger) {
            previewInfoList = m_workflowDebugger.getPreviewInfoList();
        }
        return previewInfoList;
    }

    /**
     * Gets a snapshot of the preview information list for a given instance.
     * <p>
     * Called during or after workflow execution.  Can be called repeatedly as
     * workflow progresses.
     *
     * @param instanceId identifies the instance
     * @return list of preview information
     */
    public List<PreviewInfo> getPreviewInfoList(String instanceId) {
        List<PreviewInfo> previewInfoList = null;
        if (null != m_workflowDebugger) {
            previewInfoList = m_workflowDebugger.getPreviewInfoList(instanceId);
        }
        return previewInfoList;
    }

    /**
     * Listens for output images, passes them on to external listeners.
     */
    private class OutputListener implements IOutputListener {

        public void outputImage(String name, ItemWrapper image) {
            // get output name associated with this source name
            String outName = m_outputModuleNames.get(name);
            IOutputListener listener = m_listeners.get(outName);
            if (null != listener) {
                listener.outputImage(outName, image);
            }
        }
    }

    /**
     * Data structure that keeps track of IModule and name.
     */
    private class ModuleAndName {
        final IModule m_module;
        final String m_name;

        ModuleAndName(IModule module, String name) {
            m_module = module;
            m_name = name;
        }

        public IModule getModule() {
            return m_module;
        }

        public String getName() {
        return m_name;
        }
    }
}
