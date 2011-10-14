//
// InputModifiers.java
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

/**
 * A class encapsulating keyboard modifier key states.
 * 
 * @author Curtis Rueden
 */
public class InputModifiers {

	private final boolean altDown, altGrDown, ctrlDown, metaDown, shiftDown;
	private final boolean leftButtonDown, middleButtonDown, rightButtonDown;

	public InputModifiers(final boolean altDown,
		final boolean altGrDown, final boolean ctrlDown, final boolean metaDown,
		final boolean shiftDown, final boolean leftButtonDown,
		final boolean middleButtonDown, final boolean rightButtonDown)
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
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (altDown) sb.append(", Alt");
		if (altGrDown) sb.append(", AltGr");
		if (ctrlDown) sb.append(", Ctrl");
		if (metaDown) sb.append(", Meta");
		if (shiftDown) sb.append(", Shift");
		if (leftButtonDown) sb.append(", Left Button");
		if (middleButtonDown) sb.append(", Middle Button");
		if (rightButtonDown) sb.append(", Right Button");
		return sb.toString();
	}

}
