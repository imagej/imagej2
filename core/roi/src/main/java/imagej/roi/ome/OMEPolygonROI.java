package imagej.roi.ome;

import imagej.roi.ImageJROI;
import net.imglib2.roi.AbstractIterableRegionOfInterest;
import net.imglib2.roi.RegionOfInterest;
import ome.xml.model.Polyline;

public class OMEPolygonROI extends OMEShapeROI<Polyline> implements ImageJROI {
	
	public OMEPolygonROI() {
		omeShape = new Polyline();
		omeShape.setClosed(true);
	}
	
	class OPRegionOfInterest extends AbstractIterableRegionOfInterest {
		protected OPRegionOfInterest() {
			super(2);
		}
		
		@Override
		protected boolean isMember(double[] position) {
			// TODO
			return false;
		}
		
		@Override
		protected boolean nextRaster(long[] position, long[] end) {
			// TODO Auto-generated method stub
			return false;
		}
	}
	
	@Override
	public RegionOfInterest getRegionOfInterest() {
		return new OPRegionOfInterest();
	}
	
	
}
