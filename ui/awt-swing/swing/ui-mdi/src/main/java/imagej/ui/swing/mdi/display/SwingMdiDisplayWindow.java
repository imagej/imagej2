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

package imagej.ui.swing.mdi.display;

import imagej.ImageJ;
import imagej.ext.display.DisplayPanel;
import imagej.ext.display.DisplayWindow;
import imagej.ui.UserInterface;
import imagej.ui.UIService;
import imagej.ui.swing.StaticSwingUtils;
import imagej.ui.swing.display.SwingDisplayPanel;
import imagej.ui.swing.mdi.InternalFrameEventDispatcher;
import imagej.ui.swing.mdi.JMDIDesktopPane;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.beans.PropertyVetoException;

import javax.swing.JInternalFrame;
import javax.swing.WindowConstants;

/**
 * TODO
 * 
 * @author Grant Harris
 */
public class SwingMdiDisplayWindow extends JInternalFrame implements
	DisplayWindow
{

	SwingDisplayPanel panel;

	public SwingMdiDisplayWindow() throws HeadlessException {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setMaximizable(true);
		setResizable(true);
		setIconifiable(false);
		setSize(new Dimension(400, 400));
		setLocation(StaticSwingUtils.nextFramePosition());
	}

	// -- SwingMdiDisplayWindow methods --

	public void
		addEventDispatcher(final InternalFrameEventDispatcher dispatcher)
	{
		addInternalFrameListener(dispatcher);
	}

	// -- DisplayWindow methods --

	@Override
	public void setContent(final DisplayPanel panel) {
		// TODO - eliminate hacky cast
		this.setContentPane((SwingDisplayPanel) panel);
	}

	@Override
	public void showDisplay(final boolean visible) {
		final UserInterface userInterface = ImageJ.get(UIService.class).getUI();
		final JMDIDesktopPane desktop =
			(JMDIDesktopPane) userInterface.getDesktop();
		setVisible(true);
		desktop.add(this);
//		if (desktop.getComponentCount() == 1) {
//			try {
//				setMaximum(true);
//			}
//			catch (final PropertyVetoException ex) {
//				// ignore veto
//			}
//		}
		toFront();
		try {
			setSelected(true);
		}
		catch (final PropertyVetoException e) {
			// Don't care.
		}
	}

	@Override
	public void close() {
		this.setVisible(false);
		this.dispose();
	}

}
