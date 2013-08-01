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

package imagej.legacy;

import imagej.data.DatasetService;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.OverlayService;
import imagej.data.threshold.ThresholdService;
import imagej.display.DisplayService;
import imagej.options.OptionsService;

import org.scijava.Context;
import org.scijava.Prioritized;
import org.scijava.app.StatusService;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.log.StderrLogService;
import org.scijava.plugin.PluginService;

/**
 * A dummy LegacyService.
 * 
 * Before the ImageJ {@link Context} has started up, we still would like to be
 * able to use legacy ImageJ 1.x. For that to work *even if* ImageJ 1.x has been
 * patched using the {@link LegacyInjector}, we need to set the LegacyService to
 * a valid instance.
 * 
 * This LegacyService does not do anything, though, except waiting to be
 * replaced by a <b>real</b> LegacyService when an ImageJ {@link Context}
 * initializes it.
 * 
 * 
 * @author Johannes Schindelin
 */
public class DummyLegacyService implements LegacyService {
	private LogService log = new StderrLogService();

	@Override
	public String getLegacyVersion() {
		return "(none)";
	}

	@Override
	public void initialize() {
		throw new UnsupportedOperationException("The DummyLegacyService is not intended to be initialized!");
	}

	@Override
	public void registerEventHandlers() {
		throw new UnsupportedOperationException("The DummyLegacyService is not intended to be initialized!");
	}

	@Override
	public Context getContext() {
		throw new UnsupportedOperationException("The DummyLegacyService is not part of a valid ImageJ Context!");
	}

	@Override
	public void setContext(Context context) {
		throw new UnsupportedOperationException("The DummyLegacyService is not intended to be part of a valid ImageJ Context!");
	}

	@Override
	public double getPriority() {
		return 0;
	}

	@Override
	public void setPriority(double priority) {
		// ignore
	}

	@Override
	public int compareTo(Prioritized o) {
		return Double.compare(0, -o.getPriority());
	}

	@Override
	public void dispose() {
		// ignore
	}

	@Override
	public EventService getEventService() {
		throw new UnsupportedOperationException("The DummyLegacyService has no EventService!");
	}

	@Override
	public PluginService getPluginService() {
		throw new UnsupportedOperationException("The DummyLegacyService has no PluginService!");
	}

	@Override
	public OptionsService getOptionsService() {
		throw new UnsupportedOperationException("The DummyLegacyService has no OptionService!");
	}

	@Override
	public ImageDisplayService getImageDisplayService() {
		throw new UnsupportedOperationException("The DummyLegacyService has no ImageDisplayService!");
	}

	@Override
	public DisplayService getDisplayService() {
		throw new UnsupportedOperationException("The DummyLegacyService has no DisplayService!");
	}

	@Override
	public DatasetService getDatasetService() {
		throw new UnsupportedOperationException("The DummyLegacyService has no DatasetService!");
	}

	@Override
	public OverlayService getOverlayService() {
		throw new UnsupportedOperationException("The DummyLegacyService has no OverlayService!");
	}

	@Override
	public ThresholdService getThresholdService() {
		throw new UnsupportedOperationException("The DummyLegacyService has no ThresholdService!");
	}

	@Override
	public StatusService getStatusService() {
		throw new UnsupportedOperationException("The DummyLegacyService has no StatusService!");
	}

	@Override
	public LogService getLogService() {
		return log;
	}

	@Override
	public LegacyImageMap getImageMap() {
		throw new UnsupportedOperationException("The DummyLegacyService has no LegacyImageMap!");
	}

	@Override
	public OptionsSynchronizer getOptionsSynchronizer() {
		throw new UnsupportedOperationException("The DummyLegacyService has no OptionsSynchronizer!");
	}

	@Override
	public void runLegacyCommand(String ij1ClassName, String argument) {
		IJ1Helper.runIJ1PlugIn(ij1ClassName, argument);
	}

	@Override
	public void syncActiveImage() {
		// ignore
	}

	@Override
	public boolean isInitialized() {
		return false;
	}

	@Override
	public void updateLegacyImageJSettings() {
		// ignore
	}

	@Override
	public void updateModernImageJSettings() {
		// ignore
	}

	@Override
	public void syncColors() {
		// ignore
	}

	@Override
	public boolean isLegacyMode() {
		return true;
	}

	@Override
	public void toggleLegacyMode(boolean toggle) {
		if (!toggle) {
			throw new UnsupportedOperationException("The DummyLegacyService cannot switch off legacy mode!");
		}
	}

}
