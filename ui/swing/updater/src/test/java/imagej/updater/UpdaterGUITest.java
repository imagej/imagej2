/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.updater;

import imagej.updater.gui.ProgressDialog;
import imagej.updater.gui.SwingUserInterface;
import imagej.util.MersenneTwisterFast;

/**
 * TODO
 * 
 * @author Johannes Schindelin
 */
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
