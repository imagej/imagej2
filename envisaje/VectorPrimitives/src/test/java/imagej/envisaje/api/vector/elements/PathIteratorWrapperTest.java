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

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import junit.framework.TestCase;

/**
 *
 * @author Tim Boudreau
 */
public class PathIteratorWrapperTest extends TestCase {
    
    /** Creates a new instance of PathIteratorWrapperTest */
    public PathIteratorWrapperTest(String name) {
        super (name);
    }
    
    public void testBasics() {
        System.out.println("testBasics");
        Rectangle2D.Double d = new Rectangle2D.Double (10, 10, 10, 10);
        PathIterator it = d.getPathIterator(AffineTransform.getTranslateInstance(0, 0));
        PathIterator it2 = d.getPathIterator(AffineTransform.getTranslateInstance(0, 0));
        PathIteratorWrapper p = new PathIteratorWrapper (it);
        PathIteratorWrapper p1 = new PathIteratorWrapper (it2);
        assertEquals (p, p1);
        
        assertEquals ("Expected control point count of four " + p, 
                5, p.getControlPointCount());
        double[] data = new double[10];
        p.getControlPoints (data);
        System.out.println("ARR: " + outArr (data));
        assertEquals (10D, data[0]);
        assertEquals (10D, data[1]);
        
        p.delete(1);
        System.out.println(p);
        assertEquals (4, p.getControlPointCount());
        
        p.insert(5, 5, 1, PathIterator.SEG_LINETO);
        System.out.println(p);
    }
    
    private String outArr (double[] d) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < d.length; i++) {
            double e = d[i];
            sb.append (e);
            if (i < d.length - 1) {
                sb.append (", ");
            }
        }
        return sb.toString();
    }
    
}
