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

package imagej.plugins.commands.binary;

import imagej.command.ContextCommand;
import imagej.data.Dataset;
import net.imglib2.ops.types.ConnectedType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;

/**
 * Abstract super class for commands that modify binary images in place.
 * 
 * @author Barry DeZonia
 */
public abstract class AbstractMorphOpsCommand extends ContextCommand {

	// -- constants --

	public static final String FOUR = "Four";
	public static final String EIGHT = "Eight";

	// -- Parameters --

	@Parameter(type = ItemIO.BOTH)
	private Dataset dataset;

	@Parameter(label = "Neighbors", choices = { FOUR, EIGHT })
	private String neighbors = FOUR;

	// -- abstract methods --

	abstract protected void updateDataset(Dataset ds);

	// -- accessors --

	public ConnectedType getConnectedType() {
		if (neighbors == FOUR) return ConnectedType.FOUR_CONNECTED;
		return ConnectedType.EIGHT_CONNECTED;
	}

	public void setConnectedType(ConnectedType type) {
		if (type.equals(ConnectedType.FOUR_CONNECTED)) neighbors = FOUR;
		else neighbors = EIGHT;
	}

	// -- Command methods --

	@Override
	public void run() {
		if (!isBitType(dataset)) {
			cancel("This command requires input dataset to be of type BitType.");
		}
		else updateDataset(dataset);
	}

	// -- helpers --

	private boolean isBitType(Dataset ds) {
		RealType<?> type = ds.getImgPlus().firstElement();
		return (type instanceof BitType);
	}
}
