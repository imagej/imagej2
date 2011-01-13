
package org.imagejdev.foo;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import org.openide.awt.StatusDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.Presenter;

public final class BarAction implements Presenter.Menu, ActionListener {

    private ImageIcon ICON = new ImageIcon(ImageUtilities.loadImage("org/imagejdev/foo/help.png", true));

    @Override
    public void actionPerformed(ActionEvent e) {
        // nothing needs to happen here
        StatusDisplayer.getDefault().setStatusText("Bar Action performed");
    }

    @Override
    public JMenuItem getMenuPresenter() {
        JMenuItem abc = new JMenuItem("Bar", null);
        abc.setIcon(ICON);
        abc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StatusDisplayer.getDefault().setStatusText("Bar Action performed");
            }
        });
        return abc;
    }

}