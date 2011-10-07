//
// SymbolTest.java
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

package ij.macro;

import static org.junit.Assert.*;

import org.junit.Test;

public class SymbolTest {

	@Test
	public void testSymbolIntString() 
	{
		String testString = "TestText";
		int a = MacroConstants.TOK_SHIFT;
		Symbol st = new Symbol( a, testString );
		assertEquals( st.toString(), (a&0xffff)+" 0.0 "+testString );
	}

	@Test
	public void testSymbolDouble() 
	{
		double testValue = 1.1;
		Symbol st = new Symbol( testValue );
		assertEquals( st.toString(), "0 1.1 null" );
	}

	@Test
	public void testGetFunctionType() 
	{
		String testString = "TestText";
		int a = MacroConstants.INVERT;
		Symbol st = new Symbol( a, testString );
		assertEquals( st.getFunctionType(), 134 );
	
		a = MacroConstants.GET_ZOOM;
		st = new Symbol( a, testString );
		assertEquals( st.getFunctionType(), 135 );
		
		a = MacroConstants.SELECTION_NAME;
		st = new Symbol( a, testString );
		assertEquals( st.getFunctionType(), 136 );
		
		a = MacroConstants.ARRAY_FUNC;
		st = new Symbol( a, testString );
		assertEquals( st.getFunctionType(), 137 );

	}

	@Test
	public void testToString() {
		double testValue = 1.1;
		Symbol st = new Symbol( testValue );
		assertEquals( st.toString(), "0 1.1 null" );
	}

}
