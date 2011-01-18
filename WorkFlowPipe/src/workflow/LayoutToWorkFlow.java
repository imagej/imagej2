package workflow;

import java.util.ArrayList;

import pipesapi.Module;
import pipesentity.Wire;
import loci.workflow.WorkFlow;
import loci.workflow.WorkflowManager;

/**
 * Static methods to allow transforms from Layouts to WorkFlows
 * @author rick
 *
 */
public class LayoutToWorkFlow {

	public static WorkFlow getWorkFlow( ArrayList<Wire> arrayList, ArrayList<Module> moduleList )
	{
		// Get an instance of the WorkFlow Manager
		WorkflowManager workflowManager = WorkflowManager.getInstance();
		
		// Add the modules first
		//workflowManager.addWorkflow(workflow)
		
		
		
		return new WorkFlow();
	}
}
