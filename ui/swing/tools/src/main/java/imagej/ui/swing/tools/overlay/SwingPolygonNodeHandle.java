package imagej.ui.swing.tools.overlay;

import java.awt.Point;
import java.awt.event.InputEvent;

import org.jhotdraw.draw.BezierFigure;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.handle.BezierNodeHandle;

/*
 * The BezierFigure uses a BezierNodeHandle which can change the curve
 * connecting vertices from a line to a Bezier curve. We subclass both 
 * the figure and the node handle to defeat this.
 */
public class SwingPolygonNodeHandle extends BezierNodeHandle {

	public SwingPolygonNodeHandle(final BezierFigure owner, final int index,
		final Figure transformOwner)
	{
		super(owner, index, transformOwner);
	}

	public SwingPolygonNodeHandle(final BezierFigure owner, final int index) {
		super(owner, index);
	}

	@Override
	public void trackEnd(final Point anchor, final Point lead,
		final int modifiersEx)
	{
		// Remove the behavior associated with the shift keys
		super.trackEnd(anchor, lead, modifiersEx &
			~(InputEvent.META_DOWN_MASK | InputEvent.CTRL_DOWN_MASK |
				InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
	}

}