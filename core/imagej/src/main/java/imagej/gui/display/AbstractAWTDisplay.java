package imagej.gui.display;

import imagej.display.Display;
import imagej.display.event.DisplayActivatedEvent;
import imagej.display.event.key.KyPressedEvent;
import imagej.display.event.key.KyReleasedEvent;
import imagej.display.event.key.KyTypedEvent;
import imagej.display.event.mouse.MsButtonEvent;
import imagej.display.event.mouse.MsClickedEvent;
import imagej.display.event.mouse.MsDraggedEvent;
import imagej.display.event.mouse.MsMovedEvent;
import imagej.display.event.mouse.MsPressedEvent;
import imagej.display.event.mouse.MsReleasedEvent;
import imagej.display.event.mouse.MsWheelEvent;
import imagej.event.Events;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * TODO
 *
 * @author Curtis Rueden
 */
public abstract class AbstractAWTDisplay implements Display, KeyListener,
	MouseListener, MouseMotionListener, MouseWheelListener, WindowListener
{

	// -- KeyListener methods --

	@Override
	public void keyTyped(KeyEvent e) {
		Events.publish(new KyTypedEvent(this,
				e.getKeyChar(), e.getKeyCode(), e.getModifiers()));
	}

	@Override
	public void keyPressed(KeyEvent e) {
		Events.publish(new KyPressedEvent(this,
				e.getKeyChar(), e.getKeyCode(), e.getModifiers()));
	}

	@Override
	public void keyReleased(KeyEvent e) {
		Events.publish(new KyReleasedEvent(this,
			e.getKeyChar(), e.getKeyCode(), e.getModifiers()));
	}

	// -- MouseListener methods --

	@Override
	public void mouseClicked(MouseEvent e) {
		Events.publish(new MsClickedEvent(this,
				e.getX(), e.getY(), mouseButton(e)));
	}

	@Override
	public void mousePressed(MouseEvent e) {
		Events.publish(new MsPressedEvent(this,
				e.getX(), e.getY(), mouseButton(e)));
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		Events.publish(new MsReleasedEvent(this,
			e.getX(), e.getY(), mouseButton(e)));
	}

	// -- MouseMotionListener methods --

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		Events.publish(new MsDraggedEvent(this,
			e.getX(), e.getY(), mouseButton(e)));
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		Events.publish(new MsMovedEvent(this, e.getX(), e.getY()));
	}

	// -- MouseWheelListener methods --

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		Events.publish(new MsWheelEvent(this,
			e.getX(), e.getY(), e.getWheelRotation()));
	}

	// -- WindowListener methods --

	@Override
	public void windowActivated(WindowEvent e) {
		Events.publish(new DisplayActivatedEvent(this));
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO
	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO
	}

	private int mouseButton(final MouseEvent e) {
		switch (e.getButton()) {
			case MouseEvent.BUTTON1:
				return MsButtonEvent.LEFT_BUTTON;
			case MouseEvent.BUTTON2:
				return MsButtonEvent.RIGHT_BUTTON;
			case MouseEvent.BUTTON3:
				return MsButtonEvent.MIDDLE_BUTTON;
			default:
				return -1;
		}
	}

}
