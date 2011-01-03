package pipesentity;

import java.text.ParseException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

/*
 * Match the JSON entity 'def'
 */
public class Def {
	
	private ArrayList<Module> modules;
	private ArrayList<Wire> wires;
	
	public Def( ArrayList<Module> modules,  ArrayList<Wire> wires )
	{
		this.modules = modules;
		this.wires = wires;
	}
	
	/*
	 * Given a JSONObject def, create the Java representation
	 */
	public Def ( JSONObject inputJSON )
	{
		//get the modules
		JSONArray modulesArray = inputJSON.getJSONArray("modules");
		
		//get Modules from JSON Array
		this.modules = Module.getArrayFromJSONArray( modulesArray );
		
		//get the wires
		JSONArray wiresArray = inputJSON.getJSONArray("wires");
		
		// get Wires from JSON Array
		this.wires = Wire.getArrayFromJSONArray( wiresArray );
	}

	public Def( String definition ) throws ParseException 
	{
		JSONObject jsonArray = new JSONObject( definition );
		new Def( jsonArray );
		System.out.println("Def constuctor " + definition );
	}

	
}
