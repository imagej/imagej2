/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

package imagej.ui.swing.commands;

import imagej.menu.MenuConstants;
import imagej.module.MutableModuleItem;
import imagej.options.OptionsPlugin;
import imagej.plugins.uis.swing.SwingApplicationFrame;
import imagej.ui.UserInterface;

import java.awt.EventQueue;
import java.util.ArrayList;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import org.scijava.log.LogService;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

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

	@Parameter
	private UserInterface ui;

	@Parameter
	private LogService log;

	@Parameter(label = "Look & Feel", persist = false,
		initializer = "initLookAndFeel")
	private String lookAndFeel;

	// -- OptionsLookAndFeel methods --

	public UserInterface getUI() {
		return ui;
	}

	public String getLookAndFeel() {
		return lookAndFeel;
	}

	public void setUI(final UserInterface ui) {
		this.ui = ui;
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
							final SwingApplicationFrame swingAppFrame =
								(SwingApplicationFrame) ui.getApplicationFrame();
							SwingUtilities.updateComponentTreeUI(swingAppFrame);
							swingAppFrame.pack();
						}
					});
				}
				catch (final ClassNotFoundException e) {
					log.error(e);
				}
				catch (final InstantiationException e) {
					log.error(e);
				}
				catch (final IllegalAccessException e) {
					log.error(e);
				}
				catch (final UnsupportedLookAndFeelException e) {
					log.error(e);
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

		final MutableModuleItem<String> lookAndFeelItem =
			getInfo().getMutableInput(LOOK_AND_FEEL, String.class);
		lookAndFeelItem.setChoices(lookAndFeelChoices);
	}

}
