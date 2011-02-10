package imagej.imglib.process.operation;

import static org.junit.Assert.*;
import imagej.data.Types;
import imagej.imglib.ImageUtils;
import imagej.imglib.process.OldImageUtils;
import imagej.imglib.process.operation.GetPlaneOperation;
import imagej.process.Index;
import imagej.process.Span;

import mpicbg.imglib.container.ContainerFactory;
import mpicbg.imglib.container.array.ArrayContainerFactory;
import mpicbg.imglib.container.planar.PlanarContainerFactory;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.integer.IntType;
import mpicbg.imglib.type.numeric.integer.Unsigned12BitType;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;

import org.junit.Test;

public class GetPlaneOperationTest
{
	@Test
	public void testGetPlaneOperation()
	{
		Image<UnsignedByteType> image =
			ImageUtils.createImage(new UnsignedByteType(), new ArrayContainerFactory(), new int[]{1,2,3});
		
		LocalizableByDimCursor<UnsignedByteType> cursor = image.createLocalizableByDimCursor();
		
		cursor.setPosition(new int[]{0,0,0});
		cursor.getType().set(1);
		
		cursor.setPosition(new int[]{0,1,0});
		cursor.getType().set(2);
		
		cursor.setPosition(new int[]{0,0,1});
		cursor.getType().set(3);
		
		cursor.setPosition(new int[]{0,1,1});
		cursor.getType().set(4);
		
		cursor.setPosition(new int[]{0,0,2});
		cursor.getType().set(5);
		
		cursor.setPosition(new int[]{0,1,2});
		cursor.getType().set(6);
		
		int[] origin;
		int[] span;
		Object plane;
		GetPlaneOperation<UnsignedByteType> oper;
		short[] planeShorts;
		
		span = Span.singlePlane(1, 2, 3);
		origin = Index.create(0,0,new int[]{0});
		oper = new GetPlaneOperation<UnsignedByteType>(image, origin, span, Types.findType("16-bit signed"));
		oper.execute();
		plane = oper.getOutputPlane();

		assertTrue(plane instanceof short[]);
		planeShorts = (short[]) plane;
		assertEquals(2, planeShorts.length);
		assertEquals(1, planeShorts[0]);
		assertEquals(2, planeShorts[1]);
		
		span = Span.singlePlane(1, 2, 3);
		origin = Index.create(0,0,new int[]{1});
		oper = new GetPlaneOperation<UnsignedByteType>(image, origin, span, Types.findType("16-bit signed"));
		oper.execute();
		plane = oper.getOutputPlane();

		assertTrue(plane instanceof short[]);
		planeShorts = (short[]) plane;
		assertEquals(2, planeShorts.length);
		assertEquals(3, planeShorts[0]);
		assertEquals(4, planeShorts[1]);
		
		span = Span.singlePlane(1, 2, 3);
		origin = Index.create(0,0,new int[]{2});
		oper = new GetPlaneOperation<UnsignedByteType>(image, origin, span,  Types.findType("16-bit signed"));
		oper.execute();
		plane = oper.getOutputPlane();

		assertTrue(plane instanceof short[]);
		planeShorts = (short[]) plane;
		assertEquals(2, planeShorts.length);
		assertEquals(5, planeShorts[0]);
		assertEquals(6, planeShorts[1]);
	}

	@Test
	public void testGetOutputPlane()
	{
		Image<IntType> image = ImageUtils.createImage(new IntType(), new ArrayContainerFactory(), new int[]{1,2,3});
		
		int[] origin = Index.create(3);
		
		int[] span = Span.singlePlane(1, 2, 3);
		
		GetPlaneOperation<IntType> oper = new GetPlaneOperation<IntType>(image, origin, span, Types.findType("16-bit signed"));
		
		oper.execute();
		
		Object plane = oper.getOutputPlane();
		
		assertTrue(plane instanceof short[]);
		
		short[] planeShorts = (short[]) plane;
		
		assertEquals(2, planeShorts.length);
	}

	@Test
	public void testGetPlaneAs()
	{
		ContainerFactory cFact = new PlanarContainerFactory(); 
		
		Image<Unsigned12BitType> image =
			ImageUtils.createImage(new Unsigned12BitType(), cFact, new int[]{21,23,1});

		LocalizableByDimCursor<Unsigned12BitType> cursor = image.createLocalizableByDimCursor();
		
		cursor.setPosition(new int[]{0,0,0});
		cursor.getType().set((short) 1);
		assertEquals(1,cursor.getType().get());
		
		cursor.setPosition(new int[]{1,1,0});
		cursor.getType().set((short) 2);
		assertEquals(2,cursor.getType().get());
		
		cursor.setPosition(new int[]{2,2,0});
		cursor.getType().set((short) 4);
		assertEquals(4,cursor.getType().get());
		
		cursor.setPosition(new int[]{3,4,0});
		cursor.getType().set((short) 6);
		assertEquals(6,cursor.getType().get());
		
		cursor.setPosition(new int[]{7,5,0});
		cursor.getType().set((short) 8);
		assertEquals(8,cursor.getType().get());
		
		cursor.setPosition(new int[]{10,6,0});
		cursor.getType().set((short) 10);
		assertEquals(10,cursor.getType().get());
		
		assertEquals(21, OldImageUtils.getWidth(image));
		assertEquals(23, OldImageUtils.getHeight(image));

		int[] imglibPlane = (int[]) ImageUtils.getPlanarAccess(image).getPlane(0).getCurrentStorageArray();
		
		Object myPlane = GetPlaneOperation.getPlaneAs(image, new int[]{0}, Types.findType("12-bit unsigned"));
		
		assertNotNull(myPlane);
		assertTrue(myPlane instanceof int[]);
		int[] myPlaneArray = (int[])myPlane;
		assertEquals(imglibPlane.length, myPlaneArray.length);
		assertArrayEquals(imglibPlane, myPlaneArray);
	}

}
