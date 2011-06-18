package ij;

public class ByteCreator {

	// helper - create a list of bytes in ascending order
	public static byte[] ascending(int count)
	{
		byte[] output = new byte[count];
		for (int i = 0; i < count; i++)
			output[i] = (byte)i;
		return output;
	}
	
	// helper - create a list of bytes in descending order
	public static byte[] descending(int count)
	{
		byte[] output = new byte[count];
		for (int i = 0; i < count; i++)
			output[i] = (byte)(count-i-1);
		return output;
	}
	
	// helper - create a list of repeated bytes
	public static byte[] repeated(int count, int value)
	{
		byte[] output = new byte[count];
		for (int i = 0; i < count; i++)
			output[i] = (byte)value;
		return output;
	}
	
	// helper - create a list of random bytes from 0 .. range-1
	public static byte[] random(int count, int range)
	{
		byte[] output = new byte[count];
		for (int i = 0; i < count; i++)
			output[i] = (byte)Math.floor(range*Math.random());
		return output;
	}
	}
