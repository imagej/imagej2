package imagej.selection;

/** the SelectionFunction interface implements the single method include(). Implementors of the interface should return true when a specified
 *  position and sample value combination satisfy the implementor's criteria. For example, one could define a SelectionFunction that returns
 *  true for include() if the position is inside some circle and its sample value > 10.0.
 */
public interface SelectionFunction
{
	boolean include(int[] position, double sample);
}

