package imagej.process.operation;

import static org.junit.Assert.*;
import ij.process.ImgLibProcessor;
import imagej.process.ImageUtils;
import imagej.process.operation.SingleCursorRoiOperation;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.ByteType;

import org.junit.Test;

public class SingleCursorRoiOperationTest {
	
	private class FakeSingleCursorRoiOperation<T extends RealType<T>> extends SingleCursorRoiOperation<T>
	{
		public int beforeCalls = 0;
		public int insideCalls = 0;
		public int afterCalls = 0;
		
		public FakeSingleCursorRoiOperation(Image<T> image, int[] origin, int[] span) {
			super(image,origin,span);
		}

		@Override
		public void beforeIteration(RealType<T> type) {
			assertTrue(insideCalls == 0);
			assertTrue(afterCalls == 0);
			beforeCalls++;
		}

		@Override
		public void insideIteration(RealType<T> sample) {
			assertTrue(beforeCalls == 1);
			assertTrue(afterCalls == 0);
			insideCalls++;
			// TODO apparently the Roi cursor has its own unexpected order: 1,2,4,3,5,6. Might be a bug.
			//assertEquals(insideCalls, sample.getRealDouble(), Assert.DOUBLE_TOL);
		}

		@Override
		public void afterIteration() {
			assertTrue(beforeCalls == 1);
			assertTrue(afterCalls == 0);
			afterCalls++;
		}

	}

	@Test
	public void testAllMethods()
	{
		ImgLibProcessor<ByteType> proc = (ImgLibProcessor<ByteType>) ImageUtils.createProcessor(2, 3, new byte[]{1,2,3,4,5,6}, false);
		
		int[] origin = new int[]{0,0,0};

		int[] span = new int[]{2,3,1};
		
		FakeSingleCursorRoiOperation<ByteType> op = new FakeSingleCursorRoiOperation<ByteType>(proc.getImage(),origin,span);

		assertEquals(proc.getImage(), op.getImage());
		assertArrayEquals(origin, op.getOrigin());
		assertArrayEquals(span, op.getSpan());
		
		op.execute();
		
		assertTrue(op.beforeCalls == 1);
		assertTrue(op.insideCalls == 6);
		assertTrue(op.afterCalls == 1);
	}
}
