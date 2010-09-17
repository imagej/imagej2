package imagej.process.operation;

import imagej.process.Span;
import imagej.process.TypeManager;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public class SetPlaneOperation<T extends RealType<T>> extends PositionalSingleCursorRoiOperation<T>
{
	public static enum PixelType {BYTE,SHORT,INT,FLOAT,DOUBLE,LONG};

	// set in constructor
	private PixelType pixType;
	private PixelReader reader;
	
	// set before iteration
	private int pixNum;
	private boolean isIntegral;
	
	public SetPlaneOperation(Image<T> theImage, int[] origin, Object pixels, PixelType inputType, boolean isUnsigned)
	{
		super(theImage, origin, Span.singlePlane(theImage.getDimension(0), theImage.getDimension(1), theImage.getNumDimensions()));
		this.pixType = inputType;
		
		switch (pixType)
		{
			case BYTE:
				if (isUnsigned)
					this.reader = new UnsignedByteReader(pixels);
				else
					this.reader = new ByteReader(pixels);
				break;
			case SHORT:
				if (isUnsigned)
					this.reader = new UnsignedShortReader(pixels);
				else
					this.reader = new ShortReader(pixels);
				break;
			case INT:
				if (isUnsigned)
					this.reader = new UnsignedIntReader(pixels);
				else
					this.reader = new IntReader(pixels);
				break;
			case LONG:
				this.reader = new LongReader(pixels);
				break;
			case FLOAT:
				this.reader = new FloatReader(pixels);
				break;
			case DOUBLE:
				this.reader = new DoubleReader(pixels);
				break;
			default:
				throw new IllegalArgumentException("unknown pixel type");
		}
		
	}
	
	@Override
	public void beforeIteration(RealType<T> type)
	{
		this.pixNum = 0;
		this.isIntegral = TypeManager.isIntegralType(type);
	}

	@Override
	public void insideIteration(int[] position, RealType<T> sample)
	{

		double pixelValue = reader.getValue(this.pixNum++);
		
		if (this.isIntegral)
			pixelValue = TypeManager.boundValueToType(sample, pixelValue);
		
		sample.setReal(pixelValue);
	}

	@Override
	public void afterIteration()
	{
	}
	
	private interface PixelReader
	{
		double getValue(int i);
	}
	
	private class ByteReader implements PixelReader
	{
		byte[] pixels;
		
		ByteReader(Object pixels) { this.pixels = (byte[]) pixels; }
		
		public double getValue(int i) { return pixels[i]; }
	}
	
	private class UnsignedByteReader implements PixelReader
	{
		byte[] pixels;
		
		UnsignedByteReader(Object pixels) { this.pixels = (byte[]) pixels; }
		
		public double getValue(int i)
		{
			double pixel = pixels[i];
			if (pixel < 0)
				pixel = 256.0 + pixel;
			return pixel;
		}
	}
	
	private class ShortReader implements PixelReader
	{
		short[] pixels;
		
		ShortReader(Object pixels) { this.pixels = (short[]) pixels; }
		
		public double getValue(int i) { return pixels[i]; }
	}
	
	private class UnsignedShortReader implements PixelReader
	{
		short[] pixels;
		
		UnsignedShortReader(Object pixels) { this.pixels = (short[]) pixels; }
		
		public double getValue(int i)
		{
			double pixel = pixels[i];
			if (pixel < 0)
				pixel = 65536.0 + pixel;
			return pixel;
		}
	}
	
	private class IntReader implements PixelReader
	{
		int[] pixels;
		
		IntReader(Object pixels) { this.pixels = (int[]) pixels; }
		
		public double getValue(int i) { return pixels[i]; }
	}
	
	private class UnsignedIntReader implements PixelReader
	{
		int[] pixels;
		
		UnsignedIntReader(Object pixels) { this.pixels = (int[]) pixels; }
		
		public double getValue(int i)
		{
			double pixel = pixels[i];
			if (pixel < 0)
				pixel = 4294967296.0 + pixel;
			return pixel;
		}
	}
	
	private class LongReader implements PixelReader
	{
		long[] pixels;
		
		LongReader(Object pixels) { this.pixels = (long[]) pixels; }
		
		public double getValue(int i) { return pixels[i]; }
	}
	
	private class FloatReader implements PixelReader
	{
		float[] pixels;
		
		FloatReader(Object pixels) { this.pixels = (float[]) pixels; }
		
		public double getValue(int i) { return pixels[i]; }
	}
	
	private class DoubleReader implements PixelReader
	{
		double[] pixels;
		
		DoubleReader(Object pixels) { this.pixels = (double[]) pixels; }
		
		public double getValue(int i) { return pixels[i]; }
	}
}

