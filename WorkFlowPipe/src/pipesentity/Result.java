package pipesentity;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Represents the returned result data structure
 * if url is null, result source is assumed to be "feed"
 * 
 * @author ipatron
 * 
 */
public class Result {

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
			jsonResult.put("name", this.name);

			// add the description
			jsonResult.put("description", this.description);

			// add the url
			jsonResult.put("url", this.url);

			// add the source
			jsonResult.put("source", this.source);

			return jsonResult;
		}
		
		JSONObject jsonResult = new JSONObject();

		// add the name
		jsonResult.put("name", this.name);

		// add the description
		jsonResult.put("description", this.description);

		// add the type
		jsonResult.put("type", this.type);

		return jsonResult.put("source", this.source);
	}
}
