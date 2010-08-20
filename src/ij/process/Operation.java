package ij.process;

import ij.process.DualCursorRoiOperation;
import ij.process.PositionalOperation;
import ij.process.SingleCursorRoiOperation;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.cursor.special.RegionOfInterestCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

//****************** Operations methods *******************************************************

/*  idea to abstract the visiting of the image via applying Operations.	*/

public class Operation {
	
	public static <T extends RealType<T>> void apply(SingleCursorRoiOperation<T> op)
	{
		final LocalizableByDimCursor<T> imageCursor = op.getImage().createLocalizableByDimCursor();
		final RegionOfInterestCursor<T> imageRoiCursor = new RegionOfInterestCursor<T>( imageCursor, op.getOrigin(), op.getSpan() );
		
		op.beforeIteration(imageRoiCursor.getType());
		
		//iterate over all the pixels, of the selected image plane
		for (T sample : imageRoiCursor)
		{
			op.insideIteration(sample);
		}
		
		op.afterIteration();
		
		imageRoiCursor.close();
		imageCursor.close();
	}
	
	public static <T extends RealType<T>> void apply(DualCursorRoiOperation<T> op)
	{
		LocalizableByDimCursor<T> image1Cursor = op.getImage1().createLocalizableByDimCursor();
		LocalizableByDimCursor<T> image2Cursor = op.getImage2().createLocalizableByDimCursor();

		RegionOfInterestCursor<T> image1RoiCursor = new RegionOfInterestCursor<T>(image1Cursor, op.getOrigin1(), op.getSpan1());
		RegionOfInterestCursor<T> image2RoiCursor = new RegionOfInterestCursor<T>(image2Cursor, op.getOrigin2(), op.getSpan2());
		
		op.beforeIteration(image1Cursor.getType(),image2Cursor.getType());
		
		while (image1RoiCursor.hasNext() && image2RoiCursor.hasNext())
		{
			image1RoiCursor.fwd();
			image2RoiCursor.fwd();
			
			op.insideIteration(image1Cursor.getType(),image2Cursor.getType());
		}
		
		op.afterIteration();
		
		image1RoiCursor.close();
		image2RoiCursor.close();
		image1Cursor.close();
		image2Cursor.close();
	}
	
	public static <T extends RealType<T>> void apply(PositionalOperation<T> op)
	{
		Image<T> image = op.getImage();
		
		LocalizableByDimCursor<T> cursor = image.createLocalizableByDimCursor();
		
		int[] origin = op.getOrigin();
		int[] span = op.getSpan();
		
		int[] position = origin.clone();
		
		op.beforeIteration(cursor.getType());
		
		while (Index.isValid(position,origin,span))
		{
			cursor.setPosition(position);
				
			op.insideIteration(position.clone(),cursor.getType());  // clone so that users can manipulate without messing us up
			
			Index.increment(position,origin,span);
		}
		/*
		for (int y = 0; y < span[1]; y++) {
			for (int x = 0; x < span[0]; x++) {
				
				position[0] = x;
				position[1] = y;
				
				cursor.setPosition(position);
				
				op.insideIteration(position.clone(),cursor.getType());  // clone so that users can manipulate without messing us up
			}
		}
		*/

		op.afterIteration();

		cursor.close();	
	}
	
}
