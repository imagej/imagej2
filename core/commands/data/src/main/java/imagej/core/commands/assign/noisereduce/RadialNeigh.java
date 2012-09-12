package imagej.core.commands.assign.noisereduce;

import net.imglib2.ops.condition.Condition;
import net.imglib2.ops.pointset.ConditionalPointSet;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.ops.pointset.PointSet;


public class RadialNeigh implements Neighborhood {

	private final PointSet points;
	private final long radius;
	private final int numDims;
	
	public RadialNeigh(int numDims, long radius) {
		this.numDims = numDims;
		this.radius = radius;
		long[] posOff = new long[numDims];
		long[] negOff = new long[numDims];
		for (int i = 0; i < numDims; i++) {
			posOff[i] = negOff[i] = radius-1;
		}
		PointSet space = new HyperVolumePointSet(new long[numDims], posOff, negOff);
		Condition<long[]> condition =
				new WithinRadiusOfPointCondition(radius, space.getAnchor());
		this.points = new ConditionalPointSet(space, condition);
	}
	
	@Override
	public PointSet getPoints() {
		return points;
	}

	@Override
	public String getDescription() {
		return "" + numDims + " dimensional "+radius+" pixel radial neighborhood";
	}
	
}

