package pipes;

import java.util.ArrayList;

import pipesentity.Module;

public class ModuleSearch {

	/**
	 * Helper method to search for a pipe by type
	 * 
	 * @param type - String type to search for
	 * @param pipesModulesArrayList - Collection of PipeModules
	 * @return the first string match on type field
	 */
	public static Module findfirstModuleOfType( String type, ArrayList<Module> pipesModulesArrayList )
	{
		for( Module pipesModule : pipesModulesArrayList )
		{
			if( pipesModule.matchesType( type ) )
			{
				return pipesModule;
			}
		}
		
		return null;
	}
}
