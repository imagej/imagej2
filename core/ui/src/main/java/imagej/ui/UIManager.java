//
// UIManager.java
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

import imagej.Log;
import imagej.manager.Manager;
import imagej.manager.ManagerComponent;

import java.util.ArrayList;
import java.util.List;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

/**
 * Manager component for the ImageJ user interface.
 *
 * @author Curtis Rueden
 */
@Manager
public final class UIManager implements ManagerComponent {

	/** Processes the given command line arguments. */
	public void processArgs(final String[] args) {
		Log.debug("Received command line arguments:");
		for (String arg : args) Log.debug("\t" + arg);
		// TODO - pass along arguments to applicable user interface
	}

	// -- ManagerComponent methods --

	@Override
	public void initialize() {
		final List<UserInterface> uis = getAvailableUIs();
		if (uis.size() > 0) {
			final UserInterface ui = uis.get(0);
			Log.debug("Launching user interface: " + ui);
			ui.initialize();
		}
		else Log.warn("No user interfaces found.");
	}

	// -- Helper methods --

	private List<UserInterface> getAvailableUIs() {
		// use SezPoz to discover all user interfaces
		final List<UserInterface> uis = new ArrayList<UserInterface>();
		for (final IndexItem<UI, UserInterface> item :
			Index.load(UI.class, UserInterface.class))
		{
			try {
				final UserInterface ui = item.instance();
				Log.debug("Discovered user interface: " + ui);
				uis.add(ui);
			}
			catch (InstantiationException e) {
				Log.warn("Invalid user interface: " + item, e);
			}
		}
		return uis;
	}

}
