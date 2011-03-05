//
// Response.java
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
