package imagej.roi.ome;

import imagej.roi.ImageJROI;
import mpicbg.imglib.roi.AbstractIterableRegionOfInterest;
import mpicbg.imglib.roi.RegionOfInterest;
import ome.xml.model.Ellipse;

public class OMEEllipseROI extends OMEShapeROI<Ellipse> implements ImageJROI {
	
	public OMEEllipseROI() {
		omeShape = new Ellipse();
	}
	class OERegionOfInterest extends AbstractIterableRegionOfInterest {
		protected OERegionOfInterest() {
			super(2);
		}
		
		@Override
		protected boolean isMember(double[] position) {
			double x = position[0] - omeShape.getX();
			double y = position[1] - omeShape.getY();
			double a = Math.max(omeShape.getRadiusX(), omeShape.getRadiusY());
			double b = Math.min(omeShape.getRadiusX(), omeShape.getRadiusY());
			return (Math.pow(x, 2) / Math.pow(a, 2)) + (Math.pow(y, 2) / Math.pow(b, 2)) < 1;
		}
		
		@Override
		protected boolean nextRaster(long[] position, long[] end) {
			// TODO Auto-generated method stub
			return false;
		}
	}
	
	@Override
	public RegionOfInterest getRegionOfInterest() {
		return new OERegionOfInterest();
	}
	
	
}
