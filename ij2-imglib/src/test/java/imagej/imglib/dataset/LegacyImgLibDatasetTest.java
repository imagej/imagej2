package imagej.imglib.dataset;

import static org.junit.Assert.*;

import java.util.ArrayList;

import imagej.MetaData;
import imagej.data.Type;
import imagej.data.Types;
import imagej.dataset.CompositeDataset;
import imagej.dataset.Dataset;
import imagej.imglib.TypeManager;
import imagej.imglib.dataset.LegacyImgLibDataset;

import mpicbg.imglib.container.planar.PlanarContainerFactory;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
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

@SuppressWarnings("unchecked")

public class LegacyImgLibDatasetTest
{
	private Image<?> createImage(RealType<?> type, int[] dimensions)
	{
		PlanarContainerFactory factory = new PlanarContainerFactory();
		return imagej.imglib.process.ImageUtils.createImage(type, factory, dimensions);
	}
	
	@Test
	public void testImgLibDataset()
	{
		int[] dimensions = new int[]{3,5,7};
		
		RealType<?> type = new ByteType();
		
		Image<?> image = createImage(type, dimensions);
		
		LegacyImgLibDataset ds = new LegacyImgLibDataset(image);
		
		assertNotNull(ds);
		assertSame(image, ds.getImage());
		assertEquals(TypeManager.getIJType(type), ds.getType());
	}

	@Test
	public void testGetDimensions()
	{
		int[] dimensions = new int[]{1,3,6};
		
		RealType<?> type = new IntType();
		
		Image<?> image = createImage(type, dimensions);

		LegacyImgLibDataset ds = new LegacyImgLibDataset(image);
		
		assertNotSame(dimensions, ds.getDimensions());
		assertArrayEquals(dimensions, ds.getDimensions());
	}

	@Test
	public void testGetType()
	{
		int[] dimensions = new int[]{1,2,3,4};
		
		RealType<?> type = new ShortType();
		
		Image<?> image = createImage(type, dimensions);
		
		LegacyImgLibDataset ds = new LegacyImgLibDataset(image);
		
		assertEquals(TypeManager.getIJType(type), ds.getType());
	}

	@Test
	public void testSetAndGetMetaData()
	{
		int[] dimensions = new int[]{420,201,5,2};
		
		RealType<?> type = new BitType();
		
		Image<?> image = createImage(type, dimensions);
		
		LegacyImgLibDataset ds = new LegacyImgLibDataset(image);

		MetaData origMetaData = ds.getMetaData();
		
		MetaData m = new MetaData();
		
		ds.setMetaData(m);
		
		assertFalse(m == origMetaData);
		assertEquals(m, ds.getMetaData());
	}

	@Test
	public void testIsComposite()
	{
		int[] dimensions = new int[]{83,621};
		RealType<?> type = new LongType();
		Image<?> image = createImage(type, dimensions);

		LegacyImgLibDataset ds = new LegacyImgLibDataset(image);
		
		assertFalse(ds.isComposite());

		dimensions = new int[]{83,621,2};
		image = createImage(type, dimensions);
		
		ds = new LegacyImgLibDataset(image);

		assertTrue(ds.isComposite());
	}

	@Test
	public void testSetAndGetParent()
	{
		int[] dimensions = new int[]{2,3,4,5,6,7};
		
		RealType<?> type = new UnsignedIntType();
		
		Image<?> image = createImage(type, dimensions);
		
		LegacyImgLibDataset ds = new LegacyImgLibDataset(image);
		
		assertNull(ds.getParent());

		CompositeDataset otherDs = new CompositeDataset(Types.findType("32-bit unsigned"), new int[]{}, new ArrayList<Dataset>());
		
		ds.setParent(otherDs);
		
		assertEquals(otherDs, ds.getParent());
	}

	@Test
	public void testSetAndGetData()
	{
		int[] dimensions = new int[]{5,14,3};
		
		RealType<?> type = new Unsigned12BitType();
		
		Image<?> image = createImage(type, dimensions);
		
		LegacyImgLibDataset ds = new LegacyImgLibDataset(image);
		
		Type ijType = TypeManager.getIJType(type);
		
		Object plane = ijType.allocateStorageArray(5*14);
		
		ds.getSubset(1).setData(plane);
		
		assertSame(plane, ds.getSubset(1).getData());
	}

	@Test
	public void testReleaseData()
	{
		int[] dimensions = new int[]{102,88,15,4,3};
		
		RealType<?> type = new DoubleType();
		
		Image<?> image = createImage(type, dimensions);
		
		LegacyImgLibDataset ds = new LegacyImgLibDataset(image);

		ds.releaseData();
		
		assertTrue(true);  // nothing really to test. if here no exception was thrown.
	}

	@Test
	public void testInsertNewSubset()
	{
		int[] dimensions = new int[]{2,3,4};
		
		RealType<?> type = new DoubleType();
		
		Image<DoubleType> image = (Image<DoubleType>) createImage(type, dimensions);
		
		LocalizableByDimCursor<DoubleType> cursor = image.createLocalizableByDimCursor();

		for (int x = 0; x < 2; x++)
		{
			for (int y = 0; y < 3; y++)
			{
				for (int z = 0; z < 4; z++)
				{
					cursor.setPosition(new int[]{x,y,z});
					cursor.getType().setReal(100*x + 10*y + z);
				}
			}
		}
		
		LegacyImgLibDataset ds = new LegacyImgLibDataset(image);

		Object[] planeRefs = new Object[4];
		for (int i = 0; i < 4; i++)
			planeRefs[i] = ds.getSubset(i).getData();
		
		ds.insertNewSubset(1);
		
		assertArrayEquals(new int[]{2,3,5}, ds.getDimensions());
		
		assertSame(planeRefs[0], ds.getSubset(0).getData());
		assertSame(planeRefs[1], ds.getSubset(2).getData());
		assertSame(planeRefs[2], ds.getSubset(3).getData());
		assertSame(planeRefs[3], ds.getSubset(4).getData());

		// test that each original plane is unchanged
		for (int x = 0; x < 2; x++)
			for (int y = 0; y < 3; y++)
				for (int z = 0; z < 4; z++)
				{
					if (z == 1)
						break;
					
					int newPlaneIndex = (z == 0 ? z : z+1);
					
					assertEquals(100*x + 10*y + z, ds.getDouble(new int[]{x,y,newPlaneIndex}), 0);
				}

		// test that taking a subset and getting a double with partial index works - keep this test
		assertEquals(110, ds.getSubset(0).getDouble(new int[]{1,1}), 0);
		
		// test that new plane is all zero
		for (int x = 0; x < 2; x++)
			for (int y = 0; y < 3; y++)
				assertEquals(0, ds.getDouble(new int[]{x,y,1}), 0);
	}

	@Test
	public void testRemoveSubset()
	{
		int[] dimensions = new int[]{2,3,4};
		
		RealType<?> type = new DoubleType();
		
		Image<DoubleType> image = (Image<DoubleType>) createImage(type, dimensions);
		
		LocalizableByDimCursor<DoubleType> cursor = image.createLocalizableByDimCursor();

		for (int x = 0; x < 2; x++)
		{
			for (int y = 0; y < 3; y++)
			{
				for (int z = 0; z < 4; z++)
				{
					cursor.setPosition(new int[]{x,y,z});
					cursor.getType().setReal(100*x + 10*y + z);
				}
			}
		}
		
		LegacyImgLibDataset ds = new LegacyImgLibDataset(image);

		Object[] planeRefs = new Object[4];
		for (int i = 0; i < 4; i++)
			planeRefs[i] = ds.getSubset(i).getData();
		
		ds.removeSubset(2);
		
		assertArrayEquals(new int[]{2,3,3}, ds.getDimensions());
		
		assertSame(planeRefs[0], ds.getSubset(0).getData());
		assertSame(planeRefs[1], ds.getSubset(1).getData());
		assertSame(planeRefs[3], ds.getSubset(2).getData());

		// test that each original plane is unchanged
		for (int x = 0; x < 2; x++)
			for (int y = 0; y < 3; y++)
				for (int z = 0; z < 4; z++)
				{
					if (z == 2)
						break;
					
					int newPlaneIndex = (z < 2 ? z : z-1);
					
					assertEquals(100*x + 10*y + z, ds.getDouble(new int[]{x,y,newPlaneIndex}), 0);
				}
	}

	@Test
	public void testGetSubsetInt()
	{
		int[] dimensions = new int[]{102,88,4};
		
		RealType<?> type = new FloatType();
		
		Image<?> image = createImage(type, dimensions);
		
		LegacyImgLibDataset ds = new LegacyImgLibDataset(image);
		
		Object[] planes = new Object[4];
		for (int i = 0; i < 4; i++)
		{
			planes[i] = ds.getSubset(i);
			assertNotNull(planes[i]);
			for (int j = 0; j < i; j++)
				assertNotSame(planes[i], planes[j]);
		}
		
	}

	@Test
	public void testGetSubsetIntArray()
	{
		int[] dimensions = new int[]{102,88,4,3};
		
		RealType<?> type = new UnsignedByteType();
		
		Image<?> image = createImage(type, dimensions);
		
		LegacyImgLibDataset ds = new LegacyImgLibDataset(image);
		
		Object[] planes = new Object[4*3];
		int planeNum = 0;
		for (int z = 0; z < 4; z++)
		{
			for (int t = 0; t < 3; t++)
			{
				int[] planeIndex = new int[]{z,t}; 
				planes[planeNum] = ds.getSubset(planeIndex);
				assertNotNull(planes[planeNum]);
				for (int j = 0; j < planeNum; j++)
					assertNotSame(planes[planeNum], planes[j]);
			}
		}
	}

	@Test
	public void testGetDoubleIntArray()
	{
		int[] dimensions = new int[]{2,3,4,5};
		
		RealType<?> type = new DoubleType();
		
		Image<DoubleType> image = (Image<DoubleType>) createImage(type, dimensions);
		
		LocalizableByDimCursor<DoubleType> cursor = image.createLocalizableByDimCursor();

		for (int x = 0; x < 2; x++)
		{
			for (int y = 0; y < 3; y++)
			{
				for (int z = 0; z < 4; z++)
				{
					for (int c = 0; c < 5; c++)
					{
						cursor.setPosition(new int[]{x,y,z,c});
						cursor.getType().setReal(1000*x + 100*y + 10*z + c);
					}
				}
			}
		}
		
		LegacyImgLibDataset ds = new LegacyImgLibDataset(image);
		
		for (int x = 0; x < 2; x++)
		{
			for (int y = 0; y < 3; y++)
			{
				for (int z = 0; z < 4; z++)
				{
					for (int c = 0; c < 5; c++)
					{
						assertEquals(1000*x + 100*y + 10*z + c, ds.getDouble(new int[]{x,y,z,c}), 0);
					}
				}
			}
		}
	}

	@Test
	public void testSetDoubleIntArrayDouble()
	{
		int[] dimensions = new int[]{2,3,4,5};
		
		RealType<?> type = new UnsignedShortType();

		Image<UnsignedShortType> image = (Image<UnsignedShortType>) createImage(type, dimensions);
		
		LegacyImgLibDataset ds = new LegacyImgLibDataset(image);

		for (int x = 0; x < 2; x++)
		{
			for (int y = 0; y < 3; y++)
			{
				for (int z = 0; z < 4; z++)
				{
					for (int c = 0; c < 5; c++)
					{
						ds.setDouble(new int[]{x,y,z,c}, x + 1*y + 100*z + 1000*c);
						assertEquals(x + 1*y + 100*z + 1000*c, ds.getDouble(new int[]{x,y,z,c}), 0);
					}
				}
			}
		}
	}

	@Test
	public void testGetDoubleIntArrayInt()
	{
		// Won't test - underlying method exercised elsewhere
	}

	@Test
	public void testSetDoubleIntArrayIntDouble()
	{
		// Won't test - underlying method exercised elsewhere
	}

	@Test
	public void testGetSubsetIntArrayInt()
	{
		// Won't test - underlying method exercised elsewhere
	}

	@Test
	public void testGetImage()
	{
		int[] dimensions = new int[]{2,3,4};
		
		RealType<?> type = new DoubleType();
		
		Image<DoubleType> image = (Image<DoubleType>) createImage(type, dimensions);
		
		LocalizableByDimCursor<DoubleType> cursor = image.createLocalizableByDimCursor();

		for (int x = 0; x < 2; x++)
		{
			for (int y = 0; y < 3; y++)
			{
				for (int z = 0; z < 4; z++)
				{
					cursor.setPosition(new int[]{x,y,z});
					cursor.getType().setReal(100*x + 10*y + z);
				}
			}
		}
		
		LegacyImgLibDataset ds = new LegacyImgLibDataset(image);

		assertSame(image, ds.getImage());
		
		Object[] planeRefs = new Object[4];
		for (int i = 0; i < 4; i++)
			planeRefs[i] = ds.getSubset(i).getData();

		ds.removeSubset(3);

		Image<?> newImage = ds.getImage();
		
		assertNotSame(image, newImage);
		
		assertArrayEquals(new int[]{2,3,3}, ds.getDimensions());
		
		assertSame(planeRefs[0], ds.getSubset(0).getData());
		assertSame(planeRefs[1], ds.getSubset(1).getData());
		assertSame(planeRefs[2], ds.getSubset(2).getData());

		// test that each original plane is unchanged
		for (int x = 0; x < 2; x++)
			for (int y = 0; y < 3; y++)
				for (int z = 0; z < 4; z++)
				{
					if (z == 3)
						break;
					
					int newPlaneIndex = (z < 3 ? z : z-1);
					
					assertEquals(100*x + 10*y + z, ds.getDouble(new int[]{x,y,newPlaneIndex}), 0);
				}
	}

}
