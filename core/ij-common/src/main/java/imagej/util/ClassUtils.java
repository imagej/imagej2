package imagej.util;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * TODO
 *
 * @author Curtis Rueden
 */
public final class ClassUtils {

	private ClassUtils() {
		// prevent instantiation of utility class
	}

	/** Converts the given string to a {@link Number}. */
	public static Number toNumber(String num, Class<?> type) {
		if (num.isEmpty()) return null;
		if (isByte(type) || isShort(type) || isInteger(type)) {
			return new Integer(num);
		}
		if (isLong(type)) return new Long(num);
		if (isFloat(type)) return new Float(num);
		if (isDouble(type)) return new Double(num);
		if (BigInteger.class.isAssignableFrom(type)) return new BigInteger(num);
		if (BigDecimal.class.isAssignableFrom(type)) return new BigDecimal(num);
		return null;
	}
	
	public static Number getMinimumNumber(Class<?> type) {
		if (isByte(type)) return Byte.MIN_VALUE;
		if (isShort(type)) return Short.MIN_VALUE;
		if (isInteger(type)) return Integer.MIN_VALUE;
		if (isLong(type)) return Long.MIN_VALUE;
		if (isFloat(type)) return -Float.MAX_VALUE;
		if (isDouble(type)) return -Double.MAX_VALUE;
		return Double.NEGATIVE_INFINITY;
	}

	public static Number getMaximumNumber(Class<?> type) {
		if (isByte(type)) return Byte.MAX_VALUE;
		if (isShort(type)) return Short.MAX_VALUE;
		if (isInteger(type)) return Integer.MAX_VALUE;
		if (isLong(type)) return Long.MAX_VALUE;
		if (isFloat(type)) return Float.MAX_VALUE;
		if (isDouble(type)) return Double.MAX_VALUE;
		return Double.POSITIVE_INFINITY;
	}

	public static boolean isBoolean(Class<?> type) {
		return Boolean.class.isAssignableFrom(type) ||
			boolean.class.isAssignableFrom(type);
	}

	public static boolean isByte(Class<?> type) {
		return Byte.class.isAssignableFrom(type) ||
			byte.class.isAssignableFrom(type);
	}

	public static boolean isCharacter(Class<?> type) {
		return Character.class.isAssignableFrom(type) ||
			char.class.isAssignableFrom(type);
	}

	public static boolean isDouble(Class<?> type) {
		return Double.class.isAssignableFrom(type) ||
			double.class.isAssignableFrom(type);
	}

	public static boolean isFloat(Class<?> type) {
		return Float.class.isAssignableFrom(type) ||
			float.class.isAssignableFrom(type);
	}

	public static boolean isInteger(Class<?> type) {
		return Integer.class.isAssignableFrom(type) ||
			int.class.isAssignableFrom(type);
	}

	public static boolean isLong(Class<?> type) {
		return Long.class.isAssignableFrom(type) ||
			long.class.isAssignableFrom(type);
	}

	public static boolean isShort(Class<?> type) {
		return Short.class.isAssignableFrom(type) ||
			short.class.isAssignableFrom(type);
	}

	public static boolean isNumber(Class<?> type) {
		return Number.class.isAssignableFrom(type) ||
			byte.class.isAssignableFrom(type) ||
			double.class.isAssignableFrom(type) ||
			float.class.isAssignableFrom(type) ||
			int.class.isAssignableFrom(type) ||
			long.class.isAssignableFrom(type) ||
			short.class.isAssignableFrom(type);
	}
	
	public static boolean isText(Class<?> type) {
		return String.class.isAssignableFrom(type) || isCharacter(type);
	}

}
