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
