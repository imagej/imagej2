package imagej.core.tools;

import imagej.data.Dataset;
import imagej.util.ColorRGB;

import java.util.Arrays;

import net.imglib2.RandomAccess;
import net.imglib2.meta.Axes;
import net.imglib2.type.numeric.RealType;

/**
 * This class, which does flood filling, is used by the FloodFillTool. It was
 * adapted from IJ1's FloodFiller class. That class implements the flood filling
 * code used by IJ1's macro language and IJ1's particle analyzer. The Wikipedia
 * article at "http://en.wikipedia.org/wiki/Flood_fill" has a good description
 * of the algorithm used here as well as examples in C and Java. 
 * 
 * @author Wayne Rasband
 * @author Barry DeZonia
 */
public class FloodFiller {
	private DrawingTool tool;
	private boolean isColor;
	private int colorAxis;
	private int uAxis;
	private int vAxis;
	private StackOfLongs uStack;
	private StackOfLongs vStack;
  
	/**
	 * Constructs a FloodFiller from a given DrawingTool. The FloodFiller uses the
	 * DrawingTool to fill a region of contiguous pixels in a plane of a Dataset.
	 */
	public FloodFiller(DrawingTool tool) {
		this.tool = tool;
		this.isColor = tool.getDataset().isRGBMerged();
		if (isColor)
			this.colorAxis = tool.getDataset().getAxisIndex(Axes.CHANNEL);
		else
			this.colorAxis = -1;
		this.uAxis = -1;
		this.vAxis = -1;
		this.uStack = new StackOfLongs();
		this.vStack = new StackOfLongs();
	}

	/**
	 * Does a 4-connected flood fill using the current fill/draw value.
	 */
	public void fill4(long u0, long v0, long[] position) {
		Dataset ds = tool.getDataset();
		RandomAccess<? extends RealType<?>> accessor =
			ds.getImgPlus().randomAccess();
		accessor.setPosition(position);
		uAxis = tool.getUAxis();
		vAxis = tool.getVAxis();
		long maxU = ds.dimension(uAxis);
		long maxV = ds.dimension(vAxis);
		ColorRGB origColor = getColor(accessor,u0,v0);
		Double origValue = getValue(accessor,u0,v0);
		uStack.clear();
		vStack.clear();
		push(u0, v0);
		while(!uStack.isEmpty()) {   
			long u = popU(); 
			long v = popV();
			if (!matches(accessor,u,v,origColor,origValue)) continue;
			long u1 = u;
			long u2 = u;
			// find start of scan-line
			while (u1>=0 && matches(accessor,u1,v,origColor,origValue)) u1--;
			u1++;
		  // find end of scan-line
			while (u2<maxU && matches(accessor,u2,v,origColor,origValue)) u2++;                 
			u2--;
			fillLine(tool, u1, u2, v); // fill scan-line
			boolean inScanLine = false;
			for (long i=u1; i<=u2; i++) { // find scan-lines above this one
				if (!inScanLine && v>0 && matches(accessor,i,v-1,origColor,origValue))
					{push(i, v-1); inScanLine = true;}
				else if (inScanLine && v>0 &&
									!matches(accessor,i,v-1,origColor,origValue))
					inScanLine = false;
			}
			inScanLine = false;
			for (long i=u1; i<=u2; i++) { // find scan-lines below this one
				if (!inScanLine && v<maxV-1 &&
							matches(accessor,i,v+1,origColor,origValue))
					{push(i, v+1); inScanLine = true;}
				else if (inScanLine && v<maxV-1 &&
									!matches(accessor,i,v+1,origColor,origValue))
					inScanLine = false;
			}
		}        
	}
	
	/**
	 * Does an 8-connected flood fill using the current fill/draw value.
	 */
	public void fill8(long u0, long v0, long[] position) {
		Dataset ds = tool.getDataset();
		RandomAccess<? extends RealType<?>> accessor =
			ds.getImgPlus().randomAccess();
		accessor.setPosition(position);
		uAxis = tool.getUAxis();
		vAxis = tool.getVAxis();
		long maxU = ds.dimension(uAxis);
		long maxV = ds.dimension(vAxis);
		ColorRGB origColor = getColor(accessor,u0,v0);
		Double origValue = getValue(accessor,u0,v0);
		uStack.clear();
		vStack.clear();
		push(u0, v0);
		while(!uStack.isEmpty()) {   
			long u = popU(); 
			long v = popV();
			long u1 = u;
			long u2 = u;
			if (matches(accessor,u1,v,origColor,origValue)) { 
				// find start of scan-line
				while (u1>=0 && matches(accessor,u1,v,origColor,origValue)) u1--;
				u1++;
			  // find end of scan-line
				while (u2<maxU && matches(accessor,u2,v,origColor,origValue)) u2++;
				u2--;
				fillLine(tool, u1, u2, v); // fill scan-line
			} 
			if (v > 0) {
				if (u1 > 0) {
					if (matches(accessor,u1-1,v-1,origColor,origValue)) {
						push(u1-1,v-1);
					}
				}
				if (u2 < maxU-1) {
					if (matches(accessor,u2+1,v-1,origColor,origValue)) {
						push(u2+1,v-1);
					}
				}
			}
			if (v < maxV-1) {
				if (u1 > 0) {
					if (matches(accessor,u1-1,v+1,origColor,origValue)) {
						push(u1-1,v+1);
					}
				}
				if (u2 < maxU-1) {
					if (matches(accessor,u2+1,v+1,origColor,origValue)) {
						push(u2+1,v+1);
					}
				}
			}
			boolean inScanLine = false;
			for (long i=u1; i<=u2; i++) { // find scan-lines above this one
				if (!inScanLine && v>0 && matches(accessor,i,v-1,origColor,origValue))
					{push(i, v-1); inScanLine = true;}
				else if (inScanLine && v>0 &&
									!matches(accessor,i,v-1,origColor,origValue))
					inScanLine = false;
			}
			inScanLine = false;
			for (long i=u1; i<=u2; i++) { // find scan-lines below this one
				if (!inScanLine && v<maxV-1 &&
							matches(accessor,i,v+1,origColor,origValue))
					{push(i, v+1); inScanLine = true;}
				else if (inScanLine && v<maxV-1 &&
									!matches(accessor,i,v+1,origColor,origValue))
					inScanLine = false;
			}
		}
	}

	// -- private helpers --
	
	/**
	 * Returns true if the current pixel located at the given (u,v) coordinates
	 * is the same as the specified color or gray values.
	 */
	private boolean matches(RandomAccess<? extends RealType<?>> accessor,
		long u, long v, ColorRGB origColor, double origValue)
	{
		accessor.setPosition(u, uAxis);
		accessor.setPosition(v, vAxis);
		
		// are we interested in values?
		if (!isColor) {
			double val = accessor.get().getRealDouble();
			return val == origValue;
		}
		
		// else interested in colors
		double component;
		
		accessor.setPosition(0,colorAxis);
		component = accessor.get().getRealDouble();
		if (component != origColor.getRed()) return false;
		
		accessor.setPosition(1,colorAxis);
		component = accessor.get().getRealDouble();
		if (component != origColor.getGreen()) return false;
		
		accessor.setPosition(2,colorAxis);
		component = accessor.get().getRealDouble();
		if (component != origColor.getBlue()) return false;
		
		return true;
	}

	/**
	 * Gets the color of the pixel at the (u,v) coordinates of the UV plane of the
	 * current DrawingTool. If the underlying Dataset is not color returns null.
	 */
	private ColorRGB getColor(RandomAccess<? extends RealType<?>> accessor,
		long u, long v)
	{
		if (!isColor) return null;
		accessor.setPosition(u,uAxis);
		accessor.setPosition(v,vAxis);
		accessor.setPosition(0,colorAxis);
		int r = (int) accessor.get().getRealDouble();
		accessor.setPosition(1,colorAxis);
		int g = (int) accessor.get().getRealDouble();
		accessor.setPosition(2,colorAxis);
		int b = (int) accessor.get().getRealDouble();
		return new ColorRGB(r,g,b);
	}
	
	/**
	 * Gets the gray value of the pixel at the (u,v) coordinates of the UV plane
	 * of the current DrawingTool. If the underlying Dataset is not gray returns
	 * Double.NaN.
	 */
	private double getValue(RandomAccess<? extends RealType<?>> accessor,
		long u, long v)
	{
		if (isColor) return Double.NaN;
		accessor.setPosition(u,uAxis);
		accessor.setPosition(v,vAxis);
		return accessor.get().getRealDouble();
	}

	/**
	 *  Draws a horizontal line in the current UV plane of the current DrawingTool.
	 *  The line runs from u1 to u2 at height v.
	 */
	private void fillLine(DrawingTool dTool, long u1, long u2, long v) {
		dTool.drawLine(u1, v, u2, v);
	}

	/**
	 * Pushes the specified (u,v) point on the working stacks.
	 */
	private void push(long u, long v) {
		uStack.push(u);
		vStack.push(v);
	}

	/** Pops a U value of the working U stack. */
	private long popU() {
		return uStack.pop();
	}

	/** Pops a V value of the working V stack. */
	private long popV() {
		return vStack.pop();
	}

	/**
	 * To minimize object creations/deletions we want a stack of primitives
	 */
	private class StackOfLongs {
		private int top;
		private long[] stack;
		
		public StackOfLongs() {
			top = -1;
			stack = new long[100];
		}
		
		public boolean isEmpty() {
			return top < 0;
		}
		
		public void clear() {
			top = -1;
		}
		
		public void push(long value) {
			if (top == stack.length-1) {
				long[] newArray = Arrays.copyOf(stack, stack.length*2);
				stack = newArray;
			}
			top++;
			stack[top] = value;
		}
		
		public long pop() {
			if (top < 0)
				throw new IllegalArgumentException("can't pop empty stack");
			long value = stack[top];
			top--;
			return value;
		}
	}
}
