package imagej.imglib.process.operation;

import imagej.EncodingManager;
import imagej.UserType;
import imagej.primitive.BitWriter;
import imagej.primitive.ByteWriter;
import imagej.primitive.DataWriter;
import imagej.primitive.DoubleWriter;
import imagej.primitive.FloatWriter;
import imagej.primitive.IntWriter;
import imagej.primitive.LongWriter;
import imagej.primitive.ShortWriter;
import imagej.primitive.UnsignedByteWriter;
import imagej.primitive.UnsignedIntWriter;
import imagej.primitive.UnsignedShortWriter;
import imagej.primitive.UnsignedTwelveBitWriter;
import imagej.process.Index;
import imagej.process.Span;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;


public class GetPlaneOperation<T extends RealType<T>> extends PositionalSingleCursorRoiOperation<T>
{
	// *********** instance variables ******************************************************
	
	private int originX, originY, spanX, spanY;
	private UserType asType;
	private Object outputPlane;
	private DataWriter planeWriter;

	// ************ public interface - private declarations later **************************
	
	public GetPlaneOperation(Image<T> image, int[] origin, int[] span, UserType asType)
	{
		super(image, origin, span);
		this.originX = origin[0];
		this.originY = origin[1];
		this.spanX = span[0];
		this.spanY = span[1];
		this.asType = asType;
	}

	@Override
	protected void beforeIteration(RealType<T> type)
	{
		int planeSize = this.spanX * this.spanY;
		
		this.outputPlane = EncodingManager.allocateCompatibleArray(this.asType, planeSize);

		switch (this.asType)
		{
			case BIT:
				this.planeWriter = new BitWriter(this.outputPlane);
				break;

			case BYTE:
				this.planeWriter = new ByteWriter(this.outputPlane);
				break;
				
			case UBYTE:
				this.planeWriter = new UnsignedByteWriter(this.outputPlane);
				break;
		
			case UINT12:
				this.planeWriter = new UnsignedTwelveBitWriter(this.outputPlane);
				break;
		
			case SHORT:
				this.planeWriter = new ShortWriter(this.outputPlane);
				break;
		
			case USHORT:
				this.planeWriter = new UnsignedShortWriter(this.outputPlane);
				break;
		
			case INT:
				this.planeWriter = new IntWriter(this.outputPlane);
				break;
		
			case UINT:
				this.planeWriter = new UnsignedIntWriter(this.outputPlane);
				break;
		
			case FLOAT:
				this.planeWriter = new FloatWriter(this.outputPlane);
				break;
		
			case LONG:
				this.planeWriter = new LongWriter(this.outputPlane);
				break;
		
			case DOUBLE:
				this.planeWriter = new DoubleWriter(this.outputPlane);
				break;
		
			default:
				throw new IllegalStateException("GetPlaneOperation(): unsupported data type - "+this.asType);
		}
	}

	@Override
	protected void insideIteration(int[] position, RealType<T> sample)
	{
		int index = (position[1] - this.originY) * this.spanX + (position[0] - this.originX); 
		
		this.planeWriter.setValue(index, sample.getRealDouble());
	}

	@Override
	protected void afterIteration()
	{
	}
	
	public Object getOutputPlane()
	{
		return this.outputPlane;
	}

	public static <T extends RealType<T>> Object getPlaneAs(Image<T> img, int[] planePos, UserType asType)
	{
		int[] origin = Index.create(0,0,planePos);
		
		int[] span = Span.singlePlane(img.getDimension(0), img.getDimension(1), img.getNumDimensions());
		
		GetPlaneOperation<T> operation = new GetPlaneOperation<T>(img, origin, span, asType);
	
		operation.execute();
		
		return operation.getOutputPlane();
	}
}
