 package ij.process;

import java.util.*;
import java.awt.*;
import java.awt.image.*;
// BDZ - BEGIN CHANGES
import ij.measure.Calibration;
import ij.process.ImageStatistics;
// BDZ - END CHANGES
import ij.process.ShortStatistics;

/** ShortProcessors contain a 16-bit unsigned image
	and methods that operate on that image. */
public class ShortProcessor extends ImageProcessor {

	private int min, max, snapshotMin, snapshotMax;
	private short[] pixels;
	private byte[] pixels8;
	private short[] snapshotPixels;
// BDZ - DELETED CODE
	private boolean fixedScale;


	/** Creates a new ShortProcessor using the specified pixel array and ColorModel.
		Set 'cm' to null to use the default grayscale LUT. */
	public ShortProcessor(int width, int height, short[] pixels, ColorModel cm) {
		if (pixels!=null && width*height!=pixels.length)
			throw new IllegalArgumentException(WRONG_LENGTH);
		init(width, height, pixels, cm);
	}

	/** Creates a blank ShortProcessor using the default grayscale LUT that
		displays zero as black. Call invertLut() to display zero as white. */
	public ShortProcessor(int width, int height) {
		this(width, height, new short[width*height], null);
	}
	
	/** Creates a ShortProcessor from a TYPE_USHORT_GRAY BufferedImage. */
	public ShortProcessor(BufferedImage bi) {
		if (bi.getType()!=BufferedImage.TYPE_USHORT_GRAY)
			throw new IllegalArgumentException("Type!=TYPE_USHORT_GRAY");
		WritableRaster raster = bi.getRaster();
		DataBuffer buffer = raster.getDataBuffer();
		short[] data = ((DataBufferUShort) buffer).getData();
		//short[] data2 = new short[data.length];
		//System.arraycopy(data, 0, data2, 0, data.length);
		init(raster.getWidth(), raster.getHeight(), data, null);
	}

	void init(int width, int height, short[] pixels, ColorModel cm) {
		this.width = width;
		this.height = height;
		this.pixels = pixels;
		this.cm = cm;
		resetRoi();
	}

	/**
	* @deprecated
	* 16 bit images are normally unsigned but signed images can be simulated by
	* subtracting 32768 and using a calibration function to restore the original values.
	*/
	public ShortProcessor(int width, int height, short[] pixels, ColorModel cm, boolean unsigned) {
		this(width, height, pixels, cm);
	}

	/** Obsolete. 16 bit images are normally unsigned but signed images can be used by
		subtracting 32768 and using a calibration function to restore the original values. */
	public ShortProcessor(int width, int height,  boolean unsigned) {
		this(width, height);
	}
	
	public void findMinAndMax() {
		if (fixedScale || pixels==null)
			return;
		int size = width*height;
		int value;
		min = 65535;
		max = 0;
		for (int i=0; i<size; i++) {
			value = pixels[i]&0xffff;
			if (value<min)
				min = value;
			if (value>max)
				max = value;
		}
		minMaxSet = true;
	}

	/** Create an 8-bit AWT image by scaling pixels in the range min-max to 0-255. */
	public Image createImage() {
		boolean firstTime = pixels8==null;
		if (firstTime || !lutAnimation)
			create8BitImage();
		if (cm==null)
			makeDefaultColorModel();
		if (ij.IJ.isJava16())
			return createBufferedImage();
		if (source==null) {
			source = new MemoryImageSource(width, height, cm, pixels8, 0, width);
			source.setAnimated(true);
			source.setFullBufferUpdates(true);
			img = Toolkit.getDefaultToolkit().createImage(source);
		} else if (newPixels) {
			source.newPixels(pixels8, cm, 0, width);
			newPixels = false;
		} else
			source.newPixels();
		lutAnimation = false;
	    return img;
	}
	
	// create 8-bit image by linearly scaling from 16-bits to 8-bits
// BDZ - BEGIN CHANGES
	protected byte[] create8BitImage() {
// BDZ - END CHANGES
		int size = width*height;
		if (pixels8==null)
			pixels8 = new byte[size];
		int value;
		int min2=(int)getMin(), max2=(int)getMax(); 
		double scale = 256.0/(max2-min2+1);
		for (int i=0; i<size; i++) {
			value = (pixels[i]&0xffff)-min2;
			if (value<0) value = 0;
			value = (int)(value*scale+0.5);
			if (value>255) value = 255;
			pixels8[i] = (byte)value;
		}
		return pixels8;
	}

	Image createBufferedImage() {
		if (raster==null) {
			SampleModel sm = getIndexSampleModel();
			DataBuffer db = new DataBufferByte(pixels8, width*height, 0);
			raster = Raster.createWritableRaster(sm, db, null);
		}
		if (image==null || cm!=cm2) {
			if (cm==null) cm = getDefaultColorModel();
			image = new BufferedImage(cm, raster, false, null);
			cm2 = cm;
		}
		lutAnimation = false;
		return image;
	}

	/** Returns this image as an 8-bit BufferedImage . */
	public BufferedImage getBufferedImage() {
		return convertToByte(true).getBufferedImage();
	}

	/** Returns a copy of this image as a TYPE_USHORT_GRAY BufferedImage. */
	public BufferedImage get16BitBufferedImage() {
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_GRAY);
        Raster raster = bi.getData();
        DataBufferUShort db = (DataBufferUShort)raster.getDataBuffer();
        System.arraycopy(getPixels(), 0, db.getData(), 0, db.getData().length);
        bi.setData(raster);
        return bi;
	}

	/** Returns a new, blank ShortProcessor with the specified width and height. */
	public ImageProcessor createProcessor(int width, int height) {
		ImageProcessor ip2 = new ShortProcessor(width, height, new short[width*height], getColorModel());
		ip2.setMinAndMax(getMin(), getMax());
		ip2.setInterpolationMethod(interpolationMethod);
		return ip2;
	}

	public void snapshot() {
		snapshotWidth=width;
		snapshotHeight=height;
		snapshotMin=(int)getMin();
		snapshotMax=(int)getMax();
		if (snapshotPixels==null || (snapshotPixels!=null && snapshotPixels.length!=pixels.length))
			snapshotPixels = new short[width * height];
		System.arraycopy(pixels, 0, snapshotPixels, 0, width*height);
	}
	
	public void reset() {
		if (snapshotPixels==null)
			return;
	    min=snapshotMin;
		max=snapshotMax;
		minMaxSet = true;
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
		snapshotPixels = (short[])pixels;
		snapshotWidth=width;
		snapshotHeight=height;
	}

	public Object getSnapshotPixels() {
		return snapshotPixels;
	}

	/* Obsolete. */
	//public boolean isUnsigned() {
	//	return true;
	//}

	/** Returns the smallest displayed pixel value. */
	public double getMin() {
		if (!minMaxSet) findMinAndMax();
		return min;
	}

	/** Returns the largest displayed pixel value. */
	public double getMax() {
		if (!minMaxSet) findMinAndMax();
		return max;
	}

	/**
	Sets the min and max variables that control how real
	pixel values are mapped to 0-255 screen values.
	@see #resetMinAndMax
	@see ij.plugin.frame.ContrastAdjuster 
	*/
	public void setMinAndMax(double minimum, double maximum) {
		if (minimum==0.0 && maximum==0.0)
			{resetMinAndMax(); return;}
		if (minimum<0.0)
			minimum = 0.0;
		if (maximum>65535.0)
			maximum = 65535.0;
		min = (int)minimum;
		max = (int)maximum;
		fixedScale = true;
		minMaxSet = true;
		resetThreshold();
	}
	
	/** Recalculates the min and max values used to scale pixel
		values to 0-255 for display. This ensures that this 
		ShortProcessor is set up to correctly display the image. */
	public void resetMinAndMax() {
		fixedScale = false;
		findMinAndMax();
		resetThreshold();
	}

	public int getPixel(int x, int y) {
		if (x>=0 && x<width && y>=0 && y<height)
			return pixels[y*width+x]&0xffff;
		else
			return 0;
	}

	public final int get(int x, int y) {
		return pixels[y*width+x]&0xffff;
	}

	public final void set(int x, int y, int value) {
		pixels[y*width+x] = (short)value;
	}

	public final int get(int index) {
		return pixels[index]&0xffff;
	}

	public final void set(int index, int value) {
		pixels[index] = (short)value;
	}

	public final float getf(int x, int y) {
		return pixels[y*width+x]&0xffff;
	}

	public final void setf(int x, int y, float value) {
		pixels[y*width + x] = (short)value;
	}

	public final float getf(int index) {
		return pixels[index]&0xffff;
	}

	public final void setf(int index, float value) {
		pixels[index] = (short)value;
	}

	/** Uses the current interpolation method (BILINEAR or BICUBIC)
		to calculate the pixel value at real coordinates (x,y). */
	public double getInterpolatedPixel(double x, double y) {
		if (interpolationMethod==BICUBIC)
			return getBicubicInterpolatedPixel(x, y, this);
		else {
			if (x<0.0) x = 0.0;
			if (x>=width-1.0) x = width-1.001;
			if (y<0.0) y = 0.0;
			if (y>=height-1.0) y = height-1.001;
			return getInterpolatedPixel(x, y, pixels);
		}
	}

	final public int getPixelInterpolated(double x, double y) {
		if (interpolationMethod==BILINEAR) {
			if (x<0.0 || y<0.0 || x>=width-1 || y>=height-1)
				return 0;
			else
				return (int)Math.round(getInterpolatedPixel(x, y, pixels));
		} else if (interpolationMethod==BICUBIC) {
			int value = (int)(getBicubicInterpolatedPixel(x, y, this)+0.5);
			if (value<0) value = 0;
			if (value>65535) value = 65535;
			return value;
		} else
			return getPixel((int)(x+0.5), (int)(y+0.5));
	}

	/** Stores the specified value at (x,y). Does
		nothing if (x,y) is outside the image boundary.
		Values outside the range 0-65535 are clipped.
	*/
	public final void putPixel(int x, int y, int value) {
		if (x>=0 && x<width && y>=0 && y<height) {
			if (value>65535) value = 65535;
			if (value<0) value = 0;
			pixels[y*width + x] = (short)value;
		}
	}

	/** Stores the specified real value at (x,y). Does nothing
		if (x,y) is outside the image boundary. Values outside 
		the range 0-65535 (-32768-32767 for signed images)
		are clipped. Support for signed values requires a calibration
		table, which is set up automatically with PlugInFilters.
	*/
	public void putPixelValue(int x, int y, double value) {
		if (x>=0 && x<width && y>=0 && y<height) {
			if (cTable!=null&&cTable[0]==-32768f) // signed image
				value += 32768.0;
			if (value>65535.0)
				value = 65535.0;
			else if (value<0.0)
				value = 0.0;
			pixels[y*width + x] = (short)(value+0.5);
		}
	}

	/** Draws a pixel in the current foreground color. */
	public void drawPixel(int x, int y) {
		if (x>=clipXMin && x<=clipXMax && y>=clipYMin && y<=clipYMax)
			putPixel(x, y, fgColor);
	}

	/** Returns the value of the pixel at (x,y) as a float. For signed
		images, returns a signed value if a calibration table has
		been set using setCalibrationTable() (this is done automatically 
		in PlugInFilters). */
	public float getPixelValue(int x, int y) {
		if (x>=0 && x<width && y>=0 && y<height) {
			if (cTable==null)
				return pixels[y*width + x]&0xffff;
			else
				return cTable[pixels[y*width + x]&0xffff];
		} else
			return 0f;
	}

	/**	Returns a reference to the short array containing this image's
		pixel data. To avoid sign extension, the pixel values must be
		accessed using a mask (e.g. int i = pixels[j]&0xffff). */
 	public Object getPixels() {
		return (Object)pixels;
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
			short[] pixels2 = new short[width*height];
        	System.arraycopy(pixels, 0, pixels2, 0, width*height);
			return pixels2;
		}
	}

	public void setPixels(Object pixels) {
		this.pixels = (short[])pixels;
		resetPixels(pixels);
		if (pixels==null) snapshotPixels = null;
		if (pixels==null) pixels8 = null;
		raster = null;
	}

	void getRow2(int x, int y, int[] data, int length) {
// BDZ - DELETED CODE
		for (int i=0; i<length; i++)
			data[i] = pixels[y*width+x+i]&0xffff;
	}
	
	void putColumn2(int x, int y, int[] data, int length) {
// BDZ - DELETED CODE
		for (int i=0; i<length; i++)
			pixels[(y+i)*width+x] = (short)data[i];
	}
	
	/** Copies the image contained in 'ip' to (xloc, yloc) using one of
		the transfer modes defined in the Blitter interface. */
	public void copyBits(ImageProcessor ip, int xloc, int yloc, int mode) {
		ip = ip.convertToShort(false);
		new ShortBlitter(this).copyBits(ip, xloc, yloc, mode);
	}

	/** Transforms the pixel data using a 65536 entry lookup table. */
	public void applyTable(int[] lut) {
		if (lut.length!=65536)
			throw new IllegalArgumentException("lut.length!=65536");
		int lineStart, lineEnd, v;
		for (int y=roiY; y<(roiY+roiHeight); y++) {
			lineStart = y * width + roiX;
			lineEnd = lineStart + roiWidth;
			for (int i=lineEnd; --i>=lineStart;) {
				v = lut[pixels[i]&0xffff];
				pixels[i] = (short)v;
			}
		}
		findMinAndMax();
	}

	private void process(int op, double value) {
		int v1, v2;
		double range = getMax()-getMin();
		//boolean resetMinMax = roiWidth==width && roiHeight==height && !(op==FILL);
		int offset = cTable!=null&&cTable[0]==-32768f?32768:0; // signed images have 32768 offset
		int min2 = (int)getMin() - offset;
		int max2 = (int)getMax() - offset;
		int fgColor2 = fgColor - offset;
		
		for (int y=roiY; y<(roiY+roiHeight); y++) {
			int i = y * width + roiX;
			for (int x=roiX; x<(roiX+roiWidth); x++) {
				v1 = (pixels[i]&0xffff) - offset;
				switch(op) {
					case INVERT:
						v2 = max2 - (v1 - min2);
						//v2 = 65535 - (v1+offset);
						break;
					case FILL:
						v2 = fgColor2;
						break;
					case ADD:
						v2 = v1 + (int)value;
						break;
					case MULT:
						v2 = (int)Math.round(v1*value);
						break;
					case AND:
						v2 = v1 & (int)value;
						break;
					case OR:
						v2 = v1 | (int)value;
						break;
					case XOR:
						v2 = v1 ^ (int)value;
						break;
					case GAMMA:
						if (range<=0.0 || v1==min2)
							v2 = v1;
						else					
							v2 = (int)(Math.exp(value*Math.log((v1-min2)/range))*range+min2);
						break;
					case LOG:
						if (v1<=0)
							v2 = 0;
						else 
							v2 = (int)(Math.log(v1)*(max2/Math.log(max2)));
						break;
					case EXP:
						v2 = (int)(Math.exp(v1*(Math.log(max2)/max2)));
						break;
					case SQR:
							v2 = v1*v1;
						break;
					case SQRT:
						v2 = (int)Math.sqrt(v1);
						break;
					case ABS:
						v2 = (int)Math.abs(v1);
						break;
					case MINIMUM:
						if (v1<value)
							v2 = (int)value;
						else
							v2 = v1;
						break;
					case MAXIMUM:
						if (v1>value)
							v2 = (int)value;
						else
							v2 = v1;
						break;
					 default:
					 	v2 = v1;
				}
				v2 += offset;
				if (v2 < 0)
					v2 = 0;
				if (v2 > 65535)
					v2 = 65535;
				pixels[i++] = (short)v2;
			}
		}
    }

	public void invert() {
		resetMinAndMax();
		process(INVERT, 0.0);
	}
	
	public void add(int value) {process(ADD, value);}
	public void add(double value) {process(ADD, value);}
	public void multiply(double value) {process(MULT, value);}
	public void and(int value) {process(AND, value);}
	public void or(int value) {process(OR, value);}
	public void xor(int value) {process(XOR, value);}
	public void gamma(double value) {process(GAMMA, value);}
	public void log() {process(LOG, 0.0);}
	public void exp() {process(EXP, 0.0);}
	public void sqr() {process(SQR, 0.0);}
	public void sqrt() {process(SQRT, 0.0);}
	public void abs() {process(ABS, 0.0);}
	public void min(double value) {process(MINIMUM, value);}
	public void max(double value) {process(MAXIMUM, value);}

	/** Fills the current rectangular ROI. */
	public void fill() {
		process(FILL, 0.0);
	}

	/** Fills pixels that are within roi and part of the mask.
		Does nothing if the mask is not the same as the ROI. */
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
					pixels[i] = (short)fgColor;
				i++;
			}
		}
	}

	/** Does 3x3 convolution. */
	public void convolve3x3(int[] kernel) {
		filter3x3(CONVOLVE, kernel);
	}

	/** Filters using a 3x3 neighborhood. */
	public void filter(int type) {
		filter3x3(type, null);
	}

    /** 3x3 filter operations, code partly based on 3x3 convolution code
     *  contributed by Glynne Casteel. */
    void filter3x3(int type, int[] kernel) {
        int v1, v2, v3;           //input pixel values around the current pixel
        int v4, v5, v6;
        int v7, v8, v9;
        int k1=0, k2=0, k3=0;  //kernel values (used for CONVOLVE only)
        int k4=0, k5=0, k6=0;
        int k7=0, k8=0, k9=0;
        int scale = 0;
        if (type==CONVOLVE) {
            k1=kernel[0]; k2=kernel[1]; k3=kernel[2];
            k4=kernel[3]; k5=kernel[4]; k6=kernel[5];
            k7=kernel[6]; k8=kernel[7]; k9=kernel[8];
            for (int i=0; i<kernel.length; i++)
                scale += kernel[i];
            if (scale==0) scale = 1;
        }
        int inc = roiHeight/25;
        if (inc<1) inc = 1;

        short[] pixels2 = (short[])getPixelsCopy();
        int xEnd = roiX + roiWidth;
        int yEnd = roiY + roiHeight;
        for (int y=roiY; y<yEnd; y++) {
            int p  = roiX + y*width;            //points to current pixel
            int p6 = p - (roiX>0 ? 1 : 0);      //will point to v6, currently lower
            int p3 = p6 - (y>0 ? width : 0);    //will point to v3, currently lower
            int p9 = p6 + (y<height-1 ? width : 0); // ...  to v9, currently lower
            v2 = pixels2[p3]&0xffff;
            v5 = pixels2[p6]&0xffff;
            v8 = pixels2[p9]&0xffff;
            if (roiX>0) { p3++; p6++; p9++; }
            v3 = pixels2[p3]&0xffff;
            v6 = pixels2[p6]&0xffff;
            v9 = pixels2[p9]&0xffff;

            switch (type) {
                case BLUR_MORE:
                for (int x=roiX; x<xEnd; x++,p++) {
                    if (x<width-1) { p3++; p6++; p9++; }
                    v1 = v2; v2 = v3;
                    v3 = pixels2[p3]&0xffff;
                    v4 = v5; v5 = v6;
                    v6 = pixels2[p6]&0xffff;
                    v7 = v8; v8 = v9;
                    v9 = pixels2[p9]&0xffff;
                    pixels[p] = (short)((v1+v2+v3+v4+v5+v6+v7+v8+v9+4)/9);
                }
                break;
                case FIND_EDGES:
                for (int x=roiX; x<xEnd; x++,p++) {
                    if (x<width-1) { p3++; p6++; p9++; }
                    v1 = v2; v2 = v3;
                    v3 = pixels2[p3]&0xffff;
                    v4 = v5; v5 = v6;
                    v6 = pixels2[p6]&0xffff;
                    v7 = v8; v8 = v9;
                    v9 = pixels2[p9]&0xffff;
                    double sum1 = v1 + 2*v2 + v3 - v7 - 2*v8 - v9;
                    double sum2 = v1  + 2*v4 + v7 - v3 - 2*v6 - v9;
                    double result = Math.sqrt(sum1*sum1 + sum2*sum2);
                    if (result>65535.0) result = 65535.0;
                    pixels[p] = (short)result;
                }
                break;
                case CONVOLVE:
                for (int x=roiX; x<xEnd; x++,p++) {
                    if (x<width-1) { p3++; p6++; p9++; }
                    v1 = v2; v2 = v3;
                    v3 = pixels2[p3]&0xffff;
                    v4 = v5; v5 = v6;
                    v6 = pixels2[p6]&0xffff;
                    v7 = v8; v8 = v9;
                    v9 = pixels2[p9]&0xffff;
                    int sum = k1*v1 + k2*v2 + k3*v3
                            + k4*v4 + k5*v5 + k6*v6
                            + k7*v7 + k8*v8 + k9*v9;
                    sum = (sum+scale/2)/scale;   //scale/2 for rounding
                    if(sum>65535) sum = 65535;
                    if(sum<0) sum = 0;
                    pixels[p] = (short)sum;
                }
                break;
            }
            if (y%inc==0)
                showProgress((double)(y-roiY)/roiHeight);
        }
        showProgress(1.0);
    }

	/** Rotates the image or ROI 'angle' degrees clockwise.
		@see ImageProcessor#setInterpolate
	*/
	public void rotate(double angle) {
		short[] pixels2 = (short[])getPixelsCopy();
		ImageProcessor ip2 = null;
		if (interpolationMethod==BICUBIC)
			ip2 = new ShortProcessor(getWidth(), getHeight(), pixels2, null);
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
		double dwidth=width,dheight=height;
		double xlimit = width-1.0, xlimit2 = width-1.001;
		double ylimit = height-1.0, ylimit2 = height-1.001;
		// zero is 32768 for signed images
		int background = cTable!=null && cTable[0]==-32768?32768:0; 
		
		if (interpolationMethod==BICUBIC) {
			for (int y=roiY; y<(roiY + roiHeight); y++) {
				index = y*width + roiX;
				tmp3 = tmp1 - y*sa + centerX;
				tmp4 = tmp2 + y*ca + centerY;
				for (int x=roiX; x<=xMax; x++) {
					xs = x*ca + tmp3;
					ys = x*sa + tmp4;
					int value = (int)(getBicubicInterpolatedPixel(xs, ys, ip2)+0.5);
					if (value<0) value = 0;
					if (value>65535) value = 65535;
					pixels[index++] = (short)value;
				}
				if (y%30==0) showProgress((double)(y-roiY)/roiHeight);
			}
		} else {
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
							pixels[index++] = (short)(getInterpolatedPixel(xs, ys, pixels2)+0.5);
						} else {
							ixs = (int)(xs+0.5);
							iys = (int)(ys+0.5);
							if (ixs>=width) ixs = width - 1;
							if (iys>=height) iys = height -1;
							pixels[index++] = pixels2[width*iys+ixs];
						}
					} else
						pixels[index++] = (short)background;
				}
				if (y%30==0)
				showProgress((double)(y-roiY)/roiHeight);
			}
		}
		showProgress(1.0);
	}

	public void flipVertical() {
		int index1,index2;
		short tmp;
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
	
	/** Scales the image or selection using the specified scale factors.
		@see ImageProcessor#setInterpolationMethod
	*/
	public void scale(double xScale, double yScale) {
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
		short[] pixels2 = (short[])getPixelsCopy();
		ImageProcessor ip2 = null;
		if (interpolationMethod==BICUBIC)
			ip2 = new ShortProcessor(getWidth(), getHeight(), pixels2, null);
		boolean checkCoordinates = (xScale < 1.0) || (yScale < 1.0);
		short min2 = (short)getMin();
		int index1, index2, xsi, ysi;
		double ys, xs;
		if (interpolationMethod==BICUBIC) {
			for (int y=ymin; y<=ymax; y++) {
				ys = (y-yCenter)/yScale + yCenter;
				int index = y*width + xmin;
				for (int x=xmin; x<=xmax; x++) {
					xs = (x-xCenter)/xScale + xCenter;
					int value = (int)(getBicubicInterpolatedPixel(xs, ys, ip2)+0.5);
					if (value<0) value=0; if (value>65535) value=65535;
					pixels[index++] = (short)value;
				}
				if (y%30==0) showProgress((double)(y-ymin)/height);
			}
		} else {
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
						pixels[index1++] = min2;
					else {
						if (interpolationMethod==BILINEAR) {
							if (xs<0.0) xs = 0.0;
							if (xs>=xlimit) xs = xlimit2;
							pixels[index1++] = (short)(getInterpolatedPixel(xs, ys, pixels2)+0.5);
						} else
							pixels[index1++] = pixels2[index2+xsi];
					}
				}
				if (y%30==0) showProgress((double)(y-ymin)/height);
			}
		}
		showProgress(1.0);
	}

	/** Uses bilinear interpolation to find the pixel value at real coordinates (x,y). */
	private final double getInterpolatedPixel(double x, double y, short[] pixels) {
		int xbase = (int)x;
		int ybase = (int)y;
		double xFraction = x - xbase;
		double yFraction = y - ybase;
		int offset = ybase * width + xbase;
		int lowerLeft = pixels[offset]&0xffff;
		int lowerRight = pixels[offset + 1]&0xffff;
		int upperRight = pixels[offset + width + 1]&0xffff;
		int upperLeft = pixels[offset + width]&0xffff;
		double upperAverage = upperLeft + xFraction * (upperRight - upperLeft);
		double lowerAverage = lowerLeft + xFraction * (lowerRight - lowerLeft);
		return lowerAverage + yFraction * (upperAverage - lowerAverage);
	}

	/** Creates a new ShortProcessor containing a scaled copy of this image or selection. */
	public ImageProcessor resize(int dstWidth, int dstHeight) {
		double srcCenterX = roiX + roiWidth/2.0;
		double srcCenterY = roiY + roiHeight/2.0;
		double dstCenterX = dstWidth/2.0;
		double dstCenterY = dstHeight/2.0;
		double xScale = (double)dstWidth/roiWidth;
		double yScale = (double)dstHeight/roiHeight;
		if (interpolationMethod!=NONE) {
			dstCenterX += xScale/2.0;
			dstCenterY += yScale/2.0;
		}
		ImageProcessor ip2 = createProcessor(dstWidth, dstHeight);
		short[] pixels2 = (short[])ip2.getPixels();
		double xs, ys;
		if (interpolationMethod==BICUBIC) {
			for (int y=0; y<=dstHeight-1; y++) {
				ys = (y-dstCenterY)/yScale + srcCenterY;
				int index2 = y*dstWidth;
				for (int x=0; x<=dstWidth-1; x++) {
					xs = (x-dstCenterX)/xScale + srcCenterX;
					int value = (int)(getBicubicInterpolatedPixel(xs, ys, this)+0.5);
					if (value<0) value=0; if (value>65535) value=65535;
					pixels2[index2++] = (short)value;
				}
				if (y%30==0) showProgress((double)y/dstHeight);
			}
		} else {
			double xlimit = width-1.0, xlimit2 = width-1.001;
			double ylimit = height-1.0, ylimit2 = height-1.001;
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
						pixels2[index2++] = (short)(getInterpolatedPixel(xs, ys, pixels)+0.5);
					} else
						pixels2[index2++] = pixels[index1+(int)xs];
				}
				if (y%30==0) showProgress((double)y/dstHeight);
			}
		}
		showProgress(1.0);
		return ip2;
	}

	public ImageProcessor crop() {
		ImageProcessor ip2 = createProcessor(roiWidth, roiHeight);
		short[] pixels2 = (short[])ip2.getPixels();
		for (int ys=roiY; ys<roiY+roiHeight; ys++) {
			int offset1 = (ys-roiY)*roiWidth;
			int offset2 = ys*width+roiX;
			for (int xs=0; xs<roiWidth; xs++)
				pixels2[offset1++] = pixels[offset2++];
		}
        return ip2;
	}
	
	/** Returns a duplicate of this image. */ 
	public synchronized ImageProcessor duplicate() { 
		ImageProcessor ip2 = createProcessor(width, height); 
		short[] pixels2 = (short[])ip2.getPixels(); 
		System.arraycopy(pixels, 0, pixels2, 0, width*height); 
		return ip2; 
	} 

	/** Sets the foreground fill/draw color. */
	public void setColor(Color color) {
		int bestIndex = getBestIndex(color);
		if (bestIndex>0 && getMin()==0.0 && getMax()==0.0) {
			setValue(bestIndex);
			setMinAndMax(0.0,255.0);
		} else if (bestIndex==0 && getMin()>0.0 && (color.getRGB()&0xffffff)==0) {
			if (cTable!=null&&cTable[0]==-32768f) // signed image
				setValue(32768);
			else
				setValue(0.0);
		} else
			fgColor = (int)(getMin() + (getMax()-getMin())*(bestIndex/255.0));
	}
	
	/** Sets the default fill/draw value, where 0<=value<=65535). */
	public void setValue(double value) {
			fgColor = (int)value;
			if (fgColor<0) fgColor = 0;
			if (fgColor>65535) fgColor = 65535;
	}

	/** Does nothing. The rotate() and scale() methods always zero fill. */
	public void setBackgroundValue(double value) {
	}

	/** Always returns 0. */
	public double getBackgroundValue() {
		return 0.0;
	}

	/** Returns 65536 bin histogram of the current ROI, which
		can be non-rectangular. */
	public int[] getHistogram() {
		if (mask!=null)
			return getHistogram(mask);
		int[] histogram = new int[65536];
		for (int y=roiY; y<(roiY+roiHeight); y++) {
			int i = y*width + roiX;
			for (int x=roiX; x<(roiX+roiWidth); x++)
					histogram[pixels[i++]&0xffff]++;
		}
		return histogram;
	}

	int[] getHistogram(ImageProcessor mask) {
		if (mask.getWidth()!=roiWidth||mask.getHeight()!=roiHeight)
			throw new IllegalArgumentException(maskSizeError(mask));
		byte[] mpixels = (byte[])mask.getPixels();
		int[] histogram = new int[65536];
		for (int y=roiY, my=0; y<(roiY+roiHeight); y++, my++) {
			int i = y * width + roiX;
			int mi = my * roiWidth;
			for (int x=roiX; x<(roiX+roiWidth); x++) {
				if (mpixels[mi++]!=0)
					histogram[pixels[i]&0xffff]++;
				i++;
			}
		}
		return histogram;
	}

	public void setThreshold(double minThreshold, double maxThreshold, int lutUpdate) {
		if (minThreshold==NO_THRESHOLD)
			{resetThreshold(); return;}
		if (minThreshold<0.0) minThreshold = 0.0;
		if (maxThreshold>65535.0) maxThreshold = 65535.0;
		int min2=(int)getMin(), max2=(int)getMax();
		if (max2>min2) {
			// scale to 0-255 using same method as create8BitImage()
			double scale = 256.0/(max2-min2+1);
			double minT = minThreshold-min2;
			if (minT<0) minT = 0;
			minT = (int)(minT*scale+0.5);
			if (minT>255) minT = 255;
			//ij.IJ.log("setThreshold: "+minT+" "+Math.round(((minThreshold-min2)/(max2-min2))*255.0));
			double maxT = maxThreshold-min2;
			if (maxT<0) maxT = 0;
			maxT = (int)(maxT*scale+0.5);
			if (maxT>255) maxT = 255;
			super.setThreshold(minT, maxT, lutUpdate); // update LUT
		} else
			super.resetThreshold();
		this.minThreshold = Math.round(minThreshold);
		this.maxThreshold = Math.round(maxThreshold);
	}
	
	/** Performs a convolution operation using the specified kernel. */
	public void convolve(float[] kernel, int kernelWidth, int kernelHeight) {
		ImageProcessor ip2 = convertToFloat();
		ip2.setRoi(getRoi());
		new ij.plugin.filter.Convolver().convolve(ip2, kernel, kernelWidth, kernelHeight);
		ip2 = ip2.convertToShort(false);
		short[] pixels2 = (short[])ip2.getPixels();
		System.arraycopy(pixels2, 0, pixels, 0, pixels.length);
	}

    public void noise(double range) {
		Random rnd=new Random();
		int v, ran;
		boolean inRange;
		for (int y=roiY; y<(roiY+roiHeight); y++) {
			int i = y * width + roiX;
			for (int x=roiX; x<(roiX+roiWidth); x++) {
				inRange = false;
				do {
					ran = (int)Math.round(rnd.nextGaussian()*range);
					v = (pixels[i] & 0xffff) + ran;
					inRange = v>=0 && v<=65535;
					if (inRange) pixels[i] = (short)v;
				} while (!inRange);
				i++;
			}
		}
		resetMinAndMax();
    }
    
	public void threshold(int level) {
		for (int i=0; i<width*height; i++) {
			if ((pixels[i]&0xffff)<=level)
				pixels[i] = 0;
			else
				pixels[i] = (short)255;
		}
		findMinAndMax();
	}

	/** Returns a FloatProcessor with the same image, no scaling or calibration
	*  (pixel values 0 to 65535).
	*  The roi, mask, lut (ColorModel), threshold, min&max are
	*  also set for the FloatProcessor
	*  @param channelNumber   Ignored (needed for compatibility with ColorProcessor.toFloat)
	*  @param fp              Here a FloatProcessor can be supplied, or null. The FloatProcessor
	*                         is overwritten by this method (re-using its pixels array 
	*                         improves performance).
	*  @return A FloatProcessor with the converted image data
	*/
	public FloatProcessor toFloat(int channelNumber, FloatProcessor fp) {
		int size = width*height;
		if (fp == null || fp.getWidth()!=width || fp.getHeight()!=height)
			fp = new FloatProcessor(width, height, new float[size], cm);
		float[] fPixels = (float[])fp.getPixels();
		for (int i=0; i<size; i++)
			fPixels[i] = pixels[i]&0xffff;
		fp.setRoi(getRoi());
		fp.setMask(mask);
		fp.setMinAndMax(getMin(), getMax());
		fp.setThreshold(minThreshold, maxThreshold, ImageProcessor.NO_LUT_UPDATE);
		return fp;
	}
	
	/** Sets the pixels from a FloatProcessor, no scaling.
	*  Also the min&max values are taken from the FloatProcessor.
	*  @param channelNumber   Ignored (needed for compatibility with ColorProcessor.toFloat)
	*  @param fp              The FloatProcessor where the image data are read from.
	*/
	public void setPixels(int channelNumber, FloatProcessor fp) {
		float[] fPixels = (float[])fp.getPixels();
		float value;
		int size = width*height;
		for (int i=0; i<size; i++) {
			value = fPixels[i] + 0.5f;
			if (value<0f) value = 0f;
			if (value>65535f) value = 65535f;
			pixels[i] = (short)value;
		}
		setMinAndMax(fp.getMin(), fp.getMax());
	}
		
	/** Returns the maximum possible pixel value. */
	public double maxValue() {
		return 65535.0;
	}

	/** Not implemented. */
	public void medianFilter() {}
	/** Not implemented. */
	public void erode() {}
	/** Not implemented. */
	public void dilate() {}

// BDZ - BEGIN ADDITIONS
	// NEW METHODS FOR IJ 2.0 SUPPORT
	
	public int getBitDepth() { return 16; }
	public double getBytesPerPixel() { return 2; }
	
	public ImageStatistics getStatistics(int mOptions, Calibration cal)
	{
		return new ShortStatistics(this, mOptions, cal);
	}

	public boolean isFloatingType() { return false; }
	public boolean isUnsignedType() { return true; }  // TODO - may need to do some checking for signed 16 bit code

	// TODO - do these need to be different to support signed 16 bit also?
	public double getMinimumAllowedValue() { return 0; }
	public double getMaximumAllowedValue() { return 65535; }

	public String getTypeName() { return "16-bit unsigned"; }

	public double getd(int x, int y) { return getf(x, y); }
// BDZ - END ADDITIONS
	public double getd(int index) { return getf(index); }
}

