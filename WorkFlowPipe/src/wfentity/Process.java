package wfentity;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import wfapi.EntityBase;

/**
 * A process is akin to a workflow in that it consists of inputs, outputs, and processes
 * @author rick
 */
public class Process extends EntityBase {
	
	private URI processReferenceLocation;
	private ArrayList< Objective > objectiveArrayList;
	
	public ArrayList<Objective> getObjectiveArrayList() {
		return objectiveArrayList;
	}

	public Process( UUID identifier, URI processReferenceLocation, 
			HashMap< String, Object > propertyHashMap, ArrayList< Objective > objectiveArrayList )
	{
		this.propertyHashMap = propertyHashMap;
		this.identifier = identifier;
		this.processReferenceLocation = processReferenceLocation;
		this.objectiveArrayList = objectiveArrayList;
	}
	
	public URI getReferenceLocation() 
	{
		return processReferenceLocation;
	}
	
}
