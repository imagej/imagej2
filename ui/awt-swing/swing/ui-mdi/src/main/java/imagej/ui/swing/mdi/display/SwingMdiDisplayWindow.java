
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
