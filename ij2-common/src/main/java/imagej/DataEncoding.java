package imagej;

// this interface is for delineating how a data value is represented in storage. an example is Unsigned12Bit. it can be encoded as 12 BITs, 1.5 UBYTEs,
//   all of one USHORT (wasting 4 bits), or 12/32 of a UINT32

public interface DataEncoding
{
	StorageType getBackingType();
	double getNumTypesPerValue();
	// For one 12 bit sample using ubyte encoding value == 3.0.
	// For one 12 bit sample using uncompacted ushort encoding value = 1.0.
	// For one 12 bit sample using compacted ushort encoding value = 0.75.

	// lets us know how to handle primitive array contents when changing them
	// also useful for calculating how much memory is allocated for a given type
	//   would this be better served by having encoding have a "double getBytesPerValue()"???
}

