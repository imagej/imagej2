/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
//TODO
/*
 * Is the name a short name or the full path, i.e. loci.plugin.Whatever?  Why have a set name?
 * Need to instantiate the plugin.
 * Feeding in the image should work; how to set up listener?
 * How to do the linking?  IComponent could have chain() method, delegates to m_linkedPlugin.  How do WorkFlows get
 * chained?
 */

package imagej.workflow;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import imagej.workflow.plugin.AbstractPlugin;
import imagej.workflow.plugin.ItemWrapper;
import imagej.workflow.plugin.IPlugin;
import imagej.workflow.plugin.IPluginLauncher;
import imagej.workflow.plugin.PluginAnnotations;
import imagej.workflow.plugin.PluginClassException;
import imagej.workflow.plugin.PluginLauncher;
import imagej.workflow.plugin.annotations.Input;
import imagej.workflow.plugin.annotations.Output;
import imagej.workflow.util.xmllight.XMLException;
import imagej.workflow.util.xmllight.XMLParser;
import imagej.workflow.util.xmllight.XMLTag;
import imagej.workflow.util.xmllight.XMLWriter;

/**
 *
 * @author Aivar Grislis
 */
public class PluginModule implements IModule {
    public static final String PLUGIN = "plugin";
    public static final String CLASSNAME = "classname";
    String m_pluginClassName;
    String m_name;
    String m_instanceId;
    PluginAnnotations m_annotations;
    IPluginLauncher m_launcher;
    Set<String> m_inputNames = Collections.EMPTY_SET;
    Set<String> m_outputNames = Collections.EMPTY_SET;
    Map<String, IOutputListener> m_listenerMap = new HashMap<String, IOutputListener>();

    /**
     * Create a plugin module instance.  Generates a unique instance identifier.
     */
    public PluginModule() {
        m_instanceId = UUID.randomUUID().toString();
    }

    /**
     * Create an instance for a given plugin class name.
     *
     * @param pluginClassName
     */
    public PluginModule(String pluginClassName) throws PluginClassException {
        init(pluginClassName);
    }

    /**
     * Create an instance for a given plugin class.
     *
     * @param className
     * @param instanceId null or unique identifier for this instance
     */
    public PluginModule(Class pluginClass) {
        init(pluginClass);
    }

    /**
     * Initializes given a plugin class name.
     *
     * @param className
     */
    private void init(String pluginClassName) {
        m_instanceId = UUID.randomUUID().toString();

        // get associated class
        Class pluginClass = null;
        try {
            pluginClass = Class.forName(pluginClassName);
        }
        catch (ClassNotFoundException e) {
            // class cannot be located
            System.out.println("Can't find " + pluginClassName);
        }
        catch (ExceptionInInitializerError e) {
            // initialization provoked by this method fails
            System.out.println("Error initializing " + pluginClassName + " " + e.getStackTrace());
        }
        catch (LinkageError e) {
            // linkage fails
            System.out.println("Linkage error " + pluginClassName + " " + e.getStackTrace());
        }

        // validate class
        boolean success = false;
        if (null != pluginClass) {
            success = true;

            System.out.println(pluginClass.toString());

            if (!pluginClass.isAssignableFrom(AbstractPlugin.class)) {
                //success = false; //TODO fails this!!
                System.out.println("Plugin " + pluginClassName + " should extend AbstractPlugin");
            }

            if (!pluginClass.isAssignableFrom(IPlugin.class)) {
                //success = false; //TODO fails this!!
                System.out.println("Plugin " + pluginClassName + " should implement IPlugin");
            }
        }

        if (success) {
            init(pluginClass);
        }
        else {
            throw new PluginClassException("Plugin class is invalid " + pluginClassName);
        }
    }

    /**
     * Initializes given a plugin class.
     *
     * @param pluginClass
     * @param instanceId null or unique instance identifier
     */
    private void init(Class pluginClass) {
        m_pluginClassName = pluginClass.getName();
        int lastDotIndex = m_pluginClassName.lastIndexOf('.');
        m_name = m_pluginClassName.substring(lastDotIndex + 1, m_pluginClassName.length());

        // examine annotations
        m_annotations = new PluginAnnotations(pluginClass);
        m_inputNames = m_annotations.getInputNames();
        m_outputNames = m_annotations.getOutputNames();

        // create launcher
        m_launcher = new PluginLauncher(pluginClass, m_instanceId, m_annotations);
    }

    /**
     * Sets a unique instance identifier.
     * 
     * @param instanceId
     */
    public void setInstanceId(String instanceId) {
        m_instanceId = instanceId;
    }

    /**
     * Gets name of component.
     *
     * @return
     */
    public String getName() {
        return m_name;
    }

    /**
     * Sets name of component.
     *
     * @param name
     */
    public void setName(String name) {
        m_name = name;
    }

    public IPluginLauncher getLauncher() {
        return m_launcher;
    }

    /**
     * Saves component as XML string representation.
     *
     * @return
     */
    public String toXML() {
        StringBuilder xmlBuilder = new StringBuilder();
        XMLWriter xmlHelper = new XMLWriter(xmlBuilder);

        // add workflow tag, name, and class name
        xmlHelper.addTag(PLUGIN);
        xmlHelper.addTagWithContent(Workflow.NAME, getName());
        xmlHelper.addTagWithContent(CLASSNAME, m_pluginClassName);

        // add inputs
        xmlHelper.addTag(Workflow.INPUTS);
        for (String name : m_inputNames) {
            xmlHelper.addTag(Workflow.INPUT);
            xmlHelper.addTagWithContent(Workflow.NAME, name);
            xmlHelper.addEndTag(Workflow.INPUT);
        }
        xmlHelper.addEndTag(Workflow.INPUTS);

        // add outputs
        xmlHelper.addTag(Workflow.OUTPUTS);
        for (String name : m_outputNames) {
            xmlHelper.addTag(Workflow.OUTPUT);
            xmlHelper.addTagWithContent(Workflow.NAME, name);
            xmlHelper.addEndTag(Workflow.OUTPUT);
        }
        xmlHelper.addEndTag(Workflow.OUTPUTS);

        // end workflow
        xmlHelper.addEndTag(PLUGIN);

        return xmlBuilder.toString();
    }

    /**
     * Restores component from XML string representation.
     *
     * @param xml
     * @return whether successfully parsed
     */
    public boolean fromXML(String xml) {
        boolean success = false;
        XMLParser xmlHelper = new XMLParser();

        try {
            // handle test tag and name
            //
            // <plugin>
            //   <name>A</name>

            XMLTag tag = xmlHelper.getNextTag(xml);
            if (!PLUGIN.equals(tag.getName())) {
                throw new XMLException("Missing <plugin> tag");
            }
            xml = tag.getContent();
            tag = xmlHelper.getNextTag(xml);
            if (!Workflow.NAME.equals(tag.getName())) {
                throw new XMLException("Missing <name> for <plugin>");
            }
            setName(tag.getContent());
            xml = tag.getRemainder();

            // handle class name
            tag = xmlHelper.getNextTag(xml);
            if (!CLASSNAME.equals(tag.getName())) {
                throw new XMLException("Missing <classname> for <plugin>");
            }
            init(tag.getContent());
            if (true) return true; //TODO the follow code analyzes given input/output names, which are merely a descriptive nicety; could compare with annotated input/output names.

            // handle inputs
            //
            //  <inputs>
            //    <input>
            //      <name>RED</name>
            //   </input>
            // </inputs>

            tag = xmlHelper.getNextTag(xml);
            if (!Workflow.INPUTS.equals(tag.getName())) {
                throw new XMLException("Missing <inputs> within <plugin>");
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

                m_inputNames.add(inName);
            }

            // handle outputs
            //  <outputs>
            //    <output>
            //      <name>OUTPUT</name>
            //    </output>
            //  </outputs>
            tag = xmlHelper.getNextTag(xml);
            if (!Workflow.OUTPUTS.equals(tag.getName())) {
                throw new XMLException("Missing <outputs> within <plugin>");
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
                m_outputNames.add(outName);
            }
            success = true;
        }
        catch (XMLException e) {
            System.out.println("XML Exception");
        }
        return success;
    }

    /**
     * Gets input image names.
     *
     * @return
     */
    public String[] getInputNames() {
        return m_inputNames.toArray(new String[0]);
    }

    /**
     * Gets output names.
     *
     * @return
     */
    public String[] getOutputNames() {
        return m_outputNames.toArray(new String[0]);
    }

    /**
     * Furnish input image.
     *
     * @param image
     */
    public void input(ItemWrapper image) {
        input(image, Input.DEFAULT);
    }

    /**
     * Furnish input image
     *
     * @param image
     * @param name
     */
    public void input(ItemWrapper image, String name) {
        m_launcher.externalPut(name, image); //TODO order inconsistency!
    }

    /**
     * Listen for output image.
     *
     * @param listener
     */
    public void setOutputListener(IOutputListener listener) {
        setOutputListener(Output.DEFAULT, listener);
    }

    /**
     * Listen for output image.
     *
     * @param name
     * @param listener
     */
    public void setOutputListener(String name, IOutputListener listener) {
        m_listenerMap.put(name, listener);
        //TODO hook up the listener
    }

}
