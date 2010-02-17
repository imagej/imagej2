package ij.io;

class Rgb48Format extends PixelFormat
{
	Rgb48Format()
	{
		super("Rgb48",3,16,1);  // super(String name, int numSamples, int bitsPerSample, int planes)
	}
	
	boolean canDoImageCombo(int compression, ByteOrder.Value byteOrder, int headerBytes, boolean stripped)
	{
		if (compression == FileInfo.LZW_WITH_DIFFERENCING)
			return false;
		if (compression == FileInfo.COMPRESSION_UNKNOWN)
			return false;
		if (compression == FileInfo.JPEG)  // TODO: remove this restriction to test jpeg compression
			return false;

		// this class always works with strips
		if (stripped == false)
			return false;
		
		return true;
	}

	byte[] nativeBytes(long pix, ByteOrder.Value byteOrder)
	{
		byte[] output = new byte[6];
		
		
		if (byteOrder == ByteOrder.Value.INTEL)
		{
			output[0] = (byte)((pix & 0xff00000000L) >> 32);
			output[1] = (byte)((pix & 0xff0000000000L) >> 40);
			output[2] = (byte)((pix & 0xff0000L) >> 16);
			output[3] = (byte)((pix & 0xff000000L) >> 24);
			output[4] = (byte)((pix & 0xffL) >> 0);
			output[5] = (byte)((pix & 0xff00L) >> 8);
		}
		else
		{
			output[0] = (byte)((pix & 0xff0000000000L) >> 40);
			output[1] = (byte)((pix & 0xff00000000L) >> 32);
			output[2] = (byte)((pix & 0xff000000L) >> 24);
			output[3] = (byte)((pix & 0xff0000L) >> 16);
			output[4] = (byte)((pix & 0xff00L) >> 8);
			output[5] = (byte)((pix & 0xffL) >> 0);
		}
		
		return output;
	}
	
	byte[] getBytes(long[][] image, int compression, ByteOrder.Value byteOrder, int headerBytes, boolean inStrips, FileInfo fi)
	{
		initializeFileInfo(fi,FileInfo.RGB48,compression,byteOrder,image.length,image[0].length);

		// ALWAYS only do stripped data for this format
		
		byte[] output = PixelArranger.arrangeInStrips(this,image,fi);

		output = PixelArranger.attachHeader(fi,headerBytes,output);
		
		return output;
	}

	Object expectedResults(long[][] inputImage)
	{
		int rows = inputImage.length;
		int cols = inputImage[0].length;

		short[][] output = new short[3][];
		
		for (int i = 0; i < 3; i++)
			output[i] = new short[rows*cols];
		
		int i = 0;
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < cols; c++)
			{
				output[0][i] = (short) ((inputImage[r][c] & 0xffff00000000L) >> 32);
				output[1][i] = (short) ((inputImage[r][c] & 0x0000ffff0000L) >> 16);
				output[2][i] = (short) ((inputImage[r][c] & 0x00000000ffffL) >> 0);
				i++;
			}
		
		return output;
	}		

	Object pixelsFromBytes(byte[] bytes)
	{
		int numPix = bytes.length / 6;

		short[][] output = new short[3][numPix];
		
		//output[0] = (byte)((pix & 0xff0000000000L) >> 40);
		//output[1] = (byte)((pix & 0xff00000000L) >> 32);
		//output[2] = (byte)((pix & 0xff000000L) >> 24);
		//output[3] = (byte)((pix & 0xff0000L) >> 16);
		//output[4] = (byte)((pix & 0xff00L) >> 8);
		//output[5] = (byte)((pix & 0xffL) >> 0);

		int i = 0;
		for (int j = 0; j < numPix; j++)
		{
			output[0][j] = (short) (((bytes[i+0] & 0xff) << 8) | (bytes[i+1] & 0xff));
			output[1][j] = (short) (((bytes[i+2] & 0xff) << 8) | (bytes[i+3] & 0xff));
			output[2][j] = (short) (((bytes[i+4] & 0xff) << 8) | (bytes[i+5] & 0xff));
			i += 6;
		}
		
		return output;
	}
}

