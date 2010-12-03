package imagej.ij1bridge;

import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import imagej.DataType;
import imagej.ij1bridge.process.ImgLibProcessor;
import imagej.imglib.TypeManager;

/** SampleManager manages the information related to all supported types in ImageJ */
public class SampleManager
{
	//***** constructor **********************************************/
	
	/** make this class uninstantiable */
	private SampleManager() {}

	//***** public interface **********************************************/
	
	/** get the UserType associated with an ImageProcessor */
	public static DataType getUserType(ImageProcessor proc)
	{
		if (proc instanceof ImgLibProcessor<?>)
			return TypeManager.getUserType(((ImgLibProcessor<?>)proc).getType());

		if (proc instanceof ByteProcessor)
			return DataType.UBYTE;

		if (proc instanceof ShortProcessor)
			return DataType.USHORT;

		if (proc instanceof FloatProcessor)
			return DataType.FLOAT;
		
		if (proc instanceof ColorProcessor)
			return DataType.UINT;
		
		throw new IllegalArgumentException("unknown processor type");
	}
	
	
	/** get the UserType associated with an ImagePlus. Calls ImagePlus::getProcessor(). */
	public static DataType getUserType(ImagePlus imp)
	{
		return getUserType(imp.getProcessor());
	}
	
}
