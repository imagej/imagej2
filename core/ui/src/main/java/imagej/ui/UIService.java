//
// UIService.java
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

import imagej.Service;
import imagej.IService;
import imagej.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

/**
 * Service for the ImageJ user interface.
 *
 * @author Curtis Rueden
 */
@Service(priority = Service.LAST_PRIORITY)
public final class UIService implements IService {

	/** The active user interface. */
	private UserInterface userInterface;

	/** Available user interfaces. */
	private List<UserInterface> availableUIs;

	/** Processes the given command line arguments. */
	public void processArgs(final String[] args) {
		Log.info("Received command line arguments:");
		for (String arg : args) Log.info("\t" + arg);
		userInterface.processArgs(args);
	}

	/** Gets the active user interface. */
	public UserInterface getUI() {
		return userInterface;
	}

	/** Gets the user interfaces available on the classpath. */
	public List<UserInterface> getAvailableUIs() {
		return availableUIs;
	}

	// -- IService methods --

	@Override
	public void initialize() {
		final List<UserInterface> uis = discoverUIs();
		availableUIs = Collections.unmodifiableList(uis);
		if (uis.size() > 0) {
			final UserInterface ui = uis.get(0);
			Log.info("Launching user interface: " + ui.getClass().getName());
			ui.initialize();
			userInterface = ui;
		}
		else Log.warn("No user interfaces found.");
	}

	// -- Helper methods --

	/** Discovers user interfaces using SezPoz. */
	private List<UserInterface> discoverUIs() {
		final List<UserInterface> uis = new ArrayList<UserInterface>();
		for (final IndexItem<UI, UserInterface> item :
			Index.load(UI.class, UserInterface.class))
		{
			try {
				final UserInterface ui = item.instance();
				Log.info("Discovered user interface: " + ui.getClass().getName());
				uis.add(ui);
			}
			catch (InstantiationException e) {
				Log.warn("Invalid user interface: " + item, e);
			}
		}
		return uis;
	}

}
