package wfmodel;


import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import wfapi.Model;
import wfentity.Person;
import wfentity.Technology;

import wfentity.Connection;

/**
 * Contains the entities and relationships needed to model a scoped ('bounded') architecture
 * An architecture is composed of people, processes, technologies, and connections
 */
public class Architecture implements Model {
	
	private ArrayList< Person > personArrayList;
	private ArrayList< Technology > technologyArrayList;
	private ArrayList< Process > processArrayList;
	private ArrayList< Connection > connectionArrayList;
	
	private UUID architectureID;
	private URI architectureLocation;
	private HashMap<String, Object> hashMap;
	
	public HashMap<String, Object> getProperties()
	{
		return hashMap;
	}

	public UUID getID() {
		return architectureID;
	}

	public URI getReferenceLocation() {
		return architectureLocation;
	}
	
	public Architecture( UUID architectureID, URI workingURI, HashMap<String, 
			Object> hashMap, ArrayList< Person > personArrayList, 
			ArrayList< Technology > technologyArrayList, 
			ArrayList< Connection > connectionArrayList,
			ArrayList< Process> processArrayList )
	{
		this.hashMap = hashMap;
		this.architectureID = architectureID;
		this.architectureLocation = workingURI;
		this.hashMap = hashMap;
		this.technologyArrayList = technologyArrayList;
		this.connectionArrayList = connectionArrayList;
		this.personArrayList = personArrayList;
		this.processArrayList = processArrayList;
	}

	public ArrayList<Technology> getTechnologyArrayList() {
		return technologyArrayList;
	}

	public ArrayList<Connection> getConnectionArrayList() {
		return connectionArrayList;
	}

	public ArrayList<Person> getPersonArrayList() {
		return this.personArrayList;
	}
	
	public ArrayList< Process > getProcessList(){
		return this.processArrayList;
	}
	
}
