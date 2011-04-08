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

import imagej.envisaje.api.vector.aggregate.TransformedPrimitive;
import imagej.envisaje.api.vector.util.Pt;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import junit.framework.TestCase;

/**
 *
 * @author Tim Boudreau
 */
public class TransformedPrimitiveTest extends TestCase {
    
    /** Creates a new instance of TransformedPrimitiveTest */
    public TransformedPrimitiveTest(String name) {
        super (name);
    }
    
    public void testTransformedPrimitive() {
        System.out.println("testTransformedPrimitive");
        Rectangle r = new Rectangle (10, 10, 10, 10, true);
        TransformedPrimitive tp = TransformedPrimitive.create (r, 
                AffineTransform.getTranslateInstance (-10, -10));
        Rectangle2D.Double bds = new Rectangle2D.Double();
        tp.getBounds(bds);
        assertEquals (0D, bds.getX());
        assertEquals (0D, bds.getY());
        assertEquals (10D, bds.getWidth());
        assertEquals (10D, bds.getHeight());
        tp.setLocation(10D, 10D);
        Rectangle r2 = (Rectangle) tp.toIdentityPrimitive();
        assertEquals (r, r2);
        Pt pt = tp.getLocation();
        assertEquals (10D, pt.x);
        assertEquals (10D, pt.y);
    }
    
    public void testScaling() {
        System.out.println("testScaling");
        Rectangle r = new Rectangle (10, 10, 10, 10, true);
        TransformedPrimitive tp = TransformedPrimitive.create (r, 
                AffineTransform.getScaleInstance (2D, 2D));
        Rectangle2D.Double bds = new Rectangle2D.Double();
        tp.getBounds(bds);
        assertEquals (bds.x, 20D);
        assertEquals (bds.y, 20D);
        assertEquals (bds.width, 20D);
        assertEquals (bds.height, 20D);
        
        r = new Rectangle (20, 20, 20, 20, true);
        int ct = tp.getControlPointCount();
        assertEquals (r.getControlPointCount(), ct);
        double[] d = new double [ ct * 2 ];
        tp.getControlPoints(d);
        double[] rd = new double [ ct * 2 ];
        r.getControlPoints(rd);
        for (int i = 0; i < rd.length; i++) {
            assertEquals (rd[i], d[i]);
        }
    }    
    
}
