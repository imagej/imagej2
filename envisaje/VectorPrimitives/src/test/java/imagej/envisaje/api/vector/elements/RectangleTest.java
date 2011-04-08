/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package imagej.envisaje.api.vector.elements;

import imagej.envisaje.api.vector.design.ControlPoint;
import imagej.envisaje.api.vector.design.ControlPoint.Controller;
import imagej.envisaje.api.vector.design.ControlPointFactory;
import imagej.envisaje.api.vector.util.Pt;
import imagej.envisaje.api.vector.util.Size;
import junit.framework.TestCase;


/**
 *
 * @author Tim Boudreau
 */
public class RectangleTest extends TestCase {
    
    /** Creates a new instance of RectangleTest */
    public RectangleTest(String nm) {
        super (nm);
    }
    
    public void testRect() {
        System.out.println("testRect");
        Rectangle r = new Rectangle (10, 10, 10, 10, true);
        assertEquals (10.0D, r.x);
        assertEquals (10.0D, r.y);
        assertEquals (10.0D, r.w);
        assertEquals (10.0D, r.h);
    }
    
    public void testControlPoints() {
        System.out.println("testControlPoints");
        Rectangle r = new Rectangle (10, 10, 10, 10, true);
        assertEquals (4, r.getControlPointCount());
        double[] xy = new double [r.getControlPointCount() * 2];
        r.getControlPoints(xy);
        assertEquals (10D, xy[0]);
        assertEquals (10D, xy[1]);
    }
    
    public void testControlPointModification() {
        System.out.println("testControlPointModification");
        Rectangle r = new Rectangle (10, 10, 10, 10, true);
        r.setControlPointLocation(0, new Pt (0, 0));
        
        assertEquals (r + " should be 0, 0, 20, 20", 0.0D, r.x);
        assertEquals (r + " should be 0, 0, 20, 20", 0.0D, r.y);
        assertEquals (r + " should be 0, 0, 20, 20", 20.0D, r.w);
        assertEquals (r + " should be 0, 0, 20, 20", 20.0D, r.h);
        
        r.setControlPointLocation (2, new Pt (10, 10));
        assertEquals (r + " should be 0, 0, 10, 10", 0.0D, r.x);
        assertEquals (r + " should be 0, 0, 10, 10", 0.0D, r.y);
        assertEquals (r + " should be 0, 0, 10, 10", 10.0D, r.w);
        assertEquals (r + " should be 0, 0, 10, 10", 10.0D, r.h);

        r = new Rectangle (10, 10, 10, 10, true);
        r.setControlPointLocation (1, new Pt (5, 25));
        assertEquals (15D, r.w);
        assertEquals (15D, r.h);

        r = new Rectangle (10, 10, 10, 10, true);
        r.setControlPointLocation (3, new Pt (25, 5));
        assertEquals (15D, r.w);
        assertEquals (15D, r.h);
        
        r = new Rectangle (10, 10, 10, 10, true);
        r.setControlPointLocation(2, new Pt(5, 5));
        assertEquals (5D, r.x);
        assertEquals (5D, r.y);
        assertEquals (5D, r.w);
        assertEquals (5D, r.h);        
        
        r = new Rectangle (10, 10, 10, 10, true);
        r.setControlPointLocation(2, new Pt(17, 15));
        assertEquals (r + " wrong", 10D, r.x);
        assertEquals (r + " wrong", 10D, r.y);
        assertEquals (r + " wrong", 7D, r.w);
        assertEquals (r + " wrong", 5D, r.h);
        
        r = new Rectangle (10, 10, 10, 10, true);
        r.setControlPointLocation(1, new Pt(12, 12));
        assertEquals (r + " wrong", 12D, r.x);
        assertEquals (r + " wrong", 2D, r.h);
    }
    
    public void testControlPointObjects() {
        System.out.println("testControlPointObjects");
        Rectangle r = new Rectangle (10, 10, 10, 10, true);        
        ControlPointFactory cf = new ControlPointFactory ();
        ControllerImpl impl = new ControllerImpl();
        ControlPoint[] pts = cf.getControlPoints(r, impl);
        assertEquals (4, pts.length);
        double[] xy = new double [r.getControlPointCount() * 2];
        r.getControlPoints(xy);
        for (int i=0; i < pts.length; i++) {
            assertSame (r, pts[0].getPrimitive());
        }
        assertEquals (10D, pts[0].getX());
        assertEquals (10D, pts[0].getY());
        assertEquals (20D, pts[2].getX());
        assertEquals (20D, pts[2].getY());
        
        pts[0].set(0, 0);
        impl.assertChanged(pts[0]);
        assertEquals (0D, r.x);
        assertEquals (0D, r.y);
        assertEquals (20D, r.w);
        assertEquals (20D, r.h);
        pts[0].move(40, 40);
        impl.assertChanged(pts[0]);
        assertEquals (20D, r.w);
        assertEquals (20D, r.h);
    }

    public void testRRect() {
        System.out.println("testRRect");
        RoundRect r = new RoundRect (10, 10, 10, 10, 5, 5, true);
        assertEquals (10.0D, r.x);
        assertEquals (10.0D, r.y);
        assertEquals (10.0D, r.w);
        assertEquals (10.0D, r.h);
    }
    
    public void testRRControlPoints() {
        System.out.println("testRRControlPoints");
        RoundRect r = new RoundRect (10, 10, 10, 10, 5, 5, true);
        assertEquals (4, r.getControlPointCount());
        double[] xy = new double [r.getControlPointCount() * 2];
        r.getControlPoints(xy);
        assertEquals (10D, xy[0]);
        assertEquals (10D, xy[1]);
    }
    
    public void testRRControlPointModification() {
        System.out.println("testRRControlPointModification");
        RoundRect r = new RoundRect (10, 10, 10, 10, 5, 5, true);
        r.setControlPointLocation(0, new Pt (0, 0));
        
        assertEquals (r + " should be 0, 0, 20, 20", 0.0D, r.x);
        assertEquals (r + " should be 0, 0, 20, 20", 0.0D, r.y);
        assertEquals (r + " should be 0, 0, 20, 20", 20.0D, r.w);
        assertEquals (r + " should be 0, 0, 20, 20", 20.0D, r.h);
        
        r.setControlPointLocation (2, new Pt (10, 10));
        assertEquals (r + " should be 0, 0, 10, 10", 0.0D, r.x);
        assertEquals (r + " should be 0, 0, 10, 10", 0.0D, r.y);
        assertEquals (r + " should be 0, 0, 10, 10", 10.0D, r.w);
        assertEquals (r + " should be 0, 0, 10, 10", 10.0D, r.h);

        r = new RoundRect (10, 10, 10, 10, 5,5,true);
        r.setControlPointLocation (1, new Pt (5, 25));
        assertEquals (15D, r.w);
        assertEquals (15D, r.h);

        r = new RoundRect (10, 10, 10, 10, 5,5,true);
        r.setControlPointLocation (3, new Pt (25, 5));
        assertEquals (15D, r.w);
        assertEquals (15D, r.h);
        
        r = new RoundRect (10, 10, 10, 10, 5,5,true);
        r.setControlPointLocation(2, new Pt(5, 5));
        assertEquals (5D, r.x);
        assertEquals (5D, r.y);
        assertEquals (5D, r.w);
        assertEquals (5D, r.h);        
    }
    
    public void testRRControlPointObjects() {
        System.out.println("testRRControlPointObjects");
        RoundRect r = new RoundRect (10, 10, 10, 10, 5, 5, true);
        ControlPointFactory cf = new ControlPointFactory ();
        ControllerImpl impl = new ControllerImpl();
        ControlPoint[] pts = cf.getControlPoints(r, impl);
        assertEquals (4, pts.length);
        double[] xy = new double [r.getControlPointCount() * 2];
        r.getControlPoints(xy);
        for (int i=0; i < pts.length; i++) {
            assertSame (r, pts[0].getPrimitive());
        }
        assertEquals (10D, pts[0].getX());
        assertEquals (10D, pts[0].getY());
        assertEquals (20D, pts[2].getX());
        assertEquals (20D, pts[2].getY());
        
        pts[0].set(0, 0);
        impl.assertChanged(pts[0]);
        assertEquals (0D, r.x);
        assertEquals (0D, r.y);
        assertEquals (20D, r.w);
        assertEquals (20D, r.h);
        pts[0].move(40, 40);
        impl.assertChanged(pts[0]);
        assertEquals (20D, r.w);
        assertEquals (20D, r.h);
    }
    
    public void testLine() {
        System.out.println("testLine");
        Line l = new Line (10, 10, 20, 20);
        assertEquals (l.x1, 10D);
        assertEquals (l.y1, 10D);
        assertEquals (l.x2, 20D);
        assertEquals (l.y2, 20D);
        assertEquals (2, l.getControlPointCount());
        double[] d = new double [4];
        l.getControlPoints(d);
        l.setControlPointLocation(0, new Pt (0,0));
        assertEquals (l.x1, 0D);
        assertEquals (l.y1, 0D);
        l.setControlPointLocation(1, new Pt (10,15));
        assertEquals (l.x2, 10D);
        assertEquals (l.y2, 15D);
    }

    public void testOval() {
        System.out.println("testOval");
        Oval r = new Oval (10, 10, 10, 10, true);
        assertEquals (10.0D, r.x);
        assertEquals (10.0D, r.y);
        assertEquals (10.0D, r.width);
        assertEquals (10.0D, r.height);
    }
    
    public void testOvalControlPoints() {
        System.out.println("testOvalControlPoints");
        Oval r = new Oval (10, 10, 10, 10, true);
        assertEquals (4, r.getControlPointCount());
        double[] xy = new double [r.getControlPointCount() * 2];
        r.getControlPoints(xy);
        assertEquals (10D, xy[0]);
        assertEquals (10D, xy[1]);
    }
    
    public void testOvalControlPointModification() {
        System.out.println("testOvalControlPointModification");
        Oval r = new Oval (10, 10, 10, 10, true);
        r.setControlPointLocation(0, new Pt (0, 0));
        
        assertEquals (r + " should be 0, 0, 20, 20", 0.0D, r.x);
        assertEquals (r + " should be 0, 0, 20, 20", 0.0D, r.y);
        assertEquals (r + " should be 0, 0, 20, 20", 20.0D, r.width);
        assertEquals (r + " should be 0, 0, 20, 20", 20.0D, r.height);
        
        r.setControlPointLocation (2, new Pt (10, 10));
        assertEquals (r + " should be 0, 0, 10, 10", 0.0D, r.x);
        assertEquals (r + " should be 0, 0, 10, 10", 0.0D, r.y);
        assertEquals (r + " should be 0, 0, 10, 10", 10.0D, r.width);
        assertEquals (r + " should be 0, 0, 10, 10", 10.0D, r.height);

        r = new Oval (10, 10, 10, 10, true);
        r.setControlPointLocation (1, new Pt (5, 25));
        assertEquals (15D, r.width);
        assertEquals (15D, r.height);

        r = new Oval (10, 10, 10, 10, true);
        r.setControlPointLocation (3, new Pt (25, 5));
        assertEquals (15D, r.width);
        assertEquals (15D, r.height);
        
        r = new Oval (10, 10, 10, 10, true);
        r.setControlPointLocation(2, new Pt(5, 5));
        assertEquals (5D, r.x);
        assertEquals (5D, r.y);
        assertEquals (5D, r.width);
        assertEquals (5D, r.height);        
    }
    
    public void testOvalControlPointObjects() {
        System.out.println("testControlPointObjects");
        Rectangle r = new Rectangle (10, 10, 10, 10, true);        
        ControlPointFactory cf = new ControlPointFactory ();
        ControllerImpl impl = new ControllerImpl();
        ControlPoint[] pts = cf.getControlPoints(r, impl);
        assertEquals (4, pts.length);
        double[] xy = new double [r.getControlPointCount() * 2];
        r.getControlPoints(xy);
        for (int i=0; i < pts.length; i++) {
            assertSame (r, pts[0].getPrimitive());
        }
        assertEquals (10D, pts[0].getX());
        assertEquals (10D, pts[0].getY());
        assertEquals (20D, pts[2].getX());
        assertEquals (20D, pts[2].getY());
        
        pts[0].set(0, 0);
        impl.assertChanged(pts[0]);
        assertEquals (0D, r.x);
        assertEquals (0D, r.y);
        assertEquals (20D, r.w);
        assertEquals (20D, r.h);
        pts[0].move(40, 40);
        impl.assertChanged(pts[0]);
        assertEquals (20D, r.w);
        assertEquals (20D, r.h);
    }
    
    
    private static class ControllerImpl implements Controller {
        ControlPoint chg = null;
        public void changed(ControlPoint pt) {
            chg = pt;
        }
        
        public ControlPoint assertChanged () {
            ControlPoint result = chg;
            chg = null;
            assertNotNull (result);
            return result;
        }
        
        public void assertChanged (ControlPoint pt) {
            assertEquals (pt, assertChanged());
        }

        public Size getControlPointSize() {
            return new Size (5, 5);
        }
    }
}
