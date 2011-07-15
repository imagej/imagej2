//
// RevertImage.java
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

import javax.swing.JOptionPane;

import imagej.data.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Plugin;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 * Resets the current {@link Dataset} to its original state.
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = { @Menu(label = "File", mnemonic = 'f'),
	@Menu(label = "Revert [IJ2]", weight = 20, mnemonic = 'r'
//	 , accelerator = "control R"
	) })
public class RevertImage<T extends RealType<T> & NativeType<T>> implements
	ImageJPlugin
{
	//@Parameter
	//private Dataset dataset;

	@Override
	public void run() {
		JOptionPane.showMessageDialog(null, "This feature has not been implemented");

		/*
		// TODO - enable this in ImgLib
//		final String id = currDataset.getImgPlus().getSource();
//		if (id == null) {
//			throw new IllegalArgumentException("Dataset " + currDataset.getName() +
//				" does not have an external source");
//		}
		final String id = null; // do nothing right now

		// open image
		final ImgOpener imageOpener = new ImgOpener();
		try {
			final ImgPlus<T> imgPlus = imageOpener.openImg(id);
			dataset.setImgPlus(imgPlus);
		}
		catch (final ImgIOException e) {
			Log.error(e);
		}
		catch (final IncompatibleTypeException e) {
			Log.error(e);
		}
		 */
	}

}
