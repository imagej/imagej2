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
import imagej.data.Type;
import imagej.dataset.Dataset;
import imagej.dataset.DatasetDuplicator;
import imagej.dataset.PlanarDataset;
import imagej.dataset.PlanarDatasetFactory;
import imagej.function.unary.IntegralSubstitutionUnaryFunction;
import imagej.imglib.process.ImageUtils;
import imagej.operation.RegionCopyOperation;
import imagej.process.Index;
import imagej.process.Span;

public class DatasetProcessor extends ImageProcessor
{
	// ************* instance variables ***********************************************************************************
	
	private Dataset dataset;

	/** a per thread variable. a cursor used to facilitate fast access to the image. */
	private ThreadLocal<int[]> threadLocalPosition =
		new ThreadLocal<int[]>()
		{
			@Override
			protected int[] initialValue() { return new int[2];	}
		};
	
	/** used by some methods that fill in default values. only applies to float data */
	private double fillColor;

	private double max;
	
	private double min;
	
	private int numPixels;
	
	private byte[] pixels8;
	
	private Dataset snapshot;
	
	private double snapshotMin;
	
	private double snapshotMax;

	private Type type;
	
	// ************* constructor ***********************************************************************************

	public DatasetProcessor(Dataset dataset)
	{
		int[] dimensions = dataset.getDimensions();
		
		if (dimensions.length != 2)
			throw new IllegalArgumentException("DatasetProcessors are planar and only work with 2-D Datasets. Was given a "+
					dimensions.length+"-D dataset.");
		
		this.dataset = dataset;
		this.width = dimensions[0];
		this.height = dimensions[1];
		this.numPixels = (int)((long)this.width * (long)this.height);
		this.type = dataset.getType();
		this.pixels8 = null;
		this.snapshot = null;
	}


	// ************* private interface ***********************************************************************************
	
	private void setPixelsFromFloatProc(FloatProcessor proc)
	{
		int[] pos = this.threadLocalPosition.get();
		for (int x = 0; x < this.width; x++)
		{
			for (int y = 0; y < this.height; y++)
			{
				double value = proc.getd(x, y);
				pos[0] = x;
				pos[1] = y;
				this.dataset.setDouble(pos, value);
			}
		}
	}
	
	/** returns the coordinates of the ROI origin as an int[] */
	private int[] originOfRoi()
	{
		return Index.create(super.roiX, super.roiY, new int[]{});
	}

	/** returns the span of the ROI plane as an int[] */
	private int[] spanOfRoiPlane()
	{
		return Span.singlePlane(super.roiWidth, super.roiHeight, 2);
	}

	// ************* public interface ***********************************************************************************


	@Override
	public void applyTable(int[] lut)
	{
		if (this.isFloatingType())
			return;

		long expectedLutSize = (1L << this.getBitDepth());
		
		if (lut.length != expectedLutSize)
			throw new IllegalArgumentException("lut size ("+lut.length+" values) does not match range of pixel type ("+expectedLutSize+" values)");
		
		/*
		IntegralSubstitutionUnaryFunction substFunction =
			new IntegralSubstitutionUnaryFunction((int)this.type.getMinValue(), lut);

		nonPositionalTransform(substFunction);

		findMinAndMax();
		*/
	}

	@Override
	public void convolve(float[] kernel, int kernelWidth, int kernelHeight)
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public void convolve3x3(int[] kernel)
	{
		// TODO Auto-generated method stub
		
	}

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

		for (int i = 0; i < this.numPixels; i++)
		{
			double value = getd(i);  // TODO - this method may display some longs a little inaccurately. probably not a problem.
			
			double relPos = (value - this.min) / (this.max - this.min);
			
			if (relPos < 0) relPos = 0;
			if (relPos > 1) relPos = 1;
			
			this.pixels8[i] = (byte) Math.round(255 * relPos);
		}

		return this.pixels8;
	}

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
			super.source = new MemoryImageSource(super.width, super.height, super.cm, this.pixels8, 0, width);
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

	@Override
	public ImageProcessor createProcessor(int width, int height)
	{
		Dataset newDataset = new PlanarDatasetFactory().createDataset(this.type, new int[]{width, height});

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
		
		Dataset newDataset = new PlanarDatasetFactory().createDataset(this.type, span);
		
		RegionCopyOperation copier =
			new RegionCopyOperation(this.dataset, srcOrigin, newDataset, Index.create(2), span, this.isFloatingType());
		
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
		Dataset outputDataset = new DatasetDuplicator().createDataset(new PlanarDatasetFactory(), this.dataset);
		
		RegionCopyOperation copier =
			new RegionCopyOperation(this.dataset, Index.create(2), outputDataset, Index.create(2), this.dataset.getDimensions(), this.isFloatingType());
		
		copier.execute();
		
		return new DatasetProcessor(outputDataset);
	}
	
	@Override
	public void encodePixelInfo(int[] destination, int x, int y)
	{
		// TODO
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
		boolean isFloat = this.isFloatingType();
		
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
				
				if (isFloat)
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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int get(int index)
	{
		int x = index % this.width;
		int y = index / this.width;
		return get(x, y);
	}

	@Override
	public double getBackgroundValue()
	{
		// TODO Auto-generated method stub
		return 0;
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
		int x = index % this.width;
		int y = index / this.width;
		return getd(x, y);
	}

	@Override
	public float getf(int x, int y)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getf(int index)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int[] getHistogram()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getInterpolatedPixel(double x, double y)
	{
		// TODO Auto-generated method stub
		return 0;
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
		int x = index % this.width;
		int y = index / this.width;
		return getl(x, y);
	}

	@Override
	public double getMax()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMaximumAllowedValue()
	{
		return this.type.getMaxReal();
	}

	@Override
	public double getMin()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMinimumAllowedValue()
	{
		return this.type.getMinReal();
	}

	@Override
	public int getPixel(int x, int y)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getPixelInterpolated(double x, double y)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getPixels()
	{
		Object data = this.dataset.getData();
		
		if (data != null)
			return data;
		
		System.out.println("DatasetProcessor::getPixels() - nonoptimal data use - returning pixel values by copy rather than by reference");
		
		// make a copy of the pixels
		
		Dataset twoDimDataset = new DatasetDuplicator().createDataset(new PlanarDatasetFactory(), this.dataset);

		return twoDimDataset.getData();
	}

	@Override
	public Object getPixelsCopy()
	{
		if (this.snapshot!=null && getSnapshotCopyMode())
		{
			setSnapshotCopyMode(false);

			Dataset copy = new DatasetDuplicator().createDataset(new PlanarDatasetFactory(), this.snapshot);
			
			return copy.getData();
		}
		else
		{
			Dataset copy = new DatasetDuplicator().createDataset(new PlanarDatasetFactory(), this.dataset);
			
			return copy.getData();
		}
	}

	@Override
	public float getPixelValue(int x, int y)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getSnapshotPixels()
	{
		if (this.snapshot == null)
			return null;
		
		Dataset copy = new DatasetDuplicator().createDataset(new PlanarDatasetFactory(), this.snapshot);
		
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

	@Override
	public void putPixel(int x, int y, int value)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void putPixelValue(int x, int y, double value)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reset()
	{
		if (this.snapshot!=null)
		{
			int[] origin = new int[2];
			RegionCopyOperation copier =
				new RegionCopyOperation(this.snapshot, origin, this.dataset, origin, this.snapshot.getDimensions(), this.isFloatingType());
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void rotate(double angle)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void scale(double xScale, double yScale)
	{
		// TODO Auto-generated method stub
		
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
		int x = index % this.width;
		int y = index / this.width;
		set(x, y, value);
	}

	@Override
	public void setBackgroundValue(double value)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setColor(Color color)
	{
		// TODO Auto-generated method stub
		
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
		int x = index % this.width;
		int y = index / this.width;
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
		int x = index % this.width;
		int y = index / this.width;
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
		int x = index % this.width;
		int y = index / this.width;
		setl(x, y, value);
	}

	@Override
	public void setMinAndMax(double min, double max)
	{
		// TODO Auto-generated method stub
		
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
				
				RegionCopyOperation copier = new RegionCopyOperation(tempDataset, origin, this.dataset, origin, dimensions, this.isFloatingType());
				
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setValue(double value)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void snapshot()
	{
		this.snapshot = new DatasetDuplicator().createDataset(new PlanarDatasetFactory(), this.dataset);

		this.snapshotMin = this.min;
		this.snapshotMax = this.max;
	}

	@Deprecated
	@Override
	public void threshold(int thresholdLevel)
	{
		if (this.isFloatingType())
			return;

		thresholdLevel = (int) DoubleRange.bound(this.type.getMinReal(), this.type.getMaxReal(), thresholdLevel);
		
		threshold((double)thresholdLevel);
	}

	public void threshold(double thresholdLevel)
	{
		// TODO
	}
	
	@Override
	public FloatProcessor toFloat(int channelNumber, FloatProcessor fp)
	{
		float[] pixelValues = new float[this.numPixels];
		
		for (int i = 0; i < this.numPixels; i++)
			pixelValues[i] = getf(i);
		
		if (fp == null || fp.getWidth()!=this.width || fp.getHeight()!=this.height)
			fp = new FloatProcessor(this.width, this.height, null, super.cm);

		fp.setPixels(pixelValues);

		fp.setRoi(getRoi());
		fp.setMask(getMask());
		fp.setMinAndMax(this.min, this.max);
		fp.setThreshold(getMinThreshold(), getMaxThreshold(), ImageProcessor.NO_LUT_UPDATE);

		return fp;
	}

}
