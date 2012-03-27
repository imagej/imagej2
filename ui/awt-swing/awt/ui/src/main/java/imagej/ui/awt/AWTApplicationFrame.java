
package imagej.ui.awt;

import imagej.ui.ApplicationFrame;

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.HeadlessException;

/**
 * AWT implementation of {@link ApplicationFrame}.
 * 
 * @author Grant Harris
 */
public class AWTApplicationFrame extends Frame implements ApplicationFrame {

	public AWTApplicationFrame(final String title) throws HeadlessException {
		super(title);
	}
	
	// -- ApplicationFrame methods --

	@Override
	public int getLocationX() {
		return getLocation().x;
	}

	@Override
	public int getLocationY() {
		return getLocation().y;
	}

	@Override
	public void activate() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				// NB: You might think calling requestFocus() would work, but no.
				// The following solution is from: http://bit.ly/zAXzd5
				toFront();
				repaint();
			}
		});
	}

}
