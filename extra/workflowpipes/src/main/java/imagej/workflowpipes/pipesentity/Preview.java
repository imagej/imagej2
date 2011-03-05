//
// Preview.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.workflowpipes.pipesentity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

/**
 * Represents the JSON response for pipes preview call
 * @author rick
 *
 */
public class Preview implements Serializable {
	
	
	private JSONObject statsPua = new JSONObject();
	private HashMap<ID,JSONObject> previewHashMap = new HashMap<ID,JSONObject>();
	private HashMap<ID,Error> errorHashMap = new HashMap<ID,Error>();
	
	
	public Preview()
	{
		
	}

	/**
	 * Add the errors reported by the instance if any
	 * @param moduleID
	 * @param errors
	 */
	public void addErrors( ID moduleID, ArrayList<Error> errors ) {
		
		// if there are errors
		if ( errors.size() > 0)
		{
			// iterate through them and add them
			for(Error error : errors )
			{
				errorHashMap.put( moduleID, error );
			}
		}
		
	}

	public void addPreview( ID id, JSONObject previewJSON ) {
		previewHashMap.put( id, previewJSON );
	}

	/**
	 * @return the entire response message in support of a call to ajax.pipe.preview
	 */
	public JSONObject getJSON() 
	{
		JSONObject output = new JSONObject();
		
		// add ok response
		output.put( "ok", new Integer(1) );
		
		// add the errors
		JSONObject jsonErrors = new JSONObject();
		jsonErrors.put("modules", getModuleErrors() );
		jsonErrors.put("pipes", getPipeErrors() );
		output.put( "errors", jsonErrors  );
		
		// add the preview content
		output.put("preview", getPreviewJSON() );
		
		// add the stats
		JSONObject jsonPua = new JSONObject();
		jsonPua.put( "pua", statsPua );
		output.put( "stats", jsonPua );
		
		return output;
	}

	private String getPipeErrors() {
		JSONObject pipeErrors = new JSONObject();
		
		//TODO: implement pipe error reporting
		
		return null;
	}

	private JSONObject getPreviewJSON() 
	{
		JSONObject previewJSON = new JSONObject();
		
		for( ID id : previewHashMap.keySet() )
		{
			previewJSON.put( id.getValue(), previewHashMap.get( id ) );
		}
		
		return previewJSON;
	}

	// returns the JSONObject
	private JSONObject getModuleErrors() 
	{
		JSONObject jsonErrors = new JSONObject();
		
		// for each error
		for ( ID id : errorHashMap.keySet() )
		{
			// add the error
			jsonErrors.put( id.getValue(), errorHashMap.get( id ).getJSON() );
		}
		
		return jsonErrors;
	}

	public void addStats( String title, JSONObject statsJSON ) {
		statsPua.put( title, statsJSON );
	};
	
	

	

}
