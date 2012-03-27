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

package imagej.updater.util;

public class StderrProgress implements Progress {

	protected final static boolean redirected = System.console() == null;
	protected final static String end = redirected ? "\n" : "\033[K\r";
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
		if (redirected || skipShow()) return;
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
