package modules;

import java.io.Serializable;
import java.util.ArrayList;

import pipesapi.Module;
import pipesentity.Description;
import pipesentity.ID;
import pipesentity.Name;
import pipesentity.Tag;
import pipesentity.Terminal;
import pipesentity.Type;
import pipesentity.UI;
import pipesentity.Error;

/**
 * Represents the module type "OUTPUT"
 * @author rick
 *
 */
public class Output extends Module implements Serializable {

	private static final long serialVersionUID = -7994336091379467057L;

	public static Output getOutput() {
		
		Output output = new Output();
		
		output.id = new ID("_OUTPUT");
		
		output.terminals.add( Terminal.getInputTerminal("rss") );

		output.ui = new UI("");

		output.name = new Name("Pipe Output");

		output.type = new Type("output");

		output.description = new Description("The pipe output needs to be fed to this module");

		Tag tag = new Tag(null);
		output.tags = Tag.getTagsArray(tag);
		
		return output;
	}


	@Override
	public void go() {
		
		//process input
		
		
	}

}
