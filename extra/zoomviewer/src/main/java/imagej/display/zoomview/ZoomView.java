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
 * This represents a zoomed view, showing x and y plane at different zoom levels,
 * for particular non-x,y dimensional indices.
 *
 * @author Aivar Grislis
 */
public class ZoomView {
    TileCache m_tileCache;
    ITileSet m_tileSet[];

    public void init(TileCache tileCache, ITileFactory originalTileFactory, Dimension dimension, int levels) {
        m_tileCache = tileCache;
        m_tileSet = new ITileSet[levels];

        // create shared tile factories
        //ITileFactory scaleInHalfTileFactory = new ScaleInHalfTileFactory();
        //scaleInHalfTileFactory.init(this);
        ITileFactory decimationTileFactory = new DecimationTileFactory();
        decimationTileFactory.init(this);

        int width = dimension.width;
        int height = dimension.height;
        for (int level = 0; level < levels; ++level) {
            System.out.println("Create level " + level);
            ITileFactory tileFactory = null;
            if (0 == level) {
                tileFactory = originalTileFactory;
            }
            else {
                tileFactory = decimationTileFactory; //scaleInHalfTileFactory;
            }
            tileFactory.init(this);
            ITileManager tileManager = new FileTileManager();
            int tileWidth = ZoomTileServer.TILE_WIDTH;
            int tileHeight = ZoomTileServer.TILE_HEIGHT;
            int bytesPerPixel = 4;
            tileManager.init(tileFactory, level, width, height, tileWidth, tileHeight, bytesPerPixel);
            ITileSet tileSet = new TileSet();
            tileSet.init(tileManager);
            m_tileSet[level] = tileSet;
            tileCache.addTileSet(tileSet);
            tileSet.setLevel(level);

            // compute next width and height
            width = ++width / 2;
            height = ++height / 2;
        }
    }
    
    public void tearDown() {
        for (ITileSet tileSet : m_tileSet) {
            //TODO don't necessarily want to flush to disk at this point.
            //TODO clear all temporary files
            tileSet.flush();
        }
    }

    public ITileSet getTileSet(int level) {
        return m_tileSet[level];
    }

    public Tile getTile(int level, int index[]) {
        //if (level > 0) {
        //    System.out.println("level is " + level);
        //}
        // ask the cache for this tile
        return m_tileCache.getTile(m_tileSet[level], index);
    }
}
