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

import imagej.envisaje.api.vector.Attribute;
import imagej.envisaje.api.vector.aggregate.PaintedPrimitive;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import junit.framework.TestCase;

/**
 *
 * @author Tim Boudreau
 */
public class PaintedPrimitiveTest extends TestCase {
    
    /** Creates a new instance of PaintedPrimitiveTest */
    public PaintedPrimitiveTest(String nm) {
        super (nm);
    }
    
    public void testPaintedPrimitive() {
        Rectangle r = new Rectangle (10, 10, 10, 10, true);
        PaintedPrimitive pp = PaintedPrimitive.create (r, Collections.<Attribute>emptyList());
        Rectangle2D.Double rd = new Rectangle2D.Double();
        Rectangle2D.Double pd = new Rectangle2D.Double();
        r.getBounds (rd);
        pp.getBounds (pd);
        assertEquals (rd, pd);
        
        int ct = r.getControlPointCount();
        assertEquals (ct, pp.getControlPointCount());
        double[] rrd = new double [ct * 2];
        double[] ppd = new double [ct * 2];
        r.getControlPoints(rrd);
        pp.getControlPoints(ppd);
        for (int i = 0; i < ppd.length; i++) {
            assertEquals (rrd[i], ppd[i]);
        }
    }
    
}
