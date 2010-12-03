package imagej;

import imagej.DataType;

public class SampleInfoCompileCheck
{
	public void proveExistence()
	{
		// compile time check for existence
		
		DataType type;
		
		type = DataType.BIT;
		type = DataType.BYTE;
		type = DataType.UBYTE;
		type = DataType.UINT12;
		type = DataType.SHORT;
		type = DataType.USHORT;
		type = DataType.INT;
		type = DataType.UINT;
		type = DataType.FLOAT;
		type = DataType.DOUBLE;
		type = DataType.LONG;
	}
}
