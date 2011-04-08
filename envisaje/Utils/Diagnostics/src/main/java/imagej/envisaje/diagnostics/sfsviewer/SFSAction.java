package imagej.envisaje.diagnostics.sfsviewer;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Action which shows SFS component.
 */
public class SFSAction extends AbstractAction {
    
    public SFSAction() {
        super(NbBundle.getMessage(SFSAction.class, "CTL_SFSAction"));
//        putValue(SMALL_ICON, new ImageIcon(Utilities.loadImage(SFSTopComponent.ICON_PATH, true)));
    }
    
    public void actionPerformed(ActionEvent evt) {
        TopComponent win = SFSTopComponent.findInstance();
        win.open();
        win.requestActive();
    }
    
}
