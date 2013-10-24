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

package imagej.core.commands.assign.noisereduce;

import imagej.command.Command;
import imagej.command.ContextCommand;

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Plugin for specifying a rectangular neighborhood.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Command.class, label="Rectangular Neighborhood Specification")
public class RectangularNeighborhoodSpecifier extends ContextCommand {
	
	@Parameter(label = "Dimensionality")
	private int numDims;
	
	@Parameter(label = "Neighborhood: positive width", min = "0L")
	private long posX;

	@Parameter(label = "Neighborhood: positive height", min = "0L")
	private long posY;

	@Parameter(label = "Neighborhood: negative width", min = "0L")
	private long negX;

	@Parameter(label = "Neighborhood: negative height", min = "0L")
	private long negY;

	@Parameter(type = ItemIO.OUTPUT)
	private Neighborhood neighborhood;
	
	@Override
	public void run() {
		long[] posOffsets = new long[numDims];
		long[] negOffsets = new long[numDims];
		posOffsets[0] = posX;
		negOffsets[0] = negX;
		posOffsets[1] = posY;
		negOffsets[1] = negY;
		neighborhood = new RectangularNeigh(posOffsets, negOffsets);
	}

	public void setDimensionality(int d) { numDims = d; }
	
	public long getDimensionality() { return numDims; }
	
	public void setPositiveX(long size) {
		posX = size;
	}
	
	public long getPositiveX() {
		return posX;
	}

	public void setNegativeX(long size) {
		negX = size;
	}
	
	public long getNegativeX() {
		return negX;
	}
	
	public void setPositiveY(long size) {
		posY = size;
	}
	
	public long getPositiveY() {
		return posY;
	}

	public void setNegativeY(long size) {
		negY = size;
	}
	
	public long getNegativeY() {
		return negY;
	}
	
	public Neighborhood getNeighborhood() { return neighborhood; }
}
