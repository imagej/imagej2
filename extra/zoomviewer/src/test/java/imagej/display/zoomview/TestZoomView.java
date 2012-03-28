/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.display.zoomview;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author aivar
 */
public class TestZoomView extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TestZoomView(String testName) {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(TestZoomView.class);
    }

    public void testGetFurtherDims()
    {
        System.out.println("testGetFurtherDims");
        ZoomTileServer zoomView = new ZoomTileServer();
        int dim[] = { 4, 5, 2, 2, 2, 2, 2, 2, 3 };
        String furtherDims[] = zoomView.getFurtherDims(dim);
        for (String furtherDim : furtherDims) {
            System.out.println("further Dim is " + furtherDim);
        }
        assertTrue(1 == 1);
    }

    public void testGetFurtherIndices()
    {
        System.out.println("testGetFurtherIndices");
        ZoomTileServer zoomView = new ZoomTileServer();
        int index[] = { 4, 5, 6, 7, 8 };
        String furtherIndices = zoomView.getFurtherIndices(index);
        System.out.println("furtherIndices is " + furtherIndices);
    }
}
