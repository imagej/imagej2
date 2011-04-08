/*
 * ZoomFullAction.java
 *
 * Created on November 20, 2006, 8:01 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.paintui.actions;
import javax.swing.ImageIcon;
import imagej.envisaje.api.actions.GenericContextSensitiveAction;
import imagej.envisaje.api.editor.Zoom;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Tim
 */
public class ZoomFullAction extends GenericContextSensitiveAction <Zoom> {
    
    /** Creates a new instance of ZoomFullAction */
    public ZoomFullAction() {
        super (Utilities.actionsGlobalContext(), Zoom.class);
        setIcon(new ImageIcon(
                Utilities.loadImage("imagej/envisaje/paintui/resources/zoomFull.png")));
        putValue (NAME, NbBundle.getMessage(ZoomInAction.class, "ACT_ZoomFull"));
    }
    
    public ZoomFullAction(Lookup lkp) {
        super(lkp);
        setIcon(new ImageIcon(
                Utilities.loadImage("imagej/envisaje/paintui/resources/zoomFull.png")));
    }
    
    
    protected void performAction(Zoom t) {
        
    }
    
}
