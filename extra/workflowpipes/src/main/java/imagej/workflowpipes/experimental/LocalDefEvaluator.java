package imagej.workflowpipes.experimental;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import imagej.workflow.Workflow;
import imagej.workflow.debug.PreviewInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import imagej.workflowpipes.pipes.ModuleGenerator;
import imagej.workflowpipes.pipes.Service;
import imagej.workflowpipes.pipesapi.Module;
import imagej.workflowpipes.pipesentity.Def;
import imagej.workflowpipes.pipesentity.Preview;
import imagej.workflowpipes.util.LayoutToWorkFlow;
public class LocalDefEvaluator {

	static final boolean DEBUG = true;
	
	public static JSONObject getPreview( Def def, HashMap<Service, Module> moduleHashMap )
	{	
		if (DEBUG) System.out.println(" LocalDefEvaluator :: getPreview ... starting array list generation from def ");
		
		JSONArray modulesJSONArray = def.getModulesArray();
			
		ArrayList<Module> moduleList = new ArrayList<Module>();

		for (int i = 0; i < modulesJSONArray.length(); i++) 
		{
			//get the JSONObject
			JSONObject jsonInternal = modulesJSONArray.getJSONObject(i);
			
			String moduleType = jsonInternal.getString("type");
			
			// get the module
			Module moduleCopy = ModuleGenerator.getModule( moduleType, moduleHashMap );
			
			// System.out.println( "LocalDefEvaluator :: getPreview :: Getting the instance for type : " + moduleType );

			moduleCopy.assignInstanceValues( jsonInternal );
			
			//get the service
			moduleList.add( moduleCopy );
		}
		
		// TODO: extend checking and ordering logic
		
		// Create a loci workflow from the module instances and wires
		
		
		// Create the workflow from the pipes connection
		Workflow workflow = LayoutToWorkFlow.getWorkFlow( def.getWires(), moduleList );
                //TODO extremely hokey; let the workflow finish
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {
                    
                }
		
		//Create a preview response
		Preview previewResponse = new Preview();

		// evaluate the modules output individually
		for( Module module : moduleList )
		{
			System.out.println( "LocalDefEvaluator :: getPreview " + module.getType().getValue()  );	
				
			// module build preview
                        List<PreviewInfo> previewInfoList = workflow.getPreviewInfoList(module.getID().getValue());
                            for (PreviewInfo previewInfo : previewInfoList) {
                                System.out.println( "Description: " + previewInfo.getDesc() );
                                System.out.println( "HTML UI: " + previewInfo.getContent() );
                                System.out.println( "Unique instance id: " + previewInfo.getInstanceId() );
                        }
			module.go( previewInfoList );
			
			// add the errors
			previewResponse.addErrors( module.getID(), module.getErrors() );
			
			// add the preview
			previewResponse.addPreview( module.getID(), module.getPreviewJSON() );
			
			// add the stats
			previewResponse.addStats( module.getResponse().getTitle(), module.getResponse().getJSON() );
		}
		
		return  previewResponse.getJSON(); 
		
		//return new JSONObject();
	}

}
