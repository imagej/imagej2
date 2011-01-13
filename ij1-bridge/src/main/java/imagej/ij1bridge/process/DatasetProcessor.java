package imagej.ij1bridge.process;

import java.awt.Color;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;
import java.util.Random;

import ij.Prefs;
import ij.measure.Calibration;
import ij.process.Blitter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import imagej.DoubleRange;
import imagej.LongRange;
import imagej.data.DataAccessor;
import imagej.data.Type;
import imagej.dataset.Dataset;
import imagej.dataset.DatasetDuplicator;
import imagej.dataset.FixedDimensionDataset;
import imagej.dataset.PlanarDataset;
import imagej.dataset.PlanarDatasetFactory;
import imagej.function.DoubleFunction;
import imagej.operation.RegionCopyOperation;
import imagej.operation.TransformOperation;
import imagej.process.Index;
import imagej.process.Span;

// TODO
//   Made some changes from old ImgLibProcessor code since this processor does not have to perfectly replicate IJ1 processor behavior
//     - changed get() to not encode float values but casts data to int
//     - changed resetMinAndMax() to always find min and max values (used to always set to 0/255 for unsigned byte data)
//     - set/getBackgroundValue() do nothing but return 0 (was different for unsigned byte type)

/**
 * Implements the ImageProcessor interface for an ImageJ 2.x Dataset. The dataset must be 2-Dimensional.
 */
public class DatasetProcessor extends ImageProcessor
{
	// ************* statics *********************************************************************************************
	
	public static float divideByZeroValue;

	static {
		divideByZeroValue = (float)Prefs.getDouble(Prefs.DIV_BY_ZERO_VALUE, Float.POSITIVE_INFINITY);
		if (divideByZeroValue==Float.MAX_VALUE)
			divideByZeroValue = Float.POSITIVE_INFINITY;
	}
	
	// ************* instance variables ***********************************************************************************

	/** a per thread variable. a position index used to facilitate fast access to the image. */
	private ThreadLocal<int[]> threadLocalPosition =
		new ThreadLocal<int[]>()
		{
			@Override
			protected int[] initialValue() { return new int[2];	}
		};
	
	/** the 2d dataset that this processor references for getting/setting data */
	private Dataset dataset;

	/** a DatasetDuplicator used internally */
	private DatasetDuplicator duplicator;

	/** a PlanarDatasetFactory used internally */
	private PlanarDatasetFactory factory;

	/** used by some methods that fill in default values. only applies to float data */
	private double fillColor;

	/** flag for determining if we are working with float data */
	private boolean isFloat;
	
	/** the largest value actually present in the image */
	private double max;
	
	/** the smallest value actually present in the image */
	private double min;
	
	/** the total number of pixels in the plane referenced by this processor */
	private int numPixels;
	
	/** the 8 bit image created in create8bitImage() - the method and this variable probably belong in base class */
	private byte[] pixels8;
	
	/** the snapshot undo buffer associated with this processor */
	private Dataset snapshot;
	
	/** used by snapshot() and reset(). */
	private double snapshotMin;
	
	/** used by snapshot() and reset(). */
	private double snapshotMax;

	/** the underlying ImageJ type of the plane data */
	private Type type;
	
	// ************* constructor ***********************************************************************************

	/** create a DatasetProcessor that will reference a given Dataset. Dataset must be 2-D. */
	public DatasetProcessor(Dataset dataset)
	{
		int[] dimensions = dataset.getDimensions();
		
		if (dimensions.length != 2)
			throw new IllegalArgumentException("DatasetProcessors are planar and only work with 2-D Datasets. Was given a "+
					dimensions.length+"-D dataset.");
		
		this.dataset = dataset;
		super.width = dimensions[0];
		super.height = dimensions[1];
		this.numPixels = (int)((long)super.width * (long)super.height);
		this.type = dataset.getType();
		this.pixels8 = null;
		this.snapshot = null;
		this.snapshotMin = 0;
		this.snapshotMax = 0;
		this.isFloat = this.type.isFloat();
		this.fillColor = 0;
		this.min = 0;
		this.max = 0;
		this.factory = new PlanarDatasetFactory();
		this.duplicator = new DatasetDuplicator();
		resetRoi();
		// no longer necessary since getMin()/getMax(0 calc as needed
		//findMinAndMax();
	}


	// ************* private interface ***********************************************************************************
	
	/** Uses bilinear interpolation to find the pixel value at real coordinates (x,y). */
	private double calcBilinearPixel(double x, double y)
	{
		int xbase = (int)x;
		int ybase = (int)y;
		double xFraction = x - xbase;
		double yFraction = y - ybase;
		double lowerLeft = getd(xbase,ybase);
		double lowerRight = getd(xbase+1,ybase);
		double upperRight = getd(xbase+1,ybase+1);
		double upperLeft = getd(xbase,ybase+1);
		double upperAverage = upperLeft + xFraction * (upperRight - upperLeft);
		double lowerAverage = lowerLeft + xFraction * (lowerRight - lowerLeft);
		return lowerAverage + yFraction * (upperAverage - lowerAverage);
	}

    private void filter3x3(int type, int[] kernel)
    {
		double v1, v2, v3;           //input pixel values around the current pixel
        double v4, v5, v6;
        double v7, v8, v9;
        double k1=0, k2=0, k3=0;  //kernel values (used for CONVOLVE only)
        double k4=0, k5=0, k6=0;
        double k7=0, k8=0, k9=0;
        double scale = 0;

        if (type==CONVOLVE)
        {
            k1=kernel[0]; k2=kernel[1]; k3=kernel[2];
            k4=kernel[3]; k5=kernel[4]; k6=kernel[5];
		    k7=kernel[6]; k8=kernel[7]; k9=kernel[8];
    		for (int i=0; i<kernel.length; i++)
    			scale += kernel[i];
    		if (scale==0)
    			scale = 1.0;
    		else
    			scale = 1.0/scale; //multiplication factor (multiply is faster than divide)
        }
		
		ProgressTracker tracker = new ProgressTracker(this, ((long)roiWidth)*roiHeight, 25*roiWidth);
		
		Object pixelsCopy = getPixelsCopy();
		DataAccessor accessor = this.type.allocateArrayAccessor(pixelsCopy);
		
        int xEnd = roiX + roiWidth;
        int yEnd = roiY + roiHeight;
		for (int y=roiY; y<yEnd; y++)
		{
			int p  = roiX + y*width;            //points to current pixel
            int p6 = p - (roiX>0 ? 1 : 0);      //will point to v6, currently lower
            int p3 = p6 - (y>0 ? width : 0);    //will point to v3, currently lower
            int p9 = p6 + (y<height-1 ? width : 0); // ...  to v9, currently lower
            v2 = accessor.getReal(p3);
            v5 = accessor.getReal(p6);
            v8 = accessor.getReal(p9);
            if (roiX>0) { p3++; p6++; p9++; }
            v3 = accessor.getReal(p3);
            v6 = accessor.getReal(p6);
            v9 = accessor.getReal(p9);

            double value;
            switch (type) {
                case BLUR_MORE:
    			for (int x=roiX; x<xEnd; x++,p++) {
                    if (x<width-1) { p3++; p6++; p9++; }
    				v1 = v2; v2 = v3;
    				v3 = accessor.getReal(p3);
    				v4 = v5; v5 = v6;
    				v6 = accessor.getReal(p6);
    				v7 = v8; v8 = v9;
    				v9 = accessor.getReal(p9);
    				value =  (v1+v2+v3+v4+v5+v6+v7+v8+v9) / 9.0;
                    setd(p, value);
                }
                break;
                case FIND_EDGES:
    			for (int x=roiX; x<xEnd; x++,p++) {
                    if (x<width-1) { p3++; p6++; p9++; }
    				v1 = v2; v2 = v3;
    				v3 = accessor.getReal(p3);
    				v4 = v5; v5 = v6;
    				v6 = accessor.getReal(p6);
    				v7 = v8; v8 = v9;
    				v9 = accessor.getReal(p9);
                    double sum1 = v1 + 2*v2 + v3 - v7 - 2*v8 - v9;
                    double sum2 = v1  + 2*v4 + v7 - v3 - 2*v6 - v9;
                    value = (double)Math.sqrt(sum1*sum1 + sum2*sum2);
                    setd(p, value);
                }
                break;
                case CONVOLVE:
    			for (int x=roiX; x<xEnd; x++,p++) {
                    if (x<width-1) { p3++; p6++; p9++; }
    				v1 = v2; v2 = v3;
    				v3 = accessor.getReal(p3);
    				v4 = v5; v5 = v6;
    				v6 = accessor.getReal(p6);
    				v7 = v8; v8 = v9;
    				v9 = accessor.getReal(p9);
                    double sum = k1*v1 + k2*v2 + k3*v3
                              + k4*v4 + k5*v5 + k6*v6
                              + k7*v7 + k8*v8 + k9*v9;
                    value = sum * scale;
                    setd(p, value);
                }
                break;
			}
            
            tracker.update();
    	}
		
		tracker.done();
    }

	/** find the minimum and maximum values present in this plane of the image data */
	private void findMinAndMax()
	{
		double tmpMin = Double.MAX_VALUE;
		double tmpMax = -Double.MAX_VALUE;
		
		for (int x = 0; x < super.width; x++)
		{
			for (int y = 0; y < super.height; y++)
			{
				double value = this.getd(x, y);
				
				if (value < tmpMin)
					tmpMin = value;
				
				if (value > tmpMax)
					tmpMax = value;
			}
		}

		setMinAndMaxOnly(tmpMin, tmpMax);
		
		super.minMaxSet = true;
	}

	/** returns the coordinates of the ROI origin as an int[] */
	private int[] originOfRoi()
	{
		return Index.create(super.roiX, super.roiY, new int[]{});
	}

	/** sets the min and max variables associated with this processor and does nothing else! */
	private void setMinAndMaxOnly(double min, double max)
	{
		this.min = min;
		this.max = max;
	}

	/** sets the pixels of the image from provided FloatProcessor's pixel values */
	private void setPixelsFromFloatProc(FloatProcessor proc)
	{
		for (int x = 0; x < super.width; x++)
		{
			for (int y = 0; y < super.height; y++)
			{
				float value = proc.getf(x, y);
				this.setf(x, y, value);
			}
		}
	}
	
	/** returns the span of the ROI plane as an int[] */
	private int[] spanOfRoiPlane()
	{
		return Span.singlePlane(super.roiWidth, super.roiHeight, 2);
	}

	// ************* public interface ***********************************************************************************

	/** Replaces each pixel in the current ROI area of the current plane of data with its absolute value. */
	@Override
	public void abs()
	{
		if (this.type.isUnsigned())
			return;
		
		if (this.isFloat)
		{
			for (int x = super.roiX; x < (super.roiX+super.roiWidth); x++)
			{
				for (int y = super.roiY; y < (super.roiY+super.roiHeight); y++)
				{
					double value = getd(x, y);
					if (value < 0)
						setd(x, y, -value);
				}
			}
		}
		else // else integer data
		{
			for (int x = super.roiX; x < (super.roiX+super.roiWidth); x++)
			{
				for (int y = super.roiY; y < (super.roiY+super.roiHeight); y++)
				{
					long value = getl(x, y);
					if (value < 0)
						setl(x, y, -value);
				}
			}
		}
			
	}
	
	/** Adds 'value' to each pixel in the image or ROI. */
	@Override
	public void add(int value)
	{
		add((double) value);
	}
	
	/** Adds 'value' to each pixel in the image or ROI. */
	@Override
	public void add(double value)
	{
		for (int x = super.roiX; x < (super.roiX+super.roiWidth); x++)
		{
			for (int y = super.roiY; y < (super.roiY+super.roiHeight); y++)
			{
				double newValue = getd(x, y) + value;
				setd(x, y, newValue);
			}
		}
	}
	
	/** Binary AND of each pixel in the image or ROI with 'value'. */
	@Override
	public void and(int value)
	{
		if (this.isFloat)
			return;
		
		for (int x = super.roiX; x < (super.roiX+super.roiWidth); x++)
		{
			for (int y = super.roiY; y < (super.roiY+super.roiHeight); y++)
			{
				long pix = getl(x, y);
				setl(x, y, (pix & (long)value));
			}
		}
	}

	/** Does a lut substitution on current ROI area image data. Applies only to integral data. Note that given table
	 *  should be of the correct size for the pixel type. It should be constructed in such a fashion that the minimum
	 *  pixel value maps to the first table entry and the maximum pixel value to the last. This allows signed integral
	 *  types to have lookup tables also.
	 */
	@Override
	public void applyTable(int[] lut)
	{
		if (this.isFloat)
			return;

		long expectedLutSize = (1L << this.getBitDepth());
		
		if (lut.length != expectedLutSize)
			throw new IllegalArgumentException("lut size ("+lut.length+" values) does not match range of pixel type ("+expectedLutSize+" values)");

		int minValue = (int) this.type.getMinIntegral();
		
		for (int x = 0; x < super.width; x++)
		{
			for (int y = 0; y < super.height; y++)
			{
				int newValue = lut[this.get(x, y) - minValue];
				
				this.set(x, y, newValue);
			}
		}
		
		findMinAndMax();
	}

	/** runs super class autoThreshold() for integral data */
	@Override
	public void autoThreshold()
	{
		if (!this.isFloat)
			super.autoThreshold();
	}

	/**  Convolves the current image plane data with the provided kernel. */
	@Override
	public void convolve(float[] kernel, int kernelWidth, int kernelHeight)
	{
		// general convolve method for simplicity

		FloatProcessor fp = toFloat(0, null);

		fp.setRoi(getRoi());

		new ij.plugin.filter.Convolver().convolve(fp, kernel, kernelWidth, kernelHeight);

		setPixelsFromFloatProc(fp);
	}
	
	/** Convolves the current ROI area data with the provided 3x3 kernel. */
	@Override
	public void convolve3x3(int[] kernel)
	{
		filter3x3(CONVOLVE, kernel);
	}

	/** uses a blitter to copy pixels to xloc,yloc from ImageProcessor ip using the given mode. */
	@Override
	public void copyBits(ImageProcessor ip, int xloc, int yloc, int mode)
	{
		Rectangle r1, r2;
		int srcIndex, dstIndex;
		int xSrcBase, ySrcBase;
		Object source;
		DataAccessor srcAccessor;

		int srcWidth = ip.getWidth();
		int srcHeight = ip.getHeight();
		r1 = new Rectangle(srcWidth, srcHeight);
		r1.setLocation(xloc, yloc);
		r2 = new Rectangle(width, height);
		if (!r1.intersects(r2))
			return;
		source = ip.getPixels();
		srcAccessor = this.type.allocateArrayAccessor(source);
		r1 = r1.intersection(r2);
		xSrcBase = (xloc<0)?-xloc:0;
		ySrcBase = (yloc<0)?-yloc:0;
		boolean useDBZValue = !Float.isInfinite(divideByZeroValue);
		ProgressTracker tracker = new ProgressTracker(this, ((long)r1.width)*r1.height, 20*srcHeight);
		for (int y=r1.y; y<(r1.y+r1.height); y++) {
			srcIndex = (y-yloc)*srcWidth + (r1.x-xloc);
			dstIndex = y * width + r1.x;
			switch (mode) {
				case Blitter.COPY: case Blitter.COPY_INVERTED:
					for (int i=r1.width; --i>=0;)
					{
						if (this.isFloat)
						{
							double value = srcAccessor.getReal(srcIndex++);
							setd(dstIndex++, value);
						}
						else // integral
						{
							long value = srcAccessor.getIntegral(srcIndex++);
							setl(dstIndex++, value);
						}
						tracker.update();
					}
					break;
				case Blitter.COPY_ZERO_TRANSPARENT:
					for (int i=r1.width; --i>=0;)
					{
						if (this.isFloat)
						{
							double value = srcAccessor.getReal(srcIndex++);
							if (value == 0)
								dstIndex++;
							else
								setd(dstIndex++, value);
						}
						else // integral
						{
							long value = srcAccessor.getIntegral(srcIndex++);
							if (value == 0)
								dstIndex++;
							else
								setd(dstIndex++, value);
						}
						tracker.update();
					}
					break;
				case Blitter.ADD:
					for (int i=r1.width; --i>=0; srcIndex++, dstIndex++)
					{
						if (this.isFloat)
						{
							double srcValue = srcAccessor.getReal(srcIndex);
							double myValue = getd(dstIndex);
							setd(dstIndex, srcValue + myValue);
						}
						else  // integral
						{
							long srcValue = srcAccessor.getIntegral(srcIndex);
							long myValue = getl(dstIndex);
							setl(dstIndex, srcValue + myValue);
						}
						tracker.update();
					}
					break;
				case Blitter.AVERAGE:
					for (int i=r1.width; --i>=0; srcIndex++, dstIndex++)
					{
						if (this.isFloat)
						{
							double srcValue = srcAccessor.getReal(srcIndex);
							double myValue = getd(dstIndex);
							setd(dstIndex, (srcValue + myValue) / 2);
						}
						else  // integral
						{
							long srcValue = srcAccessor.getIntegral(srcIndex);
							long myValue = getl(dstIndex);
							setl(dstIndex, (srcValue + myValue) / 2);
						}
						tracker.update();
					}
					break;
				case Blitter.DIFFERENCE:
					for (int i=r1.width; --i>=0; srcIndex++, dstIndex++)
					{
						if (this.isFloat)
						{
							double srcValue = srcAccessor.getReal(srcIndex);
							double myValue = getd(dstIndex);
							double newValue = myValue - srcValue;
							if (newValue < 0) newValue = -newValue;
							setd(dstIndex, newValue);
						}
						else  // integral
						{
							long srcValue = srcAccessor.getIntegral(srcIndex);
							long myValue = getl(dstIndex);
							long newValue = myValue - srcValue;
							if (newValue < 0) newValue = -newValue;
							setl(dstIndex, newValue);
						}
						tracker.update();
					}
					break;
				case Blitter.SUBTRACT:
					for (int i=r1.width; --i>=0; srcIndex++, dstIndex++)
					{
						if (this.isFloat)
						{
							double srcValue = srcAccessor.getReal(srcIndex);
							double myValue = getd(dstIndex);
							double newValue = myValue - srcValue;
							setd(dstIndex, newValue);
						}
						else  // integral
						{
							long srcValue = srcAccessor.getIntegral(srcIndex);
							long myValue = getl(dstIndex);
							long newValue = myValue - srcValue;
							setl(dstIndex, newValue);
						}
						tracker.update();
					}
				case Blitter.MULTIPLY:
					for (int i=r1.width; --i>=0; srcIndex++, dstIndex++)
					{
						if (this.isFloat)
						{
							double srcValue = srcAccessor.getReal(srcIndex);
							double myValue = getd(dstIndex);
							double newValue = myValue * srcValue;
							setd(dstIndex, newValue);
						}
						else  // integral
						{
							long srcValue = srcAccessor.getIntegral(srcIndex);
							long myValue = getl(dstIndex);
							long newValue = myValue * srcValue;
							setl(dstIndex, newValue);
						}
						tracker.update();
					}
					break;
				case Blitter.DIVIDE:
					for (int i=r1.width; --i>=0; srcIndex++, dstIndex++)
					{
						if (this.isFloat)
						{
							double srcValue = srcAccessor.getReal(srcIndex);
							if (useDBZValue && srcValue == 0)
								setd(dstIndex, divideByZeroValue);
							else
							{
								double myValue = getd(dstIndex);
								double newValue = myValue / srcValue;
								setd(dstIndex, newValue);
							}
							tracker.update();
						}
						else  // integral
						{
							long srcValue = srcAccessor.getIntegral(srcIndex);
							long myValue = getl(dstIndex);
							if (srcValue == 0)
							{
								if (myValue > 0)
									setl(dstIndex, this.type.getMaxIntegral());
								else if (myValue < 0)
									setl(dstIndex, this.type.getMinIntegral());
								// else myValue == 0 -> nothing to do
							}
							else  // src value is valid
							{
								long newValue = myValue / srcValue;
								setl(dstIndex, newValue);
							}
							tracker.update();
						}
					}
					break;
				case Blitter.AND:
					for (int i=r1.width; --i>=0; srcIndex++, dstIndex++)
					{
						long srcValue = srcAccessor.getIntegral(srcIndex);
						long myValue = getl(dstIndex);
						long newValue = srcValue & myValue;
						setl(dstIndex, newValue);
						tracker.update();
					}
					break;
				case Blitter.OR:
					for (int i=r1.width; --i>=0; srcIndex++, dstIndex++)
					{
						long srcValue = srcAccessor.getIntegral(srcIndex);
						long myValue = getl(dstIndex);
						long newValue = srcValue | myValue;
						setl(dstIndex, newValue);
						tracker.update();
					}
					break;
				case Blitter.XOR:
					for (int i=r1.width; --i>=0; srcIndex++, dstIndex++)
					{
						long srcValue = srcAccessor.getIntegral(srcIndex);
						long myValue = getl(dstIndex);
						long newValue = srcValue ^ myValue;
						setl(dstIndex, newValue);
						tracker.update();
					}
					break;
				case Blitter.MIN:
					for (int i=r1.width; --i>=0; srcIndex++, dstIndex++)
					{
						if (this.isFloat)
						{
							double srcValue = srcAccessor.getReal(srcIndex);
							double myValue = getd(dstIndex);
							double newValue = (srcValue < myValue) ? srcValue : myValue;
							setd(dstIndex, newValue);
						}
						else  // integral
						{
							long srcValue = srcAccessor.getIntegral(srcIndex);
							long myValue = getl(dstIndex);
							long newValue = (srcValue < myValue) ? srcValue : myValue;
							setl(dstIndex, newValue);
						}
						tracker.update();
					}
					break;
				case Blitter.MAX:
					for (int i=r1.width; --i>=0; srcIndex++, dstIndex++)
					{
						if (this.isFloat)
						{
							double srcValue = srcAccessor.getReal(srcIndex);
							double myValue = getd(dstIndex);
							double newValue = (srcValue > myValue) ? srcValue : myValue;
							setd(dstIndex, newValue);
						}
						else  // integral
						{
							long srcValue = srcAccessor.getIntegral(srcIndex);
							long myValue = getl(dstIndex);
							long newValue = (srcValue > myValue) ? srcValue : myValue;
							setl(dstIndex, newValue);
						}
						tracker.update();
					}
					break;
			}
		}
		
		tracker.done();
	}

	/** required method. used in createImage(). creates an 8bit image from our image data of another type. */
	@Override
	protected byte[] create8BitImage()
	{
		if (this.pixels8 == null)
			this.pixels8 = new byte[this.numPixels];

		double min = this.getMin();
		double max = this.getMax();
		
		for (int i = 0; i < this.numPixels; i++)
		{
			double value = getd(i);  // TODO - due to precision loss this method may display some longs a little inaccurately. probably not a problem.
			
			double relPos = (value - min) / (max - min);
			
			if (relPos < 0) relPos = 0;
			if (relPos > 1) relPos = 1;
			
			this.pixels8[i] = (byte) Math.round(255 * relPos);
		}

		return this.pixels8;
	}

	/** creates a java.awt.Image from super class variables */
	@Override
	public Image createImage()
	{
		boolean firstTime = this.pixels8==null;
		if (firstTime || !super.lutAnimation)
			create8BitImage();
		if (super.cm==null)
			makeDefaultColorModel();
		if (super.source==null)
		{
			super.source = new MemoryImageSource(super.width, super.height, super.cm, this.pixels8, 0, super.width);
			super.source.setAnimated(true);
			super.source.setFullBufferUpdates(true);
			super.img = Toolkit.getDefaultToolkit().createImage(super.source);
		}
		else if (super.newPixels)
		{
			super.source.newPixels(this.pixels8, super.cm, 0, super.width);
			super.newPixels = false;
		}
		else
			super.source.newPixels();

		super.lutAnimation = false;
		return super.img;
	}

	/** creates a new DatasetProcessor of desired height and width. copies some state from this processor. as a side effect it creates
	 *  a new 2D Dataset that is owned by the processor and is available via proc.getDataset().
	*/
	@Override
	public ImageProcessor createProcessor(int width, int height)
	{
		Dataset newDataset = this.factory.createDataset(this.type, new int[]{width, height});

		ImageProcessor proc = new DatasetProcessor(newDataset);
		
		proc.setColorModel(getColorModel());
		proc.setMinAndMax(getMin(), getMax());
		proc.setInterpolationMethod(super.interpolationMethod);
		
		return proc;
	}

	/** creates a DatasetProcessor on a new Dataset whose size and contents match the current ROI area. */
	@Override
	public ImageProcessor crop()
	{
		int[] srcOrigin = originOfRoi();
		int[] span = spanOfRoiPlane();
		
		Dataset newDataset = this.factory.createDataset(this.type, span);
		
		RegionCopyOperation copier =
			new RegionCopyOperation(this.dataset, srcOrigin, newDataset, Index.create(2), span, this.isFloat);
		
		copier.execute();
		
		return new DatasetProcessor(newDataset);
	}

	/** does a filter operation vs. min or max as appropriate. applies to UnsignedByte data only.*/
	@Override
	public void dilate()
	{
		// IJ1 - only supported with ByteProcessor and then needs binary image. There exist general erode/dilate algorithms. implement later
		throw new UnsupportedOperationException("not yet implemented");
	}

	/** set the pixel at x,y to the fill/foreground value. if x,y outside clip region does nothing. */
	@Override
	public void drawPixel(int x, int y)
	{
		if (x>=super.clipXMin && x<=super.clipXMax && y>=super.clipYMin && y<=super.clipYMax)
		{
			setd(x,y,this.fillColor);
		}
	}

	/** creates a processor of the same size and sets its pixel values to this processor's current plane data */
	@Override
	public ImageProcessor duplicate()
	{
		Dataset outputDataset = this.duplicator.createDataset(this.factory, this.dataset);
		
		return new DatasetProcessor(outputDataset);
	}

	/** encodes pixel info into a passed in 4 element integer array. Called by ImagePlus::getPixel() */
	@Override
	public void encodePixelInfo(int[] destination, int x, int y)
	{
		int bitDepth = this.getBitDepth();

		if (this.isFloat)
		{
			switch (bitDepth)
			{
				case 32:
					destination[0] = Float.floatToIntBits(this.getf(x, y));
					break;
				case 64:
					double value = getd(x, y);
					long encoding = Double.doubleToLongBits(value);
					destination[0] = (int)(encoding & 0xffffffffL);
					destination[1] = (int)((encoding >>> 32) & 0xffffffffL);
					break;
				default:  // don't know what it is
					throw new IllegalStateException("unsupported floating point bitDepth : "+bitDepth);
			}
		}
		else // integral type
		{
			if (bitDepth <= 32)
				destination[0] = this.get(x, y);
			else if (bitDepth <= 64)
			{
				long value = getl(x, y);
				destination[0] = (int)(value & 0xffffffffL);
				destination[1] = (int)((value >>> 32) & 0xffffffffL);
			}
			else  // don't know what it is
				throw new IllegalStateException("unsupported integral bitDepth : "+bitDepth);
		}
	}

	/** does a filter operation vs. min or max as appropriate. applies to UnsignedByte data only.*/
	@Override
	public void erode()
	{
		// IJ1 - only supported with ByteProcessor and then needs binary image. There exist general erode/dilate algorithms. implement later
		throw new UnsupportedOperationException("not yet implemented");
	}

	/** Performs a exponential transform on the image or ROI. */
	@Override
	public void exp()
	{
		if (this.isFloat)
		{
			for (int x = super.roiX; x < (super.roiX+super.roiWidth); x++)
			{
				for (int y = super.roiY; y < (super.roiY+super.roiHeight); y++)
				{
					double value = getd(x, y);
					
					setd(x, y, Math.exp(value));
				}
			}
		}
		else  // integral
		{
			double currMax = getMax();
			
			for (int x = super.roiX; x < (super.roiX+super.roiWidth); x++)
			{
				for (int y = super.roiY; y < (super.roiY+super.roiHeight); y++)
				{
					long pixValue = getl(x, y);
					
					long newValue = (long)(Math.exp(pixValue*(Math.log(currMax)/currMax)));
				
					newValue = LongRange.bound(this.type.getMinIntegral(), this.type.getMaxIntegral(), newValue);
					
					setl(x, y, newValue);
				}
			}
		}
	}

	/** apply the FILL point operation over the current ROI area of the current plane of data */
	@Override
	public void fill()
	{
		for (int x = super.roiX; x < (super.roiX+super.roiWidth); x++)
		{
			for (int y = super.roiY; y < (super.roiY+super.roiHeight); y++)
			{
				setd(x, y, this.fillColor);
			}
		}
	}

	/** fills the current ROI area of the current plane of data with the fill color wherever the input mask is nonzero */
	@Override
	public void fill(ImageProcessor mask)
	{
		if (mask==null)
		{
			fill();
			return;
		}
		
		int roiWidth=this.roiWidth;
		int roiHeight=this.roiHeight;
		int roiX=this.roiX;
		int roiY=this.roiY;
		
		if (mask.getWidth()!=roiWidth||mask.getHeight()!=roiHeight)
			return;
		
		byte[] mpixels = (byte[])mask.getPixels();
		
		for (int y=roiY, my=0; y<(roiY+roiHeight); y++, my++)
		{
			int i = y * width + roiX;
			int mi = my * roiWidth;
			
			for (int x=roiX; x<(roiX+roiWidth); x++)
			{
				if (mpixels[mi++]!=0)
					setd(i, this.fillColor);
				i++;
			}
		}
	}

    /** run specified filter on current ROI area of current plane data */
	@Override
	public void filter(int type)
	{
		filter3x3(type, null);
	}

	/** swap the rows of the current ROI area about its central row */
	@Override
	public void flipVertical()
	{
		Rectangle roi = getRoi();
		
		int x,y,mirrorY;
		
		int halfY = roi.height/2;
		
		for (int yOff = 0; yOff < halfY; yOff++)
		{
			y = roi.y + yOff;
			
			mirrorY = roi.y + roi.height - yOff - 1;

			for (int xOff = 0; xOff < roi.width; xOff++)
			{
				x = roi.x + xOff;
				
				if (this.isFloat)
				{
					double tmp = getd(x, y);
					setd(x, y, getd(x, mirrorY));
					setd(x, mirrorY, tmp);
				}
				else // integral
				{
					long tmp = getl(x, y);
					setl(x, y, getl(x, mirrorY));
					setl(x, mirrorY, tmp);
				}
			}
		}
	}

	/** apply the GAMMA point operation over the current ROI area of the current plane of data */
	@Override
	public void gamma(double constant)
	{
		if (this.isFloat)
		{
			for (int x = super.roiX; x < (super.roiX+super.roiWidth); x++)
			{
				for (int y = super.roiY; y < (super.roiY+super.roiHeight); y++)
				{
					double pixValue = getd(x, y);
					
					double newValue;
					if (pixValue <= 0)
						newValue = 0;
					else
						newValue = Math.exp(constant * Math.log(pixValue));
					
					setd(x, y, newValue);
				}
			}
		}
		else  // integral
		{
			double currMin = getMin();
			double currMax = getMax();
			
			double currRange = currMax - currMin;
			
			for (int x = super.roiX; x < (super.roiX+super.roiWidth); x++)
			{
				for (int y = super.roiY; y < (super.roiY+super.roiHeight); y++)
				{
					long pixValue = getl(x, y);
					
					long newValue;
					if ((currRange <= 0) || (pixValue <= currMin))
						newValue = pixValue;
					else
						newValue = (long) (Math.exp(constant * Math.log((pixValue-currMin)/currRange)) * currRange + currMin);
					
					newValue = LongRange.bound(this.type.getMinIntegral(), this.type.getMaxIntegral(), newValue);

					if (newValue != pixValue)
						setd(x, y, newValue);
				}
			}
		}
	}
	
	// TODO - for IJ1 float type processor this one gets using intToFloatBits. we will not mirror this functionality.
	//   We could test if our dataset is float and if so then calc get with intToFloatBits()) but then acts differently if int or float.
	//   Think about this further.
	/** get the pixel value at x,y as an int. */
	@Override
	public int get(int x, int y)
	{
		return (int) getl(x,y);
	}

	/** get the pixel value at index as an int. */
	@Override
	public int get(int index)
	{
		int x = index % super.width;
		int y = index / super.width;
		return get(x, y);
	}

	/** get the current background value. always 0 if not UnsignedByte data */
	@Override
	public double getBackgroundValue()
	{
		return 0;
	}

	/** This method is from Chapter 16 of "Digital Image Processing:
	An Algorithmic Introduction Using Java" by Burger and Burge
	(http://www.imagingbook.com/). */
	@Override
	public double getBicubicInterpolatedPixel(double x0, double y0, ImageProcessor ip2)
	{
		int u0 = (int) Math.floor(x0);	//use floor to handle negative coordinates too
		int v0 = (int) Math.floor(y0);
		if (u0<=0 || u0>=super.width-2 || v0<=0 || v0>=super.height-2)
			return ip2.getBilinearInterpolatedPixel(x0, y0);
		double q = 0;
		for (int j = 0; j <= 3; j++)
		{
			int v = v0 - 1 + j;
			double p = 0;
			for (int i = 0; i <= 3; i++)
			{
				int u = u0 - 1 + i;
				p = p + ip2.getd(u,v) * cubic(x0 - u);  // we override base class impl so that we can use getd() here for improved accuracy
			}
			q = q + p * cubic(y0 - v);
		}
		return q;
	}

	/** return the bit depth of the pixels associated with the processor's Dataset */
	@Override
	public int getBitDepth()
	{
		return this.type.getNumBitsData();
	}

	/** return the bytes per pixel associated with the processor's Dataset. Note that this value does not represent memory or disk usage
	 * but just bytes of information contained in a pixel. Returns a double to support non-byte aligned pixel types (i.e. 12-bit == 1.5
	 * bytes per pixel). */
	@Override
	public double getBytesPerPixel()
	{
		return getBitDepth() / 8.0;
	}

	/** get the pixel value at x,y as an double. */
	@Override
	public double getd(int x, int y)
	{
		int[] pos = this.threadLocalPosition.get();
		pos[0] = x;
		pos[1] = y;
		return this.dataset.getDouble(pos);
	}

	/** get the pixel value at index as an double. */
	@Override
	public double getd(int index)
	{
		int x = index % super.width;
		int y = index / super.width;
		return getd(x, y);
	}

	/** get the Dataset associated with the processor */
	// not an override
	public Dataset getDataset()
	{
		return new FixedDimensionDataset(this.dataset);  // prevent users from making changes to shape of Dataset. we always expect 2-D.
	}
	
	/** get the pixel value at x,y as an float. */
	@Override
	public float getf(int x, int y)
	{
		return (float) getd(x, y);
	}

	/** get the pixel value at index as an float. */
	@Override
	public float getf(int index)
	{
		int x = index % super.width;
		int y = index / super.width;
		return getf(x, y);
	}

	/** calculate the histogram of the current ROI area of the current plane. Note that this only works for integral data
	 * with bit depth <= 16. The bit depth limitation is enforced to keep ImageJ from allocating a HUGE table. The returned
	 * histogram keeps counts stored from minimum values to maximum values. For unsigned types this works as normal. For
	 * signed types the table indices are thus biased by the minimum allowed pixel value.
	 */
	@Override
	public int[] getHistogram()
	{
		if (this.isFloat)
			return null;
		
		int bitDepth = this.getBitDepth();
		
		// limit size so we don't try to allocate HUGE table
		if (bitDepth > 16)
			return null;
		
		int[] histogram = new int[1 << bitDepth];

		long minValue = this.type.getMinIntegral();
		
		for (int x = 0; x < super.width; x++)
		{
			for (int y = 0; y < super.height; y++)
			{
				long pixValue = this.getl(x, y);
				histogram[(int)(pixValue-minValue)]++;
			}
		}
		
		return histogram;
	}

	/** returns an interpolated pixel value from double coordinates using current interpolation method */
	@Override
	public double getInterpolatedPixel(double x, double y)
	{
		if (super.interpolationMethod == BICUBIC)
			return getBicubicInterpolatedPixel(x, y, this);
		else
		{
			if (x < 0.0) x = 0.0;
			if (x >= super.width-1.0) x = super.width-1.001;
			if (y < 0.0) y = 0.0;
			if (y >= super.height-1.0) y = super.height-1.001;
			return calcBilinearPixel(x, y);
		}
	}

	/** get the pixel value at x,y as a long. */
	@Override
	public long getl(int x, int y)
	{
		int[] pos = this.threadLocalPosition.get();
		pos[0] = x;
		pos[1] = y;
		return this.dataset.getLong(pos);
	}

	/** get the pixel value at index as an long. */
	@Override
	public long getl(int index)
	{
		int x = index % super.width;
		int y = index / super.width;
		return getl(x, y);
	}

	/** returns the maximum data value currently present in the dataset plane */
	@Override
	public double getMax()
	{
		if (!super.minMaxSet)
			findMinAndMax();
		
		return this.max;
	}

	/** returns the theoretical maximum possible sample value for this type of processor */
	@Override
	public double getMaximumAllowedValue()
	{
		return this.type.getMaxReal();
	}

	/** returns the minimum data value currently present in the dataset plane */
	@Override
	public double getMin()
	{
		if (!super.minMaxSet)
			findMinAndMax();
		
		return this.min;
	}

	/** returns the theoretical minimum possible sample value for this type of processor */
	@Override
	public double getMinimumAllowedValue()
	{
		return this.type.getMinReal();
	}

	/** returns the pixel at x,y as an int. if coords are out of bounds returns 0. */
	@Override
	public int getPixel(int x, int y)
	{
		if ((x >= 0) && (x < super.width) && (y >= 0) && (y < super.height))
			return get(x, y);
		return 0;
	}

	/** given x,y, double coordinates this returns an interpolated pixel using the current interpolations methods. unlike getInterpolatedPixel()
	 *  which assumes you're doing some kind of interpolation, this method will return nearest neighbors when no interpolation selected. note
	 *  that it returns as an int so float values are encoded as int bits.
	 */
	@Override
	public int getPixelInterpolated(double x, double y)
	{
		if (super.interpolationMethod == BILINEAR)
		{
			if (x<0.0 || y<0.0 || x>=super.width-1 || y>=super.height-1)
				return 0;
			else if (this.isFloat)
				return Float.floatToIntBits((float)getInterpolatedPixel(x, y));
			else
				return (int) Math.round(getInterpolatedPixel(x, y));
		}
		else if (interpolationMethod==BICUBIC)
		{
			if (this.isFloat)
				return Float.floatToIntBits((float)getBicubicInterpolatedPixel(x, y, this));

			double value = getInterpolatedPixel(x, y) + 0.5;
			
			value = LongRange.bound(this.type.getMinIntegral(), this.type.getMaxIntegral(), (long)value);
			
			return (int)value;
		}
		else
			return getPixel((int)(x+0.5), (int)(y+0.5));
	}

	/** returns the pixel values of the current Dataset's plane as an array of the appropriate type. If the Dataset supports
	 * primitive access the actual data reference is returned. Otherwise a copy of the Dataset's pixel values are returned.
	 */
	@Override
	public Object getPixels()
	{
		Object data = this.dataset.getData();
		
		if (data != null)
			return data;
		
		System.out.println("DatasetProcessor::getPixels() - nonoptimal data use - returning pixel values by copy rather than by reference");
		
		// make a copy of the pixels
		
		Dataset twoDimDataset = this.duplicator.createDataset(this.factory, this.dataset);

		return twoDimDataset.getData();
	}

	/** returns a copy of some pixels as an array of the appropriate type. depending upon super class variable "snapshotCopyMode"
	 *  it will either copy current plane data or it will copy current snapshot data.
	 */
	@Override
	public Object getPixelsCopy()
	{
		if (this.snapshot!=null && getSnapshotCopyMode())
		{
			setSnapshotCopyMode(false);

			Dataset copy = this.duplicator.createDataset(this.factory, this.snapshot);
			
			return copy.getData();
		}
		else
		{
			Dataset copy = this.duplicator.createDataset(this.factory, this.dataset);
			
			return copy.getData();
		}
	}

	/** returns the pixel value at x,y as a float. if x,y, out of bounds returns 0. */
	@Override
	public float getPixelValue(int x, int y)
	{
		// make sure its in bounds
		if ((x >= 0) && (x < super.width) && (y >= 0) && (y < super.height))
			return getf(x, y);

		return 0f;
	}

	/** returns a copy of the pixels in the current snapshot */
	@Override
	public Object getSnapshotPixels()
	{
		if (this.snapshot == null)
			return null;
		
		Dataset copy = this.duplicator.createDataset(this.factory, this.snapshot);
		
		return copy.getData();
	}

	// TODO - change GenericStatistics to handle both double and long data. Or create two types of ImageStatistics for
	//   the two types (integer and real) and call correct one here.
	/** get the type appropriate ImageStatistics for the processor. For all DatasetProcessors return GenericStatistics.
	 * This could result in precision loss for long data.
	 */
	@Override
	public ImageStatistics getStatistics(int mOptions, Calibration cal)
	{
		return new GenericStatistics(this, mOptions, cal);
	}

	/** returns the name of the type of this processor (i.e. "8-bit unsigned", "32-bit float", etc.) */ 
	@Override
	public String getTypeName()
	{
		return this.type.getName();
	}

	/** Inverts the image or ROI. */
	@Override
	public void invert()
	{
		double min = getMin();
		double max = getMax();
		
		for (int x = super.roiX; x < (super.roiX+super.roiWidth); x++)
		{
			for (int y = super.roiY; y < (super.roiY+super.roiHeight); y++)
			{
				double pix = getd(x, y);
				double newValue = max - (pix - min);
				setd(x, y, newValue);
			}
		}
	}
	
	/** returns true if the data type of the underlying Dataset is represented with floating point values */
	@Override
	public boolean isFloatingType()
	{
		return this.type.isFloat();
	}

	/** returns true if the data type of the underlying Dataset is represented with unsigned values */
	@Override
	public boolean isUnsignedType()
	{
		return this.type.isUnsigned();
	}

	/** Performs a log transform on the image or ROI. */
	@Override
	public void log()
	{
		if (this.isFloat)
		{
			for (int x = super.roiX; x < (super.roiX+super.roiWidth); x++)
			{
				for (int y = super.roiY; y < (super.roiY+super.roiHeight); y++)
				{
					double value = getd(x, y);
					
					if (value <= 0)
						value = 0;
					
					setd(x, y, Math.log(value));
				}
			}
		}
		else  // integral
		{
			double currMax = getMax();
			
			for (int x = super.roiX; x < (super.roiX+super.roiWidth); x++)
			{
				for (int y = super.roiY; y < (super.roiY+super.roiHeight); y++)
				{
					long pixValue = getl(x, y);
					
					long newValue;
					if (pixValue <= 0)
						newValue = 0;
					else 
						newValue = (long)(Math.log(pixValue)*(currMax/Math.log(currMax)));
			
					newValue = LongRange.bound(this.type.getMinIntegral(), this.type.getMaxIntegral(), newValue);
					
					setl(x, y, newValue);
				}
			}
		}
	}

	/** Pixels greater than 'value' are set to 'value'. */
	@Override
	public void max(double value)
	{
		for (int x = super.roiX; x < (super.roiX+super.roiWidth); x++)
		{
			for (int y = super.roiY; y < (super.roiY+super.roiHeight); y++)
			{
				double pix = getd(x, y);
				if (pix > value)
					pix = value;
				setd(x, y, pix);
			}
		}
	}

	/** run the MEDIAN_FILTER on current ROI area of current plane data. only applies to UnsignedByte data */
	@Override
	public void medianFilter()
	{
		// IJ1 - only supported with ByteProcessor. We could do a general case median filter here. implement later
		throw new UnsupportedOperationException("not yet implemented");
	}

	/** Pixels less than 'value' are set to 'value'. */
	@Override
	public void min(double value)
	{
		for (int x = super.roiX; x < (super.roiX+super.roiWidth); x++)
		{
			for (int y = super.roiY; y < (super.roiY+super.roiHeight); y++)
			{
				double pix = getd(x, y);
				if (pix < value)
					pix = value;
				setd(x, y, pix);
			}
		}
	}

	/** Multiplies each pixel in the image or ROI by 'value'. */
	@Override
	public void multiply(double value)
	{
		for (int x = super.roiX; x < (super.roiX+super.roiWidth); x++)
		{
			for (int y = super.roiY; y < (super.roiY+super.roiHeight); y++)
			{
				double pix = getd(x, y);
				setd(x, y, pix * value);
			}
		}
	}
	
	private class AddNoiseDoubleFunction implements DoubleFunction
	{
		private boolean dataIsIntegral;
		private double min;
		private double max;
		private double range;
		private Random rnd;
		
		public AddNoiseDoubleFunction(boolean isIntegral, double min, double max, double range)
		{
			this.dataIsIntegral = isIntegral;
			this.min = min;
			this.max = max;
			this.range = range;
			this.rnd = new Random();
			this.rnd.setSeed(System.currentTimeMillis());  // TODO - this line not present in original code (by design???)
		}

		public int getParameterCount()
		{
			return 1;
		}
		
		public double compute(double[] input)
		{
			double origValue = input[0];
			double result, ran;
			boolean inRange = false;
			do {
				ran = this.rnd.nextGaussian() * this.range;
				if (this.dataIsIntegral)
					ran = Math.round(ran);
				result = origValue + ran;
				inRange = DoubleRange.inside(this.min, this.max, result);
			} while (!inRange);
			return result;
		}
	}

	/** add noise to the current ROI area of current plane data. */
	@Override
	public void noise(double range)
	{
		// TODO - this method works in doubles regardless of backing type. Long data may suffer precision loss.
		
		AddNoiseDoubleFunction function = new AddNoiseDoubleFunction(!this.isFloat, this.type.getMinReal(), this.type.getMaxReal(), range);
		
		TransformOperation op = new TransformOperation(function, new Dataset[]{this.dataset}, new int[][]{originOfRoi()}, spanOfRoiPlane(), 0);
		
		op.execute();
	}

	/** Binary OR of each pixel in the image or ROI with 'value'. */
	@Override
	public void or(int value)
	{
		if (this.isFloat)
			return;
		
		for (int x = super.roiX; x < (super.roiX+super.roiWidth); x++)
		{
			for (int y = super.roiY; y < (super.roiY+super.roiHeight); y++)
			{
				long pix = getl(x, y);
				setl(x, y, pix | (long)value);
			}
		}
	}
	
	/** set the pixel at x,y to the given int value. if float data value is a float encoded as an int.
	 *  if x,y, out of bounds do nothing.
	 *  @deprecated use {@link DatasetProcessor::putPixelValue(int x, int y, double value)} instead. */
	@Deprecated
	@Override
	public void putPixel(int x, int y, int value)
	{
		if (x>=0 && x<super.width && y>=0 && y<super.height)
		{
			if (this.isFloat)
				setf(x, y, Float.intBitsToFloat(value));
			else
			{
				// can't use setd()/setl() effectively here since input value is already an int - breaks UINT32 and LONG

				value = (int)LongRange.bound(this.type.getMinIntegral(), this.type.getMaxIntegral(), value);
				set(x, y, value);
			}
		}
	}

	/** set the pixel at x,y to the given double value. if integral data the value is biased by 0.5 do force rounding.
	 *  if x,y, out of bounds do nothing.
	 */
	@Override
	public void putPixelValue(int x, int y, double value)
	{
		if (x>=0 && x<super.width && y>=0 && y<super.height)
		{
			if (!this.isFloat)
				value = Math.round(value);

			setd(x, y, value);
		}
	}

	/** sets the current plane data to that stored in the snapshot */
	@Override
	public void reset()
	{
		if (this.snapshot!=null)
		{
			int[] origin = new int[2];
			
			RegionCopyOperation copier =
				new RegionCopyOperation(this.snapshot, origin, this.dataset, origin, this.snapshot.getDimensions(), this.isFloat);
			
			copier.execute();
			
			this.min = this.snapshotMin;
			this.max = this.snapshotMax;
			
			this.minMaxSet = true;
		}
	}

	/** sets the current ROI area data to that stored in the snapshot wherever the mask is nonzero */
	@Override
	public void reset(ImageProcessor mask)
	{
		if (mask==null || this.snapshot==null)
			return;
		
		if (mask.getWidth()!=roiWidth||mask.getHeight()!=roiHeight)
			throw new IllegalArgumentException(maskSizeError(mask));
		
		byte[] mpixels = (byte[])mask.getPixels();
		int[] pos = new int[2];
		for (int y=roiY, my=0; y<(roiY+roiHeight); y++, my++)
		{
			int i = y * width + roiX;
			int mi = my * roiWidth;
			for (int x=roiX; x<(roiX+roiWidth); x++)
			{
				if (mpixels[mi++]==0)
				{
					pos[0] = x;
					pos[1] = y;
					
					if (this.isFloat)
					{
						double snapPix = this.snapshot.getDouble(pos);
						setd(x, y, snapPix);
					}
					else // integral
					{
						long snapPix = this.snapshot.getLong(pos);
						setd(x, y, snapPix);
					}
				}
				i++;
			}
		}
	}

	/** calculates actual min and max values present and resets the threshold */
	@Override
	public void resetMinAndMax()
	{
		findMinAndMax();
		resetThreshold();
	}
	
	/** create a new processor that has specified dimensions. populate it's data with interpolated pixels from this processor's ROI area data. */
	@Override 
	public ImageProcessor resize(int dstWidth, int dstHeight)
	{
		if (roiWidth==dstWidth && roiHeight==dstHeight)
			return crop();

		double srcCenterX = roiX + roiWidth/2.0;
		double srcCenterY = roiY + roiHeight/2.0;

		double dstCenterX = dstWidth/2.0;
		double dstCenterY = dstHeight/2.0;

		double xScale = (double)dstWidth/roiWidth;
		double yScale = (double)dstHeight/roiHeight;

		if (interpolationMethod!=NONE)
		{
			dstCenterX += xScale/2.0;
			dstCenterY += yScale/2.0;
		}

		ImageProcessor ip2 = createProcessor(dstWidth, dstHeight);

		ProgressTracker tracker = new ProgressTracker(this, ((long)dstHeight)*dstWidth, 30*dstWidth);

		double xs, ys;
		if (interpolationMethod==BICUBIC)
		{
			for (int y=0; y<=dstHeight-1; y++)
			{
				ys = (y-dstCenterY)/yScale + srcCenterY;
				int index = y*dstWidth;
				for (int x=0; x<=dstWidth-1; x++)
				{
					xs = (x-dstCenterX)/xScale + srcCenterX;
					double value = getBicubicInterpolatedPixel(xs, ys, this);
					if (!this.isFloat)
					{
						value += 0.5;
						value = LongRange.bound(this.type.getMinIntegral(), this.type.getMaxIntegral(), (long)value);
					}
					ip2.setd(index++, value);
					tracker.update();
				}
			}
		}
		else
		{  // not BICUBIC
			double xlimit = super.width-1.0, xlimit2 = super.width-1.001;
			double ylimit = super.height-1.0, ylimit2 = super.height-1.001;
			int index1, index2;
			for (int y=0; y<=dstHeight-1; y++)
			{
				ys = (y-dstCenterY)/yScale + srcCenterY;
				if (interpolationMethod==BILINEAR)
				{
					if (ys<0.0) ys = 0.0;
					if (ys>=ylimit) ys = ylimit2;
				}
				index1 = super.width*(int)ys;
				index2 = y*dstWidth;
				for (int x=0; x<=dstWidth-1; x++)
				{
					xs = (x-dstCenterX)/xScale + srcCenterX;
					if (interpolationMethod==BILINEAR)
					{
						if (xs<0.0) xs = 0.0;
						if (xs>=xlimit) xs = xlimit2;
						double value = getInterpolatedPixel(xs, ys);
						if (!this.isFloat)
						{
							value += 0.5;
							value = LongRange.bound(this.type.getMinIntegral(), this.type.getMaxIntegral(), (long)value);
						}
						ip2.setd(index2++, value);
					}
					else  // interp == NONE
					{
						double value = getd(index1+(int)xs);
						ip2.setd(index2++, value);
					}
					tracker.update();
				}
			}
		}
		tracker.done();
		return ip2;
	}

	/** rotates current ROI area pixels by given angle. destructive. interpolates pixel values as needed. */
	@Override
	public void rotate(double angle)
	{
		// avoid work when rotating by some multiple of 360.0
		if (Math.floor(angle/360.0) == (angle/360.0))
			return;

		boolean isIntegral = !this.isFloat;

		ImageProcessor ip2 = this.duplicate();

		if (interpolationMethod==BICUBIC)
			ip2.setBackgroundValue(getBackgroundValue());

		double centerX = roiX + (roiWidth-1)/2.0;
		double centerY = roiY + (roiHeight-1)/2.0;
		int xMax = roiX + roiWidth - 1;

		double angleRadians = -angle/(180.0/Math.PI);
		double ca = Math.cos(angleRadians);
		double sa = Math.sin(angleRadians);
		double tmp1 = centerY*sa-centerX*ca;
		double tmp2 = -centerX*sa-centerY*ca;
		double tmp3, tmp4, xs, ys;
		int index, ixs, iys;
		double dwidth=super.width, dheight=super.height;
		double xlimit = super.width-1.0, xlimit2 = super.width-1.001;
		double ylimit = super.height-1.0, ylimit2 = super.height-1.001;

		ProgressTracker tracker = new ProgressTracker(this, ((long)roiWidth)*roiHeight, 30*roiWidth);

		if (interpolationMethod==BICUBIC)
		{
			for (int y=roiY; y<(roiY + roiHeight); y++)
			{
				index = y*super.width + roiX;
				tmp3 = tmp1 - y*sa + centerX;
				tmp4 = tmp2 + y*ca + centerY;
				for (int x=roiX; x<=xMax; x++)
				{
					xs = x*ca + tmp3;
					ys = x*sa + tmp4;
					double value = getBicubicInterpolatedPixel(xs, ys, ip2);
					if (isIntegral)
					{
						value += 0.5;
						value = LongRange.bound(this.type.getMinIntegral(), this.type.getMaxIntegral(), (long)value);
					}
					setd(index++,value);
					tracker.update();
				}
			}
		}
		else
		{
			for (int y=roiY; y<(roiY + roiHeight); y++)
			{
				index = y*super.width + roiX;
				tmp3 = tmp1 - y*sa + centerX;
				tmp4 = tmp2 + y*ca + centerY;
				for (int x=roiX; x<=xMax; x++)
				{
					xs = x*ca + tmp3;
					ys = x*sa + tmp4;
					if ((xs>=-0.01) && (xs<dwidth) && (ys>=-0.01) && (ys<dheight))
					{
						if (interpolationMethod==BILINEAR)
						{
							if (xs<0.0) xs = 0.0;
							if (xs>=xlimit) xs = xlimit2;
							if (ys<0.0) ys = 0.0;
							if (ys>=ylimit) ys = ylimit2;
							double value = ip2.getInterpolatedPixel(xs, ys);
							if (isIntegral)
							{
								value += 0.5;
								value = LongRange.bound(this.type.getMinIntegral(), this.type.getMaxIntegral(), (long)value);
							}
							setd(index++, value);
						}
						else
						{
							ixs = (int)(xs+0.5);
							iys = (int)(ys+0.5);
							if (ixs >= super.width) ixs = super.width - 1;
							if (iys >= super.height) iys = super.height - 1;
							setd(index++, ip2.getd(ixs,iys));
						}
					}
					else
					{
						double value = 0;
						if (isIntegral)
							value = this.getBackgroundValue();
						setd(index++,value);
					}
					tracker.update();
				}
			}
		}

		tracker.done();
	}

	/** scale current ROI area in x and y by given factors. destructive. calculates interpolated pixels as needed */
	@Override
	public void scale(double xScale, double yScale)
	{
		if ((xScale == 1.0) && (yScale == 1.0))
			return;
		
		boolean isIntegral = !this.isFloat;

		double xCenter = roiX + roiWidth/2.0;
		double yCenter = roiY + roiHeight/2.0;

		int xmin, xmax, ymin, ymax;
		if ((xScale>1.0) && (yScale>1.0))
		{
			//expand roi
			xmin = (int)(xCenter-(xCenter-roiX)*xScale);
			if (xmin < 0) xmin = 0;

			xmax = xmin + (int)(roiWidth*xScale) - 1;
			if (xmax >= super.width) xmax = super.width - 1;

			ymin = (int)(yCenter-(yCenter-roiY)*yScale);
			if (ymin < 0) ymin = 0;

			ymax = ymin + (int)(roiHeight*yScale) - 1;
			if (ymax >= super.height) ymax = super.height - 1;
		}
		else
		{
			xmin = roiX;
			xmax = roiX + roiWidth - 1;
			ymin = roiY;
			ymax = roiY + roiHeight - 1;
		}

		ImageProcessor ip2 = this.duplicate();
		
		boolean checkCoordinates = (xScale < 1.0) || (yScale < 1.0);
		int index1, index2, xsi, ysi;
		double ys, xs;

		ProgressTracker tracker = new ProgressTracker(this, ((long)roiWidth)*roiHeight, 30*roiWidth);

		if (interpolationMethod==BICUBIC)
		{
			for (int y=ymin; y<=ymax; y++)
			{
				ys = (y-yCenter)/yScale + yCenter;
				int index = y*super.width + xmin;
				for (int x=xmin; x<=xmax; x++)
				{
					xs = (x-xCenter)/xScale + xCenter;
					double value = getBicubicInterpolatedPixel(xs, ys, ip2);
					if (isIntegral)
					{
						value += 0.5;
						value = LongRange.bound(this.type.getMinIntegral(), this.type.getMaxIntegral(), (long)value);
					}
					setd(index++,value);
					tracker.update();
				}
			}
		}
		else
		{
			double min = this.getMin();
			double xlimit = super.width-1.0, xlimit2 = super.width-1.001;
			double ylimit = super.height-1.0, ylimit2 = super.height-1.001;
			for (int y=ymin; y<=ymax; y++)
			{
				ys = (y-yCenter)/yScale + yCenter;
				ysi = (int)ys;
				if (ys<0.0) ys = 0.0;
				if (ys>=ylimit) ys = ylimit2;
				index1 = y*super.width + xmin;
				index2 = super.width*(int)ys;
				for (int x=xmin; x<=xmax; x++)
				{
					xs = (x-xCenter)/xScale + xCenter;
					xsi = (int)xs;
					if (checkCoordinates && ((xsi<xmin) || (xsi>xmax) || (ysi<ymin) || (ysi>ymax)))
					{
						setd(index1++, min);
					}
					else  // interpolated pixel within bounds of image
					{
						if (interpolationMethod==BILINEAR)
						{
							if (xs<0.0) xs = 0.0;
							if (xs>=xlimit) xs = xlimit2;
							double value = ip2.getInterpolatedPixel(xs, ys);
							if (isIntegral)
							{
								value += 0.5;
								value = LongRange.bound(this.type.getMinIntegral(), this.type.getMaxIntegral(), (long)value);
							}
							setd(index1++, value);
						}
						else  // interpolation type of NONE
						{
							setd(index1++, ip2.getd(index2+xsi));
						}
					}
					tracker.update();
				}
			}
		}

		tracker.done();
	}

	// TODO - for IJ1 float type processor this one sets using getFloatBits. we will not mirror this functionality.
	//   We could test if our dataset is float and if so then call setf(getFloatBits(value)) but then acts differently if int or float.
	//   Think about this further.
	/** set the pixel at x,y to provided int value. */
	@Override
	public void set(int x, int y, int value)
	{
		int[] pos = this.threadLocalPosition.get();
		pos[0] = x;
		pos[1] = y;
		this.dataset.setDouble(pos, value);
	}

	/** set the pixel at index to provided int value. */
	@Override
	public void set(int index, int value)
	{
		// TODO - for performance - cache a DataAccessor with this processor. Update it every time setPixels() done.
		//   Then use it here to access pixels avoiding x,y calc and extra method call.
		int x = index % super.width;
		int y = index / super.width;
		set(x, y, value);
	}

	/** set the current background value. only applies to UnsignedByte data */
	@Override
	public void setBackgroundValue(double value)
	{
		// we'll do nothing
		// in IJ1 only applied to unsigned 8 bit data
		//   in effect it did this:
			//if (this.isUnsignedByte)
			//{
			//	if (value < 0) value = 0;
			//	if (value > 255) value = 255;
			//	value = (int) value;
			//	this.backgroundValue = value;
			//}
	}

	/** set the current foreground value. */
	@Override
	public void setColor(Color color)
	{
		int bestIndex = getBestIndex(color);

		double min = getMin();
		double max = getMax();

		if ((bestIndex>0) && (min == 0) && (max == 0))
		{
			setValue(bestIndex);

			setMinAndMax(0,255);  // this is what ShortProcessor does
		}
		else if ((bestIndex == 0) && (min > 0) && ((color.getRGB()&0xffffff) == 0))
		{
			setValue(0);
		}
		else
		{
			double value = (min + (max-min)*(bestIndex/255.0));

			setValue(value);
		}
	}

	/** set the pixel at x,y to provided double value. */
	@Override
	public void setd(int x, int y, double value)
	{
		int[] pos = this.threadLocalPosition.get();
		pos[0] = x;
		pos[1] = y;
		this.dataset.setDouble(pos, value);
	}

	/** set the pixel at index to provided double value. */
	@Override
	public void setd(int index, double value)
	{
		// TODO - for performance - cache a DataAccessor with this processor. Update it every time setPixels() done.
		//   Then use it here to access pixels avoiding x,y calc and extra method call.
		int x = index % super.width;
		int y = index / super.width;
		setd(x, y, value);
	}

	/** set the pixel at x,y to provided float value. */
	@Override
	public void setf(int x, int y, float value)
	{
		int[] pos = this.threadLocalPosition.get();
		pos[0] = x;
		pos[1] = y;
		this.dataset.setDouble(pos, value);
	}

	/** set the pixel at index to provided float value. */
	@Override
	public void setf(int index, float value)
	{
		// TODO - for performance - cache a DataAccessor with this processor. Update it every time setPixels() done.
		//   Then use it here to access pixels avoiding x,y calc and extra method call.
		int x = index % super.width;
		int y = index / super.width;
		setf(x, y, value);
	}

	/** set the pixel at x,y to provided long value. */
	@Override
	public void setl(int x, int y, long value)
	{
		int[] pos = this.threadLocalPosition.get();
		pos[0] = x;
		pos[1] = y;
		this.dataset.setLong(pos, value);
	}

	/** set the pixel at index to provided long value. */
	@Override
	public void setl(int index, long value)
	{
		// TODO - for performance - cache a DataAccessor with this processor. Update it every time setPixels() done.
		//   Then use it here to access pixels avoiding x,y calc and extra method call.
		int x = index % super.width;
		int y = index / super.width;
		setl(x, y, value);
	}

	/** set min and max values to provided values. resets the threshold as needed. if passed (0,0) it will calculate actual min and max 1st. */
	@Override
	public void setMinAndMax(double min, double max)
	{
		if (min==0.0 && max==0.0)
		{
			resetMinAndMax();
			return;
		}

		if (this.isFloat)
		{
			this.min = min;
			this.max = max;
		}
		else // integral type
		{
			this.min = (long)min;
			this.max = (long)max;
		}

		this.minMaxSet = true;

		resetThreshold();
		
	}

	/** set the current image plane data to the provided pixel values. if underlying Dataset supports direct access if will set it's
	 * data reference to the passed in pixels. Otherwise it will copy the values from the given pixel array into the underlying
	 * Dataset's plane. */
	@Override
	public void setPixels(Object pixels)
	{
		if (pixels != null)
		{
			Object directAccess = this.dataset.getData();
			
			if (directAccess != null)
			{
				this.dataset.setData(pixels);
			}
			else // we can't set/get data directly
			{	
				System.out.println("DatasetPrcoessor::setPixels() - nonoptimal strategy - copying pixels by value rather than by reference");
				
				int[] dimensions = this.dataset.getDimensions();
				
				PlanarDataset tempDataset = new PlanarDataset(dimensions, this.dataset.getType(), pixels);
				
				int[] origin = Index.create(2);
				
				RegionCopyOperation copier = new RegionCopyOperation(tempDataset, origin, this.dataset, origin, dimensions, this.isFloat);
				
				copier.execute();
			}
		}

		super.resetPixels(pixels);

		if (pixels==null)  // free up memory
		{
			this.snapshot = null;
			this.pixels8 = null;
		}
	}

	/** set the current image plane data to the pixel values of the provided FloatProcessor. channelNumber is ignored. */
	@Override
	public void setPixels(int channelNumber, FloatProcessor fp)
	{
		// like ByteProcessor ignore channel number

		setPixelsFromFloatProc(fp);

		setMinAndMax(fp.getMin(), fp.getMax());
	}

	/** sets the current snapshot pixels to the provided pixels. It keeps a references to the pixel values. */
	@Override
	public void setSnapshotPixels(Object pixels)
	{
		if (pixels == null)
		{
			this.snapshot = null;
			return;
		}

		this.snapshot = new PlanarDataset(new int[]{super.width, super.height}, this.type, pixels);
	}

	/** sets the current fill/foreground value */
	@Override
	public void setValue(double value)
	{
		this.fillColor = value;

		// Issue - super.fgColor can't be set for UINT & LONG data types. Must rely on fillColor for all work. Problem?
		//   Could change signature of setFgColor() to take a double and change ImageProcessor so that fgColor is a
		//   double. That probably would work. Would work seamlessly with code. But plugins might need a recompile
		//   if they themselves call setFgColor().

		if ((!this.isFloat) && (this.type.getMaxIntegral() <= Integer.MAX_VALUE))
		{
			setFgColor((int) LongRange.bound(this.type.getMinIntegral(), this.type.getMaxIntegral(), (long)this.fillColor));
		}
	}

	/** copy the current pixels to the snapshot array */
	@Override
	public void snapshot()
	{
		this.snapshot = this.duplicator.createDataset(this.factory, this.dataset);

		this.snapshotMin = this.getMin();
		this.snapshotMax = this.getMax();
	}

	/** Performs a square transform on the image or ROI. */
	@Override
	public void sqr()
	{
		if (this.isFloat)
		{
			for (int x = super.roiX; x < (super.roiX+super.roiWidth); x++)
			{
				for (int y = super.roiY; y < (super.roiY+super.roiHeight); y++)
				{
					double value = getd(x, y);
					setd(x, y, value*value);
				}
			}
		}
		else // integral type
		{
			for (int x = super.roiX; x < (super.roiX+super.roiWidth); x++)
			{
				for (int y = super.roiY; y < (super.roiY+super.roiHeight); y++)
				{
					long value = getl(x, y);
					setl(x, y, value*value);
				}
			}
		}
	}

	/** Performs a square root transform on the image or ROI. */
	@Override
	public void sqrt()
	{
		for (int x = super.roiX; x < (super.roiX+super.roiWidth); x++)
		{
			for (int y = super.roiY; y < (super.roiY+super.roiHeight); y++)
			{
				double value = getd(x, y);
				setd(x, y, Math.sqrt(value));
			}
		}
	}

	/** Makes the image binary (values of 0 and 255) splitting the pixels based on their relationship to the threshold level.
	 *  Only applies to integral data types. Threshold level is limited to Integer range so some thresholding operations
	 *  not possible with Long data.
	 */
	@Override
	public void threshold(int thresholdLevel)
	{
		if (this.isFloat)
			return;

		for (int x = 0; x < super.width; x++)
		{
			for (int y = 0; y < super.height; y++)
			{
				long pix = getl(x, y);
				if (pix <= thresholdLevel)
					setl(x, y, 0);
				else
					setl(x, y, 255);
			}
		}
		
		setMinAndMaxOnly(0, 255);
		this.minMaxSet = true;
	}

	/*
	private class ThresholdDoubleFunction implements DoubleFunction
	{
		private double threshold, min, max;
		
		public ThresholdDoubleFunction(double threshold, double min, double max)
		{
			this.threshold = threshold;
			this.min = min;
			this.max = max;
		}
		
		@Override
		public int getParameterCount()
		{
			return 1;
		}

		@Override
		public double compute(double[] inputs)
		{
			if (inputs[0] <= this.threshold)
				return this.min;
			else
				return this.max;
		}
		
	}
	*/
	
	/** creates a FloatProcessor whose pixel values are set to those of this processor. */
	@Override
	public FloatProcessor toFloat(int channelNumber, FloatProcessor fp)
	{
		float[] pixelValues = new float[this.numPixels];
		
		for (int i = 0; i < this.numPixels; i++)
			pixelValues[i] = getf(i);
		
		if (fp == null || fp.getWidth()!=super.width || fp.getHeight()!=super.height)
			fp = new FloatProcessor(super.width, super.height, null, super.cm);

		fp.setPixels(pixelValues);

		fp.setRoi(getRoi());
		fp.setMask(getMask());
		fp.setMinAndMax(this.getMin(), this.getMax());
		fp.setThreshold(getMinThreshold(), getMaxThreshold(), ImageProcessor.NO_LUT_UPDATE);

		return fp;
	}

	/** Binary exclusive OR of each pixel in the image or ROI with 'value'. */
	@Override
	public void xor(int value)
	{
		if (this.isFloat)
			return;
		
		for (int x = super.roiX; x < (super.roiX+super.roiWidth); x++)
		{
			for (int y = super.roiY; y < (super.roiY+super.roiHeight); y++)
			{
				long pix = getl(x, y);
				setl(x, y, pix ^ (long)value);
			}
		}
	}
	
}
