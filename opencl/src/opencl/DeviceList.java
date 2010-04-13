package src.opencl;

import ij.IJ;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nativelibs4java.opencl.CLDevice;
import com.nativelibs4java.opencl.CLPlatform;
import com.nativelibs4java.opencl.JavaCL;

public class DeviceList 
{
	private static ArrayList<CLDevice> allDevices = null;

	/*
	 * Discover OpenCL devices
	 */
	public DeviceList()
	{
		try {
			//Create a context and program using the devices discovered.
			allDevices = new ArrayList<CLDevice>();
			for (CLPlatform platform : JavaCL.listPlatforms())
				allDevices.addAll( Arrays.asList( platform.listAllDevices( true ) ) );
		}
		catch (Exception e) 
		{
			//TODO handle this exception
		}
	}
	
	public ArrayList<CLDevice> getDevices()
	{
		return allDevices;
	}
}
