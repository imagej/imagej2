//
// KyEvent.java
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

package imagej.ext.display.event.input;

import imagej.ext.display.Display;
import imagej.ext.display.KeyCode;

/**
 * An event indicating keyboard activity in a display.
 * <p>
 * It is named <code>KyEvent</code> rather than <code>KeyEvent</code> to avoid
 * name clashes with the {@link java.awt.event.KeyEvent} hierarchy.
 * </p>
 * 
 * @author Curtis Rueden
 */
public abstract class KyEvent extends InputEvent {

	private final char character;
	private final KeyCode code;

	public KyEvent(final Display<?> display, final InputModifiers modifiers,
		final char character, final KeyCode code)
	{
		super(display, modifiers);
		this.character = character;
		this.code = code;
	}

	public char getCharacter() {
		return character;
	}

	public KeyCode getCode() {
		return code;
	}

	/**
	 * Converts the current key event into the corresponding accelerator string.
	 * 
	 * @return the string required as accelerator in the annotation of a plugin in
	 *         order to match the current key event.
	 */
	public String getAcceleratorString() {
		return getAcceleratorString(false);
	}

	/**
	 * Converts the current key event into the corresponding accelerator string.
	 * 
	 * @param forceControlModifier always pretend that the Control key (or on
	 *          Apple, the Command key) has been pressed.
	 * @return the string required as accelerator in the annotation of a plugin in
	 *         order to match the current key event.
	 */
	public String getAcceleratorString(final boolean forceControlModifier) {
		final InputModifiers modifiers = getModifiers();
		final StringBuilder builder = new StringBuilder();
		if (modifiers.isAltDown()) builder.append("alt ");
		if (forceControlModifier || modifiers.isCtrlDown()) {
			builder.append("control ");
		}
		if (modifiers.isMetaDown()) builder.append("meta ");
		if (modifiers.isShiftDown()) builder.append("shift ");
		char ch = getCharacter();
		if (ch > 0) {
			builder.append(getCode().toString());
		}
		return builder.toString();
	}

	// -- Object methods --

	@Override
	public String toString() {
		return super.toString() + "\n\tcharacter = '" + character + "'\n\tcode = " +
			code + "\n\taccelerator = " + getAcceleratorString();
	}

}
