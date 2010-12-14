package wfentity;

import java.net.URI;
import java.util.HashMap;
import java.util.UUID;

import wfapi.EntityBase;

/**
 * A plugin is an abstraction used to represent a discrete software process.
 * A plugin must have a unique namespace and a UUID within that namespace.
 * Each version of the plugin needs to have a unique UUID.
 */
public class Technology extends EntityBase {
	
	private URI pluginReferenceLocation;
	private String pluginNamespace;

	public Technology (UUID identifier, URI pluginReferenceLocation, String pluginNamespace, HashMap<String, Object> hashMap)
	{
		this.propertyHashMap = hashMap;
		this.identifier = identifier;
		this.pluginReferenceLocation = pluginReferenceLocation;
		this.pluginNamespace = pluginNamespace;
	}
	

	public String getPluginNamespace()
	{
		return this.pluginNamespace;
	}
	
	public URI getPluginReferenceLocation()
	{
		return pluginReferenceLocation;
	}

}
