package plugin;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import opencl.ChooseOpenCLDevice;
import opencl.DeviceList;
import opencl.DeviceValidator;
import opencl.Source;

import com.nativelibs4java.opencl.CLBuildException;
import com.nativelibs4java.opencl.CLByteBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLDevice;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLPlatform;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.JavaCL;
import com.nativelibs4java.util.NIOUtils;

/**
 * A simple PlugInFilter demonstrating the functionality of OpenCL within the current ImageJ
 */
public class OpenCL_Demo implements PlugInFilter
{
    /** Holds the Image Plus object passed to the setup method. */
    private ImagePlus currentImage = null;

    static String OpenCLsource = "/sobel.cl";
    private static CLDevice device;
    private static Source source;

    @Override
    public void run(ImageProcessor imageProcessor)
    {
        int width = imageProcessor.getWidth();
        int height = imageProcessor.getHeight();
        if (device == null || source == null )
        { 
            //use the native java implementation
            byte[] outImageByteArray = filter( width, height,
                    (byte[]) ((ByteProcessor) imageProcessor.convertToByte(false)).getPixels() );
            imageProcessor.setPixels(outImageByteArray);
        }
        else
        {
            //Run the OpenCL sobel filter
            try {
                ByteBuffer bb =  openCLSobelFilter( imageProcessor ) ;
                byte[] dst = new byte[width * height];
                bb.get( dst );
                imageProcessor.setPixels( dst );
            } catch (Exception e) {
                // TODO Auto-generated catch block
                IJ.handleException( e );
            }
        }

        currentImage.updateAndDraw();
    }

    @Override
    public int setup(String arg, ImagePlus imp)
    {
        currentImage = imp;

        //Get the OpenCL devices and validate them
        ArrayList<CLDevice> validatedDevices = DeviceValidator.getSupportedDevices( new DeviceList().getDevices() );

        // prompt user for which device to use
        device = ChooseOpenCLDevice.displayChoiceDialog( validatedDevices );

        //Set the source object
        try {
			source = new Source( OpenCLsource );
		} catch (Exception e) 
		{
			IJ.handleException(e);
			IJ.log( " source " + OpenCLsource + " not found. " + e.toString() );
		}

        return DOES_8G + DOES_STACKS;
    }


    /**
     * Computes the Sobel (3x3) using OpenCL source
     * @throws CLBuildException
     */
    private ByteBuffer openCLSobelFilter( ImageProcessor imageProcessor ) throws CLBuildException
    {
        int width = imageProcessor.getWidth();
        int height = imageProcessor.getHeight();

        CLPlatform clPlatform = device.getPlatform();
        CLContext context = clPlatform.createContext( null, device );
        CLQueue queue = context.createDefaultQueue();

        //Build the program
        CLProgram program = context.createProgram( source.getSource() ).build();

        //Get the image data
        byte[] pixelData = (byte[]) ((ByteProcessor) imageProcessor.convertToByte(false)).getPixels();

        //these host buffers can be replaced by mapping TODO: abstract
        ByteBuffer outputByteBuffer = NIOUtils.directBytes( width*height, context.getByteOrder() ); 
        ByteBuffer inputByteBuffer = NIOUtils.directBytes( width*height, context.getByteOrder() );
        inputByteBuffer.put( pixelData );

        //Allocate memory on the GPU device for input and output
        CLByteBuffer input = context.createByteBuffer( CLMem.Usage.Input, inputByteBuffer, true );
        CLByteBuffer output = context.createByteBuffer( CLMem.Usage.Output, width * height );

        //Create the kernel
        CLKernel kernel = program.createKernel("sobel");

        //Call the kernel
        CLEvent kernelCompletion;

        synchronized(kernel)
        {
            kernel.setArgs(input, output, width, height);

            //Run the kernel
            kernelCompletion = kernel.enqueueNDRange( queue, new int[]{ width, height }, null );
        }

        //Use event object to wait for completion
        kernelCompletion.waitFor();

        //write the data to the host buffer
        CLByteBuffer output2 = output.asCLByteBuffer();
        output2.read( queue, outputByteBuffer, true );

        return outputByteBuffer;
    }


    public byte[] filter(int width, int height, byte[] inputImageArray)
    {
        byte[] pixels = new byte[width*height];
        int p1, p2, p3, p4, p5, p6, p7, p8, p9;
        int offset, sum1, sum2=0, sum=0;
        int rowOffset = width;

        for (int y=1; y<height-1; y++) {
            offset = 1 + y * width;
            for (int x=1; x<width-1; x++) {
                p1 = inputImageArray[offset-rowOffset-1]&0xff;
                p2 = inputImageArray[offset-rowOffset]&0xff;
                p3 = inputImageArray[offset-rowOffset+1]&0xff;
                p4 = inputImageArray[offset-1]&0xff;
                p5 = inputImageArray[offset]&0xff;
                p6 = inputImageArray[offset+1]&0xff;
                p7 = inputImageArray[offset+rowOffset-1]&0xff;
                p8 = inputImageArray[offset+rowOffset]&0xff;
                p9 = inputImageArray[offset+rowOffset+1]&0xff;

                // 3x3 Sobel filter
                sum1 = p1 + 2*p2 + p3 - p7 - 2*p8 - p9;
                sum2 = p1 + 2*p4 + p7 - p3 - 2*p6 - p9;
                sum = (int)Math.sqrt(sum1*sum1 + sum2*sum2);
                if (sum> 255) sum = 255;

                pixels[offset++] = (byte)sum;
            }
        }
        return pixels;
    }
}