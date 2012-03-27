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

package imagej.options.plugins;

import imagej.ext.menu.MenuConstants;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.options.OptionsPlugin;

/**
 * Runs the Edit::Options::Input/Output dialog.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = OptionsPlugin.class, menu = {
	@Menu(label = MenuConstants.EDIT_LABEL, weight = MenuConstants.EDIT_WEIGHT,
		mnemonic = MenuConstants.EDIT_MNEMONIC),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Input/Output...", weight = 2) })
public class OptionsInputOutput extends OptionsPlugin {

	@Parameter(label = "JPEG quality (0-100)", min = "0", max = "100")
	private int jpegQuality = 85;

	@Parameter(label = "GIF and PNG transparent index")
	private int transparentIndex = -1;

	@Parameter(label = "File extension for tables")
	private String tableFileExtension = ".txt";

	@Parameter(label = "Use JFileChooser to open/save")
	private boolean useJFileChooser = false;

	@Parameter(label = "Save TIFF and raw in Intel byte order")
	private boolean saveOrderIntel = false;

	// TODO - in IJ1 these were grouped visually. How is this now done?

	@Parameter(label = "Result Table: Copy column headers")
	private boolean copyColumnHeaders = false;

	@Parameter(label = "Result Table: Copy row numbers")
	private boolean copyRowNumbers = true;

	@Parameter(label = "Result Table: Save column headers")
	private boolean saveColumnHeaders = true;

	@Parameter(label = "Result Table: Save row numbers")
	private boolean saveRowNumbers = true;

	// -- OptionsInputOutput methods --

	public OptionsInputOutput() {
		load(); // NB: Load persisted values *after* field initialization.
	}

	public int getJpegQuality() {
		return jpegQuality;
	}

	public int getTransparentIndex() {
		return transparentIndex;
	}

	public String getTableFileExtension() {
		return tableFileExtension;
	}

	public boolean isUseJFileChooser() {
		return useJFileChooser;
	}

	public boolean isSaveOrderIntel() {
		return saveOrderIntel;
	}

	public boolean isCopyColumnHeaders() {
		return copyColumnHeaders;
	}

	public boolean isCopyRowNumbers() {
		return copyRowNumbers;
	}

	public boolean isSaveColumnHeaders() {
		return saveColumnHeaders;
	}

	public boolean isSaveRowNumbers() {
		return saveRowNumbers;
	}

	public void setJpegQuality(final int jpegQuality) {
		this.jpegQuality = jpegQuality;
	}

	public void setTransparentIndex(final int transparentIndex) {
		this.transparentIndex = transparentIndex;
	}

	public void setTableFileExtension(final String tableFileExtension) {
		this.tableFileExtension = tableFileExtension;
	}

	public void setUseJFileChooser(final boolean useJFileChooser) {
		this.useJFileChooser = useJFileChooser;
	}

	public void setSaveOrderIntel(final boolean saveOrderIntel) {
		this.saveOrderIntel = saveOrderIntel;
	}

	public void setCopyColumnHeaders(final boolean copyColumnHeaders) {
		this.copyColumnHeaders = copyColumnHeaders;
	}

	public void setCopyRowNumbers(final boolean copyRowNumbers) {
		this.copyRowNumbers = copyRowNumbers;
	}

	public void setSaveColumnHeaders(final boolean saveColumnHeaders) {
		this.saveColumnHeaders = saveColumnHeaders;
	}

	public void setSaveRowNumbers(final boolean saveRowNumbers) {
		this.saveRowNumbers = saveRowNumbers;
	}

}
