package pipes;

import java.util.ArrayList;

import modules.OUTPUT;
import modules.FetchPage;
import pipesentity.Description;
import pipesentity.ID;
import pipesentity.Module;
import pipesentity.Name;
import pipesentity.Tag;
import pipesentity.Terminal;
import pipesentity.TerminalConnectorType;
import pipesentity.Type;
import pipesentity.UI;

public class ModuleGenerator {


	public static Module getSampleModule() 
	{
		ID id = new ID("sitefeed");
		
		ArrayList<Terminal> terminals = Terminal.getInOutTerminal( TerminalConnectorType.inputType.valueOf("number"), TerminalConnectorType.outputType.valueOf("number") );

		UI ui = new UI("<div class=\"horizontal\" label=\"URL\" repeat=\"true\"> <input name=\"URL\" required=\"true\" type=\"url\"/> </div>");

		Name name = new Name("Fetch Site Feed");

		Type type = new Type("fetchsitefeed");

		Description description = new Description("Find feed URLs embedded in a webpage using auto-discovery links and fetch the first one ");

		Tag tag = new Tag("system:sources");
		ArrayList<Tag> tags = Tag.getTagsArray(tag);

		return new Module( id, terminals, ui, name, type, description, tags);
	}
	
	public static ArrayList<Module> getModulesFromInternalPackage()
	{
		// Create a default collection
		ArrayList<Module> pipeModuleInternal = new ArrayList<Module>();
		
		//add the fetch page module
		//pipeModuleInternal.add( FetchPage. );
		
		//add the output module
		pipeModuleInternal.add( OUTPUT.getOUTPUT() );
		
		// return the results
		return pipeModuleInternal;
	}
	

	/**
	 * Returns a list of the internal modules
	 * 
	 * @return
	 */
	public static ArrayList<Module> getPipeModulesInternal() {

		// Create a default collection
		ArrayList<Module> pipeModuleSampleCollection = new ArrayList<Module>();

		// Create a Module
		pipeModuleSampleCollection.add( new FetchPage().getModule() );
		pipeModuleSampleCollection.add( OUTPUT.getOUTPUT() );

		return pipeModuleSampleCollection;
	}

}
