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
public class EllipseOverlay extends AbstractROIOverlay<EllipseRegionOfInterest> {

	
	public EllipseOverlay() {
		super(new EllipseRegionOfInterest(2));
	}
	private static final long serialVersionUID = 1L;
	
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		EllipseRegionOfInterest roi = getRegionOfInterest();
		out.writeDouble(roi.getOrigin(0));
		out.writeDouble(roi.getOrigin(1));
		out.writeDouble(roi.getRadius(0));
		out.writeDouble(roi.getRadius(1));
	}
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		EllipseRegionOfInterest roi = getRegionOfInterest();
		roi.setOrigin(in.readDouble(), 0);
		roi.setOrigin(in.readDouble(), 1);
		roi.setRadius(in.readDouble(), 0);
		roi.setRadius(in.readDouble(), 1);
	}

}
