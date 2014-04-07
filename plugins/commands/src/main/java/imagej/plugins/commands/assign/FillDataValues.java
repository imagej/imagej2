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

package imagej.plugins.commands.assign;

import imagej.command.Command;
import imagej.command.ContextCommand;
import imagej.data.ChannelCollection;
import imagej.data.Dataset;
import imagej.data.Position;
import imagej.data.display.OverlayService;
import imagej.data.overlay.Overlay;
import imagej.menu.MenuConstants;
import net.imglib2.type.numeric.RealType;

import org.scijava.ItemIO;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Fills the selected region of an input Dataset with the foreground values.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Command.class, menu = {
	@Menu(label = MenuConstants.EDIT_LABEL, weight = MenuConstants.EDIT_WEIGHT,
		mnemonic = MenuConstants.EDIT_MNEMONIC),
	@Menu(label = "Fill", weight = 28, accelerator = "^F") }, headless = true)
public class FillDataValues<T extends RealType<T>> extends ContextCommand {

	// -- Parameters --

	@Parameter
	private OverlayService overlayService;

	@Parameter(type = ItemIO.BOTH)
	private Dataset dataset;

	@Parameter
	private Position position;

	// TODO Investigate bug if persist = true
	@Parameter(persist = false)
	private Overlay overlay;

	@Parameter
	private ChannelCollection channelCollection;

	// -- public interface --

	@Override
	public void run() {
		if (overlay == null) {
			cancel("This command requires a selection");
			return;
		}
		if (channelCollection == null) {
			cancel("This command requires a channel collection (i.e., color) to fill with");
			return;
		}
		overlayService.fillOverlay(overlay, dataset, position, channelCollection);
	}

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(final Dataset dataset) {
		this.dataset = dataset;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(final Position position) {
		this.position = position;
	}

	public Overlay getOverlay() {
		return overlay;
	}

	public void setOverlay(final Overlay overlay) {
		this.overlay = overlay;
	}
	
	public ChannelCollection getChannelCollection() {
		return channelCollection;
	}

	public void setChannelCollection(final ChannelCollection channelCollection) {
		this.channelCollection = channelCollection;
	}

	// -- private helpers --

	// TODO - make this part of Dataset API maybe. or somewhere else.
	/*
	private void fillSelectedRegion(final Dataset dataset,
		final ChannelCollection channels)
	{
		final RealRect bounds = overlayService.getSelectionBounds(display);
		final long minX = (long) bounds.x;
		final long minY = (long) bounds.y;
		final long maxX = (long) (bounds.x + bounds.width - 1);
		final long maxY = (long) (bounds.y + bounds.height - 1);
		final long[] pos = new long[dataset.numDimensions()];
		final int xIndex = dataset.dimensionIndex(Axes.X);
		final int yIndex = dataset.dimensionIndex(Axes.Y);
		final int chIndex = dataset.dimensionIndex(Axes.CHANNEL);
		final ImgPlus<? extends RealType<?>> imgPlus = dataset.getImgPlus();
		final Cursor<? extends RealType<?>> cursor = imgPlus.localizingCursor();
		while (cursor.hasNext()) {
			final RealType<?> pixRef = cursor.next();
			cursor.localize(pos);
			if ((pos[xIndex] < minX) || (pos[xIndex] > maxX)) continue;
			if ((pos[yIndex] < minY) || (pos[yIndex] > maxY)) continue;
			long position = 0;
			if (chIndex >= 0) position = pos[chIndex];
			double value = channels.getChannelValue(position);
			pixRef.setReal(value);
		}
		dataset.update();
	}
	*/
}
