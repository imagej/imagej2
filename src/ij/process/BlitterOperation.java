package ij.process;

import ij.Prefs;
import mpicbg.imglib.type.numeric.RealType;

public class BlitterOperation<T extends RealType<T>> extends DualCursorRoiOperation<T>
{
	private static final double TOL = 0.00000001;

	private static float divideByZeroValue;
	
	static {
		divideByZeroValue = (float)Prefs.getDouble(Prefs.DIV_BY_ZERO_VALUE, Float.POSITIVE_INFINITY);
		if (divideByZeroValue==Float.MAX_VALUE)
			divideByZeroValue = Float.POSITIVE_INFINITY;
	}
	
	int mode;
	boolean isIntegral;
	double transparentValue;
	ImgLibProcessor<?> ip;
	
	double min, max;
	boolean useDBZValue = !Float.isInfinite(divideByZeroValue);
	
	long numPixels;
	long pixelsSoFar;
	long numPixelsInTwentyRows;
	
	BlitterOperation(ImgLibProcessor<T> ip, ImgLibProcessor<T> other, int xloc, int yloc, int mode, double tranVal)
	{
		super(other.getImage(),
				Index.create(2),
				Span.singlePlane(other.getWidth(), other.getHeight(), 2),
				ip.getImage(),
				Index.create(xloc, yloc, ip.getPlanePosition()),
				Span.singlePlane(other.getWidth(), other.getHeight(), ip.getImage().getNumDimensions()));
		this.ip = ip;
		this.mode = mode;
		this.transparentValue = tranVal;
		this.numPixels = other.getNumPixels();
		this.numPixelsInTwentyRows = (this.numPixels * 20) / other.getHeight();
	}
	
	@Override
	public void beforeIteration(RealType<?> type1, RealType<?> type2) {
		this.isIntegral = TypeManager.isIntegralType(type1);
		this.pixelsSoFar= 0;
		this.min = type1.getMinValue();
		this.max = type1.getMaxValue();
	}

	@Override
	public void insideIteration(RealType<?> sample1, RealType<?> sample2) {
		double value;
		switch (this.mode)
		{
			/** dst=src */
			case Blitter.COPY:
				sample2.setReal( sample1.getRealDouble() );
				break;
			
			/** dst=255-src (8-bits and RGB) */
			case Blitter.COPY_INVERTED:
				if (this.isIntegral)
					sample2.setReal( this.max - sample1.getRealDouble() );
				else
					sample2.setReal( sample1.getRealDouble() );
				break;
			
			/** Copies with white pixels transparent. */
			case Blitter.COPY_TRANSPARENT:
				if (this.isIntegral)
				{
					if ( Math.abs( sample1.getRealDouble() - this.transparentValue ) > TOL )
						sample2.setReal( sample1.getRealDouble() );
				}
				break;
			
			/** dst=dst+src */
			case Blitter.ADD:
				value = sample2.getRealDouble() + sample1.getRealDouble();
				if ((this.isIntegral) && (value > this.max))
					value = this.max;
				sample2.setReal( value );
				break;
			
			/** dst=dst-src */
			case Blitter.SUBTRACT:
				value = sample2.getRealDouble() - sample1.getRealDouble();
				if ((this.isIntegral) && (value < this.min))
					value = this.min;
				sample2.setReal( value );
				break;
				
			/** dst=src*src */
			case Blitter.MULTIPLY:
				value = sample2.getRealDouble() * sample1.getRealDouble();
				if ((this.isIntegral) && (value > this.max))
					value = this.max;
				sample2.setReal( value );
				break;
			
			/** dst=dst/src */
			case Blitter.DIVIDE:
				value = sample1.getRealDouble();
				if (value == 0)
				{
					if (this.isIntegral)
						value = this.max;
					else if (this.useDBZValue)
						value = divideByZeroValue;
					else
						value = sample2.getRealDouble() / value;
				}
				else
					value = sample2.getRealDouble() / value;
				if (this.isIntegral)
					value = Math.floor(value);
				sample2.setReal( value );
				break;
			
			/** dst=(dst+src)/2 */
			case Blitter.AVERAGE:
				if (this.isIntegral)
					sample2.setReal( ((int)sample2.getRealDouble() + (int)sample1.getRealDouble()) / 2 );
				else
					sample2.setReal( (sample2.getRealDouble() + sample1.getRealDouble()) / 2.0 );
				break;
			
			/** dst=abs(dst-src) */
			case Blitter.DIFFERENCE:
				sample2.setReal( Math.abs(sample2.getRealDouble() - sample1.getRealDouble()) );
				break;
			
			/** dst=dst AND src */
			case Blitter.AND:
				sample2.setReal( ((int)sample2.getRealDouble()) & ((int)(sample1.getRealDouble())) );
				break;
			
			/** dst=dst OR src */
			case Blitter.OR:
				sample2.setReal( ((int)sample2.getRealDouble()) | ((int)(sample1.getRealDouble())) );
				break;
			
			/** dst=dst XOR src */
			case Blitter.XOR:
				sample2.setReal( ((int)sample2.getRealDouble()) ^ ((int)(sample1.getRealDouble())) );
				break;
			
			/** dst=min(dst,src) */
			case Blitter.MIN:
				if (sample1.getRealDouble() < sample2.getRealDouble())
					sample2.setReal( sample1.getRealDouble() );
				break;
			
			/** dst=max(dst,src) */
			case Blitter.MAX:
				if (sample1.getRealDouble() > sample2.getRealDouble())
					sample2.setReal( sample1.getRealDouble() );
				break;
			
			/** Copies with zero pixels transparent. */
			case Blitter.COPY_ZERO_TRANSPARENT:
				if ( sample1.getRealDouble() != 0 )
					sample2.setReal( sample1.getRealDouble() );
				break;
				
			default:
				throw new IllegalArgumentException("GeneralBlitter::copyBits(): unknown blitter mode - "+this.mode);
		}
		
		this.pixelsSoFar++;

		if (( this.pixelsSoFar % this.numPixelsInTwentyRows) == 0) 
			this.ip.showProgress( (double)this.pixelsSoFar / this.numPixels );
	}

	@Override
	public void afterIteration() {
		this.ip.showProgress(1.0);
	}

}

