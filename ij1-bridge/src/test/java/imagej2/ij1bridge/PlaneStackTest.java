package imagej2.ij1bridge;

import static org.junit.Assert.*;
import org.junit.Test;

import imagej2.UserType;
import imagej2.ij1bridge.PlaneStack;
import imagej2.imglib.process.ImageUtils;

import mpicbg.imglib.container.array.ArrayContainerFactory;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;

public class PlaneStackTest {
	
	@Test
	public void testPlaneStackImage()
	{
		Image<?> image =
			ImageUtils.createImage(new UnsignedByteType(), new ArrayContainerFactory(), new int[]{2,3,4});
		
		PlaneStack stack = new PlaneStack(image);
		
		assertNotNull(stack);
		assertEquals(4, stack.getNumPlanes());
	}

	@Test
	public void testPlaneStackIntIntFactory()
	{
		PlaneStack stack;

		try {
			stack = new PlaneStack(-1,-1,new ArrayContainerFactory());
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		try {
			stack = new PlaneStack(0,0,new ArrayContainerFactory());
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		stack = new PlaneStack(1,5,new ArrayContainerFactory());
		assertNotNull(stack);
		assertEquals(0, stack.getNumPlanes());
	}

	@Test
	public void testGetStorage()
	{
		Image<UnsignedByteType> image =
			ImageUtils.createImage(new UnsignedByteType(), new ArrayContainerFactory(), new int[]{2,3,4});
		
		PlaneStack stack = new PlaneStack(image);
		
		assertSame(image, stack.getStorage());
	}

	@Test
	public void testGetNumPlanes()
	{
		Image<UnsignedByteType> image =
			ImageUtils.createImage(new UnsignedByteType(), new ArrayContainerFactory(), new int[]{2,3,4});
		
		PlaneStack stack = new PlaneStack(image);
		
		assertEquals(4, stack.getNumPlanes());
	}

	@Test
	public void testGetEndPosition()
	{
		Image<UnsignedByteType> image =
			ImageUtils.createImage(new UnsignedByteType(), new ArrayContainerFactory(), new int[]{2,3,4});
		
		PlaneStack stack = new PlaneStack(image);
		
		assertEquals(4, stack.getEndPosition());
		
		stack.addPlane(UserType.UBYTE, new byte[6]);
		
		assertEquals(5, stack.getEndPosition());
	}

	@Test
	public void testInsertPlane()
	{
		PlaneStack stack = new PlaneStack(2,3,new ArrayContainerFactory());
		
		stack.addPlane(UserType.BYTE, new byte[]{1,1,1,1,1,1});
		stack.addPlane(UserType.BYTE, new byte[]{2,2,2,2,2,2});
		stack.addPlane(UserType.BYTE, new byte[]{3,3,3,3,3,3});
		stack.addPlane(UserType.BYTE, new byte[]{4,4,4,4,4,4});

		// insert before beginning
		try {
			stack.insertPlane(-1, UserType.BYTE, new byte[]{6,6,6,6,6,6});
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// insert after end+1
		try {
			stack.insertPlane(5, UserType.BYTE, new byte[]{6,6,6,6,6,6});
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// insert at front
		stack.insertPlane(0, UserType.BYTE, new byte[]{6,6,6,6,6,6});
		assertEquals(6,((byte[])stack.getPlane(0))[0]);
		assertEquals(1,((byte[])stack.getPlane(1))[0]);
		assertEquals(2,((byte[])stack.getPlane(2))[0]);
		assertEquals(3,((byte[])stack.getPlane(3))[0]);
		assertEquals(4,((byte[])stack.getPlane(4))[0]);
		
		// insert in middle
		stack.insertPlane(3, UserType.BYTE, new byte[]{7,7,7,7,7,7});
		assertEquals(6,((byte[])stack.getPlane(0))[0]);
		assertEquals(1,((byte[])stack.getPlane(1))[0]);
		assertEquals(2,((byte[])stack.getPlane(2))[0]);
		assertEquals(7,((byte[])stack.getPlane(3))[0]);
		assertEquals(3,((byte[])stack.getPlane(4))[0]);
		assertEquals(4,((byte[])stack.getPlane(5))[0]);

		// insert at end
		stack.insertPlane(6, UserType.BYTE, new byte[]{8,8,8,8,8,8});
		assertEquals(6,((byte[])stack.getPlane(0))[0]);
		assertEquals(1,((byte[])stack.getPlane(1))[0]);
		assertEquals(2,((byte[])stack.getPlane(2))[0]);
		assertEquals(7,((byte[])stack.getPlane(3))[0]);
		assertEquals(3,((byte[])stack.getPlane(4))[0]);
		assertEquals(4,((byte[])stack.getPlane(5))[0]);
		assertEquals(8,((byte[])stack.getPlane(6))[0]);
	}

	@Test
	public void testAddPlane()
	{
		Image<UnsignedByteType> image =
			ImageUtils.createImage(new UnsignedByteType(), new ArrayContainerFactory(), new int[]{2,3,4});
		
		PlaneStack stack = new PlaneStack(image);
		
		assertEquals(4, stack.getEndPosition());

		for (int i = 0; i < 5; i++)
		{
			byte[] bytes = new byte[6];
			
			for (int k = 0; k < bytes.length; k++)
				bytes[k] = (byte) (i+1);
			
			stack.addPlane(UserType.UBYTE, bytes);

			assertEquals(4+i+1, stack.getEndPosition());
			
			byte[] data = (byte[]) stack.getPlane(4+i);
			
			assertEquals(i+1, data[0]);
		}
	}

	@Test
	public void testDeletePlane()
	{
		PlaneStack stack;
		
		stack = new PlaneStack(2,3,new ArrayContainerFactory());

		// try to delete from an empty stack
		try {
			stack.deletePlane(0);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// now populate
		stack.addPlane(UserType.BYTE, new byte[]{1,1,1,1,1,1});
		stack.addPlane(UserType.BYTE, new byte[]{2,2,2,2,2,2});
		stack.addPlane(UserType.BYTE, new byte[]{3,3,3,3,3,3});
		stack.addPlane(UserType.BYTE, new byte[]{4,4,4,4,4,4});
		
		// illegal argument : before beginning of stack
		try {
			stack.deletePlane(-1);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		// illegal argument : after end of stack
		try {
			stack.deletePlane(4);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// delete first
		stack.deletePlane(0);
		assertEquals(3, stack.getNumPlanes());
		assertEquals(2,((byte[])stack.getPlane(0))[0]);
		assertEquals(3,((byte[])stack.getPlane(1))[0]);
		assertEquals(4,((byte[])stack.getPlane(2))[0]);

		// delete middle
		stack.deletePlane(1);
		assertEquals(2, stack.getNumPlanes());
		assertEquals(2,((byte[])stack.getPlane(0))[0]);
		assertEquals(4,((byte[])stack.getPlane(1))[0]);
		
		// delete end
		stack.deletePlane(1);
		assertEquals(1, stack.getNumPlanes());
		assertEquals(2,((byte[])stack.getPlane(0))[0]);
	}


	@Test
	public void testGetPlane()
	{
		PlaneStack stack = new PlaneStack(2,3,new ArrayContainerFactory());
		
		stack.addPlane(UserType.BYTE, new byte[]{1,1,1,1,1,1});
		stack.addPlane(UserType.BYTE, new byte[]{2,2,2,2,2,2});
		stack.addPlane(UserType.BYTE, new byte[]{3,3,3,3,3,3});
		stack.addPlane(UserType.BYTE, new byte[]{4,4,4,4,4,4});
		
		byte[] data;

		try {
			data = (byte[]) stack.getPlane(-1);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		data = (byte[]) stack.getPlane(0);
		assertEquals(1, data[0]);
		
		data = (byte[]) stack.getPlane(1);
		assertEquals(2, data[0]);
		
		data = (byte[]) stack.getPlane(2);
		assertEquals(3, data[0]);
		
		data = (byte[]) stack.getPlane(3);
		assertEquals(4, data[0]);

		try {
			data = (byte[]) stack.getPlane(4);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}
	
	@Test
	public void testAddTwoPlanesOfDifferentType()
	{
		PlaneStack stack = new PlaneStack(2,3,new ArrayContainerFactory());
		
		stack.insertPlane(0, UserType.FLOAT, new float[]{1,2,3,4,5,6});
		
		try {
			stack.insertPlane(0, UserType.BYTE, new byte[]{1,2,3,4,5,6});
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}
}
