package modules;

import loci.workflow.IModuleInfo;
import pipesapi.Module;
import pipesentity.Description;
import pipesentity.ID;
import pipesentity.Name;
import pipesentity.Tag;
import pipesentity.Terminal;
import pipesentity.Type;
import pipesentity.UI;

public class ModuleBase extends Module {

	public ModuleBase( IModuleInfo iModuleInfo )
	{
		// populate ID
		this.id = new ID("");

		// loop through inputs and add terminals
		for ( String inputName : iModuleInfo.getInputNames() )
		{
			this.terminals.add( Terminal.getInputTerminal( "items", inputName ) );
		}
		
		// loop through output and add terminals
		for ( String outputName : iModuleInfo.getOutputNames() )
		{
			this.terminals.add( Terminal.getOutputTerminal( "items", outputName ) );
		}
		
		// TODO replace with UI Editor / Static
		this.ui = new UI("\n\t\t<div class=\"horizontal\">\n\t\t\t<label>URL: </label><input name=\"URL\" type=\"url\" required=\"true\"/>\n\t\t</div> \n\t\t<div class=\"horizontal\">\n\t\t\t<label>Cut content from: </label><input name=\"from\" type=\"text\" required=\"true\"/>\n            <label>to: </label><input name=\"to\" type=\"text\" required=\"true\"/>\n        </div>\n        <div class=\"horizontal\">\n            <label>Split using delimiter: </label><input name=\"token\" type=\"text\" required=\"true\"/>\n            \n\t\t</div> \n\t\t");

		//
		this.name = new Name( iModuleInfo.getName() );

		// TODO replace with single string representing the GUI type
		this.type = new Type( iModuleInfo.getName() );

		this.description = new Description("TODO map me with descriptive text");

		Tag tag = new Tag("system:img");

		this.tags = Tag.getTagsArray(tag);
		
		//TODO this is to be replaced with the implementation
		this.module = "Yahoo::RSS::FetchPage";
	}


}
