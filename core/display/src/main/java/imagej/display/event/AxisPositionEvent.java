package imagej.display.event;

import net.imglib2.img.Axis;
import imagej.display.Display;

public class AxisPositionEvent extends DisplayEvent {

	private Axis axis;
	private long value;
	private long max;
	private boolean relative;
	
	public AxisPositionEvent(Display display, Axis axis, long value, long max, boolean relative)
	{
		super(display);
		this.axis = axis;
		this.value = value;
		this.max = max;
		this.relative = relative;
	}

	public Axis getAxis() { return axis; }
	
	public long getValue() { return value; }

	public long getMax() { return max; }
	
	public boolean isRelative() { return relative; }
}
