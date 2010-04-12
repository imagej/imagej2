package opencl;

import ij.IJ;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import plugin.OpenCL_Demo;


public class Source 
{
	/** Stores the source string information. */
	private static String src = null; 

	/** Returns the source as a string **/
	public String getSource()
	{
		return src; 
	}

	/**
	 * Given an input path, load the source as a string
	 * @param resourcePath - resource path to the OpenCL file
	 * @throws IOException 
	 */
	public Source( String resourcePath ) throws IOException
	{
		InputStream inputStream = OpenCL_Demo.class.getResourceAsStream( resourcePath );
		byte[] sourceBytes;
		sourceBytes = new byte[ inputStream.available() ];
		inputStream.read(sourceBytes);
		inputStream.close();
		src = new String(sourceBytes);
	}

	public static String getSourceFromFullPath( String resourcePath ) throws IOException 
	{
		FileInputStream fileInputStream = new FileInputStream( resourcePath );
		byte[] sourceBytes;
		sourceBytes = new byte[ fileInputStream.available() ];
		fileInputStream.read(sourceBytes);
		fileInputStream.close();
		return new String(sourceBytes);
	}
}
