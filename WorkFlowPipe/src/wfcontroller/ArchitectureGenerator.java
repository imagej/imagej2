package wfcontroller;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import wfapi.Model;
import wfentity.Person;
import wfentity.Technology;

import wfmodel.Architecture;
import wfentity.Connection;

/**
 * Allows construction and modification of Architecture Models.  Create an instance of
 * the ArchitectureGenerator and then populate the Person, Process, Technology, and 
 * Connection elements before calling getArchitecture().
 * 
 * @author rick
 */
public class ArchitectureGenerator {

	private ArrayList<Person> personArrayList;
	private ArrayList<Process> processArrayList;
	private ArrayList<Technology> technologyArrayList;
	private ArrayList<Connection> connectionArrayList;
	private HashMap<String, Object> hashMap;
	
	private URI workingURI;

	/**
	 * 
	 * @param workingURI is the writable resource location available for publishing the 
	 * @param hashMap provides additional properties useful for this generator
	 */
	public ArchitectureGenerator( URI workingURI, HashMap<String, Object> hashMap ) 
	{
		this.workingURI = workingURI;
		this.personArrayList = new ArrayList<Person>();
		this.technologyArrayList = new ArrayList<Technology>();
		this.connectionArrayList = new ArrayList<Connection>();
		this.processArrayList = new ArrayList<Process>();
		this.hashMap = hashMap;
	}

	public void addProcess( Process process )
	{
		processArrayList.add( process );
	}

	public boolean removeProcesss ( Process process )
	{
		return processArrayList.remove( process );
	}

	public void addPerson( Person person )
	{
		personArrayList.add(person);
	}

	public boolean removePerson( Person person )
	{
		return personArrayList.remove( person );
	}

	public void addConnection( UUID fromIdentifier, UUID toIdentifier, HashMap<String, Object> connectionHashMap )
	{
		connectionArrayList.add( new Connection( fromIdentifier, toIdentifier, connectionHashMap, UUID.randomUUID() ) );
	}

	public boolean removeConnection( Connection connection )
	{
		return connectionArrayList.remove( connection );
	}

	public void addTechnology( Technology technology )
	{
		technologyArrayList.add( technology );
	}

	public boolean removeTechnology ( Technology technology )
	{
		return technologyArrayList.remove( technology );
	}
	

	public HashMap<String, Object> getHashMap() {
		return hashMap;
	}

	public void setHashMap(HashMap<String, Object> hashMap) {
		this.hashMap = hashMap;
	}


	
	public Model getArchitecture( )
	{
		//Generate a unique UUID for this Architecture Instance
		UUID uuid = UUID.randomUUID();
		
		return new Architecture( uuid, workingURI, hashMap, personArrayList, technologyArrayList, connectionArrayList, processArrayList );
	}

	/*
	 * add an entity.Person or an entity.Technology component from an existing URI
	 */
	public void addFromURI( URI uri ) throws IOException
	{
		//try to get the content
		Object object = uri.toURL().getContent();

		if( object.getClass().isInstance( Technology.class ) ){
			addTechnology( (Technology) object);
		} else if( object.getClass().isInstance( Person.class ) )
		{
			addPerson( (Person) object );
		} else if ( object.getClass().isInstance( Process.class ) )
		{
			addProcess( (Process) object );
		}
	}

}
