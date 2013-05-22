package imagej.data.autoscale;

import static org.junit.Assert.assertTrue;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.ops.util.Tuple2;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;

import org.junit.Test;
import org.scijava.Context;


public class ConfidenceIntervalAutoscaleMethodTest {

	Context context = new Context(AutoscaleService.class);

	@Test
	public void test() {
		AutoscaleService service = context.getService(AutoscaleService.class);
		AutoscaleMethod method = service.getAutoscaleMethod("95% CI");
		Img<RealType> img = getImg();
		Tuple2<Double, Double> range = method.getRange(img);
		System.out.println(range.get1());
		System.out.println(range.get2());
		assertTrue(true);
	}

	private Img<RealType> getImg() {
		Img<ByteType> img = ArrayImgs.bytes(100);
		byte i = 0;
		for (ByteType b : img)
			b.set(i++);
		return (Img<RealType>) (Img) img;
	}
}
