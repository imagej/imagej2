/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2015 Board of Regents of the University of
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

package net.imagej.app;

import org.scijava.app.App;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;

/**
 * An extension of {@link ImageJApp} that provides the legacy ImageJ 1.x version
 * in addition to the regular ImageJ version, when {@link #getVersion()} is
 * called.
 * 
 * @author Curtis Rueden
 * @see org.scijava.app.AppService
 */
@Plugin(type = App.class, name = ImageJApp.NAME,
	priority = ImageJApp.PRIORITY + 1)
public class ToplevelImageJApp extends ImageJApp {

	// NB: This app uses the same name as ImageJApp, but with a higher priority,
	// so that it takes precedence in the AppService.

	@Parameter(required = false)
	private LogService log;

	@Override
	public String getArtifactId() {
		return "imagej";
	}

	@Override
	public String getVersion() {
		final String version = super.getVersion();
		final String legacyVersion = getLegacyVersion();
		return version + (legacyVersion == null ? "" : "/" + legacyVersion);
	}

	// -- Helper methods --

	private String getLegacyVersion() {
		try {
			final Class<?> c = Class.forName("net.imagej.legacy.LegacyService");
			if (!Service.class.isAssignableFrom(c)) return null; // no imagej-legacy

			@SuppressWarnings("unchecked")
			final Class<? extends Service> sc = (Class<? extends Service>) c;

			final Service legacyService = getContext().getService(sc);
			if (legacyService == null) return null; // no LegacyService in context

			return legacyService.getVersion();
		}
		catch (final ClassNotFoundException exc) {
			if (log != null) log.debug(exc);
		}
		return null;
	}

}
