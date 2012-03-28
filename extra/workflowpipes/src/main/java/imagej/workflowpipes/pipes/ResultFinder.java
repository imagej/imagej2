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
