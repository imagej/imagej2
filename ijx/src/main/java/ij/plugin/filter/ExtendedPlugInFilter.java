package ij.plugin.filter;
import ijx.IjxImagePlus;

/** ImageJ plugins that process an image may implement this interface.
 *  In addition to the features of PlugInFilter, it is better suited
 *  for filters that have adialog asking for the options or filter
 *  parameters. It also offers support for preview, for a smooth
 *  progress bar when processing stacks and for calling back
 *  the PlugInFilterRunner (needed, e.g., to get the slice number
 *  when processing a stack in parallel threads).
 *<p>
 *  The sequence of calls to an ExtendedPlugInFilter is the following:
 *<p>
 *  - <code>setup(arg, imp)</code>:  The filter should return its flags.  <p>
 *  - <code>showDialog(imp, command, pfr)</code>: The filter should display
 *         the dialog asking for parameters (if any) and do all operations
 *         needed to prepare for processing the individual image(s) (E.g.,
 *         slices of a stack). For preview, a separate thread may call
 *         <code>setNPasses(nPasses)</code> and <code>run(ip)</code> while
 *         the dialog is displayed. The filter should return its flags.   <p>
 *  - <code>setNPasses(nPasses)</code>: Informs the filter of the number
 *         of calls of <code>run(ip)</code> that will follow.             <p>
 *  - <code>run(ip)</code>: Processing of the image(s). With the
 *         <code>CONVERT_TO_FLOAT</code> flag, this method will be called for
 *         each color channel of an RGB image. With <code>DOES_STACKS</code>,
 *         it will be called for each slice of a stack.                   <p>
 *  - <code>setup("final", imp)</code>: called only if flag
 *         <code>FINAL_PROCESSING</code> has been specified.
 *<p>
 * Flag <code>DONE</code> stops this sequence of calls.
 */
public interface ExtendedPlugInFilter extends PlugInFilter {

    /**
     * This method is called after <code>setup(arg, imp)</code> unless the
     * <code>DONE</code> flag has been set.
     * @param imp The active image already passed in the
     * <code>setup(arg, imp)</code> call. It will be null, however, if
     * the <code>NO_IMAGE_REQUIRED</code> flag has been set.
     * @param command The command that has led to the invocation of
     * the plugin-filter. Useful as a title for the dialog.
     * @param pfr The PlugInFilterRunner calling this plugin-filter.
     * It can be passed to a GenericDialog by <code>addPreviewCheckbox</code>
     * to enable preview by calling the <code>run(ip)</code> method of this
     * plugin-filter. <code>pfr</code> can be also used later for calling back
     * the PlugInFilterRunner, e.g., to obtain the slice number
     * currently processed by <code>run(ip)</code>.
     * @return  The method should return a combination (bitwise OR)
     * of the flags specified in interfaces <code>PlugInFilter</code> and
     * <code>ExtendedPlugInFilter</code>.
     */
    public int showDialog(IjxImagePlus imp, String command, PlugInFilterRunner pfr);

    /**
     * This method is called by ImageJ to inform the plugin-filter
     * about the passes to its run method. During preview, the number of
     * passes is one (or 3 for RGB images, if <code>CONVERT_TO_FLOAT</code>
     * has been specified). When processing a stack, it is the number
     * of slices to be processed (minus one, if one slice has been
     * processed for preview before), and again, 3 times that number
     * for RGB images processed with <code>CONVERT_TO_FLOAT</code>.
     */
    public void setNPasses(int nPasses);

    /** Set this flag if the last preview image may be kept as a result.
        For stacks, this flag can lead to out-of-sequence processing of the
        slices, irrespective of the <code>PARALLELIZE_STACKS<code> flag.
     */
    public final int KEEP_PREVIEW = 0x1000000;

	
}
