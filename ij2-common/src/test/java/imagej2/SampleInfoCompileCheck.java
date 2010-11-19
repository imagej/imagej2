package imagej2;

public class SampleInfoCompileCheck
{
	public void proveExistence()
	{
		// compile time check for existence
		
		UserType type;
		
		type = UserType.BIT;
		type = UserType.BYTE;
		type = UserType.UBYTE;
		type = UserType.UINT12;
		type = UserType.SHORT;
		type = UserType.USHORT;
		type = UserType.INT;
		type = UserType.UINT;
		type = UserType.FLOAT;
		type = UserType.DOUBLE;
		type = UserType.LONG;
	}
}
