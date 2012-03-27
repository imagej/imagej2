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
 * A UI-independent enumeration of mouse cursors.
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
