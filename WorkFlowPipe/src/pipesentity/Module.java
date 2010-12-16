package pipesentity;

import org.json.JSONArray;
import org.json.JSONObject;


public class Module {
	
	private Terminal[] terminals;
	private Type type;
	private UI ui;
	private Name name;
	private Description description;
	private Tag[] tags;
	
	
	public Module( Terminal[] terminals, UI ui, Name name, Type type, Description description, Tag[] tags )
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
	
	public JSONObject getJSONObject() {
		
		JSONObject jsonModule = new JSONObject();
		JSONArray jsonArrayTerminals = new JSONArray();
		
		// add the terminals
		for( Terminal terminal : getTerminals() )
		{
			JSONObject jsonObjectTerminals = new JSONObject();
			jsonObjectTerminals.put( terminal.getTypeKey(), terminal.getTypeValue() ); 
			jsonObjectTerminals.put( terminal.getDirectionKey(), terminal.getDirectionKeyValue() );
			jsonArrayTerminals.put( jsonObjectTerminals );
		}

		// add terminals array to module
		jsonModule.put("terminals",jsonArrayTerminals);

		// add ui key value to the object
		jsonModule.put( "ui", getUIValue() );

		// add name to module
		jsonModule.put( "name", getNameValue() );
	
		// add type to module
		jsonModule.put( "type", getTypeValue() );

		// add description to module
		jsonModule.put( "description", getDescriptionValue() );
	
		// create an array for the tags
		JSONArray jsonArrayTags = new JSONArray();
		
		// add tags to array
		for( Tag tag: getTags() )
			jsonArrayTags.put( tag.getValue() );
		
		//add tags array to module
		jsonModule.put( "tags", jsonArrayTags );
		
		return jsonModule;
	}
	
	public boolean matchesType( String type )
	{
		if ( this.type.getValue().equals( type ) )
		{
			return true;
		}
		return false;
	}
	

}
