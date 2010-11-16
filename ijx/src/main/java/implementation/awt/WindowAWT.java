package implementation.awt;

import ijx.gui.IjxWindow;
import java.awt.Frame;
import javax.swing.ImageIcon;

/**
 *
 * @author GBH
 */
public class WindowAWT extends Frame implements IjxWindow{

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
