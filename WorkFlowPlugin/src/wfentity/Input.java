package wfentity;

import java.util.HashMap;
import java.util.UUID;

import wfapi.EntityBase;

/*
 * Input has a TYPE that is common to both ends of the connection
 */
public class Input extends EntityBase {
	
	public Input ( UUID identifier, HashMap<String, Object> propertiesHashMap )
	{	
		this.propertyHashMap = propertiesHashMap;
		this.identifier = identifier;
	}

}
