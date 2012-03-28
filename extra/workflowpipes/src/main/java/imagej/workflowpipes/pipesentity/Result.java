/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

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
