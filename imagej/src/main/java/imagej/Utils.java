package imagej;

public class Utils
{
	public static double boundToRange(double min, double max, double value)
	{
		if (value < min)
			return min;
		
		if (value > max)
			return max;
		
		return value;
	}
	
	public static boolean insideRange(double min, double max, double value)
	{
		if (value < min)
			return false;
		
		if (value > max)
			return false;
		
		return true;
	}
}
