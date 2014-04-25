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

package net.imagej.readme;

import org.scijava.app.AppService;
import org.scijava.command.CommandService;
import org.scijava.event.EventHandler;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.ui.event.UIShownEvent;
import org.scijava.util.Prefs;

/**
 * Default service for displaying the ImageJ README.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public class DefaultReadmeService extends AbstractService implements
	ReadmeService
{

	@Parameter
	private LogService log;

	@Parameter
	private AppService appService;

	@Parameter
	private CommandService commandService;

	// -- ReadmeService methods --

	@Override
	public void displayReadme() {
		commandService.run(ShowReadme.class, true);
	}

	@Override
	public boolean isFirstRun() {
		final String firstRun = Prefs.get(getClass(), firstRunPrefKey());
		return firstRun == null || Boolean.parseBoolean(firstRun);
	}

	@Override
	public void setFirstRun(final boolean firstRun) {
		Prefs.put(getClass(), firstRunPrefKey(), firstRun);
	}

	// -- Event handlers --

	/** Displays the readme when the ImageJ UI is shown for the first time. */
	@EventHandler
	protected void onEvent(@SuppressWarnings("unused") final UIShownEvent evt) {
		if (!isFirstRun()) return;
		setFirstRun(false);
		displayReadme();
	}

	// -- Helper methods --

	/** Gets the preference key for ImageJ's first run. */
	private String firstRunPrefKey() {
		return "firstRun-" + appService.getApp().getVersion();
	}

}
