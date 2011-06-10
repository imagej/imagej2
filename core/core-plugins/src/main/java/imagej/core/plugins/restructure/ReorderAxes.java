package imagej.core.plugins.restructure;

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
		RestructureUtils.X,
		RestructureUtils.Y,
		RestructureUtils.Z,
		RestructureUtils.CH,
		RestructureUtils.TI,
		RestructureUtils.FR,
		RestructureUtils.SP,
		RestructureUtils.PH,
		RestructureUtils.PO,
		RestructureUtils.LI})
	String axis1;
	
	@Parameter(label="2nd preference",choices = {
		RestructureUtils.X,
		RestructureUtils.Y,
		RestructureUtils.Z,
		RestructureUtils.CH,
		RestructureUtils.TI,
		RestructureUtils.FR,
		RestructureUtils.SP,
		RestructureUtils.PH,
		RestructureUtils.PO,
		RestructureUtils.LI})
	String axis2;
	
	@Parameter(label="3rd preference",choices = {
		RestructureUtils.X,
		RestructureUtils.Y,
		RestructureUtils.Z,
		RestructureUtils.CH,
		RestructureUtils.TI,
		RestructureUtils.FR,
		RestructureUtils.SP,
		RestructureUtils.PH,
		RestructureUtils.PO,
		RestructureUtils.LI})
	String axis3;
	
	@Parameter(label="4th preference",choices = {
		RestructureUtils.X,
		RestructureUtils.Y,
		RestructureUtils.Z,
		RestructureUtils.CH,
		RestructureUtils.TI,
		RestructureUtils.FR,
		RestructureUtils.SP,
		RestructureUtils.PH,
		RestructureUtils.PO,
		RestructureUtils.LI})
	String axis4;
	
	@Parameter(label="5th preference",choices = {
		RestructureUtils.X,
		RestructureUtils.Y,
		RestructureUtils.Z,
		RestructureUtils.CH,
		RestructureUtils.TI,
		RestructureUtils.FR,
		RestructureUtils.SP,
		RestructureUtils.PH,
		RestructureUtils.PO,
		RestructureUtils.LI})
	String axis5;
	
	@Parameter(label="6th preference",choices = {
		RestructureUtils.X,
		RestructureUtils.Y,
		RestructureUtils.Z,
		RestructureUtils.CH,
		RestructureUtils.TI,
		RestructureUtils.FR,
		RestructureUtils.SP,
		RestructureUtils.PH,
		RestructureUtils.PO,
		RestructureUtils.LI})
	String axis6;
	
	@Parameter(label="7th preference",choices = {
		RestructureUtils.X,
		RestructureUtils.Y,
		RestructureUtils.Z,
		RestructureUtils.CH,
		RestructureUtils.TI,
		RestructureUtils.FR,
		RestructureUtils.SP,
		RestructureUtils.PH,
		RestructureUtils.PO,
		RestructureUtils.LI})
	String axis7;
	
	@Parameter(label="8th preference",choices = {
		RestructureUtils.X,
		RestructureUtils.Y,
		RestructureUtils.Z,
		RestructureUtils.CH,
		RestructureUtils.TI,
		RestructureUtils.FR,
		RestructureUtils.SP,
		RestructureUtils.PH,
		RestructureUtils.PO,
		RestructureUtils.LI})
	String axis8;
	
	@Parameter(label="9th preference",choices = {
		RestructureUtils.X,
		RestructureUtils.Y,
		RestructureUtils.Z,
		RestructureUtils.CH,
		RestructureUtils.TI,
		RestructureUtils.FR,
		RestructureUtils.SP,
		RestructureUtils.PH,
		RestructureUtils.PO,
		RestructureUtils.LI})
	String axis9;
	
	@Parameter(label="10th preference",choices = {
		RestructureUtils.X,
		RestructureUtils.Y,
		RestructureUtils.Z,
		RestructureUtils.CH,
		RestructureUtils.TI,
		RestructureUtils.FR,
		RestructureUtils.SP,
		RestructureUtils.PH,
		RestructureUtils.PO,
		RestructureUtils.LI})
	String axis10;
	
	private long[] tmpPos;
	private Axis[] tmpAxes;
	private int[] permutationAxisIndices;
	private Axis[] desiredAxisOrder;
	
	@Override
	public void run() {
		setupDesiredAxisOrder();
		if (inputBad()) return;
		setupPermutationVars();
		ImgPlus<? extends RealType<?>> newImgPlus = reorganizedData();
		input.setImgPlus(newImgPlus);
	}

	private void setupDesiredAxisOrder() {
		desiredAxisOrder = new Axis[]{
			RestructureUtils.getAxis(axis1),
			RestructureUtils.getAxis(axis2),
			RestructureUtils.getAxis(axis3),
			RestructureUtils.getAxis(axis4),
			RestructureUtils.getAxis(axis5),
			RestructureUtils.getAxis(axis6),
			RestructureUtils.getAxis(axis7),
			RestructureUtils.getAxis(axis8),
			RestructureUtils.getAxis(axis9),
			RestructureUtils.getAxis(axis10)
		};
	}
	
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

	private Axis[] getPermutedAxes(Axis[] currAxes) {
		Axis[] permuted = new Axis[currAxes.length];
		int index = 0;
		for (int i = 0; i < permuted.length; i++) {
			for (int j = 0; j < desiredAxisOrder.length; j++)
				if (currAxes[i] == desiredAxisOrder[j]) {
					permuted[index++] = currAxes[i];
					break;
				}
		}
		return permuted;
	}
	
	private void setupPermutationVars() {
		Axis[] currAxes = input.getAxes();
		Axis[] permutedAxes = getPermutedAxes(currAxes);
		tmpPos = new long[currAxes.length];
		tmpAxes = new Axis[currAxes.length];
		permutationAxisIndices = new int[currAxes.length];
		for (int i = 0; i < currAxes.length; i++) {
			Axis axis = currAxes[i];
			int newIndex = getNewAxisIndex(permutedAxes,axis);
			permutationAxisIndices[i] = newIndex;
		}
	}

	@SuppressWarnings({"rawtypes","unchecked"})
	private ImgPlus<? extends RealType<?>> reorganizedData() {
		RandomAccess<? extends RealType<?>> inputAccessor =
			input.getImgPlus().randomAccess();
		long[] inputOrigin = new long[input.getImgPlus().numDimensions()];
		long[] inputSpan = new long[inputOrigin.length];
		input.getImgPlus().dimensions(inputSpan);
		iter = new RegionIterator(inputAccessor, inputOrigin, inputSpan);
		long[] newDims = getNewDims();
		Axis[] newAxes = getNewAxes();
		ImgPlus<? extends RealType<?>> newImgPlus =
			RestructureUtils.createNewImgPlus(input, newDims, newAxes);
		RandomAccess<? extends RealType<?>> outputAccessor =
			newImgPlus.randomAccess();
		long[] currPos = new long[inputOrigin.length];
		while (iter.hasNext()) {
			RealType<?> value = iter.next();
			iter.getPosition(currPos);
			permute(currPos);
			outputAccessor.setPosition(currPos);
			outputAccessor.get().setReal(value.getRealDouble());
		}
		return newImgPlus;
	}

	private int getNewAxisIndex(Axis[] permutedAxes, Axis originalAxis) {
		for (int i = 0; i < permutedAxes.length; i++) {
			if (permutedAxes[i] == originalAxis)
				return i;
		}
		throw new IllegalArgumentException("axis not found!");
	}
	
	private long[] getNewDims() {
		long[] dims = input.getDims();
		permute(dims);
		return dims;
	}
	
	private Axis[] getNewAxes() {
		Axis[] axes = input.getAxes();
		permute(axes);
		return axes;
	}
	
	private void permute(long[] origPos) {
		for (int i = 0; i < origPos.length; i++)
			tmpPos[permutationAxisIndices[i]] = origPos[i];
		for (int i = 0; i < origPos.length; i++)
			origPos[i] = tmpPos[i];
	}

	private void permute(Axis[] origAxes) {
		for (int i = 0; i < origAxes.length; i++)
			tmpAxes[permutationAxisIndices[i]] = origAxes[i];
		for (int i = 0; i < origAxes.length; i++)
			origAxes[i] = tmpAxes[i];
	}
}
