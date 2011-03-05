//
// ResultFinder.java
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

package imagej.workflowpipes.pipes;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import imagej.workflowpipes.pipesapi.Module;
import imagej.workflowpipes.pipesentity.Result;

/**
 * 
 * @author ipatron
 *
 */
public class ResultFinder {

	private ArrayList<Result> resultArrayList;
	
	public ResultFinder(){
		//set up the resultArrayList
		resultArrayList = new ArrayList<Result>();
		
	}
	
	/**
	 * Performs a string literal search and returns the results
	 * @param searchTerm - the term to search for
	 * @param moduleArrayList - the array list
	 * @return
	 */
	public ArrayList<Result> getResults( String searchTerm, HashMap<Service,Module> modulesServiceHashMap )
	{
		//search the description and name for search term and add results
		//TODO: replace with existing search method
		for( Module module : modulesServiceHashMap.values() )
		{
			//if there is a match
			if(module.searchNameAndDescription(searchTerm))
			{				
				resultArrayList.add( module.getResult() );
			}
		}
		
		//return results
		return resultArrayList;
	}
	
	public static JSONObject getResultsJSONObject( String searchTerm, HashMap<Service, Module> modulesServiceHashMap )
	{
		//create a result finder
		ResultFinder resultFinder = new ResultFinder();
		
		//get the result array list
		ArrayList<Result> resultArrayList = resultFinder.getResults( searchTerm, modulesServiceHashMap );
		
		//get the JSON object from the resultArrayList
		return Result.getResultsJSONObject( resultArrayList );
			
	}
}
