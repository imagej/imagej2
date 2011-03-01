package imagej.gui.display;

import imagej.display.Display;
import imagej.tool.ITool;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * TODO
 *
 * @author Curtis Rueden
 * @author Grant Harris
 * @author Rick Lentz
 */
public abstract class AbstractSwingDisplay implements Display,
	KeyListener, MouseListener, MouseMotionListener, MouseWheelListener
{

	private ITool activeTool;

	public void setActiveTool(final ITool activeTool) {
		if (this.activeTool != null) this.activeTool.deactivate();
		this.activeTool = activeTool;
		activeTool.activate(this);
	}

	public ITool getActiveTool() {
		return activeTool;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent mwe) {
		int notches = mwe.getWheelRotation();
		if (notches < 0)
		{
			// TODO  Mouse Wheel up
		}
		else {
			// TODO mouse Wheel down
		}
	}

	@Override
	public void keyTyped(KeyEvent ke) {
		// NB: No implementation needed.
	}

	@Override
	public void keyPressed(KeyEvent ke) {
		if (activeTool == null) return;
		activeTool.onKeyDown( ke.getKeyCode(), InputEvent.SHIFT_DOWN_MASK );
	}

	@Override
	public void keyReleased(KeyEvent ke) {
		if (activeTool == null) return;
		activeTool.onKeyUp( ke.getKeyCode(), InputEvent.SHIFT_DOWN_MASK );
	}

	// ---------

	@Override
	public void mouseClicked(MouseEvent me) {
		// NB: No implementation needed.
	}

	@Override
	public void mousePressed(MouseEvent me) {
		if (activeTool == null) return;
		activeTool.onMouseDown(me.getButton(),
			me.getModifiers(), me.getX(), me.getY());
	}

	@Override
	public void mouseReleased(MouseEvent me) {
		if (activeTool == null) return;
		activeTool.onMouseUp(me.getButton(),
			me.getModifiers(), me.getX(), me.getY());
	}

	@Override
	public void mouseEntered(MouseEvent me) {
		// TODO
	}

	@Override
	public void mouseExited(MouseEvent me) {
		// TODO
	}

	@Override
	public void mouseDragged(MouseEvent me) {
		// TODO
	}

	@Override
	public void mouseMoved(MouseEvent me) {
		if (activeTool == null) return;
		activeTool.onMouseMove(me.getButton(),
			me.getModifiers(), me.getX(), me.getY());
	}

}
