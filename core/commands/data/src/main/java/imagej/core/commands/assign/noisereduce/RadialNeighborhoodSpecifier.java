package imagej.core.commands.assign.noisereduce;

import imagej.command.ContextCommand;
import imagej.module.ItemIO;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

@Plugin
public class RadialNeighborhoodSpecifier extends ContextCommand {
	
	@Parameter(label = "Dimensionality")
	private int numDims;
	
	@Parameter(label = "Neoghborhood: radius")
	private long radius;

	@Parameter(type = ItemIO.OUTPUT)
	private Neighborhood neighborhood;
	
	@Override
	public void run() {
		neighborhood = new RadialNeigh(numDims, radius);
	}
	
	public void setDimensionality(int d) { numDims = d; }
	
	public long getDimensionality() { return numDims; }
	
	public void setRadius(long r) { radius = r; }
	
	public long getRadius() { return radius; }
	
	public Neighborhood getNeighborhood() { return neighborhood; }
}
