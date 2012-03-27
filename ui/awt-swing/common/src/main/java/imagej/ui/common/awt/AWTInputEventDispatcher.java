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

package imagej.ui.common.awt;

import imagej.ext.InputModifiers;

import java.awt.event.InputEvent;

/**
 * Abstract superclass of AWT event dispatcher implementations for
 * {@link InputEvent}s.
 * 
 * @author Curtis Rueden
 */
public abstract class AWTInputEventDispatcher {

	public InputModifiers createModifiers(final int modsEx) {
		final boolean altDown = isOn(modsEx, InputEvent.ALT_DOWN_MASK);
		final boolean altGrDown = isOn(modsEx, InputEvent.ALT_GRAPH_DOWN_MASK);
		final boolean ctrlDown = isOn(modsEx, InputEvent.CTRL_DOWN_MASK);
		final boolean metaDown = isOn(modsEx, InputEvent.META_DOWN_MASK);
		final boolean shiftDown = isOn(modsEx, InputEvent.SHIFT_DOWN_MASK);
		final boolean leftButtonDown = isOn(modsEx, InputEvent.BUTTON1_DOWN_MASK);
		final boolean middleButtonDown = isOn(modsEx, InputEvent.BUTTON3_DOWN_MASK);
		final boolean rightButtonDown = isOn(modsEx, InputEvent.BUTTON2_DOWN_MASK);
		return new InputModifiers(altDown, altGrDown, ctrlDown, metaDown,
			shiftDown, leftButtonDown, middleButtonDown, rightButtonDown);
	}

	// -- Helper methods --

	private boolean isOn(final int modsEx, final int mask) {
		return (modsEx & mask) != 0;
	}

}
