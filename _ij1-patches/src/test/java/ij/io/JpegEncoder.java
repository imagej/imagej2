package ij.io;

import loci.formats.codec.CodecOptions;
import loci.formats.codec.JPEGCodec;

class JpegEncoder {
	JpegEncoder() {}
	
	static public byte[] encode(byte[] input, PixelFormat format, FileInfo fi)
	{
		byte[] output = null;
		try {
			CodecOptions codecOptions = new CodecOptions();
			codecOptions.height = fi.height;
			codecOptions.width = fi.width;
			codecOptions.channels = format.numSamples();
			codecOptions.bitsPerSample = format.bitsPerSample();
			codecOptions.interleaved = (format.planes() == 1);
			codecOptions.littleEndian = fi.intelByteOrder;
			//codecOptions.signed = (format == gray16SignedFormat);
			codecOptions.signed = false;
			output = new JPEGCodec().compress(input, codecOptions);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return output;
	}
}

