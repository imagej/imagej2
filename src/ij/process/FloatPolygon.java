package ij.process;

/** Used by the Roi classes to return float coordinate arrays and to
	determine if a point is inside or outside of spline fitted selections. */
public class FloatPolygon {

    /** The number of points. */
    public int npoints;

    /* The array of x coordinates. */
    public float xpoints[];

    /* The array of y coordinates. */
    public float ypoints[];


    /** Constructs a FloatPolygon. */ 
    public FloatPolygon(float xpoints[], float ypoints[], int npoints) {
		this.npoints = npoints;
		this.xpoints = xpoints;
		this.ypoints = ypoints;
    }
    
	/** Returns 'true' if the point (x,y) is inside this polygon. This is a Java
	version of the remarkably small C program by W. Randolph Franklin at
	http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html#The%20C%20Code
	*/
	public boolean contains(float x, float y) {
		boolean inside = false;
		for (int i=0, j=npoints-1; i<npoints; j=i++) {
			if (((ypoints[i]>y)!=(ypoints[j]>y)) &&
			(x<(xpoints[j]-xpoints[i])*(y-ypoints[i])/(ypoints[j]-ypoints[i])+xpoints[i]))
			inside = !inside;
		}
		return inside;
	}


}
