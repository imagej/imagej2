package ij.io;

class Gray16SignedFormat extends PixelFormat
{
	Gray16SignedFormat()
	{
		super("Gray16Signed",1,16,1);  // super(String name, int numSamples, int bitsPerSample, int planes)
	}
	
	boolean canDoImageCombo(int compression, ByteOrder.Value byteOrder, int headerBytes, boolean stripped)
	{
		if (compression == FileInfo.COMPRESSION_UNKNOWN)
			return false;
		if (compression == FileInfo.JPEG)
			return false;
		if (compression == FileInfo.PACK_BITS)
			return false;
		
		if (stripped && (compression == FileInfo.COMPRESSION_NONE))
			return false;
		
		return true;
	}
	
	byte[] nativeBytes(long pix, ByteOrder.Value byteOrder)
	{
		byte[] output = new byte[2];
		
		output[0] = (byte)((pix & 0xff00) >> 8);
		output[1] = (byte)((pix & 0x00ff) >> 0);
		
		if (byteOrder == ByteOrder.Value.INTEL)
			PixelArranger.reverse(output);
		
		return output;
	}
	
	byte[] getBytes(long[][] image, int compression, ByteOrder.Value byteOrder, int headerBytes, boolean inStrips, FileInfo fi)
	{
		initializeFileInfo(fi,FileInfo.GRAY16_SIGNED,compression,byteOrder,image.length,image[0].length);

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
		short[] output = new short[inputImage.length * inputImage[0].length];
		
		int i = 0;
		for (long[] row : inputImage)
			for (long pix : row)
				output[i++] = (short) (32768 + (pix & 0xffff)); // bias taken from ImageReader.readPixels()
		return output;
	}

	Object pixelsFromBytes(byte[] bytes, ByteOrder.Value order)
	{
		int numShorts = bytes.length / 2;
		short[] output = new short[numShorts];
		
		for (int i = 0; i < numShorts; i++)
		{
			//TODO figure out why biasing not needed here
			if (order == ByteOrder.Value.INTEL)
				output[i] = (short)(((bytes[2*i+1] & 0xff) << 8) | (bytes[2*i] & 0xff));
			else
				output[i] = (short)(((bytes[2*i] & 0xff) << 8) | (bytes[2*i+1] & 0xff));
		}
		return output;
	}
}

