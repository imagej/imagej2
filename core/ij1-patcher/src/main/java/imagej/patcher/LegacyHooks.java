/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package imagej.patcher;

import java.io.File;
import java.net.URL;
import java.util.List;

/**
 * Extension points for ImageJ 1.x.
 * <p>
 * These extension points will be patched into ImageJ 1.x by the
 * {@link CodeHacker}. To override the behavior of ImageJ 1.x, a new instance of
 * this interface needs to be installed into <code>ij.IJ._hooks</code>.
 * </p>
 * <p>
 * The essential functionality of the hooks is provided in the
 * {@link EssentialLegacyHooks} class, which makes an excellent base class for
 * project-specific implementations.
 * </p>
 * 
 * @author Johannes Schindelin
 */
public interface LegacyHooks {

	/**
	 * Determines whether the image windows should be displayed or not.
	 * 
	 * @return false if ImageJ 1.x should be prevented from opening image
	 *         windows.
	 */
	boolean isLegacyMode();

	/**
	 * Return the current context, if any.
	 * <p>
	 * For ImageJ2-specific hooks, the returned object will be the current
	 * SciJava context, or null if the context is not yet initialized.
	 * </p>
	 * 
	 * @return the context, or null
	 */
	Object getContext();

	/**
	 * Disposes and prepares for quitting.
	 * 
	 * @return whether ImageJ 1.x should be allowed to call System.exit()
	 */
	boolean quit();

	/**
	 * Runs when the hooks are installed into an existing legacy environment.
	 */
	void installed();

	/**
	 * Disposes the hooks.
	 * <p>
	 * This method is called when ImageJ 1.x is quitting or when new hooks are
	 * installed.
	 * </p>
	 */
	void dispose();

	/**
	 * Intercepts the call to {@link ij.IJ#runPlugIn(String, String)}.
	 * 
	 * @param className
	 *            the class name
	 * @param arg
	 *            the argument passed to the {@code runPlugIn} method
	 * @return the object to return, or null to let ImageJ 1.x handle the call
	 */
	Object interceptRunPlugIn(String className, String arg);

	/**
	 * Updates the progress bar, where 0 <= progress <= 1.0.
	 * 
	 * @param value
	 *            between 0.0 and 1.0
	 */
	void showProgress(double progress);

	/**
	 * Updates the progress bar, where the length of the bar is set to (
	 * <code>currentValue + 1) / finalValue</code> of the maximum bar length.
	 * The bar is erased if <code>currentValue &gt;= finalValue</code>.
	 * 
	 * @param currentIndex
	 *            the step that was just started
	 * @param finalIndex
	 *            the final step.
	 */
	void showProgress(int currentIndex, int finalIndex);

	/**
	 * Shows a status message.
	 * 
	 * @param status
	 *            the message
	 */
	void showStatus(String status);

	/**
	 * Logs a message.
	 * 
	 * @param message
	 *            the message
	 */
	void log(String message);

	/**
	 * Registers an image (possibly not seen before).
	 * 
	 * @param image
	 *            the new image
	 */
	void registerImage(Object image);

	/**
	 * Releases an image.
	 * 
	 * @param imagej
	 *            the image
	 */
	void unregisterImage(Object imagej);

	/**
	 * Logs a debug message (to be shown only in debug mode).
	 * 
	 * @param string
	 *            the debug message
	 */
	void debug(String string);

	/**
	 * Shows an exception.
	 * 
	 * @param t
	 *            the exception
	 */
	void error(Throwable t);

	/**
	 * Returns the name to use in place of "ImageJ".
	 * 
	 * @return the application name
	 */
	String getAppName();

	/**
	 * Returns the icon to use in place of the ImageJ microscope.
	 * 
	 * @return the URL to the icon to use, or null
	 */
	URL getIconURL();

	/**
	 * Extension point to override ImageJ 1.x' editor.
	 * 
	 * @param path
	 *            the path to the file to open
	 * @return true if the hook opened a different editor
	 */
	boolean openInEditor(String path);

	/**
	 * Extension point to override ImageJ 1.x' editor.
	 * 
	 * @param fileName
	 *            the name of the new file
	 * @param content
	 *            the initial content
	 * @return true if the hook opened a different editor
	 */
	boolean createInEditor(String fileName, String content);

	/**
	 * Extension point to add to ImageJ 1.x' PluginClassLoader's class path.
	 * 
	 * @return a list of class path elements to add
	 */
	List<File> handleExtraPluginJars();

	/**
	 * Extension point to run after <i>Help&gt;Refresh Menus</i>
	 */
	void runAfterRefreshMenus();

	/**
	 * Extension point to enhance ImageJ 1.x' error reporting upon
	 * {@link NoSuchMethodError}.
	 * 
	 * @param e
	 *            the exception to handle
	 * @return true if the error was handled by the legacy hook
	 */
	boolean handleNoSuchMethodError(NoSuchMethodError e);

	/**
	 * Extension point to run after a new PluginClassLoader was initialized.
	 * 
	 * @param loader
	 *            the PluginClassLoader instance
	 */
	void newPluginClassLoader(final ClassLoader loader);

	/**
	 * First extension point to run just after ImageJ 1.x spun up.
	 */
	void initialized();
}
