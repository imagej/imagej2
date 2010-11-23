package imagej.selection;

public interface SelectionFunction
{
	boolean include(int[] position, double sample);
}

