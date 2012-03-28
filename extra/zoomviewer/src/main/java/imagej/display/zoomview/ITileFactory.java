/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.display.zoomview;

//import imagej.zoomviewer.ITileLevels;

/**
 * A tile factory creates a tile on demand when it is not available in any cache.
 *
 * @author aivar
 */
public interface ITileFactory {

    /**
     * Provides a ZoomView which can be used to get other tiles.
     *
     * @param zoomView
     */
    public void init(ZoomView zoomView);

    /**
     * Creates a tile on demand.  Index array is organized as follows:
     * <p>
     * index[0] is x index of tile<p>
     * index[1] is y index of tile<p>
     * remainder of index[] if any specifies further dimensions.
     *
     * @param level
     * @param index
     * @return
     */
    public Tile createTile(int level, int index[]);
}
