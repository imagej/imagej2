package imagej2.imglib;

import imagej2.SampleInfo.ValueType;
import mpicbg.imglib.type.logic.BitType;
import mpicbg.imglib.type.numeric.IntegerType;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.ByteType;
import mpicbg.imglib.type.numeric.integer.IntType;
import mpicbg.imglib.type.numeric.integer.LongType;
import mpicbg.imglib.type.numeric.integer.ShortType;
import mpicbg.imglib.type.numeric.integer.Unsigned12BitType;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;
import mpicbg.imglib.type.numeric.integer.UnsignedIntType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;
import mpicbg.imglib.type.numeric.real.DoubleType;
import mpicbg.imglib.type.numeric.real.FloatType;

public class TypeManager {

	@SuppressWarnings("rawtypes")
	/** the internal list of imglib type data info */
	private static RealType[] realTypeArray;

	//***** static initialization **********************************************/
	
	/** initialize the type lists */
	static
	{
		realTypeArray = new RealType[ValueType.values().length];

		realTypeArray[ValueType.BIT.ordinal()] = new BitType();
		realTypeArray[ValueType.BYTE.ordinal()] = new ByteType();
		realTypeArray[ValueType.UBYTE.ordinal()] = new UnsignedByteType();
		realTypeArray[ValueType.SHORT.ordinal()] = new ShortType();
		realTypeArray[ValueType.USHORT.ordinal()] = new UnsignedShortType();
		realTypeArray[ValueType.INT.ordinal()] = new IntType();
		realTypeArray[ValueType.UINT.ordinal()] = new UnsignedIntType();
		realTypeArray[ValueType.FLOAT.ordinal()] = new FloatType();
		realTypeArray[ValueType.LONG.ordinal()] = new LongType();
		realTypeArray[ValueType.DOUBLE.ordinal()] = new DoubleType();
		realTypeArray[ValueType.UINT12.ordinal()] = new Unsigned12BitType();
	}

	/** get an imglib type from a IJ ValueType */
	public static RealType<?> getRealType(ValueType type)
	{
		return realTypeArray[type.ordinal()];
	}

	/** get the ValueType (BYTE,SHORT,UINT,etc.) associated with an ImgLibType. Right now there is a one to one correspondance
	 *  between imglib and IJ. If this changes in the future this method is defined to return the subsample ValueType.
	 */
	public static ValueType getValueType(RealType<?> imglib)
	{
		for (ValueType vType : ValueType.values())
		{
			if (realTypeArray[vType.ordinal()].getClass() == imglib.getClass())
				return vType;
		}
		
		throw new IllegalArgumentException("unknown Imglib type : "+imglib.getClass());
	}
	
	// TODO is there a better way? ask.
	/** returns true if given imglib type is an unsigned type */
	public static boolean isUnsignedType(RealType<?> t) {
		return (
			(t instanceof UnsignedByteType) ||
			(t instanceof Unsigned12BitType) ||
			(t instanceof UnsignedIntType) ||
			(t instanceof UnsignedShortType)
		);
	}

	// TODO is there a better way? ask.
	/** returns true if given imglib type is an integer type */
	public static boolean isIntegralType(RealType<?> t) {
		return (t instanceof IntegerType<?>);
	}
	
	/**
	 * Limits and returns the range of the input value
	 * to the corresponding max and min values respective to the
	 * underlying type.
	 */
	public static double boundValueToType(RealType<?> type, double inputValue)
	{
		if (inputValue < type.getMinValue() ) inputValue = type.getMinValue();
		
		if (inputValue > type.getMaxValue() ) inputValue = type.getMaxValue();

		return inputValue;
	}

    /** returns true if two imglib types are strictly compatible */
    public static boolean sameKind(RealType<?> type1, RealType<?> type2)
    {
        Class<?> type1Class = type1.getClass();
        Class<?> type2Class = type2.getClass();
        
        return type1Class.equals(type2Class);
    }

    /** returns true if a value is within the valid range defined for an imglib type */
	public static boolean validValue(RealType<?> type, double value)
	{
		return (value >= type.getMinValue()) && (value <= type.getMaxValue());
	}
}
