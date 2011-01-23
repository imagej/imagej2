package imagej.workflowpipes.pipesentity;

import java.io.Serializable;
import java.util.ArrayList;
import org.json.JSONArray;

/*
 * Match the JSON entity 'def'
 */
public class Def implements Serializable {
	
	private JSONArray modulesArrayJSON;
	private ArrayList<Wire> wires = new ArrayList<Wire>();
	
	/*
	 * Given a JSONObject def, create the Java representation
	 */
	public Def ( JSONArray modulesArrayJSON, JSONArray wiresArrayJSON )
	{
		//get the modules
		this.modulesArrayJSON = modulesArrayJSON;
		
		//get the wires
		JSONArray wiresArray = wiresArrayJSON;
		
		// get Wires from JSON Array
		this.wires = Wire.getArrayFromJSONArray( wiresArray );
		
	}

	public JSONArray getModulesArray() {
		return modulesArrayJSON;
	}

	public ArrayList<Wire> getWires() {
		return wires;
	}	
}
