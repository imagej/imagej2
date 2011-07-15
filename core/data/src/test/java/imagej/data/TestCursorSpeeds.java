package imagej.data;

import static org.junit.Assert.assertTrue;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Axes;
import net.imglib2.img.Axis;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.junit.Test;

public class TestCursorSpeeds {

	static final int X = 400;
	static final int Y = 400;
	static final int Z = 50;
	@Test
	public void testCursorSpeeds() {
		long[] dims = new long[]{X,Y,Z};
		Dataset ds1 = Dataset.create(new UnsignedByteType(), dims, "junk1", new Axis[]{Axes.X, Axes.Y, Axes.Z});
		Dataset ds2 = Dataset.create(new UnsignedByteType(), dims, "junk2", new Axis[]{Axes.X, Axes.Y, Axes.Z});
		Dataset ds3 = Dataset.create(new UnsignedByteType(), dims, "junk3", new Axis[]{Axes.X, Axes.Y, Axes.Z});

		fill(ds1);
		localizingCursorSpeedTest(ds1, ds2);
		cursorSpeedTest(ds1, ds3);

		assertTrue(true);
	}
	
	// -- helpers --
	
	private void fill(Dataset ds) {
		Img<? extends RealType<?>> image1 = ds.getImgPlus().getImg();
		Cursor<? extends RealType<?>> cursor = image1.cursor();
		for (long i = 0; i < X*Y*Z; i++) {
			cursor.next();
			cursor.get().setReal(i);
		}
	}

	private void speedTest(Img<? extends RealType<?>> img1,
		Img<? extends RealType<?>> img2,
		Cursor<? extends RealType<?>> inputCursor,
		String testName)
	{
		long start = System.currentTimeMillis();

		RandomAccess<? extends RealType<?>> outputAccessor = img2.randomAccess();
		
		long[] position = new long[img1.numDimensions()];
		
		while (inputCursor.hasNext()) {
			inputCursor.next();
			inputCursor.localize(position);
			outputAccessor.setPosition(position);
			double value = inputCursor.get().getRealDouble();
			outputAccessor.get().setReal(value);
		}

		long stop = System.currentTimeMillis();
		
		System.out.println(testName+" : elapsed time = "+(stop-start));
	}
	
	private void cursorSpeedTest(Dataset ds1, Dataset ds2) {
		for (int i = 0; i < 5; i++)
			speedTest(ds1.getImgPlus().getImg(), ds2.getImgPlus().getImg(),
				ds2.getImgPlus().getImg().cursor(),"ignore - regular cursor");
		speedTest(ds1.getImgPlus().getImg(), ds2.getImgPlus().getImg(),
			ds2.getImgPlus().getImg().cursor(),"regular cursor");
	}
	
	private void localizingCursorSpeedTest(Dataset ds1, Dataset ds2) {
		for (int i = 0; i < 5; i++)
			speedTest(ds1.getImgPlus().getImg(), ds2.getImgPlus().getImg(),
				ds2.getImgPlus().getImg().localizingCursor(),"ignore - localizing cursor");
		speedTest(ds1.getImgPlus().getImg(), ds2.getImgPlus().getImg(),
			ds2.getImgPlus().getImg().localizingCursor(),"localizing cursor");
	}
}
