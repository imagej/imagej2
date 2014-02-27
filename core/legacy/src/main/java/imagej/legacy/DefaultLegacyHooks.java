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

package imagej.legacy;

import ij.ImagePlus;
import imagej.data.display.ImageDisplay;
import imagej.legacy.plugin.LegacyAppConfiguration;
import imagej.legacy.plugin.LegacyEditor;
import imagej.legacy.plugin.LegacyPostRefreshMenus;
import imagej.patcher.EssentialLegacyHooks;
import imagej.patcher.LegacyHooks;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Date;
import java.util.List;

import org.scijava.Context;
import org.scijava.InstantiableException;
import org.scijava.log.LogService;
import org.scijava.log.StderrLogService;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.plugin.SciJavaPlugin;
import org.scijava.util.ListUtils;

/**
 * The {@link LegacyHooks} encapsulating an active {@link LegacyService} for use within the patched ImageJ 1.x.
 * 
 * @author Johannes Schindelin
 */
public class DefaultLegacyHooks extends EssentialLegacyHooks {

	private LegacyService legacyService;
	private Context context;
	private PluginService pluginService;
	private LogService log;

	public DefaultLegacyHooks(LegacyService legacyService) {
		this.legacyService = legacyService;
	}

	/** @inherit */
	@Override
	public boolean isLegacyMode() {
		return legacyService.isLegacyMode();
	}

	/** @inherit */
	@Override
	public Object getContext() {
		return legacyService.getContext();
	}

	/** @inherit */
	@Override
	public boolean quit() {
		dispose();
		return isLegacyMode();
	}

	private LegacyEditor editor;
	private LegacyAppConfiguration appConfig;
	private List<LegacyPostRefreshMenus> afterRefreshMenus;

	/** inherit */
	@Override
	public synchronized void installed() {
		context = legacyService.getContext();
		IJ1Helper.subscribeEvents(context);
		pluginService = context.getService(PluginService.class);
		log = context.getService(LogService.class);
		if (log == null) log = new StderrLogService();

		editor = createInstanceOfType(LegacyEditor.class);
		appConfig = createInstanceOfType(LegacyAppConfiguration.class);
		// TODO: inject context automatically?
		afterRefreshMenus = pluginService.createInstancesOfType(LegacyPostRefreshMenus.class);
		for (final LegacyPostRefreshMenus o : afterRefreshMenus) {
			context.inject(o);
		}
	}

	// TODO: move to scijava-common?
	private<PT extends SciJavaPlugin> PT createInstanceOfType(final Class<PT> type) {
		if (pluginService == null) return null;
		PluginInfo<PT> info = ListUtils.first(pluginService.getPluginsOfType(type));
		if (info == null) return null;
		try {
			final PT result = info.createInstance();
			context.inject(result);
			return result;
		} catch (InstantiableException e) {
			log.error(e);
			return null;
		}
	}

	/** @inherit */
	@Override
	public void dispose() {
		IJ1Helper.subscribeEvents(null);
		// TODO: if there are still things open, we should object.
	}

	/** @inherit */
	@Override
	public Object interceptRunPlugIn(String className, String arg) {
		if (LegacyService.class.getName().equals(className))
			return legacyService;
		if (Context.class.getName().equals(className))
			return legacyService == null ? null : legacyService.getContext();
		return null;
	}

	/** Resolution to use when converting double progress to int ratio. */
	private static final int PROGRESS_GRANULARITY = 1000;

	/** @inherit */
	@Override
	public void showProgress(double progress) {
		final int currentIndex = (int) (PROGRESS_GRANULARITY * progress);
		final int finalIndex = PROGRESS_GRANULARITY;
		showProgress(currentIndex, finalIndex);
	}

	/** @inherit */
	@Override
	public void showProgress(int currentIndex, int finalIndex) {
		if (!isLegacyMode()) {
			legacyService.status().showProgress(currentIndex, finalIndex);
		}
	}

	/** @inherit */
	@Override
	public void showStatus(final String status) {
		if (!isInitialized()) {
			return;
		}
		if (!isLegacyMode()) {
			legacyService.status().showStatus(status);
		}
	}

	/** @inherit */
	@Override
	public void registerImage(final ImagePlus image) {
		// TODO: avoid using ImagePlus here altogether; use ImgLib2-ij's wrap() instead
		if (image == null) return;
		if (!image.isProcessor()) return;
		if (image.getWindow() == null) return;
		if (!isLegacyMode()) {
			if (!Utils.isLegacyThread(Thread.currentThread())) return;
			legacyService.log().debug("register legacy image: " + image);
		}
		try {
			legacyService.getImageMap().registerLegacyImage(image);
		} catch (UnsupportedOperationException e) {
			// ignore: the dummy legacy service does not have an image map
		}
	}

	/** @inherit */
	@Override
	public void unregisterImage(final ImagePlus image) {
		// TODO: avoid using ImagePlus here altogether; use ImgLib2-ij's wrap() instead
		if (isLegacyMode()) return;
		if (image == null) return;
		if (!Utils.isLegacyThread(Thread.currentThread())) return;
		legacyService.log().debug("ImagePlus.hide(): " + image);
		LegacyOutputTracker.removeOutput(image);
		try {
			ImageDisplay disp = legacyService.getImageMap().lookupDisplay(image);
			if (disp == null) {
				legacyService.getImageMap().unregisterLegacyImage(image);
			}
			else {
				disp.close();
			}
		} catch (UnsupportedOperationException e) {
			// ignore: the dummy legacy service does not have an image map
		}
		// end alternate
	}

	/** @inherit */
	@Override
	public void debug(String string) {
		legacyService.log().debug(string);
	}

	/** @inherit */
	@Override
	public void error(Throwable t) {
		legacyService.log().error(t);
	}

	private boolean isInitialized() {
		return legacyService.isInitialized();
	}

	// if the ij.log.file property is set, log every message to the file pointed to
	private BufferedWriter logFileWriter;

	/** @inherit */
	@Override
	public void log(String message) {
		if (message != null) {
			String logFilePath = System.getProperty("ij.log.file");
			if (logFilePath != null) {
				try {
					if (logFileWriter == null) {
						java.io.OutputStream out = new java.io.FileOutputStream(
								logFilePath, true);
						java.io.Writer writer = new java.io.OutputStreamWriter(
								out, "UTF-8");
						logFileWriter = new java.io.BufferedWriter(writer);
						logFileWriter.write("Started new log on " + new Date() + "\n");
					}
					logFileWriter.write(message);
					if (!message.endsWith("\n"))
						logFileWriter.newLine();
					logFileWriter.flush();
				} catch (Throwable t) {
					t.printStackTrace();
					System.getProperties().remove("ij.log.file");
					logFileWriter = null;
				}
			}
		}
	}

	/**
	 * Returns the application name for use with ImageJ 1.x.
	 * 
	 * @return the application name
	 */
	@Override
	public String getAppName() {
		return appConfig == null ? "ImageJ (legacy)" : appConfig.getAppName();
	}

	/**
	 * Returns the icon for use with ImageJ 1.x.
	 * 
	 * @return the application name
	 */
	@Override
	public URL getIconURL() {
		return appConfig == null ? null : appConfig.getIconURL();
	}

	/** @inherit */
	@Override
	public void runAfterRefreshMenus() {
		if (afterRefreshMenus != null) {
			for (final Runnable run : afterRefreshMenus) {
				run.run();
			}
		}
	}

	/**
	 * Opens the given path in the registered legacy editor, if any.
	 * 
	 * @param path the path of the file to open
	 * @return whether the file was opened successfully
	 */
	@Override
	public boolean openInEditor(final String path) {
		if (editor == null) return false;
		if (path.indexOf("://") > 0) return false;
		// if it has no extension, do not open it in the legacy editor
		if (!path.matches(".*\\.[0-9A-Za-z]{1,4}")) return false;
		if (stackTraceContains(getClass().getName() + ".openInEditor(")) return false;
		final File file = new File(path);
		if (!file.exists()) return false;
		if (isBinaryFile(file)) return false;
		return editor.open(file);
	}

	/**
	 * Creates the given file in the registered legacy editor, if any.
	 * 
	 * @param title the title of the file to create
	 * @param content the text of the file to be created
	 * @return whether the fule was opened successfully
	 */
	@Override
	public boolean createInEditor(final String title, final String content) {
		if (editor == null) return false;
		return editor.create(title, content);
	}

	/**
	 * Determines whether a file is binary or text.
	 * 
	 * This just checks for a NUL in the first 1024 bytes.
	 * Not the best test, but a pragmatic one.
	 * 
	 * @param file the file to test
	 * @return whether it is binary
	 */
	private static boolean isBinaryFile(final File file) {
		try {
			InputStream in = new FileInputStream(file);
			byte[] buffer = new byte[1024];
			int offset = 0;
			while (offset < buffer.length) {
				int count = in.read(buffer, offset, buffer.length - offset);
				if (count < 0) break;
				offset += count;
			}
			in.close();
			while (offset > 0) {
				if (buffer[--offset] == 0) {
					return true;
				}
			}
		} catch (IOException e) {
		}
		return false;
	}

	/**
	 * Determines whether the current stack trace contains the specified string.
	 * 
	 * @param needle the text to find
	 * @return whether the stack trace contains the text
	 */
	private static boolean stackTraceContains(String needle) {
		final StringWriter writer = new StringWriter();
		final PrintWriter out = new PrintWriter(writer);
		new Exception().printStackTrace(out);
		out.close();
		return writer.toString().indexOf(needle) >= 0;
	}

}
