package imagej.imglib.process.operation;


import static org.junit.Assert.*;
import imagej.imglib.ImageUtils;
import imagej.imglib.process.operation.PositionalSingleCursorRoiOperation;
import imagej.process.Index;

import mpicbg.imglib.container.planar.PlanarContainerFactory;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.ByteType;

import org.junit.Test;

public class PositionalRoiOperationTest {
	
	private PlanarContainerFactory planarContainerFactory = new PlanarContainerFactory();

	// ***** fake support classes  ****************************************************************************************
	
	private class FakePositionalOperation<T extends RealType<T>> extends PositionalSingleCursorRoiOperation<T>
	{
		public int beforeCalls = 0;
		public int insideCalls = 0;
		public int afterCalls = 0;
		int[] lastPos = null;
		
		FakePositionalOperation(Image<T> image, int[] origin, int[] span)
		{
			super(image,origin,span);
		}
		
		@Override
		public void beforeIteration(RealType<T> type)
		{
			assertTrue(insideCalls == 0);
			assertTrue(afterCalls == 0);
			beforeCalls++;
		}

		@Override
		public void insideIteration(int[] position, RealType<T> sample)
		{
			assertTrue(beforeCalls == 1);
			assertTrue(afterCalls == 0);
			insideCalls++;
			
			int[] expectedIndex;
			
			if (lastPos == null)
			{
				expectedIndex = getOrigin();
				assertArrayEquals(getOrigin(), position);
			}
			else
			{
				expectedIndex = lastPos.clone();
				Index.increment(expectedIndex, getOrigin(), getSpan());
				assertArrayEquals(expectedIndex,position);
			}
			lastPos = position.clone();
		}

		@Override
		public void afterIteration()
		{
			assertTrue(beforeCalls == 1);
			assertTrue(afterCalls == 0);
			afterCalls++;
		}
	}
	
	
	// ***** tests  ****************************************************************************************

	@Test
	public void testApplyPositionalOperation()
	{
		Image<ByteType> image = ImageUtils.createImage(new ByteType(), planarContainerFactory, new int[]{2,3,1});
		
		int[] origin = new int[]{0,0,0};

		int[] span = new int[]{2,3,1};
		
		FakePositionalOperation<ByteType> op = new FakePositionalOperation<ByteType>(image,origin,span);
		
		op.execute();
		
		assertTrue(op.beforeCalls == 1);
		assertTrue(op.insideCalls == 6);
		assertTrue(op.afterCalls == 1);
	}
}
