package imagej.util;

import java.util.ArrayList;

public final class ListUtils {

	private ListUtils() {
		// prevent instantiation of utility class
	}

	@SuppressWarnings("unchecked")
	public static <T> ArrayList<T> copyList(final ArrayList<T> list) {
		return (ArrayList<T>) list.clone();
	}

}
