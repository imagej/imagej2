package imagej.workflowpipes.pipesentity;

import java.util.ArrayList;

import org.json.JSONObject;

/**
 * Represents the pipes prop type
 * @author rick
 *
 */
public class Prop {
	
	private Connector connector;
	
	public Prop( Connector connector )
	{
		this.connector = connector;
	}

	public static JSONObject getJSON( ArrayList<Prop> props ) 
	{
		JSONObject jsonObject = new JSONObject();
		for(Prop prop : props )
		{
			// add the prop
			jsonObject.put( prop.getConnector().getConnectorID(), prop.getConnector().getJSON() );
		}
		
		return jsonObject;
	}
	
	public Connector getConnector()
	{
		return connector;
	}
}
