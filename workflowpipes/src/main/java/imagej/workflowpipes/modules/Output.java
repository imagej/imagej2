package imagej.workflowpipes.modules;

import java.io.Serializable;
import java.util.ArrayList;

import imagej.workflowpipes.pipesapi.Module;
import imagej.workflowpipes.pipesentity.Description;
import imagej.workflowpipes.pipesentity.Error;
import imagej.workflowpipes.pipesentity.ID;
import imagej.workflowpipes.pipesentity.Name;
import imagej.workflowpipes.pipesentity.Tag;
import imagej.workflowpipes.pipesentity.Terminal;
import imagej.workflowpipes.pipesentity.Type;
import imagej.workflowpipes.pipesentity.UI;

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
