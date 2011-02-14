/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.zoomviewer;

import imagej.display.zoomview.TileCache;
import imagej.display.zoomview.ITileManager;
import imagej.display.zoomview.FileTileManager;
import imagej.display.zoomview.TileSet;
import imagej.display.zoomview.ITileSet;
import imagej.display.zoomview.Tile;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Aivar Grislis
 */
public class MipMapBasedTileScaler implements ITileLevels {
    private static final int IMPOSSIBLE_VALUE = -1;
    private static final int LEVEL_FACTOR = 2;
    private final TileCache m_tileCache;
    private final int m_width;
    private final int m_height;
    private int m_level;
    private FileTileManager m_mipMapFileManager;
    private MipMapScaleTileManager m_mipMapScaleManager;
    private List<ITileSet> m_tileSetLevelList;

    /**
     * This class provides different levels of scaled files.  It can be shared
     * by several viewers.
     *
     * //TODO need to pass in the level 0 ITileManager or c/b a higher level
     * construct.  That class also needs to be able to invalidate tiles cached
     * in this class.
     *
     * @param width
     * @param height
     */
    public MipMapBasedTileScaler(TileCache tileCache, int width, int height) {
        m_tileCache = tileCache;
        m_width = width;
        m_height = height;

        ITileManager tileManager = null;
        ITileSet tileSet = null;
        m_tileSetLevelList = new ArrayList<ITileSet>();
        
        tileManager = new FileTileManager();
        tileManager.init(null, 0, //TODO
                width, height,
                256, 256, 4);
        tileSet = new TileSet();
        tileSet.init(tileManager);
        m_tileSetLevelList.add(tileSet);
        m_tileCache.addTileSet(tileSet);

        width /= 2;
        height /= 2;
        tileManager = new FileTileManager();
        tileManager.init(null, 0,
                width, height,
                256, 256, 4);
        tileSet = new TileSet();
        tileSet.init(tileManager);
        m_tileSetLevelList.add(tileSet);
        m_tileCache.addTileSet(tileSet);

        width /= 2;
        height /= 2;
        tileManager = new FileTileManager();
        tileManager.init(null, 0,
                width, height,
                25, 256, 4);
        tileSet = new TileSet();
        tileSet.init(tileManager);
        m_tileSetLevelList.add(tileSet);
        m_tileCache.addTileSet(tileSet);

        //TODO just hack it up for now
        //m_level = IMPOSSIBLE_VALUE;
    }

    /**
     * Gets the 2D dimensions of the tiled image.
     * @return
     */
    public Dimension getDimension(int level) {
        return m_tileSetLevelList.get(level).getDimension();
    }

    /**
     * Gets the maximum tile indices.
     *
     * @return
     */
    public int[] getMaxIndex(int level) {
        return m_tileSetLevelList.get(level).getMaxIndex();
    }

    /**
     * Gets the size of the tile.
     *
     * @return
     */
    public int[] getTileSize(int level) {
        return m_tileSetLevelList.get(level).getTileSize();
    }

    /**
     * Gets the size of the tile in pixels.
     *
     * @return
     */
    public int getPixelsPerTile(int level) {
        return m_tileSetLevelList.get(level).getPixelsPerTile();
    }

    /**
     * Gets the size of a pixel in bytes.
     *
     * @return
     */
    public int getBytesPerPixel(int level) {
        return m_tileSetLevelList.get(level).getBytesPerPixel();
    }

    /**
     * Gets the amount of space used by one tile.
     *
     * @return cache space needed for one tile, in bytes
     */
    public int m_bytesPerTile(int level) {
        return m_tileSetLevelList.get(level).getBytesPerTile();
    }

    public Tile getTile(int level, int width, int height) {
        return m_tileCache.getTile(m_tileSetLevelList.get(level), new int[] { width, height });
    }

    public void putTile(int level, Tile tile) {
        m_tileCache.putTile(m_tileSetLevelList.get(level), tile);
    }
    
    public void fitToSize(int width, int height) {
        
    }
    
    public void setLevel(int level) {
        m_level = level;
    }

    public int getLevel() {
        return m_level;
    }
}
