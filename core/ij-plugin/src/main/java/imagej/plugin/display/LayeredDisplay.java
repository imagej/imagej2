package imagej.plugin.display;

/**
 *
 * @author GBH
 */
public interface LayeredDisplay extends Display {

	void addView(DisplayView view);

	void removeView(DisplayView view);

	void removeAllViews();

	DisplayView[] getViews();

	DisplayView getView(int n);

	DisplayView getActiveView(); // returns getView(0) for now
}
