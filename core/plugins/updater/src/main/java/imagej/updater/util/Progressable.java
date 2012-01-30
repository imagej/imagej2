
package imagej.updater.util;

import java.util.ArrayList;
import java.util.List;

/*
 * This class is the base class for serving Progress instances.  For this
 * reason, it implements the same interface.
 */
public class Progressable implements Progress {

	protected List<Progress> progress;

	public Progressable() {
		progress = new ArrayList<Progress>();
	}

	public void addProgress(final Progress progress) {
		this.progress.add(progress);
	}

	public void removeProgress(final Progress progress) {
		this.progress.remove(progress);
	}

	@Override
	public void setTitle(final String title) {
		for (final Progress progress : this.progress)
			progress.setTitle(title);
	}

	@Override
	public void setCount(final int count, final int total) {
		for (final Progress progress : this.progress)
			progress.setCount(count, total);
	}

	@Override
	public void addItem(final Object item) {
		for (final Progress progress : this.progress)
			progress.addItem(item);
	}

	@Override
	public void setItemCount(final int count, final int total) {
		for (final Progress progress : this.progress)
			progress.setItemCount(count, total);
	}

	@Override
	public void itemDone(final Object item) {
		for (final Progress progress : this.progress)
			progress.itemDone(item);
	}

	@Override
	public void done() {
		for (final Progress progress : this.progress)
			progress.done();
	}
}
