package wfapi;

import java.util.HashMap;
import java.util.UUID;

public abstract class EntityBase implements Property, Identifier, InternationalizationName {

	//Property
	protected HashMap<String, Object> propertyHashMap;
	
	//Identifier
	protected UUID identifier;
	
	//InternationalizationName
	protected String name;
	
	//Property
	@Override
	public HashMap< String, Object > getPropertyHashMap() {
		return propertyHashMap;
	}

	//Identifier
	@Override
	public Object getProperty(String property) {
		
		return propertyHashMap.get( property );
	}
	
	//Identifier
	@Override
	public UUID getIdentifier()
	{
		return this.identifier;
	}
	
	//InternationalizationName
	@Override
	public String getName() {
		return this.name;
	}

	//InternationalizationName
	@Override
	public void setName( String name ) 
	{
		this.name = name;
	}
	
}
