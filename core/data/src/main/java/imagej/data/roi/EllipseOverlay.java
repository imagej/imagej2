/**
 * 
 */
package imagej.data.roi;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import net.imglib2.roi.EllipseRegionOfInterest;

/**
 * @author leek
 *
 *An ellipse bounded by an axis-aligned rectangle, backed by EllipseRegionOfInterest 
 */
public class EllipseOverlay extends AbstractShapeOverlay<EllipseRegionOfInterest> {

	
	private static final long serialVersionUID = 1L;
	final protected EllipseRegionOfInterest roi = new EllipseRegionOfInterest(2);
	
	/* (non-Javadoc)
	 * @see imagej.data.roi.AbstractShapeOverlay#getShapeRegionOfInterest()
	 */
	@Override
	public EllipseRegionOfInterest getShapeRegionOfInterest() {
		return roi;
	}
	
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeDouble(roi.getOrigin(0));
		out.writeDouble(roi.getOrigin(1));
		out.writeDouble(roi.getRadius(0));
		out.writeDouble(roi.getRadius(1));
	}
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		roi.setOrigin(in.readDouble(), 0);
		roi.setOrigin(in.readDouble(), 1);
		roi.setRadius(in.readDouble(), 0);
		roi.setRadius(in.readDouble(), 1);
	}

}
