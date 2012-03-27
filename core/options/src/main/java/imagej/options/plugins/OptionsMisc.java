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
 * Runs the Edit::Options::Misc dialog.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = OptionsPlugin.class, menu = {
	@Menu(label = MenuConstants.EDIT_LABEL, weight = MenuConstants.EDIT_WEIGHT,
		mnemonic = MenuConstants.EDIT_MNEMONIC),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Misc...", weight = 17) })
public class OptionsMisc extends OptionsPlugin {

	// TODO - use double instead of string for divide by zero value?
	@Parameter(label = "Divide by zero value")
	private String divByZeroVal = "Infinity";

	@Parameter(label = "Use pointer cursor")
	private boolean usePtrCursor = false;

	@Parameter(label = "Hide \"Process Stack?\" dialog")
	private boolean hideProcessStackDialog = false;

	@Parameter(label = "Require command key for shortcuts")
	private boolean requireCommandKey = false;

	@Parameter(label = "Move isolated plugins to Misc. menu")
	private boolean moveIsolatedPlugins = false;

	@Parameter(label = "Run single instance listener")
	private boolean runSingleInstanceListener = false;

	@Parameter(label = "Debug mode")
	private boolean debugMode = false;

	// -- OptionsMisc methods --

	public OptionsMisc() {
		load(); // NB: Load persisted values *after* field initialization.
	}

	public String getDivByZeroVal() {
		return divByZeroVal;
	}

	public boolean isUsePtrCursor() {
		return usePtrCursor;
	}

	public boolean isHideProcessStackDialog() {
		return hideProcessStackDialog;
	}

	public boolean isRequireCommandKey() {
		return requireCommandKey;
	}

	public boolean isMoveIsolatedPlugins() {
		return moveIsolatedPlugins;
	}

	public boolean isRunSingleInstanceListener() {
		return runSingleInstanceListener;
	}

	public boolean isDebugMode() {
		return debugMode;
	}

	public void setDivByZeroVal(final String divByZeroVal) {
		this.divByZeroVal = divByZeroVal;
	}

	public void setUsePtrCursor(final boolean usePtrCursor) {
		this.usePtrCursor = usePtrCursor;
	}

	public void setHideProcessStackDialog(final boolean hideProcessStackDialog) {
		this.hideProcessStackDialog = hideProcessStackDialog;
	}

	public void setRequireCommandKey(final boolean requireCommandKey) {
		this.requireCommandKey = requireCommandKey;
	}

	public void setMoveIsolatedPlugins(final boolean moveIsolatedPlugins) {
		this.moveIsolatedPlugins = moveIsolatedPlugins;
	}

	public void setRunSingleInstanceListener(
		final boolean runSingleInstanceListener)
	{
		this.runSingleInstanceListener = runSingleInstanceListener;
	}

	public void setDebugMode(final boolean debugMode) {
		this.debugMode = debugMode;
	}

}
