package ijx.implementation.swing;

import ijx.gui.IjxWindow;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

/**
 *
 * @author GBH
 */
public class WindowSwing extends JFrame implements IjxWindow{

  private boolean closed;
    @Override
    public boolean isClosed() {
      return closed;
    }

    @Override
    public boolean canClose() {
      return true;
    }

    @Override
    public boolean close() {
        throw new UnsupportedOperationException("Not supported yet."); // @todo
    }

    public ImageIcon getImageIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
