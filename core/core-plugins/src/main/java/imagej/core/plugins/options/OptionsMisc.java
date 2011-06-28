//
// OptionsMisc.java
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
 * Runs the Edit::Options::Misc... dialog
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = "Edit", mnemonic = 'e'),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Misc...", weight = 16) })
public class OptionsMisc extends OptionsPlugin {

	@Parameter(label = "Divide by zero value",
		persistKey = SettingsKeys.OPTIONS_MISC_DBZ_VALUE)
	private String divByZeroVal;

	@Parameter(label = "Use pointer cursor",
		persistKey = SettingsKeys.OPTIONS_MISC_POINTER_CURSOR)
	private boolean usePtrCursor;

	@Parameter(label = "Hide \"Process Stack?\" dialog",
		persistKey = SettingsKeys.OPTIONS_MISC_HIDE_STACK_MSG)
	private boolean hideProcessStackDialog;

	@Parameter(label = "Require command key for shortcuts",
		persistKey = SettingsKeys.OPTIONS_MISC_REQUIRE_COMMAND)
	private boolean requireCommandKey;

	@Parameter(label = "Move isolated plugins to Misc. menu",
		persistKey = SettingsKeys.OPTIONS_MISC_MOVE_PLUGINS)
	private boolean moveIsolatedPlugins;

	@Parameter(label = "Run single instance listener",
		persistKey = SettingsKeys.OPTIONS_MISC_SINGLE_INSTANCE)
	private boolean runSingleInstanceListener;

	@Parameter(label = "Debug mode",
		persistKey = SettingsKeys.OPTIONS_MISC_DEBUG_MODE)
	private boolean debugMode;

}
