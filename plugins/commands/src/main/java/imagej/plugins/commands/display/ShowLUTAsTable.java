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

package imagej.plugins.commands.display;

import java.util.List;

import net.imagej.Position;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.table.DefaultResultsTable;
import net.imagej.table.ResultsTable;
import net.imglib2.display.ColorTable;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.menu.MenuConstants;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Loads the current {@link ImageDisplay}'s color table into a
 * {@link ResultsTable}.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Command.class, menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
		mnemonic = MenuConstants.IMAGE_MNEMONIC), @Menu(label = "Color"),
	@Menu(label = "Show LUT As Table", weight = 13) })
public class ShowLUTAsTable extends ContextCommand {

	// -- Parameters --

	@Parameter
	private ImageDisplayService imgDispService;

	@Parameter
	private ImageDisplay display;

	@Parameter(type = ItemIO.OUTPUT, label = "Look-Up Table")
	private ResultsTable table;

	// -- Command methods --

	@Override
	public void run() {
		final DatasetView view = imgDispService.getActiveDatasetView(display);
		final List<ColorTable> colorTables = view.getColorTables();
		final Position planePos = view.getPlanePosition();
		long pos = planePos.getIndex();
		if (pos < 0 || pos >= colorTables.size()) pos = 0;
		final ColorTable colorTable = colorTables.get((int) pos);
		final int rowCount = colorTable.getLength();
		final int componentCount = colorTable.getComponentCount();
		final int colCount = componentCount + 1;
		table = new DefaultResultsTable(colCount, rowCount);
		table.setColumnHeader(0, "Index");
		// TODO - For now provide default channel name column headers
		// At some point we hope to have dimensional position labels which we could
		// use here.
		for (int x = 0; x < componentCount; x++) {
			table.setColumnHeader(x + 1, "CH" + x);
		}
		// fill in values
		for (int y = 0; y < rowCount; y++) {
			table.setValue(0, y, y);
			for (int x = 0; x < componentCount; x++) {
				final double value = colorTable.get(x, y);
				table.setValue(x + 1, y, value);
			}
		}
	}

}
