package wfcontroller;

import wfmodel.Location;

/**
 * Purpose of the class is to update the Location of an 
 * entity within an Architecture Diagram
 * @author rick
 *
 */
public class LocationUpdater {
	
	public static void updateLocation( Location location, double width, double height, double depth )
	{
		location = new Location( width, height, depth );
	}

}
