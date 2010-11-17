package ij.io;

abstract class PixelFormat {
	
	private String name;  // might be useful for debugging purposes. otherwise could be an interface
	private int numSamples;
	private int bitsPerSample;
	private int planes;
	
	public int numSamples()    { return numSamples; }
	public int bitsPerSample() { return bitsPerSample; }
	public int planes()        { return planes; }
	public String name()       { return name; }
	
	
	abstract boolean canDoImageCombo(int compression, ByteOrder.Value byteOrder, int headerBytes, boolean stripped);
	abstract byte[] nativeBytes(long pixel, ByteOrder.Value byteOrder);
	abstract byte[] getBytes(long[][] image, int compression, ByteOrder.Value byteOrder, int headerBytes, boolean inStrips, FileInfo fi);
	abstract Object expectedResults(long[][] inputImage);
	abstract Object pixelsFromBytes(byte[] bytes, ByteOrder.Value byteOrder);
	
	PixelFormat(String name, int numSamples, int bitsPerSample, int planes)
	{
		this.name = name;
		this.numSamples = numSamples;
		this.bitsPerSample = bitsPerSample;
		this.planes = planes;
	}
	
	// since each format calls this method I've located it in PixelFormat. But it could be a static method somewhere else
	
	protected void initializeFileInfo(FileInfo fi, int ftype, int compression, ByteOrder.Value byteOrder, int rows, int cols)
	{
		fi.fileType = ftype;
		fi.compression = compression;
		if (byteOrder == ByteOrder.Value.INTEL)
			fi.intelByteOrder = true;
		else
			fi.intelByteOrder = false;
		fi.height = rows;
		fi.width = cols;
	}
	
}
