package imagej.workflowpipes.pipesentity;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Represents the returned result data structure
 * if url is null, result source is assumed to be "feed"
 * 
 * @author ipatron
 * 
 */
public class Result implements Serializable {

	private Name name;
	private Description description;
	private Type type;
	private Source source;
	private URL url;

	public Result(Name name, Description description, Type type, Source source) {
		this.name = name;
		this.description = description;
		this.type = type;
		this.source = source;

	}

	public Result(Name name, Description description, Source source, URL url) {
		this.name = name;
		this.description = description;
		this.url = url;
		this.source = source;
	}

	/**
	 * @return the expected JSON notation for a Result
	 */
	public JSONObject getJSONObject() {
		
		// if it is a pipe (url is null)
		if (url != null) {
			JSONObject jsonResult = new JSONObject();

			// add the name
			jsonResult.put("name", this.name.getValue() );

			// add the description
			jsonResult.put("description", this.description.getValue() );

			// add the url
			jsonResult.put("url", this.url.getValue() );

			// add the source
			jsonResult.put("source", this.source.getSource() );

			return jsonResult;
		}
		
		JSONObject jsonResult = new JSONObject();

		// add the name
		jsonResult.put("name", this.name.getValue() );

		// add the description
		jsonResult.put("description", this.description.getValue() );

		// add the type
		jsonResult.put("type", this.type.getValue() );

		return jsonResult.put("source", this.source.getSource() );
	}
	
	/**
	 * Converts an ArrayList of Result to property formatted JSON
	 * @param resultArrayList
	 * @return
	 */
	public static JSONObject getResultsJSONObject( ArrayList<Result> resultArrayList ) 
	{
		//create the Results JSON Object
		JSONObject jsonResults = new JSONObject();
		
		//create a JSON array
		JSONArray jsonArray = new JSONArray();
		
		//iterate through the results and add the results to the array
		for( Result result: resultArrayList )
		{
			jsonArray.put( result.getJSONObject() );
		}
		
		//add array to JSON Object
		jsonResults.put( "result", jsonArray );
		
		return jsonResults;
	}
}
