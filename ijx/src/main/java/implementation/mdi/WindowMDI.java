package implementation.mdi;

import implementation.swing.*;
import ijx.gui.IjxWindow;
import ijx.util.GraphicsUtilities;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;

/**
 *
 * @author GBH
 */
public class WindowMDI extends JInternalFrame implements IjxWindow{

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
       return new ImageIcon(GraphicsUtilities.iconToImage(getFrameIcon()));
    }


}
