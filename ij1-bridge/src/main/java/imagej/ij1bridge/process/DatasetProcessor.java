package imagej.ij1bridge.process;

import java.awt.Color;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;

import ij.measure.Calibration;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import imagej.DoubleRange;
import imagej.LongRange;
import imagej.data.Type;
import imagej.dataset.Dataset;
import imagej.dataset.DatasetDuplicator;
import imagej.dataset.FixedDimensionDataset;
import imagej.dataset.PlanarDataset;
import imagej.dataset.PlanarDatasetFactory;
import imagej.function.DoubleFunction;
import imagej.function.LongFunction;
import imagej.operation.RegionCopyOperation;
import imagej.operation.TransformOperation;
import imagej.process.Index;
import imagej.process.Span;

/**
 * Implements the ImageProcessor interface for an ImageJ 2.x Dataset. The dataset must be 2-Dimensional.
 */
public class DatasetProcessor extends ImageProcessor
{
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
		// in IJ this is a speed optimized version based on direct pixel access
		// for now don't build a speed optimized version
		
		float[] fKernel = new float[9];
		
		for (int i = 0; i < 9; i++)
			fKernel[i] = kernel[i];
		
		convolve(fKernel, 3, 3);
	}

	/** uses a blitter to copy pixels to xloc,yloc from ImageProcessor ip using the given mode. */
	@Override
	public void copyBits(ImageProcessor ip, int xloc, int yloc, int mode)
	{
		// TODO Auto-generated method stub
		
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

	@Override
	public void dilate()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawPixel(int x, int y)
	{
		if (x>=super.clipXMin && x<=super.clipXMax && y>=super.clipYMin && y<=super.clipYMax)
		{
			setd(x,y,this.fillColor);
		}
	}

	@Override
	public ImageProcessor duplicate()
	{
		Dataset outputDataset = this.duplicator.createDataset(this.factory, this.dataset);
		
		return new DatasetProcessor(outputDataset);
	}
	
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

	@Override
	public void erode()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fill(ImageProcessor mask)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void filter(int type)
	{
		// TODO Auto-generated method stub
		
	}

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

	// TODO - for IJ1 float type processor this one gets using intToFloatBits. we will not mirror this functionality.
	//   We could test if our dataset is float and if so then calc get with intToFloatBits()) but then acts differently if int or float.
	//   Think about this further.
	@Override
	public int get(int x, int y)
	{
		return (int) getl(x,y);
	}

	@Override
	public int get(int index)
	{
		int x = index % super.width;
		int y = index / super.width;
		return get(x, y);
	}

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

	@Override
	public int getBitDepth()
	{
		return this.type.getNumBitsData();
	}

	@Override
	public double getBytesPerPixel()
	{
		return getBitDepth() / 8.0;
	}

	@Override
	public double getd(int x, int y)
	{
		int[] pos = this.threadLocalPosition.get();
		pos[0] = x;
		pos[1] = y;
		return this.dataset.getDouble(pos);
	}

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
	
	@Override
	public float getf(int x, int y)
	{
		return (float) getd(x, y);
	}

	@Override
	public float getf(int index)
	{
		int x = index % super.width;
		int y = index / super.width;
		return getf(x, y);
	}

	@Override
	public int[] getHistogram()
	{
		if (this.isFloat)
			return null;
		
		int bitDepth = this.getBitDepth();
		
		if (bitDepth <= 16)
		{
			int[] histogram = new int[1 << bitDepth];

			int minValue = (int) this.type.getMinIntegral();
			
			for (int x = 0; x < super.width; x++)
			{
				for (int y = 0; y < super.height; y++)
				{
					int pixValue = this.get(x, y);
					histogram[pixValue-minValue]++;
				}
			}
			
			return histogram;
		}
		
		return null;
	}

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

	@Override
	public long getl(int x, int y)
	{
		int[] pos = this.threadLocalPosition.get();
		pos[0] = x;
		pos[1] = y;
		return this.dataset.getLong(pos);
	}

	@Override
	public long getl(int index)
	{
		int x = index % super.width;
		int y = index / super.width;
		return getl(x, y);
	}

	@Override
	public double getMax()
	{
		if (!super.minMaxSet)
			findMinAndMax();
		
		return this.max;
	}

	@Override
	public double getMaximumAllowedValue()
	{
		return this.type.getMaxReal();
	}

	@Override
	public double getMin()
	{
		if (!super.minMaxSet)
			findMinAndMax();
		
		return this.min;
	}

	@Override
	public double getMinimumAllowedValue()
	{
		return this.type.getMinReal();
	}

	@Override
	public int getPixel(int x, int y)
	{
		if ((x >= 0) && (x < super.width) && (y >= 0) && (y < super.height))
			return get(x, y);
		return 0;
	}

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

	@Override
	public float getPixelValue(int x, int y)
	{
		// make sure its in bounds
		if ((x >= 0) && (x < super.width) && (y >= 0) && (y < super.height))
			return getf(x, y);

		return 0f;
	}

	@Override
	public Object getSnapshotPixels()
	{
		if (this.snapshot == null)
			return null;
		
		Dataset copy = this.duplicator.createDataset(this.factory, this.snapshot);
		
		return copy.getData();
	}

	@Override
	public ImageStatistics getStatistics(int mOptions, Calibration cal)
	{
		return new GenericStatistics(this, mOptions, cal);
	}
	
	@Override
	public String getTypeName()
	{
		return this.type.getName();
	}
	@Override
	public boolean isFloatingType()
	{
		return this.type.isFloat();
	}

	@Override
	public boolean isUnsignedType()
	{
		return this.type.isUnsigned();
	}

	@Override
	public void medianFilter()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void noise(double range)
	{
		// TODO Auto-generated method stub
		
	}

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

	@Override
	public void reset(ImageProcessor mask)
	{
		// TODO Auto-generated method stub
		
	}

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

		ImgLibProcessor<?> ip2 = (ImgLibProcessor<?>)createProcessor(dstWidth, dstHeight);

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
	@Override
	public void set(int x, int y, int value)
	{
		int[] pos = this.threadLocalPosition.get();
		pos[0] = x;
		pos[1] = y;
		this.dataset.setDouble(pos, value);
	}

	@Override
	public void set(int index, int value)
	{
		int x = index % super.width;
		int y = index / super.width;
		set(x, y, value);
	}

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

	@Override
	public void setd(int x, int y, double value)
	{
		int[] pos = this.threadLocalPosition.get();
		pos[0] = x;
		pos[1] = y;
		this.dataset.setDouble(pos, value);
	}

	@Override
	public void setd(int index, double value)
	{
		int x = index % super.width;
		int y = index / super.width;
		setd(x, y, value);
	}

	@Override
	public void setf(int x, int y, float value)
	{
		int[] pos = this.threadLocalPosition.get();
		pos[0] = x;
		pos[1] = y;
		this.dataset.setDouble(pos, value);
	}

	@Override
	public void setf(int index, float value)
	{
		int x = index % super.width;
		int y = index / super.width;
		setf(x, y, value);
	}

	@Override
	public void setl(int x, int y, long value)
	{
		int[] pos = this.threadLocalPosition.get();
		pos[0] = x;
		pos[1] = y;
		this.dataset.setLong(pos, value);
	}

	@Override
	public void setl(int index, long value)
	{
		int x = index % super.width;
		int y = index / super.width;
		setl(x, y, value);
	}

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

	@Override
	public void setPixels(int channelNumber, FloatProcessor fp)
	{
		// like ByteProcessor ignore channel number

		setPixelsFromFloatProc(fp);

		setMinAndMax(fp.getMin(), fp.getMax());
	}

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

	@Override
	public void snapshot()
	{
		this.snapshot = this.duplicator.createDataset(this.factory, this.dataset);

		this.snapshotMin = this.getMin();
		this.snapshotMax = this.getMax();
	}

	@Deprecated
	@Override
	public void threshold(int thresholdLevel)
	{
		if (this.isFloat)
			return;

		thresholdLevel = (int) DoubleRange.bound(this.type.getMinReal(), this.type.getMaxReal(), thresholdLevel);
		
		threshold((double)thresholdLevel);
	}

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
		public int getValueCount()
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
	
	public void threshold(double thresholdLevel)
	{
		thresholdLevel = DoubleRange.bound(this.type.getMinReal(), this.type.getMaxReal(), thresholdLevel);
		
		DoubleFunction function = new ThresholdDoubleFunction(thresholdLevel, 0, 255);
		
		TransformOperation operation =
			new TransformOperation(function, new Dataset[]{this.dataset}, new int[][]{Index.create(2)}, this.dataset.getDimensions(), 0);
		
		operation.execute();
	}
	
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

}
