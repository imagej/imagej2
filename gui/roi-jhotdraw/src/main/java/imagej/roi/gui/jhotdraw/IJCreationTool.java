package imagej.roi.gui.jhotdraw;

import imagej.roi.ImageJROI;

import org.jhotdraw.framework.DrawingEditor;
import org.jhotdraw.framework.Figure;
import org.jhotdraw.standard.CreationTool;

public class IJCreationTool extends CreationTool {

	IJHotDrawROIAdapter adapter;
	String roiName;
	public IJCreationTool(DrawingEditor editor, IJHotDrawROIAdapter adapter, String roiName) {
		super(editor);
		this.adapter = adapter;
		this.roiName = roiName;
	}
	

	/* (non-Javadoc)
	 * @see org.jhotdraw.standard.CreationTool#createFigure()
	 */
	@Override
	protected Figure createFigure() {
		ImageJROI roi = adapter.createNewROI(roiName);
		Figure figure = adapter.attachFigureToROI(roi);
		return figure;
	}

}
