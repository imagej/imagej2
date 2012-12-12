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

	// -- LogService methods --

	@Override
	public void debug(final Object msg) {
		if (isDebug()) {
			System.err.println(msg.toString());
		}
	}

	@Override
	public void debug(final Throwable t) {
		if (isDebug()) {
			t.printStackTrace();
		}
	}

	@Override
	public void debug(final Object msg, final Throwable t) {
		if (isDebug()) {
			System.err.println(msg.toString());
			t.printStackTrace();
		}
	}

	@Override
	public void error(final Object msg) {
		if (isError()) {
			System.err.println(msg.toString());
		}
	}

	@Override
	public void error(final Throwable t) {
		if (isError()) {
			t.printStackTrace();
		}
	}

	@Override
	public void error(final Object msg, final Throwable t) {
		if (isError()) {
			System.err.println(msg.toString());
			t.printStackTrace();
		}
	}

	@Override
	public void info(final Object msg) {
		if (isInfo()) {
			System.err.println(msg.toString());
		}
	}

	@Override
	public void info(final Throwable t) {
		if (isInfo()) {
			t.printStackTrace();
		}
	}

	@Override
	public void info(final Object msg, final Throwable t) {
		if (isInfo()) {
			System.err.println(msg.toString());
			t.printStackTrace();
		}
	}

	@Override
	public void trace(final Object msg) {
		if (isTrace()) {
			System.err.println(msg.toString());
		}
	}

	@Override
	public void trace(final Throwable t) {
		if (isTrace()) {
			t.printStackTrace();
		}
	}

	@Override
	public void trace(final Object msg, final Throwable t) {
		if (isTrace()) {
			System.err.println(msg.toString());
			t.printStackTrace();
		}
	}

	@Override
	public void warn(final Object msg) {
		if (isWarn()) {
			System.err.println(msg.toString());
		}
	}

	@Override
	public void warn(final Throwable t) {
		if (isWarn()) {
			t.printStackTrace();
		}
	}

	@Override
	public void warn(final Object msg, final Throwable t) {
		if (isWarn()) {
			System.err.println(msg.toString());
			t.printStackTrace();
		}
	}

	@Override
	public boolean isDebug() {
		return System.getenv("DEBUG") != null;
	}

	@Override
	public boolean isError() {
		return false;
	}

	@Override
	public boolean isInfo() {
		return true;
	}

	@Override
	public boolean isTrace() {
		return false;
	}

	@Override
	public boolean isWarn() {
		return false;
	}

	// -- Service methods --

	@Override
	public void initialize() {
		// HACK: Dirty, because every time a new ImageJ context is created with a
		// StderrLogService, it will "steal" the default exception handling.
		DefaultUncaughtExceptionHandler.install(this);
	}

}
