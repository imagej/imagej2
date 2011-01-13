/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.imagejdev.tests;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.actions.Presenter;

/**
 *
 * @author GBH
 */
public class DynamicSubMenu extends AbstractAction implements Presenter.Menu {

    /*
     * In particular I defined statically a "container" action which is referenced by the layer.xml,
     * but does not contain any logic except the getMenuPresenter.  In this method you can instantiate
     * the JMenu to which all of the dynamically created actions are added.
     * from http://forums.netbeans.org/topic19636.html
     */
    JMenu m = new JMenu("subMenu");

    public DynamicSubMenu(String name) {
        this(name, null);
    }

    public DynamicSubMenu(String name, Icon icon) {
        m.setText(name);
        if (icon != null) {
            m.setIcon(icon);
        }
    }

//        for (int i = 0; i < 10; i++) {
//            final String put = "" + i;
//            m.add(new JMenuItem(new AbstractAction()  {
//                @Override
//                public void actionPerformed(ActionEvent e) {
//                    DialogDisplayer.getDefault().notify(
//                            new NotifyDescriptor.Message("Dynamic Action " + put, NotifyDescriptor.INFORMATION_MESSAGE));
//                }
//            }));
//        }
    public JMenuItem getMenuPresenter() {
        return m;
    }

    public void addMenuItem(JMenuItem item) {
        m.add(item);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message("Dynamic Action TopLevel", NotifyDescriptor.INFORMATION_MESSAGE));
    }
}
