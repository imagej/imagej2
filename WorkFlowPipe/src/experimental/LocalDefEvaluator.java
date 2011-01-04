package experimental;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import modules.FetchPage;

import org.json.JSONObject;

import pipesentity.Def;
import pipesentity.Module;

public class LocalDefEvaluator {

	public static JSONObject getJSONResponse( Def def, HashMap<String, Module> moduleHashMap ) 
	{	
		//create the response JSON Object
		JSONObject resultJSON = new JSONObject();
		
		//create an execution vector of Modules
		Vector<Module> executionVector = new Vector<Module>();
		
		// get the execution vector if there are wires
		if ( def.getWires().size() > 0)
		{
			executionVector = Def.getOrderedVectorofConnectedModules( def );
		} 
		else 
		{
			// get the preview module
			executionVector = Def.getOrderedVectorOfModules( def );
		}
	
		// iterate through the Modules, executing sequentially
		Iterator<Module> executionIterator = executionVector.iterator();
		while( executionIterator.hasNext() )
		{
			//Module to execute
			Module toExecuteModule = executionIterator.next();
			
			//Module's input and parameters  //TODO replace
			System.out.println("Executing module " + toExecuteModule.getType() + " " + toExecuteModule.getJSONObject() );
			
			//get an instance of the module to run
			//Module module = moduleHashMap.get( key );
			
			// run the module
			//module.
		}
		
		
		//set return status to success
		resultJSON.put("ok", new Integer( 1 ));
		
		return resultJSON;
	}

}
