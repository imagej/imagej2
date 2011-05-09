package imagej.legacy;

import static org.junit.Assert.*;

import ij.ImagePlus;
import ij.gui.NewImage;
import ij.measure.Calibration;
import imagej.data.Dataset;

import net.imglib2.Cursor;
import net.imglib2.img.Axes;
import net.imglib2.img.Axis;
import net.imglib2.img.ImgPlus;
import net.imglib2.img.cell.CellImg;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.junit.Test;


public class LegacyImageMapTest {

	// -- instance variables --
	
	private LegacyImageMap map = new LegacyImageMap();

	// -- private interface --
	
	private void testMetadataSame(Dataset ds, ImagePlus imp) {
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
	
	private void testColorSame(Dataset ds, ImagePlus imp) {
		
		long[] dimensions = ds.getDims();
		
		int xIndex = ds.getAxisIndex(Axes.X);
		int yIndex = ds.getAxisIndex(Axes.Y);
		int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		int zIndex = ds.getAxisIndex(Axes.Z);
		int tIndex = ds.getAxisIndex(Axes.TIME);

		assertEquals(dimensions[xIndex],imp.getWidth());
		assertEquals(dimensions[yIndex],imp.getHeight());
		assertEquals(dimensions[cIndex],3);
		assertEquals(imp.getNChannels(),1);
		assertEquals(dimensions[zIndex],imp.getNSlices());
		assertEquals(dimensions[tIndex],imp.getNFrames());
		
		long[] pos = new long[dimensions.length];
		for (int t = 0; t < dimensions[tIndex]; t++) {
			pos[tIndex] = t;
			for (int z = 0; z < dimensions[zIndex]; z++) {
				pos[zIndex] = z;
				imp.setPositionWithoutUpdate(1, z+1, t+1);
				for (int y = 0; y < dimensions[yIndex]; y++) {
					pos[yIndex] = y;
					for (int x = 0; x < dimensions[xIndex]; x++) {
						pos[xIndex] = x;
						
						pos[cIndex] = 0;
						int r = (int) ds.getDoubleValue(pos);
						
						pos[cIndex] = 1;
						int g = (int) ds.getDoubleValue(pos);

						pos[cIndex] = 2;
						int b = (int) ds.getDoubleValue(pos);

						int ij1Value = imp.getProcessor().get(x,y);
						int ij2Value = 0xff000000 |	(r << 16) | (g << 8) | b;

						assertEquals(ij1Value, ij2Value);
					}
				}
			}
		}
		
		testMetadataSame(ds, imp);
	}
	
	private void testSame(Dataset ds, ImagePlus imp) {
		
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
		
		long[] pos = new long[dimensions.length];
		for (int t = 0; t < dimensions[tIndex]; t++) {
			pos[tIndex] = t;
			for (int c = 0; c < dimensions[cIndex]; c++) {
				pos[cIndex] = c;
				for (int z = 0; z < dimensions[zIndex]; z++) {
					pos[zIndex] = z;
					imp.setPositionWithoutUpdate(c+1, z+1, t+1);
					for (int y = 0; y < dimensions[yIndex]; y++) {
						pos[yIndex] = y;
						for (int x = 0; x < dimensions[xIndex]; x++) {
							pos[xIndex] = x;
							double ij1Value = imp.getProcessor().getf(x,y);
							double ij2Value = ds.getDoubleValue(pos);
							assertEquals(ij1Value, ij2Value, 0.0001);
						}
					}
				}
			}
		}
		
		testMetadataSame(ds, imp);
	}
	
	private void fill(Dataset ds) {
		Cursor<? extends RealType<?>> cursor = ds.getImgPlus().cursor();
		int val = 0;
		while(cursor.hasNext()) {
			cursor.next();
			cursor.get().setReal(val++ % 256);
		}
	}
	
	// -- public tests --
	
	@Test
	public void testRegisterDataset() {
		Dataset ds0;
		ImagePlus imp1, imp2;
		Axis[] axes = new Axis[]{ Axes.X, Axes.Y, Axes.CHANNEL, Axes.Z, Axes.TIME };

		// register a gray dataset
		ds0 = Dataset.create(new long[]{1,2,3,4,5}, "temp", axes, 16, false, false);
		fill(ds0);
		ds0.getImgPlus().setCalibration(5,0);
		ds0.getImgPlus().setCalibration(6,1);
		ds0.getImgPlus().setCalibration(1,2);
		ds0.getImgPlus().setCalibration(7,3);
		ds0.getImgPlus().setCalibration(8,4);
		imp1 = map.registerDataset(ds0);
		testSame(ds0, imp1);
		
		// try again
		imp2 = map.registerDataset(ds0);
		assertSame(imp1, imp2);
		testSame(ds0, imp2);
		
		// register a color dataset
		ds0 = Dataset.create(new long[]{1,2,3,4,5}, "temp", axes, 8, false, false);
		fill(ds0);
		ds0.getImgPlus().setCalibration(5,0);
		ds0.getImgPlus().setCalibration(6,1);
		ds0.getImgPlus().setCalibration(1,2);
		ds0.getImgPlus().setCalibration(7,3);
		ds0.getImgPlus().setCalibration(8,4);
		ds0.setRGBMerged(true);
		imp1 = map.registerDataset(ds0);
		testColorSame(ds0, imp1);

		// try again
		imp2 = map.registerDataset(ds0);
		assertSame(imp1, imp2);
		testColorSame(ds0, imp2);
	}

	@Test
	public void testRegisterLegacyImage() {
		ImagePlus imp;
		Dataset ds0,ds1;
		ImgPlus<?> imgPlus0;
		int c,z,t;
		
		// dataset that was not existing
		c = 3; z = 4; t = 5;
		imp = NewImage.createShortImage("name", 4, 7, c*z*t, NewImage.FILL_RAMP);
		imp.setDimensions(c, z, t);
		ds0 = map.registerLegacyImage(imp);
		testSame(ds0,imp);
		
		// dataset that exists and no changes
		imgPlus0 = ds0.getImgPlus();
		ds1 = map.registerLegacyImage(imp);
		assertSame(ds0,ds1);
		assertSame(imgPlus0,ds1.getImgPlus());
		testSame(ds1,imp);

		// dataset exists and some changes
		
		//   some change and not backed by planar access
		c = 3; z = 4; t = 5;
		CellImgFactory<UnsignedByteType> factory = new CellImgFactory<UnsignedByteType>();
		CellImg<UnsignedByteType,?> cellImg = factory.create(new long[]{4,7,c,z,t}, new UnsignedByteType());
		Axis[] ij1Axes = new Axis[]{ Axes.X, Axes.Y, Axes.CHANNEL, Axes.Z, Axes.TIME };
		ImgPlus<UnsignedByteType> cellImgPlus = new ImgPlus<UnsignedByteType>(cellImg,"temp",ij1Axes,new double[]{4,5,1,7,8});
		ds0 = new Dataset(cellImgPlus);
		imp = map.registerDataset(ds0);
		testSame(ds0,imp);
		assertFalse(0xff == imp.getProcessor().get(0,0));
		imp.getProcessor().set(0, 0, 0xff);  // MAKE THE CHANGE
		imgPlus0 = ds0.getImgPlus();
		ds1 = map.registerLegacyImage(imp);
		assertSame(ds0,ds1);
		assertNotSame(imgPlus0,ds1.getImgPlus());
		
		//   some change and is color
		c = 1; z = 4; t = 5;
		imp = NewImage.createRGBImage("name", 4, 7, c*z*t, NewImage.FILL_RAMP);
		imp.setDimensions(c, z, t);
		ds0 = map.registerLegacyImage(imp);
		testColorSame(ds0,imp);
		assertFalse(0xffffffff == imp.getProcessor().get(0,0));
		imp.getProcessor().set(0, 0, 0xffffffff);  // MAKE THE CHANGE
		imgPlus0 = ds0.getImgPlus();
		ds1 = map.registerLegacyImage(imp);
		assertSame(ds0,ds1);
		assertNotSame(imgPlus0,ds1.getImgPlus());
		testColorSame(ds1,imp);

		//   some change - dimensions changed
		c = 3; z = 4; t = 5;
		imp = NewImage.createFloatImage("name", 4, 7, c*z*t, NewImage.FILL_RAMP);
		imp.setDimensions(c, z, t);
		ds0 = map.registerLegacyImage(imp);
		testSame(ds0,imp);
		imp.getStack().deleteLastSlice();  // MAKE THE CHANGE
		imp.setStack(imp.getStack());
		imgPlus0 = ds0.getImgPlus();
		ds1 = map.registerLegacyImage(imp);
		assertSame(ds0,ds1);
		assertNotSame(imgPlus0,ds1.getImgPlus());
		testSame(ds1,imp);
		
		// some change - metadata only
		c = 3; z = 4; t = 5;
		imp = NewImage.createByteImage("name", 4, 7, c*z*t, NewImage.FILL_RAMP);
		imp.setDimensions(c, z, t);
		ds0 = map.registerLegacyImage(imp);
		testSame(ds0,imp);
		imp.getCalibration().pixelDepth = 99;  // MAKE THE CHANGE
		imgPlus0 = ds0.getImgPlus();
		ds1 = map.registerLegacyImage(imp);
		assertSame(ds0,ds1);
		assertSame(imgPlus0,ds1.getImgPlus());
		testSame(ds1,imp);
		
		// some change - pixel data that is correct by reference
		c = 2; z = 4; t = 5;
		imp = NewImage.createByteImage("name", 4, 7, c*z*t, NewImage.FILL_RAMP);
		imp.setDimensions(c, z, t);
		ds0 = map.registerLegacyImage(imp);
		testSame(ds0,imp);
		assertFalse(100 == imp.getProcessor().get(0,0));
		imp.getProcessor().set(0, 0, 100);  // MAKE THE CHANGE
		imgPlus0 = ds0.getImgPlus();
		ds1 = map.registerLegacyImage(imp);
		assertSame(ds0,ds1);
		assertSame(imgPlus0,ds1.getImgPlus());
		testSame(ds1,imp);
		
		// TODO - what if slices were reordered??? caught???
	}

}
