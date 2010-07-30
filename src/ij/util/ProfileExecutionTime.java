package ij.util;

/**
 * Simple utility for profiling functions 
 * [Recommend calling function under test a statistically significant
 * number of times and average results.]
 */
public class ProfileExecutionTime
{
	private static long startTime = 0;
	private static long stopTime = 0;
	
	public static ProfileExecutionTime start()
	{
		ProfileExecutionTime profileExecutionTime = new ProfileExecutionTime();
		startTime = System.nanoTime();
		
		return profileExecutionTime;
	}
	
	public long getExecutionNanoSeconds()
	{
		stopTime = System.nanoTime();
		
		return stopTime - startTime;
	}
	
}
