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
