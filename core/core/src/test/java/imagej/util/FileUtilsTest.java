//
// FileUtilsTest.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
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
	public void testShortenPath() {
		assertEquals(FileUtils.shortenPath("C:\\Documents and Settings\\All Users"
			+ "\\Application Data\\Apple Computer\\iTunes\\SC Info\\SC Info.txt"),
			"C:\\Documents and Settings\\All Users\\"
				+ "Application Data\\Apple Computer\\...\\SC Info.txt");
		assertEquals(FileUtils.shortenPath(
			"C:\\Documents and Settings\\All Users\\"
				+ "Application Data\\Apple Computer\\iTunes\\SC Info\\SC Info.txt", 5),
			"C:\\Documents and Settings\\All Users\\"
				+ "Application Data\\Apple Computer\\iTunes\\...\\SC Info.txt");
		assertEquals(FileUtils.shortenPath("C:\\temp"), "C:\\temp");
		assertEquals(FileUtils.shortenPath("C:\\1\\2\\3\\4\\5\\test.txt"),
			"C:\\1\\2\\3\\4\\...\\test.txt");
		assertEquals(FileUtils.shortenPath("C:/1/2/test.txt"), "C:/1/2/test.txt");
		assertEquals(FileUtils.shortenPath("C:/1/2/3/4/5/test.txt"),
			"C:/1/2/3/4/.../test.txt");
		assertEquals(FileUtils.shortenPath("\\\\server\\p1\\p2\\p3\\p4\\p5\\p6"),
			"\\\\server\\p1\\p2\\p3\\p4\\...\\p6");
		assertEquals(FileUtils.shortenPath("\\\\server\\p1\\p2\\p3"),
			"\\\\server\\p1\\p2\\p3");
		assertEquals(FileUtils
			.shortenPath("http://www.rgagnon.com/p1/p2/p3/p4/p5/pb.html"),
			"http://www.rgagnon.com/p1/p2/p3/.../pb.html");
	}

	@Test
	public void testLimitPath() {
		assertEquals(FileUtils.limitPath("C:\\Documents and Settings\\All Users\\"
			+ "Application Data\\Apple Computer\\iTunes\\SC Info\\SC Info.txt", 20),
			"C:\\Doc...SC Info.txt");
		assertEquals(FileUtils.limitPath("C:\\temp", 20), "C:\\temp");
		assertEquals(FileUtils.limitPath("C:\\1\\2\\3\\4\\5\\test.txt", 20),
			"C:\\1\\2\\3\\...test.txt");
		assertEquals(FileUtils.limitPath("C:/1/2/testfile.txt", 15),
			"...testfile.txt");
		assertEquals(FileUtils.limitPath("C:/1/2/3/4/5/test.txt", 15),
			"C:/1...test.txt");
		assertEquals(FileUtils.limitPath("\\\\server\\p1\\p2\\p3\\p4\\p5\\p6", 20),
			"\\\\server\\p1\\p2\\...p6");
		assertEquals(FileUtils.limitPath(
			"http://www.rgagnon.com/p1/p2/p3/p4/p5/pb.html", 20),
			"http://www...pb.html");
	}

}
