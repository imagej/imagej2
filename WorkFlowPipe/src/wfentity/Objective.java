package wfentity;

import java.util.HashMap;
import java.util.UUID;

import wfapi.EntityBase;

/**
 * Objective represents a state within a process
 * @author rick
 *
 */
public class Objective extends EntityBase {
	
	Objective( UUID identifier, HashMap<String, Object> propertyHashMap )
	{
		this.propertyHashMap = propertyHashMap;
		this.identifier = identifier;
	}

}
