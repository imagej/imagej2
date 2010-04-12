package opencl;

import ij.IJ;

import java.io.IOException;
import java.util.ArrayList;

import com.nativelibs4java.opencl.CLBuildException;
import com.nativelibs4java.opencl.CLByteBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLDevice;
import com.nativelibs4java.opencl.CLFloatBuffer;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLPlatform;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;

public class OpenCLManager 
{
	private static CLDevice device;

	private long findMaxAvailableMem()
	{
		long max = device.getGlobalMemSize();
		long min = 0;
		long test = (max-min)/2;
		
		CLByteBuffer buff1 = null;
		while( test != min )
		{ 
			try 
			{
				buff1 = this.getContext().createByteBuffer( CLMem.Usage.InputOutput, test );
				//System.out.println("Memory " + test  + "tested successfully ");
				min = test;
				test = (max-min)/2;
				buff1.release();
			} 
			catch (Exception e)
			{
				//System.out.println("Memory " + test  + "failed ");
				max = test;
				test = (max-min)/2;
			} 
		}
			
		return min;
	}
	public long getDeviceAvailableMemory() 
	{		
		return findMaxAvailableMem();
	}

	private static CLPlatform clPlatform;
	private static CLContext context;
	private ArrayList<CLDevice> validatedDeviceList;
	private String[] fileNames;
    private String[] programNames;
    public CLKernel[] kernel;
    
    
    public CLContext getContext() {
		return context;
	}

	private static CLQueue queue;
	public CLQueue getQueue() {
		return queue;
	}

	

	public OpenCLManager() 
	{
		chooseOpenCLDevice();
	}

	/**
	 * Shows a dialog enabling the user to choose a single device.  The method then
	 * sets the Platform, Context, and queue objects
	 */
	public void chooseOpenCLDevice()
	{
		validatedDeviceList = DeviceValidator.getSupportedDevices( new DeviceList().getDevices() );
		device = ChooseOpenCLDevice.displayChoiceDialog( validatedDeviceList );

    	clPlatform = device.getPlatform();
    	context = clPlatform.createContext( null, device );
    	queue = context.createDefaultQueue();
	}
	
	/**
	 * Compiles the sources to programs, assigns the kernel names, 
	 * creates kernels for each program
	 * @param openclsourcearray - input string array of relative source files
	 * @param inprogramNames - OpenCL complaint kernel names
	 * @return
	 * @throws IOException - If the source file is not found
	 * @throws CLBuildException - If the source could not build for the platform
	 */
	public boolean setSource( String[] openclsourcearray, String[] inprogramNames ) 
	throws IOException, CLBuildException
	{
		fileNames = openclsourcearray.clone();
		programNames = inprogramNames.clone();
		
		kernel = new CLKernel[ openclsourcearray.length ];
		
		for( int i = 0; i < fileNames.length; i++ )
		{
			String m = Source.getSourceFromFullPath( fileNames[i] );
			CLProgram t = context.createProgram( m ).build();
			kernel[i] = t.createKernel( inprogramNames[i] );
		}
		
		return true;
	}

}
