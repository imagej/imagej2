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

package imagej.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests {@link FileUtils}.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
public class FileUtilsTest {

	@Test
	public void testGetExtension() {
		assertEquals("ext", FileUtils.getExtension("/path/to/file.ext"));
		assertEquals("", FileUtils.getExtension("/path/to/file"));
		assertEquals("a", FileUtils.getExtension("/etc/init.d/xyz/file.a"));
		assertEquals("", FileUtils.getExtension("/etc/init.d/xyz/file"));
	}

	@Test
	public void testShortenPath() {
		assertEquals("C:\\Documents and Settings\\"
			+ "All Users\\Application Data\\Apple Computer\\...\\SC Info.txt",
			FileUtils.shortenPath("C:\\Documents and Settings\\All Users"
				+ "\\Application Data\\Apple Computer\\iTunes\\SC Info\\SC Info.txt"));
		assertEquals("C:\\Documents and Settings\\All Users\\Application Data\\"
			+ "Apple Computer\\iTunes\\...\\SC Info.txt", FileUtils.shortenPath(
			"C:\\Documents and Settings\\All Users\\"
				+ "Application Data\\Apple Computer\\iTunes\\SC Info\\SC Info.txt", 5));
		assertEquals("C:\\temp", FileUtils.shortenPath("C:\\temp"));
		assertEquals("C:\\1\\2\\3\\4\\...\\test.txt", FileUtils
			.shortenPath("C:\\1\\2\\3\\4\\5\\test.txt"));
		assertEquals("C:/1/2/test.txt", FileUtils.shortenPath("C:/1/2/test.txt"));
		assertEquals("C:/1/2/3/4/.../test.txt", FileUtils
			.shortenPath("C:/1/2/3/4/5/test.txt"));
		assertEquals("\\\\server\\p1\\p2\\p3\\p4\\...\\p6", FileUtils
			.shortenPath("\\\\server\\p1\\p2\\p3\\p4\\p5\\p6"));
		assertEquals("\\\\server\\p1\\p2\\p3", FileUtils
			.shortenPath("\\\\server\\p1\\p2\\p3"));
		assertEquals("http://www.rgagnon.com/p1/p2/p3/.../pb.html", FileUtils
			.shortenPath("http://www.rgagnon.com/p1/p2/p3/p4/p5/pb.html"));
	}

	@Test
	public void testLimitPath() {
		assertEquals("C:\\Doc...SC Info.txt",
			FileUtils
				.limitPath("C:\\Documents and Settings\\All Users\\"
					+ "Application Data\\Apple Computer\\iTunes\\SC Info\\SC Info.txt",
					20));
		assertEquals("C:\\temp", FileUtils.limitPath("C:\\temp", 20));
		assertEquals("C:\\1\\2\\3\\...test.txt", FileUtils.limitPath(
			"C:\\1\\2\\3\\4\\5\\test.txt", 20));
		assertEquals("...testfile.txt", FileUtils.limitPath("C:/1/2/testfile.txt",
			15));
		assertEquals("C:/1...test.txt", FileUtils.limitPath(
			"C:/1/2/3/4/5/test.txt", 15));
		assertEquals("\\\\server\\p1\\p2\\...p6", FileUtils.limitPath(
			"\\\\server\\p1\\p2\\p3\\p4\\p5\\p6", 20));
		assertEquals("http://www...pb.html", FileUtils.limitPath(
			"http://www.rgagnon.com/p1/p2/p3/p4/p5/pb.html", 20));
	}

}
