package imagej.imglib.examples.function.observer;

/*************  Observer ****************************************************************/

public interface Observer
{
	void init();
	void update(int[] position, double value, boolean accepted);
	void done(boolean wasAborted);
}

// TODO
//   one example of an Observer is simply a progress indicator that ignores the update() parameters and displays percent done
//   another example would be a class that might gather statistics

