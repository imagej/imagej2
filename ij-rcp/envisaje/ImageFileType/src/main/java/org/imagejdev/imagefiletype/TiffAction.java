/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.imagejdev.imagefiletype;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public final class TiffAction implements ActionListener {

  private final TiffDataObject context;

  public TiffAction(TiffDataObject context) {
    this.context = context;
  }

  @Override
  public void actionPerformed(ActionEvent ev) {
   FileObject f = context.getPrimaryFile();
   String displayName = FileUtil.getFileDisplayName(f);
   String msg = "I am " + displayName + ". Hear me roar!";
   NotifyDescriptor nd = new NotifyDescriptor.Message(msg);
   DialogDisplayer.getDefault().notify(nd);

  }
}
