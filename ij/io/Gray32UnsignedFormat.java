package ij.io;

class Gray32UnsignedFormat extends PixelFormat
{
	Gray32UnsignedFormat()
	{
		super("Gray32Unsigned",1,32,1);  // super(String name, int numSamples, int bitsPerSample, int planes)
	}
	
	boolean canDoImageCombo(int compression, ByteOrder.Value byteOrder, int headerBytes, boolean stripped)
	{
		if (compression != FileInfo.COMPRESSION_NONE)
			return false;
		
		if (stripped)
			return false;
		
		return true;
	}
	
	byte[] nativeBytes(long pix, ByteOrder.Value byteOrder)
	{
		byte[] output = new byte[4];
		
		output[0] = (byte)((pix & 0xff000000) >> 24);
		output[1] = (byte)((pix & 0x00ff0000) >> 16);
		output[2] = (byte)((pix & 0x0000ff00) >> 8);
		output[3] = (byte)((pix & 0x000000ff) >> 0);

		if (byteOrder == ByteOrder.Value.INTEL)
			PixelArranger.reverse(output);

		return output;
	}
	
	byte[] getBytes(long[][] image, int compression, ByteOrder.Value byteOrder, int headerBytes, boolean inStrips, FileInfo fi)
	{
		initializeFileInfo(fi,FileInfo.GRAY32_UNSIGNED,compression,byteOrder,image.length,image[0].length);

		byte[] output;
		
		if (inStrips)
			output = PixelArranger.arrangeInStrips(this,image,fi);
		else
			output = PixelArranger.arrangeContiguously(this,image,fi);
		
		output = PixelArranger.attachHeader(fi,headerBytes,output);

		return output;
	}

	Object expectedResults(long[][] inputImage)
	{
		float[] output = new float[inputImage.length * inputImage[0].length];
		
		int i = 0;
		for (long[] row : inputImage)
			for (long pix : row)
				output[i++] = (float)(pix & 0xffffffffL);
		return output;
	}

	Object pixelsFromBytes(byte[] bytes)
	{
		// this method not tested by ImageWriter. Therefore no implementation until it will be used.
		return null;
	}
}

