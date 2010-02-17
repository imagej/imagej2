package ij.io;

class Gray12UnsignedFormat extends PixelFormat
{
	Gray12UnsignedFormat()
	{
		super("Gray12Unsigned",1,12,1);  // super(String name, int numSamples, int bitsPerSample, int planes)
	}
	
	boolean canDoImageCombo(int compression, ByteOrder.Value byteOrder, int headerBytes, boolean stripped)
	{
		if (compression != FileInfo.COMPRESSION_NONE)
			return false;

		if (byteOrder == ByteOrder.Value.INTEL)
			return false;
		
		if (stripped)
			return false;
		
		return true;
	}

	byte[] nativeBytes(long pix, ByteOrder.Value byteOrder)
	{
		// since this format spans byte boundaries it cannot work with the basic model
		// see twelveBitEncoder() for an idea how the pixels are arranged
		
		return null;
	}
	
	byte[] getBytes(long[][] image, int compression, ByteOrder.Value byteOrder, int headerBytes, boolean inStrips, FileInfo fi)
	{
		initializeFileInfo(fi,FileInfo.GRAY12_UNSIGNED,compression,byteOrder,image.length,image[0].length);
		
		byte[] output = TwelveBitEncoder.encode(image);
		
		// if (byteOrder == ByteOrder.INTEL)
		//	;  // nothing to do

		output = PixelArranger.attachHeader(fi,headerBytes,output);
		
		return output;
	}

	Object expectedResults(long[][] inputImage)
	{
		short[] output = new short[inputImage.length * inputImage[0].length];
		
		int i = 0;
		for (long[] row : inputImage)
			for (long pix: row)
				output[i++] = (short)(pix & 0xfff);
		
		return output;
	}		

	Object pixelsFromBytes(byte[] bytes)
	{
		// this method not tested by ImageWriter. Therefore no implementation until it will be used.
		return null;
	}
}

