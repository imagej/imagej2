package imagej.core.tools;

import imagej.tool.ITool;
import imagej.tool.Tool;
import javax.swing.JFrame;

/**
 * TODO
 *
 * @author Rick Lentz
 * @author Grant Harris
 */
@Tool
public class PencilTool implements ITool {


    public void setJFrame( JFrame jFrame ){
         throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean deactivate()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getCursor() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean onContextMenu(int x, int y) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void onDblClick()
    {
         throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void onKeyDown(int keyCode, int shift)
    {
         throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void onKeyUp(int keyCode, int shift)
    {
       throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void onMouseDown(int button, int shift, int x, int y)
    {
         throw new UnsupportedOperationException("Not supported yet.");

    }

    @Override
    public void onMouseMove(int button, int shift, int x, int y)
    {
         throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void onMouseUp(int button, int shift, int x, int y)
    {
         throw new UnsupportedOperationException("Not supported yet.");
    }

	@Override
	public boolean activate() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
