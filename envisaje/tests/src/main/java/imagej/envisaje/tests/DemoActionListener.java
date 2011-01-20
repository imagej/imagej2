package imagej.envisaje.tests;

import imagej.envisaje.annotations.Action;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

@Action(position = 1,
displayName = "key",
// path = "IJPlugins/Analysis",
menuBar = true,
toolBar = true,
iconBase = "imagej/envisaje/tests/help.png")
public class DemoActionListener implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        System.out.println("hello world");
        DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message("DemoActionListener Performed", NotifyDescriptor.INFORMATION_MESSAGE));
    }
}

/* This generates this:
 *
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE filesystem PUBLIC "-//NetBeans//DTD Filesystem 1.2//EN"
"http://www.netbeans.org/dtds/filesystem-1_2.dtd">
<filesystem>
<folder name="Actions">
<file name="IJPluginsijx-action-test-DemoActionListener.instance">
<!--ijx.action.test.DemoActionListener-->
<attr name="delegate" newvalue="ijx.action.test.DemoActionListener"/>
<attr name="displayName" stringvalue="key"/>
<attr name="iconBase" stringvalue="org/demo/action/icon.png"/>
<attr methodvalue="org.openide.awt.Actions.alwaysEnabled" name="instanceCreate"/>
</file>
</folder>
<folder name="Menu">
<folder name="IJPlugins">
<file name="ijx-action-test-DemoActionListener.shadow">
<!--ijx.action.test.DemoActionListener-->
<attr name="originalFile" stringvalue="Actions/IJPluginsijx-action-test-DemoActionListener.instance"/>
<attr intvalue="1" name="position"/>
</file>
</folder>
</folder>
<folder name="Toolbars">
<folder name="IJPlugins">
<file name="ijx-action-test-DemoActionListener.shadow">
<!--ijx.action.test.DemoActionListener-->
<attr name="originalFile" stringvalue="Actions/IJPluginsijx-action-test-DemoActionListener.instance"/>
<attr intvalue="1" name="position"/>
</file>
</folder>
</folder>
</filesystem>
 */
