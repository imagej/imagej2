package imagej.imglib;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import imagej.DoubleRange;
import imagej.data.Type;
import imagej.data.Types;
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

	/** the internal list of imglib type data info */
	private static HashMap<Type,RealType<?>> imagejTypeHash;

	//***** static initialization **********************************************/
	
	/** initialize the type lists */
	static
	{
		imagejTypeHash = new HashMap<Type, RealType<?>>();
		imagejTypeHash.put(Types.findType("1-bit unsigned"), new BitType());
		imagejTypeHash.put(Types.findType("8-bit signed"), new ByteType());
		imagejTypeHash.put(Types.findType("8-bit unsigned"), new UnsignedByteType());
		imagejTypeHash.put(Types.findType("12-bit unsigned"), new Unsigned12BitType());
		imagejTypeHash.put(Types.findType("16-bit signed"), new ShortType());
		imagejTypeHash.put(Types.findType("16-bit unsigned"), new UnsignedShortType());
		imagejTypeHash.put(Types.findType("32-bit signed"), new IntType());
		imagejTypeHash.put(Types.findType("32-bit unsigned"), new UnsignedIntType());
		imagejTypeHash.put(Types.findType("32-bit float"), new FloatType());
		imagejTypeHash.put(Types.findType("64-bit signed"), new LongType());
		imagejTypeHash.put(Types.findType("64-bit float"), new DoubleType());
	}

	/** get an imglib type from a IJ UserType */
	public static RealType<?> getRealType(Type type)
	{
		return imagejTypeHash.get(type);
	}

	/** get the ImageJ Type associated with an ImgLibType. Right now there is a one to one correspondance
	 *  between imglib and IJ. If this changes in the future this method will need to be modified.
	 */
	public static Type getIJType(RealType<?> imglibType)
	{
		// TODO - back with a has for quick lookup rather than searching every time
		
		Set<Entry<Type, RealType<?>>> entries = imagejTypeHash.entrySet();
		
		for (Entry<Type,RealType<?>> entry : entries)
		{
			if (entry.getValue().getClass() == imglibType.getClass())
				return entry.getKey();
		}
		
		throw new IllegalArgumentException("unknown Imglib type : "+imglibType.getClass());
	}
	
	/*
	// TODO is there a better way? ask.
	/** returns true if given imglib type is an unsigned type */
	public static boolean isUnsignedType(RealType<?> t) {
		return (
			(t instanceof BitType) ||
			(t instanceof UnsignedByteType) ||      // NOTE - this method could getIJType(realtype) and then return type.isUnsigned()
			(t instanceof Unsigned12BitType) ||
			(t instanceof UnsignedShortType) ||
			(t instanceof UnsignedIntType)
		);
	}

	// TODO is there a better way? ask.
	/** returns true if given imglib type is an integer type */
	public static boolean isIntegralType(RealType<?> t) {      // NOTE - this method could getIJType(realtype) and then return ! type.isFloat()
		return ((t instanceof IntegerType<?>) || (t instanceof BitType));
	}
	
	/**
	 * Limits and returns the range of the input value
	 * to the corresponding max and min values respective to the
	 * underlying type.
	 */
	public static double boundValueToType(RealType<?> type, double inputValue)
	{
		return DoubleRange.bound(type.getMinValue(), type.getMaxValue(), inputValue);
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
		return DoubleRange.inside(type.getMinValue(), type.getMaxValue(), value);
	}
}
