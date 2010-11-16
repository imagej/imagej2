package ijx;

import ij.LookUpTable;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.io.FileInfo;
import ij.measure.Calibration;
import ij.measure.Measurements;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.process.LUT;
import ijx.gui.IjxImageCanvas;
import ijx.gui.IjxImageWindow;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.Properties;
import java.util.Vector;

/**
 *  IjxImagePlus - interface for IjxImagePlus's ...
 * @author GBH
 */
public interface IjxImagePlus extends ImageObserver, Measurements {

// <editor-fold defaultstate="collapsed" desc=" IMAGETYPES ">
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

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Attributes">
    /**
     * Returns the current image type (IjxImagePlus.GRAY8, IjxImagePlus.GRAY16,
     * IjxImagePlus.GRAY32, IjxImagePlus.COLOR_256 or IjxImagePlus.COLOR_RGB).
     * @see #getBitDepth
     */
    int getType();

    void setType(int type);

    /**
     * Returns the bit depth, 8, 16, 24 (RGB) or 32. RGB images actually use 32 bits per pixel.
     */
    int getBitDepth();

    /**
     * Returns the number of bytes per pixel.
     */
    int getBytesPerPixel();

    /**
     * Returns the dimensions of this image (width, height, nChannels,
     * nSlices, nFrames) as a 5 element int array.
     */
    int[] getDimensions();

    int getWidth();

    int getHeight();

    /**
     * Returns this image's unique numeric ID.
     */
    int getID();

    /**
     * Returns a shortened version of image name that does not
     * include spaces or a file name extension.
     */
    String getShortTitle();

    /**
     * Returns the image name.
     */
    String getTitle();

    /**
     * Sets the image name.
     */
    void setTitle(String title);

// </editor-fold>
    /**
     * Returns a new IjxImagePlus with this image's attributes
     * (e.g. spatial scale), but no image.
     */
    IjxImagePlus createImagePlus();

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
     * Used by IjxImagePlus to monitor loading of images.
     */
    boolean imageUpdate(Image img, int flags, int x, int y, int w, int h);

    /**
     * Replaces the image, if any, with the one specified.
     * Throws an IllegalStateException if an error occurs
     * while loading the image.
     */
    void setImage(Image img);

    /**
     * Returns this image as a AWT image.
     */
    Image getImage();

    /**
     * Returns this image as a BufferedImage.
     */
    BufferedImage getBufferedImage();

    /**
     * For images with irregular ROIs, returns a byte mask, otherwise, returns
     * null. Mask pixels have a non-zero value.
     */
    ImageProcessor getMask();

    /**
     * Returns the pixel value at (x,y) as a 4 element array. Grayscale values
     * are retuned in the first element. RGB values are returned in the first
     * 3 elements. For indexed color images, the RGB values are returned in the
     * first 3 three elements and the index (0-255) is returned in the last.
     */
    int[] getPixel(int x, int y);

    boolean isChanged();

    void setChanged(boolean t);

    /**
     * Called by IjxImageWindow.windowActivated().
     */
    void setActivated();

    /**
     * Returns true if this image is currently being displayed in a window.
     */
    boolean isVisible();

    /**
     * Sets current foreground color.
     */
    void setColor(Color c);

    /**
     * Calls draw to draw the image and also repaints the
     * image window to force the information displayed above
     * the image (dimension, type, size) to be updated.
     */
    void repaintWindow();

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
     * Returns the time in milliseconds when
     * startTiming() was last called.
     */
    long getStartTime();

    /**
     * Calls System.currentTimeMillis() to save the current
     * time so it can be retrieved later using getStartTime()
     * to calculate the elapsed time of an operation.
     */
    void startTiming();

// <editor-fold defaultstate="collapsed" desc=" Processor">
    /**
     * Returns a reference to the current ImageProcessor. If there
     * is no ImageProcessor, it creates one. Returns null if this
     * IjxImagePlus contains no ImageProcessor and no AWT Image.
     */
    ImageProcessor getProcessor();

    boolean isProcessor();

    /**
     * Replaces the ImageProcessor with the one specified and updates the display.
     */
    void setProcessor(ImageProcessor ip);

    /**
     * Replaces the ImageProcessor with the one specified and updates the display.
     * Set 'title' to null to leave the image title unchanged.
     */
    void setProcessor(String title, ImageProcessor ip);

    void killStack();

    /**
     * Frees RAM by setting the snapshot (undo) buffer in
     * the current ImageProcessor to null.
     */
    void trimProcessor();

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Mouse/Cursor ">
    /**
     * Displays the cursor coordinates and pixel value in the status bar.
     * Called by ImageCanvas when the mouse moves. Can be overridden by
     * IjxImagePlus subclasses.
     */
    void mouseMoved(int x, int y);

    /**
     * Converts the current cursor location to a string.
     */
    String getLocationAsString(int x, int y);

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" LUT">
    /**
     * Creates a LookUpTable object that corresponds to this image.
     */
    LookUpTable createLut();

    LUT[] getLuts();

    /**
     * Returns true is this image uses an inverting LUT that
     * displays zero as white and 255 as black.
     */
    boolean isInvertedLut();

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Calibration ">
    /**
     * Returns this image's calibration.
     */
    Calibration getCalibration();

    /**
     * Returns this image's local calibration, ignoring
     * the "Global" calibration flag.
     */
    Calibration getLocalCalibration();

    /**
     * Returns the system-wide calibration, or null.
     */
    Calibration getGlobalCalibration();

    /**
     * Sets this image's calibration.
     */
    void setCalibration(Calibration cal);

    /**
     * Sets the system-wide calibration.
     */
    void setGlobalCalibration(Calibration global);

    /**
     * Copies the calibration of the specified image to this image.
     */
    void copyScale(IjxImagePlus imp);

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Statistics">
    /**
     * Returns an ImageStatistics object generated using the standard
     * measurement options (area, mean, mode, min and max).
     * This plugin demonstrates how get the area, mean and max of the
     * current image or selection:
     * <pre>
     * public class Get_Statistics implements PlugIn {
     * public void run(String arg) {
     * IjxImagePlus imp = IJ.getImage();
     * ImageStatistics stats = imp.getStatistics();
     * IJ.log("Area: "+stats.area);
     * IJ.log("Mean: "+stats.mean);
     * IJ.log("Max: "+stats.max);
     * }
     * }
     * </pre>
     * @see ij.process.ImageStatistics
     * @see ij.process.ImageStatistics#getStatistics
     */
    ImageStatistics getStatistics();

    /**
     * Returns an ImageStatistics object generated using the
     * specified measurement options. This plugin demonstrates how
     * get the area and centroid of the current selection:
     * <pre>
     * public class Get_Statistics implements PlugIn, Measurements {
     * public void run(String arg) {
     * IjxImagePlus imp = IJ.getImage();
     * ImageStatistics stats = imp.getStatistics(MEDIAN+CENTROID);
     * IJ.log("Median: "+stats.median);
     * IJ.log("xCentroid: "+stats.xCentroid);
     * IJ.log("yCentroid: "+stats.yCentroid);
     * }
     * }
     * </pre>
     * @see ij.process.ImageStatistics
     * @see ij.measure.Measurements
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

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" ROI ">
    /**
     * Starts the process of creating a new selection, where sx and sy are the
     * starting screen coordinates. The selection type is determined by which tool in
     * the tool bar is active. The user interactively sets the selection size and shape.
     */
    void createNewRoi(int sx, int sy);

    /**
     * Returns the current selection, or null if there is no selection.
     */
    Roi getRoi();

    /**
     * Deletes the current region of interest. Makes a copy
     * of the current ROI so it can be recovered by the
     * Edit/Restore Selection command.
     */
    void killRoi();

    void saveRoi();

    /**
     * Assigns the specified ROI to this image and displays it. Any existing
     * ROI is deleted if <code>roi</code> is null or its width or height is zero.
     */
    void setRoi(Roi newRoi);

    /**
     * Assigns 'newRoi'  to this image and displays it if 'updateDisplay' is true.
     */
    void setRoi(Roi newRoi, boolean updateDisplay);

    /**
     * Creates a rectangular selection.
     */
    void setRoi(int x, int y, int width, int height);

    /**
     * Creates a rectangular selection.
     */
    void setRoi(Rectangle r);

    void restoreRoi();

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Stack/Slice ">
    /**
     * Returns an empty image stack that has the same
     * width, height and color table as this image.
     */
    IjxImageStack createEmptyStack();

    /**
     * Returns the current stack slice number or 1 if
     * this is a single image.
     */
    int getCurrentSlice();

    /**
     * Returns the base image stack.
     */
    IjxImageStack getImageStack();

    /**
     * If this is a stack, returns the actual number of images in the stack, else returns 1.
     */
    int getImageStackSize();

    /**
     * Returns the image stack. The stack may have only
     * one slice. After adding or removing slices, call
     * <code>setStack()</code> to update the image and
     * the window that is displaying it.
     * @see #setStack
     */
    IjxImageStack getStack();

    void resetStack();

    /**
     * Returns that stack index (one-based) corresponding to the specified position.
     */
    int getStackIndex(int channel, int slice, int frame);

    /**
     * If this is a stack, returns the number of slices, else returns 1.
     */
    int getStackSize();

    /**
     * Displays the specified stack image, where 1<=n<=stackSize.
     * Does nothing if this image is not a stack.
     */
    void setSlice(int n);

    int getSlice();

    /**
     * Displays the specified stack image (1<=n<=stackSize)
     * without updating the display.
     */
    void setSliceWithoutUpdate(int n);

    /**
     * Replaces the image with the specified stack and updates the display.
     */
    void setStack(IjxImageStack stack);

    /**
     * Replaces the image with the specified stack and updates
     * the display. Set 'title' to null to leave the title unchanged.
     */
    void setStack(String title, IjxImageStack newStack);

    void setStack(IjxImageStack stack, int nChannels, int nSlices, int nFrames);

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Composite ">
    /**
     * Returns true if this is a CompositeImage.
     */
    boolean isComposite();

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Hyperstack ">
    /**
     * Returns a new hyperstack with this image's attributes
     * (e.g., width, height, spatial scale), but no image data.
     */
    IjxImagePlus createHyperStack(String title, int channels, int slices, int frames, int bitDepth);

    boolean getOpenAsHyperStack();

    /**
     * Returns 'true' if this is a hyperstack currently being displayed in a IjxStackWindow.
     */
    boolean isDisplayedHyperStack();

    /**
     * Returns 'true' if this image is a hyperstack.
     */
    boolean isHyperStack();

    void setOpenAsHyperStack(boolean openAsHyperStack);

    int getChannel();

    /**
     * Returns a reference to the current ImageProcessor. The
     * CompositeImage class overrides this method so it returns
     * the processor associated with the current channel.
     */
    ImageProcessor getChannelProcessor();

    /**
     * Returns the number of channels.
     */
    int getNChannels();

    /**
     * Sets the 3rd, 4th and 5th dimensions, where
     * <code>nChannels</code>*<code>nSlices</code>*<code>nFrames</code>
     * must be equal to the stack size.
     */
    void setDimensions(int nChannels, int nSlices, int nFrames);

    /**
     * Returns the number of dimensions (2, 3, 4 or 5).
     */
    int getNDimensions();

    /**
     * Returns the number of frames (time-points).
     */
    int getNFrames();

    /**
     * Returns the image depth (number of z-slices).
     */
    int getNSlices();

    /**
     * Converts the stack index 'n' (one-based) into a hyperstack position (channel, slice, frame).
     */
    int[] convertIndexToPosition(int n);

    /**
     * Sets the current hyperstack position and updates the display,
     * where 'channel', 'slice' and 'frame' are one-based indexes.
     */
    void setPosition(int channel, int slice, int frame);

    /**
     * Set the current hyperstack position based on the stack index 'n' (one-based).
     */
    void setPosition(int n);

    /**
     * Sets the current hyperstack position without updating the display,
     * where 'channel', 'slice' and 'frame' are one-based indexes.
     */
    void setPositionWithoutUpdate(int channel, int slice, int frame);

// </editor-fold>
    // <editor-fold defaultstate="collapsed" desc=" DisplayRange ">
    double getDisplayRangeMax();

    double getDisplayRangeMin();

    /**
     * Sets the display range of the current channel. With non-composite
     * images it is identical to ip.setMinAndMax(min, max).
     */
    void setDisplayRange(double min, double max);

    /**
     * Sets the display range of specified channels in an RGB image, where 4=red,
     * 2=green, 1=blue, 6=red+green, etc. With non-RGB images, this method is
     * identical to setDisplayRange(min, max).  This method is used by the
     * Image/Adjust/Color Balance tool .
     */
    void setDisplayRange(double min, double max, int channels);

    void resetDisplayRange();

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Overlay/Flatten">

    /* Returns the current overly, or null if this image does not have an overlay. */
    Overlay getOverlay();

    /**
     * Installs a list of ROIs that will be drawn on this image as a non-destructive overlay.
     * @see ij.gui.Roi#setStrokeColor
     * @see ij.gui.Roi#setStrokeWidth
     * @see ij.gui.Roi#setFillColor
     * @see ij.gui.Roi#setLocation
     * @see ij.gui.Roi#setNonScalable
     */
    void setOverlay(Overlay overlay);

    /**
     * Creates an Overlay from the specified Shape, Color
     * and BasicStroke, and assigns it to this image.
     * @see #setOverlay(ij.gui.Overlay)
     * @see ij.gui.Roi#setStrokeColor
     * @see ij.gui.Roi#setStrokeWidth
     */
    void setOverlay(Shape shape, Color color, BasicStroke stroke);

    /**
     * Creates an Overlay from the specified ROI, and assigns it to this image.
     * @see #setOverlay(ij.gui.Overlay)
     */
    void setOverlay(Roi roi, Color strokeColor, int strokeWidth, Color fillColor);

    boolean getHideOverlay();

    void setHideOverlay(boolean hide);

    void setFlatteningCanvas(IjxImageCanvas flatteningCanvas);

    IjxImageCanvas getFlatteningCanvas();

    /**
     * Returns a "flattened" version of this image, in RGB format.
     */
    IjxImagePlus flatten();

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Update">
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
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" FileInfo ">

    /**
     * Returns a FileInfo object containing information, including the
     * pixel array, needed to save this image. Use getOriginalFileInfo()
     * to get a copy of the FileInfo object used to open the image.
     * @see ij.io.FileInfo
     * @see #getOriginalFileInfo
     * @see #setFileInfo
     */
    FileInfo getFileInfo();

    /**
     * Saves this image's FileInfo so it can be later
     * retieved using getOriginalFileInfo().
     */
    void setFileInfo(FileInfo fi);

    /**
     * Implements the File/Revert command.
     */
    void revert();

    /**
     * Returns the FileInfo object that was used to open this image.
     * Returns null for images created using the File/New command.
     * @see ij.io.FileInfo
     * @see #getFileInfo
     */
    FileInfo getOriginalFileInfo();

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" UI Components">
    /**
     * Returns the ImageCanvas being used to
     * display this image, or null.
     */
    IjxImageCanvas getCanvas();

    /**
     * Returns the IjxImageWindow that is being used to display
     * this image. Returns null if show() has not be called
     * or the IjxImageWindow has been closed.
     */
    int getFrame();

    IjxImageWindow getWindow();

    /**
     * This method should only be called from an IjxImageWindow.
     */
    void setWindow(IjxImageWindow win);

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Locking">
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
     * Returns 'true' if the image is locked.
     */
    boolean isLocked();

    /**
     * Unlocks the image.
     */
    void unlock();

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Properties, custom ">
    /**
     * Returns this image's Properties. May return null.
     */
    Properties getProperties();

    /**
     * Returns the property associated with 'key'. May return null.
     */
    Object getProperty(String key);

    /**
     * Adds a key-value pair to this image's properties. The key
     * is removed from the properties table if value is null.
     */
    void setProperty(String key, Object value);

// </editor-fold>
    // <editor-fold defaultstate="collapsed" desc=" Clipboard">
    /**
     * Copies the contents of the current selection to the internal clipboard.
     * Copies the entire image if there is no selection. Also clears
     * the selection if <code>cut</code> is true.
     */
    void copy(boolean cut);

    /**
     * Inserts the contents of the internal clipboard into the active image. If there
     * is a selection the same size as the image on the clipboard, the image is inserted
     * into that selection, otherwise the selection is inserted into the center of the image.
     */
    void paste();

// </editor-fold>
    /**
     * Closes the window, if any, that is displaying this image.
     */
    void hide();

    /**
     * Sets the ImageProcessor, Roi, AWT Image and stack image
     * arrays to null. Does nothing if the image is locked.
     */
    void flush();

    void setIgnoreFlush(boolean ignoreFlush);

    /**
     * Closes this image and sets the ImageProcessor to null. To avoid the
     * "Save changes?" dialog, first set the public 'changes' variable to false.
     */
    void close();

    String toString();

    /* @deprecated Use setOverlay() instead */
    void setDisplayList(Vector list);

    /* @deprected Use setOverlay() instead */
    void setDisplayList(Roi roi, Color strokeColor, int strokeWidth, Color fillColor);

    /* @deprecated Use getOverlay() instead */
    Vector getDisplayList();
}
