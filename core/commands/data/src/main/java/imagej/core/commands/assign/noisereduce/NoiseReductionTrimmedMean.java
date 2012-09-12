package imagej.core.commands.assign.noisereduce;


import net.imglib2.ops.function.Function;
import net.imglib2.ops.function.real.RealAlphaTrimmedMeanFunction;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import imagej.menu.MenuConstants;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;


@Plugin(menu = {
	@Menu(label = MenuConstants.PROCESS_LABEL,
			weight = MenuConstants.PROCESS_WEIGHT,
			mnemonic = MenuConstants.PROCESS_MNEMONIC),
		@Menu(label = "Noise", mnemonic = 'n'),
		@Menu(label = "Noise Reduction", mnemonic = 'r'),
		@Menu(label = "Trimmed Mean", mnemonic = 't') })
public class NoiseReductionTrimmedMean<T extends RealType<T>>
	extends AbstractNoiseReducerPlugin<T>
	// TODO - do we want to be related to ContextCommand and add accessors?
	// Or does this class need a abstract base that is shared by others?
{
	// -- Parameters --
	
	@Parameter(label = "Numbers of samples to trim (per end)")
	private int halfTrimWidth = 1;

	@Override
	public Function<PointSet, DoubleType> getFunction(
		Function<long[], DoubleType> otherFunc)
	{
		return new RealAlphaTrimmedMeanFunction<DoubleType>(otherFunc, halfTrimWidth);
	}
	
}
