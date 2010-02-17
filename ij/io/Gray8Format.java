package ij.io;

class Gray8Format extends PixelFormat
{
	Gray8Format()
	{
		super("Gray8",1,8,1);  // super(String name, int numSamples, int bitsPerSample, int planes)
	}
	
	boolean canDoImageCombo(int compression, ByteOrder.Value byteOrder, int headerBytes, boolean stripped)
	{
		if (compression == FileInfo.COMPRESSION_UNKNOWN)
			return false;
		if (compression == FileInfo.JPEG)
			return false;
		
		if (byteOrder == ByteOrder.Value.INTEL)
			return false;

		if (stripped && (compression == FileInfo.COMPRESSION_NONE))
			return false;

		return true;
	}
	
	byte[] nativeBytes(long pix, ByteOrder.Value byteOrder)
	{
		return new byte[] {(byte)(pix & 0xff)};
	}
	
	byte[] getBytes(long[][] image, int compression, ByteOrder.Value byteOrder, int headerBytes, boolean inStrips, FileInfo fi)
	{
		initializeFileInfo(fi,FileInfo.GRAY8,compression,byteOrder,image.length,image[0].length);

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
		byte[] output = new byte[inputImage.length * inputImage[0].length];
		
		int i = 0;
		for (long[] row : inputImage)
			for (long pix : row)
				output[i++] = (byte) (pix & 0xff);
		
		return output;
	}

	Object pixelsFromBytes(byte[] bytes)
	{
		return bytes;
	}
}

