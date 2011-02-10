package imagej.imglib.process.operation;

import static org.junit.Assert.*;
import imagej.imglib.ImageUtils;
import imagej.imglib.process.operation.ManyCursorRoiOperation;

import mpicbg.imglib.container.planar.PlanarContainerFactory;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.ByteType;

import org.junit.Test;


public class ManyCursorRoiOperationTest {

	private PlanarContainerFactory planarContainerFactory = new PlanarContainerFactory();

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
		Image<ByteType> image = ImageUtils.createImage(new ByteType(), planarContainerFactory, new int[]{2,3,1});
		
		int[] origin = new int[]{0,0,0};

		int[] span = new int[]{2,3,1};
		
		Image<ByteType>[] images = new Image[]{image, image, image};

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
