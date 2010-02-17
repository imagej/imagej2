package ij.io;

class RgbPlanarFormat extends PixelFormat
{
	RgbPlanarFormat()
	{
		super("RgbPlanar",3,8,3);  // super(String name, int numSamples, int bitsPerSample, int planes)
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

		output[0] = (byte)((pix & 0x00ff0000) >> 16);
		output[1] = (byte)((pix & 0x0000ff00) >> 8);
		output[2] = (byte)((pix & 0x000000ff) >> 0);
		
		return output;
	}
	
	byte[] getBytes(long[][] image, int compression, ByteOrder.Value byteOrder, int headerBytes, boolean inStrips, FileInfo fi)
	{
		initializeFileInfo(fi,FileInfo.RGB_PLANAR,compression,byteOrder,image.length,image[0].length);
		
		byte[] output = PixelArranger.arrangeAsPlanes(this, image, fi, inStrips, false);
		
		output = PixelArranger.attachHeader(fi,headerBytes,output);

		return output;
	}

	Object expectedResults(long[][] inputImage)
	{
		int[] output = new int[inputImage.length * inputImage[0].length];
		
		// NOTICE input is rgb planar but output is argb
		
		int i = 0;
		for (long[] row : inputImage)
			for (long pix : row)
				output[i++] = (int)(0xff000000 | (pix & 0xffffff));

		return output;
	}

	Object pixelsFromBytes(byte[] bytes)
	{
		// this method not tested by ImageWriter. Therefore no implementation until it will be used.
		return null;
	}
}

