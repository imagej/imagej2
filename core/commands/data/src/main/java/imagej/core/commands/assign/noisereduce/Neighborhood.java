package imagej.core.commands.assign.noisereduce;

import net.imglib2.ops.pointset.PointSet;


public interface Neighborhood {
	public PointSet getPoints();
	public String getDescription();
}
