//
// Log.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
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

package imagej.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple logging utility class that uses <a
 * href="http://www.slf4j.org/">SLF4J</a>.
 * <p>
 * The class supports five common logging levels: error, warn, info, trace and
 * debug. It provides methods for logging messages, exception stack traces
 * and combinations of the two.
 * </p>
 * 
 * @author Curtis Rueden
 */
public final class Log {

	private Log() {
		// prevent instantiation of utility class
	}

	private static Logger logger = LoggerFactory.getLogger(Log.class);

	public static Logger getLogger() {
		return logger;
	}

	public static void setLogger(final Logger logger) {
		Log.logger = logger;
	}

	public static void debug(final Object msg) {
		logger.debug(s(msg));
	}

	public static void debug(final Throwable t) {
		debug("Exception", t);
	}

	public static void debug(final Object msg, final Throwable t) {
		logger.debug(s(msg), t);
	}

	public static void error(final Object msg) {
		logger.error(s(msg));
	}

	public static void error(final Throwable t) {
		error("Exception", t);
	}

	public static void error(final Object msg, final Throwable t) {
		logger.error(s(msg), t);
	}

	public static void info(final Object msg) {
		logger.info(s(msg));
	}

	public static void info(final Throwable t) {
		info("Exception", t);
	}

	public static void info(final Object msg, final Throwable t) {
		logger.info(s(msg), t);
	}

	public static void trace(final Object msg) {
		logger.trace(s(msg));
	}

	public static void trace(final Throwable t) {
		trace("Exception", t);
	}

	public static void trace(final Object msg, final Throwable t) {
		logger.trace(s(msg), t);
	}

	public static void warn(final Object msg) {
		logger.warn(s(msg));
	}

	public static void warn(final Throwable t) {
		warn("Exception", t);
	}

	public static void warn(final Object msg, final Throwable t) {
		logger.warn(s(msg), t);
	}

	public static boolean isDebug() {
		return logger.isDebugEnabled();
	}

	public static boolean isError() {
		return logger.isErrorEnabled();
	}

	public static boolean isInfo() {
		return logger.isInfoEnabled();
	}

	public static boolean isTrace() {
		return logger.isTraceEnabled();
	}

	public static boolean isWarn() {
		return logger.isWarnEnabled();
	}

	// -- Helper methods --

	private static String s(final Object o) {
		return o == null ? null : o.toString();
	}

}
