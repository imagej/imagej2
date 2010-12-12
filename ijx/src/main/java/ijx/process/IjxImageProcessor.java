package ijx.process;

import ijx.gui.IjxProgressBar;
import ijx.process.AutoThresholder.Method;
import ijx.roi.Roi;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;

/**
 *
 * @author GBH
 */
public interface IjxImageProcessor {
    /**
     * Interpolation methods
     */
    int BICUBIC = 2;
    /**
     * Interpolation methods
     */
    int BILINEAR = 1;
    /**
     * Value of pixels included in masks.
     */
    int BLACK = -16777216;
    int BLACK_AND_WHITE_LUT = 1;
    int BLUR_MORE = 0;
    /**
     * Center justify text.
     */
    int CENTER_JUSTIFY = 1;
    int CONVOLVE = 5;
    int FIND_EDGES = 1;
    /**
     * Isodata thresholding method
     */
    int ISODATA = 0;
    /**
     * Modified isodata method used in Image/Adjust/Threshold tool
     */
    int ISODATA2 = 1;
    /**
     * Left justify text.
     */
    int LEFT_JUSTIFY = 0;
    int MAX = 4;
    int MEDIAN_FILTER = 2;
    int MIN = 3;
    /**
     * Interpolation methods
     */
    int NEAREST_NEIGHBOR = 0;
    /**
     * Interpolation methods
     */
    int NONE = 0;
    int NO_LUT_UPDATE = 2;
    /**
     * Value returned by getMinThreshold() when thresholding is not enabled.
     */
    double NO_THRESHOLD = -808080.0;
    int OVER_UNDER_LUT = 3;
    int RED_LUT = 0;
    /**
     * Right justify text.
     */
    int RIGHT_JUSTIFY = 2;

    /**
     * If this is a 32-bit or signed 16-bit image, performs an
     * absolute value transform, otherwise does nothing.
     */
    void abs();

    /**
     * Adds 'value' to each pixel in the image or ROI.
     */
    void add(int value);

    /**
     * Adds 'value' to each pixel in the image or ROI.
     */
    void add(double value);

    /**
     * Binary AND of each pixel in the image or ROI with 'value'.
     */
    void and(int value);

    /**
     * Transforms the image or ROI using a lookup table. The
     * length of the table must be 256 for byte images and
     * 65536 for short images. RGB and float images are not
     * supported.
     */
    void applyTable(int[] lut);

    /**
     * Converts the image to binary using an automatically determined threshold.
     * For byte and short images, converts to binary using an automatically determined
     * threshold. For RGB images, converts each channel to binary. For
     * float images, does nothing.
     */
    void autoThreshold();

    /**
     * Returns an 8-bit version of this image as a ByteProcessor.
     */
    IjxImageProcessor convertToByte(boolean doScaling);

    /**
     * Returns a 32-bit float version of this image as a FloatProcessor.
     * For byte and short images, converts using a calibration function
     * if a calibration table has been set using setCalibrationTable().
     */
    IjxImageProcessor convertToFloat();

    /**
     * Returns an RGB version of this image as a ColorProcessor.
     */
    IjxImageProcessor convertToRGB();

    /**
     * Returns a 16-bit version of this image as a ShortProcessor.
     */
    IjxImageProcessor convertToShort(boolean doScaling);

    /**
     * Performs a convolution operation using the specified kernel.
     * KernelWidth and kernelHeight must be odd.
     */
    void convolve(float[] kernel, int kernelWidth, int kernelHeight);

    /**
     * Convolves the image or ROI with the specified
     * 3x3 integer convolution kernel.
     */
    void convolve3x3(int[] kernel);

    /**
     * Copies the image contained in 'ip' to (xloc, yloc) using one of
     * the transfer modes defined in the Blitter interface.
     */
    void copyBits(IjxImageProcessor ip, int xloc, int yloc, int mode);

    /**
     * Returns a copy of this image is the form of an AWT Image.
     */
    Image createImage();

    /**
     * Returns a new, blank processor with the specified width and height.
     */
    IjxImageProcessor createProcessor(int width, int height);

    /**
     * Creates a new processor containing an image
     * that corresponds to the current ROI.
     */
    IjxImageProcessor crop();

    /**
     * Dilates the image or ROI using a 3x3 minimum filter. Requires 8-bit or RGB image.
     */
    void dilate();

    /**
     * Draws an Roi.
     */
    void draw(Roi roi);

    /**
     * Draws a dot using the current line width and fill/draw value.
     */
    void drawDot(int xcenter, int ycenter);

    /**
     * @deprecated
     */
    void drawDot2(int x, int y);

    /**
     * Draws a line from (x1,y1) to (x2,y2).
     */
    void drawLine(int x1, int y1, int x2, int y2);

    /**
     * Draws an elliptical shape.
     */
    void drawOval(int x, int y, int width, int height);

    /**
     * Sets the pixel at (x,y) to the current fill/draw value.
     */
    void drawPixel(int x, int y);

    /**
     * Draws a polygon.
     */
    void drawPolygon(Polygon p);

    /**
     * Draws a rectangle.
     */
    void drawRect(int x, int y, int width, int height);

    /**
     * Draws a string at the current location using the current fill/draw value.
     * Draws multiple lines if the string contains newline characters.
     */
    void drawString(String s);

    /**
     * Draws a string at the specified location using the current fill/draw value.
     */
    void drawString(String s, int x, int y);

    /**
     * Returns a duplicate of this image.
     */
    IjxImageProcessor duplicate();

    /**
     * Erodes the image or ROI using a 3x3 maximum filter. Requires 8-bit or RGB image.
     */
    void erode();

    /**
     * Performs a exponential transform on the image or ROI.
     */
    void exp();

    /**
     * Fills the image or ROI bounding rectangle with the current fill/draw value. Use
     * fill(Roi) or fill(ip.getMask()) to fill non-rectangular selections.
     * @see #setColor(Color)
     * @see #setValue(double)
     * @see #fill(Roi)
     */
    void fill();

    /**
     * Fills pixels that are within the ROI bounding rectangle and part of
     * the mask (i.e. pixels that have a value=BLACK in the mask array).
     * Use ip.getMask() to acquire the mask.
     * Throws and IllegalArgumentException if the mask is null or
     * the size of the mask is not the same as the size of the ROI.
     * @see #setColor(Color)
     * @see #setValue(double)
     * @see #getMask
     * @see #fill(Roi)
     */
    void fill(IjxImageProcessor mask);

    /**
     * Fills the ROI with the current fill/draw value.
     * @see #setColor(Color)
     * @see #setValue(double)
     * @see #fill(Roi)
     */
    void fill(Roi roi);

    /**
     * Fills outside an Roi.
     */
    void fillOutside(Roi roi);

    /**
     * Fills an elliptical shape.
     */
    void fillOval(int x, int y, int width, int height);

    /**
     * Fills a polygon.
     */
    void fillPolygon(Polygon p);

    /**
     * A 3x3 filter operation, where the argument (BLUR_MORE,  FIND_EDGES,
     * MEDIAN_FILTER, MIN or MAX) determines the filter type.
     */
    void filter(int type);

    /**
     * Finds edges in the image or ROI using a Sobel operator.
     */
    void findEdges();

    /**
     * Flips the image or ROI horizontally.
     */
    void flipHorizontal();

    /**
     * Flips the image or ROI vertically.
     */
    void flipVertical();

    /**
     * Performs gamma correction of the image or ROI.
     */
    void gamma(double value);

    /**
     * This is a faster version of getPixel() that does not do bounds checking.
     */
    int get(int x, int y);

    int get(int index);

    /**
     * Returns a pixel value (threshold) that can be used to divide the image into objects
     * and background. It does this by taking a test threshold and computing the average
     * of the pixels at or below the threshold and pixels above. It then computes the average
     * of those two, increments the threshold, and repeats the process. Incrementing stops
     * when the threshold is larger than the composite average. That is, threshold = (average
     * background + average objects)/2. This description was posted to the ImageJ mailing
     * list by Jordan Bevic.
     */
    int getAutoThreshold();

    /**
     * This is a version of getAutoThreshold() that uses a histogram passed as an argument.
     */
    int getAutoThreshold(int[] histogram);

    /**
     * Returns the background fill value.
     */
    double getBackgroundValue();

    /**
     * Returns the LUT index that's the best match for this color.
     */
    int getBestIndex(Color c);

    /**
     * This method is from Chapter 16 of "Digital Image Processing:
     * An Algorithmic Introduction Using Java" by Burger and Burge
     * (http://www.imagingbook.com/).
     */
    double getBicubicInterpolatedPixel(double x0, double y0, IjxImageProcessor ip2);

    /**
     * Returns this image as a BufferedImage.
     */
    BufferedImage getBufferedImage();

    /**
     * Returns the calibration table or null.
     */
    float[] getCalibrationTable();

    /**
     * Returns this processor's color model. For non-RGB processors,
     * this is the base lookup table (LUT), not the one that may have
     * been modified by setMinAndMax() or setThreshold().
     */
    ColorModel getColorModel();

    /**
     * Returns the pixel values down the column starting at (x,y).
     */
    void getColumn(int x, int y, int[] data, int length);

    /**
     * Returns the current color model, which may have
     * been modified by setMinAndMax() or setThreshold().
     */
    ColorModel getCurrentColorModel();

    /**
     * Returns the default grayscale IndexColorModel.
     */
    IndexColorModel getDefaultColorModel();

    /**
     * Returns a copy of the pixel data as a 2D float
     * array with dimensions [x=0..width-1][y=0..height-1].
     */
    float[][] getFloatArray();

    /**
     * Returns the current font.
     */
    Font getFont();

    /**
     * Returns the current FontMetrics.
     */
    FontMetrics getFontMetrics();

    /**
     * Returns the height of this image in pixels.
     */
    int getHeight();

    /**
     * Returns the histogram of the image or ROI. Returns
     * a luminosity histogram for RGB images and null
     * for float images.
     */
    int[] getHistogram();

    /**
     * Returns the maximum histogram value used for histograms of float images.
     */
    double getHistogramMax();

    /**
     * Returns the minimum histogram value used for histograms of float images.
     */
    double getHistogramMin();

    /**
     * Returns the number of float image histogram bins. The bin
     * count is fixed at 256 for the other three data types.
     */
    int getHistogramSize();

    /**
     * Returns a copy of the pixel data as a 2D int array with
     * dimensions [x=0..width-1][y=0..height-1]. With RGB
     * images, the returned values are in packed ARGB format.
     * With float images, the returned values must be converted
     * to float using Float.intBitsToFloat().
     */
    int[][] getIntArray();

    /**
     * Returns the value of the interpolate field.
     */
    boolean getInterpolate();

    /**
     * Uses the current interpolation method (bilinear or bicubic)
     * to find the pixel value at real coordinates (x,y).
     */
    double getInterpolatedPixel(double x, double y);

    /**
     * Uses bilinear interpolation to find the pixel value at real coordinates (x,y).
     * Returns zero if the (x, y) is not inside the image.
     */
    double getInterpolatedValue(double x, double y);

    /**
     * Returns the current interpolation method (NONE, BILINEAR or BICUBIC).
     */
    int getInterpolationMethod();

    /**
     * Returns an array containing the pixel values along the
     * line starting at (x1,y1) and ending at (x2,y2). For byte
     * and short images, returns calibrated values if a calibration
     * table has been set using setCalibrationTable().
     * @see IjxImageProcessor#setInterpolate
     */
    double[] getLine(double x1, double y1, double x2, double y2);

    /**
     * Returns the current line width.
     */
    int getLineWidth();

    /**
     * Returns the LUT update mode, which can be RED_LUT, BLACK_AND_WHITE_LUT,
     * OVER_UNDER_LUT or NO_LUT_UPDATE.
     */
    int getLutUpdateMode();

    /**
     * For images with irregular ROIs, returns a mask, otherwise,
     * returns null. Pixels outside the mask have a value of zero.
     */
    IjxImageProcessor getMask();

    /**
     * Returns the mask byte array, or null if there is no mask.
     */
    byte[] getMaskArray();

    /**
     * Returns the largest displayed pixel value.
     */
    double getMax();

    /**
     * Returns the upper threshold level.
     */
    double getMaxThreshold();

    /**
     * Returns the smallest displayed pixel value.
     */
    double getMin();

    /**
     * Returns the lower threshold level. Returns NO_THRESHOLD
     * if thresholding is not enabled.
     */
    double getMinThreshold();

    /**
     * Returns the number of color channels in the image. The color channels can be
     * accessed by toFloat(channelNumber, fp) and written by setPixels(channelNumber, fp).
     * @return 1 for grayscale images, 3 for RGB images
     */
    int getNChannels();

    /**
     * Experimental
     */
    void getNeighborhood(int x, int y, double[][] arr);

    /**
     * Returns the value of the pixel at (x,y). For RGB images, the
     * argb values are packed in an int. For float images, the
     * the value must be converted using Float.intBitsToFloat().
     * Returns zero if either the x or y coodinate is out of range.
     */
    int getPixel(int x, int y);

    /**
     * Returns the samples for the pixel at (x,y) in an int array.
     * RGB pixels have three samples, all others have one.
     * Returns zeros if the the coordinates are not in bounds.
     * iArray is an optional preallocated array.
     */
    int[] getPixel(int x, int y, int[] iArray);

    int getPixelCount();

    /**
     * Uses the current interpolation method to find the pixel value at real coordinates (x,y).
     * For RGB images, the argb values are packed in an int. For float images,
     * the value must be converted using Float.intBitsToFloat().  Returns zero
     * if the (x, y) is not inside the image.
     */
    int getPixelInterpolated(double x, double y);

    /**
     * Returns the value of the pixel at (x,y). For byte and short
     * images, returns a calibrated value if a calibration table
     * has been  set using setCalibraionTable(). For RGB images,
     * returns the luminance value.
     */
    float getPixelValue(int x, int y);

    /**
     * Returns a reference to this image's pixel array. The
     * array type (byte[], short[], float[] or int[]) varies
     * depending on the image type.
     */
    Object getPixels();

    /**
     * Returns a copy of the pixel data. Or returns a reference to the
     * snapshot buffer if it is not null and 'snapshotCopyMode' is true.
     * @see IjxImageProcessor#snapshot
     * @see IjxImageProcessor#setSnapshotCopyMode
     */
    Object getPixelsCopy();

    /**
     * Returns a Rectangle that represents the current
     * region of interest.
     */
    Rectangle getRoi();

    /**
     * Returns the pixel values along the horizontal line starting at (x,y).
     */
    void getRow(int x, int y, int[] data, int length);

    int getSliceNumber();

    /**
     * Returns a reference to the snapshot (undo) buffer, or null.
     */
    Object getSnapshotPixels();

    ImageStatistics getStatistics();

    /**
     * Returns the width in pixels of the specified string.
     */
    int getStringWidth(String s);

    /**
     * Returns the width of this image in pixels.
     */
    int getWidth();

    float getf(int x, int y);

    float getf(int index);

    /**
     * Inserts the image contained in 'ip' at (xloc, yloc).
     */
    void insert(IjxImageProcessor ip, int xloc, int yloc);

    /**
     * Inverts the image or ROI.
     */
    void invert();

    /**
     * Inverts the values in this image's LUT (indexed color model).
     * Does nothing if this is a ColorProcessor.
     */
    void invertLut();

    /**
     * Returns 'true' if this is a binary image (8-bit-image with only 0 and 255).
     */
    boolean isBinary();

    /**
     * Returns true if this image uses a color LUT.
     */
    boolean isColorLut();

    /**
     * Returns true if the image is using the default grayscale LUT.
     */
    boolean isDefaultLut();

    /**
     * Returns true if this image uses an inverting LUT
     * that displays zero as white and 255 as black.
     */
    boolean isInvertedLut();

    /**
     * @deprecated
     */
    boolean isKillable();

    /**
     * Returns true if this image uses a pseudocolor or grayscale LUT,
     * in other words, is this an image that can be filtered.
     */
    boolean isPseudoColorLut();

    /**
     * Draws a line from the current drawing location to (x2,y2).
     */
    void lineTo(int x2, int y2);

    /**
     * Performs a log transform on the image or ROI.
     */
    void log();

    /**
     * Pixels greater than 'value' are set to 'value'.
     */
    void max(double value);

    /**
     * Returns the maximum possible pixel value.
     */
    double maxValue();

    /**
     * A 3x3 median filter. Requires 8-bit or RGB image.
     */
    void medianFilter();

    /**
     * Pixels less than 'value' are set to 'value'.
     */
    void min(double value);

    /**
     * Returns the minimum possible pixel value.
     */
    double minValue();

    /**
     * Sets the current drawing location.
     * @see IjxImageProcessor#lineTo
     * @see IjxImageProcessor#drawString
     */
    void moveTo(int x, int y);

    /**
     * Multiplies each pixel in the image or ROI by 'value'.
     */
    void multiply(double value);

    /**
     * Adds random noise to the image or ROI.
     * @param range	the range of random numbers
     */
    void noise(double range);

    /**
     * Binary OR of each pixel in the image or ROI with 'value'.
     */
    void or(int value);

    /**
     * Inserts the pixels contained in 'data' into a
     * column starting at (x,y).
     */
    void putColumn(int x, int y, int[] data, int length);

    /**
     * Sets a pixel in the image using an int array of samples.
     * RGB pixels have three samples, all others have one.
     */
    void putPixel(int x, int y, int[] iArray);

    /**
     * Stores the specified value at (x,y). Does
     * nothing if (x,y) is outside the image boundary.
     * For 8-bit and 16-bit images, out of range values
     * are clipped. For RGB images, the
     * argb values are packed in 'value'. For float images,
     * 'value' is expected to be a float converted to an int
     * using Float.floatToIntBits().
     */
    void putPixel(int x, int y, int value);

    /**
     * Stores the specified value at (x,y).
     */
    void putPixelValue(int x, int y, double value);

    /**
     * Inserts the pixels contained in 'data' into a
     * horizontal line starting at (x,y).
     */
    void putRow(int x, int y, int[] data, int length);

    /**
     * Restores the pixel data from the snapshot (undo) buffer.
     */
    void reset();

    /**
     * Restores pixels from the snapshot buffer that are
     * within the rectangular roi but not part of the mask.
     */
    void reset(IjxImageProcessor mask);

    /**
     * Resets the threshold if minThreshold=maxThreshold and lutUpdateMode=NO_LUT_UPDATE.
     * This removes the invisible threshold set by the MakeBinary and Convert to Mask commands.
     * @see IjxImageProcessor#setBinaryThreshold
     */
    void resetBinaryThreshold();

    /**
     * For short and float images, recalculates the min and max
     * image values needed to correctly display the image. For
     * ByteProcessors, resets the LUT.
     */
    void resetMinAndMax();

    /**
     * Sets the ROI (Region of Interest) and clipping rectangle to the entire image.
     */
    void resetRoi();

    /**
     * Disables thresholding.
     */
    void resetThreshold();

    /**
     * Creates a new IjxImageProcessor containing a scaled copy of this image or ROI.
     * @see ij.process.IjxImageProcessor#setInterpolate
     */
    IjxImageProcessor resize(int dstWidth, int dstHeight);

    /**
     * Creates a new IjxImageProcessor containing a scaled copy
     * of this image or ROI, with the aspect ratio maintained.
     */
    IjxImageProcessor resize(int dstWidth);

    /**
     * Creates a new IjxImageProcessor containing a scaled copy of this image or ROI.
     * @param dstWidth   Image width of the resulting IjxImageProcessor
     * @param dstHeight  Image height of the resulting IjxImageProcessor
     * @param useAverging  True means that the averaging occurs to avoid
     * aliasing artifacts; the kernel shape for averaging is determined by
     * the interpolationMethod. False if subsampling without any averaging
     * should be used on downsizing.  Has no effect on upsizing.
     * @IjxImageProcessor#setInterpolationMethod for setting the interpolation method
     * @author Michael Schmid
     */
    IjxImageProcessor resize(int dstWidth, int dstHeight, boolean useAverging);

    /**
     * Rotates the image or selection 'angle' degrees clockwise.
     * @see IjxImageProcessor#setInterpolate
     */
    void rotate(double angle);

    /**
     * Rotates the entire image 90 degrees counter-clockwise. Returns
     * a new IjxImageProcessor that represents the rotated image.
     */
    IjxImageProcessor rotateLeft();

    /**
     * Rotates the entire image 90 degrees clockwise. Returns
     * a new IjxImageProcessor that represents the rotated image.
     */
    IjxImageProcessor rotateRight();

    /**
     * Scales the image by the specified factors. Does not
     * change the image size.
     * @see IjxImageProcessor#setInterpolate
     * @see IjxImageProcessor#resize
     */
    void scale(double xScale, double yScale);

    /**
     * This is a faster version of putPixel() that does not clip
     * out of range values and does not do bounds checking.
     */
    void set(int x, int y, int value);

    void set(int index, int value);

    /**
     * Specifies whether or not text is drawn using antialiasing. Antialiased
     * test requires Java 2 and an 8 bit or RGB image. Antialiasing does not
     * work with 8-bit images that are not using 0-255 display range.
     */
    void setAntialiasedText(boolean antialiasedText);

    void setAutoThreshold(String mString);

    void setAutoThreshold(String mString, boolean darkBackground, int lutUpdate);

    void setAutoThreshold(Method method, boolean darkBackground);

    void setAutoThreshold(Method method, boolean darkBackground, int lutUpdate);

    /**
     * Automatically sets the lower and upper threshold levels, where 'method'
     * must be ISODATA or ISODATA2 and 'lutUpdate' must be RED_LUT,
     * BLACK_AND_WHITE_LUT, OVER_UNDER_LUT or NO_LUT_UPDATE.
     */
    void setAutoThreshold(int method, int lutUpdate);

    /**
     * Sets the background fill value used by the rotate(), scale() and translate() methods.
     */
    void setBackgroundValue(double value);

    void setBinaryThreshold();

    /**
     * Set a lookup table used by getPixelValue(), getLine() and
     * convertToFloat() to calibrate pixel values. The length of
     * the table must be 256 for byte images and 65536 for short
     * images. RGB and float processors do not do calibration.
     * @see ij.measure.Calibration#setCTable
     */
    void setCalibrationTable(float[] cTable);

    /**
     * Updates the clipping rectangle used by lineTo(), drawLine(), drawDot() and drawPixel().
     * The clipping rectangle is reset by passing a null argument or by calling resetRoi().
     */
    void setClipRect(Rectangle clipRect);

    /**
     * Sets the default fill/draw value to the pixel
     * value closest to the specified color.
     */
    void setColor(Color color);

    /**
     * Sets the default fill/draw value. Use setValue() with float images.
     */
    void setColor(int value);

    /**
     * Sets the color model. Must be an IndexColorModel (aka LUT)
     * for all processors except the ColorProcessor.
     */
    void setColorModel(ColorModel cm);

    /**
     * Replaces the pixel data with contents of the specified 2D float array.
     */
    void setFloatArray(float[][] a);

    /**
     * Sets the font used by drawString().
     */
    void setFont(Font font);

    /**
     * Set the range used for histograms of float images. The image range is
     * used if both <code>histMin</code> and <code>histMax</code> are zero.
     */
    void setHistogramRange(double histMin, double histMax);

    /**
     * Set the number of bins to be used for histograms of float images.
     */
    void setHistogramSize(int size);

    /**
     * Replaces the pixel data with contents of the specified 2D int array.
     */
    void setIntArray(int[][] a);

    /**
     * This method has been replaced by setInterpolationMethod().
     */
    void setInterpolate(boolean interpolate);

    /**
     * Use this method to set the interpolation method (NONE,
     * BILINEAR or BICUBIC) used by scale(), resize() and rotate().
     */
    void setInterpolationMethod(int method);

    /**
     * Sets the justification used by drawString(), where <code>justification</code>
     * is CENTER_JUSTIFY, RIGHT_JUSTIFY or LEFT_JUSTIFY. The default is LEFT_JUSTIFY.
     */
    void setJustification(int justification);

    /**
     * Sets the line width used by lineTo() and drawDot().
     */
    void setLineWidth(int width);

    /**
     * For 16 and 32 bit processors, set 'lutAnimation' true
     * to have createImage() use the cached 8-bit version
     * of the image.
     */
    void setLutAnimation(boolean lutAnimation);

    /**
     * Defines a byte mask that limits processing to an
     * irregular ROI. Background pixels in the mask have
     * a value of zero.
     */
    void setMask(IjxImageProcessor mask);

    /**
     * This image will be displayed by mapping pixel values in the
     * range min-max to screen values in the range 0-255. For
     * byte images, this mapping is done by updating the LUT. For
     * short and float images, it's done by generating 8-bit AWT
     * images. For RGB images, it's done by changing the pixel values.
     */
    void setMinAndMax(double min, double max);

    /**
     * Sets a new pixel array for the image. The length of the array must be equal to width*height.
     * Use setSnapshotPixels(null) to clear the snapshot buffer.
     */
    void setPixels(Object pixels);

    /**
     * Sets the pixels (of one color channel for RGB images) from a FloatProcessor.
     * @param channelNumber   Determines the color channel, 0=red, 1=green, 2=blue.Ignored for
     * grayscale images.
     * @param fp              The FloatProcessor where the image data are read from.
     */
    void setPixels(int channelNumber, FloatProcessor fp);

    /**
     * Assigns a progress bar to this processor. Set 'pb' to
     * null to disable the progress bar.
     */
    void setProgressBar(IjxProgressBar pb);

    /**
     * Defines a rectangular region of interest and sets the mask
     * to null if this ROI is not the same size as the previous one.
     * @see IjxImageProcessor#resetRoi
     */
    void setRoi(Rectangle roi);

    /**
     * Defines a rectangular region of interest and sets the mask to
     * null if this ROI is not the same size as the previous one.
     * @see IjxImageProcessor#resetRoi
     */
    void setRoi(int x, int y, int rwidth, int rheight);

    /**
     * Defines a non-rectangular region of interest that will consist of a
     * rectangular ROI and a mask. After processing, call <code>reset(mask)</code>
     * to restore non-masked pixels. Here is an example:
     * <pre>
     * ip.setRoi(new OvalRoi(50, 50, 100, 50));
     * ip.fill();
     * ip.reset(ip.getMask());
     * </pre>
     * The example assumes <code>snapshot()</code> has been called, which is the case
     * for code executed in the <code>run()</code> method of plugins that implement the
     * <code>PlugInFilter</code> interface.
     * @see ij.IjxImagePlus#getRoi
     */
    void setRoi(Roi roi);

    /**
     * Defines a polygonal region of interest that will consist of a
     * rectangular ROI and a mask. After processing, call <code>reset(mask)</code>
     * to restore non-masked pixels. Here is an example:
     * <pre>
     * Polygon p = new Polygon();
     * p.addPoint(50, 0); p.addPoint(100, 100); p.addPoint(0, 100);
     * ip.setRoi(triangle);
     * ip.invert();
     * ip.reset(ip.getMask());
     * </pre>
     * The example assumes <code>snapshot()</code> has been called, which is the case
     * for code executed in the <code>run()</code> method of plugins that implement the
     * <code>PlugInFilter</code> interface.
     * @see ij.gui.Roi#getPolygon
     * @see IjxImageProcessor#drawPolygon
     * @see IjxImageProcessor#fillPolygon
     */
    void setRoi(Polygon roi);

    /**
     * PlugInFilterRunner uses this method to set the slice number.
     */
    void setSliceNumber(int slice);

    /**
     * The getPixelsCopy() method returns a reference to the
     * snapshot buffer if it is not null and 'snapshotCopyMode' is true.
     * @see IjxImageProcessor#getPixelsCopy
     * @see IjxImageProcessor#snapshot
     */
    void setSnapshotCopyMode(boolean b);

    /**
     * Sets a new pixel array for the snapshot (undo) buffer.
     */
    void setSnapshotPixels(Object pixels);

    /**
     * Sets the lower and upper threshold levels. The 'lutUpdate' argument
     * can be RED_LUT, BLACK_AND_WHITE_LUT, OVER_UNDER_LUT or NO_LUT_UPDATE.
     * Thresholding of RGB images is not supported.
     */
    void setThreshold(double minThreshold, double maxThreshold, int lutUpdate);

    /**
     * Sets the default fill/draw value.
     */
    void setValue(double value);

    void setf(int x, int y, float value);

    void setf(int index, float value);

    /**
     * Sharpens the image or ROI using a 3x3 convolution kernel.
     */
    void sharpen();

    /**
     * Replaces each pixel with the 3x3 neighborhood mean.
     */
    void smooth();

    /**
     * Makes a copy of this image's pixel data that can be
     * later restored using reset() or reset(mask).
     * @see IjxImageProcessor#reset
     * @see IjxImageProcessor#reset(IjxImageProcessor)
     */
    void snapshot();

    /**
     * Performs a square transform on the image or ROI.
     */
    void sqr();

    /**
     * Performs a square root transform on the image or ROI.
     */
    void sqrt();

    /**
     * Sets pixels less than or equal to level to 0 and all other
     * pixels to 255. Only works with 8-bit and 16-bit images.
     */
    void threshold(int level);

    /**
     * Returns a FloatProcessor with the image or one color channel thereof.
     * The roi and mask are also set for the FloatProcessor.
     * @param channelNumber   Determines the color channel, 0=red, 1=green, 2=blue. Ignored for
     * grayscale images.
     * @param fp     Here a FloatProcessor can be supplied, or null. The FloatProcessor
     * is overwritten when converting data (re-using its pixels array
     * improves performance).
     * @return A FloatProcessor with the converted image data of the color channel selected
     */
    FloatProcessor toFloat(int channelNumber, FloatProcessor fp);

    /**
     * Returns a string containing information about this IjxImageProcessor.
     */
    String toString();

    /**
     * Moves the image or selection vertically or horizontally by a specified
     * number of pixels. Positive x values move the image or selection to the
     * right, negative values move it to the left. Positive y values move the
     * image or selection down, negative values move it up.
     */
    void translate(double xOffset, double yOffset);

    /**
     * @deprecated
     * replaced by translate(x,y)
     */
    void translate(int xOffset, int yOffset, boolean eraseBackground);

    /**
     * CompositeImage calls this method to generate an updated color image.
     */
    void updateComposite(int[] rgbPixels, int channel);

    /**
     * Binary exclusive OR of each pixel in the image or ROI with 'value'.
     */
    void xor(int value);

}
