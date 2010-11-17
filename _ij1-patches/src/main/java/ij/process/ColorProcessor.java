package ij.process;

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import ij.gui.*;
import ij.measure.Calibration;
import ij.ImageStack;

/**
This is an 32-bit RGB image and methods that operate on that image.. Based on the ImageProcessor class from
"KickAss Java Programming" by Tonny Espeset (http://www.sn.no/~espeset).
*/
public class ColorProcessor extends ImageProcessor {

	protected int[] pixels;
	protected int[] snapshotPixels = null;
	private int bgColor = 0xffffffff; //white
	private int min=0, max=255;
	private WritableRaster rgbRaster;
	private SampleModel rgbSampleModel;
	
	// Weighting factors used by getPixelValue(), getHistogram() and convertToByte().
	// Enable "Weighted RGB Conversion" in <i>Edit/Options/Conversions</i>
	// to use 0.299, 0.587 and 0.114.
	private static double rWeight=1d/3d, gWeight=1d/3d,	bWeight=1d/3d; 

	/**Creates a ColorProcessor from an AWT Image. */
	public ColorProcessor(Image img) {
		width = img.getWidth(null);
		height = img.getHeight(null);
		pixels = new int[width * height];
		PixelGrabber pg = new PixelGrabber(img, 0, 0, width, height, pixels, 0, width);
		try {
			pg.grabPixels();
		} catch (InterruptedException e){};
		createColorModel();
		fgColor = 0xff000000; //black
		resetRoi();
	}

	/**Creates a blank ColorProcessor of the specified dimensions. */
	public ColorProcessor(int width, int height) {
		this(width, height, new int[width*height]);
	}
	
	/**Creates a ColorProcessor from a pixel array. */
	public ColorProcessor(int width, int height, int[] pixels) {
		if (pixels!=null && width*height!=pixels.length)
			throw new IllegalArgumentException(WRONG_LENGTH);
		this.width = width;
		this.height = height;
		createColorModel();
		fgColor = 0xff000000; //black
		resetRoi();
		this.pixels = pixels;
	}


	void createColorModel() {
		cm = new DirectColorModel(24, 0xff0000, 0xff00, 0xff);
	}
	
	public Image createImage() {
		if (ij.IJ.isJava16()) return createBufferedImage();
		if (source==null) {
			source = new MemoryImageSource(width, height, cm, pixels, 0, width);
			source.setAnimated(true);
			source.setFullBufferUpdates(true);
			img = Toolkit.getDefaultToolkit().createImage(source);
		} else if (newPixels) {
			source.newPixels(pixels, cm, 0, width);
			newPixels = false;
		} else
			source.newPixels();
		return img;
	}

	Image createBufferedImage() {
		if (rgbSampleModel==null)
			rgbSampleModel = getRGBSampleModel();
		if (rgbRaster==null) {
			DataBuffer dataBuffer = new DataBufferInt(pixels, width*height, 0);
			rgbRaster = Raster.createWritableRaster(rgbSampleModel, dataBuffer, null);
		}
		if (image==null) {
			image = new BufferedImage(cm, rgbRaster, false, null);
		}
		return image;
	}

	SampleModel getRGBSampleModel() {
		WritableRaster wr = cm.createCompatibleWritableRaster(1, 1);
		SampleModel sampleModel = wr.getSampleModel();
		sampleModel = sampleModel.createCompatibleSampleModel(width, height);
		return sampleModel;
	}

	/** Returns a new, blank ColorProcessor with the specified width and height. */
	public ImageProcessor createProcessor(int width, int height) {
		ImageProcessor ip2 = new ColorProcessor(width, height);
		ip2.setInterpolationMethod(interpolationMethod);
		return ip2;
	}

	public Color getColor(int x, int y) {
		int c = pixels[y*width+x];
		int r = (c&0xff0000)>>16;
		int g = (c&0xff00)>>8;
		int b = c&0xff;
		return new Color(r,g,b);
	}


	/** Sets the foreground color. */
	public void setColor(Color color) {
		fgColor = color.getRGB();
		drawingColor = color;
	}


	/** Sets the fill/draw color, where <code>color</code> is an RGB int. */
	public void setColor(int color) {
		fgColor = color;
	}

	/** Sets the default fill/draw value, where <code>value</code> is interpreted as an RGB int. */
	public void setValue(double value) {
		fgColor = (int)value;
	}

	/** Sets the background fill value, where <code>value</code> is interpreted as an RGB int. */
	public void setBackgroundValue(double value) {
		bgColor = (int)value;
	}

	/** Returns the background fill value. */
	public double getBackgroundValue() {
		return bgColor;
	}

	/** Returns the smallest displayed pixel value. */
	public double getMin() {
		return min;
	}


	/** Returns the largest displayed pixel value. */
	public double getMax() {
		return max;
	}


	/** Uses a table look-up to map the pixels in this image from min-max to 0-255. */
	public void setMinAndMax(double min, double max) {
		setMinAndMax(min, max, 7);
	}


	public void setMinAndMax(double min, double max, int channels) {
		if (max<min)
			return;
		this.min = (int)min;
		this.max = (int)max;
		int v;
		int[] lut = new int[256];
		for (int i=0; i<256; i++) {
			v = i-this.min;
			v = (int)(256.0*v/(max-min));
			if (v < 0)
				v = 0;
			if (v > 255)
				v = 255;
			lut[i] = v;
		}
		reset();
		if (channels==7)
			applyTable(lut);
		else
			applyTable(lut, channels);
	}
	

	public void snapshot() {
		snapshotWidth = width;
		snapshotHeight = height;
		if (snapshotPixels==null || (snapshotPixels!=null && snapshotPixels.length!=pixels.length))
			snapshotPixels = new int[width * height];
		System.arraycopy(pixels, 0, snapshotPixels, 0, width*height);
	}


	public void reset() {
		if (snapshotPixels==null)
			return;
		System.arraycopy(snapshotPixels, 0, pixels, 0, width*height);
	}


	public void reset(ImageProcessor mask) {
		if (mask==null || snapshotPixels==null)
			return;	
		if (mask.getWidth()!=roiWidth||mask.getHeight()!=roiHeight)
			throw new IllegalArgumentException(maskSizeError(mask));
		byte[] mpixels = (byte[])mask.getPixels();
		for (int y=roiY, my=0; y<(roiY+roiHeight); y++, my++) {
			int i = y * width + roiX;
			int mi = my * roiWidth;
			for (int x=roiX; x<(roiX+roiWidth); x++) {
				if (mpixels[mi++]==0)
					pixels[i] = snapshotPixels[i];
				i++;
			}
		}
	}

	public void setSnapshotPixels(Object pixels) {
		snapshotPixels = (int[])pixels;
		snapshotWidth=width;
		snapshotHeight=height;
	}

	/** Returns a reference to the snapshot pixel array. Used by the ContrastAdjuster. */
	public Object getSnapshotPixels() {
		return snapshotPixels;
	}

	/** Fills pixels that are within roi and part of the mask.
		Does nothing if the mask is not the same as the the ROI. */
	public void fill(ImageProcessor mask) {
		if (mask==null)
			{fill(); return;}
		int roiWidth=this.roiWidth, roiHeight=this.roiHeight;
		int roiX=this.roiX, roiY=this.roiY;
		if (mask.getWidth()!=roiWidth||mask.getHeight()!=roiHeight)
			return;
		byte[] mpixels = (byte[])mask.getPixels();
		for (int y=roiY, my=0; y<(roiY+roiHeight); y++, my++) {
			int i = y * width + roiX;
			int mi = my * roiWidth;
			for (int x=roiX; x<(roiX+roiWidth); x++) {
				if (mpixels[mi++]!=0)
					pixels[i] = fgColor;
				i++;
			}
		}
	}

	/** Returns a copy of the pixel data. Or returns a reference to the
		snapshot buffer if it is not null and 'snapshotCopyMode' is true.
		@see ImageProcessor#snapshot
		@see ImageProcessor#setSnapshotCopyMode
	*/
	public Object getPixelsCopy() {
		if (snapshotPixels!=null && snapshotCopyMode) {
			snapshotCopyMode = false;
			return snapshotPixels;
		} else {
			int[] pixels2 = new int[width*height];
        	System.arraycopy(pixels, 0, pixels2, 0, width*height);
			return pixels2;
		}
	}

	public int getPixel(int x, int y) {
		if (x>=0 && x<width && y>=0 && y<height)
			return pixels[y*width+x];
		else
			return 0;
	}

	public final int get(int x, int y) {
		return pixels[y*width+x];
	}

	public final void set(int x, int y, int value) {
		pixels[y*width + x] = value;
	}

	public final int get(int index) {
		return pixels[index];
	}
	public final void set(int index, int value) {
		pixels[index] = value;
	}

	public final float getf(int x, int y) {
		return pixels[y*width+x];
	}

	public final void setf(int x, int y, float value) {
		pixels[y*width + x] = (int)value;
	}

	public final float getf(int index) {
		return pixels[index];
	}

	public final void setf(int index, float value) {
		pixels[index] = (int)value;
	}

    /** Returns the 3 samples for the pixel at (x,y) in an array of int.
		Returns zeros if the the coordinates are not in bounds. iArray
		is an optional preallocated array. */
	public int[] getPixel(int x, int y, int[] iArray) {
		if (iArray==null) iArray = new int[3];
		int c = getPixel(x, y);
		iArray[0] = (c&0xff0000)>>16;
		iArray[1] = (c&0xff00)>>8;
		iArray[2] = c&0xff;
		return iArray;
	}

	/** Sets a pixel in the image using a 3 element (R, G and B)
		int array of samples. */
	public final void putPixel(int x, int y, int[] iArray) {
		int r=iArray[0], g=iArray[1], b=iArray[2];
		putPixel(x, y, (r<<16)+(g<<8)+b);
	}

 	/** Calls getPixelValue(x,y). */
	public double getInterpolatedPixel(double x, double y) {
		int ix = (int)(x+0.5);
		int iy = (int)(y+0.5);
		if (ix<0) ix = 0;
		if (ix>=width) ix = width-1;
		if (iy<0) iy = 0;
		if (iy>=height) iy = height-1;
		return getPixelValue(ix, iy);
	}

	final public int getPixelInterpolated(double x,double y) {
		if (x<0.0 || y<0.0 || x>=width-1 || y>=height-1)
			return 0;
		else
			return getInterpolatedPixel(x, y, pixels);
	}

	/** Stores the specified value at (x,y). */
	public final void putPixel(int x, int y, int value) {
		if (x>=0 && x<width && y>=0 && y<height)
			pixels[y*width + x] = value;
	}

	/** Stores the specified real grayscale value at (x,y).
		Does nothing if (x,y) is outside the image boundary.
		The value is clamped to be in the range 0-255. */
	public void putPixelValue(int x, int y, double value) {
		if (x>=0 && x<width && y>=0 && y<height) {
			if (value>255.0)
				value = 255;
			else if (value<0.0)
				value = 0.0;
			int gray = (int)(value+0.5);
			pixels[y*width + x] = 0xff000000 + (gray<<16) + (gray<<8) + gray;

		}
	}

	/** Converts the specified pixel to grayscale using the
		formula g=(r+g+b)/3 and returns it as a float. 
		Call setWeightingFactors() to specify different conversion
		factors. */
	public float getPixelValue(int x, int y) {
		if (x>=0 && x<width && y>=0 && y<height) {
			int c = pixels[y*width+x];
			int r = (c&0xff0000)>>16;
			int g = (c&0xff00)>>8;
			int b = c&0xff;
			return (float)(r*rWeight + g*gWeight + b*bWeight);
		}
		else 
			return 0;
	}


	/** Draws a pixel in the current foreground color. */
	public void drawPixel(int x, int y) {
		if (x>=clipXMin && x<=clipXMax && y>=clipYMin && y<=clipYMax)
			pixels[y*width + x] = fgColor;
	}


	/**	Returns a reference to the int array containing
		this image's pixel data. */
	public Object getPixels() {
		return (Object)pixels;
	}


	public void setPixels(Object pixels) {
		this.pixels = (int[])pixels;
		resetPixels(pixels);
		if (pixels==null) snapshotPixels = null;
		rgbRaster = null;
		image = null;
	}


	/** Returns hue, saturation and brightness in 3 byte arrays. */
	public void getHSB(byte[] H, byte[] S, byte[] B) {
		int c, r, g, b;
		float[] hsb = new float[3];
		for (int i=0; i < width*height; i++) {
			c = pixels[i];
			r = (c&0xff0000)>>16;
			g = (c&0xff00)>>8;
			b = c&0xff;
			hsb = Color.RGBtoHSB(r, g, b, hsb);
			H[i] = (byte)((int)(hsb[0]*255.0));
			S[i] = (byte)((int)(hsb[1]*255.0));
			B[i] = (byte)((int)(hsb[2]*255.0));
		}
	}
	
	/** Returns an ImageStack with three 8-bit slices,
	    representing hue, saturation and brightness */
	public ImageStack getHSBStack() {
		int width = getWidth();
		int height = getHeight();
		byte[] H = new byte[width*height];
		byte[] S = new byte[width*height];
		byte[] B = new byte[width*height];
		getHSB(H, S, B);
		ColorModel cm = getDefaultColorModel();
		ImageStack stack = new ImageStack(width, height, cm);
		stack.addSlice("Hue", H);
		stack.addSlice("Saturation", S);
		stack.addSlice("Brightness", B);
		return stack;
	}

	/** Returns brightness as a FloatProcessor. */
	public FloatProcessor getBrightness() {
		int c, r, g, b;
		int size = width*height;
		float[] brightness = new float[size];
		float[] hsb = new float[3];
		for (int i=0; i<size; i++) {
			c = pixels[i];
			r = (c&0xff0000)>>16;
			g = (c&0xff00)>>8;
			b = c&0xff;
			hsb = Color.RGBtoHSB(r, g, b, hsb);
			brightness[i] = hsb[2];
		}
		return new FloatProcessor(width, height, brightness, null);
	}

	/** Returns the red, green and blue planes as 3 byte arrays. */
	public void getRGB(byte[] R, byte[] G, byte[] B)
	{
		ColorProcessor.getRGB(width, height, pixels, R, G, B);
	}
	
	public static void getRGB(int width, int height, int[] pixels, byte[] R, byte[] G, byte[] B) {
		int c, r, g, b;
		for (int i=0; i < width*height; i++) {
			c = pixels[i];
			r = (c&0xff0000)>>16;
			g = (c&0xff00)>>8;
			b = c&0xff;
			R[i] = (byte)r;
			G[i] = (byte)g;
			B[i] = (byte)b;
		}
	}


	/** Sets the current pixels from 3 byte arrays (reg, green, blue). */
	public void setRGB(byte[] R, byte[] G, byte[] B) {
		int c, r, g, b;
		for (int i=0; i < width*height; i++)
			pixels[i] = 0xff000000 | ((R[i]&0xff)<<16) | ((G[i]&0xff)<<8) | B[i]&0xff;
	}


	/** Sets the current pixels from 3 byte arrays (hue, saturation and brightness). */
	public void setHSB(byte[] H, byte[] S, byte[] B) {
		float hue, saturation, brightness;
		for (int i=0; i < width*height; i++) {
			hue = (float)((H[i]&0xff)/255.0);
			saturation = (float)((S[i]&0xff)/255.0);
			brightness = (float)((B[i]&0xff)/255.0);
			pixels[i] = Color.HSBtoRGB(hue, saturation, brightness);
		}
	}
	
	/** Updates the brightness using the pixels in the specified FloatProcessor). */
	public void setBrightness(FloatProcessor fp) {
		int c, r, g, b;
		int size = width*height;
		float[] hsb = new float[3];
		float[] brightness = (float[])fp.getPixels();
		if (brightness.length!=size)
			throw new IllegalArgumentException("fp is wrong size");
		for (int i=0; i<size; i++) {
			c = pixels[i];
			r = (c&0xff0000)>>16;
			g = (c&0xff00)>>8;
			b = c&0xff;
			hsb = Color.RGBtoHSB(r, g, b, hsb);
			float bvalue = brightness[i];
			if (bvalue<0f) bvalue = 0f;
			if (bvalue>1.0f) bvalue = 1.0f;
			pixels[i] = Color.HSBtoRGB(hsb[0], hsb[1], bvalue);
		}
	}
	
	/** Copies the image contained in 'ip' to (xloc, yloc) using one of
		the transfer modes defined in the Blitter interface. */
	public void copyBits(ImageProcessor ip, int xloc, int yloc, int mode) {
		ip = ip.convertToRGB();
		new ColorBlitter(this).copyBits(ip, xloc, yloc, mode);
	}

	/* Filters start here */

	public void applyTable(int[] lut) {
		int c, r, g, b;
		for (int y=roiY; y<(roiY+roiHeight); y++) {
			int i = y * width + roiX;
			for (int x=roiX; x<(roiX+roiWidth); x++) {
				c = pixels[i];
				r = lut[(c&0xff0000)>>16];
				g = lut[(c&0xff00)>>8];
				b = lut[c&0xff];
				pixels[i] = 0xff000000 + (r<<16) + (g<<8) + b;
				i++;
			}
		}
		showProgress(1.0);
	}
	
	public void applyTable(int[] lut, int channels) {
		int c, r=0, g=0, b=0;
		for (int y=roiY; y<(roiY+roiHeight); y++) {
			int i = y * width + roiX;
			for (int x=roiX; x<(roiX+roiWidth); x++) {
				c = pixels[i];
				if (channels==4) {
					r = lut[(c&0xff0000)>>16];
					g = (c&0xff00)>>8;
					b = c&0xff;
				} else if (channels==2) {
					r = (c&0xff0000)>>16;
					g = lut[(c&0xff00)>>8];
					b = c&0xff;
				} else if (channels==1) {
					r = (c&0xff0000)>>16;
					g = (c&0xff00)>>8;
					b = lut[c&0xff];
				} else if ((channels&6)==6) {
					r = lut[(c&0xff0000)>>16];
					g = lut[(c&0xff00)>>8];
					b = c&0xff;
				} else if ((channels&5)==5) {
					r = lut[(c&0xff0000)>>16];
					g = (c&0xff00)>>8;
					b = lut[c&0xff];
				} else if ((channels&3)==3) {
					r = (c&0xff0000)>>16;
					g = lut[(c&0xff00)>>8];
					b = lut[c&0xff];
				}
				pixels[i] = 0xff000000 + (r<<16) + (g<<8) + b;
				i++;
			}
		}
		showProgress(1.0);
	}

	/** Fills the current rectangular ROI. */
	public void fill() {
		for (int y=roiY; y<(roiY+roiHeight); y++) {
			int i = y * width + roiX;
			for (int x=roiX; x<(roiX+roiWidth); x++)
				pixels[i++] = fgColor;
			if (y%20==0)
				showProgress((double)(y-roiY)/roiHeight);
		}
		showProgress(1.0);
	}


	public static final int RGB_NOISE=0, RGB_MEDIAN=1, RGB_FIND_EDGES=2,
		RGB_ERODE=3, RGB_DILATE=4, RGB_THRESHOLD=5, RGB_ROTATE=6,
		RGB_SCALE=7, RGB_RESIZE=8, RGB_TRANSLATE=9;

 	/** Performs the specified filter on the red, green and blue planes of this image. */
 	public void filterRGB(int type, double arg) {
 		filterRGB(type, arg, 0.0);
 	}

 	final ImageProcessor filterRGB(int type, double arg, double arg2) {
		showProgress(0.01);
		byte[] R = new byte[width*height];
		byte[] G = new byte[width*height];
		byte[] B = new byte[width*height];
		ColorProcessor.getRGB(width, height, this.pixels, R, G, B);
		Rectangle roi = new Rectangle(roiX, roiY, roiWidth, roiHeight);
		
		ByteProcessor r = new ByteProcessor(width, height, R, null);
		r.setRoi(roi);
		ByteProcessor g = new ByteProcessor(width, height, G, null);
		g.setRoi(roi);
		ByteProcessor b = new ByteProcessor(width, height, B, null);
		b.setRoi(roi);
		r.setBackgroundValue((bgColor&0xff0000)>>16);
		g.setBackgroundValue((bgColor&0xff00)>>8);
		b.setBackgroundValue(bgColor&0xff);
		r.setInterpolationMethod(interpolationMethod);
		g.setInterpolationMethod(interpolationMethod);
		b.setInterpolationMethod(interpolationMethod);
		
		showProgress(0.15);
		switch (type) {
			case RGB_NOISE:
				r.noise(arg); showProgress(0.40);
				g.noise(arg); showProgress(0.65);
				b.noise(arg); showProgress(0.90);
				break;
			case RGB_MEDIAN:
				r.medianFilter(); showProgress(0.40);
				g.medianFilter(); showProgress(0.65);
				b.medianFilter(); showProgress(0.90);
				break;
			case RGB_FIND_EDGES:
				r.findEdges(); showProgress(0.40);
				g.findEdges(); showProgress(0.65);
				b.findEdges(); showProgress(0.90);
				break;
			case RGB_ERODE:
				r.erode(); showProgress(0.40);
				g.erode(); showProgress(0.65);
				b.erode(); showProgress(0.90);
				break;
			case RGB_DILATE:
				r.dilate(); showProgress(0.40);
				g.dilate(); showProgress(0.65);
				b.dilate(); showProgress(0.90);
				break;
			case RGB_THRESHOLD:
				r.autoThreshold(); showProgress(0.40);
				g.autoThreshold(); showProgress(0.65);
				b.autoThreshold(); showProgress(0.90);
				break;
			case RGB_ROTATE:
				ij.IJ.showStatus("Rotating red");
				r.rotate(arg); showProgress(0.40);
				ij.IJ.showStatus("Rotating green");
				g.rotate(arg); showProgress(0.65);
				ij.IJ.showStatus("Rotating blue");
				b.rotate(arg); showProgress(0.90);
				break;
			case RGB_SCALE:
				ij.IJ.showStatus("Scaling red");
				r.scale(arg, arg2); showProgress(0.40);
				ij.IJ.showStatus("Scaling green");
				g.scale(arg, arg2); showProgress(0.65);
				ij.IJ.showStatus("Scaling blue");
				b.scale(arg, arg2); showProgress(0.90);
				break;
			case RGB_RESIZE:
				int w=(int)arg, h=(int)arg2;
				ij.IJ.showStatus("Resizing red");
				ImageProcessor r2 = r.resize(w, h); showProgress(0.40);
				ij.IJ.showStatus("Resizing green");
				ImageProcessor g2 = g.resize(w, h); showProgress(0.65);
				ij.IJ.showStatus("Resizing blue");
				ImageProcessor b2 = b.resize(w, h); showProgress(0.90);
				R = (byte[])r2.getPixels();
				G = (byte[])g2.getPixels();
				B = (byte[])b2.getPixels();
				ColorProcessor ip2 = new ColorProcessor(w, h);
				ip2.setRGB(R, G, B);
				showProgress(1.0);
				return ip2;
			case RGB_TRANSLATE:
				ij.IJ.showStatus("Translating red");
				r.translate(arg, arg2); showProgress(0.40);
				ij.IJ.showStatus("Translating green");
				g.translate(arg, arg2); showProgress(0.65);
				ij.IJ.showStatus("Translating blue");
				b.translate(arg, arg2); showProgress(0.90);
				break;
		}
		
		R = (byte[])r.getPixels();
		G = (byte[])g.getPixels();
		B = (byte[])b.getPixels();
		
		setRGB(R, G, B);
		showProgress(1.0);
		return null;
	}

   public void noise(double range) {
    	filterRGB(RGB_NOISE, range);
    }

	public void medianFilter() {
    	filterRGB(RGB_MEDIAN, 0.0);
	}
	
	public void findEdges() {
    	filterRGB(RGB_FIND_EDGES, 0.0);
	}		
		
	public void erode() {
    	filterRGB(RGB_ERODE, 0.0);
	}
			
	public void dilate() {
    	filterRGB(RGB_DILATE, 0.0);

	}
			
	public void autoThreshold() {
   		filterRGB(RGB_THRESHOLD, 0.0);
	}
	
	/** Scales the image or selection using the specified scale factors.
		@see ImageProcessor#setInterpolate
	*/
	public void scale(double xScale, double yScale) {
        if (interpolationMethod==BICUBIC) {
        	filterRGB(RGB_SCALE, xScale, yScale);
        	return;
        }
		double xCenter = roiX + roiWidth/2.0;
		double yCenter = roiY + roiHeight/2.0;
		int xmin, xmax, ymin, ymax;
		
		if ((xScale>1.0) && (yScale>1.0)) {
			//expand roi
			xmin = (int)(xCenter-(xCenter-roiX)*xScale);
			if (xmin<0) xmin = 0;
			xmax = xmin + (int)(roiWidth*xScale) - 1;
			if (xmax>=width) xmax = width - 1;
			ymin = (int)(yCenter-(yCenter-roiY)*yScale);
			if (ymin<0) ymin = 0;
			ymax = ymin + (int)(roiHeight*yScale) - 1;
			if (ymax>=height) ymax = height - 1;
		} else {
			xmin = roiX;
			xmax = roiX + roiWidth - 1;
			ymin = roiY;
			ymax = roiY + roiHeight - 1;
		}
		int[] pixels2 = (int[])getPixelsCopy();
		boolean checkCoordinates = (xScale < 1.0) || (yScale < 1.0);
		int index1, index2, xsi, ysi;
		double ys, xs;
		double xlimit = width-1.0, xlimit2 = width-1.001;
		double ylimit = height-1.0, ylimit2 = height-1.001;
		for (int y=ymin; y<=ymax; y++) {
			ys = (y-yCenter)/yScale + yCenter;
			ysi = (int)ys;
			if (ys<0.0) ys = 0.0;			
			if (ys>=ylimit) ys = ylimit2;
			index1 = y*width + xmin;
			index2 = width*(int)ys;
			for (int x=xmin; x<=xmax; x++) {
				xs = (x-xCenter)/xScale + xCenter;
				xsi = (int)xs;
				if (checkCoordinates && ((xsi<xmin) || (xsi>xmax) || (ysi<ymin) || (ysi>ymax)))
					pixels[index1++] = bgColor;
				else {
					if (interpolationMethod==BILINEAR) {
						if (xs<0.0) xs = 0.0;
						if (xs>=xlimit) xs = xlimit2;
						pixels[index1++] = getInterpolatedPixel(xs, ys, pixels2);
					} else
						pixels[index1++] = pixels2[index2+xsi];
				}
			}
			if (y%20==0)
			showProgress((double)(y-ymin)/height);
		}
		showProgress(1.0);
	}

	public ImageProcessor crop() {
		int[] pixels2 = new int[roiWidth*roiHeight];
		for (int ys=roiY; ys<roiY+roiHeight; ys++) {
			int offset1 = (ys-roiY)*roiWidth;
			int offset2 = ys*width+roiX;
			for (int xs=0; xs<roiWidth; xs++)
				pixels2[offset1++] = pixels[offset2++];
		}
		return new ColorProcessor(roiWidth, roiHeight, pixels2);
	}
	
	/** Returns a duplicate of this image. */ 
	public synchronized ImageProcessor duplicate() { 
		int[] pixels2 = new int[width*height]; 
		System.arraycopy(pixels, 0, pixels2, 0, width*height); 
		return new ColorProcessor(width, height, pixels2); 
	} 

	/** Uses bilinear interpolation to find the pixel value at real coordinates (x,y). */
	public int getInterpolatedRGBPixel(double x, double y) {
		if (width==1||height==1)
			return getPixel((int)x, (int)y);
		if (x<0.0) x = 0.0;
		if (x>=width-1.0)
			x = width-1.001;
		if (y<0.0) y = 0.0;
		if (y>=height-1.0) y = height-1.001;
		return getInterpolatedPixel(x, y, pixels);
	}

	/** Uses bilinear interpolation to find the pixel value at real coordinates (x,y). */
	private final int getInterpolatedPixel(double x, double y, int[] pixels) {
		int xbase = (int)x;
		int ybase = (int)y;
		double xFraction = x - xbase;
		double yFraction = y - ybase;
		int offset = ybase * width + xbase;
		
		int lowerLeft = pixels[offset];
		int rll = (lowerLeft&0xff0000)>>16;
		int gll = (lowerLeft&0xff00)>>8;
		int bll = lowerLeft&0xff;
		
		int lowerRight = pixels[offset + 1];
		int rlr = (lowerRight&0xff0000)>>16;
		int glr = (lowerRight&0xff00)>>8;
		int blr = lowerRight&0xff;

		int upperRight = pixels[offset + width + 1];
		int rur = (upperRight&0xff0000)>>16;
		int gur = (upperRight&0xff00)>>8;
		int bur = upperRight&0xff;

		int upperLeft = pixels[offset + width];
		int rul = (upperLeft&0xff0000)>>16;
		int gul = (upperLeft&0xff00)>>8;
		int bul = upperLeft&0xff;
		
		int r, g, b;
		double upperAverage, lowerAverage;
		upperAverage = rul + xFraction * (rur - rul);
		lowerAverage = rll + xFraction * (rlr - rll);
		r = (int)(lowerAverage + yFraction * (upperAverage - lowerAverage)+0.5);
		upperAverage = gul + xFraction * (gur - gul);
		lowerAverage = gll + xFraction * (glr - gll);
		g = (int)(lowerAverage + yFraction * (upperAverage - lowerAverage)+0.5);
		upperAverage = bul + xFraction * (bur - bul);
		lowerAverage = bll + xFraction * (blr - bll);
		b = (int)(lowerAverage + yFraction * (upperAverage - lowerAverage)+0.5);

		return 0xff000000 | ((r&0xff)<<16) | ((g&0xff)<<8) | b&0xff;
	}

	/** Creates a new ColorProcessor containing a scaled copy of this image or selection.
		@see ImageProcessor#setInterpolate
	*/
	public ImageProcessor resize(int dstWidth, int dstHeight) {
        if (interpolationMethod==BICUBIC)
        	return filterRGB(RGB_RESIZE, dstWidth, dstHeight);
		double srcCenterX = roiX + roiWidth/2.0;
		double srcCenterY = roiY + roiHeight/2.0;
		double dstCenterX = dstWidth/2.0;
		double dstCenterY = dstHeight/2.0;
		double xScale = (double)dstWidth/roiWidth;
		double yScale = (double)dstHeight/roiHeight;
		double xlimit = width-1.0, xlimit2 = width-1.001;
		double ylimit = height-1.0, ylimit2 = height-1.001;
		if (interpolationMethod==BILINEAR) {
			if (xScale<=0.25 && yScale<=0.25)
				return makeThumbnail(dstWidth, dstHeight, 0.6);
			dstCenterX += xScale/2.0;
			dstCenterY += yScale/2.0;
		}
		ImageProcessor ip2 = createProcessor(dstWidth, dstHeight);
		int[] pixels2 = (int[])ip2.getPixels();
		double xs, ys;
		int index1, index2;
		for (int y=0; y<=dstHeight-1; y++) {
			ys = (y-dstCenterY)/yScale + srcCenterY;
			if (interpolationMethod==BILINEAR) {
				if (ys<0.0) ys = 0.0;
				if (ys>=ylimit) ys = ylimit2;
			}
			index1 = width*(int)ys;
			index2 = y*dstWidth;
			for (int x=0; x<=dstWidth-1; x++) {
				xs = (x-dstCenterX)/xScale + srcCenterX;
				if (interpolationMethod==BILINEAR) {
					if (xs<0.0) xs = 0.0;
					if (xs>=xlimit) xs = xlimit2;
					pixels2[index2++] = getInterpolatedPixel(xs, ys, pixels);
				} else
		  			pixels2[index2++] = pixels[index1+(int)xs];
			}
			if (y%20==0)
			showProgress((double)y/dstHeight);
		}
		showProgress(1.0);
		return ip2;
	}
	
	/** Uses averaging to creates a new ColorProcessor containing a scaled copy 
		of this image or selection. The amount of smoothing is determined by
		'smoothFactor', which must be greater than zero and less than or equal 1.0
	*/
	public ImageProcessor makeThumbnail(int width2, int height2, double smoothFactor) {
		ImageProcessor ip = this;
		if (roiWidth!=width || roiHeight!=height)
			ip = ip.crop();
		int width = ip.getWidth();
		int height = ip.getHeight();
		int[] pixel = new int[3];
		int[] sum = new int[3];
		double xscale, yscale;
		int w, h;
		double product;
		xscale = (double)width/width2;
		yscale = (double)height/height2;
		w = (int)(xscale*smoothFactor);
		h = (int)(yscale*smoothFactor);
		product = w*h;
		ImageProcessor ip2 = ip.createProcessor(width2, height2);
		for (int y=0; y<height2; y++) {
			for (int x=0; x<width2; x++) {
				for (int i=0; i<3; i++) sum[i] = 0;
				int xbase = (int)(x*xscale);
				int ybase = (int)(y*yscale);
				for (int y2=0; y2<h; y2++) {
					for (int x2=0;  x2<w; x2++) {
						pixel = ip.getPixel(xbase+x2, ybase+y2, pixel);
						for (int i=0; i<3; i++)
							sum[i] += pixel[i];
					}
				}
				for (int i=0; i<3; i++)
					sum[i] = (int)(sum[i]/product+0.5);
				ip2.putPixel(x, y, sum);
			}
		}
		return ip2;
	}

	/** Rotates the image or ROI 'angle' degrees clockwise.
		@see ImageProcessor#setInterpolationMethod
	*/
	public void rotate(double angle) {
        if (angle%360==0)
        	return;
        if (interpolationMethod==BICUBIC) {
        	filterRGB(RGB_ROTATE, angle);
        	return;
        }
		int[] pixels2 = (int[])getPixelsCopy();
		double centerX = roiX + (roiWidth-1)/2.0;
		double centerY = roiY + (roiHeight-1)/2.0;
		int xMax = roiX + this.roiWidth - 1;
		
		double angleRadians = -angle/(180.0/Math.PI);
		double ca = Math.cos(angleRadians);
		double sa = Math.sin(angleRadians);
		double tmp1 = centerY*sa-centerX*ca;
		double tmp2 = -centerX*sa-centerY*ca;
		double tmp3, tmp4, xs, ys;
		int index, ixs, iys;
		double dwidth = width, dheight=height;
		double xlimit = width-1.0, xlimit2 = width-1.001;
		double ylimit = height-1.0, ylimit2 = height-1.001;
		
		for (int y=roiY; y<(roiY + roiHeight); y++) {
			index = y*width + roiX;
			tmp3 = tmp1 - y*sa + centerX;
			tmp4 = tmp2 + y*ca + centerY;
			for (int x=roiX; x<=xMax; x++) {
				xs = x*ca + tmp3;
				ys = x*sa + tmp4;
				if ((xs>=-0.01) && (xs<dwidth) && (ys>=-0.01) && (ys<dheight)) {
					if (interpolationMethod==BILINEAR) {
						if (xs<0.0) xs = 0.0;
						if (xs>=xlimit) xs = xlimit2;
						if (ys<0.0) ys = 0.0;			
						if (ys>=ylimit) ys = ylimit2;
				  		pixels[index++] = getInterpolatedPixel(xs, ys, pixels2);
				  	} else {
				  		ixs = (int)(xs+0.5);
				  		iys = (int)(ys+0.5);
				  		if (ixs>=width) ixs = width - 1;
				  		if (iys>=height) iys = height -1;
						pixels[index++] = pixels2[width*iys+ixs];
					}
				} else
					pixels[index++] = bgColor;
			}
			if (y%30==0)
			showProgress((double)(y-roiY)/roiHeight);
		}
		showProgress(1.0);
	}
	
	public void flipVertical() {
		int index1,index2;
		int tmp;
		for (int y=0; y<roiHeight/2; y++) {
			index1 = (roiY+y)*width+roiX;
			index2 = (roiY+roiHeight-1-y)*width+roiX;
			for (int i=0; i<roiWidth; i++) {
				tmp = pixels[index1];
				pixels[index1++] = pixels[index2];
				pixels[index2++] = tmp;
			}
		}
	}
	
	/** 3x3 convolution contributed by Glynne Casteel. */
	public void convolve3x3(int[] kernel) {
		int p1, p2, p3, p4, p5, p6, p7, p8, p9;
		int k1=kernel[0], k2=kernel[1], k3=kernel[2],
		    k4=kernel[3], k5=kernel[4], k6=kernel[5],
		    k7=kernel[6], k8=kernel[7], k9=kernel[8];

		int scale = 0;
		for (int i=0; i<kernel.length; i++)
			scale += kernel[i];
		if (scale==0) scale = 1;
		int inc = roiHeight/25;
		if (inc<1) inc = 1;
		
		int[] pixels2 = (int[])getPixelsCopy();
		int offset;
		int rsum = 0, gsum = 0, bsum = 0;
        int rowOffset = width;
		for (int y=yMin; y<=yMax; y++) {
			offset = xMin + y * width;
			p1 = 0;
			p2 = pixels2[offset-rowOffset-1];
			p3 = pixels2[offset-rowOffset];
			p4 = 0;
			p5 = pixels2[offset-1];
			p6 = pixels2[offset];
			p7 = 0;
			p8 = pixels2[offset+rowOffset-1];
			p9 = pixels2[offset+rowOffset];

			for (int x=xMin; x<=xMax; x++) {
				p1 = p2; p2 = p3;
				p3 = pixels2[offset-rowOffset+1];
				p4 = p5; p5 = p6;
				p6 = pixels2[offset+1];
				p7 = p8; p8 = p9;
				p9 = pixels2[offset+rowOffset+1];

				rsum = k1*((p1 & 0xff0000) >> 16)
				     + k2*((p2 & 0xff0000) >> 16)
				     + k3*((p3 & 0xff0000) >> 16)
				     + k4*((p4 & 0xff0000) >> 16)
				     + k5*((p5 & 0xff0000) >> 16)
				     + k6*((p6 & 0xff0000) >> 16)
				     + k7*((p7 & 0xff0000) >> 16)
				     + k8*((p8 & 0xff0000) >> 16)
				     + k9*((p9 & 0xff0000) >> 16);
				rsum /= scale;
				if(rsum>255) rsum = 255;
				if(rsum<0) rsum = 0;

				gsum = k1*((p1 & 0xff00) >> 8)
				     + k2*((p2 & 0xff00) >> 8)
				     + k3*((p3 & 0xff00) >> 8)
				     + k4*((p4 & 0xff00) >> 8)
				     + k5*((p5 & 0xff00) >> 8)
				     + k6*((p6 & 0xff00) >> 8)
				     + k7*((p7 & 0xff00) >> 8)
				     + k8*((p8 & 0xff00) >> 8)
				     + k9*((p9 & 0xff00) >> 8);
				gsum /= scale;
				if(gsum>255) gsum = 255;
				else if(gsum<0) gsum = 0;

				bsum = k1*(p1 & 0xff)
				     + k2*(p2 & 0xff)
				     + k3*(p3 & 0xff)
				     + k4*(p4 & 0xff)
				     + k5*(p5 & 0xff)
				     + k6*(p6 & 0xff)
				     + k7*(p7 & 0xff)
				     + k8*(p8 & 0xff)
				     + k9*(p9 & 0xff);
				bsum /= scale;
				if (bsum>255) bsum = 255;
				if (bsum<0) bsum = 0; 

				pixels[offset++] = 0xff000000
				                 | ((rsum << 16) & 0xff0000)
				                 | ((gsum << 8 ) & 0xff00)
				                 |  (bsum        & 0xff);
			}
			if (y%inc==0)
				showProgress((double)(y-roiY)/roiHeight);
		}
		showProgress(1.0);
	}

	/** 3x3 unweighted smoothing. */
	public void filter(int type) {
		int p1, p2, p3, p4, p5, p6, p7, p8, p9;
		int inc = roiHeight/25;
		if (inc<1) inc = 1;
		
		int[] pixels2 = (int[])getPixelsCopy();
		int offset, rsum=0, gsum=0, bsum=0;
        int rowOffset = width;
		for (int y=yMin; y<=yMax; y++) {
			offset = xMin + y * width;
			p1 = 0;
			p2 = pixels2[offset-rowOffset-1];
			p3 = pixels2[offset-rowOffset];
			p4 = 0;
			p5 = pixels2[offset-1];
			p6 = pixels2[offset];
			p7 = 0;
			p8 = pixels2[offset+rowOffset-1];
			p9 = pixels2[offset+rowOffset];

			for (int x=xMin; x<=xMax; x++) {
				p1 = p2; p2 = p3;
				p3 = pixels2[offset-rowOffset+1];
				p4 = p5; p5 = p6;
				p6 = pixels2[offset+1];
				p7 = p8; p8 = p9;
				p9 = pixels2[offset+rowOffset+1];
				rsum = (p1 & 0xff0000) + (p2 & 0xff0000) + (p3 & 0xff0000) + (p4 & 0xff0000) + (p5 & 0xff0000)
					+ (p6 & 0xff0000) + (p7 & 0xff0000) + (p8 & 0xff0000) + (p9 & 0xff0000);
				gsum = (p1 & 0xff00) + (p2 & 0xff00) + (p3 & 0xff00) + (p4 & 0xff00) + (p5 & 0xff00)
					+ (p6 & 0xff00) + (p7 & 0xff00) + (p8 & 0xff00) + (p9 & 0xff00);
				bsum = (p1 & 0xff) + (p2 & 0xff) + (p3 & 0xff) + (p4 & 0xff) + (p5 & 0xff)
					+ (p6 & 0xff) + (p7 & 0xff) + (p8 & 0xff) + (p9 & 0xff);
				pixels[offset++] = 0xff000000 | ((rsum/9) & 0xff0000) | ((gsum/9) & 0xff00) | (bsum/9);
			}
			if (y%inc==0)
				showProgress((double)(y-roiY)/roiHeight);
		}
		showProgress(1.0);
	}

	public int[] getHistogram() {
		if (mask!=null)
			return getHistogram(mask);
		int c, r, g, b, v;
		int[] histogram = new int[256];
		for (int y=roiY; y<(roiY+roiHeight); y++) {
			int i = y * width + roiX;
			for (int x=roiX; x<(roiX+roiWidth); x++) {
				c = pixels[i++];
				r = (c&0xff0000)>>16;
				g = (c&0xff00)>>8;
				b = c&0xff;
				v = (int)(r*rWeight + g*gWeight + b*bWeight + 0.5);
				histogram[v]++;
			}
			if (y%20==0)
				showProgress((double)(y-roiY)/roiHeight);
		}
		showProgress(1.0);
		return histogram;
	}


	public int[] getHistogram(ImageProcessor mask) {
		if (mask.getWidth()!=roiWidth||mask.getHeight()!=roiHeight)
			throw new IllegalArgumentException(maskSizeError(mask));
		byte[] mpixels = (byte[])mask.getPixels();
		int c, r, g, b, v;
		int[] histogram = new int[256];
		for (int y=roiY, my=0; y<(roiY+roiHeight); y++, my++) {
			int i = y * width + roiX;
			int mi = my * roiWidth;
			for (int x=roiX; x<(roiX+roiWidth); x++) {
				if (mpixels[mi++]!=0) {
					c = pixels[i];
					r = (c&0xff0000)>>16;
					g = (c&0xff00)>>8;
					b = c&0xff;
					v = (int)(r*rWeight + g*gWeight + b*bWeight + 0.5);
					histogram[v]++;
				}
				i++;
			}
			if (y%20==0)
				showProgress((double)(y-roiY)/roiHeight);
		}
		showProgress(1.0);
		return histogram;
	}

	/** Performs a convolution operation using the specified kernel. */
	public void convolve(float[] kernel, int kernelWidth, int kernelHeight) {
		int size = width*height;
		byte[] r = new byte[size];
		byte[] g = new byte[size];
		byte[] b = new byte[size];
		ColorProcessor.getRGB(width,height,pixels,r,g,b);
		ImageProcessor rip = new ByteProcessor(width, height, r, null);
		ImageProcessor gip = new ByteProcessor(width, height, g, null);
		ImageProcessor bip = new ByteProcessor(width, height, b, null);
		ImageProcessor ip2 = rip.convertToFloat();
		Rectangle roi = getRoi();
		ip2.setRoi(roi);
		ip2.convolve(kernel, kernelWidth, kernelHeight);
		ImageProcessor r2 = ip2.convertToByte(false);
		ip2 = gip.convertToFloat();
		ip2.setRoi(roi);
		ip2.convolve(kernel, kernelWidth, kernelHeight);
		ImageProcessor g2 = ip2.convertToByte(false);
		ip2 = bip.convertToFloat();
		ip2.setRoi(roi);
		ip2.convolve(kernel, kernelWidth, kernelHeight);
		ImageProcessor b2 = ip2.convertToByte(false);
		setRGB((byte[])r2.getPixels(), (byte[])g2.getPixels(), (byte[])b2.getPixels());
   	}

	/** Sets the weighting factors used by getPixelValue(), getHistogram()
		and convertToByte() to do color conversions. The default values are
		1/3, 1/3 and 1/3. Check "Weighted RGB Conversions" in
		<i>Edit/Options/Conversions</i> to use 0.299, 0.587 and 0.114. */
	public static void setWeightingFactors(double rFactor, double gFactor, double bFactor) {
		rWeight = rFactor;
		gWeight = gFactor;
		bWeight = bFactor;
	}

	/** Returns the three weighting factors used by getPixelValue(), 
		getHistogram() and convertToByte() to do color conversions. */
	public static double[] getWeightingFactors() {
		double[] weights = new double[3];
		weights[0] = rWeight;
		weights[1] = gWeight;
		weights[2] = bWeight;
		return weights;
	}

	/** Always returns false since RGB images do not use LUTs. */
	public boolean isInvertedLut() {
		return false;
	}
	
	/** Always returns 0 since RGB images do not use LUTs. */
	public int getBestIndex(Color c) {
		return 0;
	}
	
	/** Does nothing since RGB images do not use LUTs. */
	public void invertLut() {
	}
	
	public void updateComposite(int[] rgbPixels, int channel) {
	}

	/** Not implemented. */
	public void threshold(int level) {}
	
	/** Returns the number of color channels of the image, i.e., 3. */
	public int getNChannels() {
		return 3;
	}
	
	/** Returns a FloatProcessor with one color channel of the image.
	*  The roi and mask are also set for the FloatProcessor.
	*  @param channelNumber   Determines the color channel, 0=red, 1=green, 2=blue
	*  @param fp              Here a FloatProcessor can be supplied, or null. The FloatProcessor
	*                         is overwritten by this method (re-using its pixels array 
	*                         improves performance).
	*  @return A FloatProcessor with the converted image data of the color channel selected
	*/
	public FloatProcessor toFloat(int channelNumber, FloatProcessor fp) {
		int size = width*height;
		if (fp == null || fp.getWidth()!=width || fp.getHeight()!=height)
			fp = new FloatProcessor(width, height, new float[size], null);
		float[] fPixels = (float[])fp.getPixels();
		int shift = 16 - 8*channelNumber;
		int byteMask = 255<<shift;
		for (int i=0; i<size; i++)
			fPixels[i] = (pixels[i]&byteMask)>>shift;
		fp.setRoi(getRoi());
		fp.setMask(mask);
		fp.setMinAndMax(0, 255);
		return fp;
	}
	
	/** Sets the pixels of one color channel from a FloatProcessor.
	*  @param channelNumber   Determines the color channel, 0=red, 1=green, 2=blue
	*  @param fp              The FloatProcessor where the image data are read from.
	*/
	public void setPixels(int channelNumber, FloatProcessor fp) {
		float[] fPixels = (float[])fp.getPixels();
		float value;
		int size = width*height;
		int shift = 16 - 8*channelNumber;
		int resetMask = 0xffffffff^(255<<shift);
		for (int i=0; i<size; i++) {
			value = fPixels[i] + 0.5f;
			if (value<0f) value = 0f;
			if (value>255f) value = 255f;
			pixels[i] = (pixels[i]&resetMask) | ((int)value<<shift);
		}
	}

	// NEW METHODS FOR BRIDGE/PATCH SUPPORT
	
	public int getBitDepth() { return 24; }

	public ImageStatistics getStatistics(int mOptions, Calibration cal)
	{
		return new ColorStatistics(this, mOptions, cal);
	}

	public boolean isFloatingType() { return false; }
	public boolean isUnsignedType() { return true; }

	// TODO - do these make sense? or is it never the case that we'd call this method for this kind of processor?
	public double getMinimumAllowedValue() { return 0; }
	public double getMaximumAllowedValue() { return 16777215; }

	public String getTypeName() { return "24-bit RGB"; }

	public double getd(int x, int y) { return getf(x, y); }
	public double getd(int index) { return getf(index); }
}

