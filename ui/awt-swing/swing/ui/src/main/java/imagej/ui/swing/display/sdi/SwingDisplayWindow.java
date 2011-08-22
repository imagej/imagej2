
package imagej.ui.swing.display.sdi;

import imagej.display.DisplayWindow;
import imagej.display.EventDispatcher;
import imagej.ui.common.awt.AWTWindowEventDispatcher;
import imagej.ui.swing.StaticSwingUtils;
import imagej.ui.swing.display.SwingDisplayPanel;
import java.awt.Container;
import java.awt.HeadlessException;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 *
 * @author GBH
 */
public class SwingDisplayWindow extends JFrame implements DisplayWindow {

	SwingDisplayPanel panel;

	public SwingDisplayWindow() throws HeadlessException {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocation(StaticSwingUtils.nextFramePosition());
	}

	@Override
	public void addEventDispatcher(EventDispatcher dispatcher) {
		if(dispatcher instanceof AWTWindowEventDispatcher) {
			addWindowListener((AWTWindowEventDispatcher)dispatcher);
		}
	}

	@Override
	public void setContentPane(Object panel) {
		this.setContentPane((Container)rootPane);
	}

	@Override
	public void showDisplay(boolean visible) {
		setVisible(visible);
	}

}
