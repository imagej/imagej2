/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.imagejdev.tests;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.DynamicMenuContent;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author GBH
 */
public class DynamicTopLevelMenu extends JMenu { //implements Presenter.Menu {

    public DynamicTopLevelMenu() {
        super("IJ1 Commands/Plugins");
        for (int i = 0; i < 10; i++) {
            final String put = "" + i;
            add(new JMenuItem(new AbstractAction(put) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    StringBuilder sb = new StringBuilder();
                    FileObject menuFO = FileUtil.getConfigFile("Menu");
                    Enumeration<? extends FileObject> folders = menuFO.getFolders(false);
                    List<? extends FileObject> folderList = Collections.list(folders);
                    for (FileObject folder : folderList) {
                        sb.append(folder.getName() + " >> " + folder.getClass().getName() + " || ");

                        if (folder != null) {
                            try {
                                //    FileObject file = Repository.getDefault().getDefaultFileSystem().findResource (
//    "someFolder/com-foo-mymodule-MyClass.instance");
//
                                DataObject dob = DataObject.find(folder);

                                    ///((MultiFileObject)) folder.getPrimaryEntry().getFile();
                                //dob.getLookup().
                                InstanceCookie cookie = (InstanceCookie) dob.getCookie(InstanceCookie.class);
                                JMenu theMenu = (JMenu) cookie.instanceCreate();

//                                    DataObject DO = DataObject.find(folder);
//                                    if (DO != null) {
//                                        Object o = DO.getLookup().lookup(InstanceCookie.class).instanceCreate();

                                String menuLabel = null;
                                if (theMenu != null) {
                                    menuLabel = theMenu.getLabel();
                                }
                                //.getActionCommand()
                                if (menuLabel != null) {
                                    sb.append(menuLabel);
                                }
//                                    }
//                                }
                            } catch (Exception ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                        sb.append("\n");
                    }
//                        ((Action) DataObject.find(
//                                FileUtil.getConfigFile("Actions/View/o-n-core-actions-LogAction.instance")).
//                                getLookup().lookup(InstanceCookie.class).instanceCreate()).actionPerformed(new ActionEvent(null, 3, ""));
                    //                    String s = null;
                    //                    try {
                    //                        s = menuFO.asText();
                    //                    } catch (IOException ex) {
                    //                        Exceptions.printStackTrace(ex);
                    //                    }
                    //                    Lookup lookup = Lookups.forPath("Menu");
                    //                    Collection c = lookup.lookupAll(Object.class);
                    //                    for (Object next : c) {
                    //                        sb.append(next.toString()+"\n");
                    //                    }
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(sb.toString(), NotifyDescriptor.INFORMATION_MESSAGE));
                }
            }));
        }
    }
    /*
     * In particular I defined statically a "container" action which is referenced by the layer.xml,
     * but does not contain any logic except the getMenuPresenter.  In this method you can instantiate
     * the JMenu to which all of the dynamically created actions are added.
     * from http://forums.netbeans.org/topic19636.html
     */
//    public JMenuItem getMenuPresenter() {
//        JMenu m = new JMenu("IJ1 Commands/Plugins");
//        for (int i = 0; i < 10; i++) {
//            final String put = "" + i;
//            m.add(new JMenuItem(new AbstractAction()   {
//
//                @Override
//                public void actionPerformed(ActionEvent e) {
//
//                    DialogDisplayer.getDefault().notify(
//                            new NotifyDescriptor.Message("Dynamic Action " + put, NotifyDescriptor.INFORMATION_MESSAGE));
//                }
//            }));
//        }
//        return m;
//    }
//
//    @Override
//    public void actionPerformed(ActionEvent e) {
//        DialogDisplayer.getDefault().notify(
//                new NotifyDescriptor.Message("Dynamic Action TopLevel", NotifyDescriptor.INFORMATION_MESSAGE));
//    }
}
