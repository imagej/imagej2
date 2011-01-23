package imagej.workflowpipes.pipesentity;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Represents the items used between modules
 * @author rick
 *
 */
public class Item implements Serializable {
    
	private String name;
	private String value;
	
	public Item( String name, String value )
	{
		this.name = name;
		this.value = value;
	}
	
	public static JSONArray getJSON( ArrayList<Item> items ) 
	{
		JSONArray jsonArray = new JSONArray();
		
		for( Item item : items )
		{
			JSONObject json = new JSONObject();
			
			// add the item
			json.put( item.getName(), item.getValue() );
			
			jsonArray.put(  json  );
		}
		
		return jsonArray;
	}

	public String getName() 
	{
		return name;
	}
	
	public String getValue()
	{
		return value;
	}

}
