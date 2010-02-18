package ij.io;

class Gray64FloatFormat extends PixelFormat
{
	Gray64FloatFormat()
	{
		super("Gray64Float",1,64,1);  // super(String name, int numSamples, int bitsPerSample, int planes)
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
		byte[] output = new byte[8];

		double dPix = (double) pix;

		long bPix = Double.doubleToLongBits(dPix);

		output[0] = (byte)((bPix & 0xff00000000000000L) >> 56);
		output[1] = (byte)((bPix & 0x00ff000000000000L) >> 48);
		output[2] = (byte)((bPix & 0x0000ff0000000000L) >> 40);
		output[3] = (byte)((bPix & 0x000000ff00000000L) >> 32);
		output[4] = (byte)((bPix & 0x00000000ff000000L) >> 24);
		output[5] = (byte)((bPix & 0x0000000000ff0000L) >> 16);
		output[6] = (byte)((bPix & 0x000000000000ff00L) >> 8);
		output[7] = (byte)((bPix & 0x00000000000000ffL) >> 0);
		
		if (byteOrder == ByteOrder.Value.INTEL)
			PixelArranger.reverse(output);

		return output;
	}
	
	byte[] getBytes(long[][] image, int compression, ByteOrder.Value byteOrder, int headerBytes, boolean inStrips, FileInfo fi)
	{
		initializeFileInfo(fi,FileInfo.GRAY64_FLOAT,compression,byteOrder,image.length,image[0].length);
		

		byte[] output;

		// ALWAYS contiguous in this case
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
				output[i++] = (float)pix;
		return output;
	}

	Object pixelsFromBytes(byte[] bytes, ByteOrder.Value order)
	{
		// this method not tested by ImageWriter. Therefore no implementation until it will be used.
		return null;
	}
}

