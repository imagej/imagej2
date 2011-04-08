/*
 * ShowDiskExplorerAction.java
 *
 * Created on January 20, 2005, 12:34 AM
 *
 * updated by Tom Wheeler on Aug 21 2005 to prevent being calls from
 * outside the AWT event dispatch thread (performAction method).
 */
package imagej.envisaje.filemanagement.browser;

import javax.swing.SwingUtilities;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author rich
 */
public class ShowDiskExplorerAction extends CallableSystemAction {

	/** Creates a new instance of ShowDiskExplorerAction */
	public ShowDiskExplorerAction() {
	}

	public void performAction() {
		if (SwingUtilities.isEventDispatchThread()) {
			openOrActivate();
		} else {
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					openOrActivate();
				}
			});
		}
	}

	private void openOrActivate() {
		TopComponent tc = DiskExplorerTopComponent.getInstance();
		Mode m = WindowManager.getDefault().findMode("explorer");
		if (m != null) {
			m.dockInto(tc);
		}
		if (!tc.isOpened()) {
			tc.open();
		}
		tc.requestActive();
	}

	public String getName() {
		return "Disk Explorer";
	}

	public HelpCtx getHelpCtx() {
		return HelpCtx.DEFAULT_HELP;
	}
}
