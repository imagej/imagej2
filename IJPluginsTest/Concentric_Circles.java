import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import java.awt.geom.*;
import ij.plugin.*;

public class Concentric_Circles implements PlugIn {
	static int n = 5;
	static float lineWidth = 1f;
	static float xcenter, ycenter;
	static float innerR, outerR;
	boolean hide;

	public void run(String arg) {
		if (IJ.versionLessThan("1.38o"))	 return;
		ImagePlus img = IJ.getImage();
		Roi roi = img.getRoi();
		Rectangle b = new Rectangle(0, 0, img.getWidth(), img.getHeight());
		boolean isRoi = roi!=null && roi.isArea();
		boolean isLine = roi!=null && roi.getType()==Roi.LINE;
		if (isRoi||isLine) b = roi.getBounds();
		if (outerR==0f || isRoi || isLine) setup(b, isLine);
		if (!showDialog()) return;
		ImageCanvas ic = img.getCanvas();
		if (hide) {ic.setDisplayList(null); return;}
		GeneralPath path = new GeneralPath();
		float inc = (outerR-innerR)/(n-1);
		for (float r=innerR; r<=outerR+inc/100f; r+=inc)
			path.append(new Ellipse2D.Float(xcenter-r, ycenter-r, r*2f, r*2f), false);
		ic.setDisplayList(path, Color.red, new BasicStroke(lineWidth));
	}

	void setup(Rectangle b, boolean isLine) {
		xcenter = b.x + b.width/2;
		ycenter = b.y + b.height/2;
		float size = Math.min(b.width, b.height);
		if (isLine)
			size = (float)Math.sqrt(b.width*b.width+b.height*b.height);
		innerR = size/20f;
		outerR = size/2f;
	}

	boolean showDialog() {
		GenericDialog gd = new GenericDialog("Circles");
		gd.addNumericField("Circles:", n, 0);
		gd.addNumericField("Line Width:", lineWidth, 1);
		gd.addNumericField("X Center:", xcenter, 1);
		gd.addNumericField("Y Center:", ycenter, 1);
		gd.addNumericField("Inner Radius:", innerR, 1);
		gd.addNumericField("Outer Radius:", outerR, 1);
		gd.addCheckbox("Hide", false);
		gd.showDialog();
		if (gd.wasCanceled()) return false;
		n = (int)gd.getNextNumber();
		lineWidth = (float)gd.getNextNumber();
		xcenter = (float)gd.getNextNumber();
		ycenter = (float)gd.getNextNumber();
		innerR = (float)gd.getNextNumber();
		outerR = (float)gd.getNextNumber();
		hide = gd.getNextBoolean();
		return true;
	}

}

