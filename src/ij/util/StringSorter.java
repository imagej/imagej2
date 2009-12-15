package ij.util;

/** A simple QuickSort for String arrays. */
public class StringSorter {
	
	/** Sorts the array. */
	public static void sort(String[] a) {
		if (!alreadySorted(a))
			sort(a, 0, a.length - 1);
	}
	
	static void sort(String[] a, int from, int to) {
		int i = from, j = to;
		String center = a[ (from + to) / 2 ];
		do {
			while ( i < to && center.compareTo(a[i]) > 0 ) i++;
			while ( j > from && center.compareTo(a[j]) < 0 ) j--;
			if (i < j) {String temp = a[i]; a[i] = a[j]; a[j] = temp; }
			if (i <= j) { i++; j--; }
		} while(i <= j);
		if (from < j) sort(a, from, j);
		if (i < to) sort(a,  i, to);
	}
		
	static boolean alreadySorted(String[] a) {
		for ( int i=1; i<a.length; i++ ) {
			if (a[i].compareTo(a[i-1]) < 0 )
			return false;
		}
		return true;
	}
}
