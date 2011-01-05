package pipesentity;

import org.json.JSONObject;

public class Response {
	
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
		
		return json;
	}
	
	// updates the values
	public void run( Duration duration )
	{
		//TODO: implement stats update logic 
	}
}
