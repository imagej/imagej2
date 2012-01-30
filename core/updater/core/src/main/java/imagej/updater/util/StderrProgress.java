//
// StderrProgress.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

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
