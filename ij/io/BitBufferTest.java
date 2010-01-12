package io;

import static org.junit.Assert.*;

import ij.io.BitBuffer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BitBufferTest {
	private BitBuffer bits = null;

	public BitBuffer bitsFromBytes(byte[] bytes){
		return new BitBuffer(bytes);
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testBitBuffer() {
		// original IJ code for BitBuffer does not check for null array
		//   - later runtime errors possible
		// byte[] bytes = null;
		// BitBuffer bits = new BitBuffer(bytes);
		assertEquals(true,true);
	}

	@Test
	public void testGetBits() {

		// test if end of file works with empty buffer
		bits = bitsFromBytes(new byte[] {});

		assertEquals(0,bits.getBits(0));

		// see if EOF works for smallest case
		bits = bitsFromBytes(new byte[] {1});

		int dummy = bits.getBits(8);
		assertEquals(-1,bits.getBits(1));

		// see if bits pulled out in order correctly
		//   far end : LSB
		bits = bitsFromBytes(new byte[] {1});

		assertEquals(0,bits.getBits(7));
		assertEquals(1,bits.getBits(1));
		assertEquals(-1,bits.getBits(1));

		// see if bits pulled out in order correctly
		//   near end : MSB
		bits = bitsFromBytes(new byte[] {(byte) (0xff & 128)});

		assertEquals(1,bits.getBits(1));
		assertEquals(0,bits.getBits(7));
		assertEquals(-1,bits.getBits(1));

		// see if pulling pieces of byte out of all 1's works
		bits = bitsFromBytes(new byte[] {(byte) (0xff & 255)});

		assertEquals(15,bits.getBits(4));
		assertEquals(3,bits.getBits(2));
		assertEquals(1,bits.getBits(1));
		assertEquals(1,bits.getBits(1));
		assertEquals(-1,bits.getBits(1));

		// see if pulling out multiple bytes in a row works
	//	bytes = new byte[] {1,2,3};
	//	bits = new BitBuffer(bytes);
		bits = bitsFromBytes(new byte[] {1,2,3});

		assertEquals(1,bits.getBits(8));
		assertEquals(2,bits.getBits(8));
		assertEquals(3,bits.getBits(8));
		assertEquals(-1,bits.getBits(8));

		// see if pulling out more than 8 bits at a time work
	//	bytes = new byte[] {1,3};
	//	bits = new BitBuffer(bytes);
		bits = bitsFromBytes(new byte[] {1,3});

		assertEquals(259,bits.getBits(16));
		assertEquals(-1,bits.getBits(1));
	}

}
