package ij.process;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public abstract class ManyCursorRoiOperation<T extends RealType<T>> {

		private Image<T>[] images;
		private int[][] origins;
		private int[][] spans;
		
		protected ManyCursorRoiOperation(Image<T>[] images, int[][] origins, int[][] spans)
		{
			if ((images.length != origins.length) || (origins.length != spans.length))
				throw new IllegalArgumentException("ManyCursorRoiOperation(): lengths of all input parameters do not match");
			
			this.images = images;
			this.origins = origins.clone();
			this.spans = spans.clone();

			for (int i = 0; i < this.images.length; i++)
				ImageUtils.verifyDimensions(this.images[i].getDimensions(), this.origins[i], this.spans[i]);
			
			// TODO - something needed here to test that the spans are compatible. For instance it is possible to have a 5d span where 3 of the
			//        dimensions are 1 and a 2d span and you could synchronize ROI cursors across them. Could try to just have one span but then
			//        some cases might not work. Must think about a good test for span equivalence.
		}
		
		public Image<T>[] getImages() { return images; }
		public int[][] getOrigins() { return origins; }
		public int[][] getSpans() { return spans; }

		public abstract void beforeIteration(RealType<T>[] types);
		public abstract void insideIteration(RealType<T>[] samples);
		public abstract void afterIteration();
}
