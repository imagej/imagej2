package ij.io;

public class LzwDiffEncoder {
	
	LzwDiffEncoder() {}
	
	// unfortunately this method needed to be tweaked outside the overall design to accommodate intel byte orders for 16 bit
	//   sample depths. We could eliminate the extra code at the expense of redesigning some of the other classes.
	
	static private byte[] differentiate(byte[] input, PixelFormat format, FileInfo fi)
	{
		int offset = format.numSamples();
		
		// multiplane data is stored contiguously a plane at a time : offset to neighbor byte
		if (format.planes() > 1)
			offset = 1;
		
		byte[] data = input.clone();
		
		// if 16 bit samples must calc differences on original pixel data and not bytes
		if (format.bitsPerSample() == 16)
		{
			int actualPix = data.length / 2;
			
			for (int b = actualPix-1; b >= 0; b--)
			{
				if (b / offset % fi.width == 0)
					continue;
				
				int currPix, prevPix;
				
				if (fi.intelByteOrder)
				{
					currPix = ((data[b*2+0] & 0xff) << 0) | ((data[b*2+1] & 0xff) << 8);
					prevPix = ((data[b*2-2] & 0xff) << 0) | ((data[b*2-1] & 0xff) << 8);
					currPix -= prevPix;
					data[b*2+0] = (byte)((currPix & 0x00ff) >> 0);
					data[b*2+1] = (byte)((currPix & 0xff00) >> 8);
				}
				else
				{
					currPix = ((data[b*2+0] & 0xff) << 8) | ((data[b*2+1] & 0xff) << 0);
					prevPix = ((data[b*2-2] & 0xff) << 8) | ((data[b*2-1] & 0xff) << 0);
					currPix -= prevPix;
					data[b*2+0] = (byte)((currPix & 0xff00) >> 8);
					data[b*2+1] = (byte)((currPix & 0x00ff) >> 0);
				}
			}
			
		}
		else // input data is of type byte and we can calc differences as is
			for (int b=data.length-1; b>=0; b--)
			{
				// this code adapted from TiffCompression code in BioFormats
				if (b / offset % fi.width == 0)
					continue;
				data[b] -= data[b - offset];
			}
	      
		return data;
	}
	
	static public byte[] encode(byte[] input, PixelFormat format, FileInfo fi)
	{
		input = differentiate(input, format, fi);
		
		byte[] output = LzwEncoder.encode(input);

		return output;
	}
}

