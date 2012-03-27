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

package imagej.ext;

/**
 * A UI-independent representation of keyboard modifier key states.
 * 
 * @author Curtis Rueden
 */
public class InputModifiers {

	private final boolean altDown, altGrDown, ctrlDown, metaDown, shiftDown;
	private final boolean leftButtonDown, middleButtonDown, rightButtonDown;

	public InputModifiers(final boolean altDown, final boolean altGrDown,
		final boolean ctrlDown, final boolean metaDown, final boolean shiftDown,
		final boolean leftButtonDown, final boolean middleButtonDown,
		final boolean rightButtonDown)
	{
		this.altDown = altDown;
		this.altGrDown = altGrDown;
		this.ctrlDown = ctrlDown;
		this.metaDown = metaDown;
		this.shiftDown = shiftDown;
		this.leftButtonDown = leftButtonDown;
		this.middleButtonDown = middleButtonDown;
		this.rightButtonDown = rightButtonDown;
	}

	public boolean isAltDown() {
		return altDown;
	}

	public boolean isAltGrDown() {
		return altGrDown;
	}

	public boolean isCtrlDown() {
		return ctrlDown;
	}

	public boolean isMetaDown() {
		return metaDown;
	}

	public boolean isShiftDown() {
		return shiftDown;
	}

	public boolean isLeftButtonDown() {
		return leftButtonDown;
	}

	public boolean isMiddleButtonDown() {
		return middleButtonDown;
	}

	public boolean isRightButtonDown() {
		return rightButtonDown;
	}

	// -- Object methods --

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof InputModifiers)) return false;
		final InputModifiers modifiers = (InputModifiers) o;
		if (!isAltDown() == modifiers.isAltDown()) return false;
		if (!isAltGrDown() == modifiers.isAltGrDown()) return false;
		if (!isCtrlDown() == modifiers.isCtrlDown()) return false;
		if (!isMetaDown() == modifiers.isMetaDown()) return false;
		if (!isShiftDown() == modifiers.isShiftDown()) return false;
		if (!isLeftButtonDown() == modifiers.isLeftButtonDown()) return false;
		if (!isMiddleButtonDown() == modifiers.isMiddleButtonDown()) return false;
		if (!isRightButtonDown() == modifiers.isRightButtonDown()) return false;
		return true;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		if (altDown) sb.append(" alt");
		if (altGrDown) sb.append(" altGraph");
		if (ctrlDown) sb.append(" control");
		if (metaDown) sb.append(" meta");
		if (shiftDown) sb.append(" shift");
		if (leftButtonDown) sb.append(" leftButton");
		if (middleButtonDown) sb.append(" middleButton");
		if (rightButtonDown) sb.append(" rightButton");
		return sb.toString().trim();
	}

}
