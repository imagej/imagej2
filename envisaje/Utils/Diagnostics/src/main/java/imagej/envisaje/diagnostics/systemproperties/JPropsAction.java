package imagej.envisaje.diagnostics.systemproperties;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Action which shows JavaProperties.
 */
public class JPropsAction extends AbstractAction {
    
    public JPropsAction() {
        super(NbBundle.getMessage(JPropsAction.class, "CTL_JPropsAction"));
//        putValue(SMALL_ICON, new ImageIcon(Utilities.loadImage(JPropsTopComponent.ICON_PATH, true)));
    }
    
    public void actionPerformed(ActionEvent evt) {
        TopComponent win = JPropsTopComponent.findInstance();
        win.open();
        win.requestActive();
    }
    
}
