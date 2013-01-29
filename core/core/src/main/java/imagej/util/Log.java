/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

package imagej.util;

import imagej.log.LogService;
import imagej.log.StderrLogService;

/**
 * Simple logging utility class.
 * <p>
 * The class supports five common logging levels: error, warn, info, trace and
 * debug. It provides methods for logging messages, exception stack traces and
 * combinations of the two.
 * </p>
 * 
 * @author Curtis Rueden
 * @deprecated Use {@link LogService} instead.
 */
@Deprecated
public final class Log {

	private static LogService log = new StderrLogService();

	private Log() {
		// prevent instantiation of utility class
	}

	public static void debug(final Object msg) {
		getLog().debug(s(msg));
	}

	public static void debug(final Throwable t) {
		debug("Exception", t);
	}

	public static void debug(final Object msg, final Throwable t) {
		getLog().debug(s(msg), t);
	}

	public static void error(final Object msg) {
		getLog().error(s(msg));
	}

	public static void error(final Throwable t) {
		error("Exception", t);
	}

	public static void error(final Object msg, final Throwable t) {
		getLog().error(s(msg), t);
	}

	public static void info(final Object msg) {
		getLog().info(s(msg));
	}

	public static void info(final Throwable t) {
		info("Exception", t);
	}

	public static void info(final Object msg, final Throwable t) {
		getLog().info(s(msg), t);
	}

	public static void trace(final Object msg) {
		getLog().trace(s(msg));
	}

	public static void trace(final Throwable t) {
		trace("Exception", t);
	}

	public static void trace(final Object msg, final Throwable t) {
		getLog().trace(s(msg), t);
	}

	public static void warn(final Object msg) {
		getLog().warn(s(msg));
	}

	public static void warn(final Throwable t) {
		warn("Exception", t);
	}

	public static void warn(final Object msg, final Throwable t) {
		getLog().warn(s(msg), t);
	}

	public static boolean isDebug() {
		return getLog().isDebug();
	}

	public static boolean isError() {
		return getLog().isError();
	}

	public static boolean isInfo() {
		return getLog().isInfo();
	}

	public static boolean isTrace() {
		return getLog().isTrace();
	}

	public static boolean isWarn() {
		return getLog().isWarn();
	}

	// -- Helper methods --

	private static LogService getLog() {
		return log;
	}

	private static String s(final Object o) {
		return o == null ? null : o.toString();
	}

}
