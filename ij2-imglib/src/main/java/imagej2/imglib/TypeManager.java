package imagej2.imglib;

import imagej2.UserType;
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
		realTypeArray = new RealType[UserType.values().length];

		realTypeArray[UserType.BIT.ordinal()] = new BitType();
		realTypeArray[UserType.BYTE.ordinal()] = new ByteType();
		realTypeArray[UserType.UBYTE.ordinal()] = new UnsignedByteType();
		realTypeArray[UserType.UINT12.ordinal()] = new Unsigned12BitType();
		realTypeArray[UserType.SHORT.ordinal()] = new ShortType();
		realTypeArray[UserType.USHORT.ordinal()] = new UnsignedShortType();
		realTypeArray[UserType.INT.ordinal()] = new IntType();
		realTypeArray[UserType.UINT.ordinal()] = new UnsignedIntType();
		realTypeArray[UserType.FLOAT.ordinal()] = new FloatType();
		realTypeArray[UserType.LONG.ordinal()] = new LongType();
		realTypeArray[UserType.DOUBLE.ordinal()] = new DoubleType();
	}

	/** get an imglib type from a IJ UserType */
	public static RealType<?> getRealType(UserType type)
	{
		return realTypeArray[type.ordinal()];
	}

	/** get the UserType (BYTE,SHORT,UINT,etc.) associated with an ImgLibType. Right now there is a one to one correspondance
	 *  between imglib and IJ. If this changes in the future this method is defined to return the subsample UserType.
	 */
	public static UserType getUserType(RealType<?> imglib)
	{
		for (UserType vType : UserType.values())
		{
			if (realTypeArray[vType.ordinal()].getClass() == imglib.getClass())
				return vType;
		}
		
		throw new IllegalArgumentException("unknown Imglib type : "+imglib.getClass());
	}
	
	/*
	// TODO is there a better way? ask.
	/** returns true if given imglib type is an unsigned type */
	public static boolean isUnsignedType(RealType<?> t) {
		return (
			(t instanceof BitType) ||
			(t instanceof UnsignedByteType) ||      // note that this method could getUserType(realtype) and then get SampleInfo and return isUnsigned()
			(t instanceof Unsigned12BitType) ||
			(t instanceof UnsignedShortType) ||
			(t instanceof UnsignedIntType)
		);
	}

	// TODO is there a better way? ask.
	/** returns true if given imglib type is an integer type */
	public static boolean isIntegralType(RealType<?> t) {
		return ((t instanceof IntegerType<?>) || (t instanceof BitType));
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
