/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.zoomviewer;

import imagej.display.zoomview.Tile;

import java.awt.Dimension;

/**
 *
 * @author aivar
 */
public interface ITileLevels {

    void fitToSize(int width, int height);

    /**
     * Gets the size of a pixel in bytes.
     *
     * @return
     */
    int getBytesPerPixel(int level);

    /**
     * Gets the 2D dimensions of the tiled image.
     * @return
     */
    Dimension getDimension(int level);

    int getLevel();

    /**
     * Gets the maximum tile indices.
     *
     * @return
     */
    int[] getMaxIndex(int level);

    /**
     * Gets the size of the tile in pixels.
     *
     * @return
     */
    int getPixelsPerTile(int level);

    /**
     * Gets the size of the tile.
     *
     * @return
     */
    int[] getTileSize(int level);

    /**
     * Gets the amount of space used by one tile.
     *
     * @return cache space needed for one tile, in bytes
     */
    int m_bytesPerTile(int level);

    Tile getTile(int level, int width, int height); //TODO int index[]);

    void putTile(int level, Tile tile);

    void setLevel(int level);

}
