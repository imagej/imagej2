/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.envisaje.tests;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author GBH
 */
public class DialogOkCancel {

    private static void withPrompt() {
        final NotifyDescriptor.InputLine msg;
        JButton ok = new JButton();
        JButton cancel = new JButton();
        ok.setText("OK");
        cancel.setText("Cancel");
        msg = new NotifyDescriptor.InputLine("Login:", "User name: ",
                NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE);
        msg.setOptions(new Object[]{ok, cancel});
        ok.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (!msg.getInputText().equals("tools")) {
                    try {

                        FileObject toolsMenu = FileUtil.getConfigFile("Menu/Tools");
                        toolsMenu.delete();
                        FileUtil.getConfigRoot().refresh();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        });
        DialogDisplayer.getDefault().notifyLater(msg);

    }

    public void restored() {
    }

    public static void withPanel() {
        // Create instance of your panel, extends JPanel...
        JPanel myPanel = new JPanel();
        myPanel.add(new JLabel("Testing"));

        // Create a custom NotifyDescriptor, specify the panel instance as a parameter + other params
        NotifyDescriptor nd = new NotifyDescriptor(
                myPanel, // instance of your panel
                "Title", // title of the dialog
                NotifyDescriptor.YES_NO_OPTION, // it is Yes/No dialog ...
                NotifyDescriptor.QUESTION_MESSAGE, // ... of a question type => a question mark icon
                null, // we have specified YES_NO_OPTION => can be null, options specified by L&F,
                // otherwise specify options as:
                //     new Object[] { NotifyDescriptor.YES_OPTION, ... etc. },
                NotifyDescriptor.YES_OPTION // default option is "Yes"
                );

        // let's display the dialog now...
        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.YES_OPTION) {
            // user clicked yes, do something here, for example:
            //     System.out.println(myPanel.getNameFieldValue());
        }

    }

    public static void main(String[] args) {
        withPanel();
        withPrompt();
    }
}
