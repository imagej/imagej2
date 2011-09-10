package imagej.ext.script;

import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Plugin;

import java.util.List;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

/**
 * Displays all the script engines available with JDK Platform.
 * 
 * @author Grant Harris
 */
@Plugin(menuPath = "Plugins>Script>List Engines")
public class ScriptEngineList implements ImageJPlugin {

	@Override
	public void run() {
		ScriptEngineManager sem = new ScriptEngineManager();
		//Gets all available script engine factories as a list
		List<ScriptEngineFactory> sefList = sem.getEngineFactories();
		for (ScriptEngineFactory sef : sefList) {
			//Print available script engine name
			System.out.format("Engine Name : %s \n", sef.getEngineName());
		}
		// TODO - Output to TextDisplay.
	}

}
