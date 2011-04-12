package imagej.roi.gui.jhotdraw.ome;

import imagej.roi.ImageJROI;
import imagej.roi.gui.jhotdraw.JHotDrawROIAdapter;
import imagej.roi.ome.OMEEllipseROI;
import imagej.util.Log;

import java.awt.Point;

import ome.xml.model.Ellipse;

import org.jhotdraw.figures.AttributeFigure;
import org.jhotdraw.figures.EllipseFigure;
import org.jhotdraw.framework.Figure;


@JHotDrawROIAdapter
public class OMEEllipseAdapter extends BaseShapeAdapter {
	private static final String TYPE_NAME = "Ome Ellipse";
	
	@Override
	public boolean supports(ImageJROI roi) {
		return roi instanceof OMEEllipseROI;
	}
	
	@Override
	public String[] getROITypeNames() {
		return new String[] { TYPE_NAME };
	}
	
	@Override
	public String displayName() {
		return TYPE_NAME;
	}
	
	static private Ellipse getOmeEllipse(ImageJROI roi) {
		if (! ( roi instanceof OMEEllipseROI)) {
			throw new UnsupportedOperationException("Can't adapt to " + roi.getClass().getName());
		}
		return ((OMEEllipseROI) roi).getOMEShape();
	}
	
	static private EllipseFigure getEllipseFigure(Figure f) {
		if (! (f instanceof EllipseFigure)) {
			throw new UnsupportedOperationException("Can't adapt from " + f.getClass().getName());
		}
		return (EllipseFigure)f;
	}
	
	@Override
	protected AttributeFigure getFigure(ImageJROI roi) {
		Ellipse e = getOmeEllipse(roi);
		int x = e.getX().intValue();
		int y = e.getY().intValue();
		int rx = e.getRadiusX().intValue();
		int rb = e.getRadiusY().intValue();
		Point origin = new Point(x, y);
		Point corner = new Point(x - rx, y - rb);
		return new EllipseFigure(origin, corner);
	}
	
	@Override
	protected void updateROIModel(Figure figure, ImageJROI roi) {
		super.updateROIModel(figure, roi);
		Ellipse e = getOmeEllipse(roi);
		EllipseFigure ef = getEllipseFigure(figure);
		java.awt.Rectangle displayBox = ef.displayBox();
		Log.debug(String.format("Ellipse: x=%f, y=%f, w=%f, h=%f", 
				displayBox.getX(), displayBox.getY(), 
				displayBox.getWidth(), displayBox.getHeight()));
		e.setX(new Double(displayBox.getX()));
		e.setY(new Double(displayBox.getY()));
		e.setRadiusX(new Double(displayBox.getWidth() / 2.));
		e.setRadiusY(new Double(displayBox.getHeight() / 2.));
	}

	@Override
	public String getIconName() {
		// TODO ellipse icon name?
		return "/org/jhotdraw/images/ELLIPSE";
	}

	@Override
	public ImageJROI createNewROI(String name) {
		if (name.equals(TYPE_NAME)) {
			OMEEllipseROI roi = new OMEEllipseROI();
			Ellipse r = roi.getOMEShape();
			r.setX(0.);
			r.setY(0.);
			r.setRadiusX(10.);
			r.setRadiusY(10.);
			return roi;
		}
		throw new UnsupportedOperationException("Don't know how to make " + name);
	}
}