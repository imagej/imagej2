/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.workflow;

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
     * @param xml
     * @return
     */
    public IWorkflow create(String xml) {
        IWorkflow workFlow = new Workflow();
        workFlow.fromXML(xml);
        return workFlow;
    }

}
