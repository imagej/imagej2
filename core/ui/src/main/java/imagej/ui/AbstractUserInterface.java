//
// AbstractUserInterface.java
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

import imagej.ImageJ;
import imagej.util.Prefs;

/**
 * Abstract superclass for {@link IUserInterface} implementations.
 * 
 * @author Curtis Rueden
 */
public abstract class AbstractUserInterface implements IUserInterface {

	private static final String PREF_FIRST_RUN = "firstRun-" + ImageJ.VERSION;

	private UIService uiService;

	// -- UserInterface methods --

	@Override
	public void initialize(final UIService service) {
		uiService = service;
		createUI();
		displayReadme();
	}

	@Override
	public UIService getUIService() {
		return uiService;
	}

	@Override
	public void processArgs(final String[] args) {
		// TODO
	}

	@Override
	public Desktop getDesktop() {
		return null;
	}

	@Override
	public ApplicationFrame getApplicationFrame() {
		return null;
	}

	@Override
	public ToolBar getToolBar() {
		return null;
	}

	@Override
	public StatusBar getStatusBar() {
		return null;
	}

	// -- Internal methods --

	protected void createUI() {
		// does nothing by default
	}

	// -- Helper methods --

	/** Shows the readme, if this is the first time ImageJ has run. */
	private void displayReadme() {
		final String firstRun = Prefs.get(getClass(), PREF_FIRST_RUN);
		if (firstRun != null) return;
		Prefs.put(getClass(), PREF_FIRST_RUN, false);
		uiService.getPluginService().run(ShowReadme.class);
	}

}
