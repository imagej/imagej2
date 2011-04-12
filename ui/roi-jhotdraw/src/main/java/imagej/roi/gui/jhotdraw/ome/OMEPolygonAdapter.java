package imagej.roi.gui.jhotdraw.ome;

import imagej.roi.ImageJROI;
import imagej.roi.gui.jhotdraw.JHotDrawROIAdapter;
import imagej.roi.ome.OMEPolygonROI;
import imagej.util.Log;

import java.awt.Point;
import java.awt.Polygon;

import ome.xml.model.Polyline;

import org.jhotdraw.figures.AttributeFigure;
import org.jhotdraw.contrib.PolygonFigure;
import org.jhotdraw.framework.Figure;


@JHotDrawROIAdapter
public class OMEPolygonAdapter extends BaseShapeAdapter {
	private static final String TYPE_NAME = "Ome Polygon";
	
	@Override
	public boolean supports(ImageJROI roi) {
		return roi instanceof OMEPolygonROI;
	}
	
	@Override
	public String[] getROITypeNames() {
		return new String[] { TYPE_NAME };
	}
	
	@Override
	public String displayName() {
		return TYPE_NAME;
	}
	
	static private Polyline getOmePolygon(ImageJROI roi) {
		if (! ( roi instanceof OMEPolygonROI)) {
			throw new UnsupportedOperationException("Can't adapt to " + roi.getClass().getName());
		}
		return ((OMEPolygonROI) roi).getOMEShape();
	}
	
	static private PolygonFigure getPolygonFigure(Figure f) {
		if (! (f instanceof PolygonFigure)) {
			throw new UnsupportedOperationException("Can't adapt from " + f.getClass().getName());
		}
		return (PolygonFigure)f;
	}
	
	@Override
	protected AttributeFigure getFigure(ImageJROI roi) {
		Polyline e = getOmePolygon(roi);
		//TODO:
//		e.getPoints();
		return null; //new PolygonFigure();
	}
	
	@Override
	protected void updateROIModel(Figure figure, ImageJROI roi) {
//		super.updateROIModel(figure, roi);
//		Polygon e = getOmePolygon(roi);
//		PolygonFigure ef = getPolygonFigure(figure);
//		java.awt.Rectangle displayBox = ef.displayBox();
//		Log.debug(String.format("Polygon: x=%f, y=%f, w=%f, h=%f", 
//				displayBox.getX(), displayBox.getY(), 
//				displayBox.getWidth(), displayBox.getHeight()));
//		e.setX(new Double(displayBox.getX()));
//		e.setY(new Double(displayBox.getY()));
//		e.setRadiusX(new Double(displayBox.getWidth() / 2.));
//		e.setRadiusY(new Double(displayBox.getHeight() / 2.));
	}

	@Override
	public String getIconName() {
		return "/org/jhotdraw/images/POLYGON";
	}

	@Override
	public ImageJROI createNewROI(String name) {
		if (name.equals(TYPE_NAME)) {
			OMEPolygonROI roi = new OMEPolygonROI();
			Polyline r = roi.getOMEShape();
			// TODO:
			//r.setPoints("");
			return roi;
		}
		throw new UnsupportedOperationException("Don't know how to make " + name);
	}
}