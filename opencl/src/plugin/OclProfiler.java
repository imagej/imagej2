package src.plugin;

import java.nio.FloatBuffer;

import src.opencl.OpenCLManager;

import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLFloatBuffer;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.util.NIOUtils;


public class OclProfiler {

	static final String[] openCLsourceArray = {"/Users/Rick/Documents/workspace2/DisplayImage/profile1.cl"};
	static final String[] programNamesArray = { "profile1" };
	
	private static OpenCLManager oclm;
	private static boolean useCL = false;
	
    public boolean isUseOpenCL() {
    	return useCL;
	}
    
    static void initialize()
    {
    	//Get the OpenCL devices and validate them
    	oclm = new OpenCLManager();

    	//Set the source object
    	try 
    	{
    		useCL = oclm.setSource( openCLsourceArray, programNamesArray );
		} 
    	catch (Exception e) 
		{
			System.out.print( "source could not be set " + e.toString() );
		}
    }
    
	 
    public static void main(String[] args) 
	{
		
		System.out.print( OpenCLManager.findMaxAvailableDirectNIOMemory() );	
	}
    
    public float[] testArrayMath()
    {
    	//pick device
		initialize();
		
		//determine device working memory size
		long memoryAvailableBytes = oclm.getDeviceAvailableMemory();
		
		long mem = memoryAvailableBytes/4;

		//create the local float buffer
		FloatBuffer aFloatBuffer = NIOUtils.directFloats( (int) mem/2, oclm.getContext().getByteOrder() );
		FloatBuffer bFloatBuffer = NIOUtils.directFloats( (int) mem/2, oclm.getContext().getByteOrder() );
		
		float[] s = new float[(int) (mem/2)];
		
		aFloatBuffer.put( s );
		bFloatBuffer.put( s );
		
		//create the memory on the card
		CLFloatBuffer aCLFloat = oclm.getContext().createFloatBuffer( CLMem.Usage.InputOutput, aFloatBuffer, true );
		CLFloatBuffer bCLFloat = oclm.getContext().createFloatBuffer( CLMem.Usage.InputOutput, bFloatBuffer, true );
		
		System.out.println("ready to run kernel");
		
		//Allow for blocking execution
		CLEvent kernelCompletion;
		synchronized( oclm.kernel[0] )
		{
			oclm.kernel[0].setArgs( aCLFloat, bCLFloat );
			kernelCompletion = oclm.kernel[0].enqueueNDRange( oclm.getQueue(), new int[]{ (int)mem/2 }, null );
		}  kernelCompletion.waitFor();

		
		System.out.println("kernel complete to run kernel");
		
		//write the data to the host buffer
		aCLFloat.read( oclm.getQueue(), aFloatBuffer, true );
		aFloatBuffer.rewind();
		aFloatBuffer.get( s );
		
		return s;
    }
}
