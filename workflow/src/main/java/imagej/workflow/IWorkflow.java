/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.workflow;

/**
 * An interface to build a workflow based on components that are chained
 * together.  The workflow itself is also a component.
 *
 * Building a workflow should take place in three phases:
 *   I.    Add components
 *   II.   Wire components together
 *   III.  Wire workflow inputs and outputs
 *           -or-
 *   III.  Finalize: leftover inputs and outputs become workflow inputs and outputs
 *
 * @author Aivar Grislis
 */
public interface IWorkflow extends IModule {

    /**
     * Adds a component to the workflow in phase I.
     *
     * @param component
     */
    void add(IModule component);

    /**
     * Chains default output of one component to default input of another.
     * Phase II.
     *
     * @param source
     * @param dest
     */
    void wire(IModule source, IModule dest);

    /**
     * Chains named output of one component to default input of another.
     * Phase II.
     *
     * @param source
     * @param sourceName
     * @param dest
     */
    void wire(IModule source, String sourceName, IModule dest);

    /**
     * Chains default output of one component to named input of another.
     * Phase II.
     *
     * @param source
     * @param dest
     * @param destName
     */
    void wire(IModule source, IModule dest, String destName);

    /**
     * Chains named output of one component to named input of another.
     * Phase II.
     *
     * @param source
     * @param sourceName
     * @param dest
     * @param destName
     */
    void wire(IModule source, String sourceName, IModule dest, String destName);

    /**
     * Gets the current chains.  Should be called after Phase II.
     *
     * @return array of chains
     */
    Wire[] getWires();

    /**
     * Leftover, un-wired module inputs and outputs become workflow inputs and
     * outputs.  Phase II -> III.
     */
    void finalize();

    /**
     * Chains default workflow input to default input of component.
     * Phase III.
     *
     * @param dest
     */
    void wireInput(IModule dest);

    /**
     * Chains default workflow input to named input of component.
     * Phase III.
     *
     * @param dest
     * @param destName
     */
    void wireInput(IModule dest, String destName);

    /**
     * Chains named workflow input to default input of component.
     * Phase III.
     *
     * @param inName
     * @param dest
     */
    void wireInput(String inName, IModule dest);

    /**
     * Chains named workflow input to named input of component.
     * Phase III.
     *
     * @param inName
     * @param dest
     * @param destName
     */
    void wireInput(String inName, IModule dest, String destName);

    /**
     * Chains default component output to default workflow output.
     * Phase III.
     *
     * @param source
     */
    void wireOutput(IModule source);

    /**
     * Chains named component output to default workflow output.
     * Phase III.
     *
     * @param source
     * @param sourceName
     */
    void wireOutput(IModule source, String sourceName);

    /**
     * Chains default component output to named workflow output.
     * Phase III.
     * 
     * @param outName
     * @param source
     */
    void wireOutput(String outName, IModule source);

    /**
     * Chains named component output to named workflow output.
     * Phase III.
     *
     * @param outName
     * @param source
     * @param sourceName
     */
    void wireOutput(String outName, IModule source, String sourceName);

    /**
     * Saves chained components as XML string representation.
     * Only after Phase III is complete.
     *
     * @return
     */
    String toXML();

    /**
     * Restores chained components from XML string representation.
     * Accomplishes Phases I-III.
     *
     * @param xml string containing XML representation
     * @return whether successfully parsed
     */
    boolean fromXML(String xml);
    
    /**
     * Stops processing.
     */
    void quit();

    /**
     * Clears wiring.
     */
    void clear();
}
