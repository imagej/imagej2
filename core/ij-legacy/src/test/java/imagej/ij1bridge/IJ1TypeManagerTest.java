package imagej.ij1bridge;

import static org.junit.Assert.assertEquals;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ShortProcessor;
import imagej.data.Types;

import org.junit.Test;

public class IJ1TypeManagerTest {

	@Test
	public void testGetTypeImageProcessor()
	{
		ByteProcessor bProc = new ByteProcessor(1, 1, new byte[1], null);
		assertEquals(Types.findType("8-bit unsigned"), IJ1TypeManager.getType(bProc));
		
		ShortProcessor sProc = new ShortProcessor(1, 1, new short[1], null);
		assertEquals(Types.findType("16-bit unsigned"), IJ1TypeManager.getType(sProc));

		FloatProcessor fProc = new FloatProcessor(1, 1, new float[1], null);
		assertEquals(Types.findType("32-bit float"), IJ1TypeManager.getType(fProc));

		ColorProcessor cProc = new ColorProcessor(1, 1, new int[1]);
		assertEquals(Types.findType("32-bit unsigned"), IJ1TypeManager.getType(cProc));
	}

	@Test
	public void testGetTypeImagePlus()
	{
		ImagePlus imp;
		
		ByteProcessor bProc = new ByteProcessor(1, 1, new byte[1], null);
		imp = new ImagePlus("zacko", bProc);
		assertEquals(Types.findType("8-bit unsigned"), IJ1TypeManager.getType(imp));
		
		ShortProcessor sProc = new ShortProcessor(1, 1, new short[1], null);
		imp = new ImagePlus("zacko", sProc);
		assertEquals(Types.findType("16-bit unsigned"), IJ1TypeManager.getType(imp));

		FloatProcessor fProc = new FloatProcessor(1, 1, new float[1], null);
		imp = new ImagePlus("zacko", fProc);
		assertEquals(Types.findType("32-bit float"), IJ1TypeManager.getType(imp));

		ColorProcessor cProc = new ColorProcessor(1, 1, new int[1]);
		imp = new ImagePlus("zacko", cProc);
		assertEquals(Types.findType("32-bit unsigned"), IJ1TypeManager.getType(imp));
	}
}
