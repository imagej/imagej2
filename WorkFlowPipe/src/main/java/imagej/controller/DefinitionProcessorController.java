package imagej.controller;

import imagej.pipesentity.Definition;

/**
 * Provides the functionality of processing various code
 * @author ipatron
 *
 */
public class DefinitionProcessorController {
	
	private Definition definition;
	
	public DefinitionProcessorController( Definition definition )
	{
		this.definition = definition;
	}
	
	/**
	 * returns true if the definition executes successfully
	 * @return
	 */
	public boolean execute( DefinitionProcessor definitionProcessor, Object results )
	{
		try
		{
			//try to execute the definition
			results = definitionProcessor.execute( this.definition );
		} catch (Exception e)
		{
			//TODO:Implement Logging
			return false;
		}
		
		return true;
	}
	

}
