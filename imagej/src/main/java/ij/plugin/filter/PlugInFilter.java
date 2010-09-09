package ij.plugin.filter;
import ij.*;
import ij.process.*;

/** ImageJ plugins that process an image should implement this interface.
 *  For filters that have a dialog asking for options or parameters as well
 *  as for filters that have a progress bar and process stacks the
 *  ExtendedPlugInFilter interface is recommended.
 */
public interface PlugInFilter {

	/** This method is called once when the filter is loaded. 'arg',
		which may be blank, is the argument specified for this plugin 
		in IJ_Props.txt or in the plugins.config file of a jar archive
        containing the plugin. 'imp' is the currently active image.
		This method should return a flag word that specifies the
		filters capabilities.
        <p>
        For Plugin-filters specifying the FINAL_PROCESSING flag,
        the setup method will be called again, this time with
        arg = "final" after all other processing is done.
     */
	public int setup(String arg, ImagePlus imp);

	/** Filters use this method to process the image. If the
	 	SUPPORTS_STACKS flag was set, it is called for each slice in
	 	a stack. With CONVERT_TO_FLOAT, the filter is called with
        the image data converted to a FloatProcessor (3 times per
        image for RGB images).
        ImageJ will lock the image before calling
		this method and unlock it when the filter is finished.
        For PlugInFilters specifying the NO_IMAGE_REQUIRED flag
        and not the DONE flag, run(ip) is called once with the
        argument <code>null</code>.
     */
	public void run(ImageProcessor ip);

	/** Set this flag if the filter handles 8-bit grayscale images. */
	public int DOES_8G = 1;
	/** Set this flag if the filter handles 8-bit indexed color images. */
	public int DOES_8C = 2;
	/** Set this flag if the filter handles 16-bit images. */
	public int DOES_16 = 4;
	/** Set this flag if the filter handles float images. */
	public int DOES_32 = 8;
	/** Set this flag if the filter handles RGB images. */
	public int DOES_RGB = 16;
	/** Set this flag if the filter handles all types of images. */
	public int DOES_ALL = DOES_8G+DOES_8C+DOES_16+DOES_32+DOES_RGB;
	/** Set this flag if the filter wants its run() method to be
		called for all the slices in a stack. */
	public int DOES_STACKS = 32;
	/** Set this flag if the filter wants ImageJ, for non-rectangular
		ROIs, to restore that part of the image that's inside the bounding
		rectangle but outside of the ROI. */
	public int SUPPORTS_MASKING = 64;
	/** Set this flag if the filter makes no changes to the pixel data and does not require undo. */
	public int NO_CHANGES = 128;
	/** Set this flag if the filter does not require undo. */
	public int NO_UNDO = 256;
	/** Set this flag if the filter does not require that an image be open. */
	public int NO_IMAGE_REQUIRED = 512;
	/** Set this flag if the filter requires an ROI. */
	public int ROI_REQUIRED = 1024;
	/** Set this flag if the filter requires a stack. */
	public int STACK_REQUIRED = 2048;
	/** Set this flag if the filter does not want its run method called. */
	public int DONE = 4096;
	/** Set this flag to have the ImageProcessor that is passed to the run() method 
		converted  to a FloatProcessor. With  RGB images, the run() method is  
		called three times, once for each channel. */
	public int CONVERT_TO_FLOAT = 8192;
	/** Set this flag if the filter requires a snapshot (copy of the pixels array). */
	public int SNAPSHOT = 16384;
    /** Set this flag if the filter wants to be called with arg = "dialog" after
        being invocated the first time */
    /** Set this flag if the slices of a stack may be processed in parallel threads */
    public final int PARALLELIZE_STACKS = 32768;
    /** Set this flag if the setup method of the filter should be called again after
     *  the calls to the run(ip) have finished. The argument <code>arg</code> of setup
     *  will be "final" in that case. */
    public final int FINAL_PROCESSING = 65536;
    /** Set this flag to keep the invisible binary threshold from being reset. */
    public final int KEEP_THRESHOLD = 131072;

    // flags 0x01000000 and above are reserved for ExtendedPlugInFilter
}
