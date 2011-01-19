/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.workflow;

/**
 * Information about a module.  Module may be a workflow or a plugin.
 *
 * @author aivar
 */
public interface IModuleInfo extends IModuleInfoInternal {

    /**
     * Gets name of module.
     *
     * @return
     */
    public String getName();

    /**
     * Gets input image names.
     *
     * @return
     */
    public String[] getInputNames();

    /**
     * Gets output names.
     *
     * @return
     */
    public String[] getOutputNames();

    /**
     * Is this a workflow?
     *
     * @return
     */
    public boolean isWorkflow();
}
