package ijx.gui.util;

//: GlobalHotkeyManagerTest.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GlobalHotkeyManagerTest extends JFrame {

    private final static String UIROBOT_KEY = "UIRobot";
    private final KeyStroke uirobotHotkey = KeyStroke.getKeyStroke(
            KeyEvent.VK_R, KeyEvent.CTRL_MASK + KeyEvent.ALT_MASK, false);
    private final Action uirobot = new AbstractAction() {

        public void actionPerformed(ActionEvent e) {
            setEnabled(false); // stop any other events from interfering
            JOptionPane.showMessageDialog(GlobalHotkeyManagerTest.this,
                    "UIRobot Hotkey was pressed");
            setEnabled(true);
        }
    };
    //==================================================
    private final static String QQQ_KEY = "QQQ";
    private final KeyStroke qqqHotkey = KeyStroke.getKeyStroke(
            KeyEvent.VK_X, KeyEvent.CTRL_MASK + KeyEvent.ALT_MASK, false);
    private final Action qqq = new AbstractAction() {

        public void actionPerformed(ActionEvent e) {
            setEnabled(false); // stop any other events from interfering
            // JOptionPane.showMessageDialog(GlobalHotkeyManagerTest.this,
            // "UIRobot Hotkey was pressed");
            setEnabled(true);
        }
    };

    public GlobalHotkeyManagerTest() {
        super("Global Hotkey Manager Test");
        setSize(500, 400);
        getContentPane().setLayout(new FlowLayout());
        getContentPane().add(new JButton("Button 1"));
        getContentPane().add(new JTextField(20));
        getContentPane().add(new JButton("Button 2"));

        GlobalHotkeyManager hotkeyManager = GlobalHotkeyManager.getInstance();
        hotkeyManager.getInputMap().put(uirobotHotkey, UIROBOT_KEY);
        hotkeyManager.getActionMap().put(UIROBOT_KEY, uirobot);

        hotkeyManager.getInputMap().put(qqqHotkey, QQQ_KEY);
        hotkeyManager.getActionMap().put(QQQ_KEY, qqq);



        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // JDK 1.3
        setVisible(true);
    }

    public static void main(String[] args) {
        new GlobalHotkeyManagerTest();



    }
}

