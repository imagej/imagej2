/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.workflow;

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
     * Gets input item information array.
     *
     * @return
     */
    public IItemInfo[] getInputItemInfos();

    /**
     * Gets output item information array.
     *
     * @return
     */
    public IItemInfo[] getOutputItemInfos();

    /**
     * Is this a workflow?
     *
     * @return
     */
    public boolean isWorkflow();
}
