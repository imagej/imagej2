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

package imagej.ui;

import imagej.ImageJ;
import imagej.ext.display.AbstractTextDisplay;
import imagej.ext.display.TextDisplay;
import imagej.ext.plugin.Plugin;

import java.util.Collection;

/**
 * Display for showing text onscreen.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = TextDisplay.class)
public class DefaultTextDisplay extends AbstractTextDisplay {

	/** Output window backing this text display. */
	private OutputWindow outputWindow;

	private int lastIndex;
	private boolean cleared;

	// -- AbstractDisplay methods --

	@Override
	protected void rebuild() {
		// NB: no action needed
	}

	// -- TextDisplay methods --

	@Override
	public void append(final String text) {
		add(text);
	}

	// -- Display methods --

	@Override
	public void update() {
		if (outputWindow == null) {
			outputWindow = ImageJ.get(UIService.class).createOutputWindow(getName());
		}
		if (cleared) {
			// output window was cleared; start again
			outputWindow.clear();
			lastIndex = 0;
		}
		while (lastIndex < size()) {
			outputWindow.append(get(lastIndex++) + "\n");
		}
		outputWindow.setVisible(true);
	}

	@Override
	public void close() {
		outputWindow.setVisible(false);
		outputWindow.dispose();
	}

	// FIXME - return a non-null DisplayPanel?

	// -- List methods --

	@Override
	public void add(final int index, final String element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(final int index, final Collection<? extends String> c)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String remove(final int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String set(final int index, final String element) {
		throw new UnsupportedOperationException();
	}

	// -- Collection methods --

	@Override
	public void clear() {
		super.clear();
		cleared = true;
	}

	@Override
	public boolean remove(final Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(final Collection<?> c) {
		throw new UnsupportedOperationException();
	}

}
