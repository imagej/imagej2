package imagej.process.operation;

import imagej.process.ImageUtils;
import imagej.process.Observer;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.cursor.special.RegionOfInterestCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public abstract class ManyCursorRoiOperation<T extends RealType<T>> {

		private Image<T>[] images;
		private int[][] origins;
		private int[][] spans;
		private Observer observer;
		
		protected ManyCursorRoiOperation(Image<T>[] images, int[][] origins, int[][] spans)
		{
			if ((images.length != origins.length) || (origins.length != spans.length))
				throw new IllegalArgumentException("ManyCursorRoiOperation(): lengths of all input parameters do not match");
			
			this.images = images;
			this.origins = origins.clone();
			this.spans = spans.clone();
			this.observer = null;
			
			for (int i = 0; i < this.images.length; i++)
				ImageUtils.verifyDimensions(this.images[i].getDimensions(), this.origins[i], this.spans[i]);
			
			
			for (int i = 1; i < this.spans.length; i++)
				if (ImageUtils.getTotalSamples(spans[0]) != ImageUtils.getTotalSamples(spans[i]))
					throw new IllegalArgumentException("ManyCursorRoiOperation(): span sizes differ");
		}
		
		public Image<T>[] getImages() { return images; }
		public int[][] getOrigins() { return origins; }
		public int[][] getSpans() { return spans; }

		public void addObserver(Observer o) { this.observer = o; }
		
		public abstract void beforeIteration(RealType<T> type);
		public abstract void insideIteration(RealType<T>[] samples);
		public abstract void afterIteration();

		private void collectSamples(RegionOfInterestCursor<T>[] cursors, RealType<T>[] samples)
		{
			for (int i = 0; i < cursors.length; i++)
				samples[i] = cursors[i].getType();
		}
		
		private boolean hasNext(RegionOfInterestCursor<T>[] cursors)
		{
			for (int i = 0; i < cursors.length; i++)
				if (!cursors[i].hasNext())
					return false;
					
			return true;
		}
		
		private void fwd(RegionOfInterestCursor<T>[] cursors)
		{
			for (int i = 0; i < cursors.length; i++)
				cursors[i].fwd();
		}
		
		private void close(Cursor<T>[] cursors)
		{
			for (int i = 0; i < cursors.length; i++)
				cursors[i].close();
		}
		
		public void execute()
		{
			if (this.observer != null)
				observer.init();

			// create cursors
			LocalizableByDimCursor<T>[] cursors = new LocalizableByDimCursor[images.length];
			for (int i = 0; i < images.length; i++)
				cursors[i] = images[i].createLocalizableByDimCursor();

			// create roiCursors
			RegionOfInterestCursor<T>[] roiCursors = new RegionOfInterestCursor[images.length];
			for (int i = 0; i < images.length; i++)
				roiCursors[i] = new RegionOfInterestCursor<T>(cursors[i], origins[i], spans[i]);

			// gather type info to pass along
			RealType<T>[] samples = new RealType[images.length];
			collectSamples(roiCursors,samples);

			// do the iteration
	
			beforeIteration(cursors[0].getType());  // pass along type info

			while (hasNext(roiCursors))
			{
				fwd(roiCursors);
				
				collectSamples(roiCursors,samples);
				
				insideIteration(samples);

				if (this.observer != null)
					observer.update();
			}
			
			afterIteration();

			// close the cursors
			
			close(roiCursors);
			close(cursors);
			
			if (this.observer != null)
				observer.done();
		}
}
