/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.display;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import imagej.ImageJ;

import org.junit.Test;

/**
 * Unit tests for core {@link Display} classes.
 * 
 * @author Curtis Rueden
 */
public class DisplayTest {

	@Test
	public void testCreate() {
		final ImageJ context = new ImageJ(DisplayService.class);
		final DisplayService displayService =
			context.getService(DisplayService.class);
		final String name = "Create";
		// TODO: Reenable once DefaultDisplay is discoverable again.
//		final Double value = 12.34;
		final String value = "Some text to display.";
		final Display<?> display = displayService.createDisplay(name, value);

		// was the display created successfully?
		assertNotNull(display);

		// does the display have the proper context?
		assertEquals(context, display.getContext());

		// does the display have the proper name?
		assertEquals(name, display.getName());

		// does the display contain the required object?
		final Object result = display.get(0);
		assertNotNull(result);
		assertEquals(value, result);
	}

	@Test
	public void testAddRemove() {
		final ImageJ context = new ImageJ(DisplayService.class);
		final DisplayService displayService =
			context.getService(DisplayService.class);
		final String name = "AddRemove";
		// TODO: Reenable once DefaultDisplay is discoverable again.
//		final Object[] values = { 12.34, 890, 0, 93.73f };
		final Object[] values = { "quick", "brown", "fox", "jumps" };
		@SuppressWarnings("unchecked")
		final Display<Object> display =
			(Display<Object>) displayService.createDisplay(name, values[0]);
		assertNotNull(display);

		for (int i = 1; i < values.length; i++) {
			display.add(values[i]);
		}
		display.update();

		// are all objects present in the display?
		assertFalse(display.isEmpty());
		assertEquals(display.size(), values.length);
		for (int i = 0; i < values.length; i++) {
			final Object result = display.get(i);
			assertEquals(values[i], result);
		}

		// does removing an object work as expected?
		display.remove(0);
		final Object result = display.get(0);
		assertEquals(values[1], result);

		// does removing all objects work as expected?
		for (int i = 1; i < values.length; i++) {
			display.remove(0);
		}
		assertTrue(display.isEmpty());
	}

	@Test
	public void testText() {
		final ImageJ context = new ImageJ(DisplayService.class);
		final DisplayService displayService =
			context.getService(DisplayService.class);
		final String name = "Text";
		final String value = "Hello";
		final Display<?> d = displayService.createDisplay(name, value);
		assertNotNull(d);

		// is the display the expected type?
		assertTrue(d instanceof TextDisplay);
		final TextDisplay textDisplay = (TextDisplay) d;

		// does the display contain the required text string?
		final String result = textDisplay.get(0);
		assertNotNull(result);
		assertEquals(value, result);
	}

}
