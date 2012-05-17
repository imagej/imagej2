package imagej.updater;

import imagej.updater.gui.ProgressDialog;
import imagej.updater.gui.SwingUserInterface;
import imagej.util.MersenneTwisterFast;

public class UpdaterGUITest {
	public static void main(String[] args) {
		//testProgressDialog();
		testPassword();
	}

	protected static void testProgressDialog() {
		int count = 35;
		int minSize = 8192;
		int maxSize = 65536;
		int minChunk = 256;
		int maxChunk = 16384;

		MersenneTwisterFast random = new MersenneTwisterFast();

		ProgressDialog progress = new ProgressDialog(null);

		progress.setTitle("Hello");

		int totalSize = 0;
		int totalCurrent = 0;
		int[] sizes = new int[count];
		for (int i = 0; i < count; i++) {
			sizes[i] = minSize + random.nextInt(maxSize - minSize);
			totalSize += sizes[i];
		}

		for (int i = 0; i < count; i++) {
			int current = 0;
			String item = "Item " + i + "/" + sizes[i];
			progress.addItem(item);
			while (current < sizes[i]) {
				int byteCount = minChunk + random.nextInt(maxChunk - minChunk);
				current += byteCount;
				progress.setItemCount(current, sizes[i]);
				totalCurrent += byteCount;
				progress.setCount(totalCurrent, totalSize);
				int millis = random.nextInt(500);
				if (millis > 0) try {
					Thread.sleep(millis);
				} catch (InterruptedException e) {
					// we've been asked to stop
					progress.done();
					return;
				}
			}
			progress.itemDone(item);
		}
		progress.done();
	}

	protected static void testPassword() {
		SwingUserInterface ui = new SwingUserInterface(null);
		System.err.println(ui.getPassword("Enter password"));
	}
}
