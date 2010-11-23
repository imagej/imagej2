package ijx.roi;
import ijx.process.ImageProcessor;
import ijx.process.ByteProcessor;
import ijx.measure.Calibration;
import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.awt.event.KeyEvent;
import java.util.*;
import ij.*;
import ijx.gui.IjxToolbar;


import ijx.plugin.frame.Recorder;
import ijx.plugin.filter.Analyzer;
import ijx.util.Tools;
import ijx.CentralLookup;

/**A subclass of <code>ij.gui.Roi</code> (2D Regions Of Interest) implemented in terms of java.awt.Shape.
 * A ShapeRoi is constructed from a <code>ij.gui.Roi</code> object, or as a result of logical operators
 * (i.e., union, intersection, exclusive or, and subtraction) provided by this class. These operators use the package
 * <code>java.awt.geom</code> as a backend. <br>
 * This code is in the public domain.
 * @author Cezar M.Tigaret <c.tigaret@ucl.ac.uk>
 */
public class ShapeRoi extends Roi {

	/***/
	static final int NO_TYPE = 128;
	
	/**The maximum tolerance allowed in calculating the length of the curve segments of this ROI's shape.*/
	static final double MAXERROR = 1.0e-3;
	
	/**Coefficient used to obtain a flattened version of this ROI's shape. A flattened shape is the
	* closest approximation of the original shape's curve segments with line segments.*/
	static final double FLATNESS = 0.1;
	
	/**Parsing a shape composed of linear segments less than this value will result in Roi objects of type
	 * {@link ij.gui.Roi#POLYLINE} and {@link ij.gui.Roi#POLYGON} for open and closed shapes, respectively.
	 * Conversion of shapes open and closed with more than MAXPOLY line segments will result,
	 * respectively, in {@link ij.gui.Roi#FREELINE} and {@link ij.gui.Roi#FREEROI} (or
	 * {@link ij.gui.Roi#TRACED_ROI} if {@link #forceTrace} flag is <strong><code>true</code></strong>.
	 */
	private static final int MAXPOLY = 10; // I hate arbitrary values !!!!

    private static final int OR=0, AND=1, XOR=2, NOT=3;
    
    private static final double SHAPE_TO_ROI=-1.0;

	/**The <code>java.awt.Shape</code> encapsulated by this object.*/
	private Shape shape;
	
	/**The instance value of the maximum tolerance (MAXERROR) allowed in calculating the 
	 * length of the curve segments of this ROI's shape.
	 */
	private double maxerror = ShapeRoi.MAXERROR;
	
	/**The instance value of the coefficient (FLATNESS) used to 
	 * obtain a flattened version of this ROI's shape.
	 */
	private double flatness = ShapeRoi.FLATNESS;
	
	/**The instance value of MAXPOLY.*/
	private int maxPoly = ShapeRoi.MAXPOLY;
    
	/**If <strong></code>true</code></strong> then methods that manipulate this ROI's shape will work on
	 * a flattened version of the shape. */
	private boolean flatten;
	
	/**Flag which specifies how Roi objects will be constructed from closed (sub)paths having more than
	 * <code>MAXPOLY</code> and composed exclusively of line segments.
	 * If <strong><code>true</code></strong> then (sub)path will be parsed into a
	 * {@link ij.gui.Roi#TRACED_ROI}; else, into a {@link ij.gui.Roi#FREEROI}. */
	private boolean forceTrace = false;

	/**Flag which specifies if Roi objects constructed from open (sub)paths composed of only two line segments
	 * will be of type {@link ij.gui.Roi#ANGLE}.
	 * If <strong><code>true</code></strong> then (sub)path will be parsed into a {@link ij.gui.Roi#ANGLE};
	 * else, into a {@link ij.gui.Roi#POLYLINE}. */
	private boolean forceAngle = false;
	
	private Vector savedRois;
	private static Stroke defaultStroke = new BasicStroke();


	/** Constructs a ShapeRoi from an Roi. */
	public ShapeRoi(Roi r) {
		this(r, ShapeRoi.FLATNESS, ShapeRoi.MAXERROR, false, false, false, ShapeRoi.MAXPOLY);
	}

	/** Constructs a ShapeRoi from a Shape. */
	public ShapeRoi(Shape s) {
		super(s.getBounds());
		AffineTransform at = new AffineTransform();
		at.translate(-x, -y);
		shape = new GeneralPath(at.createTransformedShape(s));
		type = COMPOSITE;
	}

	/** Constructs a ShapeRoi from a Shape. */
	public ShapeRoi(int x, int y, Shape s) {
		super(x, y, s.getBounds().width, s.getBounds().height);
		shape = new GeneralPath(s);
		type = COMPOSITE;
	}

	/**Creates a ShapeRoi object from a "classical" ImageJ ROI.
	 * @param r An ij.gui.Roi object
	 * @param flatness The flatness factor used in convertion of curve segments into line segments.
	 * @param maxerror Error correction for calculating length of Bezeir curves.
	 * @param forceAngle flag used in the conversion of Shape objects to Roi objects (see {@link #shapeToRois()}.
	 * @param forceTrace flag for conversion of Shape objects to Roi objects (see {@link #shapeToRois()}.
	 * @param flatten if <strong><code>true</code></strong> then the shape of this ROI will be flattened
	 * (i.e., curve segments will be aproximated by line segments).
	 * @param maxPoly Roi objects constructed from shapes composed of linear segments fewer than this
	 * value will be of type {@link ij.gui.Roi#POLYLINE} or {@link ij.gui.Roi#POLYGON}; conversion of
	 * shapes with linear segments more than this value will result in Roi objects of type
	 * {@link ij.gui.Roi#FREELINE} or {@link ij.gui.Roi#FREEROI} (see {@link #shapeToRois()}).
	 */
	ShapeRoi(Roi r, double flatness, double maxerror, boolean forceAngle, boolean forceTrace, boolean flatten, int maxPoly) {
		super(r.startX, r.startY, r.width, r.height);
		this.type = COMPOSITE;
		this.flatness = flatness;
		this.maxerror = maxerror;
		this.forceAngle = forceAngle;
		this.forceTrace = forceTrace;
		this.maxPoly= maxPoly;
		this.flatten = flatten;
		shape = roiToShape((Roi)r.clone());
	}

	/** Constructs a ShapeRoi from an array of variable length path segments. Each
		segment consists of the segment type followed by 0-3 end points and control
		points. Depending on the type, a segment uses from 1 to 7 elements of the array. */
	public ShapeRoi(float[] shapeArray) {
		super(0,0,null);
		shape = makeShapeFromArray(shapeArray);
		Rectangle r = shape.getBounds();
		x = r.x;
		y = r.y;
		width = r.width;
		height = r.height;
		
		setState(NORMAL);
		oldX=x; oldY=y; oldWidth=width; oldHeight=height;
				
		AffineTransform at = new AffineTransform();
		at.translate(-x, -y);
		shape = new GeneralPath(at.createTransformedShape(shape));
		flatness = ShapeRoi.FLATNESS;
		maxerror = ShapeRoi.MAXERROR;
		maxPoly = ShapeRoi.MAXPOLY;
		flatten = false;
		type = COMPOSITE;
	}
	
	/**Returns a deep copy of this. */
	public synchronized Object clone() { // the equivalent of "operator=" ?
		ShapeRoi sr = (ShapeRoi)super.clone();
		sr.type = COMPOSITE;
		sr.flatness = flatness;
		sr.maxerror = maxerror;
		sr.forceAngle = forceAngle;
		sr.forceTrace = forceTrace;
		//sr.setImage(imp); //wsr
		sr.setShape(ShapeRoi.cloneShape(shape));
		return sr;
	}
	
	/**Returns a deep copy of the argument. */
	static Shape cloneShape(Shape rhs) {
		if(rhs==null) return null;
		if(rhs instanceof Rectangle2D.Double) { return (Rectangle2D.Double)((Rectangle2D.Double)rhs).clone(); }
		else if(rhs instanceof Ellipse2D.Double) { return (Ellipse2D.Double)((Ellipse2D.Double)rhs).clone(); }
		else if(rhs instanceof Line2D.Double) { return (Line2D.Double)((Line2D.Double)rhs).clone(); }
		else if(rhs instanceof Polygon) { return new Polygon(((Polygon)rhs).xpoints, ((Polygon)rhs).ypoints, ((Polygon)rhs).npoints); }
		else if(rhs instanceof GeneralPath) { return (GeneralPath)((GeneralPath)rhs).clone(); }
		return new GeneralPath(); // dodgy !!!
	}

	/**********************************************************************************/
	/***                  Logical operations on shaped rois                        ****/
	/**********************************************************************************/

	/**Unary union operator.
	 * The caller is set to its union with the argument.
	 * @return the union of <strong><code>this</code></strong> and <code>sr</code>
	 */
	public ShapeRoi or(ShapeRoi sr) {return unaryOp(sr, OR);}

	/**Unary intersection operator.
	 * The caller is set to its intersection with the argument (i.e., the overlapping regions between the
	 * operands).
	 * @return the overlapping regions between <strong><code>this</code></strong> and <code>sr</code>
	 */
	public ShapeRoi and(ShapeRoi sr) {return unaryOp(sr, AND);}

	/**Unary exclusive or operator.
	 * The caller is set to the non-overlapping regions between the operands.
	 * @return the union of the non-overlapping regions of <strong><code>this</code></strong> and <code>sr</code>
	 */
	public ShapeRoi xor(ShapeRoi sr) {return unaryOp(sr, XOR);}

	/**Unary subtraction operator.
	 * The caller is set to the result of the operation between the operands.
	 * @return <strong><code>this</code></strong> subtracted from <code>sr</code>
	 */
	public ShapeRoi not(ShapeRoi sr) {return unaryOp(sr, NOT);}

	ShapeRoi unaryOp(ShapeRoi sr, int op) {
		AffineTransform at = new AffineTransform();
		at.translate(x, y);
		Area a1 = new Area(at.createTransformedShape(getShape()));
		at = new AffineTransform();
		at.translate(sr.x, sr.y);
		Area a2 = new Area(at.createTransformedShape(sr.getShape()));
		switch (op) {
			case OR: a1.add(a2); break;
			case AND: a1.intersect(a2); break;
			case XOR: a1.exclusiveOr(a2); break;
			case NOT: a1.subtract(a2); break;
		}
		Rectangle r = a1.getBounds();
		at = new AffineTransform();
		at.translate(-r.x, -r.y);
		setShape(new GeneralPath(at.createTransformedShape(a1)));
		x = r.x;
		y = r.y;
		return this;
	}

	/**********************************************************************************/
	/***         Interconversions between "regular" rois and shaped rois           ****/
	/**********************************************************************************/

	/**Converts the Roi argument to an instance of java.awt.Shape.
	 * Currently, the following conversions are supported:<br>
		<table><col><col><col><col><col><col><col>
			<thead>
				<tr><th scope=col> Roi class </th><th scope=col> Roi type </th><th scope=col> Shape </th><th scope=col> Winding<br> rule </th><th scope=col> Flag<br> forceAngle </th><th scope=col> Flag<br> forceTrace </th><th scope=col> Flag<br> complexShape </th></tr>
			</thead>
			<tbody>
				<tr><td> ij.gui.Roi </td><td> Roi.RECTANGLE </td><td> java.awt.geom.Rectangle2D.Double </td><td></td><td> false </td><td> false </td><td> false </td>	</tr>
				<tr><td> ij.gui.OvalRoi </td><td> Roi.OVAL </td><td> java.awt.geom.Ellipse2D.Double </td><td></td>	<td> false </td><td> false </td><td> false </td></tr>
				<tr><td> ij.gui.Line </td><td> Roi.LINE </td><td> java.awt.geom.Line2D.Double </td><td></td><td> false </td><td> false </td><td> false </td></tr>
				<tr>	<td> ij.gui.PolygonRoi </td>	<td> Roi.POLYGON </td>	<td> java.awt.Polygon </td>	<td></td><td> false </td>	<td> false </td><td> false </td></tr>
				<tr><td> ij.gui.PolygonRoi </td>	<td> Roi.FREEROI </td>	<td> closed java.awt.geom.GeneralPath </td>	<td> GeneralPath.WIND_EVEN_ODD </td><td> false </td><td> false </td>	<td> false </td>	</tr>
				<tr><td> ij.gui.PolygonRoi </td><td> Roi.TRACED_ROI </td><td> closed java.awt.geom.GeneralPath  </td>	<td> GeneralPath.WIND_EVEN_ODD </td>	<td> false </td>	<td> true </td>	<td> false </td></tr>
				<tr><td> ij.gui.PolygonRoi </td>	<td> Roi.POLYLINE </td>	<td> open java.awt.geom.GeneralPath  </td>	<td> GeneralPath.WIND_NON_ZERO </td>	<td> false </td>	<td> false </td><td> false </td></tr>
				<tr><td> ij.gui.PolygonRoi </td>	<td> Roi.FREELINE </td>	<td> open java.awt.geom.GeneralPath  </td>	<td> GeneralPath.WIND_NON_ZERO </td>	<td> false </td>	<td> false </td>	<td> false </td>	</tr>
				<tr>	<td> ij.gui.PolygonRoi </td>	<td> Roi.ANGLE </td>	<td> open java.awt.geom.GeneralPath  </td>	<td> GeneralPath.WIND_NON_ZERO </td>	<td> true </td>	<td> false </td>	<td> false </td>	</tr>
				<tr>	<td> ij.gui.ShapeRoi </td>	<td> Roi.COMPOSITE </td>	<td> shape of argument  </td>	<td> winding rule of<br> argument </td><td> flag of<br> argument </td>	<td> flag of<br> argument </td>	<td> flag of<br> argument </td>	</tr>
				<tr><td> ij.gui.ShapeRoi </td>	<td> ShapeRoi.NO_TYPE </td>	<td> null </td>	<td>  </td>	<td> false </td>	<td> false </td>	<td> false </td>	</tr>
			</tbody>
		</table>
	 *
	 * @return A java.awt.geom.* object that inherits from java.awt.Shape interface.
	 *
	 */
	private Shape roiToShape(Roi roi) {
		Shape shape = null;
		Rectangle r = roi.getBounds();
		int[] xCoords = null;
		int[] yCoords = null;
		int nCoords = 0;
		switch(roi.getType()) {
			case Roi.LINE:
				Line line = (Line)roi;				
				shape = new Line2D.Double ((double)(line.x1-r.x), (double)(line.y1-r.y), (double)(line.x2-r.x), (double)(line.y2-r.y) );
				break;
			case Roi.RECTANGLE:
				int arcSize = roi.getRoundRectArcSize();
				if (arcSize>0)
					shape = new RoundRectangle2D.Float(0, 0, r.width, r.height, arcSize, arcSize);
				else
					shape = new Rectangle2D.Double(0.0, 0.0, (double)r.width, (double)r.height);
				break;
			case Roi.OVAL:
				Polygon p = roi.getPolygon();
				for (int i=0; i<p.npoints; i++) {
					p.xpoints[i] -= r.x;
					p.ypoints[i] -= r.y;
				}
				shape = new Polygon(p.xpoints, p.ypoints, p.npoints);
				break;
			case Roi.POLYGON:
				nCoords =((PolygonRoi)roi).getNCoordinates();
				xCoords = ((PolygonRoi)roi).getXCoordinates();
				yCoords = ((PolygonRoi)roi).getYCoordinates();
				shape = new Polygon(xCoords,yCoords,nCoords);
				break;
			case Roi.FREEROI: case Roi.TRACED_ROI:
				nCoords =((PolygonRoi)roi).getNCoordinates();
				xCoords = ((PolygonRoi)roi).getXCoordinates();
				yCoords = ((PolygonRoi)roi).getYCoordinates();
				shape = new GeneralPath(GeneralPath.WIND_EVEN_ODD,nCoords);
				((GeneralPath)shape).moveTo((float)xCoords[0], (float)yCoords[0]);
				for (int i=1; i<nCoords; i++)
					((GeneralPath)shape).lineTo((float)xCoords[i],(float)yCoords[i]);
				((GeneralPath)shape).closePath();
				break;
			case Roi.POLYLINE: case Roi.FREELINE: case Roi.ANGLE:
				nCoords =((PolygonRoi)roi).getNCoordinates();
				xCoords = ((PolygonRoi)roi).getXCoordinates();
				yCoords = ((PolygonRoi)roi).getYCoordinates();
				shape = new GeneralPath(GeneralPath.WIND_NON_ZERO,nCoords);
				((GeneralPath)shape).moveTo((float)xCoords[0], (float)yCoords[0]);
				for (int i=1; i<nCoords; i++)
					((GeneralPath)shape).lineTo((float)xCoords[i],(float)yCoords[i]);
				break;
			case Roi.POINT:
				ImageProcessor mask = roi.getMask();
				byte[] maskPixels = (byte[])mask.getPixels();
				Rectangle maskBounds = roi.getBounds();
				int maskWidth = mask.getWidth();
				Area area = new Area();
				for (int y=0; y<mask.getHeight(); y++) {
					int yOffset = y*maskWidth;
					for (int x=0; x<maskWidth; x++) {
						if (maskPixels[x+yOffset]!=0)
							area.add(new Area(new Rectangle(x+maskBounds.x, y+maskBounds.y, 1, 1)));
					}
				}
				shape = area;
				break;
			case Roi.COMPOSITE: shape = ShapeRoi.cloneShape(((ShapeRoi)roi).getShape());
				break;
			default:
				throw new IllegalArgumentException("Roi type not supported");
		}

		if(shape!=null) {
			this.x = roi.x;
			this.y = roi.y;
            Rectangle bounds = shape.getBounds();
			this.width = bounds.width;
			this.height = bounds.height;
			this.startX = x;
			this.startY = y;
			//IJ.log("RoiToShape: "+x+" "+y+" "+width+" "+height+" "+bounds);
		}
		return shape;
	}

	/**Constructs a Shape from a float array. */
	Shape makeShapeFromArray(float[] array) {
		if(array==null) return null;
		Shape s = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
		int index=0, type, len;
		float[] seg = new float[7];
		while (true) {
			len = getSegment(array, seg, index);
			if (len<0) break;
			index += len;
			type = (int)seg[0];
			switch(type) {
				case PathIterator.SEG_MOVETO:
					((GeneralPath)s).moveTo(seg[1], seg[2]);
					break;
				case PathIterator.SEG_LINETO:
					((GeneralPath)s).lineTo(seg[1], seg[2]);
					break;
				case PathIterator.SEG_QUADTO:
					((GeneralPath)s).quadTo(seg[1], seg[2],seg[3], seg[4]);
					break;
				case PathIterator.SEG_CUBICTO:
					((GeneralPath)s).curveTo(seg[1], seg[2], seg[3], seg[4], seg[5], seg[6]);
					break;
				case PathIterator.SEG_CLOSE:
					((GeneralPath)s).closePath();
					break;
				default: break;
			}
		}
		return s;
	}
	
	private int getSegment(float[] array, float[] seg, int index) {
		int len = array.length;
		if (index>=len) return -1; seg[0]=array[index++];
		int type = (int)seg[0];
		if (type==PathIterator.SEG_CLOSE) return 1;
		if (index>=len) return -1; seg[1]=array[index++];
		if (index>=len) return -1; seg[2]=array[index++];
		if (type==PathIterator.SEG_MOVETO||type==PathIterator.SEG_LINETO) return 3;
		if (index>=len) return -1; seg[3]=array[index++];
		if (index>=len) return -1; seg[4]=array[index++];
		if (type==PathIterator.SEG_QUADTO) return 5;
		if (index>=len) return -1; seg[5]=array[index++];
		if (index>=len) return -1; seg[6]=array[index++];
		if (type==PathIterator.SEG_CUBICTO) return 7;
		return -1;
	}

	/** Saves an Roi so it can be retrieved later using getRois(). */
	void saveRoi(Roi roi) {
		if (savedRois==null)
			savedRois = new Vector();
		savedRois.addElement(roi);
	}

	/**Converts a Shape into Roi object(s).
	 * <br>This method parses the shape into (possibly more than one) Roi objects 
	 * and returns them in an array.
	 * <br>A simple, &quot;regular&quot; path results in a single Roi following these simple rules:
		<table><col><col><col>
			<thead><tr><th scope=col> Shape type </th><th scope=col> Roi class </th><th scope=col> Roi type </th></tr></thead>
			<tbody>
				<tr><td> java.awt.geom.Rectangle2D.Double </td><td> ij.gui.Roi </td><td> Roi.RECTANGLE </td></tr>
				<tr><td> java.awt.geom.Ellipse2D.Double </td><td> ij.gui.OvalRoi</td><td> Roi.OVAL </td></tr>
				<tr><td> java.awt.geom.Line2D.Double </td><td> ij.gui.Line </td><td> Roi.LINE </td></tr>
				<tr><td> java.awt.Polygon </td>	<td> ij.gui.PolygonRoi </td><td> Roi.POLYGON </td></tr>
			</tbody>
		</table>
	 * <br><br>Each subpath of a <code>java.awt.geom.GeneralPath</code> is converted following these rules:
		<table frame="border"><col><col><col><col><col><col>
			<thead>
				<tr><th rowspan="2" scope=col> Segment<br> types </th><th rowspan="2" scope=col> Number of<br> segments </th>
					<th rowspan="2" scope=col> Closed<br> path </th><th rowspan="2" scope=col> Value of<br> forceAngle </th>
					<th rowspan="2" scope=col> Value of<br> forceTrace </th><th rowspan="2" scope=col> Roi type </th></tr>
			</thead>
			<tbody>
				<tr><td> lines only: </td><td align="center"> 0 </td><td>  </td><td>  </td><td>  </td><td> ShapeRoi.NO_TYPE </td></tr>
				<tr><td>  </td><td align="center"> 1 </td><td>  </td><td>  </td><td>  </td>	<td> ShapeRoi.NO_TYPE </td></tr>
				<tr><td>  </td><td align="center"> 2 </td><td align="center"> Y </td><td>  </td><td>  </td><td> ShapeRoi.NO_TYPE </td></tr>
				<tr><td>  </td><td>  </td><td align="center"> N </td><td>  </td><td>  </td><td> Roi.LINE </td></tr>
				<tr><td>  </td><td align="center"> 3 </td><td align="center"> Y </td><td align="center"> N </td><td>  </td><td> Roi.POLYGON </td></tr>
				<tr><td>  </td><td>  </td><td align="center"> N </td><td align="center"> Y </td><td>  </td><td> Roi.ANGLE </td></tr>
				<tr><td>  </td><td>  </td><td align="center"> N </td><td align="center"> N </td><td>  </td><td> Roi.POLYLINE </td></tr>
				<tr><td>  </td><td align="center"> 4 </td><td align="center"> Y </td><td>  </td<td>  </td><td> Roi.RECTANGLE </td></tr>
				<tr><td>  </td><td>  </td><td align="center"> N </td><td>  </td><td>  </td><td> Roi.POLYLINE </td></tr>
				<tr><td>  </td><td align="center"> &lt;= MAXPOLY </td>	<td align="center"> Y </td><td>  </td><td>  </td><td> Roi.POLYGON </td></tr>
				<tr><td>  </td><td>  </td><td align="center"> N </td><td>  </td><td>  </td><td> Roi.POLYLINE </td></tr>
				<tr><td>  </td><td align="center"> &gt; MAXPOLY </td><td align="center"> Y </td><td>  </td>	<td align="center"> Y </td><td> Roi.TRACED_ROI </td></tr>
				<tr><td>  </td><td>  </td><td>  </td><td>  </td><td align="center"> N </td><td> Roi.FREEROI </td></tr>
				<tr><td>  </td><td>  </td><td align="center"> N </td><td>  </td><td>  </td><td> Roi.FREELINE </td></tr>
				<tr><td> anything<br>else: </td><td align="center"> &lt;= 2 </td><td>  </td><td>  </td><td>  </td><td> ShapeRoi.NO_TYPE </td></tr>
				<tr><td>  </td><td align="center"> &gt; 2 </td><td>  </td><td>  </td><td>  </td><td> ShapeRoi.SHAPE_ROI </td></tr>
			</tbody>
		</table>
	 * @return an array of ij.gui.Roi objects.
	 */
	public Roi[] getRois () {
		if(shape==null) return new Roi[0];
		if (savedRois!=null)
			return getSavedRois();
		Vector rois = new Vector();
		if (shape instanceof Rectangle2D.Double) {
			Roi r = new Roi((int)((Rectangle2D.Double)shape).getX(), (int)((Rectangle2D.Double)shape).getY(), (int)((Rectangle2D.Double)shape).getWidth(), (int)((Rectangle2D.Double)shape).getHeight());
			rois.addElement(r);
		} else if (shape instanceof Ellipse2D.Double) {
			Roi r = new OvalRoi((int)((Ellipse2D.Double)shape).getX(), (int)((Ellipse2D.Double)shape).getY(), (int)((Ellipse2D.Double)shape).getWidth(), (int)((Ellipse2D.Double)shape).getHeight());
			rois.addElement(r);
		} else if (shape instanceof Line2D.Double) {
			Roi r = new ijx.roi.Line((int)((Line2D.Double)shape).getX1(), (int)((Line2D.Double)shape).getY1(), (int)((Line2D.Double)shape).getX2(), (int)((Line2D.Double)shape).getY2());
			rois.addElement(r);
		} else if (shape instanceof Polygon) {
			Roi r = new PolygonRoi(((Polygon)shape).xpoints, ((Polygon)shape).ypoints, ((Polygon)shape).npoints, Roi.POLYGON);
			rois.addElement(r);
		} else if (shape instanceof GeneralPath) {
			PathIterator pIter;
			if (flatten) pIter = getFlatteningPathIterator(shape,flatness);
			else pIter = shape.getPathIterator(new AffineTransform());
			parsePath(pIter, null, null, rois, null);
		}
		Roi[] array = new Roi[rois.size()];
		rois.copyInto((Roi[])array);
		return array;
	}
	
	Roi[] getSavedRois () {
		Roi[] array = new Roi[savedRois.size()];
		savedRois.copyInto((Roi[])array);
		return array;
	}

	/**Attempts to convert this ShapeRoi into a non-composite Roi.
	 * @return an ij.gui.Roi object or null
	 */
	public Roi shapeToRoi() {
		if(shape==null || !(shape instanceof GeneralPath))
			return null;
		PathIterator pIter = shape.getPathIterator(new AffineTransform());
		Vector rois = new Vector();
		double[] params = {SHAPE_TO_ROI};
		if (!parsePath(pIter, params, null, rois, null))
			return null;
		if (rois.size()==1)
			return (Roi)rois.elementAt(0);
		else
			return null;
	}
	
		/**Implements the rules of conversion from <code>java.awt.geom.GeneralPath</code> to <code>ij.gui.Roi</code>.
	 * @param segments The number of segments that compose the path
	 * @param linesOnly Indicates wether the GeneralPath object is composed only of SEG_LINETO segments
	 * @param curvesOnly Indicates wether the GeneralPath object is composed only of SEG_CUBICTO and SEG_QUADTO segments
	 * @param closed Indicates a closed GeneralPath
	 * @see #shapeToRois()
	 * @return a type flag
	 */
	private int guessType(int segments, boolean linesOnly, boolean curvesOnly, boolean closed) {
		//IJ.log("guessType: "+segments+" "+linesOnly+" "+curvesOnly+" "+closed);
		closed = true; // lines currently not supported
		int roiType = Roi.RECTANGLE;
		if (linesOnly) {
			switch(segments) {
				case 0: roiType = NO_TYPE; break;
				case 1: roiType = NO_TYPE; break;
				case 2: roiType = (closed ? NO_TYPE : Roi.LINE); break;
				case 3: roiType = (closed ? Roi.POLYGON : (forceAngle ? Roi.ANGLE: Roi.POLYLINE)); break;
				case 4: roiType = (closed ? Roi.RECTANGLE : Roi.POLYLINE); break;
				default:
					if (segments <= MAXPOLY) 
						roiType = closed ? Roi.POLYGON : Roi.POLYLINE;
					else 
						roiType = closed ? (forceTrace ? Roi.TRACED_ROI: Roi.FREEROI): Roi.FREELINE;
					break;
			}
		}
		else roiType = segments >=2 ? Roi.COMPOSITE : NO_TYPE;
		return roiType;
	}

	/**Creates a Roi object based on the arguments.
	 * @see #shapeToRois()
	 * @param xCoords the x coordinates
	 * @param yCoords the y coordinates
	 * @param type the type flag
	 * @return a ij.gui.Roi object
	 */
	private Roi createRoi(Vector xCoords, Vector yCoords, int roiType) {
		if (roiType==NO_TYPE) return null;
		Roi roi = null;
		if(xCoords.size() != yCoords.size() || xCoords.size()==0) { return null; }

		int[] xPoints = new int[xCoords.size()];
		int[] yPoints = new int[yCoords.size()];

		for (int i=0; i<xPoints.length; i++) {
			xPoints[i] = ((Integer)xCoords.elementAt(i)).intValue() + x;
			yPoints[i] = ((Integer)yCoords.elementAt(i)).intValue() + y;
		}

		int startX = 0;
		int startY = 0;
		int width = 0;
		int height = 0;
		switch(roiType) {
			//case NO_TYPE: roi = this; break;
			case Roi.COMPOSITE: roi = this; break; // hmmm.....!!!???
			case Roi.OVAL:
				startX = xPoints[xPoints.length-4];
				startY = yPoints[yPoints.length-3];
				width = max(xPoints)-min(xPoints);
				height = max(yPoints)-min(yPoints);
				roi = new OvalRoi(startX, startY, width, height);
				break;
			case Roi.RECTANGLE:
				startX = xPoints[0];
				startY = yPoints[0];
				width = max(xPoints)-min(xPoints);
				height = max(yPoints)-min(yPoints);
				roi = new Roi(startX, startY, width, height);
				break;
			case Roi.LINE: roi = new ijx.roi.Line(xPoints[0],yPoints[0],xPoints[1],yPoints[1]); break;
			default:
				int n = xPoints.length;
				roi = new PolygonRoi(xPoints, yPoints, n, roiType);
				if (roiType==FREEROI) {
					double length = roi.getLength();
					double mag = ic!=null?ic.getMagnification():1.0;
					length *= mag;
					//IJ.log("createRoi: "+length/n+" "+mag);
					if (length/n>=15.0) {
						roi = new PolygonRoi(xPoints, yPoints, n, POLYGON);
					}
				}
				break;
		}
		//if(roi!=null && imp!=null) roi.setImage(imp);
		return roi;
	}

	/**********************************************************************************/
	/***                                   Geometry                                ****/
	/**********************************************************************************/

	/**Checks whether the specified coordinates are inside a on this ROI's shape boundaries.*/
	public boolean contains(int x, int y) {
		if(shape==null) return false;
		return shape.contains(x-this.x, y-this.y);
	}

	/** Caculates "Feret" (maximum caliper width) and "MinFeret" (minimum caliper width). */	
	public double[] getFeretValues() {
		double min=Double.MAX_VALUE, diameter=0.0, angle=0.0;
		int p1=0, p2=0;
		double pw=1.0, ph=1.0;
		if (imp!=null) {
			Calibration cal = imp.getCalibration();
			pw = cal.pixelWidth;
			ph = cal.pixelHeight;
		}
		Shape shape = getShape();
		Shape s = null;
		Rectangle2D r = shape.getBounds2D();
		double cx = r.getX() + r.getWidth()/2;
		double cy = r.getY() + r.getHeight()/2;
		AffineTransform at = new AffineTransform();
		at.translate(cx, cy);
		for (int i=0; i<181; i++) {
			at.rotate(Math.PI/180.0);
			s = at.createTransformedShape(shape);
			r = s.getBounds2D();
			double max2 = Math.max(r.getWidth(), r.getHeight());
			if (max2>diameter) {
				diameter = max2*pw;
				//angle = i;
			}
			double min2 = Math.min(r.getWidth(), r.getHeight());
			min = Math.min(min, min2);
		}
		if (pw!=ph) {
			diameter = 0.0;
			angle = 0.0;
		}
		if (pw==ph)
			min *= pw;
		else {
			min = 0.0;
			angle = 0.0;
		}
		double[] a = new double[5];
		a[0] = diameter;
		a[1] = angle;
		a[2] = min;
		a[3] = 0.0; // FeretX
		a[4] = 0.0; // FeretY
		return a;
	}

	/**Returns the perimeter if this ShapeRoi can be decomposed 
		into simple ROIs, otherwise returns zero. */
	public double getLength() {
		double length = 0.0;
		Roi[] rois = getRois();
		if (rois!=null) {
			for (int i=0; i<rois.length; i++)
				length += rois[i].getLength();
		}
		return length;
		/*
		if(shape==null) return 0.0;
		Rectangle2D r2d = shape.getBounds2D();
		double w = r2d.getWidth();
		double h = r2d.getHeight();
		if(w==0 && h==0) return 0.0;
		PathIterator pIter;
		flatten = true;
		if(flatten) pIter = getFlatteningPathIterator(shape, flatness);
		else pIter = shape.getPathIterator(new AffineTransform());
		double[] par = new double[1];
		parsePath(pIter, par, null, null, null);
		flatten = false;
		return par[0];
		*/
	}

	/**Returns a flattened version of the path iterator for this ROi's shape*/
	FlatteningPathIterator getFlatteningPathIterator(Shape s, double fl) {
		return (FlatteningPathIterator)s.getPathIterator(new AffineTransform(),fl);
	}

	/**Length of the control polygon of the cubic B&eacute;zier curve argument, in double precision.*/
	double cplength(CubicCurve2D.Double c) {
		double result = Math.sqrt(Math.pow((c.ctrlx1-c.x1),2.0)+Math.pow((c.ctrly1-c.y1),2.0));
		result += Math.sqrt(Math.pow((c.ctrlx2-c.ctrlx1),2.0)+Math.pow((c.ctrly2-c.ctrly1),2.0));
		result += Math.sqrt(Math.pow((c.x2-c.ctrlx2),2.0)+Math.pow((c.y2-c.ctrly2),2.0));
		return result;
	}

	/**Length of the control polygon of the quadratic B&eacute;zier curve argument, in double precision.*/
	double qplength(QuadCurve2D.Double c) {
		double result = Math.sqrt(Math.pow((c.ctrlx-c.x1),2.0)+Math.pow((c.ctrly-c.y1),2.0));
		result += Math.sqrt(Math.pow((c.x2-c.ctrlx),2.0)+Math.pow((c.y2-c.ctrly),2.0));
		return result;
	}

	/**Length of the chord of the arc of the cubic B&eacute;zier curve argument, in double precision.*/
	double cclength(CubicCurve2D.Double c)
	{ return Math.sqrt(Math.pow((c.x2-c.x1),2.0) + Math.pow((c.y2-c.y1),2.0)); }

	/**Length of the chord of the arc of the quadratic B&eacute;zier curve argument, in double precision.*/
	double qclength(QuadCurve2D.Double c)
	{ return Math.sqrt(Math.pow((c.x2-c.x1),2.0) + Math.pow((c.y2-c.y1),2.0)); }

	/**Calculates the length of a cubic B&eacute;zier curve specified in double precision.
	 * The algorithm is based on the theory presented in paper <br>
	 * &quot;Jens Gravesen. Adaptive subdivision and the length and energy of B&eacute;zier curves. Computational Geometry <strong>8:</strong><em>13-31</em> (1997)&quot;
	 * implemented using <code>java.awt.geom.CubicCurve2D.Double</code>.
	 * Please visit {@link <a href="http://www.graphicsgems.org/gems.html#gemsiv">Graphics Gems IV</a>} for
	 * examples of other possible implementations in C and C++.
	 */
	double cBezLength(CubicCurve2D.Double c) {
		double l = 0.0;
		double cl = cclength(c);
		double pl = cplength(c);
		if((pl-cl)/2.0 > maxerror)
		{
			CubicCurve2D.Double[] cc = cBezSplit(c);
			for(int i=0; i<2; i++) l+=cBezLength(cc[i]);
			return l;
		}
		l = 0.5*pl+0.5*cl;
		return l;
	}

	/**Calculates the length of a quadratic B&eacute;zier curve specified in double precision.
	 * The algorithm is based on the theory presented in paper <br>
	 * &quot;Jens Gravesen. Adaptive subdivision and the length and energy of B&eacute;zier curves. Computational Geometry <strong>8:</strong><em>13-31</em> (1997)&quot;
	 * implemented using <code>java.awt.geom.CubicCurve2D.Double</code>.
	 * Please visit {@link <a href="http://www.graphicsgems.org/gems.html#gemsiv">Graphics Gems IV</a>} for
	 * examples of other possible implementations in C and C++.
	 */
	double qBezLength(QuadCurve2D.Double c) {
		double l = 0.0;
		double cl = qclength(c);
		double pl = qplength(c);
		if((pl-cl)/2.0 > maxerror)
		{
			QuadCurve2D.Double[] cc = qBezSplit(c);
			for(int i=0; i<2; i++) l+=qBezLength(cc[i]);
			return l;
		}
		l = (2.0*pl+cl)/3.0;
		return l;
	}

 /**Splits a cubic B&eacute;zier curve in half.
  * @param c A cubic B&eacute;zier curve to be divided
  * @return an array with the left and right cubic B&eacute;zier subcurves
  *
	*/
	CubicCurve2D.Double[] cBezSplit(CubicCurve2D.Double c) {
		CubicCurve2D.Double[] cc = new CubicCurve2D.Double[2];
		for (int i=0; i<2 ; i++) cc[i] = new CubicCurve2D.Double();
		c.subdivide(cc[0],cc[1]);
		return cc;
	}

 /**Splits a quadratic B&eacute;zier curve in half.
  * @param c A quadratic B&eacute;zier curve to be divided
  * @return an array with the left and right quadratic B&eacute;zier subcurves
  *
	*/
	QuadCurve2D.Double[] qBezSplit(QuadCurve2D.Double c) {
		QuadCurve2D.Double[] cc = new QuadCurve2D.Double[2];
		for(int i=0; i<2; i++) cc[i] = new QuadCurve2D.Double();
		c.subdivide(cc[0],cc[1]);
		return cc;
	}

	// c is an array of even length with x0, y0, x1, y1, ... ,xn, yn coordinate pairs
	/**Scales a coordinate array with the size calibration of a 2D image.
	 * The array is modified in place.
	 * @param c Array of coordinates in double precision with a <strong>fixed</strong> structure:<br>
	 * <code>x0,y0,x1,y1,....,xn,yn</code> and with even length of <code>2*(n+1)</code>.
	 * @param pw The x-scale of the image.
	 * @param ph The y-scale of the image.
	 *
	 */
	void scaleCoords(double[] c, double pw, double ph) {
		int k = c.length/2;
		if (2*k!=c.length) return; // bail out if array has odd length
		for(int i=0; i<c.length; i+=2)
		{
			c[i]*=pw;
			c[i+1]*=ph;
		}
	}

	Vector parseSegments(PathIterator pI) {
		Vector v = new Vector();
		if(parsePath(pI, null, v, null, null)) return v;
		return null;
	}

	/** Retrieves the end points and control points of the path as a float array. The array 
		contains a sequence of variable length segments that use from from one to seven elements.
		The first element of a segment is the type as defined in the PathIterator interface. SEG_MOVETO 
		and SEG_LINETO segments also include two coordinates, SEG_QUADTO segments include four 
		coordinates and SEG_CUBICTO segments include six coordinates. */
	public float[] getShapeAsArray() {
		if(shape==null) return null;
		//if (savedRois!=null)
		//	return getSavedRoisAsArray();
		PathIterator pIt = shape.getPathIterator(new AffineTransform());
		Vector h = new Vector(); // handles
		Vector s = new Vector(); // segment types
		if (!(parsePath(pIt, null, s, null, h))) return null;
		float[] result = new float[7*s.size()];
		Point2D.Double p;
		int segType;
		int k=0, j=0;
		int index = 0;
		for (int i=0; i<s.size(); i++) {
			segType = ((Integer)s.elementAt(i)).intValue();
			switch(segType) {
				case PathIterator.SEG_MOVETO: case PathIterator.SEG_LINETO:
					result[index++] = segType;
					p = (Point2D.Double)h.elementAt(j++);
					result[index++]=(float)p.getX()+x; result[index++]=(float)p.getY()+y;
					break;
				case PathIterator.SEG_QUADTO:
					result[index++] = segType;
					p = (Point2D.Double)h.elementAt(j++);
					result[index++]=(float)p.getX()+x; result[index++]=(float)p.getY()+y;
					p = (Point2D.Double)h.elementAt(j++);
					result[index++]=(float)p.getX()+x; result[index++]=(float)p.getY()+y;
					break;
				case PathIterator.SEG_CUBICTO:
					result[index++] = segType;
					p = (Point2D.Double)h.elementAt(j++);
					result[index++]=(float)p.getX()+x; result[index++]=(float)p.getY()+y;
					p = (Point2D.Double)h.elementAt(j++);
					result[index++]=(float)p.getX()+x; result[index++]=(float)p.getY()+y;
					p = (Point2D.Double)h.elementAt(j++);
					result[index++]=(float)p.getX()+x; result[index++]=(float)p.getY()+y;
					break;
				case PathIterator.SEG_CLOSE:
					result[index++] = segType;
					break;
				default: break;
			}
		}
		float[] result2 = new float[index];
		System.arraycopy(result, 0, result2, 0, result2.length);
		return result2;
	}

	/*
	float[] getSavedRoisAsArray() {
		int n = savedRois.length;
		Polygon[] polygons = new Polygon[n];
		for (int i=0; i<n; i++) {
			
		}
		float[] result = new float[7*s.size()];
		for (int i=0; i<savedRois.length; i++) {
			switch(segType) {
				case PathIterator.SEG_MOVETO: case PathIterator.SEG_LINETO:
					result[index++] = segType;
					p = (Point2D.Double)h.elementAt(j++);
					result[index++]=(float)p.getX()+x; result[index++]=(float)p.getY()+y;
					break;
				case PathIterator.SEG_QUADTO:
					result[index++] = segType;
					p = (Point2D.Double)h.elementAt(j++);
					result[index++]=(float)p.getX()+x; result[index++]=(float)p.getY()+y;
					p = (Point2D.Double)h.elementAt(j++);
					result[index++]=(float)p.getX()+x; result[index++]=(float)p.getY()+y;
					break;
				case PathIterator.SEG_CUBICTO:
					result[index++] = segType;
					p = (Point2D.Double)h.elementAt(j++);
					result[index++]=(float)p.getX()+x; result[index++]=(float)p.getY()+y;
					p = (Point2D.Double)h.elementAt(j++);
					result[index++]=(float)p.getX()+x; result[index++]=(float)p.getY()+y;
					p = (Point2D.Double)h.elementAt(j++);
					result[index++]=(float)p.getX()+x; result[index++]=(float)p.getY()+y;
					break;
				case PathIterator.SEG_CLOSE:
					result[index++] = segType;
					break;
				default: break;
			}
		}
		float[] result2 = new float[index];
		System.arraycopy(result, 0, result2, 0, result2.length);
		return result2;
	}
	*/

	/**Parses the geometry of this ROI's shape by means of the shape's PathIterator 
	 * and returns several convenience parameters in the arguments.
	 * Iterates through the PathIterator argument and as a byproduct sets the values of
	 * the other arguments passed to the method. To retrieve, for example, the length 
	 * (or perimeter) of a shape (or path), call this method as<br>
	 * <code> parsePath(pIter, par, null, null) <code>,
	 * <br> where <code>par</code> is a double array of length one, then get the length as <code>par[0]</code>
	 * @param pIter the PathIterator to be parsed.
	 * @param params an array with one elemet that will hold the calculated length of path;
	 * @param segments a Vector of Integer objects that will hold the types of the segments composing the shape's path
	 * @param rois a Vector that will hold ij.gui.Roi objects constructed from elements of this path
	 * (see @link #shapeToRois()} for details;
	 * @param handles a Vector of Point2D.Double objects representing vertices (segment joinings) and 
	 * control points of the curves segments in the iteration order;
	 * @return <strong><code>true</code></strong> if successful.*/
	boolean parsePath(PathIterator pIter, double[] params, Vector segments, Vector rois, Vector handles) {
		//long start = System.currentTimeMillis();
		boolean result = true;
		if(pIter==null) return false;
		double pw = 1.0, ph = 1.0;
		if(imp!=null) {
			Calibration cal = imp.getCalibration();
			pw = cal.pixelWidth;
			ph = cal.pixelHeight;
		}
		Vector xCoords = new Vector();
		Vector yCoords = new Vector();
		if(segments==null) segments = new Vector();
		if(handles==null) handles = new Vector();
		//if(rois==null) rois = new Vector();
		if(params == null) params = new double[1];
		boolean shapeToRoi = params[0]==SHAPE_TO_ROI;
		int subPaths = 0; // the number of subpaths
		int count = 0;// the number of segments in each subpath w/o SEG_CLOSE; resets to one after each SEG_MOVETO
		int roiType = Roi.RECTANGLE;
		int segType;
		boolean closed = false;
		boolean linesOnly = true;
		boolean curvesOnly = true;
		//boolean success = false;
		double[] coords; // scaled coordinates of the path segment
		double[] ucoords; // unscaled coordinates of the path segment
		double sX = Double.NaN; // start x of subpath (scaled)
		double sY = Double.NaN; // start y of subpath (scaled)
		double x0 = Double.NaN; // last x in the subpath (scaled)
		double y0 = Double.NaN; // last y in the subpath (scaled)
		double usX = Double.NaN;// unscaled versions of the above
		double usY = Double.NaN;
		double ux0 = Double.NaN;
		double uy0 = Double.NaN;
		double pathLength = 0.0;
		Shape curve; // temporary reference to a curve segment of the path
		boolean done = false;
		while (!done) {
			coords = new double[6];
			ucoords = new double[6];
			segType = pIter.currentSegment(coords);
			segments.add(new Integer(segType));
			count++;
			System.arraycopy(coords,0,ucoords,0,coords.length);
			scaleCoords(coords,pw,ph);
			switch(segType) {
				case PathIterator.SEG_MOVETO:
					if(subPaths>0) {
						closed = ((int)ux0==(int)usX && (int)uy0==(int)usY);
						if(closed && (int)ux0!=(int)usX && (int)uy0!=(int)usY) { // this may only happen after a SEG_CLOSE
							xCoords.add(new Integer(((Integer)xCoords.elementAt(0)).intValue()));
							yCoords.add(new Integer(((Integer)yCoords.elementAt(0)).intValue()));
						}
						if (rois!=null) {
							roiType = guessType(count, linesOnly, curvesOnly, closed);
							Roi r = createRoi(xCoords, yCoords, roiType);
							if (r!=null)
								rois.addElement(r);
						}
						xCoords = new Vector();
						yCoords = new Vector();
						count = 1;
					}
					subPaths++;
					usX = ucoords[0];
					usY = ucoords[1];
					ux0 = ucoords[0];
					uy0 = ucoords[1];
					sX = coords[0];
					sY = coords[1];
					x0 = coords[0];
					y0 = coords[1];
					handles.add(new Point2D.Double(ucoords[0],ucoords[1]));
					xCoords.add(new Integer((int)ucoords[0]));
					yCoords.add(new Integer((int)ucoords[1]));
					closed = false;
					break;
				case PathIterator.SEG_LINETO:
					linesOnly = linesOnly & true;
					curvesOnly = curvesOnly & false;
					pathLength += Math.sqrt(Math.pow((y0-coords[1]),2.0)+Math.pow((x0-coords[0]),2.0));
					ux0 = ucoords[0];
					uy0 = ucoords[1];
					x0 = coords[0];
					y0 = coords[1];
					handles.add(new Point2D.Double(ucoords[0],ucoords[1]));
					xCoords.add(new Integer((int)ucoords[0]));
					yCoords.add(new Integer((int)ucoords[1]));
					closed = ((int)ux0==(int)usX && (int)uy0==(int)usY);
					break;
				case PathIterator.SEG_QUADTO:
					linesOnly = linesOnly & false;
					curvesOnly = curvesOnly & true;
					curve = new QuadCurve2D.Double(x0,y0,coords[0],coords[2],coords[2],coords[3]);
					pathLength += qBezLength((QuadCurve2D.Double)curve);
					ux0 = ucoords[2];
					uy0 = ucoords[3];
					x0 = coords[2];
					y0 = coords[3];
					handles.add(new Point2D.Double(ucoords[0],ucoords[1]));
					handles.add(new Point2D.Double(ucoords[2],ucoords[3]));
					xCoords.add(new Integer((int)ucoords[2]));
					yCoords.add(new Integer((int)ucoords[3]));
					closed = ((int)ux0==(int)usX && (int)uy0==(int)usY);
					break;
				case PathIterator.SEG_CUBICTO:
					linesOnly = linesOnly & false;
					curvesOnly  = curvesOnly & true;
					curve = new CubicCurve2D.Double(x0,y0,coords[0],coords[1],coords[2],coords[3],coords[4],coords[5]);
					pathLength += cBezLength((CubicCurve2D.Double)curve);
					ux0 = ucoords[4];
					uy0 = ucoords[5];
					x0 = coords[4];
					y0 = coords[5];
					handles.add(new Point2D.Double(ucoords[0],ucoords[1]));
					handles.add(new Point2D.Double(ucoords[2],ucoords[3]));
					handles.add(new Point2D.Double(ucoords[4],ucoords[5]));
					xCoords.add(new Integer((int)ucoords[4]));
					yCoords.add(new Integer((int)ucoords[5]));
					closed = ((int)ux0==(int)usX && (int)uy0==(int)usY);
					break;
				case PathIterator.SEG_CLOSE:
					if((int)ux0 != (int)usX && (int)uy0 != (int)usY) pathLength += Math.sqrt(Math.pow((x0-sX),2.0) + Math.pow((y0-sY),2.0));
					closed = true;
					break;
				default:
					break;
			}
			pIter.next();
			done = pIter.isDone() || (shapeToRoi&&rois!=null&&rois.size()==1);
			if (done) {
				if(closed && (int)x0!=(int)sX && (int)y0!=(int)sY) { // this may only happen after a SEG_CLOSE
					xCoords.add(new Integer(((Integer)xCoords.elementAt(0)).intValue()));
					yCoords.add(new Integer(((Integer)yCoords.elementAt(0)).intValue()));
				}
				if (rois!=null) {
					roiType = shapeToRoi?TRACED_ROI:guessType(count+1, linesOnly, curvesOnly, closed);
					Roi r = createRoi(xCoords, yCoords, roiType);
					if (r!=null)
						rois.addElement(r);
				}
			}
		}
		params[0] = pathLength;
		//IJ.log("parsePath:"+ (System.currentTimeMillis()-start));
		return result;
	}

	/**********************************************************************************/
	/***                         Drawing and Image routines                        ****/
	/**********************************************************************************/

	/** Non-destructively draws the shape of this object on the associated IjxImagePlus. */
	public void draw(Graphics g) {
		if (ic==null) return;
		Color color =  strokeColor!=null? strokeColor:ROIColor;
		if (fillColor!=null) color = fillColor;
		g.setColor(color);
		AffineTransform aTx = (((Graphics2D)g).getDeviceConfiguration()).getDefaultTransform();
		if (stroke!=null) ((Graphics2D)g).setStroke(stroke);
		mag = ic.getMagnification();
		Rectangle r = ic.getSrcRect();
		aTx.setTransform(mag, 0.0, 0.0, mag, -r.x*mag, -r.y*mag);
        aTx.translate(x, y);
		Graphics2D g2d = (Graphics2D)g;
		if (fillColor!=null)
			g2d.fill(aTx.createTransformedShape(shape));
		else
			g2d.draw(aTx.createTransformedShape(shape));
		if (stroke!=null) g2d.setStroke(defaultStroke);
		if (((IjxToolbar) CentralLookup.getDefault().lookup(IjxToolbar.class)).getToolId()==IjxToolbar.OVAL)
			drawRoiBrush(g);
		if (imp!=null&&imp.getRoi()!=null) showStatus();
		if (updateFullWindow) 
			{updateFullWindow = false; imp.draw();}
	}

	public void drawRoiBrush(Graphics g) {
		g.setColor(ROIColor);
		int size = ((IjxToolbar) CentralLookup.getDefault().lookup(IjxToolbar.class)).getBrushSize();
		if (size==0) return;
		int flags = ic.getModifiers();
		if ((flags&16)==0) return; // exit if mouse button up
		size = (int)(size*mag);
		Point p = ic.getCursorLoc();
		int sx = ic.screenX(p.x);
		int sy = ic.screenY(p.y);
		g.drawOval(sx-size/2, sy-size/2, size, size);
	}
	
	/**Draws the shape of this object onto the specified ImageProcessor.
	 * <br> This method will always draw a flattened version of the actual shape
	 * (i.e., all curve segments will be approximated by line segments).
	 */
	public void drawPixels(ImageProcessor ip) {
		PathIterator pIter = shape.getPathIterator(new AffineTransform(), flatness);
		float[] coords = new float[6];
		float sx=0f, sy=0f;
		while (!pIter.isDone()) {
			int segType = pIter.currentSegment(coords);
			switch(segType) {
				case PathIterator.SEG_MOVETO:
					sx = coords[0];
					sy = coords[1];
					ip.moveTo(x+(int)sx, y+(int)sy);
					break;
				case PathIterator.SEG_LINETO:
					ip.lineTo(x+(int)coords[0], y+(int)coords[1]);
					break;
				case PathIterator.SEG_CLOSE:
					ip.lineTo(x+(int)sx, y+(int)sy);
					break;
				default: break;
			}
			pIter.next();
		}
	}

	/** Returns this ROI's mask pixels as a ByteProcessor with pixels "in" the mask
		set to white (255) and pixels "outside" the mask set to black (0). */
	public ImageProcessor getMask() {
		if(shape==null) return null;
		if (cachedMask!=null && cachedMask.getPixels()!=null)
			return cachedMask;
		//Rectangle r = getBounds();
		//if (r.x<0 || r.y<0) {
		//	if (r.x<0) r.x = 0;
		//	if (r.y<0) r.y = 0;
		//	ShapeRoi clipRect = new ShapeRoi(new Roi(r.x,r.y,r.width,r.height));
		//	setShape(getShape(this.or(clipRect)));
		//}
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D g2d = bi.createGraphics();
		g2d.setColor(Color.white);
		g2d.fill(shape);
		Raster raster = bi.getRaster();
		DataBufferByte buffer = (DataBufferByte)raster.getDataBuffer();		
		byte[] mask = buffer.getData();
		cachedMask = new ByteProcessor(width, height, mask, null);
        return cachedMask;
	}

	/**Returns a reference to the Shape object encapsulated by this ShapeRoi. */
	public Shape getShape() {
		return shape;
	}

	/**Sets the <code>java.awt.Shape</code> object encapsulated by <strong><code>this</code></strong>
	 * to the argument.
	 * <br>This object will hold a (shallow) copy of the shape argument. If a deep copy
	 * of the shape argumnt is required, then a clone of the argument should be passed
	 * in; a possible example is <code>setShape(ShapeRoi.cloneShape(shape))</code>.
	 * @return <strong><code>false</code></strong> if the argument is null.
	 */
	boolean setShape(Shape rhs) {
		boolean result = true;
		if (rhs==null) return false;
		if(shape.equals(rhs)) return false;
		shape = rhs;
		type = Roi.COMPOSITE;
		Rectangle rect = shape.getBounds();
		width = rect.width;
		height = rect.height;
		return true;
	}

	/**********************************************************************************/
	/***                               Other helpers                               ****/
	/**********************************************************************************/

	/**Returns the element with the smallest value in the array argument.*/
	private int min(int[] array) {
		int val = array[0];
		for (int i=1; i<array.length; i++) val = Math.min(val,array[i]);
		return val;
	}

	/**Returns the element with the largest value in the array argument.*/
	private int max(int[] array) {
		int val = array[0];
		for (int i=1; i<array.length; i++) val = Math.max(val,array[i]);
		return val;
	}
	
	/**
	public static void addCircle(String sx, String sy, String swidth) {
		int x = Integer.parseInt(sx);
		int y = Integer.parseInt(sy);
		int width = Integer.parseInt(swidth);
		IjxImagePlus img = IJ.getImage();
		if (img==null) return;
		Roi roi = img.getRoi();
		if (roi!=null) {
			if (!(roi instanceof ShapeRoi))
			roi = new ShapeRoi(roi);
			((ShapeRoi)roi).or(getCircularRoi(x, y, width));
		} else
			roi = getCircularRoi(x, y, width);
		img.setRoi(roi);
	}

	public static void subtractCircle(String sx, String sy, String swidth) {
		int x = Integer.parseInt(sx);
		int y = Integer.parseInt(sy);
		int width = Integer.parseInt(swidth);
		IjxImagePlus img = IJ.getImage();
		if (img==null) return;
		Roi roi = img.getRoi();
		if (roi!=null) {
			if (!(roi instanceof ShapeRoi))
			roi = new ShapeRoi(roi);
			((ShapeRoi)roi).not(getCircularRoi(x, y, width));
			img.setRoi(roi);
		}
	}
	*/

	static ShapeRoi getCircularRoi(int x, int y, int width) {
		return new ShapeRoi(new OvalRoi(x - width / 2, y - width / 2, width, width));
	}

	/** Always returns -1 since ShapeRois do not have handles. */
	public int isHandle(int sx, int sy) {
		   return -1;
	}
	
	/** Always returns null. */
	public Polygon getConvexHull() {
		return null;
	}

    /*
    static Polygon poly;
	static ShapeRoi getCircularRoi(int x, int y, int width) {
		if (poly==null || poly.getBoundingBox().width!=width) {
			Roi roi = new OvalRoi(x-width/2, y-width/2, width, width);
			poly = roi.getPolygon();
			for (int i=0; i<poly.npoints; i++) {
				poly.xpoints[i] -= x;
				poly.ypoints[i] -= y;
			}
		}
		return new ShapeRoi(x, y, poly);
	}
	*/

}
