package imagej.ij1bridge.plugin;

import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.util.ArrayList;
import java.util.HashMap;

import javax.accessibility.AccessibleContext;

import ij.IJ;
import ij.ImageJ;
import imagej.plugin.PluginEntry;

/*
 * Performs functions relating to IJ1.43 to IJ2.0 menu compatibility
 */
public class PluginAdapterUtils {

	private HashMap<String, String> keyValueLabelMap;

	public PluginAdapterUtils( ImageJ imageJ){
		keyValueLabelMap = getIJMenusHashMap( imageJ );
	}
	
	public static HashMap<String, String> getIJMenusHashMap( ImageJ imageJInstance ) {

		HashMap<String, String> keyValueLabelMap = new HashMap<String, String>();
		
		// walk the menus
		for (int menuNumber = 0; menuNumber < imageJInstance.getMenuBar().getMenuCount(); menuNumber++) 
		{
			Menu menu = imageJInstance.getMenuBar().getMenu(menuNumber);

			getMenuLabel( menu, keyValueLabelMap, null );
			
		}
		
		return keyValueLabelMap;
	}

	private static void printHashMap( HashMap<String, String> keyValueLabelMap )
	{
		for(String value:keyValueLabelMap.values())
		{
			System.out.println( value );
		}
	}
	
	/*
	 * Pass null for second argument unless you want to replace the current
	 * parent label.
	 */
	private static void getMenuLabel(Menu menu, HashMap<String, String> keyValueLabelMap, String parentStringLabel) {
		// update the partentStringLabel for hierarchy depth
		
		for (int menuItemCount = 0; menuItemCount < menu.getItemCount(); menuItemCount++) {
			MenuItem menuItem = menu.getItem(menuItemCount);

			// ignore menu spacers
			if (menuItem.getLabel() != "-") {
				String innerStringLabel;

				if (parentStringLabel != null) {
					innerStringLabel = parentStringLabel + " > "
							+ menuItem.getLabel();
				} else {
					innerStringLabel = menu.getLabel() + " > "
							+ menuItem.getLabel();
				}

				// check the class
				String classString = menuItem.getClass().toString();
				if ( "class java.awt.Menu".equals(classString) ) {
					
					getMenuLabel( (Menu) menuItem, keyValueLabelMap,
							innerStringLabel);
				} else {
					// leaf node so add + "/"
					keyValueLabelMap.put(menuItem.getLabel(), innerStringLabel);
				}
			}

		}
	}


	/**
	 * Given a PlugInEntry and an ImageJ instance, lookup the menu structure
	 * and assign the value to the PlugInEntry
	 */
	public void getIJ1PluginMenuPath( PluginEntry pluginEntry )
	{
		if ( keyValueLabelMap.containsKey( pluginEntry.getLabel() ))
		{
			//get the parent menu
			String valueAtKey = keyValueLabelMap.get( pluginEntry.getLabel() );
	 		
			//trim the full path by removing the label
			int lastIndex = valueAtKey.lastIndexOf(" > " + pluginEntry.getLabel());
			String trimmedResult = valueAtKey.substring(0, lastIndex);		

			//assign value
			pluginEntry.setParentMenu( trimmedResult  );
		} 
	}

	/**
	 * @param args
	 */
	
	/*
	 * TODO: MOVE TO TEST
	public static void main(String[] args) {
		// Print IJ menu Hierarchy
		new ImageJ();
		HashMap<String, String> keyValues = PluginAdapterUtils.getIJMenusHashMap(IJ.getInstance());
		
		//print values
		printHashMap( keyValues );
	} */

}
