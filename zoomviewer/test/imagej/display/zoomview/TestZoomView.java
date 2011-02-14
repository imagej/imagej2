/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
