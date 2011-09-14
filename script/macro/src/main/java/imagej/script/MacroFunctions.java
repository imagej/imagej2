//
// MacroFunctions.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2011, ImageJDev.org.
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

package imagej.script;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.display.ImageDisplayService;
import imagej.display.Display;
import imagej.display.DisplayService;
import imagej.display.TextDisplay;

import java.util.Arrays;
import java.util.Random;

/**
 * This class contains all built-in macro functions as static methods
 * 
 * @author Johannes Schindelin
 * @see IJ_Macro
 */
public class MacroFunctions {

	public interface Variable {

		String getName();

		Object getValue();

		void setValue(Object value);
	}

	// Math functions

	/** Returns the absolute value of n. **/
	public static double abs(final double n) {
		return Math.abs(n);
	}

	/** Returns the inverse cosine (in radians) of n. */
	public static double acos(final double n) {
		return Math.acos(n);
	}

	/** Returns the inverse sine (in radians) of n. */
	public static double asin(final double n) {
		return Math.asin(n);
	}

	/**
	 * Calculates the inverse tangent (arctangent) of n. Returns a value in the
	 * range -PI/2 through PI/2.
	 */
	public static double atan(final double n) {
		return Math.atan(n);
	}

	/**
	 * Calculates the inverse tangent of y/x and returns an angle in the range -PI
	 * to PI, using the signs of the arguments to determine the quadrant. Multiply
	 * the result by 180/PI to convert to degrees.
	 */
	public static double atan2(final double y, final double x) {
		return Math.atan2(y, x);
	}

	/**
	 * Returns the cosine of an angle (in radians).
	 */
	public static double cos(final double angle) {
		return Math.cos(angle);
	}

	/**
	 * Returns the exponential number e (i.e., 2.718...) raised to the power of n.
	 */
	public static double exp(final double n) {
		return Math.exp(n);
	}

	/**
	 * Returns the largest value that is not greater than n and is equal to an
	 * integer. See also: round.
	 */
	public static double floor(final double n) {
		return Math.floor(n);
	}

	/**
	 * Returns the natural logarithm (base e) of n. Note that log10(n) =
	 * log(n)/log(10).
	 */
	public static double log(final double n) {
		return Math.log(n);
	}

	/**
	 * Returns the greater of two values.
	 */
	public static double maxOf(final double n1, final double n2) {
		return Math.max(n1, n2);
	}

	/**
	 * Returns the smaller of two values.
	 */
	public static double minOf(final double n1, final double n2) {
		return Math.min(n1, n2);
	}

	/**
	 * Returns the value of base raised to the power of exponent.
	 */
	public static double pow(final double base, final double exponent) {
		return Math.pow(base, exponent);
	}

	protected static Random random = new Random();

	/**
	 * Returns a random number between 0 and 1.
	 */
	public static double random() {
		return random.nextDouble();
	}

	/**
	 * Sets the seed (a whole number) used by the random() function. key must be
	 * "seed"
	 */
	public static void random(final String key, final double seed) {
		if ("seed".equals(key)) random.setSeed((long) seed);
		else throw new RuntimeException("Invalid first parameter: " + key);
	}

	/**
	 * Returns the closest integer to n. See also: floor.
	 */
	public static double round(final double n) {
		// Math.round() returns a long
		return Math.floor(n + 0.5);
	}

	/**
	 * Returns the sine of an angle (in radians).
	 */
	public static double sin(final double angle) {
		return Math.sin(angle);
	}

	/**
	 * Returns the square root of n. Returns NaN if n is less than zero.
	 */
	public static double sqrt(final double n) {
		return Math.sqrt(n);
	}

	/**
	 * Returns the tangent of an angle (in radians).
	 */
	public static double tan(final double angle) {
		return Math.tan(angle);
	}

	// Get information

	/**
	 * Returns the bit depth of the active image: 8, 16, 24 (RGB) or 32 (float).
	 */
	public static int bitDepth() {
		return IJ2.getCurrentImage().getValidBits();
	}

	/**
	 * Writes the contents of the symbol table, the tokenized macro code and the
	 * variable stack to the "Log" window.
	 */
	public static void dump() {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns the string argument passed to macros called by runMacro(macro,
	 * arg), eval(macro), IJ.runMacro(macro, arg) or IJ.runMacroFile(path, arg).
	 */
	public static String getArgument() {
		throw new RuntimeException("TODO");
	}

	/**
	 * Replace by getSelectionBounds.
	 */
	public static void getBoundingRect(final Variable x, final Variable y,
		final Variable width, final Variable height)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns the cursor location in pixels and the mouse event modifier flags.
	 * The z coordinate is zero for 2D images. For stacks, it is one less than the
	 * slice number. For examples, see the GetCursorLocDemo and the
	 * GetCursorLocDemoTool macros.
	 */
	public static void getCursorLoc(final Variable x, final Variable y,
		final Variable z, final Variable modifiers)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns the current date and time. Note that 'month' and 'dayOfWeek' are
	 * zero-based indexes. For an example, refer to the GetDateAndTime macro. See
	 * also: getTime.
	 */
	public static void getDateAndTime(final Variable year, final Variable month,
		final Variable dayOfWeek, final Variable dayOfMonth, final Variable hour,
		final Variable minute, final Variable second, final Variable msec)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns the dimensions of the current image.
	 */
	public static void getDimensions(final Variable width, final Variable height,
		final Variable channels, final Variable slices, final Variable frames)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Displays a "choose directory" dialog and returns the selected directory, or
	 * returns the path to a specified directory, such as "plugins", "home", etc.
	 * The returned path ends with a file separator, either "\" (Windows) or "/
	 * ". Returns an empty string if the specified directory is not found or aborts the macro if the user cancels the "
	 * choose directory" dialog box. For examples, see the GetDirectoryDemo and
	 * ListFilesRecursively macros. See also: getFileList and the File functions.
	 * getDirectory("Choose a Directory") - Displays a file open dialog, using the
	 * argument as a title, and returns the path to the directory selected by the
	 * user. getDirectory("plugins") - Returns the path to the plugins directory.
	 * getDirectory("macros") - Returns the path to the macros directory.
	 * getDirectory("luts") - Returns the path to the luts directory.
	 * getDirectory("image") - Returns the path to the directory that the active
	 * image was loaded from. getDirectory("imagej") - Returns the path to the
	 * ImageJ directory. getDirectory("startup") - Returns the path to the
	 * directory that ImageJ was launched from. getDirectory("home") - Returns the
	 * path to users home directory. getDirectory("temp") - Returns the path to
	 * the temporary directory (/tmp on Linux and Mac OS X).
	 */
	public static String getDirectory(final Variable string) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns the pixel coordinates of the actual displayed area of the image
	 * canvas. For an example, see the Pixel Sampler Tool. Requires 1.44k.
	 */
	public static void getDisplayedArea(final Variable x, final Variable y,
		final Variable width, final Variable height)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns the height in pixels of the current image.
	 */
	public static int getHeight() {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns the histogram of the current image or selection. Values is an array
	 * that will contain the pixel values for each of the histogram counts (or the
	 * bin starts for 16 and 32 bit images), or set this argument to 0. Counts is
	 * an array that will contain the histogram counts. nBins is the number of
	 * bins that will be used. It must be 256 for 8 bit and RGB image, or an
	 * integer greater than zero for 16 and 32 bit images. With 16-bit images, the
	 * Values argument is ignored if nBins is 65536. With 16-bit and 32-bit
	 * images, the histogram range can be specified using optional histMin and
	 * histMax arguments. See also: getStatistics, HistogramLister,
	 * HistogramPlotter, StackHistogramLister and CustomHistogram.
	 */
	public static void getHistogram(final Variable values, final Variable counts,
		final Variable nBins)
	{
		getHistogram(values, counts, nBins, null, null);
	}

	/**
	 * Returns the histogram of the current image or selection. Values is an array
	 * that will contain the pixel values for each of the histogram counts (or the
	 * bin starts for 16 and 32 bit images), or set this argument to 0. Counts is
	 * an array that will contain the histogram counts. nBins is the number of
	 * bins that will be used. It must be 256 for 8 bit and RGB image, or an
	 * integer greater than zero for 16 and 32 bit images. With 16-bit images, the
	 * Values argument is ignored if nBins is 65536. With 16-bit and 32-bit
	 * images, the histogram range can be specified using optional histMin and
	 * histMax arguments. See also: getStatistics, HistogramLister,
	 * HistogramPlotter, StackHistogramLister and CustomHistogram.
	 */
	public static void getHistogram(final Variable values, final Variable counts,
		final Variable nBins, final Variable histMin, final Variable histMax)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns the unique ID (a negative number) of the active image. Use the
	 * selectImage(id), isOpen(id) and isActive(id) functions to activate an image
	 * or to determine if it is open or active.
	 */
	public static int getImageID() {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns a string containing the text that would be displayed by the
	 * Image>Show Info command. To retrieve the contents of a text window, use
	 * getInfo("window.contents"). For an example, see the ListDicomTags macros.
	 * See also: getMetadata.
	 */
	public static String getImageInfo() {
		throw new RuntimeException("TODO");
	}

	/**
	 * "font.name": Returns the name of the current font. Requires 1.43f.
	 * DICOM_TAG: Returns the value of a DICOM tag in the form "xxxx,xxxx", e.g.
	 * getInfo("0008,0060"). Returns an empty string if the current image is not a
	 * DICOM or if the tag was not found. Requires 1.43k. "image.description":
	 * Returns the TIFF image description tag, or an empty string if this is not a
	 * TIFF image or the image description is not available. "image.directory":
	 * Returns the directory that the current image was loaded from, or an empty
	 * string if the directory is not available. Requires 1.43h. "image.filename":
	 * Returns the name of the file that the current image was loaded from, or an
	 * empty string if the file name is not available. Requires 1.43h.
	 * "image.subtitle": Returns the subtitle of the current image. This is the
	 * line of information displayed above the image and below the title bar.
	 * "micrometer.abbreviation": Returns "µm", the abbreviation for micrometer.
	 * Requires 1.43d. "overlay": Returns information about the current image's
	 * overlay. Requires 1.43r. "log": Returns the contents of the Log window, or
	 * "" if the Log window is not open. Requires 1.44l. "selection.name": Returns
	 * the name of the current selection, or "" if there is no selection or the
	 * selection does not have a name. The argument can also be "roi.name".
	 * "slice.label": Return the label of the current stack slice. This is the
	 * string that appears in parentheses in the subtitle, the line of information
	 * above the image. Returns an empty string if the current image is not a
	 * stack or the current slice does not have a label. "threshold.method":
	 * Returns the current thresholding method ("IsoData", "Otsu", etc). Requires
	 * 1.45n. "threshold.mode": Returns the current thresholding mode ("Red","B&W"
	 * or"Over/Under"). Requires 1.45n. "window.contents": If the front window is
	 * a text window, returns the contents of that window. If the front window is
	 * an image, returns a string containing the text that would be displayed by
	 * Image>Show Info. Note that getImageInfo() is a more reliable way to
	 * retrieve information about an image. Use split(getInfo(),'\n') to break the
	 * string returned by this function into separate lines. Replaces the
	 * getInfo() function. key: Returns the Java property associated with the
	 * specified key (e.g., "java.version", "os.name", "user.home", "user.dir",
	 * etc.). Returns an empty string if there is no value associated with the
	 * key. See also: getList("java.properties").
	 */
	public static String getInfo(final String key) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns the starting coordinates, ending coordinates and width of the
	 * current straight line selection. The coordinates and line width are in
	 * pixels. Sets x1 = -1 if there is no line selection. Refer to the
	 * GetLineDemo macro for an example. See also: makeLine.
	 */
	public static void getLine(final Variable x1, final Variable y1,
		final Variable x2, final Variable y2, final Variable lineWidth)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * "window.titles": Returns a list (array) of non-image window titles. For an
	 * example, see the DisplayWindowTitles macro. "java.properties": Returns a
	 * list (array) of Java property keys. For an example, see the
	 * DisplayJavaProperties macro. See also: getInfo(key). "threshold.methods":
	 * Returns a list of the available automatic thresholding methods (example).
	 */
	public static String[] getList(final String key) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns the location and size, in screen coordinates, of the active image
	 * window. Use getWidth and getHeight to get the width and height, in image
	 * coordinates, of the active image. See also: setLocation,
	 */
	public static void getLocationAndSize(final Variable x, final Variable y,
		final Variable width, final Variable height)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns three arrays containing the red, green and blue intensity values
	 * from the current lookup table. See the LookupTables macros for examples.
	 */
	public static void getLut(final Variable reds, final Variable greens,
		final Variable blues)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * "Info": Returns the metadata (a string) from the "Info" property of the
	 * current image. With DICOM images, this is the information (tags) in the
	 * DICOM header. See also: setMetadata. "Label": Returns the current slice
	 * label. The first line of the this label (up to 60 characters) is display as
	 * part of the image subtitle. With DICOM stacks, returns the metadata from
	 * the DICOM header. See also: setMetadata.
	 */
	public static String getMetadata(final String key) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns the minimum and maximum displayed pixel values (display range). See
	 * the DisplayRangeMacros for examples.
	 */
	public static void getMinAndMax(final Variable min, final Variable max) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns the value of the pixel at (x,y). Note that pixels in RGB images
	 * contain red, green and blue components that need to be extracted using
	 * shifting and masking. See the Color Picker Tool macro for an example that
	 * shows how to do this.
	 */
	public static Object getPixel(final Variable x, final Variable y) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns the unit of length (as a string) and the pixel dimensions. For an
	 * example, see the ShowImageInfo macro. See also: getVoxelSize.
	 */
	public static void getPixelSize(final Variable unit,
		final Variable pixelWidth, final Variable pixelHeight)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Runs Analyze>Plot Profile (without displaying the plot) and returns the
	 * intensity values as an array. For an example, see the GetProfileExample
	 * macro.
	 */
	public static float[] getProfile() {
		throw new RuntimeException("TODO");
	}

	/**
	 * This function is similar to getStatistics except that the values returned
	 * are uncalibrated and the histogram of 16-bit images has a bin width of one
	 * and is returned as a max+1 element array. For examples, refer to the
	 * ShowStatistics macro set. See also: calibrate and List.setMeasurements
	 */
	public static void getRawStatistics(final Variable nPixels,
		final Variable mean, final Variable min, final Variable max,
		final Variable std, final Variable histogram)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns a measurement from the ImageJ results table or NaN if the specified
	 * column is not found. The first argument specifies a column in the table. It
	 * must be a "Results" window column label, such as "Area", "Mean" or "Circ.".
	 * The second argument specifies the row, where 0<=row<nResults. nResults is a
	 * predefined variable that contains the current measurement count. (Actually,
	 * it's a built-in function with the "()" optional.) Omit the second argument
	 * and the row defaults to nResults-1 (the last row in the results table). See
	 * also: nResults, setResult, isNaN, getResultLabel.
	 */
	public static double getResult(final String columnName, final int row) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns the label of the specified row in the results table, or an empty
	 * string if Display Label is not checked in Analalyze>Set Measurements.
	 */
	public static String getResultLabel(final Variable row) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns the smallest rectangle that can completely contain the current
	 * selection. x and y are the pixel coordinates of the upper left corner of
	 * the rectangle. width and height are the width and height of the rectangle
	 * in pixels. If there is no selection, returns (0, 0, ImageWidth,
	 * ImageHeight). See also: selectionType and setSelectionLocation.
	 */
	public static void getSelectionBounds(final Variable x, final Variable y,
		final Variable width, final Variable height)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns two arrays containing the X and Y coordinates, in pixels, of the
	 * points that define the current selection. See the SelectionCoordinates
	 * macro for an example. See also: selectionType, getSelectionBounds.
	 */
	public static void getSelectionCoordinates(final Variable xCoordinates,
		final Variable yCoordinates)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns the number of the currently displayed stack image, an integer
	 * between 1 and nSlices. Returns 1 if the active image is not a stack. See
	 * also: setSlice.
	 */
	public static int getSliceNumber() {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns the area, average pixel value, minimum pixel value, maximum pixel
	 * value, standard deviation of the pixel values and histogram of the active
	 * image or selection. The histogram is returned as a 256 element array. For
	 * 8-bit and RGB images, the histogram bin width is one. For 16-bit and 32-bit
	 * images, the bin width is (max-min)/256. For examples, refer to the
	 * ShowStatistics macro set. Note that trailing arguments can be omitted. For
	 * example, you can use getStatistics(area), getStatistics(area, mean) or
	 * getStatistics(area, mean, min, max). See also: getRawStatistics and
	 * List.setMeasurements
	 */
	public static void getStatistics(final Variable area, final Variable mean,
		final Variable min, final Variable max, final Variable std,
		final Variable histogram)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns the lower and upper threshold levels. Both variables are set to -1
	 * if the active image is not thresholded. See also: setThreshold,
	 * getThreshold, resetThreshold.
	 */
	public static void getThreshold(final Variable lower, final Variable upper) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns the current time in milliseconds. The granularity of the time
	 * varies considerably from one platform to the next. On Windows NT, 2K, XP it
	 * is about 10ms. On other Windows versions it can be as poor as 50ms. On many
	 * Unix platforms, including Mac OS X, it actually is 1ms. See also:
	 * getDateAndTime.
	 */
	public static long getTime() {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns the title of the current image.
	 */
	public static String getTitle() {
		throw new RuntimeException("TODO");
	}

	/**
	 * "color.foreground": Returns the integer value of the current foregound
	 * color (example). "color.background": Returns the integer value of the
	 * current background color. "font.size": Returns the size, in points, of the
	 * current font. Requires 1.43f. "font.height": Returns the height, in pixels,
	 * of the current font. Requires 1.43f.
	 */
	public static int getValue(final String key) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns the voxel size and unit of length ("pixel", "mm", etc.) of the
	 * current image or stack. See also: getPixelSize, setVoxelSize.
	 */
	public static void getVoxelSize(final Variable width, final Variable height,
		final Variable depth, final Variable unit)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns the ImageJ version number as a string (e.g., "1.34s"). See also:
	 * requires.
	 */
	public static String getVersion() {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns the width in pixels of the current image.
	 */
	public static int getWidth() {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns the magnification of the active image, a number in the range
	 * 0.03125 to 32.0 (3.1% to 3200%).
	 */
	public static double getZoom() {
		throw new RuntimeException("TODO");
	}

	/**
	 * "animated": Returns true if the current image is an animated stack.
	 * Requires 1.44g. "applet": Returns true if ImageJ is running as an applet.
	 * "Batch Mode": Returns true if the macro interpreter is running in batch
	 * mode. "binary": Returns true if the current image is binary (8-bit with
	 * only 0 and 255 values). "Caps Lock Set": Returns true if the caps lock key
	 * is set. Always return false on some platforms. "changes": Returns true if
	 * the current image's 'changes' flag is set. "composite": Returns true if the
	 * current image is a a multi-channel stack that uses the CompositeImage
	 * class. "Inverting LUT": Returns true if the current image is using an
	 * inverting lookup table. "hyperstack": Returns true if the current image is
	 * a hyperstack. "locked": Returns true if the current image is locked.
	 * "Virtual Stack": Returns true if the current image is a virtual stack.
	 */
	public static boolean is(final String key) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns true if the image with the specified ID is active.
	 */
	public static boolean isActive(final Variable id) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns true if the specified key is pressed, where key must be "shift",
	 * "alt" or "space". See also: setKeyDown.
	 */
	public static boolean isKeyDown(final Variable key) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns true if the value of the number n is NaN (Not-a-Number). A common
	 * way to create a NaN is to divide zero by zero. Comparison with a NaN always
	 * returns false so "if (n!=n)" is equilvalent to (isNaN(n))". Note that the
	 * numeric constant NaN is predefined in the macro language. The NaNs macro
	 * demonstrates how to remove NaNs from an image.
	 */
	public static boolean isNaN(final Variable n) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns true if the image with the specified ID or title is open.
	 */
	public static boolean isOpen(final Object idOrTitle) {
		throw new RuntimeException("TODO");
	}

	// TODO: ignore "function" keyword
	// TODO: handle nImages, nResults, nSlices, random, reset*, screenWidth,
	// screenHeight, selectionName, toolID, waitForUser without parentheses
	// (probably every function that takes no arguments)

	/**
	 * Returns number of open images. The parentheses "()" are optional.
	 */
	public static int nImages() {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns the current measurement counter value. The parentheses "()" are
	 * optional.
	 */
	public static int nResults() {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns the number of images in the current stack. Returns 1 if the current
	 * image is not a stack. The parentheses "()" are optional. See also:
	 * getSliceNumber,
	 */
	public static int nSlices() {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns the screen height in pixels. See also: getLocationAndSize,
	 * setLocation.
	 */
	public static int screenHeight() {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns the screen width in pixels.
	 */
	public static int screenWidth() {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns the name of the current selection, or an empty string if the
	 * selection does not have a name. Aborts the macro if there is no selection.
	 * See also: setSelectionName and selectionType.
	 */
	public static String selectionName() {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns the selection type, where 0=rectangle, 1=oval, 2=polygon,
	 * 3=freehand, 4=traced, 5=straight line, 6=segmented line, 7=freehand line,
	 * 8=angle, 9=composite and 10=point. Returns -1 if there is no selection. For
	 * an example, see the ShowImageInfo macro.
	 */
	public static int selectionType() {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns the ID of the currently selected tool. See also: setTool,
	 * IJ.getToolName.
	 */
	public static int toolID() {
		throw new RuntimeException("TODO");
	}

	// Changing pixels

	/**
	 * Changes pixels in the image or selection that have a value in the range
	 * v1-v2 to v3. For example, changeValues(0,5,5) changes all pixels less than
	 * 5 to 5, and changeValues(0x0000ff,0x0000ff,0xff0000) changes all blue
	 * pixels in an RGB image to red.
	 */
	public static void changeValues(final Variable v1, final Variable v2,
		final Variable v3)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Draws a line between (x1, y1) and (x2, y2). Use setColor() to specify the
	 * color of the line and setLineWidth() to vary the line width. See also:
	 * Overlay.drawLine.
	 */
	public static void drawLine(final Variable x1, final Variable y1,
		final Variable x2, final Variable y2)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Draws the outline of an oval using the current color and line width. See
	 * also: fillOval, setColor, setLineWidth, autoUpdate and Overlay.drawEllipse.
	 */
	public static void drawOval(final Variable x, final Variable y,
		final Variable width, final Variable height)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Draws the outline of a rectangle using the current color and line width.
	 * See also: fillRect, setColor, setLineWidth, autoUpdate and Overlay.drawRect
	 */
	public static void drawRect(final Variable x, final Variable y,
		final Variable width, final Variable height)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Draws text at the specified location. Call setFont() to specify the font.
	 * Call setJustification() to have the text centered or right justified. Call
	 * getStringWidth() to get the width of the text in pixels. Refer to the
	 * TextDemo macro for examples and to DrawTextWithBackground to see how to
	 * draw text with a background.
	 */
	public static void drawString(final String text, final Variable x,
		final Variable y)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Draws text at the specified location with a filled background (examples).
	 * Requires 1.45j.
	 */
	public static void drawString(final String text, final Variable x,
		final Variable y, final Variable background)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Fills the image or selection with the current drawing color.
	 */
	public static void fill() {
		throw new RuntimeException("TODO");
	}

	/**
	 * Fills an oval bounded by the specified rectangle with the current drawing
	 * color. See also: drawOval, setColor, autoUpdate.
	 */
	public static void fillOval(final Variable x, final Variable y,
		final Variable width, final Variable height)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Fills the specified rectangle with the current drawing color. See also:
	 * drawRect, setColor, autoUpdate.
	 */
	public static void fillRect(final Variable x, final Variable y,
		final Variable width, final Variable height)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Fills, with the foreground color, pixels that are connected to, and the
	 * same color as, the pixel at (x, y). Does 8-connected filling if there is an
	 * optional string argument containing "8", for example floodFill(x, y,
	 * "8-connected"). This function is used to implement the flood fill (paint
	 * bucket) macro tool.
	 */
	public static void floodFill(final Variable x, final Variable y) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Draws a line from current location to (x,y) . See also: Overlay.lineTo.
	 */
	public static void lineTo(final Variable x, final Variable y) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Sets the current drawing location. The origin is always assumed to be the
	 * upper left corner of the image.
	 */
	public static void moveTo(final Variable x, final Variable y) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Stores value at location (x,y) of the current image. The screen is updated
	 * when the macro exits or call updateDisplay() to have it updated
	 * immediately.
	 */
	public static void setPixel(final Variable x, final Variable y,
		final Variable value)
	{
		throw new RuntimeException("TODO");
	}

	// Executing and evaluating

	/**
	 * Calls a public static method in a Java class, passing an arbitrary number
	 * of string arguments, and returning a string. Refer to the CallJavaDemo
	 * macro and the ImpProps plugin for examples. Note that the call() function
	 * does not work when ImageJ is running as an unsigned applet.
	 */
	public static void call(final String classAndMethod, final Variable... args) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Runs an ImageJ menu command in a separate thread and returns immediately.
	 * As an example, doCommand("Start Animation") starts animating the current
	 * stack in a separate thread and the macro continues to execute. Use
	 * run("Start Animation") and the macro hangs until the user stops the
	 * animation.
	 */
	public static void doCommand(final String command) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Evaluates (runs) one or more lines of macro code. An optional second
	 * argument can be used to pass a string to the macro being evaluated. See
	 * also: EvalDemo macro and runMacro function.
	 */
	public static void eval(final Variable macro) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Evaluates the JavaScript code contained in the string javascript, for
	 * example eval("script","IJ.getInstance().setAlwaysOnTop(true);"). Mac users,
	 * and users of Java 1.5, must have a copy of JavaScript.jar in the plugins
	 * folder.
	 * 
	 * @param mode must be "script"
	 */
	public static void eval(final String mode, final Variable javascript) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Executes a native command and returns the output of that command as a
	 * string. Also opens Web pages in the default browser and documents in other
	 * applications (e.g., Excel). Refer to the ExecExamples macro for examples.
	 */
	public static void exec(final String... args) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Terminates execution of the macro
	 */
	public static void exit() {
		throw new RuntimeException("TODO");
	}

	/**
	 * Terminates execution of the macro and displays an error message.
	 */
	public static void exit(final String errorMessage) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Display a message and aborts the macro if the ImageJ version is less than
	 * the one specified. See also: getVersion.
	 * 
	 * @param minimumVersion something like "1.29p"
	 */
	public static void requires(final String minimumVersion) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Executes an ImageJ menu command. Use the Command Recorder
	 * (Plugins>Macros>Record) to generate run() function calls. Use string
	 * concatentation to pass a variable as an argument. With ImageJ 1.43 and
	 * later, variables can be passed without using string concatenation by adding
	 * "&" to the variable name. For examples, see the ArgumentPassingDemo macro.
	 */
	public static void run(final String command) {
		run(command, null);
	}

	/**
	 * Executes an ImageJ menu command. The optional second argument contains
	 * values that are automatically entered into dialog boxes (must be
	 * GenericDialog or OpenDialog). Use the Command Recorder
	 * (Plugins>Macros>Record) to generate run() function calls. Use string
	 * concatentation to pass a variable as an argument. With ImageJ 1.43 and
	 * later, variables can be passed without using string concatenation by adding
	 * "&" to the variable name. For examples, see the ArgumentPassingDemo macro.
	 */
	public static void run(final String command, final String options) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Runs the specified macro file, which is assumed to be in the Image macros
	 * folder. A full file path may also be used. The ".txt" extension is
	 * optional. Returns any string argument returned by the macro. May have an
	 * optional second string argument that is passed to macro. For an example,
	 * see the CalculateMean macro. See also: eval and getArgument.
	 */
	public static void runMacro(final Variable name) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Delays (sleeps) for n milliseconds.
	 */
	public static void wait(final Variable n) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Halts the macro and displays string in a dialog box. The macro proceeds
	 * when the user clicks "OK". Unlike showMessage, the dialog box is not modal,
	 * so the user can, for example, create a selection or adjust the threshold
	 * while the dialog is open. To display a multi-line message, add newline
	 * characters ("\n") to string. This function is based on Michael Schmid's
	 * Wait_For_User plugin. Example: WaitForUserDemo.
	 */
	public static void waitForUser(final Variable string) {
		throw new RuntimeException("TODO");
	}

	/**
	 * This is a two argument version of waitForUser, where title is the dialog
	 * box title and message is the text dispayed in the dialog.
	 */
	public static void waitForUser(final Variable title, final Variable message) {
		throw new RuntimeException("TODO");
	}

	/**
	 * This is a no argument version of waitForUser that displays
	 * "Click OK to continue" in the dialog box.
	 */
	public static void waitForUser() {
		throw new RuntimeException("TODO");
	}

	// String functions (not in the inner class String)

	/**
	 * Returns the Unicode value of the character at the specified index in
	 * string. Index values can range from 0 to lengthOf(string). Use the
	 * fromCharCode() function to convert one or more Unicode characters to a
	 * string.
	 */
	public static char charCodeAt(final Variable string, final Variable index) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Converts the number n into a string using the specified number of decimal
	 * places. Uses scientific notation if 'decimalPlaces is negative. Note that
	 * d2s stands for "double to string".
	 */
	public static String d2s(final Variable n, final Variable decimalPlaces) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns true (1) if string ends with suffix. See also: startsWith, indexOf,
	 * substring, matches.
	 */
	public static boolean endsWith(final Variable string, final Variable suffix) {
		throw new RuntimeException("TODO");
	}

	/**
	 * This function takes one or more Unicode values and returns a string.
	 */
	// TODO: support varargs
	public static String fromCharCode(final char... value1) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns an array containing the names of the files in the specified
	 * directory path. The names of subdirectories have a "/" appended. For an
	 * example, see the ListFilesRecursively macro.
	 */
	public static String[] getFileList(final String directory) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns the width in pixels of the specified string. See also: setFont,
	 * drawString.
	 */
	public static double getStringWidth(final Variable string) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns the index within string of the first occurrence of substring. See
	 * also: lastIndexOf, startsWith, endsWith, substring, toLowerCase, replace,
	 * matches.
	 */
	public static int indexOf(final Variable string, final Variable substring) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns the index within string of the first occurrence of substring, with
	 * the search starting at fromIndex.
	 */
	public static int indexOf(final Variable string, final Variable substring,
		final Variable fromIndex)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns the index within string of the rightmost occurrence of substring.
	 * See also: indexOf, startsWith, endsWith, substring.
	 */
	public static boolean lastIndexOf(final Variable string,
		final Variable substring)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns the length of a string or array.
	 */
	public static boolean lengthOf(final Variable str) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns true if string matches the specified regular expression. See also:
	 * startsWith, endsWith, indexOf, replace.
	 */
	public static boolean matches(final Variable string, final Variable regex) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Converts the string argument to a number and returns it. Returns NaN (Not a
	 * Number) if the string cannot be converted into a number. Use the isNaN()
	 * function to test for NaN. For examples, see ParseFloatIntExamples.
	 */
	public static float parseFloat(final Variable string) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Converts string to an integer and returns it. Returns NaN if the string
	 * cannot be converted into a integer.
	 */
	public static int parseInt(final Variable string) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Converts string to an integer and returns it. The optional second argument
	 * (radix) specifies the base of the number contained in the string. The radix
	 * must be an integer between 2 and 36. For radixes above 10, the letters of
	 * the alphabet indicate numerals greater than 9. Set radix to 16 to parse
	 * hexadecimal numbers. Returns NaN if the string cannot be converted into a
	 * integer. For examples, see ParseFloatIntExamples.
	 */
	public static int parseInt(final Variable string, final Variable radix) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns the new string that results from replacing all occurrences of old
	 * in string with new, where old and new are single character strings. If old
	 * or new are longer than one character, each substring of string that matches
	 * the regular expression old is replaced with new. When doing a simple string
	 * replacement, and old contains regular expression metacharacters ('.', '[',
	 * ']', '^', '$', etc.), you must escape them with a "\\". For example, to
	 * replace "[xx]" with "yy", use string=replace(string,"\\[xx\\]","yy"). See
	 * also: matches.
	 */
	public static String replace(final Variable string, final Variable oldString,
		final Variable newString)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Breaks a string into an array of substrings. Delimiters is a string
	 * containing one or more delimiter characters. The default delimiter set
	 * " \t\n\r" (space, tab, newline, return) is used if delimiters is an empty
	 * string or split is called with only one argument. Returns a one element
	 * array if no delimiter is found.
	 */
	public static String split(final String string, final String delimiters) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns true (1) if string starts with prefix. See also: endsWith, indexOf,
	 * substring, toLowerCase, matches.
	 */
	public static boolean
		startsWith(final Variable string, final Variable prefix)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns a new string that is a substring of string. The substring begins at
	 * index1 and extends to the character at index2 - 1. See also: indexOf,
	 * startsWith, endsWith, replace.
	 */
	public static String substring(final Variable string, final Variable index1,
		final Variable index2)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns a substring of string that begins at index and extends to the end
	 * of string.
	 */
	public static String substring(final Variable string, final Variable index) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns a binary string representation of number.
	 */
	public static String toBinary(final Variable number) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns a hexadecimal string representation of number.
	 */
	public static String toHex(final Variable number) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns a new string that is a copy of string with all the characters
	 * converted to lower case.
	 */
	public static String toLowerCase(final Variable string) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns a decimal string representation of number. See also: toBinary,
	 * toHex, parseFloat and parseInt.
	 */
	public static String toString(final Object number) {
		return number.toString();
	}

	/**
	 * Converts number into a string, using the specified number of decimal
	 * places.
	 */
	public static String toString(final Variable number,
		final Variable decimalPlaces)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns a new string that is a copy of string with all the characters
	 * converted to upper case.
	 */
	public static String toUpperCase(final Variable string) {
		throw new RuntimeException("TODO");
	}

	// Selection (ROI) functions

	/**
	 * Equivalent to clicking on the current image at (x,y) with the wand tool.
	 * Note that some objects, especially one pixel wide lines, may not be
	 * reliably traced unless they have been thresholded (highlighted in red)
	 * using setThreshold.
	 */
	public static void doWand(final Variable x, final Variable y) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Traces the boundary of the area with pixel values within 'tolerance' of the
	 * value of the pixel at (x,y). 'mode' can be "4-connected", "8-connected" or
	 * "Legacy". "Legacy" is for compatibility with previous versions of ImageJ;
	 * it is ignored if 'tolerance' > 0.
	 */
	public static void doWand(final Variable x, final Variable y,
		final Variable tolerance, final Variable mode)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Creates an elliptical selection, where x1,y1,x2,y2 specify the major axis
	 * of the ellipse and aspectRatio (<=1.0) is the ratio of the lengths of minor
	 * and major axis. Requires 1.44k.
	 */
	public static void makeEllipse(final Variable x1, final Variable y1,
		final Variable x2, final Variable y2, final Variable aspectRatio)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Creates a new straight line selection. The origin (0,0) is assumed to be
	 * the upper left corner of the image. Coordinates are in pixels. You can
	 * create segmented line selections by specifying more than two coordinate
	 * pairs, for example makeLine(25,34,44,19,69,30,71,56).
	 */
	public static void makeLine(final Variable x1, final Variable y1,
		final Variable x2, final Variable y2)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Creates a straight line selection with the specified width. See also:
	 * getLine.
	 */
	public static void makeLine(final Variable x1, final Variable y1,
		final Variable x2, final Variable y2, final Variable lineWidth)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Creates an elliptical selection, where (x,y) define the upper left corner
	 * of the bounding rectangle of the ellipse.
	 */
	public static void makeOval(final Variable x, final Variable y,
		final Variable width, final Variable height)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Creates a point selection at the specified location. Create a multi-point
	 * selection by using makeSelection("point",xpoints,ypoints). Use
	 * setKeyDown("shift"); makePoint(x, y); to add a point to an existing point
	 * selection.
	 */
	public static void makePoint(final Variable x, final Variable y) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Creates a polygonal selection. At least three coordinate pairs must be
	 * specified, but not more than 200. As an example,
	 * makePolygon(20,48,59,13,101,40,75,77,38,70) creates a polygon selection
	 * with five sides.
	 */
	public static void makePolygon(final double x1, final double... y1) { // TODO:
																																				// varargs
		throw new RuntimeException("TODO");
	}

	/**
	 * Creates a rectangular selection. The x and y arguments are the coordinates
	 * (in pixels) of the upper left corner of the selection. The origin (0,0) of
	 * the coordinate system is the upper left corner of the image.
	 */
	public static void makeRectangle(final Variable x, final Variable y,
		final Variable width, final Variable height)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Creates a rounded rectangular selection using the specified corner arc
	 * size. Requires 1.43n.
	 */
	public static void makeRectangle(final Variable x, final Variable y,
		final Variable width, final Variable height, final Variable arcSize)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Creates a selection from a list of XY coordinates. The first argument
	 * should be "polygon", "freehand", "polyline", "freeline", "angle" or
	 * "point", or the numeric value returned by selectionType. The xcoord and
	 * ycoord arguments are numeric arrays that contain the X and Y coordinates.
	 * See the MakeSelectionDemo macro for examples.
	 */
	public static void makeSelection(final Variable type, final Variable xcoord,
		final Variable ycoord)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Creates a text selection at the specified coordinates. The selection will
	 * use the font and size specified by the last call to setFont(). The
	 * CreateOverlay macro provides an example. Requires 1.43h.
	 */
	public static void makeText(final Variable string, final Variable x,
		final Variable y)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Runs an ROI Manager command, where cmd must be "Add", "Add & Draw",
	 * "Update", "Delete", "Deselect", "Measure", "Draw", "Fill", "Label",
	 * "Combine", "Split", "Sort", "Reset", "Multi Measure", "AND", "OR",
	 * "Multi Plot", "Show All", "Show None", "Show all with labels",
	 * "Show all without labels" or "Remove Slice Info". The ROI Manager is opened
	 * if it is not already open. Use roiManager("reset") to delete all items on
	 * the list. Use setOption("Show All", boolean) to enable/disable "Show All"
	 * mode. For examples, refer to the RoiManagerMacros, ROI Manager Stack Demo
	 * and RoiManagerSpeedTest macros. "count": Returns the number of items in the
	 * ROI Manager list. "index": Returns the index of the currently selected item
	 * on the ROI Manager list, or -1 if the list is empty, no items are selected,
	 * or more than one item is selected.
	 */
	public static Object roiManager(final Variable cmd) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Runs an ROI Manager I/O command, where cmd is "Open",
	 * "Save or "Rename", and name is a file path or name. The "Save
	 * " option ignores selections and saves all the ROIs as a ZIP archive. It displays a file save dialog if name is "
	 * ". You can get the selection name using call("
	 * ij.plugin.frame.RoiManager.getName", index). The ROI Manager is opened if
	 * it is not already open. "select", index: Selects an item in the ROI Manager
	 * list, where index must be greater than or equal zero and less than the
	 * value returned by roiManager("count"). Note that macros that use this
	 * function sometimes run orders of magnitude faster in batch mode. Use
	 * roiManager("deselect") to deselect all items on the list. For an example,
	 * refer to the ROI Manager Stack Demo macro. "select", indexes: Selects
	 * multiple items in the ROI Manager list, where indexes is an array of
	 * integers, each of which must be greater than or equal to 0 and less than
	 * the value returned by roiManager("count"). The selected ROIs are not
	 * highlighted in the ROI Manager list and are no longer selected after the
	 * next ROI Manager command is executed.
	 */
	public static Object roiManager(final Variable cmd, final Variable name) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Returns true if the point x,y is inside the current selection. Aborts the
	 * macro if there is no selection. Requires 1.44g.
	 */
	public static boolean selectionContains(final Variable x, final Variable y) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Specifies the justification used by drawString() and Plot.addText(). The
	 * argument can be "left", "right" or "center". The default is "left".
	 */
	public static void setJustification(final String align) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Specifies the line width (in pixels) used by drawLine(), lineTo(),
	 * drawRect() and drawOval().
	 */
	public static void setLineWidth(final Variable width) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Sets the transfer mode used by the Edit>Paste command, where mode is
	 * "Copy", "Blend", "Average", "Difference", "Transparent-white",
	 * "Transparent-zero", "AND", "OR", "XOR", "Add", "Subtract", "Multiply",
	 * "Divide, "Min" or "Max". The GetCurrentPasteMode macro demonstrates how to
	 * get the current paste mode. In ImageJ 1.42 and later, the paste mode is
	 * saved and restored by the saveSettings and restoreSettings.
	 */
	public static void setPasteMode(final Variable mode) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Moves the current selection to (x,y), where x and y are the pixel
	 * coordinates of the upper left corner of the selection's bounding rectangle.
	 * The RoiManagerMoveSelections macro uses this function to move all the ROI
	 * Manager selections a specified distance. See also: getSelectionBounds.
	 */
	public static void setSelectionLocation(final Variable x, final Variable y) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Sets the name of the current selection to the specified name. Aborts the
	 * macro if there is no selection. See also: selectionName and selectionType.
	 */
	public static void setSelectionName(final Variable name) {
		throw new RuntimeException("TODO");
	}

	// Image-related

	/**
	 * Closes the active image. This function has the advantage of not closing the
	 * "Log" or "Results" window when you meant to close the active image. Use
	 * run("Close") to close non-image windows.
	 */
	public static void close() {
		throw new RuntimeException("TODO");
	}

	/**
	 * Runs the Process>Image Calculator tool, where operator
	 * ("add","subtract","multiply","divide", "and", "or", "xor", "min", "max",
	 * "average", "difference" or "copy") specifies the operation, and img1 and
	 * img2 specify the operands. img1 and img2 can be either an image title (a
	 * string) or an image ID (an integer). The operator string can include up to
	 * three modifiers: "create" (e.g., "add create") causes the result to be
	 * stored in a new window, "32-bit" causes the result to be 32-bit
	 * floating-point and "stack" causes the entire stack to be processed. See the
	 * ImageCalculatorDemo macros for examples.
	 */
	public static Dataset imageCalculator(final Variable operator,
		final Variable img1, final Variable img2)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Opens a new image or stack using the name title. The string type should
	 * contain "8-bit", "16-bit", "32-bit" or "RGB". In addition, it can contain
	 * "white", "black" or "ramp" (the default is "white"). As an example, use
	 * "16-bit ramp" to create a 16-bit image containing a grayscale ramp. Width
	 * and height specify the width and height of the image in pixels. Depth
	 * specifies the number of stack slices.
	 */
	public static Dataset newImage(final Variable title, final Variable type,
		final Variable width, final Variable height, final Variable depth)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Opens and displays a tiff, dicom, fits, pgm, jpeg, bmp, gif, lut, roi, or
	 * text file. Displays an error message and aborts the macro if the specified
	 * file is not in one of the supported formats, or if the file is not found.
	 * Displays a file open dialog box if path is an empty string or if there is
	 * no argument. Use the File>Open command with the command recorder running to
	 * generate calls to this function. With 1.41k or later, opens images
	 * specified by a URL, for example
	 * open("http://imagej.nih.gov/ij/images/clown.gif").
	 */
	public static Dataset open(final Variable path) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Opens the nth image in the TIFF stack specified by path. For example, the
	 * first image in the stack is opened if n=1 and the tenth is opened if n=10.
	 */
	public static Dataset open(final Variable path, final Variable n) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Restores the backup image created by the snapshot function. Note that
	 * reset() and run("Undo") are basically the same, so only one run() call can
	 * be reset.
	 */
	public static void reset() {
		throw new RuntimeException("TODO");
	}

	/**
	 * With 16-bit and 32-bit images, resets the minimum and maximum displayed
	 * pixel values (display range) to be the same as the current image's minimum
	 * and maximum pixel values. With 8-bit images, sets the display range to
	 * 0-255. With RGB images, does nothing. See the DisplayRangeMacros for
	 * examples.
	 */
	public static void resetMinAndMax() {
		throw new RuntimeException("TODO");
	}

	/**
	 * Disables thresholding. See also: setThreshold, setAutoThreshold,
	 * getThreshold.
	 */
	public static void resetThreshold() {
		throw new RuntimeException("TODO");
	}

	/**
	 * Saves an image, lookup table, selection or text window to the specified
	 * file path. The path must end in ".tif", ".jpg", ".gif", ".zip", ".raw",
	 * ".avi", ".bmp", ".fits", ".png", ".pgm", ".lut", ".roi" or ".txt".
	 */
	public static void save(final Variable path) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Saves the active image, lookup table, selection, measurement results,
	 * selection XY coordinates or text window to the specified file path. The
	 * format argument must be "tiff", "jpeg", "gif", "zip", "raw", "avi", "bmp",
	 * "fits", "png", "pgm", "text image", "lut", "selection", "measurements",
	 * "xy Coordinates" or "text". Use saveAs(format) to have a "Save As" dialog
	 * displayed.
	 */
	public static void saveAs(final Variable format, final Variable path) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Moves the active window to a new location. See also: getLocationAndSize,
	 * screenWidth, screenHeight.
	 */
	public static void setLocation(final Variable x, final Variable y) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Moves and resizes the active image window. The new location of the top-left
	 * corner is specified by x and y, and the new size by width and height.
	 */
	public static void setLocation(final Variable x, final Variable y,
		final Variable width, final Variable height)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Creates a new lookup table and assigns it to the current image. Three input
	 * arrays are required, each containing 256 intensity values. See the
	 * LookupTables macros for examples.
	 */
	public static void setLut(final Variable reds, final Variable greens,
		final Variable blues)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Sets the minimum and maximum displayed pixel values (display range). See
	 * the DisplayRangeMacros for examples.
	 */
	public static void setMinAndMax(final Variable min, final Variable max) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Sets the display range of specified channels in an RGB image, where 4=red,
	 * 2=green, 1=blue, 6=red+green, etc. Note that the pixel data is altered
	 * since RGB images, unlike composite color images, do not have a LUT for each
	 * channel.
	 */
	public static void setMinAndMax(final Variable min, final Variable max,
		final Variable channels)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Sets the weighting factors used by the Analyze>Measure, Image>Type>8-bit
	 * and Analyze>Histogram commands when they convert RGB pixels values to
	 * grayscale. The sum of the weights must be 1.0. Use (1/3,1/3,1/3) for equal
	 * weighting of red, green and blue. The weighting factors in effect when the
	 * macro started are restored when it terminates. For examples, see the
	 * MeasureRGB, ExtractRGBChannels and RGB_Histogram macros.
	 */
	public static void setRGBWeights(final Variable redWeight,
		final Variable greenWeight, final Variable blueWeight)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Displays the nth slice of the active stack. Does nothing if the active
	 * image is not a stack. For an example, see the MeasureStack macros. See
	 * also: getSliceNumber, nSlices.
	 */
	public static void setSlice(final Variable n) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Sets the lower and upper threshold levels. The values are uncalibrated
	 * except for 16-bit images (e.g., unsigned 16-bit images). There is an
	 * optional third argument that can be "red", "black & white", "over/under" or
	 * "no color". See also: setAutoThreshold, getThreshold, resetThreshold.
	 */
	public static void setThreshold(final Variable lower, final Variable upper) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Defines the voxel dimensions and unit of length ("pixel", "mm", etc.) for
	 * the current image or stack. A "um" unit will be converted to "µm" (requires
	 * v1.43). The depth argument is ignored if the current image is not a stack.
	 * See also: getVoxelSize.
	 */
	public static void setVoxelSize(final Variable width, final Variable height,
		final Variable depth, final Variable unit)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Sets the Z coordinate used by getPixel(), setPixel() and changeValues().
	 * The argument must be in the range 0 to n-1, where n is the number of images
	 * in the stack. For an examples, see the Z Profile Plotter Tool.
	 */
	public static void setZCoordinate(final Variable z) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Creates a backup copy of the current image that can be later restored using
	 * the reset function. For examples, see the ImageRotator and BouncingBar
	 * macros.
	 */
	public static void snapshot() {
		throw new RuntimeException("TODO");
	}

	/**
	 * Redraws the active image.
	 */
	public static void updateDisplay() {
		throw new RuntimeException("TODO");
	}

	// Miscellaneous

	/**
	 * If boolean is true, the display is refreshed each time lineTo(),
	 * drawLine(), drawString(), etc. are called, otherwise, the display is
	 * refreshed only when updateDisplay() is called or when the macro terminates.
	 */
	public static void autoUpdate(final boolean value) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Emits an audible beep.
	 */
	public static void beep() {
		throw new RuntimeException("TODO");
	}

	/**
	 * Uses the calibration function of the active image to convert a raw pixel
	 * value to a density calibrated value. The argument must be an integer in the
	 * range 0-255 (for 8-bit images) or 0-65535 (for 16-bit images). Returns the
	 * same value if the active image does not have a calibration function.
	 */
	public static double calibrate(final Variable value) {
		throw new RuntimeException("TODO");
	}

	// From here

	/**
	 * Returns a new array containing size elements. You can also create arrays by
	 * listing the elements, for example newArray(1,4,7) or newArray("a","b","c").
	 * For more examples, see the Arrays macro. The ImageJ macro language does not
	 * directly support 2D arrays. As a work around, either create a blank image
	 * and use setPixel() and getPixel(), or create a 1D array using
	 * a=newArray(xmax*ymax) and do your own indexing (e.g., value=a[x+y*xmax]).
	 */
	public static Object[] newArray(final int size) {
		return new Object[size];
	}

	/**
	 * Defines a menu that will be added to the toolbar when the menu tool named
	 * macroName is installed. Menu tools are macros with names ending in
	 * "Menu Tool". StringArray is an array containing the menu commands. Returns
	 * a copy of stringArray. For an example, refer to the Toolbar Menus toolset.
	 */
	public static void newMenu(final Variable macroName,
		final Variable stringArray)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Displays a dialog box containing the specified message and "Yes", "No" and
	 * "Cancel" buttons. Returns true (1) if the user clicks "Yes", returns false
	 * (0) if the user clicks "No" and exits the macro if the user clicks
	 * "Cancel".
	 */
	public static boolean getBoolean(final String message) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Displays a dialog box and returns the number entered by the user. The first
	 * argument is the prompting message and the second is the value initially
	 * displayed in the dialog. Exits the macro if the user clicks on "Cancel" in
	 * the dialog. Returns defaultValue if the user enters an invalid number. See
	 * also: Dialog.create.
	 */
	public static double
		getNumber(final String prompt, final double defaultValue)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Displays a dialog box and returns the string entered by the user. The first
	 * argument is the prompting message and the second is the initial string
	 * value. Exits the macro if the user clicks on "Cancel" or enters an empty
	 * string. See also: Dialog.create.
	 */
	public static String
		getString(final String prompt, final String defaultValue)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Displays "message" in a dialog box.
	 */
	public static void showMessage(final String message) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Displays "message" in a dialog box using "title" as the the dialog box
	 * title.
	 */
	public static void showMessage(final String title, final String message) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Displays "message" in a dialog box with "OK" and "Cancel" buttons. "Title"
	 * (optional) is the dialog box title. The macro exits if the user clicks
	 * "Cancel" button. See also: getBoolean.
	 */
	public static void showMessageWithCancel(final String message) {
		showMessageWithCancel(null, message);
	}

	/**
	 * Displays "message" in a dialog box with "OK" and "Cancel" buttons. "Title"
	 * (optional) is the dialog box title. The macro exits if the user clicks
	 * "Cancel" button. See also: getBoolean.
	 */
	public static void showMessageWithCancel(final String title,
		final String message)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Updates the ImageJ progress bar, where 0.0<=progress<=1.0. The progress bar
	 * is not displayed if the time between the first and second calls to this
	 * function is less than 30 milliseconds. It is erased when the macro
	 * terminates or progress is >=1.0.
	 */
	public static void showProgress(final Variable progress) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Displays a message in the ImageJ status bar.
	 */
	public static void showStatus(final String message) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Outputs a string to the "Log" window. Numeric arguments are automatically
	 * converted to strings. The print() function accepts multiple arguments. For
	 * example, you can use print(x,y,width, height) instead of
	 * print(x+" "+y+" "+width+" "+height). If the first argument is a file handle
	 * returned by File.open(path), then the second is saved in the refered file
	 * (see SaveTextFileDemo). Numeric expressions are automatically converted to
	 * strings using four decimal places, or use the d2s function to specify the
	 * decimal places. For example, print(2/3) outputs "0.6667" but
	 * print(d2s(2/3,1)) outputs "0.7". The print() function accepts commands such
	 * as "\\Clear", "\\Update:<text>" and "\\Update<n>:<text>" (for n<26) that
	 * clear the "Log" window and update its contents. For example,
	 * print("\\Clear") erases the Log window, print("\\Update:new text") replaces
	 * the last line with "new text" and print("\\Update8:new 8th line") replaces
	 * the 8th line with "new 8th line". Refer to the LogWindowTricks macro for an
	 * example. The second argument to print(arg1, arg2) is appended to a text
	 * window or table if the first argument is a window title in brackets, for
	 * example print("[My Window]", "Hello, world"). With text windows, newline
	 * characters ("\n") are not automatically appended and text that starts with
	 * "\\Update:" replaces the entire contents of the window. Refer to the
	 * PrintToTextWindow, Clock and ProgressBar macros for examples. The second
	 * argument to print(arg1, arg2) is appended to a table (e.g., ResultsTable)
	 * if the first argument is the title of the table in brackets. Use the
	 * Plugins>New command to open a blank table. Any command that can be sent to
	 * the "Log" window ("\\Clear", "\\Update:<text>" , etc.) can also be sent to
	 * a table. Refer to the SineCosineTable2 and TableTricks macros for examples.
	 */
	public static void print2(final String text) { // TODO: support print(Variable
																									// String, Variable Object) {
		IJ2.getLog().append(text);
	}

	/**
	 * Changes the title of the active image to the string name.
	 */
	public static void rename(final Variable name) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Switches back to the previously selected tool. Useful for creating a tool
	 * macro that performs an action, such as opening a file, when the user clicks
	 * on the tool icon.
	 */
	public static void restorePreviousTool() {
		throw new RuntimeException("TODO");
	}

	/**
	 * Restores Edit>Options submenu settings saved by the saveSettings function.
	 */
	public static void restoreSettings() {
		throw new RuntimeException("TODO");
	}

	/**
	 * Saves most Edit>Options submenu settings so they can be restored later by
	 * calling restoreSettings.
	 */
	public static void saveSettings() {
		throw new RuntimeException("TODO");
	}

	/**
	 * Activates the image with the specified ID (a negative number). If id is
	 * greater than zero, activates the idth image listed in the Window menu. The
	 * id can also be an image title (a string).
	 */
	public static void selectImage(final Variable id) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Activates the window with the title "name".
	 */
	public static void selectWindow(final String windowName) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Uses the "Default" method to determine the threshold. It may select dark or
	 * bright areas as thresholded, as was the case with the
	 * Image>Adjust>Threshold "Auto" option in ImageJ 1.42o and earlier. See also:
	 * setThreshold, getThreshold, resetThreshold.
	 */
	public static void setAutoThreshold() {
		throw new RuntimeException("TODO");
	}

	/**
	 * Uses the specified method to set the threshold levels of the current image.
	 * Use the getList("threshold.methods") function to get a list of the
	 * available methods. Concatenate " dark" to the method name if the image has
	 * a dark background. For an example, see the AutoThresholdingDemo macro.
	 */
	public static void setAutoThreshold(final Variable method) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Sets the background color, where r, g, and b are >= 0 and <= 255. See also:
	 * setForegroundColor and getValue("color.background").
	 */
	public static void setBackgroundColor(final Variable r, final Variable g,
		final Variable b)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * If arg is true, the interpreter enters batch mode and images are not
	 * displayed, allowing the macro to run up to 20 times faster. If arg is
	 * false, exits batch mode and displays the active image in a window. ImageJ
	 * exits batch mode when the macro terminates if there is no
	 * setBatchMode(false) call. Note that a macro should not call
	 * setBatchMode(true) more than once. Set arg to "exit and display" to exit
	 * batch mode and display all open batch mode images. Here are five example
	 * batch mode macros: BatchModeTest, BatchMeasure, BatchSetScale,
	 * ReplaceRedWithMagenta.
	 */
	public static void setBatchMode(final Variable arg) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Sets the drawing color, where r, g, and b are >= 0 and <= 255. With 16 and
	 * 32 bit images, sets the color to 0 if r=g=b=0. With 16 and 32 bit images,
	 * use setColor(1,0,0) to make the drawing color the same is the minimum
	 * displayed pixel value. SetColor() is faster than setForegroundColor(), and
	 * it does not change the system wide foreground color or repaint the color
	 * picker tool icon, but it cannot be used to specify the color used by
	 * commands called from macros, for example run("Fill").
	 */
	public static void setColor(final Variable r, final Variable g,
		final Variable b)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Sets the drawing color. For 8 bit images, 0<=value<=255. For 16 bit images,
	 * 0<=value<=65535. For RGB images, use hex constants (e.g., 0xff0000 for
	 * red). This function does not change the foreground color used by
	 * run("Draw") and run("Fill").
	 */
	public static void setColor(final int value) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Sets the font used by the drawString function. The first argument is the
	 * font name. It should be "SansSerif", "Serif" or "Monospaced". The second is
	 * the point size. The optional third argument is a string containing "bold"
	 * or "italic", or both. The third argument can also contain the keyword
	 * "antialiased". For examples, run the TextDemo macro.
	 */
	public static void setFont(final String name, final int size) {
		setFont(name, size, null);
	}

	/**
	 * Sets the font used by the drawString function. The first argument is the
	 * font name. It should be "SansSerif", "Serif" or "Monospaced". The second is
	 * the point size. The optional third argument is a string containing "bold"
	 * or "italic", or both. The third argument can also contain the keyword
	 * "antialiased". For examples, run the TextDemo macro.
	 */
	public static void setFont(final String name, final int size,
		final String style)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Sets the font to the one defined in the Edit>Options>Fonts window. See
	 * also: getInfo("font.name"), getValue("font.size") and
	 * getValue("font.height"). Requires 1.43f.
	 */
	public static void setFont(final String name) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Sets the foreground color, where r, g, and b are >= 0 and <= 255. See also:
	 * setColor, setBackgroundColor and getValue("color.foreground"),
	 */
	public static void setForegroundColor(final Variable r, final Variable g,
		final Variable b)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Simulates pressing the shift, alt or space keys, where keys is a string
	 * containing some combination of "shift", "alt" or "space". Any key not
	 * specified is set "up". Use setKeyDown("none") to set all keys in the "up"
	 * position. Call setKeyDown("esc") to abort the currently running macro or
	 * plugin. For examples, see the CompositeSelections, DoWandDemo and
	 * AbortMacroActionTool macros. See also: isKeyDown.
	 */
	public static void setKeyDown(final Variable keys) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Assigns the metadata in string to the "Info" image property of the current
	 * image. This metadata is displayed by Image>Show Info and saved as part of
	 * the TIFF header. See also: getMetadata. key must be "Info" Sets string as
	 * the label of the current image or stack slice. The first 60 characters, or
	 * up to the first newline, of the label are displayed as part of the image
	 * subtitle. The labels are saved as part of the TIFF header. See also:
	 * getMetadata. key must be "Label"
	 */
	public static void setMetadata(final String key, final String string) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Enables or disables an ImageJ option, where option is one of the following
	 * options and boolean is either true or false. "DisablePopupMenu"
	 * enables/disables the the menu displayed when you right click on an image.
	 * "Show All" enables/disables the the "Show All" mode in the ROI Manager.
	 * "Changes" sets/resets the 'changes' flag of the current image. Set this
	 * option false to avoid "Save Changes?" dialog boxes when closing images.
	 * "DebugMode" enables/disables the ImageJ debug mode. ImageJ displays
	 * information, such as TIFF tag values, when it is in debug mode.
	 * "OpenUsingPlugins" controls whether standard file types (TIFF, JPEG, GIF,
	 * etc.) are opened by external plugins or by ImageJ (example). "QueueMacros"
	 * controls whether macros invoked using keyboard shortcuts run sequentially
	 * on the event dispatch thread (EDT) or in separate, possibly concurrent,
	 * threads (example). In "QueueMacros" mode, screen updates, which also run on
	 * the EDT, are delayed until the macro finishes. Note that "QueueMacros" does
	 * not work with macros using function key shortcuts in ImageJ 1.41g and
	 * earlier. "DisableUndo" enables/disables the Edit>Undo command. Note that a
	 * setOption("DisableUndo",true) call without a corresponding
	 * setOption("DisableUndo",false) will cause Edit>Undo to not work as expected
	 * until ImageJ is restarted. "Display Label", "Limit to Threshold", "Area",
	 * "Mean" and "Std", added in v1.41, enable/disable the corresponding
	 * Analyze>Set Measurements options. "ShowMin" determines whether or not the
	 * "Min" value is displayed in the Results window when "Min & Max Gray Value"
	 * is enabled in the Analyze>Set Measurements dialog box. "BlackBackground"
	 * enables/disables the Process>Binary>Options "Black background" option.
	 * "Bicubic" provides a way to force commands like Edit>Selection>Straighten,
	 * that normally use bilinear interpolation, to use bicubic interpolation.
	 * "Loop" enables/disables the Image>Stacks>Tools>Animation Options
	 * "Loop back and forth" option. Requires v1.44n. "ShowRowNumbers"
	 * enables/disables display of Results table row numbers (example). Requires
	 * v1.45o.
	 */
	public static void setOption(final Variable option, final boolean value) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Adds an entry to the ImageJ results table or modifies an existing entry.
	 * The first argument specifies a column in the table. If the specified column
	 * does not exist, it is added. The second argument specifies the row, where
	 * 0<=row<=nResults. (nResults is a predefined variable.) A row is added to
	 * the table if row=nResults. The third argument is the value to be added or
	 * modified. Call setResult("Label", row, string) to set the row label. Call
	 * updateResults() to display the updated table in the "Results" window. For
	 * examples, see the SineCosineTable and ConvexitySolidarity macros.
	 */
	public static void setResult(final String columnName, final int row,
		final double value)
	{
		throw new RuntimeException("TODO");
	}

	/**
	 * Switches to the specified tool, where name is "rectangle", "roundrect",
	 * "elliptical", "brush", "polygon", "freehand", "line", "polyline",
	 * "freeline", "arrow", "angle", "point", "multipoint", "wand", "text",
	 * "zoom", "hand" or "dropper". Refer to the SetToolDemo, ToolShortcuts or
	 * ToolSwitcher, macros for examples. See also: IJ.getToolName. or id
	 * 0=rectangle, 1=oval, 2=polygon, 3=freehand, 4=straight line, 5=polyline,
	 * 6=freeline, 7=point, 8=wand, 9=text, 10=spare, 11=zoom, 12=hand,
	 * 13=dropper, 14=angle, 15...21=spare. See also: toolID.
	 */
	public static void setTool(final Object idOrName) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Call this function before drawing on an image to allow the user the option
	 * of later restoring the original image using Edit/Undo. Note that
	 * setupUndo() may not work as intended with macros that call the run()
	 * function. For an example, see the DrawingTools tool set.
	 */
	public static void setupUndo() {
		throw new RuntimeException("TODO");
	}

	/**
	 * Converts unscaled pixel coordinates to scaled coordinates using the
	 * properties of the current image or plot. Also accepts arrays.
	 */
	// TODO: accept arrays
	public static void toScaled(final Variable x, final Variable y) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Converts scaled coordinates to unscaled pixel coordinates using the
	 * properties of the current image or plot. Also accepts arrays. Refer to the
	 * AdvancedPlots macro set for examples.
	 */
	public static void toUnscaled(final Variable x, final Variable y) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Converts (in place) a length in pixels to a scaled length using the
	 * properties of the current image.
	 */
	public static void toScaled(final Variable length) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Converts (in place) a scaled length to a length in pixels using the
	 * properties of the current image.
	 */
	public static void toUnscaled(final Variable length) {
		throw new RuntimeException("TODO");
	}

	/**
	 * Call this function to update the "Results" window after the results table
	 * has been modified by calls to the setResult() function.
	 */
	public static void updateResults() {
		throw new RuntimeException("TODO");
	}

	/*
		public static void getDimensions(Variable width, Variable height, Variable channels, Variable slices, Variable frames) {
			width.setValue(1);
			height.setValue(2);
			channels.setValue(3);
			slices.setValue(4);
			frames.setValue(5);
		}
	*/

	// Inner classes

	/**
	 * These functions operate on arrays. Refer to the ArrayFunctions macro for
	 * examples.
	 */
	public static class Array<T> {

		// TODO: test that this works
		/** Returns a copy of array. */
		public static <T> Object[] copy(final T[] array) {
			final Object[] result = new Object[array.length];
			System.arraycopy(array, 0, result, 0, array.length);
			return result;
		}

		/** Assigns the specified numeric value to each element of array. */
		public static <T> void fill(final T[] array, final T value) {
			Arrays.fill(array, value);
		}

		/**
		 * Returns the min, max, mean, and stdDev of array, which must contain all
		 * numbers.
		 */
		public static void getStatistics(final Object array, final Variable min,
			final Variable max, final Variable mean, final Variable stdDev)
		{
			double min2, max2, sum, sum2;

			min2 = Double.MAX_VALUE;
			max2 = -Double.MAX_VALUE;
			sum = sum2 = 0;

			if (array instanceof int[]) {
				final int[] intArray = (int[]) array;
				for (final int value : intArray) {
					if (min2 > value) min2 = value;
					if (max2 < value) max2 = value;
					sum += value;
					sum2 = value * value;
				}
				sum /= intArray.length;
				sum2 /= intArray.length;
			}
			else if (array instanceof double[]) {
				final double[] doubleArray = (double[]) array;
				for (final double value : doubleArray) {
					if (min2 > value) min2 = value;
					if (max2 < value) max2 = value;
					sum += value;
					sum2 = value * value;
				}
				sum /= doubleArray.length;
				sum2 /= doubleArray.length;
			}

			min.setValue(min2);
			max.setValue(max2);
			mean.setValue(sum2);
			stdDev.setValue(Math.sqrt(sum2 - sum * sum));
		}

		/** Inverts the order of the elements in array. Requires 1.43h. */
		public static <T> void invert(final T[] array) {
			for (int i = 0; i < array.length - i - 1; i++) {
				final T swap = array[i];
				array[i] = array[array.length - i - 1];
				array[array.length - i - 1] = swap;
			}
		}

		/**
		 * Sorts array, which must contain all numbers or all strings. String sorts
		 * are case-insensitive in v1.44i or later.
		 */
		public static void sort(final Object array) {
			if (array instanceof int[]) Arrays.sort((int[]) array);
			else if (array instanceof double[]) Arrays.sort((double[]) array);
		}

		/**
		 * Returns, as an array, the rank positions of array, which must contain all
		 * numbers or all strings (example). Requires 1.44k.
		 */
		public static <T> Object rankPositions(final T[] array) {
			throw new RuntimeException("TODO");
		}

		/** Returns an array that contains the first n elements of array. */
		public static <T> Object[] trim(final T[] array, final int n) {
			if (array.length == n) return array;
			final Object[] result = new Object[n];
			System.arraycopy(array, 0, result, 0, n);
			return result;
		}

	}

	public static class Dialog {

		/**
		 * Creates a dialog box with the specified title. Call Dialog.addString(),
		 * Dialog.addNumber(), etc. to add components to the dialog. Call
		 * Dialog.show() to display the dialog and Dialog.getString(),
		 * Dialog.getNumber(), etc. to retrieve the values entered by the user. Refer
		 * to the DialogDemo macro for an example.
		 */
		public static void create(final String title) {
			throw new RuntimeException("TODO");
		}

		/**
		 * Adds a message to the dialog. The message can be broken into multiple
		 * lines by inserting new line characters ("\n") into the string.
		 */
		public static void addMessage(final String string) {
			throw new RuntimeException("TODO");
		}

		/**
		 * Adds a text field to the dialog, using the specified label and initial
		 * text.
		 */
		public static void addString(final String label, final String initialText) {
			throw new RuntimeException("TODO");
		}

		/**
		 * Adds a text field to the dialog, where columns specifies the field width
		 * in characters.
		 */
		public static void addString(final String label, final String initialText,
			final int columns)
		{
			throw new RuntimeException("TODO");
		}

		/**
		 * Adds a numeric field to the dialog, using the specified label and default
		 * value.
		 */
		public static void addNumber(final String label, final double defaultValue)
		{
			throw new RuntimeException("TODO");
		}

		/**
		 * Adds a numeric field, using the specified label and default value.
		 * DecimalPlaces specifies the number of digits to right of decimal point,
		 * columns specifies the the field width in characters and units is a string
		 * that is displayed to the right of the field.
		 */
		public static void addNumber(final String label, final double defaultValue,
			final int decimalPlaces, final int columns, final String units)
		{
			throw new RuntimeException("TODO");
		}

		/**
		 * Adds a slider controlled numeric field to the dialog, using the specified
		 * label, and min, max and default values (example). Values with decimal
		 * points are used when (max-min)<=5 and min, max or default are non-integer.
		 * Requires 1.45f.
		 */
		public static void addSlider(final String label, final double min,
			final double max, final double defaultValue)
		{
			throw new RuntimeException("TODO");
		}

		/**
		 * Adds a checkbox to the dialog, using the specified label and default
		 * state (true or false).
		 */
		public static void addCheckbox(final String label,
			final boolean defaultValue)
		{
			throw new RuntimeException("TODO");
		}

		/**
		 * Adds a rowsxcolumns grid of checkboxes to the dialog, using the specified
		 * labels and default states (example).
		 */
		public static void addCheckboxGroup(final int rows, final int columns,
			final String[] labels, final boolean[] defaults)
		{
			throw new RuntimeException("TODO");
		}

		/**
		 * Adds a popup menu to the dialog, where items is a string array containing
		 * the menu items.
		 */
		public static void addChoice(final String label, final String[] items) {
			addChoice(label, items, null);
		}

		/**
		 * Adds a popup menu, where items is a string array containing the choices
		 * and default is the default choice.
		 */
		public static void addChoice(final String label, final String[] items,
			final String defaultItem)
		{
			throw new RuntimeException("TODO");
		}

		/**
		 * Adds a "Help" button that opens the specified URL in the default browser.
		 * This can be used to supply a help page for this dialog or macro.
		 */
		public static void addHelp(final String url) {
			throw new RuntimeException("TODO");
		}

		/**
		 * Overrides the default insets (margins) used for the next component added
		 * to the dialog. Default insets: addMessage: 0,20,0 (empty string) or
		 * 10,20,0 addCheckbox: 15,20,0 (first checkbox) or 0,20,0 addCheckboxGroup:
		 * 10,0,0 addNumericField: 5,0,3 (first field) or 0,0,3 addStringField:
		 * 5,0,5 (first field) or 0,0,5 addChoice: 5,0,5 (first field) or 0,0,5
		 */
		public static void
			setInsets(final int top, final int left, final int bottom)
		{
			throw new RuntimeException("TODO");
		}

		/**
		 * Displays the dialog and waits until the user clicks "OK" or "Cancel". The
		 * macro terminates if the user clicks "Cancel".
		 */
		public static void show() {
			throw new RuntimeException("TODO");
		}

		/** Returns a string containing the contents of the next text field. */
		public static String getString() {
			throw new RuntimeException("TODO");
		}

		/** Returns the contents of the next numeric field. */
		public static double getNumber() {
			throw new RuntimeException("TODO");
		}

		/** Returns the state (true or false) of the next checkbox. */
		public static boolean getCheckbox() {
			throw new RuntimeException("TODO");
		}

		/** Returns the selected item (a string) from the next popup menu. */
		public static String getChoice() {
			throw new RuntimeException("TODO");
		}

	}

	/**
	 * These are functions that have been added to the macro language by plugins
	 * using the MacroExtension interface. The Image5D_Extensions plugin, for
	 * example, adds functions that work with Image5D. The Serial Macro Extensions
	 * plugin adds functions, such as Ext.open("COM8",9600,"") and Ext.write("a"),
	 * that talk to serial devices.
	 */
	public static class Ext {
		// (Macro Extension) Functions
	}

	/**
	 * These functions allow you to get information about a file, read or write a
	 * text file, create a directory, or to delete, rename or move a file or
	 * directory. The FileDemo macro demonstrates how to use them. See also:
	 * getDirectory and getFileList.
	 */
	public static class File {

		/** Appends string to the end of the specified file. */
		public static void append(final String string, final String path) {
			throw new RuntimeException("TODO");
		}

		/**
		 * Closes the specified file, which must have been opened using File.open().
		 */
		public static void close(final String f) {
			throw new RuntimeException("TODO");
		}

		/** Returns the date and time the specified file was last modified. */
		public static String dateLastModified(final String path) {
			throw new RuntimeException("TODO");
		}

		/**
		 * Deletes the specified file or directory. With v1.41e or later, returns
		 * "1" (true) if the file or directory was successfully deleted. If the file
		 * is a directory, it must be empty. The file must be in the user's home
		 * directory, the ImageJ directory or the temp directory.
		 */
		public static void delete(final String path) {
			throw new RuntimeException("TODO");
		}

		/**
		 * The directory path of the last file opened using open(), saveAs(),
		 * File.open() or File.openAsString().
		 */
		public static String directory() {
			throw new RuntimeException("TODO");
		}

		// TODO: make sure that boolean can be cast to int in Beanshell
		/** Returns "1" (true) if the specified file exists. */
		public static boolean exists(final String path) {
			throw new RuntimeException("TODO");
		}

		/** Returns the last name in path's name sequence. */
		public static String getName(final String path) {
			throw new RuntimeException("TODO");
		}

		/** Returns the parent of the file specified by path. */
		public static String getParent(final String path) {
			throw new RuntimeException("TODO");
		}

		/** Returns "1" (true) if the specified file is a directory. */
		public static boolean isDirectory(final String path) {
			throw new RuntimeException("TODO");
		}

		/**
		 * Returns the time the specified file was last modified as the number of
		 * milliseconds since January 1, 1970.
		 */
		public static long lastModified(final String path) {
			throw new RuntimeException("TODO");
		}

		/** Returns the length in bytes of the specified file. */
		public static long length(final String path) {
			throw new RuntimeException("TODO");
		}

		/** Creates a directory. */
		public static void makeDirectory(final String path) {
			throw new RuntimeException("TODO");
		}

		/**
		 * The name of the last file opened using a file open dialog, a file save
		 * dialog, drag and drop, or the open(path) function.
		 */
		public static String name() {
			throw new RuntimeException("TODO");
		}

		/** The name of the last file opened with the extension removed. */
		public static String nameWithoutExtension() {
			throw new RuntimeException("TODO");
		}

		/**
		 * Creates a new text file and returns a file variable that refers to it. To
		 * write to the file, pass the file variable to the print function. Displays
		 * a file save dialog box if path is an empty string. The file is closed when
		 * the macro exits. Currently, only one file can be open at a time. For an
		 * example, refer to the SaveTextFileDemo macro.
		 */
		public static void open(final String path) {
			throw new RuntimeException("TODO");
		}

		/**
		 * Opens a text file and returns the contents as a string. Displays a file
		 * open dialog box if path is an empty string. Use lines=split(str,"\n") to
		 * convert the string to an array of lines.
		 */
		public static String openAsString(final String path) {
			throw new RuntimeException("TODO");
		}

		/**
		 * Opens a file and returns up to the first 5,000 bytes as a string. Returns
		 * all the bytes in the file if the name ends with ".txt". Refer to the
		 * First10Bytes and ZapGremlins macros for examples.
		 */
		public static String openAsRawString(final String path) {
			return openAsRawString(path, 5000);
		}

		/** Opens a file and returns up to the first count bytes as a string. */
		public static String openAsRawString(final String path, final int count) {
			throw new RuntimeException("TODO");
		}

		/**
		 * Opens a URL and returns the contents as a string. Returns an emptly
		 * string if the host or file cannot be found. With v1.41i and later,
		 * returns "<Error: message>" if there any error, including host or file not
		 * found.
		 */
		public static String openUrlAsString(final String url) {
			throw new RuntimeException("TODO");
		}

		/**
		 * Displays a file open dialog and returns the path to the file choosen by
		 * the user (example). The macro exits if the user cancels the dialog.
		 */
		public static String openDialog(final String title) {
			throw new RuntimeException("TODO");
		}

		/**
		 * Renames, or moves, a file or directory. Returns "1" (true) if successful.
		 */
		public static void rename(final String path1, final String path2) {
			throw new RuntimeException("TODO");
		}

		/** Saves string as a file. */
		public static void saveString(final String string, final String path) {
			throw new RuntimeException("TODO");
		}

		/** Returns the file name separator character ("/" or "\"). */
		public static String separator() {
			throw new RuntimeException("TODO");
		}

	}

	/**
	 * These functions do curve fitting. The CurveFittingDemo macro demonstrates
	 * how to use them.
	 */
	public static class Fit {

		/**
		 * Fits the specified equation to the points defined by xpoints, ypoints.
		 * Equation can be either the equation name or an index. The equation names
		 * are shown in the drop down menu in the Analyze>Tools>Curve Fitting window.
		 * With ImageJ 1.42f or later, equation can be a string containing a
		 * user-defined equation (example).
		 */
		public static void doFit(final String equation, final double[] xpoints,
			final double[] ypoints)
		{
			throw new RuntimeException("TODO");
		}

		/**
		 * Fits the specified equation to the points defined by xpoints, ypoints,
		 * using initial parameter values contained in initialGuesses, an array
		 * equal in length to the number of parameters in equation (example).
		 */
		public static void doFit(final String equation, final double[] xpoints,
			final double[] ypoints, final double[] initialGuesses)
		{
			throw new RuntimeException("TODO");
		}

		/**
		 * Returns R^2=1-SSE/SSD, where SSE is the sum of the squares of the errors
		 * and SSD is the sum of the squares of the deviations about the mean.
		 */
		public static double rSquared() {
			throw new RuntimeException("TODO");
		}

		/** Returns the value of the specified parameter. */
		public static double p(final int index) {
			throw new RuntimeException("TODO");
		}

		/** Returns the number of parameters. */
		public static int nParams() {
			throw new RuntimeException("TODO");
		}

		/** Returns the y value at x (example). */
		public static double f(final double x) {
			throw new RuntimeException("TODO");
		}

		/** Returns the number of equations. */
		public static int nEquations() {
			throw new RuntimeException("TODO");
		}

		/** Gets the name and formula of the specified equation. */
		public static String getEquation(final int index, final String name,
			final String formula)
		{
			throw new RuntimeException("TODO");
		}

		/** Plots the current curve fit. */
		public static void plot() {
			throw new RuntimeException("TODO");
		}

		/**
		 * Causes doFit() to write a description of the curve fitting results to the
		 * Log window.
		 */
		public static void logResults() {
			throw new RuntimeException("TODO");
		}

		/** Causes doFit() to display the simplex settings dialog. */
		public static void showDialog() {
			throw new RuntimeException("TODO");
		}

	}

	/**
	 * These functions provide access to miscellaneous methods in ImageJ's IJ
	 * class. Requires 1.43l.
	 */
	public static class IJ {

		/** Deletes rows index1 through index2 in the results table. */
		public static void deleteRows(final int index1, final int index2) {
			throw new RuntimeException("TODO");
		}

		/** Returns the name of the currently selected tool. See also: setTool. */
		public static String getToolName() {
			throw new RuntimeException("TODO");
		}

		/**
		 * Returns the memory status string (e.g., "2971K of 658MB (<1%)") that is
		 * displayed when the users clicks in the ImageJ status bar.
		 */
		public static long freeMemory() {
			throw new RuntimeException("TODO");
		}

		/**
		 * Returns, as a string, the amount of memory in bytes currently used by
		 * ImageJ.
		 */
		public static long currentMemory() {
			throw new RuntimeException("TODO");
		}

		/** Displays string in the Log window. */
		public static void log(final String string) {
			throw new RuntimeException("TODO");
		}

		/**
		 * Returns, as a string, the amount of memory in bytes available to ImageJ.
		 * This value (the Java heap size) is set in the Edit>Options>Memory &
		 * Threads dialog box.
		 */
		public static void maxMemory() {
			throw new RuntimeException("TODO");
		}

		/**
		 * Pads 'n' with leading zeros and returns the result (example). Requires
		 * 1.45d.
		 */
		public static String pad(final double n, final int length) {
			throw new RuntimeException("TODO");
		}

		/**
		 * Causes next image opening error to be redirected to the Log window and
		 * prevents the macro from being aborted (example). Requires 1.43n.
		 */
		public static void redirectErrorMessages() {
			throw new RuntimeException("TODO");
		}

		/**
		 * Changes the title of the Results table to the string name. Requires
		 * 1.44c.
		 */
		public static void renameResults(final String name) {
			throw new RuntimeException("TODO");
		}

	}

	/**
	 * These functions work with a list of key/value pairs. The ListDemo macro
	 * demonstrates how to use them.
	 */
	public static class List {

		/** Adds a key/value pair to the list. */
		public static void set(final String key, final String value) {
			throw new RuntimeException("TODO");
		}

		/**
		 * Returns the string value associated with key, or an empty string if the
		 * key is not found.
		 */
		public static String get(final String key) {
			throw new RuntimeException("TODO");
		}

		/**
		 * When used in an assignment statement, returns the value associated with
		 * key as a number. Aborts the macro if the value is not a number or the key
		 * is not found.
		 */
		public static String getValue(final String key) {
			throw new RuntimeException("TODO");
		}

		/** Returns the size of the list. */
		public static int size() {
			throw new RuntimeException("TODO");
		}

		/** Resets the list. */
		public static void clear() {
			throw new RuntimeException("TODO");
		}

		/** Loads the key/value pairs in the string list. */
		public static void setList(final String[] list) {
			throw new RuntimeException("TODO");
		}

		/** Returns the list as a string. */
		public static String[] getList() {
			throw new RuntimeException("TODO");
		}

		/**
		 * Measures the current image or selection and loads the resulting parameter
		 * names (as keys) and values. All parameters listed in the Analyze>Set
		 * Measurements dialog box are measured. Use List.getValue() in an assignment
		 * statement to retrieve the values. See the DrawEllipse macro for an
		 * example.
		 */
		public static void setMeasurements() {
			throw new RuntimeException("TODO");
		}

		/**
		 * Loads the ImageJ menu commands (as keys) and the plugins that implement
		 * them (as values). Requires v1.43f.
		 */
		public static void setCommands() {
			throw new RuntimeException("TODO");
		}

	}

	/**
	 * Use these functions to create and manage non-destructive graphic overlays.
	 * For an exmple, refer to the OverlayPolygons macro. See also: setColor,
	 * setLineWidth and setFont. Requires v1.44e.
	 */
	public static class Overlay {

		/** Sets the current drawing location. */
		public static void moveTo(final int x, final int y) {
			throw new RuntimeException("TODO");
		}

		/** Draws a line from the current location to (x,y) . */
		public static void lineTo(final int x, final int y) {
			throw new RuntimeException("TODO");
		}

		/** Draws a line between (x1,y1) and (x2,y2)). */
		public static void drawLine(final int x1, final int y1, final int x2,
			final int y2)
		{
			throw new RuntimeException("TODO");
		}

		/**
		 * Adds the drawing created by Overlay.lineTo(), Overlay.drawLine(), etc. to
		 * the overlay without updating the display.
		 */
		public static void add() {
			throw new RuntimeException("TODO");
		}

		/**
		 * Sets the stack position (slice number) of the last item added to the
		 * overlay (example). Requires 1.45d.
		 */
		public static void setPosition(final int n) {
			throw new RuntimeException("TODO");
		}

		/**
		 * Draws a rectangle, where (x,y) specifies the upper left corner. Requires
		 * 1.44f.
		 */
		public static void drawRect(final int x, final int y, final int width,
			final int height)
		{
			throw new RuntimeException("TODO");
		}

		/**
		 * Draws an ellipse, where (x,y) specifies the upper left corner of the
		 * bounding rectangle. Requires 1.44f.
		 */
		public static void drawEllipse(final int x, final int y, final int width,
			final int height)
		{
			throw new RuntimeException("TODO");
		}

		/**
		 * Draws text at the specified location and adds it to the overlay. Use
		 * setFont() to specify the font and setColor to set specify the color
		 * (example).
		 */
		public static void drawString(final String text, final int x, final int y) {
			throw new RuntimeException("TODO");
		}

		/** Displays the current overlay. */
		public static void show() {
			throw new RuntimeException("TODO");
		}

		/** Hides the current overlay. */
		public static void hide() {
			throw new RuntimeException("TODO");
		}

		/** Removes the current overlay. */
		public static void remove() {
			throw new RuntimeException("TODO");
		}

		/**
		 * Returns the size (selection count) of the current overlay. Returns zero
		 * if the image does not have an overlay.
		 */
		public static int size() {
			throw new RuntimeException("TODO");
		}

		/** Removes the specified selection from the overlay. */
		public static void removeSelection(final int index) {
			throw new RuntimeException("TODO");
		}

		/**
		 * Copies the overlay on the current image to the overlay clipboard.
		 * Requires 1.45e.
		 */
		public static void copy() {
			throw new RuntimeException("TODO");
		}

		/**
		 * Copies the overlay on the overlay clipboard to the current image.
		 * Requires 1.45e.
		 */
		public static void paste() {
			throw new RuntimeException("TODO");
		}

	}

	/**
	 * Returns the constant π (3.14159265), the ratio of the circumference to the
	 * diameter of a circle.
	 */
	// TODO: handle constants PI

	public static class Plot {

		/**
		 * Generates a plot using the specified title, axis labels and X and Y
		 * coordinate arrays. If only one array is specified it is assumed to
		 * contain the Y values and a 0..n-1 sequence is used as the X values. It is
		 * also permissible to specify no arrays and use Plot.setLimits() and
		 * Plot.add() to generate the plot. Use Plot.show() to display the plot in a
		 * window or it will be displayed automatically when the macro exits. For
		 * examples, check out the ExamplePlots macro file.
		 */
		public static void create(final String title, final String xAxisLabel,
			final String yAxisLabel, final double[] xValues, final double[] yValues)
		{
			throw new RuntimeException("TODO");
		}

		/**
		 * Sets the range of the x-axis and y-axis of plots created using create().
		 * Must be called immediately after create().
		 */
		public static void setLimits(final double xMin, final double xMax,
			final double yMin, final double yMax)
		{
			throw new RuntimeException("TODO");
		}

		/**
		 * Sets the plot frame size in pixels, overriding the default size defined
		 * in the Edit>Options>Profile Plot Options dialog box.
		 */
		public static void setFrameSize(final int width, final int height) {
			throw new RuntimeException("TODO");
		}

		/**
		 * Specifies the width of the line used to draw a curve. Points (circle,
		 * box, etc.) are also drawn larger if a line width greater than one is
		 * specified. Note that the curve specified in create() is the last one drawn
		 * before the plot is dispayed or updated.
		 */
		public static void setLineWidth(final int width) {
			throw new RuntimeException("TODO");
		}

		/**
		 * Specifies the color used in subsequent calls to add() or addText(). The
		 * argument can be "black", "blue", "cyan", "darkGray", "gray", "green",
		 * "lightGray", "magenta", "orange", "pink", "red", "white" or "yellow". Note
		 * that the curve specified in create() is drawn last.
		 */
		public static void setColor(final String name) {
			throw new RuntimeException("TODO");
		}

		/**
		 * Adds a curve, set of points or error bars to a plot created using
		 * create(). If only one array is specified it is assumed to contain the Y
		 * values and a 0..n-1 sequence is used as the X values. The first argument
		 * can be "line", "circles", "boxes", "triangles", "crosses", "dots", "x" or
		 * "error bars".
		 */
		public static void add(final String mode, final double[] xValues,
			final double[] yValues)
		{
			throw new RuntimeException("TODO");
		}

		/**
		 * Adds text to the plot at the specified location, where (0,0) is the upper
		 * left corner of the the plot frame and (1,1) is the lower right corner.
		 * Call setJustification() to have the text centered or right justified.
		 */
		public static void
			addText(final String text, final double x, final double y)
		{
			throw new RuntimeException("TODO");
		}

		/**
		 * Specifies the justification used by addText(). The argument can be
		 * "left", "right" or "center". The default is "left".
		 */
		public static void setJustification(final String align) {
			throw new RuntimeException("TODO");
		}

		/**
		 * Draws a line between x1,y1 and x2,y2, using the coordinate system defined
		 * by setLimits().
		 */
		public static void drawLine(final double x1, final double y1,
			final double x2, final double y2)
		{
			throw new RuntimeException("TODO");
		}

		/**
		 * Displays the plot generated by create(), add(), etc. in a window. This
		 * function is automatically called when a macro exits.
		 */
		public static void show() {
			throw new RuntimeException("TODO");
		}

		/**
		 * Draws the plot generated by create(), add(), etc. in an existing plot
		 * window. Equivalent to show() if no plot window is open.
		 */
		public static void update() {
			throw new RuntimeException("TODO");
		}

		/**
		 * Returns the values displayed by clicking on "List" in a plot or histogram
		 * window (example).
		 */
		public static void
			getValues(final Variable xpoints, final Variable ypoints)
		{
			throw new RuntimeException("TODO");
		}

	}

	/**
	 * These functions allow you to get and set the position (channel, slice and
	 * frame) of a hyperstack (a 4D or 5D stack). The HyperStackDemo demonstrates
	 * how to create a hyperstack and how to work with it using these functions
	 */
	public static class Stack {

		/** Returns true if the current image is a hyperstack. */
		public static void isHyperstack() {
			throw new RuntimeException("TODO");
		}

		/** Returns the dimensions of the current image. */
		public static void getDimensions(final Variable width,
			final Variable height, final Variable channels, final Variable slices,
			final Variable frames)
		{
			throw new RuntimeException("TODO");
		}

		/** Sets the 3rd, 4th and 5th dimensions of the current stack. */
		public static void setDimensions(final int channels, final int slices,
			final int frames)
		{
			throw new RuntimeException("TODO");
		}

		/** Displays channel n . */
		public static void setChannel(final int n) {
			throw new RuntimeException("TODO");
		}

		/** Displays slice n . */
		public static void setSlice(final int n) {
			throw new RuntimeException("TODO");
		}

		/** Displays frame n . */
		public static void setFrame(final int n) {
			throw new RuntimeException("TODO");
		}

		/** Returns the current position. */
		public static void getPosition(final int channel, final int slice,
			final int frame)
		{
			throw new RuntimeException("TODO");
		}

		/** Sets the position. */
		public static void setPosition(final int channel, final int slice,
			final int frame)
		{
			throw new RuntimeException("TODO");
		}

		/** Returns the frame rate (FPS). */
		public static void getFrameRate() {
			throw new RuntimeException("TODO");
		}

		/** Sets the frame rate. */
		public static void setFrameRate(final double fps) {
			throw new RuntimeException("TODO");
		}

		/** Returns the frame interval in time (T) units. Requires v1.45h. */
		public static void getFrameInterval() {
			throw new RuntimeException("TODO");
		}

		/** Sets the frame interval in time (T) units. Requires v1.45h. */
		public static void setFrameInterval(final double interval) {
			throw new RuntimeException("TODO");
		}

		/** Returns the x, y, z, time and value units. Requires v1.45h. */
		public static void getUnits(final Variable x, final Variable y,
			final Variable z, final Variable time, final Variable value)
		{
			throw new RuntimeException("TODO");
		}

		/** Sets the time unit. */
		public static void setTUnit(final String string) {
			throw new RuntimeException("TODO");
		}

		/** Sets the Z-dimension unit. */
		public static void setZUnit(final String string) {
			throw new RuntimeException("TODO");
		}

		/**
		 * Sets the display mode, where mode is "composite", "color" or "grayscale".
		 * Requires a multi-channel stack and v1.40a or later.
		 */
		public static void setDisplayMode(final String mode) {
			throw new RuntimeException("TODO");
		}

		/** Gets the current display mode. */
		public static void getDisplayMode(final Variable mode) {
			throw new RuntimeException("TODO");
		}

		/**
		 * Sets the active channels in a composite color image, where string is a
		 * list of ones and zeros that specify the channels to activate. For
		 * example, "101" activates channels 1 and 3.
		 */
		public static void setActiveChannels(final String string) {
			throw new RuntimeException("TODO");
		}

		/**
		 * Returns a string that represents the state of the channels in a composite
		 * color image, where '1' indicates an active channel and '0' indicates an
		 * inactive channel. Requires v1.43d.
		 */
		public static void getActiveChannels(final Variable array) {
			throw new RuntimeException("TODO");
		}

		/**
		 * Swaps the two specified stack images, where n1 and n2 are integers
		 * greater than 0 and less than or equal to nSlices.
		 */
		public static void swap(final int n1, final int n2) {
			throw new RuntimeException("TODO");
		}

		/** Calculates and returns stack statistics. */
		public static void getStatistics(final Variable voxelCount,
			final Variable mean, final Variable min, final Variable max,
			final Variable stdDev)
		{
			throw new RuntimeException("TODO");
		}

	}

	/**
	 * These functions do string buffering and copy strings to and from the system
	 * clipboard. The CopyResultsToClipboard macro demonstrates their use. See
	 * also: endsWith, indexOf, lastIndexOf, lengthOf, startsWith and substring.
	 */
// TODO: treat this specially, we cannot use the class name String here without wreaking havoc
	public static class String_ {

		/** Resets (clears) the buffer. */
		public static void resetBuffer() {
			throw new RuntimeException("TODO");
		}

		/** Appends str to the buffer. */
		public static void append(final String str) {
			throw new RuntimeException("TODO");
		}

		/** Returns the contents of the buffer. */
		public static String buffer() {
			throw new RuntimeException("TODO");
		}

		/** Copies str to the clipboard. */
		public static void copy(final String str) {
			throw new RuntimeException("TODO");
		}

		/** Copies the Results table to the clipboard. */
		public static void copyResults() {
			throw new RuntimeException("TODO");
		}

		/** Returns the contents of the clipboard. */
		public static String paste() {
			throw new RuntimeException("TODO");
		}

	}

	/**
	 * This class contains helper functions to accomodate for the stateless nature
	 * of the macro functions
	 */
	public static class IJ2 {

		public static Dataset getCurrentImage() {
			final Dataset result =
				ImageJ.get(ImageDisplayService.class).getActiveDataset();
			if (result == null) {
				throw new MacroException("No current image!");
			}
			return result;
		}

		public static TextDisplay getLog() {
			final DisplayService displayService = ImageJ.get(DisplayService.class);
			final Display<?> logDisplay = displayService.getDisplay("Log");
			if (logDisplay != null || logDisplay instanceof TextDisplay) {
				return (TextDisplay) logDisplay;
			}
			return (TextDisplay) displayService.createDisplay("Log", "");
		}
	}

}
