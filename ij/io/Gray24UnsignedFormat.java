package ij.io;

class Gray24UnsignedFormat extends PixelFormat
{
	Gray24UnsignedFormat()
	{
		super("Gray24Unsigned",1,24,1);  // super(String name, int numSamples, int bitsPerSample, int planes)
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
		byte[] output = new byte[3];
		
		output[0] = (byte) ((pix & 0x0000ff) >> 0);
		output[1] = (byte) ((pix & 0x00ff00) >> 8);
		output[2] = (byte) ((pix & 0xff0000) >> 16);

		return output;
	}
	
	byte[] getBytes(long[][] image, int compression, ByteOrder.Value byteOrder, int headerBytes, boolean inStrips, FileInfo fi)
	{
		initializeFileInfo(fi,FileInfo.GRAY24_UNSIGNED,compression,byteOrder,image.length,image[0].length);
		
		byte[] output;
		
		// ALWAYS arrange contiguously in this case
		output = PixelArranger.arrangeContiguously(this,image,fi);
		
		output = PixelArranger.attachHeader(fi,headerBytes,output);

		return output;
	}

	Object expectedResults(long[][] inputImage)
	{
		float[] output = new float[inputImage.length * inputImage[0].length];
			
		int i = 0;
		for (long[] row : inputImage)
			for (long pix: row)
				output[i++] = (float)(pix & 0xffffff);
		
		return output;
	}		

	Object pixelsFromBytes(byte[] bytes)
	{
		// this method not tested by ImageWriter. Therefore no implementation until it will be used.
		return null;
	}
}

