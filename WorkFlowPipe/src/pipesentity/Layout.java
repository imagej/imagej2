package pipesentity;

import org.json.JSONObject;

public class Layout {

	String id;
	String def;
	
	public Layout( String id, String def )
	{
		this.id = id;
		this.def = def;
	}
	
	public String getID()
	{
		return id;
	}
	
	public String getLayout()
	{
		return def;
	}
	
	@Override
	public String toString() {
		return this.id + " " + this.def;
	}
}
