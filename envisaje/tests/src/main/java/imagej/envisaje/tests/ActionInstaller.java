package imagej.envisaje.tests;

import java.io.IOException;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.text.Keymap;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
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
			if (shortcut!=null || shortcut.isEmpty()) {
				addShortcut(actionDataObject);
			}
		}

	}

	private static void addActionToMenu(String menuPath, DataObject actionDataObject) {
		// add action to menu
		FileObject menuFO = FileUtil.getConfigFile("Menu/" + menuPath);
		DataFolder menusFolder = DataFolder.findFolder(menuFO);
		try {
			org.openide.loaders.DataShadow shadow2 = actionDataObject.createShadow(menusFolder);
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
					FileUtil.createFolder(Repository.getDefault().getDefaultFileSystem().getRoot(), "Shortcuts"));
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
}
