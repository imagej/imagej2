
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

//@ActionID(category = "Build", id = "imagej.envisaje.tests.MargeAction")
//@ActionRegistration(displayName = "#CTL_MargeAction")
//@ActionReferences({
//    @ActionReference(path = "Menus/File", position = 0)
//})
@ActionID(category = "Build", id = "MargeAction")

@ActionRegistration(displayName = "Marge")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 3)
})


public final class MargeAction extends AbstractAction implements Presenter.Menu {

    private ImageIcon ICON = new ImageIcon(ImageUtilities.loadImage("imagej/envisaje/tests/help.png", true));

    @Override
    public void actionPerformed(ActionEvent e) {
        // nothing needs to happen here
    }

    @Override
    public JMenuItem getMenuPresenter() {
        JRadioButtonMenuItem abc = new JRadioButtonMenuItem("Marge", null);
        ButtonGroup local = ButtonGroupHelper.returnGroup();
        local.add(abc);
        abc.setSelected(false);
        abc.setIcon(ICON);
        abc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StatusDisplayer.getDefault().setStatusText("Marge chosen");
            }
        });
        return abc;
    }

}