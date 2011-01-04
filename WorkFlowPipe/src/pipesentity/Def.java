package pipesentity;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

/*
 * Match the JSON entity 'def'
 */
public class Def {
	
	private ArrayList<Module> modules;
	private ArrayList<Wire> wires;
	
	public Def( ArrayList<Module> modules,  ArrayList<Wire> wires )
	{
		this.modules = modules;
		this.wires = wires;
	}
	
	/*
	 * Given a JSONObject def, create the Java representation
	 */
	public Def ( JSONObject inputJSON )
	{
		//get the modules
		JSONArray modulesArray = inputJSON.getJSONArray("modules");
		
		//get Modules from JSON Array
		this.modules = Module.getArrayFromJSONArray( modulesArray );
		
		//get the wires
		JSONArray wiresArray = inputJSON.getJSONArray("wires");
		
		// get Wires from JSON Array
		this.wires = Wire.getArrayFromJSONArray( wiresArray );
	}

	public static Def getDef( String definition ) throws ParseException 
	{
		JSONObject jsonArray = new JSONObject( definition );
		return new Def( jsonArray );
	}

	
	/**
	 * returns an execution ordered vector of Module ids based on wire connections
	 * @param def
	 * @return
	 */
	public static Vector<Module> getOrderedVectorofConnectedModules( Def def )
	{
		// create the vector to be returned
		Vector<Module> moduleExecutionList = new Vector<Module>();
		
		//Get an iterator
		Iterator<Wire> wireIterator =  def.getWires().iterator();
		
		// while there are wires
		while( wireIterator.hasNext() ) 
		{
			// get the Wire
			Wire wire = wireIterator.next();
			
			// get the source moduleid
			String source = wire.getSrc().getModuleid();
			
			//TODO fix this logic to handle complex executions
			
			// get the source module
			Module sourceModule = Def.getModule( source, def );
			
			// add the source module before
			moduleExecutionList.add( sourceModule );
			
			// get the destination moduleid
			String destination = wire.getTgt().getModuleid();
			
			// get the destination module
			Module destinationModule = Def.getModule( destination, def );
			
			// add the destination module after
			moduleExecutionList.add( destinationModule );
			
		}
		

		//return the list
		return moduleExecutionList;
		
	}

	/** 
	 * 
	 * @param moduleid - the parent module
	 * @param def - the def
	 * @return the Module that matches the given moduleid
	 */
	public static Module getModule( String moduleid, Def def ) {
		return Module.getModuleByID( moduleid, def.modules );
	}

	public ArrayList<Module> getModules() {
		return modules;
	}

	public ArrayList<Wire> getWires() {
		return wires;
	}

	/**
	 * 
	 * @param def - definition
	 * @return
	 */
	public static Vector<Module> getOrderedVectorOfModules(Def def) {
//TODO: Pick up here
		return null;
	}
	
}
