/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
