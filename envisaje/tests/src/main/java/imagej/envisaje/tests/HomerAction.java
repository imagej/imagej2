
package imagej.envisaje.tests;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.Presenter;
 
//@ActionID(category = "Build", id = "imagej.envisaje.tests.HomerAction")
//@ActionRegistration(displayName = "#CTL_HomerAction")
//@ActionReferences({
//    @ActionReference(path = "Menus/File", position = 11)
//})
@ActionID(category = "Build", id = "HomerAction")

@ActionRegistration(displayName = "Homer")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 5)
})


public final class HomerAction extends AbstractAction implements Presenter.Menu {

    private ImageIcon ICON = new ImageIcon(ImageUtilities.loadImage("imagej/envisaje/tests/help.png", true));

    @Override
    public void actionPerformed(ActionEvent e) {
        // nothing needs to happen here
    }

    @Override
    public JMenuItem getMenuPresenter() {
        JRadioButtonMenuItem abc = new JRadioButtonMenuItem("Homer", null);
        ButtonGroup local = ButtonGroupHelper.returnGroup();
        local.add(abc);
        abc.setSelected(false);
        abc.setIcon(ICON);
        abc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StatusDisplayer.getDefault().setStatusText("Homer chosen");
            }
        });
        return abc;
    }

}