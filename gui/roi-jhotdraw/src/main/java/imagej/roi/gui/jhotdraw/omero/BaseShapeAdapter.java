package imagej.roi.gui.jhotdraw.omero;

import java.awt.Color;
import java.awt.Font;

import ome.xml.model.Shape;
import ome.xml.model.enums.EnumerationException;
import ome.xml.model.enums.FontStyle;
import ome.xml.model.primitives.NonNegativeInteger;
import org.jhotdraw.figures.AttributeFigure;
import org.jhotdraw.framework.Figure;
import org.jhotdraw.framework.FigureAttributeConstant;

import imagej.Log;
import imagej.roi.ImageJROI;
import imagej.roi.gui.jhotdraw.AbstractJHotDrawROIAdapter;
import imagej.roi.gui.jhotdraw.IJHotDrawROIAdapter;
import imagej.roi.omero.OmeroShapeROI;

public abstract class BaseShapeAdapter extends AbstractJHotDrawROIAdapter
	implements IJHotDrawROIAdapter {
	/**
	 * Get the JHotDraw figure that represents the ROI.
	 * @param roi - ROI in question
	 * @return a figure, properly sized and placed, but the generic shape parameters need not be filled in.
	 */
	abstract protected AttributeFigure getFigure(ImageJROI roi);
	
	static private Shape getROIShape(ImageJROI roi) {
		if (! (roi instanceof OmeroShapeROI<?>)) {
			Log.error(roi.getClass().getName() + " is not an Omero shape");
			return null;
		}
		OmeroShapeROI<?> shapeROI = (OmeroShapeROI<?>)roi;
		return  shapeROI.getOMEShape();
	}
	/* (non-Javadoc)
	 * @see imagej.roi.gui.jhotdraw.AbstractJHotDrawROIAdapter#createFigureForROI(imagej.roi.ImageJROI)
	 */
	@Override
	protected Figure createFigureForROI(ImageJROI roi) {
		Shape shape = getROIShape(roi);
		AttributeFigure figure = getFigure(roi);
		Integer fill = shape.getFill();
		NonNegativeInteger font_size = shape.getFontSize();
		FontStyle font_style = shape.getFontStyle();
		if (fill != null)
			figure.setAttribute(FigureAttributeConstant.FILL_COLOR, new Color(fill.intValue()));
		if (font_size != null)
			figure.setAttribute(FigureAttributeConstant.FONT_SIZE, font_size.getValue());
		if (font_style != null) {
			int style = Font.PLAIN;
			if (font_style.name().equals(FontStyle.BOLD)) {
				style = Font.BOLD;
			} else if (font_style.name().equals(FontStyle.ITALIC)) {
				style = Font.ITALIC;
			} else if (font_style.name().equals(FontStyle.BOLDITALIC)) {
				style = Font.BOLD + Font.ITALIC;
			}
			figure.setAttribute(FigureAttributeConstant.FONT_STYLE, new Integer(style));
		}
		return figure;
	}

	/* (non-Javadoc)
	 * @see imagej.roi.gui.jhotdraw.AbstractJHotDrawROIAdapter#updateROIModel(CH.ifa.draw.framework.Figure, imagej.roi.ImageJROI)
	 */
	@Override
	protected void updateROIModel(Figure figure, ImageJROI roi) {
		Shape shape = getROIShape(roi);
		Object oFill = figure.getAttribute(FigureAttributeConstant.FILL_COLOR);
		if (oFill instanceof Color) {
			shape.setFill(new Integer(((Color)oFill).getRGB()));
		}
		Object oFontSize = figure.getAttribute(FigureAttributeConstant.FONT_SIZE);
		if (oFontSize instanceof Number) {
			shape.setFontSize(new NonNegativeInteger(((Number)oFontSize).intValue()));
		}
		Object oFontStyle = figure.getAttribute(FigureAttributeConstant.FONT_STYLE);
		if (oFontStyle instanceof Number) {
			int font_style = ((Number)oFontStyle).intValue();
			FontStyle fontStyle = FontStyle.REGULAR;
			switch (font_style & (Font.BOLD | Font.ITALIC)) {
			case Font.BOLD:
				fontStyle = FontStyle.BOLD;
				break;
			case Font.ITALIC:
				fontStyle = FontStyle.ITALIC;
				break;
			case Font.BOLD + Font.ITALIC:
				fontStyle = FontStyle.BOLDITALIC;
				break;
			}
			shape.setFontStyle(fontStyle);
		}
	}
	
}
