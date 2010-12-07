package imagej.ij1bridge;

import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import imagej.data.Type;
import imagej.data.Types;
import imagej.ij1bridge.process.ImgLibProcessor;
import imagej.imglib.TypeManager;

/** SampleManager manages the information related to all supported types in ImageJ */
public class SampleManager
{
	//***** constructor **********************************************/
	
	/** make this class uninstantiable */
	private SampleManager() {}

	//***** public interface **********************************************/
	
	/** get the Type associated with an ImageProcessor */
	public static Type getType(ImageProcessor proc)
	{
		if (proc instanceof ImgLibProcessor<?>)
			return TypeManager.getIJType(((ImgLibProcessor<?>)proc).getType());

		if (proc instanceof ByteProcessor)
			return Types.findType("8-bit unsigned");

		if (proc instanceof ShortProcessor)
			return Types.findType("16-bit unsigned");

		if (proc instanceof FloatProcessor)
			return Types.findType("32-bit float");
		
		if (proc instanceof ColorProcessor)
			return Types.findType("32-bit unsigned");
		
		throw new IllegalArgumentException("unknown processor type");
	}
	
	
	/** get the Type associated with an ImagePlus. Calls ImagePlus::getProcessor(). */
	public static Type getType(ImagePlus imp)
	{
		return getType(imp.getProcessor());
	}
	
}
