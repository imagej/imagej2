/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.display.zoomview;

import java.awt.Dimension;

/**
 * Interface to a cached set of tiles, e.g. corresponding to a zoom level.
 *
 * @author Aivar Grislis
 */
public interface ITileSet {

    public void init(ITileManager manager);
    
    /**
     * Gets a unique identifier.
     *
     * @return
     */
    public String getId();

    public void setLevel(int level);
    public int getLevel();

    /**
     * Gets the dimension of the tiled image.
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
     * Gets the size of the tile in pixels.
     *
     * @return
     */
    public int getPixelsPerTile();

    /**
     * Gets the size of a pixel in bytes.
     *
     * @return
     */
    public int getBytesPerPixel();

    /**
     * Gets the amount of space used by one tile.
     *
     * @return cache space needed for one tile, in bytes
     */
    public int getBytesPerTile();

    /**
     * Gets the amount of space used by all the cached tiles.
     *
     * @return bytes cached
     */
    public int getBytesUsed();

    /**
     * Frees up some cache space.
     *
     * @param bytes space needed in bytes
     */
    public void freeUpCache(int bytes);

    /**
     * Gets priority of this set.
     *
     * @return priority, zero is lowest
     */
    public int getPriority();

    /**
     * Puts a tile to the cache.
     *
     * @param tile
     */
    public void putTile(Tile tile);

    /**
     * Gets a tile if cached for this set.
     *
     * @param indices indices of this tile
     * @return null or tile
     */
    public Tile getCachedTile(int indices[]);

    /**
     * Gets a tile that is not in the cache for this set.
     *
     * @param indices
     * @return non-null tile
     */
    public Tile getUncachedTile(int indices[]);

    /**
     * Empties the cache.
     */
    public void flush();
}
