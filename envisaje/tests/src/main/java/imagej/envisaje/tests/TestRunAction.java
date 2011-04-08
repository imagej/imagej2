/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.envisaje.tests;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.text.Keymap;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

@ActionID(category = "Build", id = "TestRunAction")
@ActionRegistration(displayName = "Run Test")
@ActionReferences({
	@ActionReference(path = "Menu/File", position = 80)
})

public final class TestRunAction extends AbstractAction {

	public void actionPerformed(ActionEvent e) {
		
		try {
			//this.findTopLevelMenu("Menu_a");
			//ActionInstaller.registerTopLevelMenu("Menu_a", "MenuA");
			new ActionMultiFile();
			//testShortcutsFolderThisWorked();

//			ActionInstaller.registerDynamicAction("Dummy1", new DummyAction("DynAction1", "Command the first"), "File", null);
//			ActionInstaller.registerDynamicAction("Dummy2", new DummyAction("DynAction2", "Command the second"), "Process", null);
//			ActionInstaller.registerDynamicAction("Dummy3", new DummyAction("DynAction3", "Command the third"), "Edit", null);
		} catch (Exception ex) {
			DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Exception(ex));
		} 
	}
	private void dump() {
		StringBuilder sb = new StringBuilder();
		sb.append("getFileObject=======================\n");
		FileObject root = Repository.getDefault().getDefaultFileSystem().getRoot();
		FileObject dir = root.getFileObject("Menu");
		FileObject[] kids = dir.getChildren();
		for (int i = 0; i < kids.length; i++) {
			FileObject fileObject = kids[i];
			sb.append(fileObject.getName() + " > " + fileObject.getClass().getName());

			DataObject dob = null;
			try {
				dob = DataObject.find(fileObject);
			} catch (DataObjectNotFoundException ex) {
				Exceptions.printStackTrace(ex);
			}

			InstanceCookie cookie = (InstanceCookie) dob.getCookie(InstanceCookie.class);
			if (cookie != null) {
				try {
					JMenuItem theMenu = (JMenuItem) cookie.instanceCreate();
					Container parent = theMenu.getParent();
					sb.append("parent=").append(parent.getClass().getName());
				} catch (IOException ex) {
					Exceptions.printStackTrace(ex);
				} catch (ClassNotFoundException ex) {
					Exceptions.printStackTrace(ex);
				}
			}
			sb.append("\n");
		}
		sb.append("Lookups.forPath=======================\n");
		Lookup lookup = Lookups.forPath("Menu");
		Collection c = lookup.lookupAll(Object.class);
		for (Iterator it = c.iterator(); it.hasNext();) {
			Object object = it.next();
			sb.append(object.getClass().getName() + " " + object.getClass().getSuperclass().getName() + "\n");
		}

		DialogDisplayer.getDefault().notify(
				new NotifyDescriptor.Message(sb.toString(), NotifyDescriptor.INFORMATION_MESSAGE));

	}

	public void testShortcutsFolderThisWorked() throws Exception {

		// instanciate into Action folder
		FileObject actionsFO = FileUtil.getConfigFile("Actions");
		DataFolder actions = DataFolder.findFolder(actionsFO);
		//FileUtil.createFolder(Repository.getDefault().getDefaultFileSystem().getRoot(), "Actions"));

		FileObject actionObject = FileUtil.createData(actions.getPrimaryFile(), "Dummy.instance");
		actionObject.setAttribute("instanceCreate", new DummyAction("testShortcutsFolder", "the Command"));

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
		FileObject menuFO = FileUtil.getConfigFile("Menu/File");
		DataFolder menusFolder = DataFolder.findFolder(menuFO);
		//FileUtil.createFolder(Repository.getDefault().getDefaultFileSystem().getRoot(), "Menus/File"));
		org.openide.loaders.DataShadow shadow2 = actionDataObject.createShadow(menusFolder);

		//Action action = globalMap.getAction(org.openide.util.Utilities.stringToKey("C-F2"));
//        assertNotNull("Action is registered for C-F2", action);
//        assertEquals("Is dummy", DummyAction.class, action.getClass());
//        assertTrue("Has the right name", action.toString().indexOf("testShortcutsFolder") > 0);

		//
		// now simulate the module uninstall
		//
//        obj.delete();
		//dummy.delete ();
//        assertFalse(shadow.isValid());

//        ShortcutsFolder.waitShortcutsFinished();

//        action = globalMap.getAction(org.openide.util.Utilities.stringToKey("C-F2"));
//        assertEquals("No action registered", null, action);
//
//        shadow.delete();
	}

	public void tryFileChooserBuilder() {
		// Using FileChooserBuilder

		String dirKey = "images"; // key for remembering last dir. used.
		String title = "Add Library";
		String approveText = "Add";

		//The default dir to use if no value is stored
		File defaultDir = new File(System.getProperty("user.home") + File.separator + "lib");

		//     (String dirKey, String title, String approveText, File defaultDir);
		File[] filesChoosen = new FileChooserBuilder(dirKey). // key for remembering last dir. used.
				setTitle(title).
				setDefaultWorkingDirectory(defaultDir).
				setApproveText(approveText).
				setFileFilter(null).
				showMultiOpenDialog();
		//Result will be null if the user clicked cancel or closed the dialog w/o OK
		if (filesChoosen != null) {
			//do something
		}
	}

	private static final class DummyAction extends AbstractAction {

		private final String name;
		private final String command;

		public DummyAction(String name, String command) {
			this.name = name;
			this.command = command;
			this.putValue(Action.NAME, name);
		}

		public void actionPerformed(ActionEvent e) {
			DialogDisplayer.getDefault().notify(
					new NotifyDescriptor.Message("DummyAction[" + name + "] performed: " + command,
					NotifyDescriptor.INFORMATION_MESSAGE));
		}

		public String toString() {
			return "DummyAction[" + name + "]";
		}
	}
}
