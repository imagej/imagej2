package pipesentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

public class Module {

	private ArrayList<Terminal> terminals = null;
	private Type type = null;
	private UI ui = null;
	private Name name = null;
	private Description description = null;
	private ArrayList<Tag> tags = null;
	private ArrayList<Conf> conf = null;
	private ID id = null;

	public ArrayList<Conf> getConfArray() {
		return conf;
	}

	public void setConfArray(ArrayList<Conf> conf) {
		this.conf = conf;
	}

	public Module(ID id, ArrayList<Terminal> terminals, UI ui, Name name, Type type, Description description, ArrayList<Tag> tags) {
		this.id = id;
		this.terminals = terminals;
		this.type = type;
		this.ui = ui;
		this.name = name;
		this.description = description;
		this.tags = tags;
	}

	public Module(JSONObject jsonObject) 
	{	
		// if id is defined
		if (jsonObject.getString("id") != null) {
			this.id = new ID( jsonObject.getString("id") );
		}

		// if type is defined
		if (jsonObject.getString("type") != null) {
			this.type = new Type(jsonObject.getString("type"));
		}
		
		// if conf is defined
		if (jsonObject.getString("conf") != null) {
			this.conf = Conf.getConfs(jsonObject.getString("conf"));
		}
	}

	public ArrayList<Terminal> getTerminals() {
		return terminals;
	}

	public UI getUI() {
		return ui;
	}

	public Description getDescription() {
		return description;
	}

	public Type getType() {
		return type;
	}

	public Name getName() {
		return name;
	}

	public ArrayList<Tag> getTags() {
		return tags;
	}

	public JSONObject getJSONObject() {

		JSONObject jsonModule = new JSONObject();
		JSONArray jsonArrayTerminals = new JSONArray();

		if (getID() != null) {
			// add ui key value to the object
			jsonModule.put( "ui", getID().getValue() );
		}
		
		if (getTerminals() != null) {
			// add the terminals
			for (Terminal terminal : getTerminals()) {
				JSONObject jsonObjectTerminals = new JSONObject();
				jsonObjectTerminals.put(terminal.getTypeKey(),
						terminal.getTypeValue());
				jsonObjectTerminals.put(terminal.getDirectionKey(),
						terminal.getDirectionKeyValue());
				jsonArrayTerminals.put(jsonObjectTerminals);
			}

			// add terminals array to module
			jsonModule.put("terminals", jsonArrayTerminals);
		}

		if (getUI() != null) {
			// add ui key value to the object
			jsonModule.put( "ui", getUI().getValue() );
		}

		if (getName() != null) {
			// add name to module
			jsonModule.put("name", getName().getValue() );
		}

		if (getType() != null) {
			// add type to module
			jsonModule.put("type", getType().getValue() );
		}

		if (getDescription() != null) {
			// add description to module
			jsonModule.put("description", getDescription().getValue() );
		}

		if (getTags() != null) {
			// create an array for the tags
			JSONArray jsonArrayTags = new JSONArray();

			// add tags to array
			for (Tag tag : getTags())
				jsonArrayTags.put(tag.getValue());

			// add tags array to module
			jsonModule.put("tags", jsonArrayTags);
		}

		return jsonModule;
	}

	private ID getID() {
		return this.id;
	}

	/**
	 * Performs a string literal match on the types
	 * 
	 * @param type
	 * @return
	 */
	public boolean matchesType(String type) {
		if (this.type.getValue().equals(type)) {
			return true;
		}
		return false;
	}

	public boolean searchNameAndDescription(String searchTerm) {
		if (this.name.getValue().contains(searchTerm) // or
				|| this.description.getValue().contains(searchTerm)) {
			return true;
		}
		return false;
	}

	/**
	 * Returns the Result representation of the module
	 * 
	 * @return
	 */
	public Result getResult() {
		// create a Result from the module
		Result result = new Result(this.name, this.description, this.type,
				new Source("pipe"));

		// return the result
		return result;
	}

	/**
	 * Given a JSON array of modules...
	 * 
	 * @param modulesArray
	 * @return an Array of type Module
	 */
	public static ArrayList<Module> getArrayFromJSONArray(JSONArray modulesArray) {
		ArrayList<Module> moduleList = new ArrayList<Module>();

		for (int i = 0; i < modulesArray.length(); i++) {
			moduleList.add(new Module(modulesArray.getJSONObject(i)));
		}

		return moduleList;
	}

	/**
	 * 
	 * @param modulesArrayList
	 * @returns the key value as Moduleid, Module for ease of searching
	 */
	public static HashMap<String, Module> getHashMap( ArrayList<Module> modulesArrayList ) 
	{
		HashMap< String, Module > modulesHashMap = new HashMap<String,Module>();
		
		// use an Iterator to cycle over the modules
		Iterator modulesIterator = modulesArrayList.iterator();
		
		// while there are modules
		while( modulesIterator.hasNext())
		{
			// get the module
			Module module = (Module) modulesIterator.next();
			
			//add the module to the HashMap
			modulesHashMap.put( module.getID().getValue(), module );
		}
		
		//return the resulting list
		return modulesHashMap;
	}
	
	/**
	 * Returns the Modules as an ArrayList
	 * @param moduleHashMap
	 * @return
	 */
	public static ArrayList<Module> getModulesArrayList( HashMap<String, Module> moduleHashMap) {
		ArrayList<Module> moduleArrayList = new ArrayList<Module>();
		
		for( Module module :moduleHashMap.values() )
		{
			//add the module
			moduleArrayList.add( module );
		}
		
		return moduleArrayList;
	}

	/**
	 * Given a list of input Modules, return the first matching moduleid
	 * @param moduleid
	 * @param modules
	 * @return
	 */
	public static Module getModuleByID( String moduleid, ArrayList<Module> modules ) 
	{
		//get an Iterator
		Iterator<Module> iterator = modules.iterator();
		
		//find the match
		while( iterator.hasNext() )
		{
			//get the next iterator
			Module module = iterator.next();
			
			//if ids match
			if( module.id.getValue() == moduleid )
				return module;
		}
		
		return null;
	}

}
