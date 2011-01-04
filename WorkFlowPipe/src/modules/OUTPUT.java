package modules;

import java.util.ArrayList;

import pipesentity.Description;
import pipesentity.ID;
import pipesentity.Module;
import pipesentity.Name;
import pipesentity.Tag;
import pipesentity.Terminal;
import pipesentity.TerminalConnectorType;
import pipesentity.Type;
import pipesentity.UI;

public class OUTPUT {

	public static Module getOUTPUT() {
		ID id = new ID("_OUTPUT");
		
		ArrayList<Terminal> terminals = Terminal.getInputTerminal(TerminalConnectorType.inputType.valueOf("rss"));

		UI ui = new UI("");

		Name name = new Name("Pipe Output");

		Type type = new Type("output");

		Description description = new Description("The pipe output needs to be fed to this module");

		Tag tag = new Tag(null);
		ArrayList<Tag> tags = Tag.getTagsArray(tag);

		return new Module( id, terminals, ui, name, type, description, tags);
	}

}
