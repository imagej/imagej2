package imagej.display;

/**
 *
 * @author GBH
 */
//This class is required for high precision image coordinates translation.
public class Coords {

	public double x;
	public double y;

	public Coords(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public int getIntX() {
		return (int) Math.round(x);
	}

	public int getIntY() {
		return (int) Math.round(y);
	}

	@Override
	public String toString() {
		return "[Coords: x=" + x + ",y=" + y + "]";
	}

}
