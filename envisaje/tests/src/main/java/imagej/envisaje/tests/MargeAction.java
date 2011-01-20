
package imagej.envisaje.tests;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import org.openide.awt.StatusDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.Presenter;

public final class MargeAction implements Presenter.Menu, ActionListener {

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