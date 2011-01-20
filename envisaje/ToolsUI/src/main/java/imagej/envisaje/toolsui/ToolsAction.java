package imagej.envisaje.toolsui;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Action which shows Tools component.
 */
public class ToolsAction extends AbstractAction {

    public ToolsAction() {
        putValue(NAME, NbBundle.getMessage(ToolsAction.class, "CTL_ToolsAction"));
//        putValue(SMALL_ICON, new ImageIcon(Utilities.loadImage("SET/PATH/TO/ICON/HERE", true)));
    }

    public void actionPerformed(ActionEvent evt) {
        TopComponent win = WindowManager.getDefault().findTopComponent("ToolsTopComponent");
        if (win == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, "Cannot find Tools component.");
            return;
        }
        win.open();
        win.requestActive();
    }

}
