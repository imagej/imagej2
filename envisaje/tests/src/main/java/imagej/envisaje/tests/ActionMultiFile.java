/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.envisaje.tests;

import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.text.Keymap;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 *
 * @author GBH
 */
public class ActionMultiFile extends MultiFileSystem {

    static ActionMultiFile INSTANCE;
    private static final FileSystem fragment = FileUtil.createMemoryFileSystem();

    public ActionMultiFile() {
        try {
            // instanciate into Action folder
            DataFolder actionsFolder = DataFolder.findFolder(FileUtil.createFolder(fragment.getRoot(), "Actions"));
            FileObject actionObject = FileUtil.createData(actionsFolder.getPrimaryFile(), "Dummy.instance");	
            actionObject.setAttribute("instanceCreate", new DummyAction("IAMDUMMY"));
            DataObject actionDataObject = DataObject.find(actionObject);
            InstanceCookie ic = (InstanceCookie) actionDataObject.getCookie(InstanceCookie.class);
            //        assertNotNull("Instance cookie is there", ic);
            //        assertEquals("The right class is created", DummyAction.class, ic.instanceClass());
            //        assertTrue("Name is testShortcutsFolder", ic.instanceCreate().toString().indexOf("testShortcutsFolder") > 0);
            // ShortcutsFolder.initShortcuts ();
            Keymap globalMap = (Keymap) org.openide.util.Lookup.getDefault().lookup(Keymap.class);
            //assertNotNull("Global map is registered", globalMap);
            // add a shortcut
            //        DataFolder shortcuts = DataFolder.findFolder(
            //                FileUtil.createFolder(Repository.getDefault().getDefaultFileSystem().getRoot(), "Shortcuts"));
            //        org.openide.loaders.DataShadow shadow = actionDataObject.createShadow(shortcuts);
            //        shadow.rename("C-F2");
            // ShortcutsFolder.waitShortcutsFinished ();
            // add to menu

            FileObject menuFO = FileUtil.createFolder(fragment.getRoot(), "Menu/File");
            //FileUtil.getConfigFile("Menu/File");
            DataFolder menusFolder = DataFolder.findFolder(menuFO);
            org.openide.loaders.DataShadow shadow2 = actionDataObject.createShadow(menusFolder);
            INSTANCE = this;
            setDelegates(fragment);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    private static final class DummyAction extends AbstractAction {

        private final String name;

        public DummyAction(String name) {
            this.name = name;
            this.putValue(Action.NAME, name);
        }

        public void actionPerformed(ActionEvent e) {
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message("DummyAction[" + name + "] performed",
                    NotifyDescriptor.INFORMATION_MESSAGE));
        }

        public String toString() {
            return "DummyAction[" + name + "]";

        }
    }
}
