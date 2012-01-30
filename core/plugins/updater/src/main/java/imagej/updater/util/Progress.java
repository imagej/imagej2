
package imagej.updater.util;

public interface Progress {

	public void setTitle(String title);

	public void setCount(int count, int total);

	public void addItem(Object item);

	public void setItemCount(int count, int total);

	public void itemDone(Object item);

	public void done();
}
