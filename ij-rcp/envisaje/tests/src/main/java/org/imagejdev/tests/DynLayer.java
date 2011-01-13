/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.imagejdev.tests;

import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = FileSystem.class)
public final class DynLayer extends MultiFileSystem {

    static DynLayer INSTANCE;
    private static final FileSystem fragment = FileUtil.createMemoryFileSystem();

    ;

    /**
     * Default constructor for lookup.
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public DynLayer() {
        INSTANCE = this;
        //fragment = FileUtil.createMemoryFileSystem();
        // String actionClass = "org.imagejdev.foo.BarAction";
        String actionClass = "DynDummy";
        String catagory = "Edit";
        addAction(fragment, catagory, actionClass);
        String menuPath = "Menu/Edit";
        addMenuPath(fragment, menuPath, actionClass);
        //INSTANCE.setDelegates(fragment);
        //DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("DynLayer()", NotifyDescriptor.INFORMATION_MESSAGE));
    }

    void addAction(FileSystem fragment, String catagory, String actionClass) {
        try {
            FileObject f = FileUtil.createData(fragment.getRoot(),
                    "Actions/" + catagory + "/" + actionClass.replace('.', '-') + ".instance"); // NOI18N

            f.setAttribute("instanceCreate", new DummyAction("dynLayerAction"));
            //f.setAttribute("instanceOf", new DummyAction("testShortcutsFolder"));

//            f.setAttribute("newvalue:delegate", actionClass);
//                f.setAttribute("delegate", actionClass);
//                f.setAttribute("originalFile", "Actions/" + actionClass.replace('.', '-') +".instance"); // NOI18N
//                f.setAttribute("displayName", "Dynamic"); // NOI18N
            //   f.setAttribute(catagory, f);
        } catch (IOException e) {
            NotifyDescriptor d = new NotifyDescriptor.Exception(e);
            throw new AssertionError(e);
        }
    }

    void addMenuPath(FileSystem fragment, String menuPath, String actionClass) {
        try {
            FileObject f = FileUtil.createData(fragment.getRoot(),
                    menuPath + "/" + actionClass.replace('.', '-') + ".shadow"); // NOI18N
            f.setAttribute("originalFile", "Actions/" + actionClass.replace('.', '-') + ".instance"); // NOI18N
            f.setAttribute("position", 80); // NOI18N
        } catch (IOException e) {
            NotifyDescriptor d = new NotifyDescriptor.Exception(e);
            throw new AssertionError(e);
        }
    }

    void setEnabled(boolean enabled) {
        if (enabled) {
            setDelegates(fragment);
        } else {
            setDelegates();
        }
    }
    /*
     * Creates an entry in the layers like this:
     * 
    <filesystem>
    <folder name="Actions">
    <folder name="File">
    <file name="org-imagejdev-foo-BarAction.instance">
    <attr name="delegate" newvalue="org.imagejdev.foo.BarAction"/>
    </file>
    </folder>
    </folder>
    <folder name="Menu">
    <folder name="File">
    <file name="org-imagejdev-foo-BarAction.shadow">
    <attr name="originalFile" stringvalue="Actions/File/org-imagejdev-foo-BarAction.instance"/>
    <attr name="position" intvalue="0"/>
    </file>
    </folder>
    </folder>
    </filesystem>
     *  
     */

    private static final class DummyAction extends AbstractAction {

        private final String name;

        public DummyAction(String name) {
            this.name = name;
            putValue(Action.NAME, name);
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
