//
// FileUtils.java
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

import java.io.File;

/**
 * Useful methods for working with {@link java.io.File} objects.
 * 
 * @author Johannes Schindelin
 */
public final class FileUtils {

	private FileUtils() {
		// prevent instantiation of utility class
	}

	/**
	 * Extract the file extension from a file
	 * 
	 * @param file the file object
	 * @return the file extension, or the empty string when the file name does not
	 *         contain dots
	 */
	public static String getFileExtension(final File file) {
		final String path = file.getPath();
		final int dot = path.lastIndexOf('.');
		if (dot < 0) {
			return "";
		}
		return path.substring(dot + 1);
	}

	/**
	 * Extract the file extension from a file name
	 * 
	 * @param path the path to the file (relative or absolute)
	 * @return the file extension, or the empty string when the file name does not
	 *         contain dots
	 */
	public static String getFileExtension(final String path) {
		return getFileExtension(new File(path));
	}

}
