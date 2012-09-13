package imagej.core.commands.assign.noisereduce;


import net.imglib2.ops.function.Function;
import net.imglib2.ops.function.real.RealContraharmonicMeanFunction;
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
		@Menu(label = "Contraharmonic Mean") })
public class NoiseReductionContraharmonicMean<T extends RealType<T>>
	extends AbstractNoiseReducerPlugin<T>
{
	@Parameter(label="Order")
	private double order = 1;
	
	@Override
	public Function<PointSet, DoubleType> getFunction(
		Function<long[], DoubleType> otherFunc)
	{
		return new RealContraharmonicMeanFunction<DoubleType>(otherFunc, order);
	}
	
	public void setOrder(double val) {
		order = val;
	}
	
	public double getOrder() { return order; }
}
