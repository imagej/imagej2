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

package imagej.updater.util;

import org.scijava.Context;
import org.scijava.Prioritized;
import org.scijava.log.LogService;

/**
 * Deprecated; do not use.
 * <p>
 * This class is here solely to keep the Updater process running even if only
 * ij-updater-core is updated but not ij-ui-swing-updater.
 * </p>
 * 
 * @author Johannes Schindelin
 * @deprecated Use {@link org.scijava.log.StderrLogService} instead.
 */
@Deprecated
public class StderrLogService implements LogService {
	@Override public void initialize() { }
	@Override public void dispose() { }
	@Override public Context getContext() { return null; }
	@Override public void setContext(Context context) { }
	@Override public double getPriority() { return 0; }
	@Override public void setPriority(double priority) { }
	@Override public int compareTo(Prioritized o) { return 0; }
	@Override public void debug(Object msg) { System.err.println(msg); }
	@Override public void debug(Throwable t) { t.printStackTrace(); }
	@Override public void debug(Object msg, Throwable t) { debug(msg); debug(t); }
	@Override public void error(Object msg) { debug(msg); }
	@Override public void error(Throwable t) { debug(t); }
	@Override public void error(Object msg, Throwable t) { debug(msg, t); }
	@Override public void info(Object msg) { debug(msg); }
	@Override public void info(Throwable t) { debug(t); }
	@Override public void info(Object msg, Throwable t) { debug(msg, t); }
	@Override public void trace(Object msg) { debug(msg); }
	@Override public void trace(Throwable t) { debug(t); }
	@Override public void trace(Object msg, Throwable t) { debug(msg, t); }
	@Override public void warn(Object msg) { debug(msg); }
	@Override public void warn(Throwable t) { debug(t); }
	@Override public void warn(Object msg, Throwable t) { debug(msg, t); }
	@Override public boolean isDebug() { return true; }
	@Override public boolean isError() { return true; }
	@Override public boolean isInfo() { return true; }
	@Override public boolean isTrace() { return true; }
	@Override public boolean isWarn() { return true; }
	@Override public int getLevel() { return TRACE; }
	@Override public void setLevel(int level) { }
}
