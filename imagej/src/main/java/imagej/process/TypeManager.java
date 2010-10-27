package imagej.process;

import mpicbg.imglib.type.numeric.IntegerType;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.Unsigned12BitType;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;
import mpicbg.imglib.type.numeric.integer.UnsignedIntType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;

public class TypeManager {

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
