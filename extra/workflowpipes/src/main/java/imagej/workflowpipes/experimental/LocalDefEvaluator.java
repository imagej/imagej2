//
// LocalDefEvaluator.java
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
