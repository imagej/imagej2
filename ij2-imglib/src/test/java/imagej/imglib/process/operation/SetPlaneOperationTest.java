package imagej.imglib.process.operation;

import static org.junit.Assert.*;

import imagej.data.Types;
import imagej.imglib.process.ImageUtils;
import imagej.imglib.process.operation.SetPlaneOperation;
import imagej.process.Index;

import mpicbg.imglib.type.numeric.integer.*;
import mpicbg.imglib.image.*;

import mpicbg.imglib.container.planar.PlanarContainerFactory;

import org.junit.Test;

public class SetPlaneOperationTest {

	@Test
	public void testSetPlaneOperation()
	{
		int[] origin = Index.create(3);
		
		PlanarContainerFactory cFact = new PlanarContainerFactory();
		
		Image<IntType> image = ImageUtils.createImage(new IntType(), cFact, new int[]{3,2,1});

		SetPlaneOperation<IntType> planeOp;
		int[] imglibPlane;
		int[] inputPlane;
		
		// try a valid set operation
		inputPlane = new int[]{0,9,8,7,6,5};
		planeOp = new SetPlaneOperation<IntType>(image, origin, inputPlane, Types.findType("32-bit signed"));
		planeOp.execute();
		imglibPlane = (int[]) ImageUtils.getPlanarAccess(image).getPlane(0).getCurrentStorageArray();
		assertArrayEquals(inputPlane, imglibPlane);
		
		// try something type unsafe
		try {
			short[] badPlane = new short[]{0,9,8,7,6,5};
			planeOp = new SetPlaneOperation<IntType>(image, origin, badPlane, Types.findType("32-bit signed"));
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}

}
