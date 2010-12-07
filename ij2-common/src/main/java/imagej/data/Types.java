package imagej.data;

import java.util.HashMap;

/** this class delineates all of the Type classes that ImageJ supports. If new types are implemented they need to be instantiated
 * in this class' static initialization block.
 */
public class Types
{
	// *********  instance variables ***************************************
	
	private static HashMap<String,Type> types;
	
	// *********  initialization ***************************************

	/** one time setup code. add new Types here as needed. */
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
	
	/** helper method - ensures that Types do not share names */
	private static void addType(Type type)
	{
		String typeName = type.getName();
		
		if (types.get(typeName) != null)
			throw new IllegalStateException("more than one definition exists for type "+typeName);
		
		types.put(typeName, type);
	}
	
	// *********  public interface ***************************************

	/** returns the Type associated with given name. */
	public static Type findType(String name)
	{
		return types.get(name);
	}
	
	/** throws an exception if given data array is not compatible with given Type's internal representation */
	public static void verifyCompatibility(Type type, Object data)
	{
		if ( ! type.isStorageCompatible(data) )
			throw new IllegalArgumentException("internal representation type clash : type ("+type.getName()+") and given data class ("+data.getClass()+")");
	}
}
