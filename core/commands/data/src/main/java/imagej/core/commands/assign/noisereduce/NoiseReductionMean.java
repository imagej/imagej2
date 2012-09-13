package imagej.core.commands.assign.noisereduce;


import net.imglib2.ops.function.Function;
import net.imglib2.ops.function.real.RealArithmeticMeanFunction;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import imagej.menu.MenuConstants;
import imagej.plugin.Menu;
import imagej.plugin.Plugin;


@Plugin(menu = {
	@Menu(label = MenuConstants.PROCESS_LABEL,
			weight = MenuConstants.PROCESS_WEIGHT,
			mnemonic = MenuConstants.PROCESS_MNEMONIC),
		@Menu(label = "Noise", mnemonic = 'n'),
		@Menu(label = "Noise Reduction", mnemonic = 'r'),
		@Menu(label = "Mean") })
public class NoiseReductionMean<T extends RealType<T>>
	extends AbstractNoiseReducerPlugin<T>
{
	// -- Parameters --
	
	@Override
	public Function<PointSet, DoubleType> getFunction(
		Function<long[], DoubleType> otherFunc)
	{
		return new RealArithmeticMeanFunction<DoubleType>(otherFunc);
	}
	
}
