package opencl;

import ij.IJ;

import java.util.ArrayList;
import java.util.List;

import com.nativelibs4java.opencl.CLDevice;

/*
 * Screens an ArrayList<CLDevice> for fully supported OpenCL devices
 */
public class DeviceValidator {
	
	static final String[] AMD_OK_LIST= {"5970", "5870", "5850", "5770", "5750", "5670", "5570"};
	
	public static ArrayList<CLDevice> getSupportedDevices( ArrayList<CLDevice> inputDeviceList )
	{
		//create a device list for fully supported devices
		ArrayList<CLDevice> checkedDevices = new ArrayList<CLDevice>();
		
		for( CLDevice uncheckedCLDevice : inputDeviceList )
		{
			String deviceName = uncheckedCLDevice.toString();
			
			//Check AMD devices
			if( uncheckedCLDevice.getVendorId() == 16914944  )
			{
				//check if "AMD" is supported
				for( String deviceModelOK: AMD_OK_LIST )
				{
					if(deviceName.contains( deviceModelOK ) )
					{
						checkedDevices.add( uncheckedCLDevice );
					}
				}
			}
			else //Check device 
			{
				checkedDevices.add( uncheckedCLDevice );
			}
			
			//TODO:Check NVidia devices
			
			//TODO:Check Cell devices
			
			//TODO:Check FPGA devices
			
		}
		
		return checkedDevices;
	}

}
