package imagej.data.autoscale;

import static org.junit.Assert.assertEquals;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;

import org.junit.Test;
import org.scijava.Context;


public class ConfidenceIntervalAutoscaleMethodTest {

	Context context = new Context(AutoscaleService.class);

	@Test
	public void test() {
		AutoscaleService service = context.getService(AutoscaleService.class);
		AutoscaleMethod method = service.getAutoscaleMethod("Confidence Interval");
		Img<RealType> img = getImg();
		DataRange range = method.getRange(img);
		// System.out.println(range.get1());
		// System.out.println(range.get2());
		assertEquals(2, range.getMin(), 0);
		assertEquals(97, range.getMax(), 0);
	}

	private Img<RealType> getImg() {
		Img<ByteType> img = ArrayImgs.bytes(100);
		byte i = 0;
		for (ByteType b : img)
			b.set(i++);
		return (Img<RealType>) (Img) img;
	}
}
