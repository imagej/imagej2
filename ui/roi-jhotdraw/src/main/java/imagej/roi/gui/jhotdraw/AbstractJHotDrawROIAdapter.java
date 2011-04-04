//
// AbstractJHotDrawROIAdapter.java
//

package imagej.roi.gui.jhotdraw;

import java.util.HashMap;
import java.util.Map;

import org.jhotdraw.framework.Figure;
import imagej.roi.ImageJROI;
import org.jhotdraw.framework.FigureChangeEvent;
import org.jhotdraw.framework.FigureChangeListener;

/**
 * An abstract class that gives default behavior for the IJHotDrawROIAdapter interface
 *
 * @author Lee Kamentsky
 */
public abstract class AbstractJHotDrawROIAdapter implements IJHotDrawROIAdapter, FigureChangeListener {

	protected Map<Figure, ImageJROI> map = new HashMap<Figure, ImageJROI>();
	
	/* (non-Javadoc)
	 * @see imagej.roi.gui.jhotdraw.IJHotDrawROIAdapter#attachFigureToROI(imagej.roi.ImageJROI)
	 */
	@Override
	public Figure attachFigureToROI(ImageJROI roi) {
		Figure figure = createFigureForROI(roi);
		map.put(figure, roi);
		figure.addFigureChangeListener(this);
		return figure;
	}

	/* (non-Javadoc)
	 * @see imagej.roi.gui.jhotdraw.IJHotDrawROIAdapter#detachFigureFromROI(CH.ifa.draw.framework.Figure)
	 */
	@Override
	public void detachFigureFromROI(Figure figure) {
		figure.removeFigureChangeListener(this);
	}

	@Override
	public void figureChanged(FigureChangeEvent event) {
		Figure figure = event.getFigure();
		updateROIModel(figure, map.get(figure));
	}

	@Override
	public void figureInvalidated(FigureChangeEvent event) {
		
	}

	@Override
	public void figureRemoved(FigureChangeEvent event) {
		
	}

	@Override
	public void figureRequestRemove(FigureChangeEvent event) {
		
	}

	@Override
	public void figureRequestUpdate(FigureChangeEvent event) {
		
	}
	/**
	 * Create the appropriate figure for the given ROI.
	 * @param roi
	 * @return
	 */
	protected abstract Figure createFigureForROI(ImageJROI roi);
	/**
	 * The implementer should update the ROI model to reflect the
	 * data in the figure.
	 * @param figure
	 * @param roi
	 */
	protected abstract void updateROIModel(Figure figure, ImageJROI roi);

}
