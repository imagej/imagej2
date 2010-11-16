package ijx;

import ij.*;
import ij.gui.*;
import ij.io.*;
import ij.plugin.frame.*;
import ij.text.TextWindow;
import ij.macro.Interpreter;
import ijx.app.IjxApplication;
import ijx.gui.IjxImageWindow;
import ijx.gui.IjxWindow;
import java.io.File;
import java.applet.Applet;


import ijx.sezpoz.ActionIjx;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FileWindowCommands {

    /**	Runs File and Window menu commands. */

    /* Template:
    @MenuItem(menu = "", label = "", hotKey = KeyEvent.VK_O)
    public static class  implements ActionListener {
    public void actionPerformed(ActionEvent e) {
    }
    }
     */
    @ActionIjx(
    label = "TestItem",
    menu = "File",
    commandKey = "commandName",
    toolbar = "main",
    hotKey = "alt shift X",
    mnemonic = java.awt.event.KeyEvent.VK_1,
    tip = "Tool tip displayed",
    position = 9,
    separate = true,
    icon = "demo/plugin1/movieNew16gif",
    bundle = "demo.plugin1.properties")
    public static class TestAction implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            close();
        }
    }
    @ActionIjx(label = "RadioItemA",
    menu = "File",
    commandKey = "radioA",
    hotKey = "alt shift A",
    mnemonic = java.awt.event.KeyEvent.VK_1,
    tip = "Tool tip displayed",
    group = "group1",
    bundle = "demo.plugin1.properties")
    public static class RadioActionA implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            System.out.println("Yaba Daba Doo");
        }
    }

    @ActionIjx(label = "RadioItemB",
    menu = "File",
    commandKey = "radioB",
    hotKey = "alt shift B",
    mnemonic = java.awt.event.KeyEvent.VK_1,
    tip = "Tool tip displayed",
    group = "group1",
    bundle = "demo.plugin1.properties")
    public static class RadioActionB implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            close();
        }
    }

    //===============================================================

    @ActionIjx(menu = "File", label = "Open...", hotKey = "O", position=100)
    public static class OpenAction implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            new ExecuterIjx("theComand()", e).run();

            actionInvoked("command", e);
//            if (Prefs.useJFileChooser && !IJ.macroRunning()) {
//                new Opener().openMultiple();
//            } else {
//                new Opener().open();
//            }
        }
    }

    @ActionIjx(menu = "File", label = "Close", hotKey = "W", position=400)
    public static class CloseAction implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            close();
        }
    }

    @ActionIjx(menu = "File", label = "Close All", position=500)
    public static class CloseAllAction implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            closeAll();
        }
    }

    @ActionIjx(menu = "File", label = "Save", hotKey = "S", position=300)
    public static class SaveAction implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            save();
        }
    }

    @ActionIjx(menu = "File", label = "Revert", hotKey = "R")
    public static class RevertAction implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            revert();
        }
    }

    @ActionIjx(menu = "edit", label = "Undo", hotKey = "Z")
    public static class UndoAction implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            undo();
        }
    }

    @ActionIjx(menu = "window", label = "Put Behind [tab]")
    public static class PutBehindAction implements ActionListener {

        public void actionPerformed(ActionEvent e) {
        }
    }

    @ActionIjx(menu = "File", label = "Quit", position=9999999)
    public static class QuitAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            IjxApplication ij = IJ.getInstance();
            if (ij != null) {
                ij.quit();
            }
        }
    }

    @ActionIjx(menu = "File", label = "Image...[n]")
    public static class NewImageAction implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            new NewImage().openImage();
        }
    }

    @ActionIjx(menu = "File", label = "Startup Macros...")
    public static class StartupMacrosAction implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            openStartupMacros();
        }
    }

    /** Handle menu events. */
    public static void actionInvoked(String cmd, ActionEvent e) {
//		if ((e.getSource() instanceof MenuItem)) {
//			MenuItem item = (MenuItem)e.getSource();
//			String cmd = e.getActionCommand();
//			if (item.getParent()==Menus.openRecentMenu) {
//				new RecentOpener(cmd); // open image in separate thread
//				return;
//			}

//			int flags = e.getModifiers();
        //IJ.log(""+KeyEvent.getKeyModifiersText(flags));
//			boolean hotkey = false;
//			long actionPerformedTime = System.currentTimeMillis();
//			long ellapsedTime = actionPerformedTime-keyPressedTime;
////			if (cmd!=null && (ellapsedTime>=200L||!cmd.equals(lastKeyCommand))) {
//				if ((flags & Event.ALT_MASK)!=0)
//					IJ.setKeyDown(KeyEvent.VK_ALT);
//				if ((flags & Event.SHIFT_MASK)!=0)
//					IJ.setKeyDown(KeyEvent.VK_SHIFT);

        new ExecuterIjx(cmd, e);

    }


    /* from Menus:
    addPlugInItem(file, "Open...", "ij.plugin.Commands(\"open\")", KeyEvent.VK_O, false);
    addPlugInItem(file, "Close", "ij.plugin.Commands(\"close\")", KeyEvent.VK_W, false);
    addPlugInItem(file, "Close All", "ij.plugin.Commands(\"close-all\")", 0, false);
    addPlugInItem(file, "Save", "ij.plugin.Commands(\"save\")", KeyEvent.VK_S, false);
    addPlugInItem(file, "Revert", "ij.plugin.Commands(\"revert\")", KeyEvent.VK_R, false);
    addPlugInItem(window, "Put Behind [tab]", "ij.plugin.Commands(\"tab\")", 0, false);
    addPlugInItem(file, "Quit", "ij.plugin.Commands(\"quit\")", 0, false);
    addPlugInItem(edit, "Undo", "ij.plugin.Commands(\"undo\")", KeyEvent.VK_Z, false);
     * from IJ_Props:
    new01="Image...[n]",ij.plugin.Commands("new")
    macros04="Startup Macros...",ij.plugin.Commands("startup")
     */
//    public void run(String cmd) {
//        if (cmd.equals("new")) {
//            new NewImage();
//        } else if (cmd.equals("open")) {
//            if (Prefs.useJFileChooser && !IJ.macroRunning()) {
//                new Opener().openMultiple();
//            } else {
//                new Opener().open();
//            }
//        } else if (cmd.equals("close")) {
//            close();
//        } else if (cmd.equals("close-all")) {
//            closeAll();
//        } else if (cmd.equals("save")) {
//            save();
//        } else if (cmd.equals("ij")) {
//            ImageJ ij = IJ.getInstance();
//            if (ij != null) {
//                ij.toFront();
//            }
//        } else if (cmd.equals("tab")) {
//            WindowManager.putBehind();
//        } else if (cmd.equals("quit")) {
//            ImageJ ij = IJ.getInstance();
//            if (ij != null) {
//                ij.quit();
//            }
//        } else if (cmd.equals("revert")) {
//            revert();
//        } else if (cmd.equals("undo")) {
//            undo();
//        } else if (cmd.equals("startup")) {
//            openStartupMacros();
//        }
//    }
    static void revert() {
        IjxImagePlus imp = WindowManager.getCurrentImage();
        if (imp != null) {
            imp.revert();
        } else {
            IJ.noImage();
        }
    }

    static void save() {
        IjxImagePlus imp = WindowManager.getCurrentImage();
        if (imp != null) {
            boolean unlockedImage = imp.getStackSize() == 1 && !imp.isLocked();
            if (unlockedImage) {
                imp.lock();
            }
            new FileSaver(imp).save();
            if (unlockedImage) {
                imp.unlock();
            }
        } else {
            IJ.noImage();
        }
    }

    static void undo() {
        IjxImagePlus imp = WindowManager.getCurrentImage();
        if (imp != null) {
            Undo.undo();
        } else {
            IJ.noImage();
        }
    }

    static void close() {
        IjxImagePlus imp = WindowManager.getCurrentImage();
        IjxWindow frame = WindowManager.getFrontWindow();
        if (frame == null || (Interpreter.isBatchMode() && frame instanceof IjxImageWindow)) {
            closeImage(imp);
        } else if (frame instanceof PlugInFrame) {
            ((PlugInFrame) frame).close();
        } else if (frame instanceof TextWindow) {
            ((TextWindow) frame).close();
        } else {
            closeImage(imp);
        }
    }

    static void closeAll() {
        int[] list = WindowManager.getIDList();
        if (list != null) {
            int imagesWithChanges = 0;
            for (int i = 0; i < list.length; i++) {
                IjxImagePlus imp = WindowManager.getImage(list[i]);
                if (imp != null && imp.isChanged()) {
                    imagesWithChanges++;
                }
            }
            if (imagesWithChanges > 0) {
                GenericDialog gd = new GenericDialog("Close All");
                String msg = null;
                if (imagesWithChanges == 1) {
                    msg = "There is one image";
                } else {
                    msg = "There are " + imagesWithChanges + " images";
                }
                gd.addMessage(msg + " with unsaved changes. If you\nclick \"OK\" they will be closed without being saved.");
                gd.showDialog();
                if (gd.wasCanceled()) {
                    return;
                }
            }
            for (int i = 0; i < list.length; i++) {
                IjxImagePlus imp = WindowManager.getImage(list[i]);
                if (imp != null) {
                    imp.setChanged(false);
                    imp.close();
                }
            }
        }
        //Frame[] windows = WindowManager.getNonImageWindows();
        //for (int i=0; i<windows.length; i++) {
        //	if ((windows[i] instanceof PlugInFrame) && !(windows[i] instanceof Editor))
        //		((PlugInFrame)windows[i]).close();
        //}
    }

    static void closeImage(IjxImagePlus imp) {
        if (imp == null) {
            IJ.noImage();
            return;
        }
        imp.close();
        if (Recorder.record && !IJ.isMacro()) {
            if (Recorder.scriptMode()) {
                Recorder.recordCall("imp.close();");
            } else {
                Recorder.record("close");
            }
            Recorder.setCommand(null); // don't record run("Close")
        }
    }

    // Plugins>Macros>Open Startup Macros command
    static void openStartupMacros() {
        Applet applet = IJ.getApplet();
        if (applet != null) {
            IJ.run("URL...", "url=" + IJ.URL + "/applet/StartupMacros.txt");
        } else {
            String path = IJ.getDirectory("macros") + "/StartupMacros.txt";
            if (IJ.runPlugIn("fiji.scripting.Script_Editor", path) != null) {
                return;
            }
            File f = new File(path);
            if (!f.exists()) {
                IJ.error("\"StartupMacros.txt\" not found in ImageJ/macros/");
            } else {
                IJ.open(path);
            }
        }
    }
}
