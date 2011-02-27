package imagej.gui.plugins;

import imagej.plugin.ImageJPlugin;
import imagej.plugin.Plugin;

import java.awt.HeadlessException;

import javax.swing.JOptionPane;

/**
 * TODO
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
@Plugin(menuPath = "File>Quit")
public class QuitProgram implements ImageJPlugin {

	@Override
	public void run() {
		try {
			int rval = JOptionPane.showConfirmDialog(null, "Really quit ImageJ?",
				"ImageJ", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (rval != JOptionPane.YES_OPTION) return; // do not quit
		}
		catch (HeadlessException exc) {
			// in a headless environment, assume quit is OK
		}
		// TODO - save existing data
		// TODO - close windows
		System.exit(0);
	}

}
