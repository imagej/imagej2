package org.imagejdev.pluginloader;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.modules.ModuleInstall;
import org.openide.windows.IOProvider;
import org.openide.windows.WindowManager;

/**
 * Manages a module's lifecycle. This is executed when the module is installed/initialized
 * Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall {

  @Override
  public void validate() {
    System.out.println("Installed.validate()...");
  }

  @Override
  public void restored() {

    // Put your startup code here.

    WindowManager.getDefault().invokeWhenUIReady(
            new Runnable() {
              public void run() {
                // any code here will be run with the UI is available
                //SomeTopComponent.findInstance().open();
                System.out.println("Installed.restored()...");
                IOProvider.getDefault().getIO("PluginLoader", false).getOut().println("Installed.restored()...");
                // Dialog...
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("Installed.restored()...", NotifyDescriptor.INFORMATION_MESSAGE));
              }
            });
  }
}
