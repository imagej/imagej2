package pipesentity;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;


public class Module {
	
	private Terminal[] terminals;
	private Type type;
	private UI ui;
	private Name name;
	private Description description;
	private Tag[] tags;
	private Conf[] conf;
	
	public Conf[] getConfArray() {
		return conf;
	}

	public void setConfArray( Conf[] conf ) {
		this.conf = conf;
	}

	public Module( Terminal[] terminals, UI ui, Name name, Type type, Description description, Tag[] tags )
	{
		this.terminals = terminals;
		this.type = type;
		this.ui = ui;
		this.name = name;
		this.description = description;
		this.tags = tags;
	}

	public Module( JSONObject jsonObject ) {
		System.out.println( "Module constructor " + jsonObject.toString() );
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
	
	/**
	 * Performs a string literal match on the types
	 * @param type
	 * @return
	 */
	public boolean matchesType( String type )
	{
		if ( this.type.getValue().equals( type ) )
		{
			return true;
		}
		return false;
	}
	
	public boolean searchNameAndDescription( String searchTerm )
	{
		if ( this.name.getValue().contains( searchTerm ) //or
				|| this.description.getValue().contains(searchTerm))
		{
			return true;
		}
		return false;
	}

	/**
	 * Returns the Result representation of the module
	 * @return
	 */
	public Result getResult() 
	{
		//create a Result from the module
		Result result = new Result( this.name, this.description, this.type, new Source("pipe") );
		
		//return the result
		return result;
	}

	/**
	 * Given a JSON array of modules...
	 * @param modulesArray
	 * @return an Array of type Module
	 */
	public static ArrayList<Module> getArrayFromJSONArray( JSONArray modulesArray ) 
	{
		ArrayList<Module> moduleList = new ArrayList<Module>();
		
		for(int i = 0; i < modulesArray.length(); i++ )
		{
			moduleList.add( new Module( modulesArray.getJSONObject(i) ) );
		}
		
		return moduleList;
	}
	

}
