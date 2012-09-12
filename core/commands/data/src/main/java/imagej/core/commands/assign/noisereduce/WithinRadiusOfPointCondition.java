package imagej.core.commands.assign.noisereduce;


import net.imglib2.ops.condition.Condition;


public class WithinRadiusOfPointCondition implements Condition<long[]> {

	private final long[] point;
	private final long radius;
	private final long radiusSquared;
	
	public WithinRadiusOfPointCondition(long radius, long[] point) {
		this.point = point;
		this.radius = radius;
		this.radiusSquared = radius * radius;
	}
	
	@Override
	public boolean isTrue(long[] val) {
		/* old debugging code
		if (!Arrays.equals(point,lastPt)) {
			for (int i = 0; i < point.length; i++) {
				lastPt[i] = point[i];
			}
		}
		*/
		long sumSq = 0;
		for (int i = 0; i < val.length; i++) {
			long delta = val[i] - point[i];
			sumSq += delta * delta;
		}
		return sumSq <= radiusSquared;
	}

	@Override
	public WithinRadiusOfPointCondition copy() {
		// NOTE - purposely not cloning point here. Not sure what is best. But this
		// allows one to have numerous conditions all comparing to a single point
		// stored elsewhere. That could or could not be bad depending upon the
		// circumstance. For instance you may have one single point that you want to
		// update in one place and have all conditions respect it. But you also may
		// have the case where you want one point per subspace in which the condition
		// applies (like the center of a neighborhood). There could be multiple
		// subspaces because of copy()'s called by parallelization code. This idea
		// needs to be thought out more carefully. Maybe use a nonparallel assigner
		// in cases like this.
		return new WithinRadiusOfPointCondition(radius, point);
	}
	
}
