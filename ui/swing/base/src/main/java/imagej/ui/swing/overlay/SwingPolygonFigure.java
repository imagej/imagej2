package imagej.ui.swing.overlay;

import java.util.Collection;
import java.util.LinkedList;

import org.jhotdraw.draw.BezierFigure;
import org.jhotdraw.draw.handle.BezierOutlineHandle;
import org.jhotdraw.draw.handle.Handle;

public class SwingPolygonFigure extends BezierFigure {

	public SwingPolygonFigure() {
		// The constructor makes the BezierFigure a closed figure.
		super(true);
	}

	@Override
	public Collection<Handle> createHandles(final int detailLevel) {
		final LinkedList<Handle> handles = new LinkedList<Handle>();
		if (detailLevel != 0) {
			return super.createHandles(detailLevel);
		}
		handles.add(new BezierOutlineHandle(this));
		for (int i = 0, n = path.size(); i < n; i++) {
			handles.add(new SwingPolygonNodeHandle(this, i));
		}
		return handles;
	}

	private static final long serialVersionUID = 1L;

}
