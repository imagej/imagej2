package imagej.envisaje.toolconfigui;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Action which shows Customization component.
 */
public class CustomizationAction extends AbstractAction {

    public CustomizationAction() {
        putValue(NAME, NbBundle.getMessage(CustomizationAction.class, "CTL_CustomizationAction"));
//        putValue(SMALL_ICON, new ImageIcon(Utilities.loadImage("SET/PATH/TO/ICON/HERE", true)));
    }

    public void actionPerformed(ActionEvent evt) {
        TopComponent win = WindowManager.getDefault().findTopComponent("CustomizationTopComponent");
        if (win == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, "Cannot find Customization component.");
            return;
        }
        win.open();
        win.requestActive();
    }

}
