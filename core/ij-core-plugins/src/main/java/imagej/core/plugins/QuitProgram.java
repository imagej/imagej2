package imagej.core.plugins;

import imagej.plugin.ImageJPlugin;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

/**
 * TODO
 *
 * @author Barry DeZonia
 */
@Plugin(
		menuPath = "File>Quit IJ2"
)
public class QuitProgram implements ImageJPlugin {

	@Parameter(label="Exit ImageJ 2?")
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
