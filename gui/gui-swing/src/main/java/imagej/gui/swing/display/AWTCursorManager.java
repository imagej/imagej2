//
// AWTCursorManager.java
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

package imagej.gui.swing.display;

import imagej.display.MouseCursor;

import java.awt.Cursor;

/**
 * TODO To decouple Cursors from GUI toolkit March 19: Not used yet
 * 
 * @author Grant Harris
 */
public class AWTCursorManager {

//	void setCursor(int cursorCode) {
//        int newAwtCursorCode = getAWTCursorCode(cursorCode);
//        if (newAwtCursorCode == Cursor.CUSTOM_CURSOR && invisibleCursor == null) {
//            newAwtCursorCode = Cursor.DEFAULT_CURSOR;
//        }
//
//        if (newAwtCursorCode == Cursor.DEFAULT_CURSOR) {
//            cursorCode = Input.DEFAULT;
//        }
//
//        if (this.cursorCode != cursorCode || this.awtCursorCode != newAwtCursorCode) {
//            if (newAwtCursorCode == Cursor.CUSTOM_CURSOR) {
//                comp.setCursor(invisibleCursor);
//            }
//            else {
//                comp.setCursor(Cursor.getPredefinedCursor(newAwtCursorCode));
//            }
//            this.awtCursorCode = newAwtCursorCode;
//            this.cursorCode = cursorCode;
//        }
//    }

	public static int getAWTCursorCode(final MouseCursor cursorCode) {
		switch (cursorCode) {
			default:
				return Cursor.DEFAULT_CURSOR;
			case DEFAULT:
				return Cursor.DEFAULT_CURSOR;
			case OFF:
				return Cursor.CUSTOM_CURSOR;
			case HAND:
				return Cursor.HAND_CURSOR;
			case CROSSHAIR:
				return Cursor.CROSSHAIR_CURSOR;
			case MOVE:
				return Cursor.MOVE_CURSOR;
			case TEXT:
				return Cursor.TEXT_CURSOR;
			case WAIT:
				return Cursor.WAIT_CURSOR;
			case N_RESIZE:
				return Cursor.N_RESIZE_CURSOR;
			case S_RESIZE:
				return Cursor.S_RESIZE_CURSOR;
			case W_RESIZE:
				return Cursor.W_RESIZE_CURSOR;
			case E_RESIZE:
				return Cursor.E_RESIZE_CURSOR;
			case NW_RESIZE:
				return Cursor.NW_RESIZE_CURSOR;
			case NE_RESIZE:
				return Cursor.NE_RESIZE_CURSOR;
			case SW_RESIZE:
				return Cursor.SW_RESIZE_CURSOR;
			case SE_RESIZE:
				return Cursor.SE_RESIZE_CURSOR;
		}
	}

}
