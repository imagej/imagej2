package ij.process;

import mpicbg.imglib.type.numeric.IntegerType;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;
import mpicbg.imglib.type.numeric.integer.UnsignedIntType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;

public class TypeManager {

	// TODO is there a better way? ask.
	//   Note - needed to go from type T to type RealType as our Hudson wouldn't build even though Eclipse can 
	public static boolean isUnsignedType(RealType t) {
		return (
			(t instanceof UnsignedByteType) ||
			(t instanceof UnsignedIntType) ||
			(t instanceof UnsignedShortType)
		);
	}

	// TODO is there a better way? ask.
	//   Note - needed to go from type T to type RealType as our Hudson wouldn't build even though Eclipse can 
	public static boolean isIntegralType(RealType t) {
		return (t instanceof IntegerType);
	}
	
	/**
	 * Limits and returns the range of the input value
	 * to the corresponding max and min values respective to the
	 * underlying type.
	 */
	public static double boundValueToType(RealType type, double inputValue)
	{
		if (isIntegralType(type))
		{
			if (inputValue < type.getMinValue() ) inputValue = type.getMinValue();
			if (inputValue > type.getMaxValue() ) inputValue = type.getMaxValue();
		}

		return inputValue;
	}

}
