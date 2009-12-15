/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ijx;

import ij.*;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Properties;

/**
 *
 * @author GBH
 */
public interface IjxApplication extends Runnable, ActionListener, ItemListener,  KeyListener {

    /**
     * Address of socket where Image accepts commands
     */
    int DEFAULT_PORT = 57294;
    int EMBEDDED = 1;
    int STANDALONE = 0;
    /**
     * Plugins should call IJ.getVersion() to get the version string.
     */
    String VERSION = "1.42a";

    /**
     * Handle menu events.
     */
    void actionPerformed(ActionEvent e);

    /**
     * Handles CheckboxMenuItem state changes.
     */
    void itemStateChanged(ItemEvent e);

    /*    void keyPressed(KeyEvent e);
    
    void keyReleased(KeyEvent e);
    
    void keyTyped(KeyEvent e);
   */ 
    
    boolean isHotkey();
    
    /**
     * ImageJ calls System.exit() when qutting when 'exitWhenQuitting' is true.
     */
    void exitWhenQuitting(boolean ewq);

    /**
     * Called by ImageJ when the user selects Quit.
     */
    void quit();

    /**
     * Returns true if ImageJ is exiting.
     */
    boolean quitting();

    /**
     * Adds the specified class to a Vector to keep it from being
     * garbage collected, causing static fields to be reset.
     */
    void register(Class c);

    void doCommand(String name);

    /**
     * Quit using a separate thread, hopefully avoiding thread deadlocks.
     */
    void run();

    void runFilterPlugIn(Object theFilter, String cmd, String arg);

    Object runUserPlugIn(String commandName, String className, String arg, boolean createNewLoader);

    void abortPluginOrMacro(ImagePlus imp);

    /**
     * Called once when ImageJ quits.
     */
    void savePreferences(Properties prefs);

    void saveWindowLocations();

}
