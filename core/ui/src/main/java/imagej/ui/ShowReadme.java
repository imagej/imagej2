//
// ShowReadme.java
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

package imagej.ui;

import imagej.ext.module.ItemIO;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.util.FileUtils;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Displays the ImageJ readme file.
 * 
 * @author Curtis Rueden
 */
@Plugin
public class ShowReadme implements ImageJPlugin {

	private static final String README_FILE = "README.txt";

	@Parameter(label = "Readme", type = ItemIO.OUTPUT)
	private String readmeText;

	// -- ShowReadme methods --

	public String getReadmeText() {
		return readmeText;
	}

	// -- Runnable methods --

	@Override
	public void run() {
		readmeText = loadReadmeFile();
	}

	// -- Helper methods --

	private String loadReadmeFile() {
		final File baseDir = FileUtils.getImageJDirectory();
		final File readmeFile = new File(baseDir, README_FILE);

		try {
			final DataInputStream in =
				new DataInputStream(new FileInputStream(readmeFile));
			final int len = (int) readmeFile.length();
			final byte[] bytes = new byte[len];
			in.readFully(bytes);
			in.close();
			return new String(bytes);
		}
		catch (final FileNotFoundException e) {
			throw new IllegalArgumentException(README_FILE + " not found at " +
				baseDir.getAbsolutePath());
		}
		catch (final IOException e) {
			throw new IllegalStateException(e.getMessage());
		}
	}

}
