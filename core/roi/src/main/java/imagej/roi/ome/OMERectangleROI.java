//
// OMERectangleROI.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.roi.ome;

import imagej.roi.ImageJROI;
import mpicbg.imglib.roi.AbstractRegionOfInterest;
import mpicbg.imglib.roi.RegionOfInterest;
import ome.xml.model.Rectangle;

/**
 * @author leek
 *ImageJ ROI corresponding to the Omero rectangle ROI.
 */
public class OMERectangleROI extends OMEShapeROI<Rectangle> implements ImageJROI {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5766648749200171813L;

	public OMERectangleROI() {
		omeShape = new Rectangle();
	}
	class ORRegionOfInterest extends AbstractRegionOfInterest {

		protected ORRegionOfInterest() {
			super(2);
		}

		@Override
		protected boolean isMember(double[] position) {
			return ((omeShape.getX() <= position[0]) &&
					(omeShape.getX() + omeShape.getWidth() > position[0]) &&
					(omeShape.getY() <= position[1]) &&
					(omeShape.getY() + omeShape.getHeight() > position[1]));
		}

		@Override
		protected boolean nextRaster(long[] position, long[] end) {
			long x = getLongX();
			long y = getLongY();
			long x_end = getLongXEnd();
			long y_last = getLongYEnd() - 1;
			
			long next_y;
			if (position[1] < y) {
				next_y = y;
			} else if (position[1] == y) {
				if (position[0] < x) {
					next_y = y;
				} else {
					next_y = y + 1;
				}
			} else if (position[1] < y_last) {
				next_y = position[1] + 1;
			} else if ((position[1] == y_last) &&
					   (position[0] < x)) {
				next_y = y_last;
			} else {
				return false;
			}
			position[0] = x;
			position[1] = next_y;
			end[0] = x_end;
			end[1] = next_y;
			return true;
		}
		
		/* (non-Javadoc)
		 * @see mpicbg.imglib.roi.AbstractRegionOfInterest#size()
		 */
		@Override
		protected long size() {
			return getLongWidth() * getLongHeight();
		}

		/* (non-Javadoc)
		 * @see mpicbg.imglib.roi.AbstractRegionOfInterest#jumpFwd(long, long[], long[])
		 */
		@Override
		protected boolean jumpFwd(long steps, long[] position, long[] end) {
			long delta_x = position[0] - getLongX();
			long delta_y = position[1] - getLongY();
			long next_delta = delta_x + delta_y * getLongWidth();
			if (next_delta >= size()) return false;
			position[0] = getLongX() + (next_delta % getLongWidth());
			position[1] = getLongY() + (next_delta / getLongHeight());
			end[0] = getLongXEnd();
			end[1] = position[1];
			return true;
		}

		/* (non-Javadoc)
		 * @see mpicbg.imglib.roi.AbstractRegionOfInterest#getExtrema(long[], long[])
		 */
		@Override
		protected void getExtrema(long[] minima, long[] maxima) {
			minima[0] = getLongX();
			minima[1] = getLongY();
			maxima[0] = getLongXEnd() - 1;
			maxima[1] = getLongYEnd() - 1;
		}

		/* (non-Javadoc)
		 * @see mpicbg.imglib.roi.AbstractRegionOfInterest#getRealExtrema(double[], double[])
		 */
		@Override
		protected void getRealExtrema(double[] minima, double[] maxima) {
			minima[0] = omeShape.getX().doubleValue();
			minima[1] = omeShape.getY().doubleValue();
			maxima[0] = omeShape.getX().doubleValue() + omeShape.getWidth().doubleValue();
			maxima[1] = omeShape.getY().doubleValue() + omeShape.getHeight().doubleValue();
		}
		private long getLongX() {
			return (long)Math.ceil(omeShape.getX().doubleValue());
		}
		private long getLongY() {
			return (long)Math.ceil(omeShape.getY().doubleValue());
		}
		private long getLongXEnd() {
			return (long)(omeShape.getX().doubleValue() + omeShape.getWidth().doubleValue());
		}
		private long getLongYEnd() {
			return (long)(omeShape.getY().doubleValue() + omeShape.getHeight().doubleValue());
		}
		
		private long getLongWidth() {
			return getLongXEnd() - getLongX();
		}
		
		private long getLongHeight() {
			return getLongYEnd() - getLongY();
		}
	}

	@Override
	public RegionOfInterest getRegionOfInterest() {
		return new ORRegionOfInterest();
	}
	
}
