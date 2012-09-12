package imagej.core.commands.assign.noisereduce;

import imagej.command.ContextCommand;
import imagej.module.ItemIO;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

@Plugin
public class RectangularNeighborhoodSpecifier extends ContextCommand {
	
	@Parameter(label = "Dimensionality")
	private int numDims;
	
	@Parameter(label = "Positive X", min = "0L")
	private long posX;

	@Parameter(label = "Positive Y", min = "0L")
	private long posY;

	@Parameter(label = "Negative X", min = "0L")
	private long negX;

	@Parameter(label = "Negative Y", min = "0L")
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
