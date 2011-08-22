package imagej.ui.awt;

import imagej.ui.ApplicationFrame;
import java.awt.Frame;
import java.awt.HeadlessException;

/**
 *
 * @author GBH
 */
	public class AWTApplicationFrame extends Frame implements ApplicationFrame {

	public AWTApplicationFrame(String title) throws HeadlessException {
		super(title);
	}
}
