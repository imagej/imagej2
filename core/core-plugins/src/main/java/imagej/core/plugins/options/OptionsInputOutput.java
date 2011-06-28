//
// OptionsInputOutput.java
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
package imagej.core.plugins.options;

import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.util.SettingsKeys;

/**
 * Runs the Edit::Options::Input/Output... dialog
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = "Edit", mnemonic = 'e'),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Input/Output...", weight = 2) })
public class OptionsInputOutput extends OptionsPlugin {

	@Parameter(label = "JPEG quality (0-100)",
		persistKey = SettingsKeys.OPTIONS_IO_JPEG_QUALITY)
	private int jpegQuality;
	
	@Parameter(label = "GIF and PNG transparent index",
		persistKey = SettingsKeys.OPTIONS_IO_TRANSPARENT_INDEX)
	private int transparentIndex;

	@Parameter(label = "File extension for tables",
		persistKey = SettingsKeys.OPTIONS_IO_FILE_EXT)
	private String tableFileExtension;

	@Parameter(label = "Use JFileChooser to open/save",
		persistKey = SettingsKeys.OPTIONS_IO_USE_JFILECHOOSER)
	private boolean useJFileChooser;

	@Parameter(label = "Save TIFF and raw in Intel byte order",
		persistKey = SettingsKeys.OPTIONS_IO_SAVE_INTEL)
	private boolean saveOrderIntel;
	
	// TODO - in IJ1 these were grouped visually. How is this now done?
	
	@Parameter(label = "Result Table: Copy column headers",
		persistKey = SettingsKeys.OPTIONS_IO_COPY_COLUMNS)
	private boolean copyColumnHeaders;
	
	@Parameter(label = "Result Table: Copy row numbers",
		persistKey = SettingsKeys.OPTIONS_IO_COPY_ROWS)
	private boolean copyRowNumbers;

	@Parameter(label = "Result Table: Save column headers",
		persistKey = SettingsKeys.OPTIONS_IO_SAVE_COLUMNS)
	private boolean saveColumnHeaders;
	
	@Parameter(label = "Result Table: Save row numbers",
		persistKey = SettingsKeys.OPTIONS_IO_SAVE_ROWS)
	private boolean saveRowNumbers;

}
