package imagej.tool;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.JFrame;
/**
 *
 * @author rick
 */
public class ToolHandler implements MouseWheelListener, KeyListener, MouseListener {

    private ITool iTool;
    private JFrame jframe;
    
    public ToolHandler(JFrame jframe, ITool iTool )
    {
        // assign the Tool
        this.iTool = iTool;
        this.jframe = jframe;
        jframe.addMouseWheelListener(this);
        jframe.addKeyListener(this);
        
    }

    public ITool getTool() { return iTool; }

    @Override
    public void mouseWheelMoved( MouseWheelEvent mwe ) {
        int notches = mwe.getWheelRotation();
        if (notches < 0)
        {
           // TODO  Mouse Wheel up
        } else { // TODO mouse Wheel down
            
        }
    }

    @Override
    public void keyTyped( KeyEvent ke ) {
      // Hmmm is this needed?
    }

    @Override
    public void keyPressed(KeyEvent ke) {
       iTool.onKeyDown( ke.getKeyCode(), ke.SHIFT_DOWN_MASK );
    }

    @Override
    public void keyReleased(KeyEvent ke) {
        iTool.onKeyUp( ke.getKeyCode(), ke.SHIFT_DOWN_MASK );
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mousePressed(MouseEvent me) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mouseEntered(MouseEvent me) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mouseExited(MouseEvent me) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
