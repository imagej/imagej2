package imagej;

// TODO : add isIntegral() and isUnsigned()??
//   Get rid of TypeManager and put code in here? Or does that code belong in imglib?

public interface SampleInfo
{
	enum SampleType {BYTE, UBYTE, SHORT, USHORT, INT, UINT, LONG, FLOAT, DOUBLE};
	
	SampleType getValueType(); // type of each value
	int getNumValues();  // values per sample (a Complex could be two values of DOUBLE data)
	int getNumBitsPerValue();  // bits per value
	int getNumBits();  // bits in a complete sample : does not imply a storage layout
	String getName();
	// note that we can compose sample types bigger than 64 bit ints via multiple values per sample
}
