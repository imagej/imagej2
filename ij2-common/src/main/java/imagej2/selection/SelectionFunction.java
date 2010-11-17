package imagej2.selection;

public interface SelectionFunction
{
	boolean include(int[] position, double sample);
}

