/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.display.zoomview;

import imagej.display.zoomview.ITileSet;
import imagej.display.zoomview.Tile;

import java.awt.Dimension;

/**
 * The tile manager manages one or more tile sets.
 *
 * @author Aivar Grislis
 */
public interface ITileManager {

    /**
     * Initializes the tile manager.
     *
     * @param factory
     * @param level
     * @param width
     * @param height
     * @param tileWidth
     * @param tileHeight
     * @param bytesPerPixel
     */
    public void init(
            ITileFactory factory,
            int level,
            int width, int height,
            int tileWidth, int tileHeight,
            int bytesPerPixel);

    /**
     * Returns unique identifier string.
     *
     * @return
     */
    public String getId();

    /**
     * Gets the 2D dimensions of the tiled image.
     * @return
     */
    public Dimension getDimension();

    /**
     * Gets the maximum tile indices.
     *
     * @return
     */
    public int[] getMaxIndex();

    /**
     * Gets the size of the tile.
     *
     * @return
     */
    public int[] getTileSize();

    /**
     * Gets teh size of a pixel.
     *
     * @return bytes per pixel
     */
    public int getBytesPerPixel();

    /**
     * Gets priority of given tile set.
     *
     * @param tileSet
     * @return priority int, 0 is lowest
     */
    public int getPriority(ITileSet tileSet);

    /**
     * Saves a tile that has been bumped from the cache.
     *
     * @param tileSet
     * @param tile to save
     */
    public void putTile(ITileSet tileSet, Tile tile);

    /**
     * Gets a tile that is not available in the cache.
     *
     * @param tileSet
     * @param index
     * @return tile
     */
    public Tile getTile(ITileSet tileSet, int index[]);
}
