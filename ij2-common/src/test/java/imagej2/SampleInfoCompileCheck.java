package imagej2;

import imagej2.SampleInfo.ValueType;

public class SampleInfoCompileCheck
{
	public void proveExistence()
	{
		// compile time check for existence
		
		ValueType type;
		
		type = ValueType.BIT;
		type = ValueType.BYTE;
		type = ValueType.UBYTE;
		type = ValueType.UINT12;
		type = ValueType.SHORT;
		type = ValueType.USHORT;
		type = ValueType.INT;
		type = ValueType.UINT;
		type = ValueType.FLOAT;
		type = ValueType.DOUBLE;
		type = ValueType.LONG;
	}
}
