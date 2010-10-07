package ij.gui;

import java.util.*;

import mpicbg.imglib.container.array.ArrayContainerFactory;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

import ij.*;
import ij.process.*;
import imagej.SampleInfo;
import imagej.SampleInfo.ValueType;
import imagej.SampleManager;
import imagej.process.ImageUtils;
import imagej.process.ImgLibProcessor;
import imagej.process.TypeManager;
import imagej.process.function.FillUnaryFunction;

/** New image dialog box plus several static utility methods for creating images.*/
public class NewImage
{

	//***************** constants ******************************************************

	public static final int GRAY8=0, GRAY16=1, GRAY32=2, RGB=3;
	public static final int FILL_BLACK=1, FILL_RAMP=2, FILL_WHITE=4, CHECK_AVAILABLE_MEMORY=8, FILL_ZERO = 16;
	private static final int OLD_FILL_WHITE=0;
	
	//***************** static members  ******************************************************
	
    static final String NAME = "new.name";
    static final String TYPE = "new.type";
    static final String FILL = "new.fill";
	static final String WIDTH = "new.width";
	static final String HEIGHT = "new.height";
	static final String SLICES = "new.slices";
	static final String SAMPLE_TYPE = "new.sampleType";

    private static String name = Prefs.getString(NAME, "Untitled");
    private static int width = Prefs.getInt(WIDTH, 400);
    private static int height = Prefs.getInt(HEIGHT, 400);
    private static int slices = Prefs.getInt(SLICES, 1);
    private static int type = Prefs.getInt(TYPE, GRAY8);
    private static int fillWith = Prefs.getInt(FILL, OLD_FILL_WHITE);
    private static String[] fill = {"White", "Black", "Ramp", "Zero"};

    private static String[] oldTypes = {"8-bit", "16-bit", "32-bit", "RGB"};
	private static RealType<?> imgLibType;
	private static SampleInfo sampleType;
	private static String[] sampleNames;

	//***************** static initialization ******************************************************

	static
	{
		String name = Prefs.getString(SAMPLE_TYPE);
		
		sampleType = SampleManager.findSampleInfo(name);
		
		if (sampleType == null)
			sampleType = SampleManager.getSampleInfo(SampleInfo.ValueType.UBYTE);
		
		// TODO - temporary kludge until IJ supports 1-bit images
		if (sampleType.getNumBits() < 8)
			sampleType = SampleManager.getSampleInfo(SampleInfo.ValueType.UBYTE);
	}
	
	static
	{
		sampleNames = new String[9];

		// TODO - expand to support BIT images when the time comes
		sampleNames[0] = getSampleName(SampleInfo.ValueType.BYTE);
		sampleNames[1] = getSampleName(SampleInfo.ValueType.UBYTE);
		sampleNames[2] = getSampleName(SampleInfo.ValueType.SHORT);
		sampleNames[3] = getSampleName(SampleInfo.ValueType.USHORT);
		sampleNames[4] = getSampleName(SampleInfo.ValueType.INT);
		sampleNames[5] = getSampleName(SampleInfo.ValueType.UINT);
		sampleNames[6] = getSampleName(SampleInfo.ValueType.FLOAT);
		sampleNames[7] = getSampleName(SampleInfo.ValueType.LONG);
		sampleNames[8] = getSampleName(SampleInfo.ValueType.DOUBLE);
	}

	//***************** constructor ******************************************************
	
    public NewImage() {
    	openImage();
    }
    
	//***************** private interface  ******************************************************

	private boolean compatibleShowDialog() {
		if (type<GRAY8|| type>RGB)
			type = GRAY8;
		if (fillWith<OLD_FILL_WHITE||fillWith>FILL_RAMP)
			fillWith = OLD_FILL_WHITE;
		GenericDialog gd = new GenericDialog("New Image...", IJ.getInstance());
		gd.addStringField("Name:", name, 12);
		gd.addChoice("Type:", oldTypes, oldTypes[type]);
		gd.addChoice("Fill With:", fill, fill[fillWith]);
		gd.addNumericField("Width:", width, 0, 5, "pixels");
		gd.addNumericField("Height:", height, 0, 5, "pixels");
		gd.addNumericField("Slices:", slices, 0, 5, "");
		gd.showDialog();
		if (gd.wasCanceled())
			return false;
		name = gd.getNextString();
		String s = gd.getNextChoice();
		if (s.startsWith("8"))
			type = GRAY8;
		else if (s.startsWith("16"))
			type = GRAY16;
		else if (s.endsWith("RGB") || s.endsWith("rgb"))
			type = RGB;
		else
			type = GRAY32;
		fillWith = gd.getNextChoiceIndex();
		width = (int)gd.getNextNumber();
		height = (int)gd.getNextNumber();
		slices = (int)gd.getNextNumber();
		return true;
	}

	private static boolean createStack(ImagePlus imp, ImageProcessor ip, int nSlices, int type, int options) {
		int fill = getFill(options);
		int width = imp.getWidth();
		int height = imp.getHeight();
		long bytesPerPixel = 1;
		if (type==GRAY16) bytesPerPixel = 2;
		else if (type==GRAY32||type==RGB) bytesPerPixel = 4;
		long size = (long)width*height*nSlices*bytesPerPixel;
		String size2 = size/(1024*1024)+"MB ("+width+"x"+height+"x"+nSlices+")";
		if ((options&CHECK_AVAILABLE_MEMORY)!=0) {
			long max = IJ.maxMemory(); // - 100*1024*1024;
			if (max>0) {
				long inUse = IJ.currentMemory();
				long available = max - inUse;
				if (size>available)
					System.gc();
				inUse = IJ.currentMemory();
				available = max-inUse;
				if (size>available) {
					IJ.error("Insufficient Memory", "There is not enough free memory to allocate a \n"
					+ size2+" stack.\n \n"
					+ "Memory available: "+available/(1024*1024)+"MB\n"		
					+ "Memory in use: "+IJ.freeMemory()+"\n \n"	
					+ "More information can be found in the \"Memory\"\n"
					+ "sections of the ImageJ installation notes at\n"
					+ "\""+IJ.URL+"/docs/install/\".");
					return false;
				}
			}
		}
		ImageStack stack = imp.createEmptyStack();
		int inc = nSlices/40;
		if (inc<1) inc = 1;
		IJ.showStatus("Allocating "+size2+". Press 'Esc' to abort.");
		IJ.resetEscape();
		try {
			stack.addSlice(null, ip);
			for (int i=2; i<=nSlices; i++) {
				if ((i%inc)==0) IJ.showProgress(i, nSlices);
				Object pixels2 = null;
				switch (type) {
					case GRAY8: pixels2 = new byte[width*height]; break;
					case GRAY16: pixels2 = new short[width*height]; break;
					case GRAY32: pixels2 = new float[width*height]; break;
					case RGB: pixels2 = new int[width*height]; break;
				}
				if (fill!=FILL_BLACK || type==RGB)
					System.arraycopy(ip.getPixels(), 0, pixels2, 0, width*height);
				stack.addSlice(null, pixels2);
				if (IJ.escapePressed()) {IJ.beep(); break;};
			}
		}
		catch(OutOfMemoryError e) {
			IJ.outOfMemory(imp.getTitle());
			stack.trim();
		}
		IJ.showProgress(nSlices, nSlices);
		if (stack.getSize()>1)
			imp.setStack(null, stack);
		return true;
	}

	private static ImagePlus createImagePlus() {
		//ImagePlus imp = WindowManager.getCurrentImage();
		//if (imp!=null)
		//	return imp.createImagePlus();
		//else
		return new ImagePlus();
	}
	
	private boolean currentShowDialog()
	{
		if (type<GRAY8 || type>ImagePlus.IMGLIB)
			throw new IllegalArgumentException("unknown image type "+type);
		
		imgLibType = null;
		
		if ((fillWith < 0) || (fillWith >= fill.length))
			fillWith = 0;

		GenericDialog gd = new GenericDialog("New Image...", IJ.getInstance());
		gd.addStringField("Name:", name, 12);
		gd.addChoice("Type:", sampleNames, sampleType.getName());
		gd.addChoice("Fill With:", fill, fill[fillWith]);
		gd.addNumericField("Width:", width, 0, 5, "pixels");
		gd.addNumericField("Height:", height, 0, 5, "pixels");
		gd.addNumericField("Slices:", slices, 0, 5, "");
		
		gd.showDialog();
		
		if (gd.wasCanceled())
			return false;
		
		name = gd.getNextString();

		String typeName = gd.getNextChoice();
		
		SampleInfo info = SampleManager.findSampleInfo(typeName);
		
		if (info == null)
			throw new IllegalArgumentException("unknown sample type chosen "+typeName);

		// TODO: this works for now until we break mapping of value types to sample types
		imgLibType = SampleManager.getRealType(info.getValueType());
		
		sampleType = info;
		
		type = ImagePlus.IMGLIB;
		
		fillWith = gd.getNextChoiceIndex();
		width = (int)gd.getNextNumber();
		height = (int)gd.getNextNumber();
		slices = (int)gd.getNextNumber();

		return true;
	}

	private static void fill(ImagePlus imp, double value)
	{
		FillUnaryFunction fillFunc = new FillUnaryFunction(value);
		int planeCount = imp.getNSlices();
		for (int plane = 0; plane < planeCount; plane++)
		{
			ImgLibProcessor<?> proc = (ImgLibProcessor<?>)imp.getStack().getProcessor(plane+1);
			proc.transform(fillFunc, null);
		}
	}
	
	private static void fillBlack(ImagePlus imp)
	{
		SampleInfo.ValueType type = SampleManager.getValueType(imp);
		
		SampleInfo info = SampleManager.getSampleInfo(type);
		
		if (info.isIntegral())
		{
			// fill with min
			double min = SampleManager.getRealType(type).getMinValue();

			fill(imp, min);
		}
		else  // floating type
		{
			fill(imp,0);
		}
	}
	
	private static void fillRamp(ImagePlus imp)
	{
		int width = imp.getWidth();
		int height = imp.getHeight();
		
		double[] ramp = new double[width];
		
		SampleInfo.ValueType type = SampleManager.getValueType(imp);
		SampleInfo info = SampleManager.getSampleInfo(type);
		
		// integral data
		if (info.isIntegral())
		{
			// create a ramp from min possible pixel value to max possible pixel value
			RealType<?> realType = SampleManager.getRealType(type);
			double min = realType.getMinValue();
			double max = realType.getMaxValue();
			for (int x = 0; x < width; x++)
			{
				double relSize = ((double)x)/(width-1); 
				ramp[x] = min + (relSize*(max-min));
			}
		}
		else  // floating type data
		{
			// create a ramp between 0 and 1
			for (int x = 0; x < width; x++)
				ramp[x] = ((double)x) / (width-1); 
		}
		
		// set each plane's pixels to the ramp values
		int planeCount = imp.getNSlices();
		for (int plane = 0; plane < planeCount; plane++)
		{
			ImgLibProcessor<?> proc = (ImgLibProcessor<?>)imp.getStack().getProcessor(plane+1);
			for (int y = 0; y < height; y++)
				for (int x = 0; x < width; x++)
					proc.setd(x, y, ramp[x]);
		}
	}
	
	private static void fillWhite(ImagePlus imp)
	{
		SampleInfo.ValueType type = SampleManager.getValueType(imp);
		
		SampleInfo info = SampleManager.getSampleInfo(type);
		
		if (info.isIntegral())
		{
			// fill with max
			double max = SampleManager.getRealType(type).getMaxValue();

			fill(imp,max);
		}
		else  // floating type
		{
			fill(imp,1.0);
		}
	}
	
	private static void fillZero(ImagePlus imp)
	{
		fill(imp, 0);
	}

	private static int getFill(int options)
	{
		int numPresent = 0;

		boolean white = false;
		if ((options == 0) || ((options & FILL_WHITE) > 0))
		{
			white = true;
			numPresent++;
		}

		boolean black = false;
		if ((options & FILL_BLACK) > 0)
		{
			black = true;
			numPresent++;
		}

		boolean ramp = false;
		if ((options & FILL_RAMP) > 0)
		{
			ramp = true;
			numPresent++;
		}

		boolean zero = false;
		if ((options & FILL_ZERO) > 0)
		{
			zero = true;
			numPresent++;
		}

		if (numPresent > 1)
			return FILL_ZERO;
		
		if (white)
			return FILL_WHITE;
		
		if (black)
			return FILL_BLACK;
		
		if (ramp)
			return FILL_RAMP;
		
		return FILL_ZERO;
	}

	private static String getSampleName(SampleInfo.ValueType type)
	{
		return SampleManager.getSampleInfo(type).getName();
	}
	
	private static ImagePlus imglibCreate(String title, int width, int height, int nSlices, RealType<?> type, int options)
	{
		int[] dimensions = new int[]{width, height, nSlices};
		
		ArrayContainerFactory factory = new ArrayContainerFactory();
		factory.setPlanar(true);
		
		Image<?> image = ImageUtils.createImage(type, factory, dimensions);
		
		ImagePlus imp = ImageUtils.createImagePlus(image);
		
		if (imp != null)
		{
			switch (getFill(options))
			{
				case FILL_RAMP:
					fillRamp(imp);
					break;
					
				case FILL_WHITE:
					fillWhite(imp);
					break;
					
				case FILL_BLACK:
					fillBlack(imp);
					break;
					
				default:
					fillZero(imp);
					break;
			}
			
			double min, max;
			
			if ( ! TypeManager.isIntegralType(type))  // float type
			{
				min = 0;
				max = 1;
			}
			else  // integral type
			{
				min = type.getMinValue();
				max = type.getMaxValue();
			}

			imp.getProcessor().setMinAndMax(min, max);
		}
		
		return imp;
	}
	
	private void openImage() {
		if (!showDialog())
			return;
		try
		{
			//open(name, width, height, slices, type, fillWith);
			open(name, width, height, slices, sampleType.getValueType(), fillWith);
		}
		catch(OutOfMemoryError e)
		{
			IJ.outOfMemory("New Image...");
		}
	}

	private boolean showDialog()
	{
		if (Prefs.get("IJ_1.4_Compatible", false))
			return compatibleShowDialog();
		else
			return currentShowDialog();
	}
	
    //***************** public interface ******************************************************
    
	/** create an ImagePlus of specified dimensions backed by old style processors.
	 * @deprecated Use {@link #NewImage.createImage(String title, int width, int height, int nSlices, ValueType type,
	 * int options)} instead.
	 */
	public static ImagePlus createImage(String title, int width, int height, int nSlices, int bitDepth, int options)
	{
		ImagePlus imp = null;
		switch (bitDepth) {
			case 8: imp = createByteImage(title, width, height, nSlices, options); break;
			case 16: imp = createShortImage(title, width, height, nSlices, options); break;
			case 32: imp = createFloatImage(title, width, height, nSlices, options); break;
			case 24: imp = createRGBImage(title, width, height, nSlices, options); break;
			default: throw new IllegalArgumentException("Invalid bitDepth: "+bitDepth);
		}
		return imp;
	}
	
	/** create an ImagePlus of specified type and dimensions. Depending upon the existence of the compatibility preference
	 *  may return 1.4 compatible data and processors. Otherwise returns an imglib aware ImagePlus. 
	 */
	public static ImagePlus createImage(String title, int width, int height, int nSlices, ValueType type, int options)
	{
		ImagePlus imp = null;
		
		if (Prefs.get("IJ_1.4_Compatible", false))  // old data/processors desired
		{
			switch (type)
			{
				case UBYTE:
					imp = createByteImage(title, width, height, nSlices, options); break;
				case USHORT:
					imp = createShortImage(title, width, height, nSlices, options); break;
				case UINT:
					imp = createRGBImage(title, width, height, nSlices, options); break;
				case FLOAT:
					imp = createFloatImage(title, width, height, nSlices, options); break;
				default:
					throw new IllegalArgumentException("createImage(): unsupported image type for old processor compatibility - "+type);
			}
		}
		else // new data/processors desired
		{
			RealType<?> rType = SampleManager.getRealType(type);
			imp = imglibCreate(title, width, height, nSlices, rType, options);
		}
		
		return imp;
	}
	
	/** create an old style byte image with given dimensions
	 * @deprecated Use
	 *  {@link #NewImage.createImage(String title, int width, int height, int nSlices, ValueType type, int options)}
	*/
	public static ImagePlus createByteImage(String title, int width, int height, int slices, int options) {
		int fill = getFill(options);
		byte[] pixels = new byte[width*height];
		switch (fill) {
			case FILL_WHITE:
				for (int i=0; i<width*height; i++)
					pixels[i] = (byte)255;
				break;
			case FILL_BLACK:
				break;
			case FILL_RAMP:
				byte[] ramp = new byte[width];
				for (int i=0; i<width; i++)
					ramp[i] = (byte)((i*256.0)/width);
				int offset;
				for (int y=0; y<height; y++) {
					offset = y*width;
					for (int x=0; x<width; x++)
						pixels[offset++] = ramp[x];
				}
				break;
		}
		ImageProcessor ip = new ByteProcessor(width, height, pixels, null);
		ImagePlus imp = createImagePlus();
		imp.setProcessor(title, ip);
		if (slices>1) {
			boolean ok = createStack(imp, ip, slices, GRAY8, options);
			if (!ok) imp = null;
		}
		return imp;
	}

	/** create an old style float image with given dimensions
	 * @deprecated Use
	 *  {@link #NewImage.createImage(String title, int width, int height, int nSlices, ValueType type, int options)}
	*/
	public static ImagePlus createFloatImage(String title, int width, int height, int slices, int options) {
		int fill = getFill(options);
		float[] pixels = new float[width*height];
		switch (fill) {
			case FILL_WHITE: case FILL_BLACK:
				break;
			case FILL_RAMP:
				float[] ramp = new float[width];
				for (int i=0; i<width; i++)
					ramp[i] = (float)((i*1.0)/width);
				int offset;
				for (int y=0; y<height; y++) {
					offset = y*width;
					for (int x=0; x<width; x++)
						pixels[offset++] = ramp[x];
				}
				break;
		}
	    ImageProcessor ip = new FloatProcessor(width, height, pixels, null);
	    if (fill==FILL_WHITE)
	    	ip.invertLut();
		ImagePlus imp = createImagePlus();
		imp.setProcessor(title, ip);
		if (slices>1) {
			boolean ok = createStack(imp, ip, slices, GRAY32, options);
			if (!ok) imp = null;
		}
		imp.getProcessor().setMinAndMax(0.0, 1.0); // default display range
		return imp;
	}

	/** create an old style rgb image with given dimensions
	 * @deprecated Use
	 *  {@link #NewImage.createImage(String title, int width, int height, int nSlices, ValueType type, int options)}
	*/
	public static ImagePlus createRGBImage(String title, int width, int height, int slices, int options) {
		int fill = getFill(options);
		int[] pixels = new int[width*height];
		switch (fill) {
			case FILL_WHITE:
				for (int i=0; i<width*height; i++)
					pixels[i] = -1;
				break;
			case FILL_BLACK:
				for (int i=0; i<width*height; i++)
					pixels[i] = 0xff000000;
				break;
			case FILL_RAMP:
				int r,g,b,offset;
				int[] ramp = new int[width];
				for (int i=0; i<width; i++) {
					r = g = b = (byte)((i*256.0)/width);
					ramp[i] = 0xff000000 | ((r<<16)&0xff0000) | ((g<<8)&0xff00) | (b&0xff);
				}
				for (int y=0; y<height; y++) {
					offset = y*width;
					for (int x=0; x<width; x++)
						pixels[offset++] = ramp[x];
				}
				break;
		}
		ImageProcessor ip = new ColorProcessor(width, height, pixels);
		ImagePlus imp = createImagePlus();
		imp.setProcessor(title, ip);
		if (slices>1) {
			boolean ok = createStack(imp, ip, slices, RGB, options);
			if (!ok) imp = null;
		}
		return imp;
	}

	/** create an old style short image with given dimensions
	 * @deprecated Use
	 *  {@link #NewImage.createImage(String title, int width, int height, int nSlices, ValueType type, int options)}
	*/
	public static ImagePlus createShortImage(String title, int width, int height, int slices, int options) {
		int fill = getFill(options);
		short[] pixels = new short[width*height];
		switch (fill) {
			case FILL_WHITE: case FILL_BLACK:
				break;
			case FILL_RAMP:
				short[] ramp = new short[width];
				for (int i=0; i<width; i++)
					ramp[i] = (short)(((i*65536.0)/width)+0.5);
				int offset;
				for (int y=0; y<height; y++) {
					offset = y*width;
					for (int x=0; x<width; x++)
						pixels[offset++] = ramp[x];
				}
				break;
		}
	    ImageProcessor ip = new ShortProcessor(width, height, pixels, null);
	    if (fill==FILL_WHITE)
	    	ip.invertLut();
		ImagePlus imp = createImagePlus();
		imp.setProcessor(title, ip);
		if (slices>1) {
			boolean ok = createStack(imp, ip, slices, GRAY16, options);
			if (!ok) imp = null;
		}
		imp.getProcessor().setMinAndMax(0, 65535); // default display range
		return imp;
	}

	/** @deprecated Obsolete. Use
	 *  {@link #NewImage.createImage(String title, int width, int height, int nSlices, ValueType type, int options)}
	*/
	public static ImagePlus createUnsignedShortImage(String title, int width, int height, int slices, int options) {
		return createShortImage(title, width, height, slices, options);
	}

	/** create an ImagePlus with given type and dimensions and open it in a window.
	 * @deprecated Use {@link #NewImage.open(String title, int width, int height, int nSlices, ValueType type,
	 * int options)} instead.
	 */
	public static void open(String title, int width, int height, int nSlices, int type, int options)
	{
		ValueType vType = ValueType.UBYTE;
		
		switch (type)
		{
			case GRAY8:
				vType = ValueType.UBYTE;
				break;
			case GRAY16:
				vType = ValueType.USHORT;
				break;
			case GRAY32:
				vType = ValueType.FLOAT;
				break;
			case RGB:
				vType = ValueType.UINT;
				break;
			default:
				// do nothing but accept default valueType set above
				break;
		}
		
		open(title, width, height, nSlices, vType, options);
	}

	/** create an ImagePlus with given type and dimensions and open it in a window. */
	public static void open(String title, int width, int height, int nSlices, ValueType type, int options)
	{
		long startTime = System.currentTimeMillis();
		ImagePlus imp = createImage(title, width, height, nSlices, type, options);
		if (imp!=null)
		{
			WindowManager.checkForDuplicateName = true;          
			imp.show();
			IJ.showStatus(IJ.d2s(((System.currentTimeMillis()-startTime)/1000.0),2)+" seconds");
		}
	}
	
	/** Called when ImageJ quits. */
	public static void savePreferences(Properties prefs) {
		prefs.put(NAME, name);
		prefs.put(TYPE, Integer.toString(type));
		prefs.put(FILL, Integer.toString(fillWith));
		prefs.put(WIDTH, Integer.toString(width));
		prefs.put(HEIGHT, Integer.toString(height));
		prefs.put(SLICES, Integer.toString(slices));
		prefs.put(SAMPLE_TYPE, sampleType.getName());
	}
}
