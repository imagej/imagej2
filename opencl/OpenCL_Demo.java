//
// OpenCL_Demo.java
//

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

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

import ij.plugin.filter.PlugInFilter;

/**
 * This plugin demonstrates the capability of OpenCL within ImageJ as a Filter
 * Plugin. Please note that this example is written for ease of understanding
 * rather than for performance reasons.
 * @author Rick Lentz (imagejdev.org)
 */
public class OpenCL_Demo implements PlugInFilter {

  /** Stores the source string information. */
  private static String src = null;

  /** Holds the Image Plus object passed to the setup method. */
  private ImagePlus currentImage = null;

  /** Device to use in processing. */
  private CLDevice device;

  @Override
  public void run(ImageProcessor imageProcessor) {
    IJ.log("Entering OpenCL ImageJ Filter Demo Plugin run method.");

    //Get the image's byte[] from the imageProcessor object
    byte[] imageByteArray = (byte[])
      ((ByteProcessor) imageProcessor.convertToByte(false)).getPixels();

    try {
      if (device == null) {
        // No suitable OpenCL devices were found in the setup() method
        // Run the plugin using the included Java source (at end of file)
        IJ.log("Using Java, no OpenCL devices compiled the source successfully.");

        byte[] outImageByteArray = filter(imageProcessor.getWidth(),
          imageProcessor.getHeight(), imageByteArray);
        imageProcessor.setPixels(outImageByteArray);
      }
      else {
        //Run the sobel filter using OpenCL
        int w = imageProcessor.getWidth();
        int h = imageProcessor.getHeight();
        ByteBuffer results = openCLSobelFilter(w, h, imageByteArray);
        //byte[] testOutput = new byte[w * h];
        //ByteProcessor bp = new ByteProcessor(w, h, testOutput, null);

        byte[] dst = new byte[w * h];
        results.get(dst);
        imageProcessor.setPixels(dst);
        //displayGraphicsInNewJFrame(imageProcessor.getBufferedImage(),
        //  "returned results", 3000);

        //results.get(imageByteArray);
        //imageProcessor.setPixels(imageByteArray);
      }
    }
    catch (Throwable t) {
      IJ.handleException(t);
    }
    currentImage.updateAndDraw();
    IJ.log("Exiting OpenCL ImageJ Filter Demo Plugin run method.");
  }

  @Override
  public int setup(String arg, ImagePlus imp) {
    IJ.log("Entering the OpenCL ImageJ demo setup method.");
    currentImage = imp;

    // use pure Java by default
    device = null;

    //Share the same context if applicable
    //CLContext context = JavaCL.createContextFromCurrentGL();
    List<CLDevice> allDevices = null;
    try {
      //Create a context and program using the devices discovered.
      allDevices = new ArrayList<CLDevice>();
      for (CLPlatform platform : JavaCL.listPlatforms())
        allDevices.addAll(Arrays.asList(platform.listAllDevices(true)));
    }
    catch (Exception e) {
      return DOES_8G + DOES_STACKS;
    }

    for (CLDevice clDevice : allDevices) {
      IJ.log("Found " + clDevice);
    }

    // prompt user for which device to use

    GenericDialog gd = new GenericDialog("OpenCL Devices");
    final String[] dArray = new String[allDevices.size() + 1];
    dArray[0] = "Native Java";
    for (int i=0; i<allDevices.size(); i++) {
      dArray[i + 1] = allDevices.get(i).toString();
    }
    String defaultDevice = dArray.length > 0 ? dArray[1] : dArray[0];
    gd.addChoice("Device", dArray, defaultDevice);
    gd.showDialog();
    if (gd.wasCanceled()) return DOES_8G + DOES_STACKS;
    int index = gd.getNextChoiceIndex();

    device = index > 0 ? allDevices.get(index - 1) : null;

    // try to compile the OpenCL source on the chosen device

    if (device != null) {
      compile(device, "sobel.cl");
    }
    IJ.log("Leaving the OpenCL ImageJ demo setup method.");
    return DOES_8G + DOES_STACKS;
  }

  /**
   * Compiles the given OpenCL source code on the specified device.
   * @return true if the source was successfully compiled
   */
  public static boolean compile(CLDevice device, String resourcePath) {
    //read in the OpenCL source
    InputStream inputStream =
      OpenCL_Demo.class.getResourceAsStream(resourcePath);
    byte[] sourceBytes;

    try {
      sourceBytes = new byte[inputStream.available()];
      inputStream.read(sourceBytes);
      inputStream.close();
      src = new String(sourceBytes);
    }
    catch (IOException e1) {
      IJ.handleException(e1);
      return false;
    }

    IJ.log("Source: " + src);

    if (device.isCompilerAvailable() && device.isAvailable()) {
      try {
        CLPlatform clPlatform = device.getPlatform();
        CLContext context = clPlatform.createContext(null, device);
        CLProgram program = context.createProgram(src).build();
        clPlatform.release();
        context.release();
        IJ.log(device + " has compiled the source.");
      }
      catch (CLBuildException e) {
        // This exception is expected due to differences
        // in supported capabilities
        //IJ.handleException(e);
        IJ.log(device + " does not support the features of the OpenCL program");
        IJ.handleException(e);
        return false;
      }
    }
    else {
      IJ.error(device + " is not available, or does not support OpenCL");
      return false;
    }
    return true;
  }

  /**
   * Computes the Sobel (3x3) using the current host device.
   * @param width - width of the image
   * @param height - height of the image
   * @param input - byte[] containing the 8bit grayscale intensity values
   * @return ByteBuffer containing the computed result
   * @throws IOException
   * @throws CLBuildException
   */
  public ByteBuffer openCLSobelFilter(int width, int height,
    byte[] inputArray) throws IOException, CLBuildException
  {
    //Use the first device
    CLPlatform clPlatform = device.getPlatform();
    CLContext context = clPlatform.createContext(null, device);
    CLQueue queue = context.createDefaultQueue();

    //Start the timer used to capture the time to build and run the OpenCL kernel
    long startTime = System.nanoTime();

    //Build the program
    CLProgram program = context.createProgram(src).build();
    ByteBuffer outputImageByteBuffer = NIOUtils.directBytes(width*height, context.getByteOrder()); 
    ByteBuffer inputByteBuffer = NIOUtils.directBytes(width*height, context.getByteOrder());
    inputByteBuffer.put(inputArray);

    //Allocate memory on the device for input and output (device memory)
    CLByteBuffer input = context.createByteBuffer(CLMem.Usage.Input, inputByteBuffer, true);
    CLByteBuffer output = context.createByteBuffer(CLMem.Usage.InputOutput, width * height);

    //Create the kernel
    CLKernel kernel = program.createKernel("sobel");

    //Call the kernel
    CLEvent kernelCompletion;

    synchronized(kernel)
    {
      kernel.setArgs(input, output, width, height);

      //Run the kernel
      kernelCompletion = kernel.enqueueNDRange(queue,
        new int[] {width * height}, null);
    }

    kernelCompletion.waitFor();
    output.read(queue, outputImageByteBuffer, true);

    //Get the end time for building and running the OpenCL kernel
    long time = System.nanoTime() - startTime;

    //Find and display the performance metrics
    long timePerPixel = time / (width * height);
    String label = "Computed in " + (time / 1000) + " microseconds\n(" +
      timePerPixel + " nanoseconds per pixel)";
    IJ.log(label + " using " + device);

    return outputImageByteBuffer;
  }

  /**
   * Filters using a 3x3 neighborhood. The p1, p2, etc variables, which
   * contain the values of the pixels in the neighborhood, are arranged
   * as follows:
   * <pre>
   * p1 p2 p3
   * p4 p5 p6
   * p7 p8 p9
   * </pre>
   */
  public byte[] filter(int width, int height, byte[] inputImageArray) {
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

  public static void displayGraphicsInNewJFrame(BufferedImage i, String label,
    long millis)
  {
    Dimension preferredSize = new Dimension(i.getWidth(), i.getHeight());
    JFrame f = new JFrame("ij-test Display");
    f.setPreferredSize(preferredSize);

    //f.getContentPane().add("Center",
    //  new JScrollPane(new JLabel(new ImageIcon(image))));
    ImageIcon imageIcon = new ImageIcon(i);

    JLabel jl = new JLabel();

    jl.setIcon(imageIcon);
    //JScrollPane jsp = new JScrollPane(jl);

    f.getContentPane().add("Center", jl);
    f.getContentPane().add("North", new JLabel(label));
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    //f.pack();
    f.setSize(preferredSize);
    f.setVisible(true);
    try {
      Thread.sleep(millis);
    }
    catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
