/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.workflow;

/**
 *
 * @author Aivar Grislis
 */
public class WorkflowFactory implements IModuleFactory {
    private static WorkflowFactory s_instance = null;

    private WorkflowFactory() {
    }

    public static synchronized WorkflowFactory getInstance() {
        if (null == s_instance) {
            s_instance = new WorkflowFactory();
        }
        return s_instance;
    }

    /**
     * Creates a workflow from XML.
     *
     * @param xml string containing XML representation
     * @return
     */
    public IWorkflow create(String xml) {
        return create(xml, null);
   }

    /**
     * Creates a workflow from XML.
     *
     * @param xml String containing XML representation
     * @param instanceId null or unique instance identifier
     * @return
     */
    public IWorkflow create(String xml, String instanceId) {
        IWorkflow workFlow = new Workflow(instanceId);
        workFlow.fromXML(xml);
        return workFlow;
    }

}
