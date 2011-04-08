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

import imagej.envisaje.api.vector.aggregate.Compound;
import java.awt.geom.Rectangle2D;
import junit.framework.TestCase;

/**
 *
 * @author Tim Boudreau
 */
public class CompoundTest extends TestCase {
    
    public CompoundTest(String nm) {
        super (nm);
    }
    
    public void testCompound() {
        System.out.println("testCompound");
        Rectangle a = new Rectangle (10,10,10,10, true);
        Rectangle b = new Rectangle (20, 20, 10, 10, true);
        Compound c = new Compound (0, 0);
        assertNotNull (c.getBounds());
        assertEquals (0, c.getBounds().width);
        assertEquals (0, c.getBounds().height);
        c.add(a);
        c.add(b);
        assertEquals (new java.awt.Rectangle (10, 10, 20, 20), c.getBounds());
        Rectangle2D.Double ra = new Rectangle2D.Double();
        Rectangle2D.Double rb = new Rectangle2D.Double();
        rb.setRect(c.getBounds());
        c.getBounds (ra);
        assertEquals (rb, ra);
        c.setLocation(10, 10);
        assertEquals (new java.awt.Rectangle (20, 20, 20, 20), c.getBounds());
    }
    
    public void testCompoundControlPoints() {
        System.out.println("testCompoundControlPoints");
        Rectangle a = new Rectangle (10,10,10,10, true);
        Rectangle b = new Rectangle (20, 20, 10, 10, true);
        Compound c = new Compound (0, 0);
        c.add(a);
        c.add(b);
        assertEquals (8, c.getControlPointCount());
        double[] d = new double [16];
        c.getControlPoints(d);
        double[] ad = new double[8];
        a.getControlPoints (ad);
        double[] bd = new double[8];
        b.getControlPoints (bd);
        for (int i = 0; i < d.length; i++) {
            if (i < 8) {
                assertEquals ("Failed at " + i, ad[i], d[i]);
            } else {
                assertEquals ("Failed at " + i, bd[i-8], d[i]);
            }
        }
    }
}
