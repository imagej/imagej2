package imagej.core.plugins.assign;

import java.util.ArrayList;
import java.util.List;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.DefaultDataset;
import imagej.event.EventService;
import imagej.event.StatusEvent;
import imagej.ext.menu.MenuConstants;
import imagej.ext.module.ItemIO;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.util.Log;
import net.imglib2.img.Img;
import net.imglib2.img.ImgPlus;
import net.imglib2.ops.Function;
import net.imglib2.ops.PointSet;
import net.imglib2.ops.function.real.RealAdaptiveMedianFunction;
import net.imglib2.ops.function.real.RealAlphaTrimmedMeanFunction;
import net.imglib2.ops.function.real.RealArithmeticMeanFunction;
import net.imglib2.ops.function.real.RealContraharmonicMeanFunction;
import net.imglib2.ops.function.real.RealGeometricMeanFunction;
import net.imglib2.ops.function.real.RealHarmonicMeanFunction;
import net.imglib2.ops.function.real.RealImageFunction;
import net.imglib2.ops.function.real.RealMaxFunction;
import net.imglib2.ops.function.real.RealMedianFunction;
import net.imglib2.ops.function.real.RealMidpointFunction;
import net.imglib2.ops.function.real.RealMinFunction;
import net.imglib2.ops.image.ImageAssignment;
import net.imglib2.ops.input.PointSetInputIteratorFactory;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory.Boundary;
import net.imglib2.type.numeric.RealType;


@Plugin(menu = {
	@Menu(label = MenuConstants.PROCESS_LABEL,
			weight = MenuConstants.PROCESS_WEIGHT,
			mnemonic = MenuConstants.PROCESS_MNEMONIC),
		@Menu(label = "Noise", mnemonic = 'n'),
		@Menu(label = "Noise Reducer", weight = 4) })
public class NoiseReductionPlugin<T extends RealType<T>> implements ImageJPlugin
{
	// -- constants --
	
	private static final String MEDIAN = "Median";
	private static final String MEAN = "Mean";
	private static final String MIN = "Min";
	private static final String MAX = "Max";
	private static final String MIDPOINT = "Midpoint";
	private static final String ADAPTIVE = "Adaptive median";
	private static final String CONTRAHARMONIC = "Contraharmonic mean";
	private static final String GEOMETRIC = "Geometric mean";
	private static final String HARMONIC = "Harmonic mean";
	private static final String TRIMMED = "Trimmed mean";

	// -- Parameters --
	
	@Parameter(persist = false)
	ImageJ context;
	
	@Parameter
	EventService eventService;
	
	@Parameter(required = true, persist = false)
	Dataset dataset;
	
	@Parameter(label = "Method: ", choices={MEDIAN, MEAN, MIN, MAX, MIDPOINT,
		ADAPTIVE, CONTRAHARMONIC, GEOMETRIC, HARMONIC, TRIMMED})
	String functionName = MEDIAN;

	@Parameter(label="Neighborhood: negative width span", min="0")
	int windowNegWidthSpan = 1;
	
	@Parameter(label="Neighborhood: negative height span", min="0")
	int windowNegHeightSpan = 1;
	
	@Parameter(label="Neighborhood: positive width span", min="0")
	int windowPosWidthSpan = 1;
	
	@Parameter(label="Neighborhood: positive height span", min="0")
	int windowPosHeightSpan = 1;
	
	@Parameter(label="Adaptive median: number of expansions",min="1")
	int windowExpansions = 1;
	
	@Parameter(label="Contraharmonic mean: order", min="1")
	int order = 1;
	
	@Parameter(label="Trimmed mean: count of samples to trim (single end)", min="0")
	int halfTrimCount = 1;

	@Parameter(type=ItemIO.OUTPUT)
	Dataset output = null;

	// -- public interface --
	
	@Override
	public void run() {
		if (dataset.numDimensions() != 2) {
			// TODO - generic warning dialog
			Log.error("This plugin only works with 2d images");
			return;
		}
		updateUser();
		long[] inputDims = dataset.getDims();
		@SuppressWarnings("unchecked")
		ImgPlus<T> origImg = (ImgPlus<T>) dataset.getImgPlus();
		ImgPlus<T> newImg = origImg.copy();
		OutOfBoundsMirrorFactory<T, Img<T>> oobFactory =
				new OutOfBoundsMirrorFactory<T,Img<T>>(Boundary.DOUBLE);
		RealImageFunction<T,T> imageFunc =
				new RealImageFunction<T,T>(origImg, oobFactory, origImg.firstElement());
		Function<PointSet,T> inputFunction = getFunction(imageFunc);
		HyperVolumePointSet neighborhood =
				new HyperVolumePointSet(
					new long[]{0,0},
					new long[]{windowNegWidthSpan, windowNegHeightSpan},
					new long[]{windowPosWidthSpan, windowPosHeightSpan});
		PointSetInputIteratorFactory inputFactory =
				new PointSetInputIteratorFactory(neighborhood);
		long[] outputOrigin = new long[]{0,0};
		long[] outputSpan = new long[]{inputDims[0], inputDims[1]};
		ImageAssignment<T,T,PointSet> assigner =
				new ImageAssignment<T,T,PointSet>(
					newImg,
					outputOrigin,
					outputSpan,
					inputFunction,
					null,
					inputFactory);
		assigner.assign();
		output = new DefaultDataset(context, newImg);
	}

	// -- private interface --
	
	private Function<PointSet,T> getFunction(Function<long[],T> otherFunc) {
		if (functionName.equals(MEDIAN)) {
			return new RealMedianFunction<T>(otherFunc);
		}
		else if (functionName.equals(MEAN)) {
			return new RealArithmeticMeanFunction<T>(otherFunc);
		}
		else if (functionName.equals(MIN)) {
			return new RealMinFunction<T>(otherFunc);
		}
		else if (functionName.equals(MAX)) {
			return new RealMaxFunction<T>(otherFunc);
		}
		else if (functionName.equals(MIDPOINT)) {
			return new RealMidpointFunction<T>(otherFunc);
		}
		else if (functionName.equals(ADAPTIVE)) {
			return new RealAdaptiveMedianFunction<T>(otherFunc, getExpansionWindows());
		}
		else if (functionName.equals(CONTRAHARMONIC)) {
			return new RealContraharmonicMeanFunction<T>(otherFunc, order);
		}
		else if (functionName.equals(GEOMETRIC)) {
			return new RealGeometricMeanFunction<T>(otherFunc);
		}
		else if (functionName.equals(HARMONIC)) {
			return new RealHarmonicMeanFunction<T>(otherFunc);
		}
		else if (functionName.equals(TRIMMED)) {
			return new RealAlphaTrimmedMeanFunction<T>(otherFunc, halfTrimCount);
		}
		else
			throw new IllegalArgumentException("Unknown function: "+functionName);
	}
	
	private List<PointSet> getExpansionWindows() {
		ArrayList<PointSet> pointSets = new ArrayList<PointSet>();
		for (int i = 0; i < windowExpansions; i++) {
			PointSet rect =
					new HyperVolumePointSet(
						new long[]{0,0},
						new long[]{windowNegWidthSpan+i, windowNegHeightSpan+i},
						new long[]{windowPosWidthSpan+i, windowPosHeightSpan+i});
			pointSets.add(rect);
		}
		return pointSets;
	}
	
	private void updateUser() {
		int w = 1 + windowNegWidthSpan + windowPosWidthSpan;
		int h = 1 + windowNegHeightSpan + windowPosHeightSpan;
		String message = functionName + " of "+ w + " X " + h + " neighborhood";
		eventService.publish(new StatusEvent(message));
	}
}
