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
		
		addType(new BitType());
		addType(new ByteType());
		addType(new UnsignedByteType());
		addType(new Unsigned12BitType());
		addType(new ShortType());
		addType(new UnsignedShortType());
		addType(new IntType());
		addType(new UnsignedIntType());
		addType(new FloatType());
		addType(new LongType());
		addType(new DoubleType());
	}
	
	private static void addType(Type type)
	{
		String typeName = type.getName();
		
		if (types.get(typeName) != null)
			throw new IllegalStateException("more than one definition exists for type "+typeName);
		
		types.put(typeName, type);
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
