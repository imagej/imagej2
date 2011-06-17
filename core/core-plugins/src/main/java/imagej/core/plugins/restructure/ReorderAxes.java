package imagej.core.plugins.restructure;

import imagej.core.plugins.axispos.AxisUtils;
import imagej.data.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.util.Log;
import net.imglib2.RandomAccess;
import net.imglib2.img.Axis;
import net.imglib2.img.ImgPlus;
import net.imglib2.ops.operation.RegionIterator;
import net.imglib2.type.numeric.RealType;

// TODO
// - make a nicer UI that doesn't show all axes but just those present in
//     Dataset. This capability would be useful in all the restructure plugins.
// - make the "choices" arrays somehow reuse a static array in RestructureUtils
// - if must keep all axes in UI then only make user specify the 1st N that
//     match their Dataset at the moment
// - can reorder X & Y out of 1st two positions. This could be useful in future
//     but might need to block right now. Similarly the DeleteAxis plugin can
//     totally delete X & Y I think.

/** changes the internal ImgPlus of a Dataset so that its data stays the same
 * but the order of the axes is changed.
 */
@Plugin(menu = {
	@Menu(label = "Image", mnemonic = 'i'),
	@Menu(label = "Stacks", mnemonic = 's'),
	@Menu(label = "Reorder Axes") })
public class ReorderAxes implements ImageJPlugin {
	RegionIterator<?> iter;
	
	@Parameter(required = true)
	private Dataset input;

	@Parameter(label="1st preference",choices = {
		AxisUtils.X,
		AxisUtils.Y,
		AxisUtils.Z,
		AxisUtils.CH,
		AxisUtils.TI,
		AxisUtils.FR,
		AxisUtils.SP,
		AxisUtils.PH,
		AxisUtils.PO,
		AxisUtils.LI})
	String axis1;
	
	@Parameter(label="2nd preference",choices = {
		AxisUtils.X,
		AxisUtils.Y,
		AxisUtils.Z,
		AxisUtils.CH,
		AxisUtils.TI,
		AxisUtils.FR,
		AxisUtils.SP,
		AxisUtils.PH,
		AxisUtils.PO,
		AxisUtils.LI})
	String axis2;
	
	@Parameter(label="3rd preference",choices = {
		AxisUtils.X,
		AxisUtils.Y,
		AxisUtils.Z,
		AxisUtils.CH,
		AxisUtils.TI,
		AxisUtils.FR,
		AxisUtils.SP,
		AxisUtils.PH,
		AxisUtils.PO,
		AxisUtils.LI})
	String axis3;
	
	@Parameter(label="4th preference",choices = {
		AxisUtils.X,
		AxisUtils.Y,
		AxisUtils.Z,
		AxisUtils.CH,
		AxisUtils.TI,
		AxisUtils.FR,
		AxisUtils.SP,
		AxisUtils.PH,
		AxisUtils.PO,
		AxisUtils.LI})
	String axis4;
	
	@Parameter(label="5th preference",choices = {
		AxisUtils.X,
		AxisUtils.Y,
		AxisUtils.Z,
		AxisUtils.CH,
		AxisUtils.TI,
		AxisUtils.FR,
		AxisUtils.SP,
		AxisUtils.PH,
		AxisUtils.PO,
		AxisUtils.LI})
	String axis5;
	
	@Parameter(label="6th preference",choices = {
		AxisUtils.X,
		AxisUtils.Y,
		AxisUtils.Z,
		AxisUtils.CH,
		AxisUtils.TI,
		AxisUtils.FR,
		AxisUtils.SP,
		AxisUtils.PH,
		AxisUtils.PO,
		AxisUtils.LI})
	String axis6;
	
	@Parameter(label="7th preference",choices = {
		AxisUtils.X,
		AxisUtils.Y,
		AxisUtils.Z,
		AxisUtils.CH,
		AxisUtils.TI,
		AxisUtils.FR,
		AxisUtils.SP,
		AxisUtils.PH,
		AxisUtils.PO,
		AxisUtils.LI})
	String axis7;
	
	@Parameter(label="8th preference",choices = {
		AxisUtils.X,
		AxisUtils.Y,
		AxisUtils.Z,
		AxisUtils.CH,
		AxisUtils.TI,
		AxisUtils.FR,
		AxisUtils.SP,
		AxisUtils.PH,
		AxisUtils.PO,
		AxisUtils.LI})
	String axis8;
	
	@Parameter(label="9th preference",choices = {
		AxisUtils.X,
		AxisUtils.Y,
		AxisUtils.Z,
		AxisUtils.CH,
		AxisUtils.TI,
		AxisUtils.FR,
		AxisUtils.SP,
		AxisUtils.PH,
		AxisUtils.PO,
		AxisUtils.LI})
	String axis9;
	
	@Parameter(label="10th preference",choices = {
		AxisUtils.X,
		AxisUtils.Y,
		AxisUtils.Z,
		AxisUtils.CH,
		AxisUtils.TI,
		AxisUtils.FR,
		AxisUtils.SP,
		AxisUtils.PH,
		AxisUtils.PO,
		AxisUtils.LI})
	String axis10;
	
	private int[] permutationAxisIndices;
	private Axis[] desiredAxisOrder;

	/** run the plugin and reorder axes as specified by user */
	@Override
	public void run() {
		setupDesiredAxisOrder();
		if (inputBad()) return;
		setupPermutationVars();
		ImgPlus<? extends RealType<?>> newImgPlus = getReorganizedData();
		//reportDims(input.getImgPlus());
		//reportDims(newImgPlus);
		int count = input.getCompositeChannelCount();
		input.setImgPlus(newImgPlus);
		input.setCompositeChannelCount(count);
	}

	// -- helpers --
	
	/*
	private void reportDims(ImgPlus<?> imgPlus) {
		System.out.println("Dimension report");
		long[] dims = new long[imgPlus.numDimensions()];
		imgPlus.dimensions(dims);
		Axis[] axes = new Axis[dims.length];
		imgPlus.axes(axes);
		for (int i = 0; i < dims.length; i++) {
			System.out.println(dims[i]+" "+axes[i]);
		}
		System.out.println();
	}
	*/
	
	/** fills the internal variable "desiredAxisOrder" with the order of axes
	 * that the user specified in the dialog. all axes are present rather than
	 * just those present in the input Dataset. */
	private void setupDesiredAxisOrder() {
		desiredAxisOrder = new Axis[]{
			AxisUtils.getAxis(axis1),
			AxisUtils.getAxis(axis2),
			AxisUtils.getAxis(axis3),
			AxisUtils.getAxis(axis4),
			AxisUtils.getAxis(axis5),
			AxisUtils.getAxis(axis6),
			AxisUtils.getAxis(axis7),
			AxisUtils.getAxis(axis8),
			AxisUtils.getAxis(axis9),
			AxisUtils.getAxis(axis10)
		};
	}
	
	/** returns true if user input is invalid. Basically this is a test that the
	 * user did not repeat any axis when specifying the axis ordering.
	 */
	private boolean inputBad() {
		for (int i = 0; i < desiredAxisOrder.length; i++)
			for (int j = i+1; j < desiredAxisOrder.length; j++)
				if (desiredAxisOrder[i] == desiredAxisOrder[j]) {
					Log.error("at least one axis preference is repeated:" +
							" axis preferences must be mututally exclusive");
					return true;
				}
		return false;
	}

	/** takes a given set of axes (usually a subset of all possible axes) and
	 * returns a permuted set of axes that reflect the user specified axis order
	 */
	private Axis[] getPermutedAxes(Axis[] currAxes) {
		Axis[] permuted = new Axis[currAxes.length];
		int index = 0;
		for (int i = 0; i < desiredAxisOrder.length; i++)
			for (int j = 0; j < currAxes.length; j++) {
				if (currAxes[j] == desiredAxisOrder[i]) {
					permuted[index++] = currAxes[j];
					break;
				}
		}
		return permuted;
	}

	/** sets up the working variable "permutationAxisIndices" which is used to
	 * actually permute positions.
	 */
	private void setupPermutationVars() {
		Axis[] currAxes = input.getAxes();
		Axis[] permutedAxes = getPermutedAxes(currAxes);
		permutationAxisIndices = new int[currAxes.length];
		for (int i = 0; i < currAxes.length; i++) {
			Axis axis = currAxes[i];
			int newIndex = getNewAxisIndex(permutedAxes, axis);
			permutationAxisIndices[i] = newIndex;
		}
	}

	/** returns an ImgPlus that has same data values as the input Dataset but
	 * which has them stored in a different axis order */
	@SuppressWarnings({"rawtypes","unchecked"})
	private ImgPlus<? extends RealType<?>> getReorganizedData() {
		RandomAccess<? extends RealType<?>> inputAccessor =
			input.getImgPlus().randomAccess();
		long[] inputOrigin = new long[input.getImgPlus().numDimensions()];
		long[] inputSpan = new long[inputOrigin.length];
		input.getImgPlus().dimensions(inputSpan);
		iter = new RegionIterator(inputAccessor, inputOrigin, inputSpan);
		long[] origDims = input.getDims();
		Axis[] origAxes = input.getAxes();
		long[] newDims = getNewDims(origDims);
		Axis[] newAxes = getNewAxes(origAxes);
		ImgPlus<? extends RealType<?>> newImgPlus =
			RestructureUtils.createNewImgPlus(input, newDims, newAxes);
		RandomAccess<? extends RealType<?>> outputAccessor =
			newImgPlus.randomAccess();
		long[] currPos = new long[inputOrigin.length];
		long[] permutedPos = new long[inputOrigin.length];
		while (iter.hasNext()) {
			RealType<?> valueRef = iter.next();
			double value = valueRef.getRealDouble();
			iter.getPosition(currPos);
			permute(currPos, permutedPos);
			outputAccessor.setPosition(permutedPos);
			outputAccessor.get().setReal(value);
		}
		return newImgPlus;
	}

	/** returns the axis index of an Axis given a permuted set of axes. */
	private int getNewAxisIndex(Axis[] permutedAxes, Axis originalAxis) {
		for (int i = 0; i < permutedAxes.length; i++) {
			if (permutedAxes[i] == originalAxis)
				return i;
		}
		throw new IllegalArgumentException("axis not found!");
	}
	
	/** taking the original dims this method returns the new dimensions of the
	 * permuted space.
	 */
	private long[] getNewDims(long[] origDims) {
		long[] newDims = new long[origDims.length];
		permute(origDims, newDims);
		return newDims;
	}
	
	/** taking the original axes order this method returns the new axes in the
	 * order of the permuted space.
	 */
	private Axis[] getNewAxes(Axis[] origAxes) {
		Axis[] newAxes = new Axis[origAxes.length];
		permute(origAxes, newAxes);
		return newAxes;
	}

	/** permutes from a position in the original space into a position in the
	 * permuted space
	 */
	private void permute(long[] origPos, long[] permutedPos) {
		for (int i = 0; i < origPos.length; i++)
			permutedPos[permutationAxisIndices[i]] = origPos[i];
	}

	/** permutes from an axis order in the original space into an axis order in
	 * the permuted space
	 */
	private void permute(Axis[] origAxes, Axis[] permutedAxes) {
		for (int i = 0; i < origAxes.length; i++)
			permutedAxes[permutationAxisIndices[i]] = origAxes[i];
	}
}
