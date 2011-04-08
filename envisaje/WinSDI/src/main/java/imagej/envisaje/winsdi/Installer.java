/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.envisaje.winsdi;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.modules.ModuleInstall;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall {

	@Override
	public void restored() {
		
//		JFrame mainFrame = new JFrame("Main");
//		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		mainFrame.setSize(400,300);
//		JMenuBar jMenuBar = new JMenuBar();
//
//		//jMenuBar.add(jMenuBar)
//		mainFrame.setVisible(true);
//		DialogDisplayer.getDefault().notify(
//				new NotifyDescriptor.Message("WinSDI Installer.restored()", NotifyDescriptor.INFORMATION_MESSAGE));


		WindowManager.getDefault().invokeWhenUIReady(new Runnable() {

			@Override
			public void run() {
				TopComponent tc = new TopComponent();
				tc.open();
				tc.requestActive();
			}
		});

	}
}
