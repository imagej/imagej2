package imagej.ui.swing.tools.overlay;

import java.util.Collection;
import java.util.LinkedList;

import org.jhotdraw.draw.BezierFigure;
import org.jhotdraw.draw.handle.BezierOutlineHandle;
import org.jhotdraw.draw.handle.Handle;

/**
 * JHotDraw Figure for a multi-segmented line
 * 
 * @author Benjamin Nanes
 */
public class PolylineFigure extends BezierFigure {
        
    public PolylineFigure() {
        super(false);
    }
    
    @Override
	public Collection<Handle> createHandles(final int detailLevel) {
		final LinkedList<Handle> handles = new LinkedList<Handle>();
		if (detailLevel != 0) {
			return super.createHandles(detailLevel);
		}
		handles.add(new BezierOutlineHandle(this));
		for (int i = 0, n = path.size(); i < n; i++) {
			handles.add(new PolygonNodeHandle(this, i));
		}
		return handles;
	}
    
}
