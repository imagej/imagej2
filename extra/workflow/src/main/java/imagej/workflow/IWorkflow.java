//
// IWorkflow.java
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
