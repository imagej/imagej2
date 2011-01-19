/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.workflow;

/**
 *
 * @author aivar
 */
public interface IWorkflowManager {

    /**
     * Adds a new workflow or replaces previous.
     *
     * @param workflow
     */
    public void addWorkflow(IWorkflow workflow);

    /**
     * Deletes a named workflow.
     *
     * @param name
     */
    public void deleteWorkflow(String name);

    /**
     * Saves all workflows.
     */
    public void saveAllWorkflows();

    /**
     * Gets an array of module infos.  May be workflows and/or plugins.
     *
     * @return
     */
    public IModuleInfo[] getModuleInfos();

    /**
     * Gets an array of workflow infos.
     *
     * @return
     */
    public IWorkflowInfo[] getWorkflowInfos();

    /**
     * Given the module information, creates an instance of the
     * module.  Works for workflows and plugins.
     *
     * @param moduleInfo
     * @return
     */
    public IModule createInstance(IModuleInfo moduleInfo);
}
