package imagej.process;

import mpicbg.imglib.type.numeric.IntegerType;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.ByteType;
import mpicbg.imglib.type.numeric.integer.IntType;
import mpicbg.imglib.type.numeric.integer.LongType;
import mpicbg.imglib.type.numeric.integer.ShortType;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;
import mpicbg.imglib.type.numeric.integer.UnsignedIntType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;
import mpicbg.imglib.type.numeric.real.DoubleType;
import mpicbg.imglib.type.numeric.real.FloatType;

public class TypeManager {

	// TODO is there a better way? ask.
	/** returns true if given imglib type is an unsigned type */
	public static boolean isUnsignedType(RealType<?> t) {
		return (
			(t instanceof UnsignedByteType) ||
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
		// NOTE
		//   Would like to do this but disallowed;
		//     type1 instanceof type2 && type2 instanceof type1
		//   There maybe a way to do it but I'm not sure how at the moment.
		//   Unfortunately this means every time we add a type to ImageJ we need to edit this method.
		
		if ((type1 instanceof ByteType) && (type2 instanceof ByteType))
		 	return true;
		
		else if ((type1 instanceof UnsignedByteType) && (type2 instanceof UnsignedByteType))
		 	return true;
		
		else if ((type1 instanceof ShortType) && (type2 instanceof ShortType))
			return true;
		
		else if ((type1 instanceof UnsignedShortType) && (type2 instanceof UnsignedShortType))
			return true;
		
		else if ((type1 instanceof IntType) && (type2 instanceof IntType))
			return true;
		
		else if ((type1 instanceof UnsignedIntType) && (type2 instanceof UnsignedIntType))
			return true;
		
		else if ((type1 instanceof LongType) && (type2 instanceof LongType))
			return true;
		
		else if ((type1 instanceof FloatType) && (type2 instanceof FloatType))
			return true;
		
		else if ((type1 instanceof DoubleType) && (type2 instanceof DoubleType))
			return true;
		
		return false;
	}
	
	/** returns true if a value is within the valid range defined for an imglib type */
	public static boolean validValue(RealType<?> type, double value)
	{
		return (value >= type.getMinValue()) && (value <= type.getMaxValue());
	}
}
