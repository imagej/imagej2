package pipes;

import java.util.ArrayList;

import pipesentity.Terminal;
import pipesentity.TerminalConnectorType;

public class ModuleGenerator {

	public ModuleGenerator(){
		
	}

	public static PipesModule getSampleModule() {
		//TODO:finish
		/*
		Terminal[] terminals = Terminal.getInOutTerminal( TerminalConnectorType.inputType.valueOf("number"), 
				TerminalConnectorType.inputType.valueOf("number") );
		PipesModule pipesModule = new PipesModule( 
				
				//Get in inputOutput terminal using helper
				 ),
	}, 
				ui, name, type, description, tags) */
		
		return new PipesModule(null, null, null, null, null, null);
	}

	/**
	 * Returns a sample collection representative of the inputs and features of a ModuleGenerator
	 * @return
	 */
	public static ArrayList<PipesModule> getPipeModuleSampleCollection() {
		
		//Create a default collection
		ArrayList<PipesModule> pipeModuleSampleCollection = new ArrayList<PipesModule>();
		
		//Create a Module
		pipeModuleSampleCollection.add( getSampleModule() ) ;
		
		return pipeModuleSampleCollection;
	}
}
