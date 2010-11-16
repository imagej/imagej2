package ijx.gui.util;

//: GlobalHotkeyManager.java

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class GlobalHotkeyManager
        extends EventQueue {

    private static final boolean DEBUG = true; // BUG? what's that? ;-))
    
    private static final GlobalHotkeyManager instance = new GlobalHotkeyManager();
    private final InputMap keyStrokes = new InputMap();
    private final ActionMap actions = new ActionMap();

    static {
        // here we register ourselves as a new link in the chain of responsibility
        Toolkit.getDefaultToolkit().getSystemEventQueue().push(instance);
    }

    private GlobalHotkeyManager() {
    } // One is enough - singleton

    public static GlobalHotkeyManager getInstance() {
        return instance;
    }

    public InputMap getInputMap() {
        return keyStrokes;
    }

    public ActionMap getActionMap() {
        return actions;
    }

    @Override
    protected void dispatchEvent(AWTEvent event) {
        if (event instanceof KeyEvent) {
            // KeyStroke.getKeyStrokeForEvent converts an ordinary KeyEvent
            // to a keystroke, as stored in the InputMap.  Keep in mind that
            // Numpad keystrokes are different to ordinary keys, i.e. if you
            // are listening to
            KeyStroke ks = KeyStroke.getKeyStrokeForEvent((KeyEvent) event);
            if (DEBUG) {
                System.out.println("GlobalHotkeyManager> KeyStroke=" + ks);
            }
            String actionKey = (String) keyStrokes.get(ks);
            if (actionKey != null) {
                if (DEBUG) {
                    System.out.println("GlobalHotkeyManager> ActionKey=" + actionKey);
                }
                Action action = actions.get(actionKey);
                if (action != null && action.isEnabled()) {
                    // I'm not sure about the parameters
                    action.actionPerformed(
                            new ActionEvent(event.getSource(), event.getID(),
                            actionKey, ((KeyEvent) event).getModifiers()));
                    return; // consume event
                }
            }
        }
        super.dispatchEvent(event); // let the next in chain handle event
    }

    /*
    // GlobalHotkeyManager Example
    
    private final static String QQQ_KEY = "UIRobot";
    private final KeyStroke qqqHotkey = KeyStroke.getKeyStroke(
    KeyEvent.VK_R, KeyEvent.CTRL_MASK + KeyEvent.ALT_MASK, false);
    private final Action qqq = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
    setEnabled(false); // stop any other events from interfering
    // JOptionPane.showMessageDialog(GlobalHotkeyManagerTest.this,
    // "UIRobot Hotkey was pressed");
    setEnabled(true);
    }
    };
    
    // Then, in method... set them...
    
    GlobalHotkeyManager hotkeyManager = GlobalHotkeyManager.getInstance();
    hotkeyManager.getInputMap().put(qqqHotkey, QQQ_KEY);
    hotkeyManager.getActionMap().put(QQQ_KEY, qqq);
     */
}
