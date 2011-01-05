package modules;

import java.util.ArrayList;

import org.json.JSONObject;

import modulesapi.IModule;

import pipesapi.Module;
import pipesentity.Conf;
import pipesentity.Description;
import pipesentity.ID;
import pipesentity.Name;
import pipesentity.Tag;
import pipesentity.Terminal;
import pipesentity.TerminalConnectorType;
import pipesentity.Type;
import pipesentity.UI;

public class OUTPUT extends Module implements IModule {
	
	public OUTPUT( JSONObject jsonObject ) {
		
		super( jsonObject );
		
		id = new ID("_OUTPUT");
		
		terminals = Terminal.getInputTerminal(TerminalConnectorType.inputType.valueOf("rss"));

		ui = new UI("");

		name = new Name("Pipe Output");

		type = new Type("output");

		description = new Description("The pipe output needs to be fed to this module");

		Tag tag = new Tag(null);
		tags = Tag.getTagsArray(tag);
	}

	@Override
	public Module getModule() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void go(ArrayList<Conf> confs) {
		// TODO Auto-generated method stub
		
	}

}
