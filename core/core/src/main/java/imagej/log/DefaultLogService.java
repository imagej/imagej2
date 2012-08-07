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

import imagej.ImageJ;
import imagej.ext.plugin.Plugin;
import imagej.service.AbstractService;
import imagej.service.IService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default service for logging, implemented using <a
 * href="http://www.slf4j.org/">SLF4J</a>.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = IService.class)
public final class DefaultLogService extends AbstractService implements
	LogService
{

	private Logger logger;

	// -- Constructors --

	public DefaultLogService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	public DefaultLogService(final ImageJ context) {
		super(context);

		// HACK: Dirty, because every time a new ImageJ context is created with a
		// DefaultLogService, it will "steal" the default exception handling.
		DefaultUncaughtExceptionHandler.install(this);

		logger = LoggerFactory.getLogger(DefaultLogService.class);
	}

	// -- DefaultLogService methods --

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(final Logger logger) {
		this.logger = logger;
	}

	// -- LogService methods --

	@Override
	public void debug(final Object msg) {
		logger.debug(s(msg));
	}

	@Override
	public void debug(final Throwable t) {
		debug("Exception", t);
	}

	@Override
	public void debug(final Object msg, final Throwable t) {
		logger.debug(s(msg), t);
	}

	@Override
	public void error(final Object msg) {
		logger.error(s(msg));
	}

	@Override
	public void error(final Throwable t) {
		error("Exception", t);
	}

	@Override
	public void error(final Object msg, final Throwable t) {
		logger.error(s(msg), t);
	}

	@Override
	public void info(final Object msg) {
		logger.info(s(msg));
	}

	@Override
	public void info(final Throwable t) {
		info("Exception", t);
	}

	@Override
	public void info(final Object msg, final Throwable t) {
		logger.info(s(msg), t);
	}

	@Override
	public void trace(final Object msg) {
		logger.trace(s(msg));
	}

	@Override
	public void trace(final Throwable t) {
		trace("Exception", t);
	}

	@Override
	public void trace(final Object msg, final Throwable t) {
		logger.trace(s(msg), t);
	}

	@Override
	public void warn(final Object msg) {
		logger.warn(s(msg));
	}

	@Override
	public void warn(final Throwable t) {
		warn("Exception", t);
	}

	@Override
	public void warn(final Object msg, final Throwable t) {
		logger.warn(s(msg), t);
	}

	@Override
	public boolean isDebug() {
		return logger.isDebugEnabled();
	}

	@Override
	public boolean isError() {
		return logger.isErrorEnabled();
	}

	@Override
	public boolean isInfo() {
		return logger.isInfoEnabled();
	}

	@Override
	public boolean isTrace() {
		return logger.isTraceEnabled();
	}

	@Override
	public boolean isWarn() {
		return logger.isWarnEnabled();
	}

	// -- Helper methods --

	private String s(final Object o) {
		return o == null ? null : o.toString();
	}

}
