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

import imagej.service.Service;

/**
 * Interface for the logging service.
 * <p>
 * The service supports five common logging levels: {@link #ERROR},
 * {@link #WARN}, {@link #INFO}, {@link #TRACE} and {@link #DEBUG}. It provides
 * methods for logging messages, exception stack traces and combinations of the
 * two.
 * </p>
 * 
 * @author Curtis Rueden
 */
public interface LogService extends Service {

	/** System property to set for overriding the default logging level. */
	String LOG_LEVEL_PROPERTY = "ij.log.level";

	int NONE = 0;
	int ERROR = 1;
	int WARN = 2;
	int INFO = 3;
	int DEBUG = 4;
	int TRACE = 5;

	void debug(Object msg);

	void debug(Throwable t);

	void debug(Object msg, Throwable t);

	void error(Object msg);

	void error(Throwable t);

	void error(Object msg, Throwable t);

	void info(Object msg);

	void info(Throwable t);

	void info(Object msg, Throwable t);

	void trace(Object msg);

	void trace(Throwable t);

	void trace(Object msg, Throwable t);

	void warn(Object msg);

	void warn(Throwable t);

	void warn(Object msg, Throwable t);

	boolean isDebug();

	boolean isError();

	boolean isInfo();

	boolean isTrace();

	boolean isWarn();

	int getLevel();

	void setLevel(int level);

}
