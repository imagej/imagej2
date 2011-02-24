package imagej.core.plugins;

import imagej.plugin.ImageJPlugin;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

@Plugin(
		menuPath = "File>QuitIJ2"
)
public class QuitProgram implements ImageJPlugin {

	@Parameter(label="Exit ImageJ2?")
	private boolean userWantsToQuit;
	
	@Override
	public void run()
	{
		if (userWantsToQuit)
		{
			// TODO - save existing data
			// TODO - close windows
			System.exit(0);
		}
	}

}
