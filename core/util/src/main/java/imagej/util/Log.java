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
 * TODO
 *
 * @author Curtis Rueden
 */
public final class Log {

	private Log() {
		// prevent instantiation of utility class
	}

	private static Logger logger =
		LoggerFactory.getLogger(Log.class);

	public static Logger getLogger() {
		return logger;
	}

	public static void setLogger(Logger logger) {
		Log.logger = logger;
	}

	public static void debug(String msg) {
		logger.debug(msg);
	}

	public static void debug(Throwable t) {
		debug("Exception", t);
	}

	public static void debug(String msg, Throwable t) {
		logger.debug(msg, t);
	}

	public static void error(String msg) {
		logger.error(msg);
	}

	public static void error(Throwable t) {
		error("Exception", t);
	}

	public static void error(String msg, Throwable t) {
		logger.error(msg, t);
	}

	public static void info(String msg) {
		logger.info(msg);
	}

	public static void info(Throwable t) {
		info("Exception", t);
	}

	public static void info(String msg, Throwable t) {
		logger.info(msg, t);
	}
	
	public static void trace(String msg) {
		logger.trace(msg);
	}

	public static void trace(Throwable t) {
		trace("Exception", t);
	}

	public static void trace(String msg, Throwable t) {
		logger.trace(msg, t);
	}

	public static void warn(String msg) {
		logger.warn(msg);
	}

	public static void warn(Throwable t) {
		warn("Exception", t);
	}

	public static void warn(String msg, Throwable t) {
		logger.warn(msg, t);
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

}
