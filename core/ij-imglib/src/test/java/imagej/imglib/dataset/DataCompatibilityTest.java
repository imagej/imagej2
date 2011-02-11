package imagej.imglib.dataset;

import static org.junit.Assert.*;

import imagej.dataset.Dataset;
import imagej.dataset.PlanarDatasetFactory;
import imagej.imglib.TypeManager;
import imagej.imglib.ImageUtils;
import imagej.process.Index;
import imagej.process.Span;
import imagej.types.Type;
import imagej.types.Types;

import java.util.Random;

import mpicbg.imglib.container.planar.PlanarContainerFactory;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.logic.BitType;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.ByteType;
import mpicbg.imglib.type.numeric.integer.IntType;
import mpicbg.imglib.type.numeric.integer.LongType;
import mpicbg.imglib.type.numeric.integer.ShortType;
import mpicbg.imglib.type.numeric.integer.Unsigned12BitType;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;
import mpicbg.imglib.type.numeric.integer.UnsignedIntType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;
import mpicbg.imglib.type.numeric.real.DoubleType;
import mpicbg.imglib.type.numeric.real.FloatType;

import org.junit.Test;

// this class tests that IJ2's and ImgLib's data access methods do the same thing

public class DataCompatibilityTest
{
	private static final int DATA_SIZE = 70000;
	private static final double SCALE_FACTOR = 2.1;
	private double base, range;
	private double[] data;
	private Dataset ijDS;
	private Image<?> imglibImage;
	private RealType<?> imglibType;
	
	public void setupData()
	{
		data = new double[DATA_SIZE];
		
		Random rng = new Random();
		
		rng.setSeed(107731);
		
		for (int i = 0; i < DATA_SIZE; i++)
		{
			if (rng.nextBoolean())
				data[i] = rng.nextDouble();
			else
				data[i] = -rng.nextDouble();
		}
	}

	private double getValue(int i)
	{
		return this.base + (this.data[i] * this.range * SCALE_FACTOR);
	}
	
	private void createImglibImage(String typeName)
	{
		Type type = Types.findType(typeName);
		
		int[] dimensions = new int[]{1,DATA_SIZE};
		
		this.imglibType = TypeManager.getRealType(type);
		
		PlanarContainerFactory imglibFactory = new PlanarContainerFactory();
		
		imglibFactory.setOptimizedContainerUse(true);
		
		this.imglibImage = ImageUtils.createImage(imglibType, imglibFactory, dimensions);
	}
	
	private void createIjDataset(String typeName)
	{
		Type type = Types.findType(typeName);
		
		int[] dimensions = new int[]{1,DATA_SIZE};
		
		PlanarDatasetFactory factory = new PlanarDatasetFactory();
		
		this.ijDS = factory.createDataset(type, dimensions);
	}

	private void setBaseAndRange()
	{
		if (this.imglibType instanceof BitType)
		{
				this.base = 0.0;
				this.range = 1.0;
		}
		
		else if (this.imglibType instanceof ByteType)
		{
				this.base = Byte.MIN_VALUE;
				this.range = 256.0;
		}
		
		else if (this.imglibType instanceof UnsignedByteType)
		{
				this.base = 0.0;
				this.range = 256.0;
		}
		
		else if (this.imglibType instanceof Unsigned12BitType)
		{
				this.base = 0.0;
				this.range = 4096.0;
		}
		
		else if (this.imglibType instanceof ShortType)
		{
				this.base = Short.MIN_VALUE;
				this.range = 65536.0;
		}
		
		else if (this.imglibType instanceof UnsignedShortType)
		{
				this.base = 0.0;
				this.range = 65536.0;
		}
		
		else if (this.imglibType instanceof IntType)
		{
				this.base = Integer.MIN_VALUE;
				this.range = 0xffffffffL;
		}
		
		else if (this.imglibType instanceof UnsignedIntType)
		{
				this.base = 0.0;
				this.range = 0xffffffffL;
		}
		
		else if (this.imglibType instanceof FloatType)
		{
				this.base = -50000000.0;
				this.range = 100000000.0;
		}
		
		else if (this.imglibType instanceof LongType)
		{
				this.base = -100L + Integer.MIN_VALUE;
				this.range = 200L + 2L * Integer.MAX_VALUE;
		}
		
		else if (this.imglibType instanceof DoubleType)
		{
			this.base = -50000000.0;
			this.range = 100000000.0;
		}
		
		else
			throw new IllegalStateException("unknown type specified");
	}

	@SuppressWarnings("unchecked")
	private void fillImglibImage()
	{
		double value;
		
		int i = 0;
		
		if (this.imglibType instanceof BitType)
		{
			Cursor<BitType> cursor = ((Image<BitType>)this.imglibImage).createCursor();
			while (cursor.hasNext())
			{
				cursor.fwd();
				value = getValue(i++);
				cursor.getType().setReal(value);
			}
		}
		
		else if (this.imglibType instanceof ByteType)
		{
			Cursor<ByteType> cursor = ((Image<ByteType>)this.imglibImage).createCursor();
			while (cursor.hasNext())
			{
				cursor.fwd();
				value = getValue(i++);
				cursor.getType().setReal(value);
			}
		}
		
		else if (this.imglibType instanceof UnsignedByteType)
		{
			Cursor<UnsignedByteType> cursor = ((Image<UnsignedByteType>)this.imglibImage).createCursor();
			while (cursor.hasNext())
			{
				cursor.fwd();
				value = getValue(i++);
				cursor.getType().setReal(value);
			}
		}
		
		else if (this.imglibType instanceof Unsigned12BitType)
		{
			Cursor<Unsigned12BitType> cursor = ((Image<Unsigned12BitType>)this.imglibImage).createCursor();
			while (cursor.hasNext())
			{
				cursor.fwd();
				value = getValue(i++);
				cursor.getType().setReal(value);
			}
		}
		
		else if (this.imglibType instanceof ShortType)
		{
			Cursor<ShortType> cursor = ((Image<ShortType>)this.imglibImage).createCursor();
			while (cursor.hasNext())
			{
				cursor.fwd();
				value = getValue(i++);
				cursor.getType().setReal(value);
			}
		}
		
		else if (this.imglibType instanceof UnsignedShortType)
		{
			Cursor<UnsignedShortType> cursor = ((Image<UnsignedShortType>)this.imglibImage).createCursor();
			while (cursor.hasNext())
			{
				cursor.fwd();
				value = getValue(i++);
				cursor.getType().setReal(value);
			}
		}
		
		else if (this.imglibType instanceof IntType)
		{
			Cursor<IntType> cursor = ((Image<IntType>)this.imglibImage).createCursor();
			while (cursor.hasNext())
			{
				cursor.fwd();
				value = getValue(i++);
				cursor.getType().setReal(value);
			}
		}
		
		else if (this.imglibType instanceof UnsignedIntType)
		{
			Cursor<UnsignedIntType> cursor = ((Image<UnsignedIntType>)this.imglibImage).createCursor();
			while (cursor.hasNext())
			{
				cursor.fwd();
				value = getValue(i++);
				cursor.getType().setReal(value);
			}
		}
		
		else if (this.imglibType instanceof FloatType)
		{
			Cursor<FloatType> cursor = ((Image<FloatType>)this.imglibImage).createCursor();
			while (cursor.hasNext())
			{
				cursor.fwd();
				value = getValue(i++);
				cursor.getType().setReal(value);
			}
		}
		
		else if (this.imglibType instanceof LongType)
		{
			Cursor<LongType> cursor = ((Image<LongType>)this.imglibImage).createCursor();
			while (cursor.hasNext())
			{
				cursor.fwd();
				value = getValue(i++);
				cursor.getType().setReal(value);
			}
		}
		
		else if (this.imglibType instanceof DoubleType)
		{
			Cursor<DoubleType> cursor = ((Image<DoubleType>)this.imglibImage).createCursor();
			while (cursor.hasNext())
			{
				cursor.fwd();
				value = getValue(i++);
				cursor.getType().setReal(value);
			}
		}
		
		else
			throw new IllegalStateException("unknown type specified");
	}
	
	private void fillIjDataset()
	{
		int[] origin = Index.create(2);
		int[] span = Span.singlePlane(1, DATA_SIZE, 2);
		int[] index = Index.create(2);
		
		int i = 0;
		
		while (Index.isValid(index, origin, span))
		{
			double value = getValue(i++);
				
			this.ijDS.setDouble(index, value);
			
			Index.increment(index, origin, span);
		}
	}
	
	private void fillImages()
	{
		setBaseAndRange();
		fillImglibImage();
		fillIjDataset();
	}
	
	private void createImages(String typeName)
	{
		setupData();
		createImglibImage(typeName);
		createIjDataset(typeName);
	}
	
	private void compareData()
	{
		Dataset imglibDS = new LegacyImgLibDataset(this.imglibImage);

		int[] origin = Index.create(2);
		int[] span = Span.singlePlane(1, DATA_SIZE, 2);
		int[] index = Index.create(2);
		
		//int i = 0;
		while (Index.isValid(index, origin, span))
		{
			// TODO - should do isFloat and then getDouble() vs. getLong() but Imglib doesn't support setting/getting longs correctly yet
			
			double ijDouble = this.ijDS.getDouble(index);
			
			double imglibDouble = imglibDS.getDouble(index);
			
			//double dataValue = getValue(i++);
			
			//if (Math.abs(ijDouble-imglibDouble) > 0.00001)
			//	System.out.println(this.ijDS.getType().getName()+" (entry "+(i-1)+") "+dataValue+" ij("+ijDouble+") imglib("+imglibDouble+")");
			
			assertEquals(ijDouble, imglibDouble, 0.00001);
			
			Index.increment(index, origin, span);
		}
	}
	
	@Test
	public void testBitCompatibility()
	{
		createImages("1-bit unsigned");
		fillImages();
		compareData();
	}
	
	@Test
	public void testByteCompatibility()
	{
		createImages("8-bit signed");
		fillImages();
		compareData();
	}
	
	@Test
	public void testUnsignedByteCompatibility()
	{
		createImages("8-bit unsigned");
		fillImages();
		compareData();
	}
	
	@Test
	public void testUnsigned12BitCompatibility()
	{
		createImages("12-bit unsigned");
		fillImages();
		compareData();
	}
	
	@Test
	public void testShortCompatibility()
	{
		createImages("16-bit signed");
		fillImages();
		compareData();
	}
	
	@Test
	public void testUnsignedShortCompatibility()
	{
		createImages("16-bit unsigned");
		fillImages();
		compareData();
	}
	
	@Test
	public void testIntCompatibility()
	{
		createImages("32-bit signed");
		fillImages();
		compareData();
	}
	
	@Test
	public void testUnsignedIntCompatibility()
	{
		createImages("32-bit unsigned");
		fillImages();
		compareData();
	}
	
	@Test
	public void testFloatCompatibility()
	{
		createImages("32-bit float");
		fillImages();
		compareData();
	}
	
	@Test
	public void testLongCompatibility()
	{
		createImages("64-bit signed");
		fillImages();
		compareData();
	}
	
	@Test
	public void testDoubleCompatibility()
	{
		createImages("64-bit float");
		fillImages();
		compareData();
	}
}
