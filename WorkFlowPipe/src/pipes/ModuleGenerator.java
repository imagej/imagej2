package pipes;

import java.util.HashMap;

import modules.FetchPage;
import pipesapi.Module;
import pipesentity.Type;

public class ModuleGenerator {
		

	/**
	 * Returns a list of the internal modules
	 * 
	 * @return
	 */
	public static HashMap<Service, Module> getInternalModules() {

		// Create a default collection
		HashMap< Service, Module > moduleServiceHashMap = new HashMap<Service,Module>();

		//add the fetch page module
		moduleServiceHashMap.put( new Service( new Type("fetchpage") ), new FetchPage( null ) );

		return moduleServiceHashMap;
	}

}
