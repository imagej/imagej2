/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ijx;

import ij.*;
import ijx.gui.IjxImageCanvas;
import ijx.gui.IjxImageWindow;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.io.FileInfo;
import ij.measure.Calibration;
import ij.measure.Measurements;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import java.awt.Color;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.Properties;

/**
 *
 * @author GBH
 */
public interface IjxImagePlus extends ImageObserver, Measurements {
    /**
     * 8-bit indexed color
     */
    int COLOR_256 = 3;
    /**
     * 32-bit RGB color
     */
    int COLOR_RGB = 4;
    /**
     * 16-bit grayscale (unsigned)
     */
    int GRAY16 = 1;
    /**
     * 32-bit floating-point grayscale
     */
    int GRAY32 = 2;
    /**
     * 8-bit grayscale (unsigned)
     */
    int GRAY8 = 0;

    Object clone();

    /**
     * Closes this image and sets the pixel arrays to null. To avoid the
     * "Save changes?" dialog, first set the public 'changes' variable to false.
     */
    void close();

    void setChanged(boolean t);
    
    boolean wasChanged();
    /**
     * Copies the contents of the current selection to the internal clipboard.
     * Copies the entire image if there is no selection. Also clears
     * the selection if <code>cut</code> is true.
     */
    void copy(boolean cut);

    /**
     * Copies the calibration of the specified image to this image.
     */
    void copyScale(IjxImagePlus imp);

    /**
     * Returns an empty image stack that has the same
     * width, height and color table as this image.
     */
    IjxImageStack createEmptyStack();

    /**
     * Returns a new ImagePlus with this ImagePlus' attributes
     * (e.g. spatial scale), but no image.
     */
    IjxImagePlus createImagePlus();

    /**
     * Creates a LookUpTable object corresponding to this image.
     */
    LookUpTable createLut();

    /**
     * Starts the process of creating a new selection, where sx and sy are the
     * starting screen coordinates. The selection type is determined by which tool in
     * the tool bar is active. The user interactively sets the selection size and shape.
     */
    void createNewRoi(int sx, int sy);

    /**
     * Draws the image. If there is an ROI, its
     * outline is also displayed.  Does nothing if there
     * is no window associated with this image (i.e. show()
     * has not been called).
     */
    void draw();

    /**
     * Draws image and roi outline using a clip rect.
     */
    void draw(int x, int y, int width, int height);

    /**
     * Sets the image arrays to null to help the garbage collector
     * do its job. Does nothing if the image is locked or a
     * setIgnoreFlush(true) call has been made.
     */
    void flush();

    /**
     * Returns the bit depth, 8, 16, 24 (RGB) or 32. RGB images actually use 32 bits per pixel.
     */
    int getBitDepth();

    /**
     * Returns this image as a BufferedImage.
     */
    BufferedImage getBufferedImage();

    /**
     * Returns the number of bytes per pixel.
     */
    int getBytesPerPixel();

    /**
     * Returns this image's calibration.
     */
    Calibration getCalibration();

    /**
     * Returns the ImageCanvas being used to display this image, or null.
     */
    IjxImageCanvas getCanvas();

    int getChannel();

    /**
     * Returns a reference to the current ImageProcessor. The
     * CompositeImage class overrides this method so it returns
     * the processor associated with the current channel.
     */
    ImageProcessor getChannelProcessor();

    /**
     * Returns the current stack slice number or 1 if
     * this is a single image.
     */
    int getCurrentSlice();

    /**
     * Returns the dimensions of this image (width, height, nChannels,
     * nSlices, nFrames) as a 5 element int array.
     */
    int[] getDimensions();

    double getDisplayRangeMax();

    double getDisplayRangeMin();

    /**
     * Returns a FileInfo object containing information, including the
     * pixel array, needed to save this image. Use getOriginalFileInfo()
     * to get a copy of the FileInfo object used to open the image.
     * @see ij.io.FileInfo
     * @see #getOriginalFileInfo
     * @see #setFileInfo
     */
    FileInfo getFileInfo();

    int getFrame();

    /**
     * Returns the system-wide calibration, or null.
     */
    Calibration getGlobalCalibration();

    int getHeight();

    /**
     * Returns this image's unique numeric ID.
     */
    int getID();

    /**
     * Returns this image as a AWT image.
     */
    Image getImage();

    /**
     * Returns the base image stack.
     */
    IjxImageStack getImageStack();

    /**
     * If this is a stack, returns the actual number of images in the stack, else returns 1.
     */
    int getImageStackSize();

    /**
     * Returns this image's local calibration, ignoring
     * the "Global" calibration flag.
     */
    Calibration getLocalCalibration();

    /**
     * Converts the current cursor location to a string.
     */
    String getLocationAsString(int x, int y);

    /**
     * For images with irregular ROIs, returns a byte mask, otherwise, returns
     * null. Mask pixels have a non-zero value.
     */
    ImageProcessor getMask();

    /**
     * Returns the number of channels.
     */
    int getNChannels();

    /**
     * Returns the number of frames (time-points).
     */
    int getNFrames();

    /**
     * Returns the image depth (number of z-slices).
     */
    int getNSlices();

    boolean getOpenAsHyperStack();

    /**
     * Returns the FileInfo object that was used to open this image.
     * Returns null for images created using the File/New command.
     * @see ij.io.FileInfo
     * @see #getFileInfo
     */
    FileInfo getOriginalFileInfo();

    /**
     * Returns the pixel value at (x,y) as a 4 element array. Grayscale values
     * are retuned in the first element. RGB values are returned in the first
     * 3 elements. For indexed color images, the RGB values are returned in the
     * first 3 three elements and the index (0-255) is returned in the last.
     */
    int[] getPixel(int x, int y);

    /**
     * Returns a reference to the current ImageProcessor. If there
     * is no ImageProcessor, it creates one. Returns null if this
     * ImagePlus contains no ImageProcessor and no AWT Image.
     */
    ImageProcessor getProcessor();

    /**
     * Returns this image's Properties. May return null.
     */
    Properties getProperties();

    /**
     * Returns the property associated with 'key'. May return null.
     */
    Object getProperty(String key);

    Roi getRoi();

    /**
     * Returns a shortened version of image name that does not
     * include spaces or a file name extension.
     */
    String getShortTitle();

    int getSlice();

    /**
     * Returns the image stack. The stack may have only
     * one slice. After adding or removing slices, call
     * <code>setStack()</code> to update the image and
     * the window that is displaying it.
     * @see #setStack
     */
    IjxImageStack getStack();

    int getStackIndex(int channel, int slice, int frame);

    /**
     * If this is a stack, returns the number of slices, else returns 1.
     */
    int getStackSize();

    /**
     * Returns the time in milliseconds when
     * startTiming() was last called.
     */
    long getStartTime();

    /**
     * Returns an ImageStatistics object generated using the standard
     * measurement options (area, mean, mode, min and max).
     */
    ImageStatistics getStatistics();

    /**
     * Returns an ImageStatistics object generated using the
     * specified measurement options.
     */
    ImageStatistics getStatistics(int mOptions);

    /**
     * Returns an ImageStatistics object generated using the
     * specified measurement options and histogram bin count.
     * Note: except for float images, the number of bins
     * is currently fixed at 256.
     */
    ImageStatistics getStatistics(int mOptions, int nBins);

    /**
     * Returns an ImageStatistics object generated using the
     * specified measurement options, histogram bin count and histogram range.
     * Note: for 8-bit and RGB images, the number of bins
     * is fixed at 256 and the histogram range is always 0-255.
     */
    ImageStatistics getStatistics(int mOptions, int nBins, double histMin, double histMax);

    /**
     * Returns the image name.
     */
    String getTitle();

    /**
     * Returns the current image type (ImagePlus.GRAY8, ImagePlus.GRAY16,
     * ImagePlus.GRAY32, ImagePlus.COLOR_256 or ImagePlus.COLOR_RGB).
     * @see #getBitDepth
     */
    int getType();
    
    void setType(int type);

    int getWidth();

    /**
     * Returns the ImageWindow that is being used to display
     * this image. Returns null if show() has not be called
     * or the ImageWindow has been closed.
     */
    IjxImageWindow getWindow();

    /**
     * Closes the window, if any, that is displaying this image.
     */
    void hide();

    /**
     * Used by ImagePlus to monitor loading of images.
     */
    boolean imageUpdate(Image img, int flags, int x, int y, int w, int h);

    /**
     * Returns true if this is a CompositeImage.
     */
    boolean isComposite();

    boolean isHyperStack();

    /**
     * Returns true is this image uses an inverting LUT that
     * displays zero as white and 255 as black.
     */
    boolean isInvertedLut();

    /**
     * Returns 'true' if the image is locked.
     */
    boolean isLocked();

    boolean isProcessor();

    /**
     * Returns true if this image is currently being displayed in a window.
     */
    boolean isVisible();

    /**
     * Obsolete.
     */
    void killProcessor();

    /**
     * Deletes the current region of interest. Makes a copy
     * of the current ROI so it can be recovered by the
     * Edit/Restore Selection command.
     */
    void killRoi();

    void killStack();

    /**
     * Locks the image so other threads can test to see if it
     * is in use. Returns true if the image was successfully locked.
     * Beeps, displays a message in the status bar, and returns
     * false if the image is already locked.
     */
    boolean lock();

    /**
     * Similar to lock, but doesn't beep and display an error
     * message if the attempt to lock the image fails.
     */
    boolean lockSilently();

    /**
     * Displays the cursor coordinates and pixel value in the status bar.
     * Called by ImageCanvas when the mouse moves. Can be overridden by
     * ImagePlus subclasses.
     */
    void mouseMoved(int x, int y);

    /**
     * Inserts the contents of the internal clipboard into the active image. If there
     * is a selection the same size as the image on the clipboard, the image is inserted
     * into that selection, otherwise the selection is inserted into the center of the image.
     */
    void paste();

    /**
     * Calls draw to draw the image and also repaints the
     * image window to force the information displayed above
     * the image (dimension, type, size) to be updated.
     */
    void repaintWindow();

    void resetDisplayRange();

    void resetStack();

    void restoreRoi();

    /**
     * Implements the File/Revert command.
     */
    void revert();

    void saveRoi();

    /**
     * Called by ImageWindow.windowActivated().
     */
    void setActivated();

    /**
     * Sets this image's calibration.
     */
    void setCalibration(Calibration cal);

    /**
     * Sets current foreground color.
     */
    void setColor(Color c);

    /**
     * Sets the 3rd, 4th and 5th dimensions, where
     * <code>nChannels</code>*<code>nSlices</code>*<code>nFrames</code>
     * must be equal to the stack size.
     */
    void setDimensions(int nChannels, int nSlices, int nFrames);

    void setDisplayRange(double min, double max);

    void setDisplayRange(double min, double max, int channels);

    /**
     * Saves this image's FileInfo so it can be later
     * retieved using getOriginalFileInfo().
     */
    void setFileInfo(FileInfo fi);

    /**
     * Sets the system-wide calibration.
     */
    void setGlobalCalibration(Calibration global);

    /**
     * Set <code>ignoreFlush true</code> to not have the pixel
     * data set to null when the window is closed.
     */
    void setIgnoreFlush(boolean ignoreFlush);

    /**
     * Replaces the image, if any, with the one specified.
     * Throws an IllegalStateException if an error occurs
     * while loading the image.
     */
    void setImage(Image img);

    void setOpenAsHyperStack(boolean openAsHyperStack);

    void setPosition(int channel, int slice, int frame);

    void setPosition(int n);

    void setPositionWithoutUpdate(int channel, int slice, int frame);

    /**
     * Replaces the ImageProcessor, if any, with the one specified.
     * Set 'title' to null to leave the image title unchanged.
     */
    void setProcessor(String title, ImageProcessor ip);

    /**
     * Adds a key-value pair to this image's properties. The key
     * is removed from the properties table if value is null.
     */
    void setProperty(String key, Object value);

    /**
     * Assigns the specified ROI to this image and displays it. Any existing
     * ROI is deleted if <code>roi</code> is null or its width or height is zero.
     * Sets the ImageProcessor mask to null.
     */
    void setRoi(Roi newRoi);

    /**
     * Creates a rectangular selection.
     */
    void setRoi(int x, int y, int width, int height);

    /**
     * Creates a rectangular selection.
     */
    void setRoi(Rectangle r);

    /**
     * Activates the specified slice. The index must be >= 1
     * and <= N, where N in the number of slices in the stack.
     * Does nothing if this ImagePlus does not use a stack.
     */
    void setSlice(int index);

    /**
     * Replaces the stack, if any, with the one specified.
     * Set 'title' to null to leave the title unchanged.
     */
    void setStack(String title, IjxImageStack stack);

    /**
     * Sets the image name.
     */
    void setTitle(String title);

    /**
     * This method should only be called from an ImageWindow.
     */
    void setWindow(IjxImageWindow win);

    /**
     * Opens a window to display this image and clears the status bar.
     */
    void show();

    /**
     * Opens a window to display this image and displays
     * 'statusMessage' in the status bar.
     */
    void show(String statusMessage);

    /**
     * Calls System.currentTimeMillis() to save the current
     * time so it can be retrieved later using getStartTime()
     * to calculate the elapsed time of an operation.
     */
    void startTiming();

    String toString();

    /**
     * Frees RAM by setting the snapshot (undo) buffer in
     * the current ImageProcessor to null.
     */
    void trimProcessor();

    /**
     * Unlocks the image.
     */
    void unlock();

    /**
     * Updates this image from the pixel data in its
     * associated ImageProcessor, then displays it. Does
     * nothing if there is no window associated with
     * this image (i.e. show() has not been called).
     */
    void updateAndDraw();

    /**
     * Calls updateAndDraw to update from the pixel data
     * and draw the image, and also repaints the image
     * window to force the information displayed above
     * the image (dimension, type, size) to be updated.
     */
    void updateAndRepaintWindow();

    /**
     * Updates this image from the pixel data in its
     * associated ImageProcessor, then displays it.
     * The CompositeImage class overrides this method
     * to only update the current channel.
     */
    void updateChannelAndDraw();

    /**
     * ImageCanvas.paint() calls this method when the
     * ImageProcessor has generated new image.
     */
    void updateImage();

    void updatePosition(int c, int z, int t);

    /**
     * Redisplays the (x,y) coordinates and pixel value (which may
     * have changed) in the status bar. Called by the Next Slice and
     * Previous Slice commands to update the z-coordinate and pixel value.
     */
    void updateStatusbarValue();

}
