package imagej.legacy;

import static org.junit.Assert.assertEquals;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import imagej.data.Dataset;
import net.imglib2.RandomAccess;
import net.imglib2.img.Axes;
import net.imglib2.img.Axis;
import net.imglib2.type.numeric.RealType;


public class TestUtils {

	public static boolean allNull(Axis[] axes) {
		for (Axis axis : axes)
			if (axis != null)
				return false;
		return true;
	}
	
	public static boolean repeated(Axis[] axes) {
		int cCount = 0, zCount = 0, tCount = 0;
		for (Axis axis : axes) {
			if (axis == Axes.CHANNEL) cCount++;
			if (axis == Axes.Z) zCount++;
			if (axis == Axes.TIME) tCount++;
		}
		return (cCount > 1 || zCount > 1 || tCount > 1);
	}
	
	public static void testMetadataSame(Dataset ds, ImagePlus imp) {
		int xIndex = ds.getAxisIndex(Axes.X);
		int yIndex = ds.getAxisIndex(Axes.Y);
		int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		int zIndex = ds.getAxisIndex(Axes.Z);
		int tIndex = ds.getAxisIndex(Axes.TIME);
		Calibration cal = imp.getCalibration();
		
		assertEquals(ds.getName(), imp.getTitle());
		assertEquals(ds.calibration(xIndex), cal.pixelWidth, 0);
		assertEquals(ds.calibration(yIndex), cal.pixelHeight, 0);
		assertEquals(ds.calibration(cIndex), 1, 0);
		assertEquals(ds.calibration(zIndex), cal.pixelDepth, 0);
		assertEquals(ds.calibration(tIndex), cal.frameInterval, 0);
	}
	
	public static void testSame(Dataset ds, ImagePlus imp) {
		
		long[] dimensions = ds.getDims();
		
		int xIndex = ds.getAxisIndex(Axes.X);
		int yIndex = ds.getAxisIndex(Axes.Y);
		int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		int zIndex = ds.getAxisIndex(Axes.Z);
		int tIndex = ds.getAxisIndex(Axes.TIME);

		assertEquals(dimensions[xIndex],imp.getWidth());
		assertEquals(dimensions[yIndex],imp.getHeight());
		assertEquals(dimensions[cIndex],imp.getNChannels());
		assertEquals(dimensions[zIndex],imp.getNSlices());
		assertEquals(dimensions[tIndex],imp.getNFrames());
		
		RandomAccess<? extends RealType<?>> accessor = ds.getImgPlus().randomAccess();
		int ij1PlaneNumber = 1;
		long[] pos = new long[dimensions.length];
		for (int t = 0; t < dimensions[tIndex]; t++) {
			pos[tIndex] = t;
			for (int z = 0; z < dimensions[zIndex]; z++) {
				pos[zIndex] = z;
				for (int c = 0; c < dimensions[cIndex]; c++) {
					pos[cIndex] = c;
					ImageProcessor proc = imp.getStack().getProcessor(ij1PlaneNumber++);
					for (int y = 0; y < dimensions[yIndex]; y++) {
						pos[yIndex] = y;
						for (int x = 0; x < dimensions[xIndex]; x++) {
							pos[xIndex] = x;
							accessor.setPosition(pos);
							double ij1Value = proc.getf(x,y);
							double ij2Value = accessor.get().getRealDouble();
							if (Math.abs(ij1Value - ij2Value) > 0.1)
								System.out.println("x="+x+" y="+y+" c="+c+" z="+z+" t="+t+" && ij1="+ij1Value+" ij2="+ij2Value);
							assertEquals(ij1Value, ij2Value, 0.0001);
						}
					}
				}
			}
		}
		
		testMetadataSame(ds, imp);
	}

	public static void testColorSame(Dataset ds, ImagePlus imp) {
		
		long[] dimensions = ds.getDims();
		
		int xIndex = ds.getAxisIndex(Axes.X);
		int yIndex = ds.getAxisIndex(Axes.Y);
		int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		int zIndex = ds.getAxisIndex(Axes.Z);
		int tIndex = ds.getAxisIndex(Axes.TIME);

		assertEquals(dimensions[xIndex],imp.getWidth());
		assertEquals(dimensions[yIndex],imp.getHeight());
		assertEquals(dimensions[cIndex],3*imp.getNChannels());
		assertEquals(dimensions[zIndex],imp.getNSlices());
		assertEquals(dimensions[tIndex],imp.getNFrames());
		
		int c = imp.getNChannels();
		int z = imp.getNSlices();
		int t = imp.getNFrames();
		
		RandomAccess<? extends RealType<?>> accessor = ds.getImgPlus().randomAccess();
		long[] pos = new long[dimensions.length];
		int ijPlaneNumber = 1;
		for (int ti = 0; ti < t; ti++) {
			pos[tIndex] = ti;
			for (int zi = 0; zi < z; zi++) {
				pos[zIndex] = zi;
				for (int ci = 0; ci < c; ci++) {
					ImageProcessor proc = imp.getStack().getProcessor((int)ijPlaneNumber++);
					for (int y = 0; y < dimensions[yIndex]; y++) {
						pos[yIndex] = y;
						for (int x = 0; x < dimensions[xIndex]; x++) {
							pos[xIndex] = x;
							
							pos[cIndex] = 3*ci+0;
							accessor.setPosition(pos);
							int r = (int) accessor.get().getRealDouble();
							
							pos[cIndex] = 3*ci+1;
							accessor.setPosition(pos);
							int g = (int) accessor.get().getRealDouble();
	
							pos[cIndex] = 3*ci+2;
							accessor.setPosition(pos);
							int b = (int) accessor.get().getRealDouble();
	
							int ij1Value = proc.get(x,y);
							int ij2Value = 0xff000000 |	(r << 16) | (g << 8) | b;
	
							assertEquals(ij1Value, ij2Value);
						}
					}
				}
			}
		}
		
		testMetadataSame(ds, imp);
	}
}
