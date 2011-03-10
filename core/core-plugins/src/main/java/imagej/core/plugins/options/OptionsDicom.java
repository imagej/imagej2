package imagej.core.plugins.options;

import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

/**
 * Runs the Edit::Options::DICOM... dialog
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = "Edit", mnemonic = 'e'),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "DICOM...", weight = 14) })
public class OptionsDicom implements ImageJPlugin{

	@Parameter(label = "Open as 32-bit float", persist=true)
	private boolean generateDebugInfo;

	@Parameter(label = "Orthogonal Views: Rotate YZ", persist=true)
	private boolean rotateYZ;

	@Parameter(label = "Orthogonal Views: Rotate XZ", persist=true)
	private boolean rotateXZ;

	@Override
	public void run() {
		// DO NOTHING - all functionality contained in annotations
	}

}
