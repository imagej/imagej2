package imagej.display.lut;

/**
 * Adds Alpha to Lookup Table for 256 RGB 
 * 
 * @author GBH
 */
public class LutWithAlpha extends Lut {

	public final byte[] alpha = new byte[lutSize];
	
}
