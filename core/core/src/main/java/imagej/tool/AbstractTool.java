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

package imagej.tool;

import imagej.display.event.input.KyPressedEvent;
import imagej.display.event.input.KyReleasedEvent;
import imagej.display.event.input.MsClickedEvent;
import imagej.display.event.input.MsDraggedEvent;
import imagej.display.event.input.MsMovedEvent;
import imagej.display.event.input.MsPressedEvent;
import imagej.display.event.input.MsReleasedEvent;
import imagej.display.event.input.MsWheelEvent;

import java.text.DecimalFormat;

import org.scijava.event.StatusService;
import org.scijava.input.MouseCursor;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.SortablePlugin;

/**
 * Abstract base class for ImageJ tools.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
public abstract class AbstractTool extends SortablePlugin implements Tool {

	private PluginInfo<? extends Tool> info;

	@Override
	public PluginInfo<? extends Tool> getInfo() {
		return info;
	}

	@Override
	public void setInfo(final PluginInfo<? extends Tool> info) {
		this.info = info;
	}

	@Override
	public MouseCursor getCursor() {
		return MouseCursor.DEFAULT;
	}

	@Override
	public void activate() {
		// do nothing by default
	}

	@Override
	public void deactivate() {
		// do nothing by default
	}

	@Override
	public void onKeyDown(final KyPressedEvent evt) {
		// do nothing by default
	}

	@Override
	public void onKeyUp(final KyReleasedEvent evt) {
		// do nothing by default
	}

	@Override
	public void onMouseDown(final MsPressedEvent evt) {
		// do nothing by default
	}

	@Override
	public void onMouseUp(final MsReleasedEvent evt) {
		// do nothing by default
	}

	@Override
	public void onMouseClick(final MsClickedEvent evt) {
		// do nothing by default
	}

	@Override
	public void onMouseMove(final MsMovedEvent evt) {
		// do nothing by default
	}

	@Override
	public void onMouseDrag(final MsDraggedEvent evt) {
		// do nothing by default
	}

	@Override
	public void onMouseWheel(final MsWheelEvent evt) {
		// do nothing by default
	}

	@Override
	public void configure() {
		// do nothing by default
	}

	@Override
	public String getDescription() {
		return info.getDescription();
	}

	// -- Internal methods --

	/** Publishes rectangle dimensions in the status bar. */
	protected void reportRectangle(final double x, final double y,
		final double w, final double h)
	{
		final DecimalFormat f = new DecimalFormat("0.##");
		final String fx = f.format(x);
		final String fy = f.format(y);
		final String fw = f.format(w);
		final String fh = f.format(h);
		report("x=" + fx + ", y=" + fy + ", w=" + fw + ", h=" + fh);
	}

	/** Publishes line length and angle in the status bar. */
	protected void reportLine(final double x1, final double y1, final double x2,
		final double y2)
	{
		// compute line angle
		final double dx = x2 - x1;
		final double dy = y1 - y2;
		final double angle = 180.0 / Math.PI * Math.atan2(dy, dx);

		// compute line length
		final double w = Math.abs(x2 - x1);
		final double h = Math.abs(y2 - y1);
		final double length = Math.sqrt(w * w + h * h);

		final DecimalFormat f = new DecimalFormat("0.##");
		final String fx = f.format(x2);
		final String fy = f.format(y2);
		final String fa = f.format(angle);
		final String fl = f.format(length);
		report("x=" + fx + ", y=" + fy + ", angle=" + fa + ", length=" + fl);
	}

	/** Publishes point location in the status bar. */
	protected void reportPoint(final double x, final double y) {
		final DecimalFormat f = new DecimalFormat("0.##");
		final String fx = f.format(x);
		final String fy = f.format(y);
		report("x=" + fx + ", y=" + fy);
	}

	/** Reports the given message using the associated status service, if any. */
	protected void report(final String message) {
		final StatusService statusService =
			getContext().getService(StatusService.class);
		if (statusService != null) statusService.showStatus(message);
	}

}
