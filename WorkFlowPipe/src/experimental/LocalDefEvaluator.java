package experimental;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import pipes.Service;
import pipesapi.Module;
import pipesentity.Conf;
import pipesentity.Def;
import pipesentity.ID;
import pipesentity.Type;

public class LocalDefEvaluator {

	public static JSONObject getPreview( Def def, HashMap<Service, Module> moduleHashMap )
	{	
		//create the response JSON Object
		JSONObject json = new JSONObject();
		
		// TODO:use wires for ordering
		// TODO:use modules connectors for input/outputs		
		// TODO: extend checking and ordering logic
		
		JSONArray modulesJSONArray = def.getModulesArray();
		
		ArrayList<Module> moduleList = new ArrayList<Module>();

		for (int i = 0; i < modulesJSONArray.length(); i++) 
		{
			//get the JSONObject
			JSONObject jsonInternal = modulesJSONArray.getJSONObject(i);
			
			//get the type
			Type type = new Type( jsonInternal.getString("type") );
			
			// get the ID
			ID id = new ID( jsonInternal.getString("id"));
			
			// get the confs
			ArrayList<Conf> confs = Conf.getConfs( jsonInternal.getString("conf") );
			
			// get an instance of the service
			Module moduleInstance = moduleHashMap.get( type ).clone();
			
			moduleInstance.setID( id );
			moduleInstance.setConfs( confs );
			
			//get the service
			moduleList.add( moduleInstance );
		}
		
		// evaluate the modules individually
		for( Module module : moduleList )
		{
			// get the confs
			ArrayList<Conf> confs = module.getConfArray();
			
			System.out.println( "Runnning module " + module.getType().getValue()  + " with configurations " + Conf.getJSON( confs ) );	
			
			//just print the results
			System.out.println( module.getJSONObjectResults() );
		}
		return json;
	}

}
