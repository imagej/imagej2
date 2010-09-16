package imagej.process.operation;

import imagej.process.Span;
import imagej.process.TypeManager;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public class SetPlaneOperation<T extends RealType<T>> extends PositionalRoiOperation<T>
{
	public static enum PixelType {BYTE,SHORT,INT,FLOAT,DOUBLE,LONG};

	// set in constructor
	private Object pixels;
	private PixelType pixType;
	private boolean isUnsigned;
	
	// set before iteration
	private int pixNum;
	private RealType<?> type;
	private boolean isIntegral;
	
	public SetPlaneOperation(Image<T> theImage, int[] origin, Object pixels, PixelType inputType, boolean isUnsigned)
	{
		super(theImage, origin, Span.singlePlane(theImage.getDimension(0), theImage.getDimension(1), theImage.getNumDimensions()));
		this.pixels = pixels;
		this.pixType = inputType;
		this.isUnsigned = isUnsigned;
	}
	
	@Override
	public void beforeIteration(RealType<T> type) {
		this.pixNum = 0;
		this.type = type;
		this.isIntegral = TypeManager.isIntegralType(type);
	}

	@Override
	public void insideIteration(int[] position, RealType<T> sample) {

		double inputPixValue = getPixValue(pixels, pixType, isUnsigned, this.pixNum++);
		
		if (this.isIntegral)
			inputPixValue = TypeManager.boundValueToType(this.type, inputPixValue);
		
		sample.setReal(inputPixValue);
	}

	@Override
	public void afterIteration() {
	}
	
	private double getPixValue(Object pixels, PixelType inputType, boolean unsigned, int pixNum)
	{
		switch (inputType) {
			case BYTE:
				byte b = ((byte[])pixels)[pixNum];
				if ((unsigned) && (b < 0))
					return 256.0 + b;
				else
					return b;
			case SHORT:
				short s = ((short[])pixels)[pixNum];
				if ((unsigned) && (s < 0))
					return 65536.0 + s;
				else
					return s;
			case INT:
				int i = ((int[])pixels)[pixNum];
				if ((unsigned) && (i < 0))
					return 4294967296.0 + i;
				else
					return i;
			case FLOAT:
				return ((float[])pixels)[pixNum];
			case DOUBLE:
				return ((double[])pixels)[pixNum];
			case LONG:
				return ((long[])pixels)[pixNum];  // TODO : possible precision loss here. Also unsigned not supported here.
			default:
				throw new IllegalArgumentException("unknown pixel type");
		}
	}

}

