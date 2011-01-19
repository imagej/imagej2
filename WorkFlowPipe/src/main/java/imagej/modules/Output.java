package imagej.modules;

import java.io.Serializable;
import java.util.ArrayList;

import imagej.pipesapi.Module;
import imagej.pipesentity.Description;
import imagej.pipesentity.ID;
import imagej.pipesentity.Name;
import imagej.pipesentity.Tag;
import imagej.pipesentity.Terminal;
import imagej.pipesentity.Type;
import imagej.pipesentity.UI;
import imagej.pipesentity.Error;

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
		
		output.terminals.add( Terminal.getInputTerminal("rss","_INPUT") );

		output.ui = new UI("");

		output.name = new Name("Pipe Output");

		output.type = new Type("output");

		output.description = new Description("The pipe output needs to be fed to this module");

		Tag tag = new Tag(null);
		output.tags = Tag.getTagsArray(tag);
		
		return output;
	}


	public void go() {
		
		//process input
		
		
	}

}
