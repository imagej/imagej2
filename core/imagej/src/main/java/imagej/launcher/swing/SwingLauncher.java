package imagej.launcher.swing;

import imagej.gui.swing.MainFrame;

/**
 * Launches the ImageJ Swing user interface.
 *
 * @author Curtis Rueden
 */
public final class SwingLauncher {

	private SwingLauncher() {
		// prevent instantiation of utility class
	}

	public static void main(String[] args) {
		new MainFrame();
	}

}
