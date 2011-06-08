package imagej.legacy;

import static org.junit.Assert.*;

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
import net.imglib2.img.cell.CellImg;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.junit.Test;


public class LegacyImageMapTest {

	// -- instance variables --
	
	private LegacyImageMap map = new LegacyImageMap();

	// -- private interface --
	
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
		TestUtils.testSame(ds0, imp1);
		
		// try again
		imp2 = map.registerDataset(ds0);
		assertSame(imp1, imp2);
		TestUtils.testSame(ds0, imp2);
		
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
		TestUtils.testColorSame(ds0, imp1);

		// try again
		imp2 = map.registerDataset(ds0);
		assertSame(imp1, imp2);
		TestUtils.testColorSame(ds0, imp2);
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
		TestUtils.testSame(ds0,imp);
		
		// dataset that exists and no changes
		imgPlus0 = ds0.getImgPlus();
		ds1 = map.registerLegacyImage(imp);
		assertSame(ds0,ds1);
		assertSame(imgPlus0,ds1.getImgPlus());
		TestUtils.testSame(ds1,imp);
	}

	@Test
	public void testReconciliation() {
		DatasetHarmonizer harmonizer = new DatasetHarmonizer(map.getTranslator());
		ImagePlus imp;
		Dataset ds0,ds1;
		ImgPlus<?> imgPlus0;
		int c,z,t;
		
		// dataset that was not existing
		c = 3; z = 4; t = 5;
		imp = NewImage.createShortImage("name", 4, 7, c*z*t, NewImage.FILL_RAMP);
		imp.setDimensions(c, z, t);
		ds0 = map.registerLegacyImage(imp);
		//map.reconcileDifferences(ds0, imp);
		TestUtils.testSame(ds0,imp);
		
		// dataset that exists and no changes
		imgPlus0 = ds0.getImgPlus();
		ds1 = map.registerLegacyImage(imp);
		//map.reconcileDifferences(ds1, imp);
		assertSame(ds0,ds1);
		assertSame(imgPlus0,ds1.getImgPlus());
		TestUtils.testSame(ds1,imp);

		// dataset exists and some changes
		
		//   some change and not backed by planar access
		c = 3; z = 4; t = 5;
		CellImgFactory<UnsignedByteType> factory = new CellImgFactory<UnsignedByteType>();
		CellImg<UnsignedByteType,?> cellImg = factory.create(new long[]{4,7,c,z,t}, new UnsignedByteType());
		Axis[] ij1Axes = new Axis[]{ Axes.X, Axes.Y, Axes.CHANNEL, Axes.Z, Axes.TIME };
		ImgPlus<UnsignedByteType> cellImgPlus = new ImgPlus<UnsignedByteType>(cellImg,"temp",ij1Axes,new double[]{4,5,1,7,8});
		ds0 = new Dataset(cellImgPlus);
		imp = map.registerDataset(ds0);
		//map.reconcileDifferences(ds0, imp);
		TestUtils.testSame(ds0,imp);
		assertFalse(0xff == imp.getProcessor().get(0,0));
		imp.getProcessor().set(0, 0, 0xff);  // MAKE THE CHANGE
		imgPlus0 = ds0.getImgPlus();
		ds1 = map.registerLegacyImage(imp);
		harmonizer.registerType(imp);
		harmonizer.updateDataset(ds1, imp);
		assertSame(ds0,ds1);
		//assertNotSame(imgPlus0,ds1.getImgPlus());
		
		//   some change and is color
		c = 1; z = 4; t = 5;
		imp = NewImage.createRGBImage("name", 4, 7, c*z*t, NewImage.FILL_RAMP);
		imp.setDimensions(c, z, t);
		ds0 = map.registerLegacyImage(imp);
		//map.reconcileDifferences(ds0, imp);
		TestUtils.testColorSame(ds0,imp);
		assertFalse(0xffffffff == imp.getProcessor().get(0,0));
		imp.getProcessor().set(0, 0, 0xffffffff);  // MAKE THE CHANGE
		imgPlus0 = ds0.getImgPlus();
		ds1 = map.registerLegacyImage(imp);
		harmonizer.registerType(imp);
		harmonizer.updateDataset(ds1, imp);
		assertSame(ds0,ds1);
		assertNotSame(imgPlus0,ds1.getImgPlus());
		TestUtils.testColorSame(ds1,imp);

		//   some change - dimensions changed
		c = 3; z = 4; t = 5;
		imp = NewImage.createFloatImage("name", 4, 7, c*z*t, NewImage.FILL_RAMP);
		imp.setDimensions(c, z, t);
		ds0 = map.registerLegacyImage(imp);
		//map.reconcileDifferences(ds0, imp);
		TestUtils.testSame(ds0,imp);
		imp.getStack().deleteLastSlice();  // MAKE THE CHANGE
		imp.setStack(imp.getStack());
		imgPlus0 = ds0.getImgPlus();
		ds1 = map.registerLegacyImage(imp);
		harmonizer.registerType(imp);
		harmonizer.updateDataset(ds1, imp);
		assertSame(ds0,ds1);
		assertNotSame(imgPlus0,ds1.getImgPlus());
		TestUtils.testSame(ds1,imp);
		
		// some change - metadata only
		c = 3; z = 4; t = 5;
		imp = NewImage.createByteImage("name", 4, 7, c*z*t, NewImage.FILL_RAMP);
		imp.setDimensions(c, z, t);
		ds0 = map.registerLegacyImage(imp);
		//map.reconcileDifferences(ds0, imp);
		TestUtils.testSame(ds0,imp);
		imp.getCalibration().pixelDepth = 99;  // MAKE THE CHANGE
		imgPlus0 = ds0.getImgPlus();
		ds1 = map.registerLegacyImage(imp);
		harmonizer.registerType(imp);
		harmonizer.updateDataset(ds1, imp);
		assertSame(ds0,ds1);
		assertSame(imgPlus0,ds1.getImgPlus());
		TestUtils.testSame(ds1,imp);
		
		// some change - pixel data that is correct by reference
		c = 2; z = 4; t = 5;
		imp = NewImage.createByteImage("name", 4, 7, c*z*t, NewImage.FILL_RAMP);
		imp.setDimensions(c, z, t);
		ds0 = map.registerLegacyImage(imp);
		//map.reconcileDifferences(ds0, imp);
		TestUtils.testSame(ds0,imp);
		assertFalse(100 == imp.getProcessor().get(0,0));
		imp.getProcessor().set(0, 0, 100);  // MAKE THE CHANGE
		imgPlus0 = ds0.getImgPlus();
		ds1 = map.registerLegacyImage(imp);
		harmonizer.registerType(imp);
		harmonizer.updateDataset(ds1, imp);
		assertSame(ds0,ds1);
		assertSame(imgPlus0,ds1.getImgPlus());
		TestUtils.testSame(ds1,imp);
	}
}
