package ij.io;

class TwelveBitEncoder
{
	TwelveBitEncoder() {}
	
	static public byte[] encode(long[][] inPix)
	{
		int rows = inPix.length;
		int cols = inPix[0].length;
		
		int bytesPerRow = (int) Math.ceil(cols * 1.5); // 1.5 bytes per pix
		
		byte[] output = new byte[rows * bytesPerRow];

		int o = 0;
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < cols; c++)
				if (c%2 == 0) // even numbered column
				{
					// set this byte's 8 bits plus next byte's high 4 bits
					// use 12 bits of the input int
					output[o] = (byte)((inPix[r][c] & 0xff0) >> 4) ;
					output[o+1] = (byte)((inPix[r][c] & 0x00f) << 4);				
					o += 1;  // finished 1 pixel
					if (c == cols-1)
						o += 1; // if end of row then next pixel completed also
				}
				else // odd numbered column
				{
					// set this byte's low 4 bits and next byte's 8 bits
					// use 12 bits of the input int
					output[o] = (byte)(output[o] | ((inPix[r][c] & 0xf00) >> 8));
					output[o+1] = (byte)(inPix[r][c] & 0x0ff);
					o += 2;  // finished 2 pixels
				}
		
		return output;
	}
}

