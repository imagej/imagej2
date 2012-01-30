
package imagej.updater.util;

public class StderrProgress implements Progress {

	final static String end = "\033[K\r";
	protected String label;
	protected Object item;
	protected long lastShown, minShowDelay = 500;
	protected int lineWidth = -1;

	public StderrProgress() {}

	public StderrProgress(final int lineWidth) {
		this.lineWidth = lineWidth;
	}

	protected void print(String label, final String rest) {
		if (lineWidth < 0) System.err.print(label + " " + rest + end);
		else {
			if (label.length() >= lineWidth - 3) label =
				label.substring(0, lineWidth - 3) + "...";
			else {
				final int diff = label.length() + 1 + rest.length() - lineWidth;
				if (diff < 0) label += " " + rest;
				else label +=
					(" " + rest).substring(0, rest.length() - diff - 3) + "...";
			}
			System.err.print(label + end);
		}
	}

	protected boolean skipShow() {
		final long now = System.currentTimeMillis();
		if (now - lastShown < minShowDelay) return true;
		lastShown = now;
		return false;
	}

	@Override
	public void setTitle(final String title) {
		label = title;
	}

	@Override
	public void setCount(final int count, final int total) {
		if (skipShow()) return;
		print(label, "" + count + "/" + total);
	}

	@Override
	public void addItem(final Object item) {
		this.item = item;
		print(label, "(" + item + ") ");
	}

	@Override
	public void setItemCount(final int count, final int total) {
		if (skipShow()) return;
		print(label, "(" + item + ") [" + count + "/" + total + "]");
	}

	@Override
	public void itemDone(final Object item) {
		print(item.toString(), "done");
	}

	@Override
	public void done() {
		print("Done:", label);
		System.err.println("");
	}
}
