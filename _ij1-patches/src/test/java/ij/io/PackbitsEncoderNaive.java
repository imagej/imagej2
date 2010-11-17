package ij.io;

// PackbitsEncoderNaive is designed with two things in mind
//   - test ImageReader's ability to handle more than one style of packbits encoded data
//   - stand in for the real PackBitsEncoder if we think it is ever faulty

class PackbitsEncoderNaive {
	
	PackbitsEncoderNaive() {}
	
	static public byte[] encode(byte[] input)
	{
		byte[] output = new byte[input.length*2];
		int i = 0;
		for (byte b : input)
		{
			output[i++] = 0;
			output[i++] = b;
		}
		return output;
	}
}

