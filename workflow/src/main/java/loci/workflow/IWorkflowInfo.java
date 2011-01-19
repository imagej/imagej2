/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.workflow;

/**
 *
 * @author aivar
 */
public interface IWorkflowInfo extends IModuleInfo {

    /**
     * Gets module names.
     *
     * @return
     */
    public String[] getModuleNames();

    /**
     * Gets information about wires that link modules.
     *
     * @return
     */
    public WireInfo[] getWireInfos();
}
