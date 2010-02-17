package ij.io;

class RgbFormat extends PixelFormat
{
	RgbFormat()
	{
		super("Rgb",3,8,1);  // super(String name, int numSamples, int bitsPerSample, int planes)
	}
	
	boolean canDoImageCombo(int compression, ByteOrder.Value byteOrder, int headerBytes, boolean stripped)
	{
		if (compression == FileInfo.COMPRESSION_UNKNOWN)
			return false;
		if (compression == FileInfo.JPEG)  // TODO: remove this restriction to test jpeg compression
			return false;
		
		if (byteOrder == ByteOrder.Value.INTEL)
			return false;
		
		if (stripped && (compression == FileInfo.COMPRESSION_NONE))
			return false;
		
		return true;
	}
	
	byte[] nativeBytes(long pix, ByteOrder.Value byteOrder)
	{
		byte[] output = new byte[3];
		
		output[0] = (byte)((pix & 0xff0000) >> 16);
		output[1] = (byte)((pix & 0x00ff00) >> 8);
		output[2] = (byte)((pix & 0x0000ff) >> 0);
		
		return output;
	}
	
	byte[] getBytes(long[][] image, int compression, ByteOrder.Value byteOrder, int headerBytes, boolean inStrips, FileInfo fi)
	{
		initializeFileInfo(fi,FileInfo.RGB,compression,byteOrder,image.length,image[0].length);
		
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
		int[] output = new int[inputImage.length * inputImage[0].length];
		
		// NOTICE that input is rgb but output is argb
		
		int i = 0;
		for (long[] row : inputImage)
			for (long pix : row)
				output[i++] = (int)(0xff000000 | (pix & 0xffffff));

		return output;
	}

	Object pixelsFromBytes(byte[] bytes)
	{
		int numInts = bytes.length / 3;
		int[] output = new int[numInts];
		for (int i = 0; i < numInts; i++)
			output[i] = 0xff000000 | ((bytes[3*i] & 0xff) << 16) | ((bytes[3*i+1] & 0xff) << 8) | ((bytes[3*i+2] & 0xff) << 0);
		return output;
	}
}

