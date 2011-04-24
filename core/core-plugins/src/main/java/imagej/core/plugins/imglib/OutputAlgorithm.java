package imagej.core.plugins.imglib;

// TODO
//  this interface was created from imglib's Algorithm & OutputAlgorithm.
//  use those later when imglib algorithms ported to imglib2

public interface OutputAlgorithm<T> {
	boolean checkInput();
	boolean process();
	String getErrorMessage();
	T getResult();
}
