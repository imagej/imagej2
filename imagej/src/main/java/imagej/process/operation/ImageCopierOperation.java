package imagej.process.operation;

import imagej.process.function.CopyUnaryFunction;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public class ImageCopierOperation<K extends RealType<K>> extends BinaryAssignOperation<K>
{
	public ImageCopierOperation(Image<K> srcImg, int[] srcOrigin, int[] srcSpan,
				Image<K> dstImg, int[] dstOrigin, int[] dstSpan)
	{
		super(dstImg, dstOrigin, dstSpan, srcImg, srcOrigin, srcSpan, new CopyUnaryFunction());
	}
}
