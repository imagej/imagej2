/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.envisaje.ijpluginsloader;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;

@ActionID(category = "Build", id = "CheckTopMenuAction")

@ActionRegistration(displayName = "CheckTopMenu")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 0)
})
public final class CheckTopMenuAction extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
		ActionInstaller.findTopLevelMenu("Menu_a");
		ActionInstaller.findTopLevelMenu("Process");
    }

}