package wfapi;

import java.util.HashMap;

/**
 * Properties of each object
 * @author rick
 */
public interface Property {

	public HashMap< String, Object > getPropertyHashMap();
	public Object getProperty ( String property );
}
