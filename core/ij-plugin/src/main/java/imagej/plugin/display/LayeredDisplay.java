package imagej.plugin.display;

/**
 * TODO
 *
 * @author Grant Harris
 */
public interface LayeredDisplay extends Display {

	void addView(DisplayView view);

	void removeView(DisplayView view);

	void removeAllViews();

	DisplayView[] getViews();

	DisplayView getView(int n);

	DisplayView getActiveView();

}
