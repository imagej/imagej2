// Java API classes
import java.awt.Frame;
import java.lang.Integer;
import java.util.Random;

// ImageJ classes
import ij.*;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.MessageDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteBlitter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;


public class Filter_Rank  implements PlugInFilter {
	/**
	 *
	 * This is a ImageJ plugin that performs local histogram rank filtering.
	 * Produces local contrast-enhancement in 8 bit greyscale images.
	 *
	 * The user must provides the side length of a square window size which
	 * becomes the local submimage from which the rank is computed.
	 *
	 * The contrast-enhancing algorithm was proposed by
	 * Didier Pelat (Paris Observatory)  http://www.obspm.fr/
	 * and implemented by Artenum  http://www.artenum.com/
	 *
	 * Copyright (C) 2001 ARTENUM SARL.
	 *
	 * Originally released as "Contrast_Enhancer_", now "FilterRank_"
	 *
	 * This program is a free software; you can redistribute it and/or
	 * modify it under the terms of the GNU General Public license
	 * as published by the Free Software Foundation; either version 2
	 * of license, or (at your option) any later version.
	 *
	 * This program is distributed in the hope that it will be useful,
	 * but WITHOUT ANY WARRANTY; without even the implied warranty of
	 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
	 * General Public License for more details.
	 *
	 * You should have received a copy of the GNU General Public License along
	 * with this program; if not, write to the Free Software Foundation, Inc.,
	 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
	 *
	 * @author	Jean-Francois Leveque
	 * software@artenum.com
	 * @version	1.1.0
	 *
	 * Current FilterRank_ version 1.1.0 based on Contrast_Enhancer_ version 1.0.3
	 *
	 * Modified by G. Landini G.Landini@bham.ac.uk on 23/Nov/2003:
	 *
	 *  Changed "half-length" for "filter window size" (more intuitive)
	 *  Added option to do (or rather "not to do") rank randomisation, so please be
	 *   aware of this change in the algorithm
	 *  Added option for (not to) rescale to 8 bit range (to keep the real rank value)
	 *  Added option to overwrite the image data (no new image is created)
	 *  Added capability to filter stacks and to be recordable (for macro creation)
	 *  Moved dialog to setup()
	 *  Added "About..."
	 *
	 *  Pending issues / bugs:
	 *  When a stack is processed and "Create new image" is checked,
	 *  it creates individual images rather than a new stack.
	 *  As a temporary measure do run("Convert Images to Stack"); to merge result into a stack.
	 */

	private int halfLength; // Half length of the filter window size
	private static final int MIN_LENGTH=3; //Minimum length of windowsize value (3)

	/* Maximum lenght of windowsize: 46337 as otherwise
	* the ranking could go past java.lang.Integer.MAX_VALUE
	* If you want to go beyond that, you will have to alter this plugin (and
	* maybe ImageJ) to use Long instead of Integer. However are you sure that
	* you need a "local" window of 46000+ pixels?...
	*/
	private static final int MAX_LENGTH=46337;

	private int sideLength; //Pixel side length of the contrast-enhancing square: 2*halfLength+1
	private int maxRank; //Maximum possible rank for selected ROI (halfLength):sideLength*sideLength-1
	private static final int MAX_8BIT_RANK=255; //Maximum possible rank for making a 8 bit image (255).

	// Maximum value for an unsigned 32 bit integer (2^32-1).
	// Calculated as: (long) Integer.MAX_VALUE - Integer.MIN_VALUE .
	private static final long MAX_U_INT_VALUE = (long) Integer.MAX_VALUE - Integer.MIN_VALUE ;
	ImagePlus newImg; //Rank-filtered image.
	ImagePlus origImg; //Original image to be ranked.
	private int origHeight; //Original image height.
	private int origWidth; //Original image width.
	private ByteProcessor origBp; //ByteProcessor from the original image.
	private ByteProcessor tmpCopyBp; //Temporary ByteProcessor needed for this implementation.
	private Random rand; //Random number generator to make the a unique rank of the pixel in the window (makePixelFromRoi).

	protected boolean doIrescale;
	protected boolean doIrandomise;
	protected boolean doInewimage;
	protected int windowsize;


	public int setup(String arg, ImagePlus imp) {
		/**
		* This method gets a reference to the image to be locally ranked
		* and returns the filter capabilities.
		* @param	arg	calls for the "about" information.
		* @param	imp	This is the image to be processed
		*
		* @return	Returns DOES_8G + DOES_STACKS or DONE.
		*/

		if (arg.equals("about"))
			{showAbout(); return DONE;}

		// Gets a reference to the original image to be processed.
		this.origImg = imp ;

		// Gets the original image height and width.
		if (imp==null)
			return DONE;
		this.origHeight = this.origImg.getHeight();
		this.origWidth = this.origImg.getWidth();

		boolean askValue = true ;// Do we keep on asking the value?

		int value;// value returned from the dialog
		while (askValue) {
			// Gets the value from the dialog.
			GenericDialog gd = new GenericDialog("Parameters");
			gd.addNumericField("Window size (odd)",3,0);
			gd.addCheckbox("Randomise equal ranks",false);
			gd.addCheckbox("Rescale 8-bit space",false);
			gd.addCheckbox("Create new image",false);

			gd.showDialog();
			if ( gd.wasCanceled() )
				return DONE;

			windowsize=(int) gd.getNextNumber();
			if (windowsize/2==(double)windowsize/2.0) //even?
				windowsize++; //guarantees an odd number

			doIrandomise = gd.getNextBoolean ();
			doIrescale = gd.getNextBoolean ();
			doInewimage = gd.getNextBoolean ();

			/* Checks value correctness regarding MIN_LENGTH, MAX_LENGTH
			and the original image height and width and acts accordingly. */
			if ( checkSize(windowsize) ) {
				askValue = false ;
				this.halfLength = (windowsize-1)/2 ;
				this.sideLength = windowsize ;
				this.maxRank = this.sideLength * this.sideLength - 1 ;
				gd.dispose();
			}
		}
		return DOES_8G+DOES_STACKS;
	}

	public void run(ImageProcessor ip) {
		/**
		* This method creates a new locally ranked image from the input image.
		*
		* It returns when the result image is created or when the
		* window size dialog is canceled.
		*
		* @param	ip	This is the ImageProcessor of the image to be
		* 			processed. It is cast to be used as a ByteProcessor.
		*/

		/*	Get a ByteProcessor from the original image ImageProcessor
		to make a copy with extra stripes for future processing. */
		this.origBp = (ByteProcessor) ip ;

		// Make the ByteProcessor copy with extra stripes for future processing.
		copyOrig();

		// filter the image.
		enhance(origBp);
		return;
	}


	private boolean checkSize(int value) {
		/**
		 * This method checks if the window size value is correct regarding
		 * MIN_LENGTH, MAX_LENGTH and the original image height and width.
		 *
		 * @param	value	This is the value to be checked.
		 *
		 * @return	"true" if it's ok, pops a dialog and returns "false" if it is not.
		 */
		int maxLength;// max length of window size value for image

		if ( value < MIN_LENGTH ) {
			MessageDialog md = new MessageDialog(new Frame(),"Too small value",
			"The minimum value for the window size is "+MIN_LENGTH+".");
			md.dispose();
			return false;
		}

		if ( value > MAX_LENGTH ) {
			MessageDialog md = new MessageDialog(new Frame(), "Too large value",
			"The maximum value for the window size is " +MAX_LENGTH+ " because\n"+
			"otherwise, ranking could go past java.lang.Integer.MAX_VALUE.\n"+
			"If you want to go beyond that, you will have to alter this\n"+
			"plugin (and maybe ImageJ) to use Long instead of Integer types.");
			md.dispose();
			return false;
		}

		if ( ( this.origHeight < value ) || ( this.origWidth < value ) ) {
			maxLength = this.origHeight < this.origWidth ?
			(int) this.origHeight:
			(int) this.origWidth ;
			 MessageDialog md = new MessageDialog(new Frame(), "Invalid value",
			"The image must have width and length greater or equal\n"+
			"to "+ value + " if you want a window size of "+value+".\n"+
			"The maximum window size for the current image is "+ maxLength +".");
			md.dispose();
			return false;
		}
		return true;
	}

	private void copyOrig() {
		/**
		 * This method makes a ByteProcessor copy of the original image ByteProcessor
		 * with extra stripes for future processing.
		 */
		int copyHeight;
		int copyWidth;

		// Create ByteProcessor with necessary height and width.
		copyHeight = this.origHeight + 2 * this.halfLength ;
		copyWidth = this.origWidth + 2 * this.halfLength ;
		this.tmpCopyBp = new ByteProcessor(copyWidth, copyHeight);

		// Copy the original image to the upper left corner.
		this.tmpCopyBp.copyBits(this.origBp,0,0,ByteBlitter.COPY);

		// Copy the right stripe for future processing.
		this.tmpCopyBp.copyBits(this.origBp,origWidth,0,ByteBlitter.COPY);

		// Copy the lower stripe for future processing.
		this.tmpCopyBp.copyBits(this.origBp,0,origHeight,ByteBlitter.COPY);

		// Copy the lower right stripe for future processing.
		this.tmpCopyBp.copyBits(this.origBp,origWidth,origHeight,ByteBlitter.COPY);
	}


	private void enhance(ByteProcessor oBp) {
		/**
		 * This method calls the ranking routine on all regions
		 * of the image using the necessary stripes.
		 */
		ByteProcessor newBp;  // new image ByteProcessor
		int i; // original image x-coordinate
		int j ;// original image y-coordinate
		int i_plus; // original image x-coordinate translated into a copy stripe
		int j_plus; // original image y-coordinate translated into a copy stripe


		// Create a new image ByteProcessor, if required.
		if (doInewimage)
			newBp = new ByteProcessor(origWidth, origHeight);
		else
			newBp=oBp;

		// Initialize the random number generator used in makePixelFromRoi.
		rand = new Random();

		// Call upper left corner processing translating it right and down.
		for ( i = 0 , i_plus = origWidth ; i < halfLength ; i++ , i_plus++ ) {
			for ( j = 0 , j_plus = origHeight ; j < halfLength ; j++ , j_plus++ ) {
				makePixelFromRoi(i,j,i_plus,j_plus,newBp);
			}
		}

		// Call upper stripe processing translating it down.
		for ( i = halfLength ; i < origWidth ; i++ ) {
			for ( j = 0 , j_plus = origHeight ; j < halfLength ; j++ , j_plus++ ) {
				makePixelFromRoi(i,j,i,j_plus,newBp);
			}
		}

		// Call left stripe processing translating it right.
		for ( i = 0 , i_plus = origWidth ; i < halfLength ; i++ , i_plus++ ) {
			for ( j = halfLength ; j < origHeight ; j++ ) {
				makePixelFromRoi(i,j,i_plus,j,newBp);
			}
		}

		// Call processing on remaining image with no translation.
		for ( i = halfLength ; i < origWidth ; i++ ) {
			for ( j = halfLength ; j < origHeight ; j++ ) {
				makePixelFromRoi(i,j,i,j,newBp);
			}
		}

		// Create the new image from the ByteProcessor processed, otherwise uses original image
		if (doInewimage){
			this.newImg = new ImagePlus(origImg.getTitle()+" (Rank : " + windowsize + " )", newBp);
			this.newImg.show();
		}
	}


	private void makePixelFromRoi(int x, int y, int x_plus, int y_plus, ByteProcessor bp) {
		/**
		 * This method ranks a pixel using the histogram of the sideLength
		 * square ROI centered on it.
		 *
		 * The pixel value is set to its rank in the ROI.
		 *
		 * Because more than one pixel in the ROI may have the same rank, we can
		 * randomize the rank between rank and rank + number of pixels of the same
		 * rank. (doIrandomise) Otherwise it returns the minimum rank achieved.
		 *
		 * The ROI rank can also be converted into the 0-255 range of 8 bit grayscale images.
		 * (doIrescale), but be aware that the pixel values become re-scaled.
		 *
		 * @param	x	This is the original and new image pixel x-coordinate.
		 * @param	y	This is the original and new image pixel y-coordinate.
		 * @param	x_plus	This is the x-coordinate of the ROI selection in the
		 *			original image copy.
		 * @param	y_plus	This is the y-coordinate of the ROI selection in the
		 *			original image copy.
		 * @param	bp	This is the new ByteProcessor where ROI pixel value is
		 * 			set.
		 */

		int rank=0;// Rank of the pixel in the ROI (1st rank=0)
		int i;
		int[] histogram;
		int pixelValue;

		// Set the ROI around the pixel.
		this.tmpCopyBp.setRoi(x_plus - halfLength, y_plus - halfLength, sideLength, sideLength);

		histogram = this.tmpCopyBp.getHistogram(); // Get the histogram from the set ROI.

		pixelValue = this.origBp.getPixel(x,y);  // Get value from the pixel to be ranked.

		// Get the rank of all the pixels with the same value.
		for ( i = 0 ; i < pixelValue - 1 ; i++ )
			rank += histogram[i];

		// Randomise rank, if required.
		// Java Platform 1.2 API version
		//    rank += rand.nextInt(histogram[pixelValue]);
		//Java Platform 1.1.8 API version for ImageJ users with no Java 2 JRE
		if (doIrandomise)
			rank += (int) ( ( (double) rand.nextInt() - Integer.MIN_VALUE ) / MAX_U_INT_VALUE * ( histogram[pixelValue] ) );

		// Convert rank to the 8 bit grayscale images range, if required.
		// this could also be done afterwards by adjusting "brightness & contrast"
		if (doIrescale)
			rank = (int) ( (double) rank / this.maxRank * this.MAX_8BIT_RANK ) ;

    	//Set the pixel rank in the new ByteProcessor.
		bp.putPixel(x,y,rank);
	}

	void showAbout() {
		IJ.showMessage("Filter_Rank...",
		"FilterRank_ by Jean-Francois Leveque, modifications by G. Landini\n"+
		"This ImageJ plugin returns the rank (as intensity) of the central pixel\n"+
		"in the local neighbourhood (8 bit images only).\n"+
		"This procedure enhances the local contrast of the image.\n"+
		"It can sometimes produce visually pleasing results by randomising the\n"+
		"output values that have non-unique ranks, otherwise it will output\n"+
		"the minimum rank achieved by that value in the histogram.\n"+
		"Be aware that randomised ranks are (obviously) not reproducible! (you are\n"+
		"not very likely to get the same image twice).\n"+
		"The result can also be rescaled to span the 8 bit range.\n"+
		"The filtering may be slow when using large window sizes.");
	}

}

