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
