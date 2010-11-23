package ijx.io;

/**
 * A class for reading arbitrary numbers of bits from a byte array.
 * @author Eric Kjellman egkjellman at wisc.edu
 */
public class BitBuffer {

	private int currentByte;
	private int currentBit;
	private byte[] byteBuffer;
	private int eofByte;
	private int[] backMask;
	private int[] frontMask;
	private boolean eofFlag;

	public BitBuffer(byte[] byteBuffer) {
		this.byteBuffer = byteBuffer;
		currentByte = 0;
		currentBit = 0;
		eofByte = byteBuffer.length;
		backMask = new int[] {0x0000, 0x0001, 0x0003, 0x0007,
			0x000F, 0x001F, 0x003F, 0x007F};
		frontMask = new int[] {0x0000, 0x0080, 0x00C0, 0x00E0,
			0x00F0, 0x00F8, 0x00FC, 0x00FE};
	}

	public int getBits(int bitsToRead) {
		if (bitsToRead == 0)
			return 0;
		if (eofFlag)
			return -1; // Already at end of file
		int toStore = 0;
		while(bitsToRead != 0  && !eofFlag) {
			if (bitsToRead >= 8 - currentBit) {
				if (currentBit == 0) { // special
					toStore = toStore << 8;
					int cb = ((int) byteBuffer[currentByte]);
					toStore += (cb<0 ? (int) 256 + cb : (int) cb);
					bitsToRead -= 8;
					currentByte++;
				} else {
					toStore = toStore << (8 - currentBit);
					toStore += ((int) byteBuffer[currentByte]) & backMask[8 - currentBit];
					bitsToRead -= (8 - currentBit);
					currentBit = 0;
					currentByte++;
				}
			} else {
				toStore = toStore << bitsToRead;
				int cb = ((int) byteBuffer[currentByte]);
				cb = (cb<0 ? (int) 256 + cb : (int) cb);
				toStore += ((cb) & (0x00FF - frontMask[currentBit])) >> (8 - (currentBit + bitsToRead));
				currentBit += bitsToRead;
				bitsToRead = 0;
			}
			if (currentByte == eofByte) {
				eofFlag = true;
				return toStore;
			}
		}
		return toStore;
	}

}
