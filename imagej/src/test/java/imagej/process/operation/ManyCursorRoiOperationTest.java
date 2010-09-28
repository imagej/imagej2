package imagej.process.operation;

import static org.junit.Assert.*;
import imagej.process.ImageUtils;
import imagej.process.ImgLibProcessor;
import imagej.process.operation.ManyCursorRoiOperation;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.ByteType;

import org.junit.Test;


public class ManyCursorRoiOperationTest {

	private class FakeManyCursorRoiOperation<T extends RealType<T>> extends ManyCursorRoiOperation<T>
	{
		public int beforeCalls = 0;
		public int insideCalls = 0;
		public int afterCalls = 0;
		
		protected FakeManyCursorRoiOperation(Image<T>[] images,	int[][] origins, int[][] spans)
		{
			super(images, origins, spans);
		}

		@Override
		public void beforeIteration(RealType<T> type) {
			assertTrue(insideCalls == 0);
			assertTrue(afterCalls == 0);
			beforeCalls++;
		}

		@Override
		public void insideIteration(RealType<T>[] samples) {
			assertTrue(beforeCalls == 1);
			assertTrue(afterCalls == 0);
			insideCalls++;
		}
		
		@Override
		public void afterIteration() {
			assertTrue(beforeCalls == 1);
			assertTrue(afterCalls == 0);
			afterCalls++;
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testAllMethods()
	{
		ImgLibProcessor<ByteType> proc = (ImgLibProcessor<ByteType>) ImageUtils.createProcessor(2, 3, new byte[]{1,2,3,4,5,6}, false);
		
		int[] origin = new int[]{0,0,0};

		int[] span = new int[]{2,3,1};
		
		Image<ByteType>[] images = new Image[]{proc.getImage(), proc.getImage(), proc.getImage()};

		int[][] origins = new int[][]{origin,origin,origin};
		
		int[][] spans = new int[][]{span,span,span};
		
		FakeManyCursorRoiOperation<ByteType> op = new FakeManyCursorRoiOperation<ByteType>(images,origins,spans);

		for (int i = 0; i < images.length; i++)
		{
			assertEquals(images[i], op.getImages()[i]);
			assertArrayEquals(origin, op.getOrigins()[i]);
			assertArrayEquals(span, op.getSpans()[i]);
		}
		
		op.execute();
		
		assertTrue(op.beforeCalls == 1);
		assertTrue(op.insideCalls == 6);
		assertTrue(op.afterCalls == 1);
	}
}
