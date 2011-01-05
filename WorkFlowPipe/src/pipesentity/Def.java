package pipesentity;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

/*
 * Match the JSON entity 'def'
 */
public class Def 
{
	
	private JSONArray modulesJSONArray;
	private ArrayList<Wire> wires = new ArrayList<Wire>();
	
	/*
	 * Given a JSONObject def, create the Java representation
	 */
	public Def ( JSONObject inputJSON )
	{
		//get the modules
		this.modulesJSONArray = inputJSON.getJSONArray("modules");
		
		//get the wires
		JSONArray wiresArray = inputJSON.getJSONArray("wires");
		
		// get Wires from JSON Array
		this.wires = Wire.getArrayFromJSONArray( wiresArray );
		
	}

	public JSONArray getModulesArray() {
		return modulesJSONArray;
	}

	public ArrayList<Wire> getWires() {
		return wires;
	}	
}
