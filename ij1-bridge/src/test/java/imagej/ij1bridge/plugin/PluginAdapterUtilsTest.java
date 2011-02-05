package imagej.ij1bridge.plugin;

import static org.junit.Assert.*;
import ij.IJ;
import ij.ImageJ;
import imagej.plugin.api.PluginEntry;

import java.util.HashMap;

import org.junit.Test;

public class PluginAdapterUtilsTest {
	
	//TODO: Removed comments once headless operation is enabled on Hudson
	@Test
	public void hudsonTestHelperTODOREMOVE(){}
	
//	@Test
//	public void assignParentMenuNameToPluginEntryTest()
//	{
//		new ImageJ();
//		PluginAdapterUtils pluginAdapterUtils = new PluginAdapterUtils( IJ.getInstance() );
//		PluginEntry pluginEntry = new PluginEntry("ij.plugin.Close", "Close");
//		
//		//call method to lookup the parent menu hierarchy
//		pluginAdapterUtils.setIJPluginParentMenu(pluginEntry);
//	
//		//check to see if the value parent value has been set
//		assertEquals( "File", pluginEntry.getParentMenu()  );
//	}
//
//	
//	@Test
//	public void returnAHashMapOfTheIJMenusTest()
//	{
//		new ImageJ(); 
//		
//		//HashMap is key, value: E.g. "Close", "File > Close"
//		HashMap<String, String> menuHashMap = PluginAdapterUtils.getIJMenusHashMap( IJ.getInstance() );
//			
//		//check to see if the value parent value has been set
//		assertEquals( "File > Close", menuHashMap.get("Close")  );
//	}
}
