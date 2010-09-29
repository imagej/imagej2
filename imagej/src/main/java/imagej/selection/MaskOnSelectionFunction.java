package imagej.selection;

public class MaskOnSelectionFunction implements SelectionFunction {

	private MaskOffSelectionFunction maskOff;
	
	public MaskOnSelectionFunction(int[] maskOrigin, int[] maskSpan, byte[] mask)
	{
		this.maskOff = new MaskOffSelectionFunction(maskOrigin, maskSpan, mask);
	}
	
	public boolean include(int[] position, double sample)
	{
		return ! (maskOff.include(position, 0));
	}

}
