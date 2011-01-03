package pipesentity;

import org.json.JSONObject;

/*
 * Used to represent the conf JSON entity
 */
public class Conf {
	
	private Name name;
	private Type type;
	private Value value;
	
	public Conf( Name name, Type type, Value value )
	{
		this.name = name;
		this.type = type;
		this.value = value;
	}
	
	/**
	 * @returns the JSON Object representing this object
	 */
	public JSONObject getJSONObject()
	{
		//create a new object
		JSONObject json = new JSONObject();
		
		//populate the value
		json.put("value", value.getValue() );
		
		//poplutate the type
		json.put("type", type.getValue() );
		
		JSONObject jsonOutput = new JSONObject();
		
		jsonOutput.put( name.getValue(), json );
		
		//return the object
		return jsonOutput;
	}
	
	

}
