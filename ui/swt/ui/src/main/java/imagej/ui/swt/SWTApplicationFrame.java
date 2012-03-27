
package imagej.ui.swt;

import imagej.ui.ApplicationFrame;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * SWT implementation of {@link ApplicationFrame}.
 * 
 * @author Curtis Rueden
 */
public class SWTApplicationFrame extends Shell implements ApplicationFrame {

	public SWTApplicationFrame(final Display display) {
		super(display, 0);
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
		forceFocus();
	}

}
