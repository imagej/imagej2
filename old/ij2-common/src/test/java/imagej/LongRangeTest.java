package imagej;

import static org.junit.Assert.*;

import org.junit.Test;

public class LongRangeTest {

	@Test
	public void testInside() {
		assertFalse(LongRange.inside(-5, 5, -10));
		assertFalse(LongRange.inside(-5, 5, 10));
		assertTrue(LongRange.inside(-5, 5, 0));
		assertTrue(LongRange.inside(-5, 5, -2));
		assertTrue(LongRange.inside(-5, 5, 3));
	}

	@Test
	public void testBound() {
		assertEquals(-5, LongRange.bound(-5, 5, -10));
		assertEquals(5, LongRange.bound(-5, 5, 10));
		assertEquals(0, LongRange.bound(-5, 5, 0));
		assertEquals(-2, LongRange.bound(-5, 5, -2));
		assertEquals(3, LongRange.bound(-5, 5, 3));
	}

}
