
package imagej.ui.swing.display;

import imagej.data.display.ui.ImageDisplayViewer;

/**
 * Interface for Swing-based image display viewers.
 * 
 * @author Curtis Rueden
 */
public interface SwingImageDisplayViewer extends ImageDisplayViewer {

	@Override
	SwingDisplayPanel getPanel();

	/** Gets the JHotDraw canvas used by the viewer. */
	JHotDrawImageCanvas getCanvas();

}
