package imagej.workflowpipes.pipes;

import java.util.HashMap;

import imagej.workflow.IModuleInfo;
import imagej.workflow.WorkflowManager;
import imagej.workflowpipes.modules.DisplayImage;
import imagej.workflowpipes.modules.FetchPage;
import imagej.workflowpipes.modules.ModuleBase;
import imagej.workflowpipes.modules.Output;
import imagej.workflowpipes.pipesapi.Module;
import imagej.workflowpipes.pipesentity.Type;
import imagej.workflowpipes.util.DeepCopy;

public class ModuleGenerator {
	
	private static final boolean DEBUG = true;

	/**
	 * Returns a list of the internal modules
	 * 
	 * @return
	 */
	public static HashMap<Service, Module> getInternalModules() {

		// Create a module hashmap
		HashMap< Service, Module > moduleServiceHashMap = new HashMap<Service,Module>();

		//add the fetch page module
		moduleServiceHashMap.put( new Service( new Type("fetchpage") ), FetchPage.getFetchPage() );

		//add the output module
		moduleServiceHashMap.put( new Service( new Type("output") ), Output.getOutput() );

		//add the output module
		moduleServiceHashMap.put( new Service( new Type("displayimage") ), DisplayImage.getDisplayImage() );

		return moduleServiceHashMap;
	}
	
	public static HashMap<Service, Module> getInternalModules2() {
		
		// Create a module Hashmap
		HashMap< Service, Module > moduleServiceHashMap = new HashMap<Service,Module>();

		// Get the workflow manager
		WorkflowManager workflowManager = WorkflowManager.getInstance();
		
		// Iterate over the discovered modules
		for ( IModuleInfo iModuleInfo : workflowManager.getModuleInfos() ) 
		{
			// Create a module from the iModuleInfo ...
			Module module = new ModuleBase( iModuleInfo );

			if (DEBUG) System.out.println("Found module name " + module.getType().getValue() );
			
			// add the module
			moduleServiceHashMap.put( new Service( module.getType() ), module );
		}

               

		//add the output module
		//moduleServiceHashMap.put( new Service( new Type("output") ), Output.getOutput() );

		return moduleServiceHashMap;
	}
	
	//TODO: replace with lookup api
	public static Module getModule( String type )
	{
		if ( type.equalsIgnoreCase("fetchpage") )
			return FetchPage.getFetchPage();
		
		if( type.equalsIgnoreCase("displayimage"))
			return DisplayImage.getDisplayImage();
		
		return Output.getOutput();
	}

	public static Module getModule(String moduleType, HashMap< Service, Module > moduleHashMap) {
		// iterate through the modules
		for (Module module : moduleHashMap.values() )
		{
			// TODO remove namespace collision potential
			// TODO implement deep copy() to allow multiple instances
			if ( module.getType().getValue().equals( moduleType ) )
			{
				// return a deep copy of this module
				return (Module) DeepCopy.copy( module );
			}
		}
		//TODO add error handling
		return null;
	}

}
