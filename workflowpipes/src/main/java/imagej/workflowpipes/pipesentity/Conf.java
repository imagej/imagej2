package imagej.workflowpipes.pipesentity;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONObject;

/*
 * Used to represent the conf JSON entity
 */
public class Conf implements Serializable {
	
	private Name name;
	private Type type;
	private Value value;
	
	/**
	 * 
	 * @param name - Conf name
	 * @param value - Conf value
	 * @param type - Conf type
	 */
	public Conf( Name name, Value value, Type type )
	{
		this.name = name;
		this.type = type;
		this.value = value;
	}
	
	public Type getType() {
		return type;
	}

	public Value getValue() {
		return value;
	}

	/**
	 * returns true if conf's name matches inputName
	 * @param name
	 * @return
	 */
	public boolean isName( String inputName )
	{
		if( inputName.equals( this.name.getValue() ) )
		{
			return true;
		}
		
		return false;
	}
	
	/**
	 * 
	 * @param confJSON - JSON input string used to create new Conf type
	 */
	public Conf( String name, JSONObject confJSON ) 
	{
		this.name = new Name( name );
		this.type = new Type( confJSON.getString("type") );
                System.out.println("confJSON is " + confJSON);
		this.value = new Value( confJSON.getString("value") );
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

	public static ArrayList<Conf> getConfs( String string ) 
	{
		ArrayList<Conf> confs = new ArrayList<Conf>();
		
		//let class parse the input string
		JSONObject json = null;
		try {
			json = new JSONObject( string );
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		//use an iterator to find all the confs in the JSONObject
		Iterator i = json.keys();
		while( i.hasNext() )	
		{
			JSONObject jsonConf;
			
			// get the name (key) value
			String name = (String) i.next();
			
			// get the next conf as JSONObject
			jsonConf = json.getJSONObject( name );
			
			//Create a conf object and add to the array
			confs.add( new Conf( name, jsonConf ) );
		}
		
		return confs;
	}

	/**
	 * returns the first conf found matching the string name
	 * @param confName
	 * @param confs
	 * @return null if no match
	 */
	public static Conf getConf( String confName, ArrayList<Conf> confs ) 
	{
		//for each conf in the array
		for(Conf conf:confs)
		{
			System.out.println("Conf :: getConf :: conf is " + conf.getJSONObject() );
			
			//check to see if the name matches
			if( conf.isName(confName))
			{
				//it matches so return this conf
				return conf;
			}
		}
		
		// no match - return null
		return null;
		
	}

	public static JSONObject getJSON(ArrayList<Conf> confs) {
		JSONObject json = new JSONObject(); 
		
		// iterate over the confs
		for ( Conf conf : confs )
		{
			JSONObject internalJSON = new JSONObject(); 
			internalJSON.put( "value", conf.value.getValue()  );
			internalJSON.put( "type", conf.type.getValue()  );
			
			json.put( conf.name.getValue(), internalJSON );
		}
		return json;
	}
	
	

}
