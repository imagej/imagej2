package imagej.process.operation;


import static org.junit.Assert.*;
import imagej.SampleInfo.*;
import imagej.process.ImageUtils;
import imagej.process.ImgLibProcessor;
import imagej.process.Index;
import imagej.process.operation.PositionalSingleCursorRoiOperation;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.ByteType;

import org.junit.Test;

public class PositionalRoiOperationTest {

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
	@SuppressWarnings("unchecked")
	public void testApplyPositionalOperation()
	{
		ImgLibProcessor<ByteType> proc = (ImgLibProcessor<ByteType>) ImageUtils.createProcessor(2, 3, new byte[]{1,2,3,4,5,6}, ValueType.BYTE);
		
		int[] origin = new int[]{0,0,0};

		int[] span = new int[]{2,3,1};
		
		FakePositionalOperation<ByteType> op = new FakePositionalOperation<ByteType>(proc.getImage(),origin,span);
		
		op.execute();
		
		assertTrue(op.beforeCalls == 1);
		assertTrue(op.insideCalls == 6);
		assertTrue(op.afterCalls == 1);
	}
}
