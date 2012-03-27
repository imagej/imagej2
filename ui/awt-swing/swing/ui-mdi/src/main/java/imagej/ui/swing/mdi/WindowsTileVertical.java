
package imagej.ui.swing.mdi;

import imagej.ImageJ;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Plugin;
import imagej.ui.Arrangeable.Arrangement;
import imagej.ui.Desktop;
import imagej.ui.UserInterface;
import imagej.ui.UIService;

/**
 * Arranges the Windows in an MDI environment.
 * 
 * @author Grant Harris
 */
@Plugin( menuPath = "Window>Tile Vertical")
public class WindowsTileVertical implements ImageJPlugin {

	@Override
	public void run() {
		final UserInterface ui = ImageJ.get(UIService.class).getUI();
		Desktop desk = ui.getDesktop();
		desk.setArrangement(Arrangement.VERTICAL);
	}

}
