package imagej.tool;

import javax.swing.JFrame;

/**
 * 
 * @author rick
 */
@Tool
public class ProbeTool implements ITool {

    JFrame jFrame;
    public void setJFrame( JFrame jFrame ){
        this.jFrame = jFrame;
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
         jFrame.setTitle( "DoubleClick "  );
    }

    @Override
    public void onKeyDown(int keyCode, int shift)
    {
         jFrame.setTitle( "Key down " + keyCode );
    }

    @Override
    public void onKeyUp(int keyCode, int shift)
    {
       jFrame.setTitle( "Key up " + keyCode );
    }

    @Override
    public void onMouseDown(int button, int shift, int x, int y)
    {
        jFrame.setTitle( "Mouse down " + x + " " + y + " " + shift );

    }

    @Override
    public void onMouseMove(int button, int shift, int x, int y)
    {
         jFrame.setTitle( "Mouse move " + x + " " + y + " " + shift );
    }

    @Override
    public void onMouseUp(int button, int shift, int x, int y)
    {
         jFrame.setTitle( "Mouse up " + x + " " + y + " " + shift );
    }
}
