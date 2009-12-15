package ijx;

import ij.process.ImageProcessor;
import java.awt.Rectangle;
import java.awt.image.ColorModel;

/**
 *
 * @author GBH
 */
public interface IjxImageStack {

    /**
     * Adds an image in the forma of a pixel array to the end of the stack.
     */
    void addSlice(String sliceLabel, Object pixels);

    /**
     * Adds the image in 'ip' to the end of the stack.
     */
    void addSlice(String sliceLabel, ImageProcessor ip);

    /**
     * Adds the image in 'ip' to the stack following slice 'n'. Adds
     * the slice to the beginning of the stack if 'n' is zero.
     */
    void addSlice(String sliceLabel, ImageProcessor ip, int n);

    /**
     * Obsolete. Short images are always unsigned.
     */
    void addUnsignedShortSlice(String sliceLabel, Object pixels);

    /**
     * Deletes the last slice in the stack.
     */
    void deleteLastSlice();

    /**
     * Deletes the specified slice, were 1<=n<=nslices.
     */
    void deleteSlice(int n);

    /**
     * Returns this stack's color model. May return null.
     */
    ColorModel getColorModel();

    int getHeight();

    /**
     * Returns the stack as an array of 1D pixel arrays. Note
     * that the size of the returned array may be greater than
     * the number of slices currently in the stack, with
     * unused elements set to null.
     */
    Object[] getImageArray();

    /**
     * Returns the pixel array for the specified slice, were 1<=n<=nslices.
     */
    Object getPixels(int n);

    /**
     * Returns an ImageProcessor for the specified slice,
     * were 1<=n<=nslices. Returns null if the stack is empty.
     */
    ImageProcessor getProcessor(int n);

    Rectangle getRoi();

    /**
     * Returns a shortened version (up to the first 60 characters or first newline and
     * suffix removed) of the label of the specified slice.
     * Returns null if the slice does not have a label.
     */
    String getShortSliceLabel(int n);

    /**
     * Returns the number of slices in this stack.
     */
    int getSize();

    /**
     * Returns the label of the specified slice, were 1<=n<=nslices.
     * Returns null if the slice does not have a label. For DICOM
     * and FITS stacks, labels may contain header information.
     */
    String getSliceLabel(int n);

    /**
     * Returns the slice labels as an array of Strings. Note
     * that the size of the returned array may be greater than
     * the number of slices currently in the stack. Returns null
     * if the stack is empty or the label of the first slice is null.
     */
    String[] getSliceLabels();

    int getWidth();

    /**
     * Returns true if this is a 3-slice HSB stack.
     */
    boolean isHSB();

    /**
     * Returns true if this is a 3-slice RGB stack.
     */
    boolean isRGB();

    /**
     * Returns true if this is a virtual (disk resident) stack.
     * This method is overridden by the VirtualStack subclass.
     */
    boolean isVirtual();

    /**
     * Assigns a new color model to this stack.
     */
    void setColorModel(ColorModel cm);

    /**
     * Assigns a pixel array to the specified slice,
     * were 1<=n<=nslices.
     */
    void setPixels(Object pixels, int n);

    void setRoi(Rectangle roi);

    /**
     * Sets the label of the specified slice, were 1<=n<=nslices.
     */
    void setSliceLabel(String label, int n);

    String toString();

    /**
     * Frees memory by deleting a few slices from the end of the stack.
     */
    void trim();

    /**
     * Updates this stack so its attributes, such as min, max,
     * calibration table and color model, are the same as 'ip'.
     */
    void update(ImageProcessor ip);

}
