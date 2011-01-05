package pipesentity;

import java.util.ArrayList;

import org.json.JSONArray;

/**
 * Represents the items used between modules
 * @author rick
 *
 */
public class Item 
{
	private String name;
	private String value;
	
	public Item( String name, String value )
	{
		this.name = name;
		this.value = value;
	}
	
	public static JSONArray getJSON(ArrayList<Item> items) 
	{
		JSONArray jsonArray = new JSONArray();
		
		for( Item item : items )
		{
			// add the item
			jsonArray.put(  item.getName() +":" + item.getName()  );
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
