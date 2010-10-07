package ij;

import ij.process.*;
import imagej.PlaneStack;
import imagej.process.ImageUtils;
import imagej.process.ImgLibProcessor;
import imagej.process.Index;
import imagej.process.TypeManager;

import java.awt.image.ColorModel;
import java.awt.Rectangle;
import java.util.ArrayList;

import mpicbg.imglib.container.ContainerFactory;
import mpicbg.imglib.container.array.ArrayContainerFactory;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.integer.ByteType;
import mpicbg.imglib.type.numeric.integer.IntType;
import mpicbg.imglib.type.numeric.integer.LongType;
import mpicbg.imglib.type.numeric.integer.ShortType;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;
import mpicbg.imglib.type.numeric.integer.UnsignedIntType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;
import mpicbg.imglib.type.numeric.real.DoubleType;
import mpicbg.imglib.type.numeric.real.FloatType;

/**
This class represents an expandable array of images.
@see ImagePlus
*/

public class ImageStack {

	static final String outOfRange = "Argument out of range: ";
	private int width, height;
	private Rectangle roi;
	private ColorModel cm;
	private double min=Double.MAX_VALUE;
	private double max;
	private float[] cTable;
	
	private enum OrigProcType {BYTE,SHORT,FLOAT,COLOR,IMGLIB};
	private PlaneStack<?> stack;
	private ArrayList<String> labels;
	private ContainerFactory factory;
	private OrigProcType origProc;
	
	/**
	* Creates a new, empty image stack given width, height, ColorModel, and ContainerFactory. If ContainerFactory is null
	* all planes of ImageStack will be created inside ArrayContainers.
	*/
	public ImageStack(int width, int height, ColorModel cm, ContainerFactory factory)
	{
		this.width = width;
		this.height = height;
		this.cm = cm;
		if (factory == null)
		{
			ArrayContainerFactory f = new ArrayContainerFactory();
			f.setPlanar(true);
			this.factory = f;
		}
		else
			this.factory = factory;
		this.stack = null;
		this.labels = new ArrayList<String>();
		this.origProc = OrigProcType.IMGLIB;
	}
	
	/**
	* Creates a new, empty image stack given width, height, and ColorModel.
	* @deprecated Use {@link #ImageStack(int width, int height, ColorModel cm, ContainerFactory factory)} instead.
	*/
	public ImageStack(int width, int height, ColorModel cm)
	{
		this(width,height,cm,null);
	}

	/**
	* Creates a new, empty image stack given width, and height.
	* @deprecated Use {@link #ImageStack(int width, int height, ColorModel cm, ContainerFactory factory)} instead.
	*/
	public ImageStack(int width, int height)
	{
		this(width, height, null);
	}
	
	/*
	// Default constructor.
	public ImageStack()
	{
		// TODO - this method may only be called in Macro_Runner and I've changed the call there from
		//   "new ImageStack().getClass()" to "ImageStack.class". Might be able to phase out this method.
		
		//throw new OperationNotSupportedException("this method is no longer supported");
		
		this.stack = null;
		this.labels = new ArrayList<String>();
	}
	*/
	
	// TODO - NEW - needed to support ImagePlus::flush()
	//   Note that its side effect of deleting all existing slice labels is not the same as IJ. Since IJ had few constraints
	//   lots of behavior is possible. We must document that ImagePlus::flush() will reinit the stack and labels are lost.
	//   The other possibility is to not allow flush() to do anything to the ImageStack.
	/** Empties the stack discarding all slice data, removing all label info, and freeing up memory. */
	public void reset()
	{
		this.stack = null;
		this.labels = new ArrayList<String>();
	}
	
	/**
	* Creates a new image stack given width and height of planes. Does NOT preallocate stack to given size parameter.
	* @deprecated Use {@link #ImageStack(int width, int height, ColorModel cm, ContainerFactory factory)} instead.
	*/
	public ImageStack(int width, int height, int size)
	{
		this(width,height);  // ignore size. this method is no longer needed
	}

	private int numSlices()
	{
		return labels.size();
	}
	
	private void initStack(boolean unsigned, Object data)
	{
		if (data instanceof byte[])
		{
			if (unsigned)
				stack = new PlaneStack<UnsignedByteType>(this.width, this.height, this.factory);
			else
				stack = new PlaneStack<ByteType>(this.width, this.height, this.factory);
		}
		else if (data instanceof short[])
		{
			if (unsigned)
				stack = new PlaneStack<UnsignedShortType>(this.width, this.height, this.factory);
			else
				stack = new PlaneStack<ShortType>(this.width, this.height, this.factory);
		}
		else if (data instanceof int[])
		{
			if (unsigned)
				stack = new PlaneStack<UnsignedIntType>(this.width, this.height, this.factory);
			else
				stack = new PlaneStack<IntType>(this.width, this.height, this.factory);
		}
		else if (data instanceof long[])
		{
			if (unsigned)
				throw new IllegalArgumentException("ImageStack::initStack(): unsigned long data type not supported");
			else
				stack = new PlaneStack<LongType>(this.width, this.height, this.factory);
		}
		else if (data instanceof float[])
		{
			stack = new PlaneStack<FloatType>(this.width, this.height, this.factory);
		}
		else if (data instanceof double[])
		{
			stack = new PlaneStack<DoubleType>(this.width, this.height, this.factory);
		}
		else
			throw new IllegalArgumentException("ImageStack::initStack(): unknown data type "+data.getClass());
	}
	
	private void addSliceToImage(int atDepth, String label, boolean unsigned, Object pixels)
	{
		// ADD PLANE
		
		if (stack == null)
			initStack(unsigned,pixels);
		
		stack.insertPlane(atDepth, unsigned, pixels);
		
		// UPDATE LABELS
		
		labels.add(atDepth,label);
	}

	private void deleteSliceFromImage(int sliceNumber)  // slice should range from 0 .. numSlices-1
	{
		if (stack == null)
			return;
		
		long numSlices = stack.getNumPlanes();
		
		// 1 plane
		if (numSlices <= 1)  // a one plane image
		{
			stack = null;
			labels.remove(0);
			return;
		}
		
		// 2 or more planes
		
		// remove the label associated with the sliceNumber
		labels.remove(sliceNumber);

		// and then create a smaller image and copy relevant data to it
		stack.deletePlane(sliceNumber);
	}
	
	/**
	* Add a plane of data to the ImageStack.
	* @deprecated Use {@link #addSlice(String sliceLabel, boolean unsigned, Object pixels)} instead.
	*/
	public void addSlice(String sliceLabel, Object pixels)
	{
		addSlice(sliceLabel,true,pixels); // TODO - always true keeps us from supporting signed integral pixel types
	}
	
	/** Adds an image in the form of a pixel array to the end of the stack. Both signed and unsigned data is supported.*/
	public void addSlice(String sliceLabel, boolean dataIsUnsigned, Object pixels)
	{
		if (pixels==null) 
			throw new IllegalArgumentException("'pixels' is null!");

		if (!pixels.getClass().isArray()) 
			throw new IllegalArgumentException("'pixels' is not an array");

		int atPlaneNumber = 0;
		if (stack != null)
			atPlaneNumber = stack.getEndPosition();
		
		addSliceToImage(atPlaneNumber, sliceLabel, dataIsUnsigned, pixels);
	}
	
	/**
	* Obsolete. Short images are always unsigned.
	* @deprecated Use {@link #addSlice(String sliceLabel, boolean unsigned, Object pixels)} instead.
	*/
	public void addUnsignedShortSlice(String sliceLabel, Object pixels)
	{
		addSlice(sliceLabel, pixels);
	}

	private boolean processorIsUnsigned(ImageProcessor ip)
	{
		boolean unsigned = false;
		
		if ((ip instanceof ByteProcessor) || (ip instanceof ShortProcessor) || (ip instanceof ColorProcessor))
			unsigned = true;
		
		if (ip instanceof ImgLibProcessor<?>)
			unsigned = TypeManager.isUnsignedType(((ImgLibProcessor<?>) ip).getType());
		
		return unsigned;
	}
	
	/** Adds the image in 'ip' to the end of the stack. */
	public void addSlice(String sliceLabel, ImageProcessor ip)
	{
		if (ip.getWidth()!=width || ip.getHeight()!=height)
			throw new IllegalArgumentException("Dimensions do not match");
		
		if (numSlices() == 0)
		{
			if (ip instanceof ByteProcessor)
				this.origProc = OrigProcType.BYTE;
			else if (ip instanceof ShortProcessor)
				this.origProc = OrigProcType.SHORT;
			else if (ip instanceof FloatProcessor)
				this.origProc = OrigProcType.FLOAT;
			else if (ip instanceof ColorProcessor)
				this.origProc = OrigProcType.COLOR;
			else if (ip instanceof ImgLibProcessor)
				this.origProc = OrigProcType.IMGLIB;
			else
				throw new IllegalStateException();

			cm = ip.getColorModel();
			min = ip.getMin();
			max = ip.getMax();
		}
		
		boolean unsigned = processorIsUnsigned(ip);
		
		addSlice(sliceLabel, unsigned, ip.getPixels());
	}
	
	/** Adds the image in 'ip' to the stack following slice 'n'. Adds
		the slice to the beginning of the stack if 'n' is zero. */
	public void addSlice(String sliceLabel, ImageProcessor ip, int n)
	{
		if (n<0 || n>numSlices())
			throw new IllegalArgumentException(outOfRange+n);

		boolean unsigned = processorIsUnsigned(ip);

		addSliceToImage(n,sliceLabel,unsigned,ip.getPixels());
	}
	
	/** Deletes the specified slice, were 1<=n<=nslices. */
	public void deleteSlice(int n)
	{
		if (n<1 || n>numSlices())
			throw new IllegalArgumentException(outOfRange+n);
		
		deleteSliceFromImage(n-1);
	}
	
	/** Deletes the last slice in the stack. */
	public void deleteLastSlice()
	{
		if (stack != null)
		{
			int endPos = stack.getEndPosition();
			if (endPos > 0)
				deleteSlice(endPos);
		}
	}
	
    public int getWidth()
    {
    	return width;
    }

    public int getHeight()
    {
    	return height;
    }
    
	public void setRoi(Rectangle roi)
	{
		this.roi = roi;
	}
	
	public Rectangle getRoi()
	{
		if (roi==null)
			return new Rectangle(0, 0, width, height);
		else
			return(roi);
	}
	
	/** Updates this stack so its attributes, such as min, max,
		calibration table and color model, are the same as 'ip'. */
	public void update(ImageProcessor ip)
	{
		if (ip!=null) {
			min = ip.getMin();
			max = ip.getMax();
			cTable = ip.getCalibrationTable();
			cm = ip.getColorModel();
		}
	}
	
	/** Returns the pixel array for the specified slice, were 1<=n<=nslices. */
	public Object getPixels(int n)
	{
		if (n<1 || n>numSlices())
			throw new IllegalArgumentException(outOfRange+n);
		return getProcessor(n).getPixels();
	}
	
	/** Assigns a pixel array to the specified slice,
		were 1<=n<=nslices. */
	public void setPixels(Object pixels, int n)
	{
		if (n<1 || n>numSlices())
			throw new IllegalArgumentException(outOfRange+n);
		if (pixels == null)
			throw new IllegalArgumentException("pixel reference is null!");
		getProcessor(n).setPixels(pixels);
	}
	
	/** Returns the stack as an array of 1D pixel arrays. Note
		that the size of the returned array may be greater than
		the number of slices currently in the stack, with
		unused elements set to null. */
	public Object[] getImageArray()
	{
		if (stack == null)
			return null;

		int planes = (int)stack.getNumPlanes();
		
		Object[] pixelsFromAllSlices = new Object[planes];
		
		for (int i = 0; i < planes; i++)
			pixelsFromAllSlices[i] = getProcessor(i+1).getPixels();
		
		return pixelsFromAllSlices;
	}
	
	/** Returns the number of slices in this stack. */
	public int getSize()
	{
		if (stack == null)
			return 0;
		
		return stack.getEndPosition();
	}

	/** Returns the slice labels as an array of Strings. Note
		that the size of the returned array may be greater than
		the number of slices currently in the stack. Returns null
		if the stack is empty or the label of the first slice is null.  */
	public String[] getSliceLabels()
	{
		if (labels.size() == 0)
			return null;
		else
			return labels.toArray(new String[]{});
	}
	
	/** Returns the label of the specified slice, were 1<=n<=nslices.
		Returns null if the slice does not have a label. For DICOM
		and FITS stacks, labels may contain header information. */
	public String getSliceLabel(int n)
	{
		if (n<1 || n>numSlices())
			throw new IllegalArgumentException(outOfRange+n);
		return labels.get(n-1);
	}
	
	/** Returns a shortened version (up to the first 60 characters or first newline and 
		suffix removed) of the label of the specified slice.
		Returns null if the slice does not have a label. */
	public String getShortSliceLabel(int n)
	{
		String shortLabel = getSliceLabel(n);
		if (shortLabel==null) return null;
    	int newline = shortLabel.indexOf('\n');
    	if (newline==0) return null;
    	if (newline>0)
    		shortLabel = shortLabel.substring(0, newline);
    	int len = shortLabel.length();
		if (len>4 && shortLabel.charAt(len-4)=='.' && !Character.isDigit(shortLabel.charAt(len-1)))
			shortLabel = shortLabel.substring(0,len-4);
		if (shortLabel.length()>60)
			shortLabel = shortLabel.substring(0, 60);
		return shortLabel;
	}

	/** Sets the label of the specified slice, were 1<=n<=nslices. */
	public void setSliceLabel(String label, int n)
	{
		if (n<1 || n>numSlices())
			throw new IllegalArgumentException(outOfRange+n);
		this.labels.set(n-1,label);
	}
	
	/** Returns an ImageProcessor for the specified slice,
		were 1<=n<=nslices. Returns null if the stack is empty.
	*/
	public ImageProcessor getProcessor(int n)
	{
		ImageProcessor ip;
		if (n<1 || n>numSlices())
			throw new IllegalArgumentException(outOfRange+n);
		if (numSlices()==0)
			return null;
		// otherwise if here stack should be non null
		Image<?> image = stack.getStorage();
		ImgLibProcessor<?> proc = new ImgLibProcessor(image, Index.getPlanePosition(image.getDimensions(), n-1));
		if (this.origProc == OrigProcType.IMGLIB)
		{
			ip = proc;
		}
		else  // stack created originally from an older style processor
		{
			switch (this.origProc)
			{
				case BYTE:
					ip = new ByteProcessor(width, height, null, cm);
					break;
				case SHORT:
					ip = new ShortProcessor(width, height, null, cm);
					break;
				case FLOAT:
					ip = new FloatProcessor(width, height, null, cm);
					break;
				case COLOR:
					ip = new ColorProcessor(width, height, null);
					break;
				default:
					throw new IllegalArgumentException("unknown processor type "+this.origProc);
			}
			ip.setPixels(proc.getPixels());
		}
		if (min!=Double.MAX_VALUE && ip!=null && !(ip instanceof ColorProcessor))
			ip.setMinAndMax(min, max);
		if (cTable!=null)
			ip.setCalibrationTable(cTable);
		return ip;
	}
	
	/** Assigns a new color model to this stack. */
	public void setColorModel(ColorModel cm)
	{
		this.cm = cm;
	}
	
	/** Returns this stack's color model. May return null. */
	public ColorModel getColorModel()
	{
		return cm;
	}
	
	/** Returns true if this is a 3-slice RGB stack. */
	public boolean isRGB()
	{
		Image<?> image = null;
		if (stack != null)
			image = stack.getStorage();
		
    	if ((image != null) && (numSlices()==3) && (ImageUtils.getType(image) instanceof UnsignedByteType))
    	{
    		String firstLabel = getSliceLabel(1);
    		if (firstLabel!=null && firstLabel.equals("Red"))	
    			return true;
    	}
    	
		return false;
	}
	
	/** Returns true if this is a 3-slice HSB stack. */
	public boolean isHSB()
	{
    	if (numSlices()==3)
    	{
    		String firstLabel = getSliceLabel(1);
    		if (firstLabel!=null && firstLabel.equals("Hue"))	
    			return true;
    	}
		
    	return false;
	}

	/** Returns true if this is a virtual (disk resident) stack. 
		This method is overridden by the VirtualStack subclass. */
	public boolean isVirtual()
	{
		return false;
	}

	/** Frees memory by deleting a few slices from the end of the stack. */
	public void trim()
	{
		int n = (int)Math.round(Math.log(numSlices())+1.0);
		for (int i=0; i<n; i++) {
			deleteLastSlice();
			System.gc();
		}
	}

	public String toString()
	{
		String v = isVirtual()?"(V)":"";
		return ("stack["+getWidth()+"x"+getHeight()+"x"+getSize()+v+"]");
	}
		
}
