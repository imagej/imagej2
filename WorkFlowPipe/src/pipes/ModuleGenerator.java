package pipes;

import java.util.ArrayList;

import pipesentity.Description;
import pipesentity.Name;
import pipesentity.Module;
import pipesentity.Tag;
import pipesentity.Terminal;
import pipesentity.TerminalConnectorType;
import pipesentity.Type;
import pipesentity.UI;

public class ModuleGenerator {

	public ModuleGenerator(){
		
	}

	public static Module getSampleModule() {

		Terminal[] terminals = Terminal.getInOutTerminal( 
				TerminalConnectorType.inputType.valueOf("number"), 
				TerminalConnectorType.outputType.valueOf("number") );
		
		UI ui = new UI( " <div class=\"horizontal\" label=\"URL\" repeat=\"true\"> <input name=\"URL\" required=\"true\" type=\"url\"/> </div> " );
		
		Name name = new Name( "Fetch Site Feed" );
		
		Type type = new Type( "fetchsitefeed" );
		
		Description description = new Description( "Find feed URLs embedded in a webpage using auto-discovery links and fetch the first one " );
		
		Tag tag = new Tag( "system:sources" );
		Tag[] tags = Tag.getTagsArray( tag );
		
		return new Module( terminals, ui, name, type, description, tags );
	}

	/**
	 * Returns a sample collection representative of the inputs and features of a ModuleGenerator
	 * @return
	 */
	public static ArrayList<Module> getPipeModuleSampleCollection() {
		
		//Create a default collection
		ArrayList<Module> pipeModuleSampleCollection = new ArrayList<Module>();
		
		//Create a Module
		pipeModuleSampleCollection.add( getSampleModule() );
		pipeModuleSampleCollection.add( getOutputModule() );
		
		return pipeModuleSampleCollection;
	}

	/**
	 * returns a fixed output module
	 * @return
	 */
	public static Module getOutputModule() {
	
		Terminal[] terminals = Terminal.getInputTerminal( 
				TerminalConnectorType.inputType.valueOf("rss")  );
		
		UI ui = new UI( "" );
		
		Name name = new Name( "Pipe Output" );
		
		Type type = new Type( "output" );
		
		Description description = new Description( "The pipe output needs to be fed to this module" );
		
		Tag tag = new Tag( null );
		Tag[] tags = Tag.getTagsArray( tag );
		
		return new Module( terminals, ui, name, type, description, tags );
	}
}
