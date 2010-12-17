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

	public ModuleGenerator() {

	}

	public static Module getFetchPageModule() {

		Terminal[] terminals = Terminal
				.getOutTerminal( TerminalConnectorType.outputType.valueOf("rss") );

		UI ui = new UI(
				"\n\t\t<div class=\"horizontal\">\n\t\t\t<label>URL: </label><input name=\"URL\" type=\"url\" required=\"true\"/>\n\t\t</div> \n\t\t<div class=\"horizontal\">\n\t\t\t<label>Cut content from: </label><input name=\"from\" type=\"text\" required=\"true\"/>\n            <label>to: </label><input name=\"to\" type=\"text\" required=\"true\"/>\n        </div>\n        <div class=\"horizontal\">\n            <label>Split using delimiter: </label><input name=\"token\" type=\"text\" required=\"true\"/>\n            \n\t\t</div> \n\t\t");

		Name name = new Name("Fetch Page");

		Type type = new Type("fetchpage");

		Description description = new Description(
				"Fetch HTML or XHTML documents and emit as a string");

		Tag tag = new Tag("system:sources");
		Tag[] tags = Tag.getTagsArray(tag);

		return new Module(terminals, ui, name, type, description, tags);

	}

	public static Module getSampleModule() {

		Terminal[] terminals = Terminal.getInOutTerminal(
				TerminalConnectorType.inputType.valueOf("number"),
				TerminalConnectorType.outputType.valueOf("number"));

		UI ui = new UI(
				" <div class=\"horizontal\" label=\"URL\" repeat=\"true\"> <input name=\"URL\" required=\"true\" type=\"url\"/> </div> ");

		Name name = new Name("Fetch Site Feed");

		Type type = new Type("fetchsitefeed");

		Description description = new Description(
				"Find feed URLs embedded in a webpage using auto-discovery links and fetch the first one ");

		Tag tag = new Tag("system:sources");
		Tag[] tags = Tag.getTagsArray(tag);

		return new Module(terminals, ui, name, type, description, tags);
	}

	/**
	 * Returns a sample collection representative of the inputs and features of
	 * a ModuleGenerator
	 * 
	 * @return
	 */
	public static ArrayList<Module> getPipeModuleSampleCollection() {

		// Create a default collection
		ArrayList<Module> pipeModuleSampleCollection = new ArrayList<Module>();

		// Create a Module
		pipeModuleSampleCollection.add(getSampleModule());
		pipeModuleSampleCollection.add(getFetchPageModule());
		pipeModuleSampleCollection.add(getOutputModule());

		return pipeModuleSampleCollection;
	}

	/**
	 * returns a fixed output module
	 * 
	 * @return
	 */
	public static Module getOutputModule() {

		Terminal[] terminals = Terminal
				.getInputTerminal(TerminalConnectorType.inputType
						.valueOf("rss"));

		UI ui = new UI("");

		Name name = new Name("Pipe Output");

		Type type = new Type("output");

		Description description = new Description(
				"The pipe output needs to be fed to this module");

		Tag tag = new Tag(null);
		Tag[] tags = Tag.getTagsArray(tag);

		return new Module(terminals, ui, name, type, description, tags);
	}
	
}
