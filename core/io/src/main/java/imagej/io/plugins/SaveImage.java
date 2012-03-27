//
// SaveImage.java
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

package imagej.io.plugins;

import imagej.data.Dataset;
import imagej.ext.menu.MenuConstants;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.ext.plugin.PluginService;

import java.io.File;
import java.util.HashMap;

import net.imglib2.img.ImgPlus;

/**
 * Saves the current {@link Dataset} to disk.
 * 
 * @author Barry DeZonia
 * @author Mark Hiner
 */
@Plugin(menu = {
	@Menu(label = MenuConstants.FILE_LABEL, weight = MenuConstants.FILE_WEIGHT,
		mnemonic = MenuConstants.FILE_MNEMONIC),
	@Menu(label = "Save", weight = 20, mnemonic = 's') })
public class SaveImage implements ImageJPlugin {

	@Parameter
	private PluginService pluginService;

	@Parameter
	Dataset dataset;

	@Override
	public void run() {
		final HashMap<String, Object> inputMap = new HashMap<String, Object>();
		inputMap.put("dataset", dataset);

		final ImgPlus<?> img = dataset.getImgPlus();
		final String source = img.getSource();

		final File sourceFile = source.isEmpty() ? null : new File(source);

		if (sourceFile != null && sourceFile.isFile()) {
			inputMap.put("outputFile", new File(source));
		}
		pluginService.run(SaveAsImage.class, inputMap);
	}

}
