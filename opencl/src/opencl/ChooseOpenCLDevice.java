package opencl;

import java.util.ArrayList;

import com.nativelibs4java.opencl.CLDevice;

import ij.gui.GenericDialog;

public class ChooseOpenCLDevice 
{
	public static CLDevice displayChoiceDialog( ArrayList<CLDevice> deviceList )
	{
		CLDevice device = null;
		GenericDialog gd = new GenericDialog("OpenCL Devices");
		final String[] dArray = new String[deviceList.size() + 1];
		dArray[0] = "Native Java";
		for (int i=0; i<deviceList.size(); i++) 
		{
			dArray[i + 1] = deviceList.get(i).toString();
		}

		String defaultDevice = dArray.length > 0 ? dArray[1] : dArray[0];
		gd.addChoice("Device", dArray, defaultDevice);
		gd.showDialog();

		if (gd.wasCanceled()) 
			return device;

		int index = gd.getNextChoiceIndex();

		device = index > 0 ? deviceList.get(index - 1) : null;

		return device;
	}
}
