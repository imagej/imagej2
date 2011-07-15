package imagej.legacy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import imagej.data.Dataset;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Axes;
import net.imglib2.img.Axis;
import net.imglib2.img.ImgPlus;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.Test;

public class GrayscaleImageTranslatorTest {

	private enum DataType {BYTE, SHORT, FLOAT}
	
	private GrayscaleImageTranslator translator = new GrayscaleImageTranslator();

	// -- helper methods --
	
	private void fill(Dataset ds) {
		ImgPlus<? extends RealType<?>> data = ds.getImgPlus();
		Cursor<? extends RealType<?>> cursor = data.cursor();
		long i = 0;
		while (cursor.hasNext()) {
			cursor.next();
			cursor.get().setReal(i++);
		}
	}
	
	// -- helper tests --
	
	private void testDataSame(Dataset ds, ImagePlus imp, int x, int y, int c, int z, int t) {
		long[] dims = ds.getDims();
		
		int xIndex = ds.getAxisIndex(Axes.X);
		int yIndex = ds.getAxisIndex(Axes.Y);
		int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		int zIndex = ds.getAxisIndex(Axes.Z);
		int tIndex = ds.getAxisIndex(Axes.TIME);
		
		if (xIndex >= 0) assertEquals(x, dims[xIndex]);
		if (yIndex >= 0) assertEquals(y, dims[yIndex]);
		if (cIndex >= 0) assertEquals(c, dims[cIndex]);
		if (zIndex >= 0) assertEquals(z, dims[zIndex]);
		if (tIndex >= 0) assertEquals(t, dims[tIndex]);

		RandomAccess<? extends RealType<?>> accessor = ds.getImgPlus().randomAccess();
		
		long[] pos = new long[dims.length];
		
		for (int ti = 0; ti < t; ti++) {
			if (tIndex >= 0) pos[tIndex] = ti;
			for (int zi = 0; zi < z; zi++) {
				if (zIndex >= 0) pos[zIndex] = zi;
				for (int ci = 0; ci < c; ci++) {
					if (cIndex >= 0) pos[cIndex] = ci;
					int sliceNumber = ti*c*z + zi*c +ci;
					ImageProcessor proc = imp.getStack().getProcessor(sliceNumber+1);
					for (int yi = 0; yi < y; yi++) {
						pos[yIndex] = yi;
						for (int xi = 0; xi < x; xi++) {
							pos[xIndex] = xi;
							accessor.setPosition(pos);
							assertEquals(accessor.get().getRealDouble(), proc.getf(xi, yi), 0);
						}
					}
				}
			}
		}
	}

	private void testMetadataSame(Dataset ds, ImagePlus imp) {
		Axis[] axesPresent = ds.getAxes();

		// axes
		for (Axis axis : axesPresent) {
			int axisIndex = ds.getAxisIndex(axis);
			if (axisIndex >= 0) {
				if (axis == Axes.X) assertEquals(ds.axis(axisIndex), axis);
				if (axis == Axes.Y) assertEquals(ds.axis(axisIndex), axis);
				if (axis == Axes.CHANNEL) assertEquals(ds.axis(axisIndex), axis);
				if (axis == Axes.Z) assertEquals(ds.axis(axisIndex), axis);
				if (axis == Axes.TIME) assertEquals(ds.axis(axisIndex), axis);
			}
		}
		
		// type
		if (imp.getType() == ImagePlus.GRAY8)
			assertTrue(ds.getType() instanceof UnsignedByteType);
		if (imp.getType() == ImagePlus.GRAY16)
			assertTrue(ds.getType() instanceof UnsignedShortType);
		if (imp.getType() == ImagePlus.GRAY32)
			assertTrue(ds.getType() instanceof FloatType);

		// calibration
		Calibration cal = imp.getCalibration();
		int axisIndex;
		axisIndex = ds.getAxisIndex(Axes.X);
		if (axisIndex >= 0) assertEquals(ds.calibration(axisIndex), cal.pixelWidth, 0);
		axisIndex = ds.getAxisIndex(Axes.Y);
		if (axisIndex >= 0) assertEquals(ds.calibration(axisIndex), cal.pixelHeight, 0);
		axisIndex = ds.getAxisIndex(Axes.CHANNEL);
		if (axisIndex >= 0) assertEquals(ds.calibration(axisIndex), 1, 0);
		axisIndex = ds.getAxisIndex(Axes.Z);
		if (axisIndex >= 0) assertEquals(ds.calibration(axisIndex), cal.pixelDepth, 0);
		axisIndex = ds.getAxisIndex(Axes.TIME);
		if (axisIndex >= 0) assertEquals(ds.calibration(axisIndex), cal.frameInterval, 0);
		
		// name
		assertEquals(ds.getName(), imp.getTitle());
		
		// integer
		assertEquals(!ds.isInteger(), (imp.getType() == ImagePlus.GRAY32));
		
		// color
		assertFalse(ds.isRGBMerged());
		
		// signed data flag
		assertEquals(ds.isSigned(), (imp.getType() == ImagePlus.GRAY32));
		
	}
	
	private void testImageFromIJ1(DataType type, int x, int y, int c, int z, int t) {
		
		ImagePlus imp;
		switch (type) {
			case BYTE:
				imp = NewImage.createByteImage("byte image", x, y, c*z*t, NewImage.FILL_RAMP);
				break;
			case SHORT:
				imp = NewImage.createShortImage("short image", x, y, c*z*t, NewImage.FILL_RAMP);
				break;
			case FLOAT:
				imp = NewImage.createFloatImage("float image", x, y, c*z*t, NewImage.FILL_RAMP);
				break;
			default:
				throw new IllegalStateException();
		}
		imp.setDimensions(c, z, t);
		Calibration cal = new Calibration();
		cal.pixelHeight = 3;
		cal.pixelDepth = 4;
		cal.pixelWidth = 7;
		imp.setCalibration(cal);
		// CTR FIXME - Fix comparison tests.
//		Dataset ds = translator.createDataset(imp);
//		testDataSame(ds, imp, x, y, c, z, t);
//		testMetadataSame(ds, imp);
	}

	private void testImageFromIJ2(DataType type, int x, int y, int c, int z, int t) {
		
		Axes[] axes = new Axes[]{Axes.X, Axes.Y, Axes.CHANNEL, Axes.Z, Axes.TIME};
		
		Dataset ds;
		switch (type) {
			case BYTE:
				ds = Dataset.create(new long[]{x,y,c,z,t}, "byte image", axes, 8, false, false);
				break;
			case SHORT:
				ds = Dataset.create(new long[]{x,y,c,z,t}, "short image", axes, 16, false, false);
				break;
			case FLOAT:
				ds = Dataset.create(new long[]{x,y,c,z,t}, "float image", axes, 32, true, true);
				break;
			default:
				throw new IllegalStateException();
		}
		fill(ds);
		ds.setCalibration(3, 0);
		ds.setCalibration(7, 1);
		ds.setCalibration(1, 2);
		ds.setCalibration(9, 3);
		ds.setCalibration(1, 4);
		// CTR FIXME - Fix comparison tests.
//		ImagePlus imp = translator.createLegacyImage(ds);
//		testDataSame(ds, imp, x, y, c, z, t);
//		testMetadataSame(ds, imp);
	}

	// -- public tests --
	
	@Test
	public void testFromIJ1() {
		int x,y,c,z,t;
		
		x = 25; y = 35; c = 1; z = 1; t = 1; 
		testImageFromIJ1(DataType.BYTE, x, y, c, z, t);

		x = 95; y = 22; c = 3; z = 5; t = 7; 
		testImageFromIJ1(DataType.BYTE, x, y, c, z, t);

		x = 80; y = 91; c = 1; z = 1; t = 1; 
		testImageFromIJ1(DataType.SHORT, x, y, c, z, t);

		x = 80; y = 48; c = 5; z = 7; t = 3; 
		testImageFromIJ1(DataType.SHORT, x, y, c, z, t);

		x = 107; y = 185; c = 1; z = 1; t = 1; 
		testImageFromIJ1(DataType.FLOAT, x, y, c, z, t);

		x = 83; y = 56; c = 7; z = 3; t = 5; 
		testImageFromIJ1(DataType.FLOAT, x, y, c, z, t);
	}

	@Test
	public void testToImageJ1() {
		int x,y,c,z,t;
		
		x = 25; y = 35; c = 1; z = 1; t = 1; 
		testImageFromIJ2(DataType.BYTE, x, y, c, z, t);

		x = 95; y = 22; c = 3; z = 5; t = 7; 
		testImageFromIJ2(DataType.BYTE, x, y, c, z, t);

		x = 80; y = 91; c = 1; z = 1; t = 1; 
		testImageFromIJ2(DataType.SHORT, x, y, c, z, t);

		x = 80; y = 48; c = 5; z = 7; t = 3; 
		testImageFromIJ2(DataType.SHORT, x, y, c, z, t);

		x = 107; y = 185; c = 1; z = 1; t = 1; 
		testImageFromIJ2(DataType.FLOAT, x, y, c, z, t);

		x = 83; y = 56; c = 7; z = 3; t = 5; 
		testImageFromIJ2(DataType.FLOAT, x, y, c, z, t);
	}

	private void testOrdering(Axis[] axes) {
		//System.out.println("Testing order : "+axes[0]+","+axes[1]+","+axes[2]);
		int nullAxes = 0;
		for (Axis axis : axes)
			if (axis == null)
				nullAxes++;
		Axis[] fullAxes = new Axis[2 + axes.length - nullAxes];
		fullAxes[0] = Axes.X;
		fullAxes[1] = Axes.Y;
		int axisIndex = 2;
		for (int i = 0; i < axes.length; i++)
			if (axes[i] != null)
				fullAxes[axisIndex++] = axes[i];
		long[] dims = new long[fullAxes.length];
		dims[0] = 3;
		dims[1] = 1;
		for (int i = 2; i < dims.length; i++)
			dims[i] = 5 + i*2;
		Dataset ds = Dataset.create(new UnsignedByteType(), dims, "temp", fullAxes);
		int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		int zIndex = ds.getAxisIndex(Axes.Z);
		int tIndex = ds.getAxisIndex(Axes.TIME);
		long cCount = (cIndex < 0) ? 1 : dims[cIndex];   
		long zCount = (zIndex < 0) ? 1 : dims[zIndex];   
		long tCount = (tIndex < 0) ? 1 : dims[tIndex];
		long[] position = new long[dims.length];
		RandomAccess<? extends RealType<?>> accessor = ds.getImgPlus().randomAccess();
		for (int t = 0; t < tCount; t++) {
			if (tIndex >= 0) position[tIndex] = t;
			for (int z = 0; z < zCount; z++) {
				if (zIndex >= 0) position[zIndex] = z;
				for (int c = 0; c < cCount; c++) {
					if (cIndex >= 0) position[cIndex] = c;
					position[1] = 0;
					position[0] = 0;
					accessor.setPosition(position);
					accessor.get().setReal(c);
					position[0] = 1;
					accessor.setPosition(position);
					accessor.get().setReal(z);
					position[0] = 2;
					accessor.setPosition(position);
					accessor.get().setReal(t);
				}
			}
		}
		// CTR FIXME - Fix comparison tests.
//		ImagePlus imp = translator.createLegacyImage(ds);
//		for (int t = 0; t < tCount; t++) {
//			if (tIndex >= 0) position[tIndex] = t;
//			for (int z = 0; z < zCount; z++) {
//				if (zIndex >= 0) position[zIndex] = z;
//				for (int c = 0; c < cCount; c++) {
//					if (cIndex >= 0) position[cIndex] = c;
//					imp.setPositionWithoutUpdate(c+1, z+1, t+1);
//					ImageProcessor proc = imp.getProcessor();
//					position[1] = 0;
//					position[0] = 0;
//					accessor.setPosition(position);
//					assertEquals(accessor.get().getRealDouble(), proc.get(0,0), 0);
//					position[0] = 1;
//					accessor.setPosition(position);
//					assertEquals(accessor.get().getRealDouble(), proc.get(1,0), 0);
//					position[0] = 2;
//					accessor.setPosition(position);
//					assertEquals(accessor.get().getRealDouble(), proc.get(2,0), 0);
//				}
//			}
//		}
	}
	
	@Test
	public void testAxisOrderingIJ2DatasetToImageJ1() {
		Axis[] axes = new Axis[]{null, Axes.CHANNEL, Axes.Z, Axes.TIME};
		for (Axis outer : axes) {
			for (Axis middle : axes) {
				for (Axis inner : axes) {
					if (Utils.allNull(new Axis[]{outer,middle,inner})) continue;
					if (Utils.repeated(new Axis[]{outer,middle,inner})) continue;
					testOrdering(new Axis[]{outer,middle,inner});
				}
			}
		}
	}
}
