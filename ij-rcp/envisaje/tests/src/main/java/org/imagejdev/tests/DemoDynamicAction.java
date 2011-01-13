package org.imagejdev.tests;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

public class DemoDynamicAction implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message("Dynamic Action  Performed", NotifyDescriptor.INFORMATION_MESSAGE));
    }
}
