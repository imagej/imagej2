package imagej.envisaje.layersui;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Action which shows Layers component.
 */
public class LayersAction extends AbstractAction {

    public LayersAction() {
        putValue(NAME, NbBundle.getMessage(LayersAction.class, "CTL_LayersAction"));
//        putValue(SMALL_ICON, new ImageIcon(Utilities.loadImage("SET/PATH/TO/ICON/HERE", true)));
    }

    public void actionPerformed(ActionEvent evt) {
        TopComponent win = WindowManager.getDefault().findTopComponent("LayersTopComponent");
        if (win == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, "Cannot find Layers component.");
            return;
        }
        win.open();
        win.requestActive();
    }

}
