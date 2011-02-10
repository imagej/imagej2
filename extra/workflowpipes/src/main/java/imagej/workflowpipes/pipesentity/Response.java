package imagej.workflowpipes.pipesentity;

import java.io.Serializable;
import java.util.HashMap;

import org.json.JSONObject;

public class Response implements Serializable {
	
	private HashMap<String,Object> otherStatsHashMap = new HashMap<String, Object>(); 
	
	private String title;
	
	private int count = 0;
	private double variance = 0.0;
	private double mode = 0.0;
	private double min = 0.0;
	private double range = 0.0;
	private double max = 0.0;
	private double median = 0.0;
	private double mean = 0.0;
	private double stddev = 0.0;
	private double sum = 0.0;
	
	public Response()
	{
		
	}
		
	public JSONObject getJSON()
	{
		JSONObject json = new JSONObject();
		JSONObject jsonInternals = new JSONObject();
		
		// populate the internals
		jsonInternals.put("count", count );
		jsonInternals.put("variance", variance );
		jsonInternals.put("mode", mode );
		jsonInternals.put("min", min ); 
		jsonInternals.put("range", range );
		jsonInternals.put("max", max );
		jsonInternals.put("median", median );
		jsonInternals.put("mean", mean );
		jsonInternals.put("stddev", stddev );
		jsonInternals.put("sum", sum );
		
		// tag the internals
		json.put("response", jsonInternals);
		
		// add the module specific stats
		for( String keyString : otherStatsHashMap.keySet() )
		{
			json.put( keyString, otherStatsHashMap.get( keyString ) );
		}
		
		return json;
	}
	
	/**
	 * Add a module specific key / value pair
	 */
	public void addStat( String key, Object value )
	{
		otherStatsHashMap.put(key, value);
	}
	
	public void setTitle( String title )
	{
		this.title = title;
	}
	
	// updates the values
	public void run( Duration duration, Start start )
	{	
		//TODO: implement stats update logic 
	}

	public String getTitle() {
		if ( this.title != null )
		return this.title;
		return "";
	}
}
