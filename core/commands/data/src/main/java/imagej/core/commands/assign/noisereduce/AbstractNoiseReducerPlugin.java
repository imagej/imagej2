package imagej.core.commands.assign.noisereduce;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import net.imglib2.img.Img;
import net.imglib2.img.ImgPlus;
import net.imglib2.ops.function.Function;
import net.imglib2.ops.function.real.RealImageFunction;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory.Boundary;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import imagej.Cancelable;
import imagej.ImageJ;
import imagej.command.Command;
import imagej.command.CommandModule;
import imagej.command.CommandService;
import imagej.data.Dataset;
import imagej.plugin.Parameter;


public abstract class AbstractNoiseReducerPlugin<U extends RealType<U>>
	implements Command, Cancelable
{
	private static final String RADIAL = "Radial";
	private static final String RECTANGULAR = "Rectangular";
	
	@Parameter
	private ImageJ context;
	
	@Parameter
	private CommandService commandService;
	
	@Parameter
	private Dataset input;

	@Parameter(label = "Neighborhood type", choices = {RADIAL,RECTANGULAR})
	private String neighType = RADIAL;

	@Parameter
	private Dataset output;
	
	private String cancelReason;

	public abstract Function<PointSet,DoubleType> getFunction(
		Function<long[],DoubleType> otherFunc);

	@Override
	public boolean isCanceled() {
		return cancelReason != null;
	}

	@Override
	public String getCancelReason() {
		return cancelReason;
	}

	public void setInput(Dataset ds) {
		input = ds;
	}
	
	public Dataset getInput() {
		return input;
	}
	
	@Override
	public void run() {
		Neighborhood neighborhood = determineNeighborhood(input.numDimensions());
		if (neighborhood == null) return;
		ImgPlus<U> inputImg = (ImgPlus<U>) input.getImgPlus();
		OutOfBoundsMirrorFactory<U, Img<U>> oobFactory =
				new OutOfBoundsMirrorFactory<U,Img<U>>(Boundary.DOUBLE);
		Function<long[],DoubleType> otherFunc =
				new RealImageFunction<U,DoubleType>(inputImg, oobFactory, new DoubleType());
		PointSet ps = neighborhood.getPoints();
		Reducer<U,DoubleType> reducer =
				new Reducer<U,DoubleType>(context, inputImg, getFunction(otherFunc), ps);
		output = reducer.reduceNoise(neighborhood.getDescription());
	}

	private Neighborhood determineNeighborhood(int numDims) {
		CommandModule<?> module = null;
		try {
			Map<String,Object> inputs = new HashMap<String,Object>();
			inputs.put("numDims", numDims);
			if (neighType.equals(RADIAL)) {
				Future<CommandModule<RadialNeighborhoodSpecifier>> futureModule =
						commandService.run(RadialNeighborhoodSpecifier.class, inputs);
				module = futureModule.get();
			}
			else {
				Future<CommandModule<RectangularNeighborhoodSpecifier>> futureModule =
						commandService.run(RectangularNeighborhoodSpecifier.class, inputs);
				module = futureModule.get();
			}
		} catch (Exception e) {
			cancelReason = e.getMessage();
			return null;
		}
		// unnecessary:
		//module.run();
		if (module.isCanceled()) {
			cancelReason = "Neighborhood specification cancelled by user";
			return null;
		}
		return (Neighborhood) module.getOutputs().get("neighborhood");
	}
}
