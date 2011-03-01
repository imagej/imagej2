package imagej.core.tools;

import imagej.tool.AbstractTool;
import imagej.tool.Tool;

import java.awt.event.KeyEvent;

// TODO - rework tool framework to use event bus instead
// E.g., PanTool implements ImageSubscriber<DisplayMouseEvent>?

/**
 * TODO
 * 
 * @author Rick Lentz
 * @author Grant Harris
 * @author Curtis Rueden
 */
@Tool(
	name = "Pan",
	description = "Pans the display",
	iconPath = "/tools/pan.png"
)
public class PanTool extends AbstractTool {

	private static final float PAN_AMOUNT = 10;

	private int lastX, lastY;

	@Override
	public void onKeyDown(int keyCode, int shift) {
		switch (keyCode) {
			case KeyEvent.VK_UP:
				display.pan(0, -PAN_AMOUNT);
				break;
			case KeyEvent.VK_DOWN:
				display.pan(0, -PAN_AMOUNT);
				break;
			case KeyEvent.VK_LEFT:
				display.pan(-PAN_AMOUNT, 0);
				break;
			case KeyEvent.VK_RIGHT:
				display.pan(PAN_AMOUNT, 0);
				break;
		}
	}

	@Override
	public void onMouseDown(int button, int shift, int x, int y) {
		lastX = x;
		lastY = y;
	}

	@Override
	public void onMouseMove(int button, int shift, int x, int y) {
		display.pan(x - lastX, y - lastY);
		lastX = x;
		lastY = y;
	}

}
