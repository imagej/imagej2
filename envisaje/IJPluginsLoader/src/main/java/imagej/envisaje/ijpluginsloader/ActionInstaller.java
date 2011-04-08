package imagej.envisaje.ijpluginsloader;

import java.io.IOException;
import javax.swing.Action;
import javax.swing.JMenu;
// import javax.swing.text.Keymap;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
// import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 *
 * @author GBH
 */
public class ActionInstaller {

    public static void registerDynamicAction(String name, Action theAction, String menuPath, String shortcut) {
        // instanciate into Action folder
        FileObject actionsFO = FileUtil.getConfigFile("Actions");
        DataFolder actionsFolder = DataFolder.findFolder(actionsFO);
        //
        FileObject actionObject;
        DataObject actionDataObject = null;
        try {
            actionObject = FileUtil.createData(actionsFolder.getPrimaryFile(), name + ".instance");
            //actionObject.setAttribute("instanceCreate", new DummyAction(label, command));
            actionObject.setAttribute("instanceCreate", theAction);
            actionDataObject = DataObject.find(actionObject);
            // InstanceCookie ic = (InstanceCookie) actionDataObject.getCookie(InstanceCookie.class);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (actionDataObject != null) {
            addActionToMenu(menuPath, actionDataObject);
//            if (shortcut != null || shortcut.isEmpty()) {
//                addShortcut(actionDataObject);
//            }
        }

    }

    private static void addActionToMenu(String menuPath, DataObject actionDataObject) {
        // add action to menu
//        FileObject menuFO = FileUtil.getConfigFile("Menu/" + menuPath);
//        FileObject menuFO = FileUtil.getConfigFile("Menu/" + menuPath);
//        DataFolder menusFolder = DataFolder.findFolder(menuFO);

        DataFolder menuDataFolder = null;
        try {
            menuDataFolder = DataFolder.findFolder(
                    FileUtil.createFolder(FileUtil.getConfigRoot(), "Menu/" + menuPath));
            org.openide.loaders.DataShadow shadow2 = actionDataObject.createShadow(menuDataFolder);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static void addShortcut(DataObject actionDataObject) {
        // ShortcutsFolder.initShortcuts ();
//		 Keymap globalMap = (Keymap) org.openide.util.Lookup.getDefault().lookup(Keymap.class);
        // add a shortcut
        DataFolder shortcutsFolder;
        org.openide.loaders.DataShadow shadow;
        try {
            shortcutsFolder = DataFolder.findFolder(
                    //FileUtil.createFolder(Repository.getDefault().getDefaultFileSystem().getRoot(), "Shortcuts"));
                    FileUtil.createFolder(FileUtil.getConfigRoot(), "Shortcuts"));
            shadow = actionDataObject.createShadow(shortcutsFolder);
            shadow.rename("C-F2");
            // ShortcutsFolder.waitShortcutsFinished ();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    //private static final FileSystem fragment = FileUtil.createMemoryFileSystem();

    public static void findTopLevelMenu(String name) {
        FileObject menusFO = FileUtil.getConfigFile("Menu/" + name);
        if (menusFO != null) {
            DataFolder menus = DataFolder.findFolder(menusFO);
            DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(menus.getName(), NotifyDescriptor.INFORMATION_MESSAGE));
        } else {
            DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message("Not found: " + name, NotifyDescriptor.ERROR_MESSAGE));
        }
    }

    public static boolean checkTopLevelMenu(String name) {
        FileObject menusFO = FileUtil.getConfigFile("Menu/" + name);
        return (menusFO != null);
    }

    // Effectively adds this to layer
    // <folder name="Menu">
    //    <attr name="imagej-envisaje-pluginfinder-Menu1.instance/Window" boolvalue="true"/>
    //    <file name="imagej-envisaje-pluginfinder-Menu1.instance"/>
    // </folder>
    public static void registerTopLevelMenu(String name, String label) throws Exception {

        // instanciate into menu folder
        FileObject menusFO = FileUtil.getConfigFile("Menu");
        //FileObject menusFO =fragment.findResource("Menu");

        DataFolder menus = DataFolder.findFolder(menusFO);
        FileObject menuObject = FileUtil.createData(menus.getPrimaryFile(), name + ".instance");
        menuObject.setAttribute("instanceCreate", new JMenu(label));
        // perhaps the following is unnecessary...
        //DataObject menuDataObject = DataObject.find(menuObject);
        //InstanceCookie ic = (InstanceCookie) menuDataObject.getCookie(InstanceCookie.class);
    }

    void addAction(FileSystem fragment, String catagory, String actionClass, Action action) {
        try {
            FileObject f = FileUtil.createData(fragment.getRoot(),
                    "Actions/" + catagory + "/" + actionClass.replace('.', '-') + ".instance"); // NOI18N
            fragment.findResource(catagory);
            f.setAttribute("instanceCreate", action);
            //f.setAttribute("instanceOf", new DummyAction("testShortcutsFolder"));

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
}
