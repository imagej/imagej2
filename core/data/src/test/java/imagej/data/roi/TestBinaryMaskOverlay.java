package imagej.data.roi;

import static org.junit.Assert.*;

import imagej.util.ColorRGB;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteOrder;
import java.util.Random;

import net.imglib2.RandomAccess;
import net.imglib2.RealRandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.NativeImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.basictypeaccess.BitAccess;
import net.imglib2.roi.BinaryMaskRegionOfInterest;
import net.imglib2.type.logic.BitType;

import org.junit.Test;

public class TestBinaryMaskOverlay {
	private Img<BitType> makeImg(boolean [][] imgArray) {
		NativeImg<BitType, ? extends BitAccess> img = new ArrayImgFactory<BitType>().createBitInstance(new long[] {imgArray.length, imgArray[0].length}, 1);
		BitType t = new BitType(img);
		img.setLinkedType(t);
		RandomAccess<BitType> ra = img.randomAccess();
		for (int i=0; i<imgArray.length; i++) {
			ra.setPosition(i, 0);
			for (int j=0; j<imgArray[i].length; j++) {
				ra.setPosition(j,1);
				ra.get().set(imgArray[i][j]);
			}
		}
		return img;
	}
	
	private BinaryMaskRegionOfInterest<BitType, Img<BitType>> makeRoi(boolean [][] imgArray) {
		return new BinaryMaskRegionOfInterest<BitType, Img<BitType>>(makeImg(imgArray));
	}
	
	private BinaryMaskOverlay makeOverlay(boolean [][] imgArray) {
		return 	new BinaryMaskOverlay(makeRoi(imgArray));
	}

	@Test
	public void testWriteExternal() {
		BinaryMaskOverlay overlay = makeOverlay(new boolean [][] { { true } });
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			ObjectOutputStream out = new ObjectOutputStream(os);
			out.writeObject(overlay);
		} catch (IOException e) {
			e.printStackTrace();
			throw new AssertionError(e.getMessage());
		}
		
	}

	@Test
	public void testReadExternal() {
		Random r = new Random(54321);
		for (int iter=0; iter<100; iter++) {
			boolean [][] imgArray = new boolean [5][5];
			for (int i=0; i<5; i++) {
				for (int j=0; j<5; j++) {
					imgArray[i][j] = r.nextBoolean();
				}
			}
			BinaryMaskOverlay overlay = makeOverlay(imgArray);
			ColorRGB colorIn = new ColorRGB(r.nextInt(256), r.nextInt(256), r.nextInt(256));
			overlay.setFillColor(colorIn);
			overlay.setAlpha(r.nextInt(256));
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			try {
				ObjectOutputStream out = new ObjectOutputStream(os);
				out.writeObject(overlay);
				byte [] buffer = os.toByteArray();
				ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer));
				Object o = in.readObject();
				assertEquals(0, in.available());
				assertTrue(o instanceof BinaryMaskOverlay);
				BinaryMaskOverlay overlayOut = (BinaryMaskOverlay)o;
				assertEquals(overlayOut.getAlpha(), overlay.getAlpha());
				ColorRGB fillOut = overlayOut.getFillColor();
				assertEquals(colorIn, fillOut);
				RealRandomAccess<BitType> ra = overlayOut.getRegionOfInterest().realRandomAccess();
				for (int i=0; i<5; i++) {
					ra.setPosition(i, 0);
					for (int j=0; j<5; j++) {
						ra.setPosition(j,1);
						assertEquals(imgArray[i][j], ra.get().get());
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new AssertionError(e.getMessage());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				throw new AssertionError(e.getMessage());
			}
		}
	}

	@Test
	public void testBinaryMaskOverlay() {
		makeOverlay(new boolean [][] { { true} });
	}

}
