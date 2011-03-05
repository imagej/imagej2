package imagej;

/**
 * TODO
 *
 * @author Barry DeZonia
 */
public class DoubleRange
{
	protected DoubleRange() {}
	
	public static boolean inside(double min, double max, double value)
	{
		return (min <= value) && (value <= max);
	}
	
	public static double bound(double min, double max, double value)
	{
		if (value < min)
			return min;
		
		if (value > max)
			return max;
		
		return value;
	}
}
