package modules;

import java.util.ArrayList;

import modulesapi.JavaModule;
import modulesapi.ModuleBase;

import org.json.JSONObject;

import pipesentity.Conf;
import pipesentity.Description;
import pipesentity.ID;
import pipesentity.Module;
import pipesentity.Name;
import pipesentity.Preview;
import pipesentity.Tag;
import pipesentity.Terminal;
import pipesentity.TerminalConnectorType;
import pipesentity.Type;
import pipesentity.UI;

/**
 * Represents the SW9 module settings
 * 
 * @author rick
 * 
 */
public class FetchPage extends ModuleBase implements JavaModule{

	@Override
	public Module getModule() {
		
		id = new ID("");

		terminals = Terminal.getOutTerminal(TerminalConnectorType.outputType.valueOf("rss"));

		ui = new UI("\n\t\t<div class=\"horizontal\">\n\t\t\t<label>URL: </label><input name=\"URL\" type=\"url\" required=\"true\"/>\n\t\t</div> \n\t\t<div class=\"horizontal\">\n\t\t\t<label>Cut content from: </label><input name=\"from\" type=\"text\" required=\"true\"/>\n            <label>to: </label><input name=\"to\" type=\"text\" required=\"true\"/>\n        </div>\n        <div class=\"horizontal\">\n            <label>Split using delimiter: </label><input name=\"token\" type=\"text\" required=\"true\"/>\n            \n\t\t</div> \n\t\t");

		name = new Name("Fetch Page");

		type = new Type("fetchpage");

		description = new Description("Fetch HTML or XHTML documents and emit as a string");

		Tag tag = new Tag("system:sources");

		tags = Tag.getTagsArray(tag);

		return new Module(id, terminals, ui, name, type, description, tags);

	}

	@Override
	public Preview getPreview( Conf[] confs ) 
	{	
		this.confs = confs;
	
		System.out.println("sw-9 conf is " );

		// TODO: add the smarts here
		return new Preview();
	}
}
