package imagej.pipes;

import java.util.HashMap;

import imagej.pipesapi.Module;

public class ModuleSearch {

	/**
	 * Helper method to search for a pipe by type
	 * 
	 * @param type - String type to search for
	 * @param pipesModulesArrayList - Collection of PipeModules
	 * @return the first string match on type field
	 */
	public static Module findfirstModuleOfType( String type, HashMap<Service, Module> modulesServiceHashMap )
	{
		for( Module pipesModule : modulesServiceHashMap.values() )
		{
			if( pipesModule.matchesType( type ) )
			{
				return pipesModule;
			}
		}
		
		return null;
	}
}
