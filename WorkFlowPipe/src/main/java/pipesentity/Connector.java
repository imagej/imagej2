package pipesentity;

import org.json.JSONObject;

/**
 * Connector represents a type of connector for a module
 * @author rick
 *
 */
public class Connector {
	
	private String connectorID;
	private Type type;
	private Attr attr;
	
	public Connector ( String connectorID, Type type, Attr attr )
	{
		this.connectorID = connectorID;
		this.type = type;
		this.attr = attr;
	}
	
	public String getConnectorID()
	{
		return connectorID;
	}
	
	public JSONObject getJSON()
	{
		JSONObject json = new JSONObject();
		
		json.put("_type", type.getValue() );
		json.put("_attr", attr.getJSON() );
		
		return json;
	}
	

}
