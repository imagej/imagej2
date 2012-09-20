/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.log;

import imagej.Priority;
import imagej.plugin.Plugin;
import imagej.service.AbstractService;
import imagej.service.Service;

/**
 * Implementation of {@link LogService} using the standard error stream.
 * 
 * @author Johannes Schindelin
 * @author Curtis Rueden
 */
@Plugin(type = Service.class, priority = Priority.LOW_PRIORITY)
public class StderrLogService extends AbstractService implements LogService {

	private int level;

	public StderrLogService() {
		// check ImageJ log level system property for initial logging level
		final String logProp = System.getProperty(LOG_LEVEL_PROPERTY);
		if (logProp != null) {
			// check whether it's a string label (e.g., "debug")
			final String log = logProp.trim().toLowerCase();
			if (log.startsWith("n")) level = NONE;
			else if (log.startsWith("e")) level = ERROR;
			else if (log.startsWith("w")) level = WARN;
			else if (log.startsWith("i")) level = INFO;
			else if (log.startsWith("d")) level = DEBUG;
			else if (log.startsWith("t")) level = TRACE;
			else {
				// check whether it's a numerical value (e.g., 5)
				try {
					level = Integer.parseInt(log);
				}
				catch (final NumberFormatException exc) {
					// nope!
				}
			}
		}

		if (level == 0) {
			// use the default, which is INFO unless the DEBUG env. variable is set
			level = System.getenv("DEBUG") == null ? INFO : DEBUG;
		}
	}

	// -- LogService methods --

	@Override
	public void debug(final Object msg) {
		if (isDebug()) {
			log("[DEBUG] ", msg);
		}
	}

	@Override
	public void debug(final Throwable t) {
		if (isDebug()) {
			log("[DEBUG] ", t);
		}
	}

	@Override
	public void debug(final Object msg, final Throwable t) {
		if (isDebug()) {
			debug(msg);
			debug(t);
		}
	}

	@Override
	public void error(final Object msg) {
		if (isError()) {
			log("[ERROR] ", msg);
		}
	}

	@Override
	public void error(final Throwable t) {
		if (isError()) {
			log("[ERROR] ", t);
		}
	}

	@Override
	public void error(final Object msg, final Throwable t) {
		if (isError()) {
			error(msg);
			error(t);
		}
	}

	@Override
	public void info(final Object msg) {
		if (isInfo()) {
			log("[INFO] ", msg);
		}
	}

	@Override
	public void info(final Throwable t) {
		if (isInfo()) {
			log("[INFO] ", t);
			t.printStackTrace();
		}
	}

	@Override
	public void info(final Object msg, final Throwable t) {
		if (isInfo()) {
			info(msg);
			info(t);
		}
	}

	@Override
	public void trace(final Object msg) {
		if (isTrace()) {
			log("[TRACE] ", msg);
		}
	}

	@Override
	public void trace(final Throwable t) {
		if (isTrace()) {
			log("[TRACE] ", t);
		}
	}

	@Override
	public void trace(final Object msg, final Throwable t) {
		if (isTrace()) {
			trace(msg);
			trace(t);
		}
	}

	@Override
	public void warn(final Object msg) {
		if (isWarn()) {
			log("[WARN] ", msg);
		}
	}

	@Override
	public void warn(final Throwable t) {
		if (isWarn()) {
			log("[WARN] ", t);
		}
	}

	@Override
	public void warn(final Object msg, final Throwable t) {
		if (isWarn()) {
			warn(msg);
			warn(t);
		}
	}

	@Override
	public boolean isDebug() {
		return getLevel() >= DEBUG;
	}

	@Override
	public boolean isError() {
		return getLevel() >= ERROR;
	}

	@Override
	public boolean isInfo() {
		return getLevel() >= INFO;
	}

	@Override
	public boolean isTrace() {
		return getLevel() >= TRACE;
	}

	@Override
	public boolean isWarn() {
		return getLevel() >= WARN;
	}

	@Override
	public int getLevel() {
		return level;
	}

	@Override
	public void setLevel(int level) {
		this.level = level;
	}

	// -- Service methods --

	@Override
	public void initialize() {
		// HACK: Dirty, because every time a new ImageJ context is created with a
		// StderrLogService, it will "steal" the default exception handling.
		DefaultUncaughtExceptionHandler.install(this);
	}

	// -- private helper methods

	/**
	 * Prints a message to stderr.
	 * 
	 * @param prefix the prefix (can be an empty string)
	 * @param message the message
	 */
	private void log(final String prefix, final Object message) {
		System.err.print(prefix);
		System.err.println(message);
	}

	/**
	 * Prints an exception to stderr.
	 * 
	 * @param prefix the prefix (can be an empty string)
	 * @param t the exception
	 */
	private void log(final String prefix, final Throwable t) {
		System.err.print(prefix);
		t.printStackTrace();
	}

}
