
package imagej.display;

import net.imglib2.img.Axis;

/**
 *
 * @author GBH
 */
public interface ImageDisplayPanel extends DisplayPanel {
	long getAxisPosition(Axis axis);
	void setAxisPosition(final Axis axis, final int position);
}
