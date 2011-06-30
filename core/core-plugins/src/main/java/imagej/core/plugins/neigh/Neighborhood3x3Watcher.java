package imagej.core.plugins.neigh;

/**
 * This interface is implemented by classes who want to do a 3x3 neighborhood
 * operation of some sort
 */
public interface Neighborhood3x3Watcher {

	/**
	 * Called once before the neighborhood iterations take place to allow
	 * implementer to initialize state
	 */
	void setup();

	/**
	 * Called once each time a neighborhood is visited for the first time to
	 * allow implementer to initialize local neighborhood state
	 */
	void initializeNeighborhood(long[] position);

	/**
	 * Called 9 times (3x3), once for each value at a location so implementer
	 * can update state for the local neighborhood
	 */
	void visitLocation(int dx, int dy, double value);

	/**
	 * Called after neighborhood is completely visited. allows implementer to
	 * calculate the output value for that neighborhood
	 */
	double calcOutputValue();
}

