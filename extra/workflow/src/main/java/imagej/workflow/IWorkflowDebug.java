/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.workflow;

import java.util.List;

import imagej.workflow.debug.PreviewInfo;
import imagej.workflow.debug.WorkflowDebugger;

/**
 *
 * @author aivar
 */
public interface IWorkflowDebug {

    /**
     * Starts debugging.
     */
    public void setDebug(boolean debug);

    /**
     * Gets debugger, if any.
     *
     * @return null or workflow debugger
     */
    public WorkflowDebugger getDebugger();

    /**
     * Gets a snapshot of the preview information list.  Processes the debugging
     * information list.
     * <p>
     * Called during or after workflow execution.
     *
     * @return list of preview information.
     */
    public List<PreviewInfo> getPreviewInfoList();

    /**
     * Gets a snapshot of the preview information list for a given instance.
     * <p>
     * Called during or after workflow execution.  Can be called repeatedly as
     * workflow progresses.
     *
     * @param instanceId identifies the instance
     * @return list of preview information
     */
    public List<PreviewInfo> getPreviewInfoList(String instanceId);
}
