//
// MouseCursor.java
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

package imagej.display;

/**
 * TODO
 * 
 * @author Grant Harris
 * @author Curtis Rueden
 */
public enum MouseCursor {

	/** Default pointer. */
	DEFAULT,

	/** No cursor (invisible). */
	OFF,

	/** Custom cursor. */
	CUSTOM,

	/** A crosshair cursor. */
	CROSSHAIR,

	/** A hand-shaped cursor. */
	HAND,

	/** Four-way arrow cursor. */
	MOVE,

	/** Vertical bar cursor, for text selection. */
	TEXT,

	/** Hourglass cursor. */
	WAIT,

	/** Cursor for resizing an edge northward. */
	N_RESIZE,

	/** Cursor for resizing an edge southward. */
	S_RESIZE,

	/** Cursor for resizing an edge westward. */
	W_RESIZE,

	/** Cursor for resizing an edge eastward. */
	E_RESIZE,

	/** Cursor for resizing an edge northwest. */
	NW_RESIZE,

	/** Cursor for resizing an edge northeast. */
	NE_RESIZE,

	/** Cursor for resizing an edge southwest. */
	SW_RESIZE,

	/** Cursor for resizing an edge southeast. */
	SE_RESIZE

}
