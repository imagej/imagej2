package imagej.pipesentity;

import org.json.JSONObject;

/**
 * Used to represent the definition returned by the GUI
 * @author ipatron
 *
 */
public abstract class Definition {

	JSONObject jsonObject;
	
	public Definition( String def )
	{
		jsonObject = parseJSON( def );
	}

	/**
	 * returns true if the definition can be parsed into JSON
	 * TODO:Add other methods of parsing and logging/exception handling
	 * @return
	 */
	public static JSONObject parseJSON( String def )
	{
		JSONObject json;
		
		//try to handle exceptions
		boolean results = false;
		
		try {
			json = new JSONObject( def );
		}catch (Exception e){
			return null;
			}

		return json;
	}

	public JSONObject getJSONObject() {
		return this.jsonObject;

	}	
	
	
	
	
}
