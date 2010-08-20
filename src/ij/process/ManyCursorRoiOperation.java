package ij.process;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public abstract class ManyCursorRoiOperation<T extends RealType<T>> {

		Image<T>[] images;
		int[][] origins;
		int[][] spans;
		
		protected ManyCursorRoiOperation(Image<T>[] images, int[][] origins, int[][] spans)
		{
			if ((images.length != origins.length) || (origins.length != spans.length))
				throw new IllegalArgumentException("ManyCursorRoiOperation(): lengths of all input parameters do not match");
			
			this.images = images;
			this.origins = origins.clone();
			this.spans = spans.clone();

			for (int i = 0; i < this.images.length; i++)
				ImageUtils.verifyDimensions(this.images[i].getDimensions(), this.origins[i], this.spans[i]);
		}
		
		public Image<T>[] getImages() { return images; }
		public int[][] getOrigins() { return origins; }
		public int[][] getSpans() { return spans; }

		public abstract void beforeIteration(RealType<T>[] types);
		public abstract void insideIteration(RealType<T>[] samples);
		public abstract void afterIteration();
}
