package imagej.core.commands.assign.noisereduce;

import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.ops.pointset.PointSet;


public class RectangularNeigh implements Neighborhood {

	private final long[] posOffsets;
	private final long[] negOffsets;
	private final PointSet points;
	
	public RectangularNeigh(long[] posOffsets, long[] negOffsets) {
		this.posOffsets = posOffsets;
		this.negOffsets = negOffsets;
		long[] origin = new long[posOffsets.length];
		points = new HyperVolumePointSet(origin, posOffsets, negOffsets);
	}
	
	@Override
	public PointSet getPoints() {
		return points;
	}

	@Override
	public String getDescription() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < posOffsets.length; i++) {
			if (i != 0) {
				builder.append(" x ");
			}
			builder.append(1 + posOffsets[i] + negOffsets[i]);
		}
		builder.append(" rectangular neighborhood");
		return builder.toString();
	}
	
}

