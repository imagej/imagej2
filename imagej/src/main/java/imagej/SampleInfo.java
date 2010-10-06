package imagej;

/**
 * SampleInfo contains information related to samples in IJ. Here a sample is defined as a collection
 * of values of a value type. For example a 4 byte sample or a 1 float sample. No storage layout is
 * implied by a SampleInfo definition. SampleInfo only provides accessors which are used to discover
 * the attributes of supported types. All IJ legacy types have 1 value per sample.
 */
public interface SampleInfo
{
	/** ValueType represents the underlying types that a sample is built upon. */
	enum ValueType {BIT, BYTE, UBYTE, SHORT, USHORT, INT, UINT, LONG, FLOAT, DOUBLE};

	/** returns the underlying ValueType for this sample's values. */
	ValueType getValueType();
	
	/** returns the number of values in the sample */
	int getNumValues();
	
	/** returns the number of bits needed to represent one value of a sample */
	int getNumBitsPerValue();

	/** returns the number of bits needed to represent a complete sample (multiple values).
	 *  Does not simply any storage layout. */
	int getNumBits();
	
	/** returns true if sample is considered a signed numeric value */
	boolean isSigned();

	/** returns true if sample is considered an unsigned numeric value */
	boolean isUnsigned();
	
	/** returns true if sample is considered an integral numeric value */
	boolean isIntegral();
	
	/** returns true if sample is considered a floating point numeric value */
	boolean isFloat();
	
	/** internal name of the sample */
	String getName();
}
