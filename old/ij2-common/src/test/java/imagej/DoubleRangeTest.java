package imagej;

import static org.junit.Assert.*;

import org.junit.Test;

public class DoubleRangeTest {

	@Test
	public void testInside() {
		assertFalse(DoubleRange.inside(-5.1, 5.1, -10.1));
		assertFalse(DoubleRange.inside(-5.1, 5.1, 10.1));
		assertTrue(DoubleRange.inside(-5.1, 5.1, 0));
		assertTrue(DoubleRange.inside(-5.1, 5.1, -2.67));
		assertTrue(DoubleRange.inside(-5.1, 5.1, 3.73));
	}

	@Test
	public void testBound() {
		assertEquals(-5.1, DoubleRange.bound(-5.1, 5.1, -10.1), 0);
		assertEquals(5.1, DoubleRange.bound(-5.1, 5.1, 10.1), 0);
		assertEquals(0, DoubleRange.bound(-5.1, 5.1, 0), 0);
		assertEquals(-2.67, DoubleRange.bound(-5.1, 5.1, -2.67), 0);
		assertEquals(3.73, DoubleRange.bound(-5.1, 5.1, 3.73), 0);
	}

}
