package ij.io;

// ERROR conditions found in original code that are uncaught
// 1) getBits(99) on a 48 bit buffer just gives the 32 bits - no exception thrown, no overflow testing
// 2) BitBuffer(null) and then getBits(any nonzero number) will generate an uncaught runtime exception

import static org.junit.Assert.*;

import ij.io.BitBuffer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BitBufferTest {
	private BitBuffer bits = null;

	private BitBuffer bitsFromBytes(byte[] bytes){
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
		try{
			bits = bitsFromBytes(null);
			fail();
		} catch (NullPointerException e) {
			assertEquals(true,true);
		}

		bits = bitsFromBytes(new byte[]{1});
        
		assertEquals(true,true);
	}

	@Test
	public void testGetBits() {
	
		// can't test bitsFromBytes(null) followed by getBits() as original code would bomb
		// test against for now and support existing behavior but need to fix in code
		try{
			bits = bitsFromBytes(null);
			bits.getBits(1);
			fail();
		}
		catch (NullPointerException e){
			assertEquals(true,true);
		}

		// test if end of file works with empty buffer
		bits = bitsFromBytes(new byte[] {});

		assertEquals(0,bits.getBits(0));

		// see if EOF works for smallest case
		bits = bitsFromBytes(new byte[] {1});

		bits.getBits(8);
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
		bits = bitsFromBytes(new byte[] {1,2,3});

		assertEquals(1,bits.getBits(8));
		assertEquals(2,bits.getBits(8));
		assertEquals(3,bits.getBits(8));
		assertEquals(-1,bits.getBits(8));

		// see if pulling out more than 8 bits at a time work
		bits = bitsFromBytes(new byte[] {1,3});

		assertEquals(259,bits.getBits(16));
		assertEquals(-1,bits.getBits(1));
		
		// test if seeking past end of file works
		bits = bitsFromBytes(new byte[] {1,1,1,1});

		assertEquals(16843009,bits.getBits(55));  // this behavior is questionable: 55 bits asked for and 32 returned

		// test what happens when we overflow an int and enough data is present
		bits = bitsFromBytes(new byte[] {(byte)(0xff & 255),(byte)(0xff & 255),(byte)(0xff & 255),(byte)(0xff & 255),(byte)(0xff & 255)});
		
		// have to supply IJ's current return value for now as code will only return 32 bits correctly and then -1 beyond that
		assertEquals(-1,bits.getBits(33));
	}

}
