package ijx.implementation.mdi;

import ijx.gui.IjxWindow;
import imagej.util.GraphicsUtilities;

import javax.swing.ImageIcon;
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
