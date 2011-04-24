package imagej.display.view;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author GBH
 */
public class ImgDisplayController {
	
	protected List<DatasetView> views = new ArrayList<DatasetView>();

	public List<DatasetView> getViews() {
		return views;
	}

	public void addView(DatasetView view) {
		views.add(view);
	}
	
	public void reMapAll() {
		for (int i = 0; i < views.size(); i++) {
			DatasetView view = views.get(i);
			view.getProjector().map();
		}
	}

}
