package wfentity;

import java.util.HashMap;
import java.util.UUID;
import wfapi.EntityBase;

/**
 * Represents a specific person that performs an activity within a workflow
 */
public class Person extends EntityBase  {
	
	private UUID personID;
	
	public Person(UUID personID, HashMap<String, Object> hashMap )
	{
		this.propertyHashMap = hashMap;
		this.personID = personID;
	}
	
	public UUID getPersonID()
	{
		return personID;
	}
	
}
