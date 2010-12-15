package pipes;

import pipesentity.Description;
import pipesentity.Name;
import pipesentity.Tag;
import pipesentity.Terminal;
import pipesentity.Type;
import pipesentity.UI;

public class PipesModule {
	
	private Terminal[] terminals;
	private Type type;
	private UI ui;
	private Name name;
	private Description description;
	private Tag[] tags;
	
	
	public PipesModule( Terminal[] terminals, UI ui, Name name, Type type, Description description, Tag[] tags )
	{
		this.terminals = terminals;
		this.type = type;
		this.ui = ui;
		this.name = name;
		this.description = description;
		this.tags = tags;
	}

	public Terminal[] getTerminals() {
		return terminals;
	}

	public Object getUIValue() {
		return ui.getValue();
	}

	public Object getDescriptionValue() {
		return description.getValue();
	}

	public String getTypeValue() {
		return type.getValue();
	}

	public String getNameValue() {
		return name.getValue();
	}

	public Tag[] getTags() {
		return tags;
	}
	
	

}
