//
// OptionsLookAndFeel.java
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

package imagej.ui.swing.plugins;

import imagej.ext.menu.MenuConstants;
import imagej.ext.module.DefaultModuleItem;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.options.OptionsPlugin;
import imagej.ui.UserInterface;
import imagej.ui.UIService;
import imagej.ui.swing.SwingApplicationFrame;
import imagej.util.Log;

import java.awt.EventQueue;
import java.util.ArrayList;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Runs the Edit::Options::Look and Feel dialog.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = OptionsPlugin.class, menu = {
	@Menu(label = MenuConstants.EDIT_LABEL, weight = MenuConstants.EDIT_WEIGHT,
		mnemonic = MenuConstants.EDIT_MNEMONIC),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Look and Feel...", weight = 100, mnemonic = 'l') })
public class OptionsLookAndFeel extends OptionsPlugin {

	// -- Constants --

	private static final String LOOK_AND_FEEL = "lookAndFeel";

	// -- Parameters --

	@Parameter(persist = false)
	private UIService uiService;

	@Parameter(label = "Look & Feel", persist = false,
		initializer = "initLookAndFeel")
	private String lookAndFeel;

	// -- Constructor --

	public OptionsLookAndFeel() {
		load(); // NB: Load persisted values *after* field initialization.
	}

	// -- OptionsLookAndFeel methods --

	public UIService getUIService() {
		return uiService;
	}

	public String getLookAndFeel() {
		return lookAndFeel;
	}

	public void setUIService(final UIService uiService) {
		this.uiService = uiService;
	}

	public void setLookAndFeel(final String lookAndFeel) {
		this.lookAndFeel = lookAndFeel;
	}

	// -- Runnable methods --

	@Override
	public void run() {
		// set look and feel
		final LookAndFeelInfo[] lookAndFeels = UIManager.getInstalledLookAndFeels();
		for (final LookAndFeelInfo lookAndFeelInfo : lookAndFeels) {
			if (lookAndFeelInfo.getName().equals(lookAndFeel)) {
				try {
					UIManager.setLookAndFeel(lookAndFeelInfo.getClassName());
					EventQueue.invokeLater(new Runnable() {

						@Override
						public void run() {
							// FIXME: Get all windows from UIService rather than just app.
							final UserInterface ui = getUIService().getUI();
							final SwingApplicationFrame swingAppFrame =
								(SwingApplicationFrame) ui.getApplicationFrame();
							SwingUtilities.updateComponentTreeUI(swingAppFrame);
							swingAppFrame.pack();
						}
					});
				}
				catch (final ClassNotFoundException e) {
					Log.error(e);
				}
				catch (final InstantiationException e) {
					Log.error(e);
				}
				catch (final IllegalAccessException e) {
					Log.error(e);
				}
				catch (final UnsupportedLookAndFeelException e) {
					Log.error(e);
				}
				break;
			}
		}

		super.run();
	}

	// -- Initializers --

	protected void initLookAndFeel() {
		final String lafClass = UIManager.getLookAndFeel().getClass().getName();

		final LookAndFeelInfo[] lookAndFeels = UIManager.getInstalledLookAndFeels();
		final ArrayList<String> lookAndFeelChoices = new ArrayList<String>();
		for (final LookAndFeelInfo lafInfo : lookAndFeels) {
			final String lafName = lafInfo.getName();
			lookAndFeelChoices.add(lafInfo.getName());
			if (lafClass.equals(lafInfo.getClassName())) {
				lookAndFeel = lafName;
			}
		}

		@SuppressWarnings("unchecked")
		final DefaultModuleItem<String> lookAndFeelItem =
			(DefaultModuleItem<String>) getInfo().getInput(LOOK_AND_FEEL);
		lookAndFeelItem.setChoices(lookAndFeelChoices);
	}

}
