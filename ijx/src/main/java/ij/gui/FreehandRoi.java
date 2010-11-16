package ij.gui;

import java.awt.*;
import java.awt.image.*;
import ij.*;
import ijx.CentralLookup;
import ijx.IjxImagePlus;

/** Freehand region of interest or freehand line of interest*/
public class FreehandRoi extends PolygonRoi {


	public FreehandRoi(int sx, int sy, IjxImagePlus imp) {
		super(sx, sy, imp);
		if (((IjxToolbar) CentralLookup.getDefault().lookup(IjxToolbar.class)).getToolId()==IjxToolbar.FREEROI)
			type = FREEROI;
		else
			type = FREELINE;
		if (nPoints==2) nPoints--;
	}

	protected void grow(int sx, int sy) {
		int ox = ic.offScreenX(sx);
		int oy = ic.offScreenY(sy);
		if (ox<0) ox = 0;
		if (oy<0) oy = 0;
		if (ox>xMax) ox = xMax;
		if (oy>yMax) oy = yMax;
		if (ox!=xp[nPoints-1]+x || oy!=yp[nPoints-1]+y) {
			xp[nPoints] = ox-x;
			yp[nPoints] = oy-y;
			nPoints++;
			if (nPoints==xp.length)
				enlargeArrays();
			drawLine();
		}
	}
	
	void drawLine() {
		int x1 = xp[nPoints-2]+x;
		int y1 = yp[nPoints-2]+y;
		int x2 = xp[nPoints-1]+x;
		int y2 = yp[nPoints-1]+y;
		int xmin = Math.min(x1, x2);
		int xmax = Math.max(x1, x2);
		int ymin = Math.min(y1, y2);
		int ymax = Math.max(y1, y2);
		int margin = 4;
		if (lineWidth>margin && isLine())
			margin = lineWidth;
		if (ic!=null) {
			double mag = ic.getMagnification();
			if (mag<1.0) margin = (int)(margin/mag);
		}
		imp.draw(xmin-margin, ymin-margin, (xmax-xmin)+margin*2, (ymax-ymin)+margin*2);
	}

	public void handleMouseUp(int screenX, int screenY) {
		if (getState()==CONSTRUCTING) {
            addOffset();
			finishPolygon();
        }
		setState(NORMAL);
	}

}
