package ij.io;

class BargFormat extends PixelFormat
{
	BargFormat()
	{
		super("Barg",4,8,1);  // super(String name, int numSamples, int bitsPerSample, int planes)
	}
	
	boolean canDoImageCombo(int compression, ByteOrder.Value byteOrder, int headerBytes, boolean stripped)
	{
		// top test replaced commented out test after Wayne's changes to ImageReader in 143.s3
		
		if (compression != FileInfo.COMPRESSION_NONE)
			return false;

		//if (compression == FileInfo.COMPRESSION_UNKNOWN)
		//	return false;

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
		byte[] output = new byte[4];
		
		output[0] = (byte)((pix & 0x000000ff) >> 0);
		output[1] = (byte)((pix & 0xff000000) >> 24);
		output[2] = (byte)((pix & 0x00ff0000) >> 16);
		output[3] = (byte)((pix & 0x0000ff00) >> 8);
		
		return output;
	}
	
	byte[] getBytes(long[][] image, int compression, ByteOrder.Value byteOrder, int headerBytes, boolean inStrips, FileInfo fi)
	{
		initializeFileInfo(fi,FileInfo.BARG,compression,byteOrder,image.length,image[0].length);
		
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
		
		// NOTICE that input is barg but output is argb
		
		int i = 0;
		for (long[] row : inputImage)
			for (long pix : row)
				output[i++] = (int)(0xff000000 | (pix & 0xffffff));

		return output;
	}

	Object pixelsFromBytes(byte[] bytes, ByteOrder.Value order)
	{
		// this method not tested by ImageWriter. Therefore no implementation until it will be used.
		return null;
	}
}

