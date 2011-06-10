package imagej.ui.swing.plugins;

import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Plugin;
import imagej.ui.swing.SwingOverlayManager;

@Plugin(menu = { @Menu(label = "Image"), @Menu(label = "Overlay"),
		@Menu(label = "OverlayManager") })
public class SwingOverlayManagerPlugin implements ImageJPlugin {

	@Override
	public void run() {
		SwingOverlayManager overlaymgr = new SwingOverlayManager();
		overlaymgr.setVisible(true);

	}

}
