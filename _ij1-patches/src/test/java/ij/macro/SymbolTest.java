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
