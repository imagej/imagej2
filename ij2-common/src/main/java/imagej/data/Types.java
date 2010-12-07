package imagej.data;

import java.util.HashMap;

public class Types
{
	// *********  instance variables ***************************************
	
	//private static ArrayList<Type> types;
	private static HashMap<String,Type> types;
	
	// *********  initialization ***************************************
	
	static
	{
		types = new HashMap<String,Type>();
		
		Type type;
		
		type = new BitType();
		types.put(type.getName(), type);
		
		type = new ByteType();
		types.put(type.getName(), type);

		type = new UnsignedByteType();
		types.put(type.getName(), type);

		type = new Unsigned12BitType();
		types.put(type.getName(), type);

		type = new ShortType();
		types.put(type.getName(), type);

		type = new UnsignedShortType();
		types.put(type.getName(), type);

		type = new IntType();
		types.put(type.getName(), type);

		type = new UnsignedIntType();
		types.put(type.getName(), type);

		type = new FloatType();
		types.put(type.getName(), type);

		type = new LongType();
		types.put(type.getName(), type);

		type = new DoubleType();
		types.put(type.getName(), type);
	}
	
	// *********  public interface ***************************************

	public static Type findType(String name)
	{
		return types.get(name);
	}
	
	public static void verifyCompatibility(Type type, Object data)
	{
		if ( ! type.isStorageCompatible(data) )
			throw new IllegalArgumentException("internal representation type clash : type ("+type.getName()+") and given data class ("+data.getClass()+")");
	}
}
