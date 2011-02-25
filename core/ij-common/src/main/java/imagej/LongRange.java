package imagej;

/**
 * TODO
 *
 * @author Barry DeZonia
 */
public class LongRange
{
	protected LongRange() {}
	
	public static boolean inside(long min, long max, long value)
	{
		return (min <= value) && (value <= max);
	}
	
	public static long bound(long min, long max, long value)
	{
		if (value < min)
			return min;
		
		if (value > max)
			return max;
		
		return value;
	}
}
