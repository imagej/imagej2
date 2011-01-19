package pipesentity;

import java.lang.annotation.Target;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Represents the JSON wire entity
 * @author rick
 *
 */
public class Wire {
	
	private ID id;
	private Src src;
	private Tgt tgt;
	
	public Wire( ID id, Src src, Tgt tgt )
	{
		this.id = id;
		this.src = src;
		this.tgt = tgt;
	}
	
	public Wire( JSONObject jsonObject ) 
	{
		//System.out.println("Wire Constructor input " + jsonObject );
		this.id = new ID( jsonObject.getString("id") );
		this.src = new Src( jsonObject.getJSONObject("src") );
		this.tgt = new Tgt( jsonObject.getJSONObject("tgt") );
		//System.out.println("Wire Constructor output " + getJSONObject() );	
	}

	public JSONObject getJSONObject()
	{
		JSONObject json = new JSONObject();
		
		//populate the json object
		json.put( "id", id.getValue() );
		json.put( "src", src.getJSONObject() );
		json.put( "tgt", tgt.getJSONObject() );
		
		return json;
	}

	public static ArrayList<Wire> getArrayFromJSONArray( JSONArray wiresArray ) 
	{
		ArrayList<Wire> wires = new ArrayList<Wire>();
		
		//for each wire
		for(int i = 0; i < wiresArray.length(); i++)
			wires.add( new Wire( wiresArray.getJSONObject(i) ) );
		return null;
	}

	public Src getSrc() {
		return this.src;
	}

	public Tgt getTgt() {
		return this.tgt;
	}

}
