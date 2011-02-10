package ijx.gui;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;

/**
 *
 * @author GBH <imagejdev.org>
 */
public abstract class AbstractIcon implements Icon {
        private int HEIGHT = 10;
        private int WIDTH = 10;
        public int getIconWidth() { return WIDTH; }
        public int getIconHeight() { return HEIGHT; }

        // constructor
//        public SomeIcon(int size) {
//            HEIGHT = size;
//            WIDTH = size;
//        }

        public abstract void paintIcon(Component comp, Graphics g, int x, int y);
    }
