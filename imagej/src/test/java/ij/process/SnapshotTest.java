package ij.process;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import mpicbg.imglib.type.numeric.real.*;
import mpicbg.imglib.container.array.ArrayContainerFactory;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.cursor.special.RegionOfInterestCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.image.ImageFactory;

public class SnapshotTest {

	// ************* Instance variables *******************************
	
	private Image<FloatType> image;
	private static final int WIDTH = 104, HEIGHT = 73, DEPTH = 18;
	private static int numDims;
	
	// ************* Helper methods *******************************
	
	@Before
	public void setup()
	{
		ImageFactory<FloatType> factory = new ImageFactory<FloatType>(new FloatType(),new ArrayContainerFactory());
		
		image = factory.createImage(new int[] {WIDTH, HEIGHT, DEPTH});
		
		int value = 0;
		
		Cursor<FloatType> cursor = image.createCursor();
		for (FloatType sample : cursor)
			sample.setReal(value++);
		
		cursor.close();
		
		numDims = image.getNumDimensions();
	}
	
	private void compareData(Image<FloatType> image, int[] origin, int[] span, Image<FloatType> snapData)
	{
		int[] snapOrigin = Index.create(origin.length);

		LocalizableByDimCursor<FloatType> imageCursor = image.createLocalizableByDimCursor();
		LocalizableByDimCursor<FloatType> snapCursor = snapData.createLocalizableByDimCursor();
		
		RegionOfInterestCursor<FloatType> imageRoiCursor = new RegionOfInterestCursor<FloatType>(imageCursor,origin,span);
		RegionOfInterestCursor<FloatType> snapRoiCursor = new RegionOfInterestCursor<FloatType>(snapCursor,snapOrigin,span);
		
		while ((imageRoiCursor.hasNext()) && (snapRoiCursor.hasNext()))
		{
			imageRoiCursor.fwd();
			snapRoiCursor.fwd();
			
			assertEquals(imageRoiCursor.getType().getRealDouble(), snapRoiCursor.getType().getRealDouble(), 0);
		}
	}
	
	private void tryCopyFromImage(int[] origin, int[] span)
	{
		setup();
		
		Snapshot<FloatType> snap = new Snapshot<FloatType>(image,origin,span);

		// make sure data is the same after snapshot
		compareData(image,origin,span,snap.getStorage());
		
		// set image data to something else
		float value = 4995632;
		Cursor<FloatType> cursor = image.createCursor();
		for (FloatType sample : cursor)
			sample.setReal(value--);
		cursor.close();
		
		// copy the data
		snap.copyFromImage(image, origin, span);

		// make sure data is the same again
		compareData(image,origin,span,snap.getStorage());
	}
	
	// ************* Tests *******************************

	@Test
	public void testSnapshot() {

		// create a snapshot of the first plane of the image
		
		int[] origin = Index.create(numDims);
		int[] span = Span.singlePlane(WIDTH,HEIGHT,numDims);
		Snapshot<FloatType> snap = new Snapshot<FloatType>(image,origin,span);
		
		assertNotNull(snap);
		Image<FloatType> snapData = snap.getStorage();
		assertEquals(numDims,snapData.getNumDimensions());
		assertEquals(WIDTH,snapData.getDimension(0));
		assertEquals(HEIGHT,snapData.getDimension(1));
		assertEquals(1,snapData.getDimension(2));
		compareData(image,origin,span,snapData);
		
		// create a snapshot of the whole image
		
		span = Span.create(new int[]{WIDTH,HEIGHT,DEPTH});
		snap = new Snapshot<FloatType>(image,origin,span);

		assertNotNull(snap);
		snapData = snap.getStorage();
		assertEquals(numDims,snapData.getNumDimensions());
		assertEquals(WIDTH,snapData.getDimension(0));
		assertEquals(HEIGHT,snapData.getDimension(1));
		assertEquals(DEPTH,snapData.getDimension(2));
		compareData(image,origin,span,snapData);
	}

	@Test
	public void testGetStorage() {
		
		int[] origin = Index.create(numDims);
		int[] span = Span.singlePlane(WIDTH,HEIGHT,numDims);
		Snapshot<FloatType> snap = new Snapshot<FloatType>(image,origin,span);
		
		assertNotNull(snap.getStorage());
	}

	@Test
	public void testCopyFromImage() {

		int[] origin, span;
		
		origin = Index.create(numDims);
		span = Span.singlePlane(WIDTH,HEIGHT,numDims);
		
		tryCopyFromImage(origin,span);
		
		origin = Index.create(new int[]{20,10,5});
		span = Span.create(new int[]{WIDTH/2,HEIGHT/2,DEPTH/2});
		
		tryCopyFromImage(origin,span);
	}

	@Test
	public void testPasteIntoImage() {
		
		// make a snapshot of test image
		
		int[] origin = Index.create(numDims);
		int[] span = Span.create(new int[]{WIDTH,HEIGHT,DEPTH});
		Snapshot<FloatType> snap = new Snapshot<FloatType>(image,origin,span);
		
		// make another image same dims as original with different data
		
		Image<FloatType> image2 = image.createNewImage();

		int value = 607;
		
		Cursor<FloatType> cursor = image2.createCursor();
		for (FloatType sample : cursor)
			sample.setReal(value++);
		
		cursor.close();
		
		// past into second image
		
		snap.pasteIntoImage(image2);
		
		// compare two images for equality
		compareData(image,origin,span,image2);
	}

	@Test
	public void testToString() {

		int[] origin = Index.create(numDims);
		int[] span = Span.singlePlane(WIDTH,HEIGHT,numDims);
		Snapshot<FloatType> snap = new Snapshot<FloatType>(image,origin,span);

		String expected = "snapshot(["+WIDTH+":"+HEIGHT+":"+1+"],["+
							origin[0]+":"+origin[1]+":"+origin[2]+"],["+
							span[0]+":"+span[1]+":"+span[2]+"])";
		
		assertEquals(expected,snap.toString());
		
		span = Span.create(new int[]{WIDTH,HEIGHT,DEPTH});
		snap = new Snapshot<FloatType>(image,origin,span);

		expected = "snapshot(["+WIDTH+":"+HEIGHT+":"+DEPTH+"],["+
						origin[0]+":"+origin[1]+":"+origin[2]+"],["+
						span[0]+":"+span[1]+":"+span[2]+"])";

		assertEquals(expected,snap.toString());
	}

}
