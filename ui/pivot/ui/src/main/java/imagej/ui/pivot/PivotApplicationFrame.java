
package imagej.ui.pivot;

import imagej.ui.ApplicationFrame;

import org.apache.pivot.wtk.Frame;

/**
 * Pivot implementation of {@link ApplicationFrame}.
 * 
 * @author Curtis Rueden
 */
public class PivotApplicationFrame extends Frame implements ApplicationFrame {
	
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
		requestActive();
	}
	
}
