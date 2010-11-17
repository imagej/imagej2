package imagej2.ij1bridge;

import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import imagej2.SampleInfo.ValueType;
import imagej2.ij1bridge.process.ImgLibProcessor;
import imagej2.imglib.TypeManager;

/** SampleManager manages the information related to all supported types in ImageJ */
public class SampleManager
{
	//***** constructor **********************************************/
	
	/** make this class uninstantiable */
	private SampleManager() {}

	//***** public interface **********************************************/
	
	/** get the ValueType associated with an ImageProcessor */
	public static ValueType getValueType(ImageProcessor proc)
	{
		if (proc instanceof ImgLibProcessor<?>)
			return TypeManager.getValueType(((ImgLibProcessor<?>)proc).getType());

		if (proc instanceof ByteProcessor)
			return ValueType.UBYTE;

		if (proc instanceof ShortProcessor)
			return ValueType.USHORT;

		if (proc instanceof FloatProcessor)
			return ValueType.FLOAT;
		
		if (proc instanceof ColorProcessor)
			return ValueType.UINT;
		
		throw new IllegalArgumentException("unknown processor type");
	}
	
	
	/** get the ValueType associated with an ImagePlus. Calls ImagePlus::getProcessor(). */
	public static ValueType getValueType(ImagePlus imp)
	{
		return getValueType(imp.getProcessor());
	}
	
}
