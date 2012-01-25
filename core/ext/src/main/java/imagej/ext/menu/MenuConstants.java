//
// MenuConstants.java
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

package imagej.ext.menu;

/**
 * Useful constants when defining ImageJ menu entries.
 * 
 * @author Curtis Rueden
 */
public final class MenuConstants {

	private MenuConstants() {
		// prevent instantiation of utility class
	}

	// TODO - Finalize the location of these constants. The potential confusion is
	// that everything in imagej.ext is part of the general-purpose extensibility
	// framework, which could be used by applications other than ImageJ, except
	// for these constants, which are very specific to the ImageJ user interface.

	public static final String FILE_LABEL = "File";
	public static final String EDIT_LABEL = "Edit";
	public static final String IMAGE_LABEL = "Image";
	public static final String PROCESS_LABEL = "Process";
	public static final String ANALYZE_LABEL = "Analyze";
	public static final String PLUGINS_LABEL = "Plugins";
	public static final String WINDOW_LABEL = "Window";
	public static final String HELP_LABEL = "Help";

	public static final double FILE_WEIGHT = 0;
	public static final double EDIT_WEIGHT = 1;
	public static final double IMAGE_WEIGHT = 2;
	public static final double PROCESS_WEIGHT = 3;
	public static final double ANALYZE_WEIGHT = 4;
	public static final double PLUGINS_WEIGHT = 5;
	public static final double WINDOW_WEIGHT = 6;
	public static final double HELP_WEIGHT = 1e7;

	public static final char FILE_MNEMONIC = 'f';
	public static final char EDIT_MNEMONIC = 'e';
	public static final char IMAGE_MNEMONIC = 'i';
	public static final char PROCESS_MNEMONIC = 'p';
	public static final char ANALYZE_MNEMONIC = 'a';
	public static final char PLUGINS_MNEMONIC = 'u';
	public static final char WINDOW_MNEMONIC = 'w';
	public static final char HELP_MNEMONIC = 'h';

}
