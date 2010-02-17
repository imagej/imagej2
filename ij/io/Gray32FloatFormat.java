package ij.io;

class Gray32FloatFormat extends PixelFormat
{
	Gray32FloatFormat()
	{
		super("Gray32Float",1,32,1);  // super(String name, int numSamples, int bitsPerSample, int planes)
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
		
		float fPix = (float) pix;

		int bPix = Float.floatToIntBits(fPix);
		
		output[0] = (byte)((bPix & 0xff000000) >> 24);
		output[1] = (byte)((bPix & 0x00ff0000) >> 16);
		output[2] = (byte)((bPix & 0x0000ff00) >> 8);
		output[3] = (byte)((bPix & 0x000000ff) >> 0);
		
		if (byteOrder == ByteOrder.Value.INTEL)
			PixelArranger.reverse(output);

		return output;
	}
	
	byte[] getBytes(long[][] image, int compression, ByteOrder.Value byteOrder, int headerBytes, boolean inStrips, FileInfo fi)
	{
		initializeFileInfo(fi,FileInfo.GRAY32_FLOAT,compression,byteOrder,image.length,image[0].length);
		
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

	Object pixelsFromBytes(byte[] bytes)
	{
		int numFloats = bytes.length / 4;
		float[] output = new float[numFloats];
		for (int i = 0; i < numFloats; i++)
		{
			int theInt = ((bytes[4*i+0] & 0xff) << 24) | ((bytes[4*i+1] & 0xff) << 16) | ((bytes[4*i+2] & 0xff) << 8) | ((bytes[4*i+3] & 0xff) << 0);
			output[i] = Float.intBitsToFloat(theInt);
		}
		return output;
	}
}

