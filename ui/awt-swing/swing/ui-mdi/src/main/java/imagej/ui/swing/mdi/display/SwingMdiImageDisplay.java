
package imagej.ui.swing.mdi.display;

import imagej.data.display.ImageDisplay;
import imagej.ext.plugin.Plugin;
import imagej.ui.common.awt.AWTKeyEventDispatcher;
import imagej.ui.swing.display.AbstractSwingImageDisplay;
import imagej.ui.swing.mdi.InternalFrameEventDispatcher;

import javax.swing.JInternalFrame;

/**
 * Multiple Document Interface implementation of Swing image display plugin. The
 * MDI display is housed in a {@link JInternalFrame}.
 * 
 * @author Grant Harris
 * @author Curtis Rueden
 * @see AbstractSwingImageDisplay
 */
@Plugin(type = ImageDisplay.class)
public class SwingMdiImageDisplay extends AbstractSwingImageDisplay {

	public SwingMdiImageDisplay() {
		super(new SwingMdiDisplayWindow());
		final SwingMdiDisplayWindow mdiWindow = (SwingMdiDisplayWindow) window;

		getPanel()
			.addEventDispatcher(new AWTKeyEventDispatcher(this, eventService));
		mdiWindow.addEventDispatcher(new InternalFrameEventDispatcher(this,
			eventService));
	}

}
