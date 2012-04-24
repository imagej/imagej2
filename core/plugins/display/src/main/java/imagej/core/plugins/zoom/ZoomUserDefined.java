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

package imagej.core.plugins.zoom;

import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.ext.menu.MenuConstants;
import imagej.ext.module.DefaultModuleItem;
import imagej.ext.plugin.DynamicPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.util.IntCoords;
import imagej.util.RealCoords;

/**
 * Zooms in on the center of the image at the user-specified magnification
 * level.
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
		mnemonic = MenuConstants.IMAGE_MNEMONIC),
	@Menu(label = "Zoom", mnemonic = 'z'), @Menu(label = "Set...", weight = 6) },
	headless = true)
public class ZoomUserDefined extends DynamicPlugin {

	// -- Constants --

	private static final String ZOOM = "userDefinedScale";
	private static final String CTR_U = "centerU";
	private static final String CTR_V = "centerV";

	// -- Parameters --

	@Parameter(persist = false)
	private ImageDisplayService imageDisplayService;

	@Parameter(persist = false)
	private ImageDisplay display;

	@Parameter(label = "Zoom (%) :", persist = false, initializer = "initAll")
	private double zoomPercent;

	@Parameter(label = "X center:", persist = false)
	private long centerU;

	@Parameter(label = "Y center:", persist = false)
	private long centerV;

	// -- Fields --

	private long maxU, maxV;

	// -- ZoomUserDefined methods --

	public ImageDisplayService getImageDisplayService() {
		return imageDisplayService;
	}

	public void setImageDisplayService(
		final ImageDisplayService imageDisplayService)
	{
		this.imageDisplayService = imageDisplayService;
	}

	public ImageDisplay getDisplay() {
		return display;
	}

	public void setDisplay(final ImageDisplay display) {
		this.display = display;
	}

	public double getZoomPercent() {
		return zoomPercent;
	}

	public void setZoomPercent(final double zoomPercent) {
		this.zoomPercent = zoomPercent;
	}

	public long getCenterU() {
		return centerU;
	}

	public void setCenterU(final long centerU) {
		this.centerU = centerU;
	}

	public long getCenterV() {
		return centerV;
	}

	public void setCenterV(final long centerV) {
		this.centerV = centerV;
	}

	// -- Runnable methods --

	@Override
	public void run() {
		display.getCanvas().setZoom(zoomPercent / 100.0,
				new RealCoords(getCenterU(), getCenterV()));
	}

	public double getUserDefinedScale() {
		return zoomPercent;
	}

	public void setUserDefinedScale(final double userDefinedScale) {
		this.zoomPercent = userDefinedScale;
	}

	// -- Initializers --

	protected void initAll() {
		initZoom();
		initCenter();
	}

	// -- Helper methods --

	private void initZoom() {
		@SuppressWarnings("unchecked")
		final DefaultModuleItem<Double> zoomItem =
			(DefaultModuleItem<Double>) getInfo().getInput(ZOOM);
		zoomItem.setMinimumValue(0.1);
		zoomItem.setMaximumValue(500000.0);
		setZoomPercent(100);
	}

	private void initCenter() {
		final Dataset dataset = imageDisplayService.getActiveDataset(display);
		maxU = dataset.getImgPlus().dimension(0);
		maxV = dataset.getImgPlus().dimension(1);
		@SuppressWarnings("unchecked")
		final DefaultModuleItem<Long> centerXItem =
			(DefaultModuleItem<Long>) getInfo().getInput(CTR_U);
		@SuppressWarnings("unchecked")
		final DefaultModuleItem<Long> centerYItem =
			(DefaultModuleItem<Long>) getInfo().getInput(CTR_V);
		centerXItem.setMinimumValue(0L);
		centerXItem.setMaximumValue(maxU - 1);
		centerYItem.setMinimumValue(0L);
		centerYItem.setMaximumValue(maxV - 1);
		setCenterU(maxU / 2);
		setCenterV(maxV / 2);
	}

}
