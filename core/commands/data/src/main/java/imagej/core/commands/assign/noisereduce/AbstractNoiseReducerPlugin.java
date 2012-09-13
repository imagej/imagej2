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
import imagej.module.ItemIO;
import imagej.plugin.Parameter;


public abstract class AbstractNoiseReducerPlugin<U extends RealType<U>>
	// TODO : extends ContextCommand
	implements Command, Cancelable
{
	// -- constants --

	public enum NeighborhoodType {RADIAL, RECTANGULAR}

	private static final String RADIAL_STRING = "Radial (n dimensional)";
	private static final String RECTANGULAR_STRING = "Rectangular (2 dimensional)";
	
	// -- Parameters --

	@Parameter
	protected ImageJ context;
	
	@Parameter
	protected CommandService commandService;
	
	@Parameter
	protected Dataset input;

	@Parameter(label = "Neighborhood type",
			choices = {RADIAL_STRING,RECTANGULAR_STRING})
	protected String neighTypeString = RADIAL_STRING;

	@Parameter(type = ItemIO.OUTPUT)
	protected Dataset output;

	// -- private instance variables --
	
	private NeighborhoodType neighType;
	
	private String cancelReason;

	private Neighborhood neighborhood = null;
	
	// -- public API --
	
	public abstract Function<PointSet,DoubleType> getFunction(
		Function<long[],DoubleType> otherFunc);

	@Override
	public void run() {
		if (neighborhood == null)
			neighborhood = determineNeighborhood(input.numDimensions());
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

	public void setInput(Dataset ds) {
		input = ds;
	}
	
	public Dataset getInput() {
		return input;
	}

	public Dataset getOutput() {
		return output;
	}

	public void setNeighborhood(Neighborhood n) {
		neighborhood = n;
	}
	
	public NeighborhoodType getNeighborhoodType() {
		return neighType;
	}

	public void setNeighborhoodType(NeighborhoodType type) {
		neighType = type;
		setNeighString();
	}
	
	@Override
	public boolean isCanceled() {
		return cancelReason != null;
	}

	@Override
	public String getCancelReason() {
		return cancelReason;
	}

	// -- private helpers --
	
	private Neighborhood determineNeighborhood(int numDims) {
		setNeighType();
		CommandModule<?> module = null;
		try {
			Map<String,Object> inputs = new HashMap<String,Object>();
			inputs.put("numDims", numDims);
			if (neighType == NeighborhoodType.RADIAL) {
				Future<CommandModule<RadialNeighborhoodSpecifier>> futureModule =
						commandService.run(RadialNeighborhoodSpecifier.class, inputs);
				module = futureModule.get();
			}
			else { // neighType == RECTANGULAR
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
	
	private void setNeighString() {
		if (neighType == NeighborhoodType.RADIAL)
			neighTypeString = RADIAL_STRING;
		else
			neighTypeString = RECTANGULAR_STRING;
	}
	
	private void setNeighType() {
		if (neighTypeString.equals(RADIAL_STRING))
			neighType = NeighborhoodType.RADIAL;
		else
			neighType = NeighborhoodType.RECTANGULAR;
	}
}
