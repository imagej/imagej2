package wfmodel;

/**
 * Stores the location of an entity with respect to an ArchitectureDiagramView.  
 * Referenced from the upper, left, near point of the reference cube
 * @author rick
 *
 */
public class Location {

	private double width;
	private double height;
	private double depth;
	
	public Location( double width, double height, double depth )
	{
		this.width = width;
		this.height = height;
		this.depth = depth;
	}
	
	public double getHeight() {
		return height;
	}

	public double getDepth() {
		return depth;
	}
	
	public double getWidth() {
		return width;
	}
	
	public void setWidth(double width) {
		this.width = width;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public void setDepth(double depth) {
		this.depth = depth;
	}

}
