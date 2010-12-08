package wfentity;

import java.util.HashMap;
import java.util.UUID;

import wfapi.EntityBase;

/**
 * Represents a single output
 * @author rick
 *
 */
public class Output extends EntityBase {
	
	public Output( UUID identifier, HashMap<String, Object> propertiesHashMap  )
	{
		this.identifier = identifier;
		this.propertyHashMap = propertiesHashMap;
	}
	
}
