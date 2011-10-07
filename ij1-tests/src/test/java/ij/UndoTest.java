//
// UndoTest.java
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

package ij;

import static org.junit.Assert.*;
import org.junit.Test;

public class UndoTest {
	@Test
	public void testConstants() {
		assertEquals(0,Undo.NOTHING);
		assertEquals(1,Undo.FILTER);
		assertEquals(2,Undo.TYPE_CONVERSION);
		assertEquals(3,Undo.PASTE);
		assertEquals(4,Undo.COMPOUND_FILTER);
		assertEquals(5,Undo.COMPOUND_FILTER_DONE);
		assertEquals(6,Undo.TRANSFORM);
		assertEquals(7,Undo.OVERLAY_ADDITION);
	}
	
	// I don't think I can test this
	//   I can't test internals as it is all private state with no accessors
	//   I can't test side effects
	//     it relies on WindowManager to determine which ImagePlus to manipulate
	//     I somehow need to fake WindowManager into thinking my imagePlus is the one that is active
	//       but it is tied to threads and windows neither of which I have access to.
}
