/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.zoomviewer;

import imagej.display.zoomview.ITileFactory;
import imagej.display.zoomview.ITileManager;
import imagej.display.zoomview.ITileSet;
import imagej.display.zoomview.Tile;

import java.awt.Dimension;
import java.util.UUID;

/**
 *
 * @author aivar
 */
public class MipMapScaleTileManager implements ITileManager {
    private final String m_id = UUID.randomUUID().toString();
    private ITileFactory m_factory;
    private int m_level;
    private int m_width;
    private int m_height;
    private int m_tileWidth;
    private int m_tileHeight;
    private int m_bytesPerPixel;

    public void init(
            ITileFactory factory,
            int level,
            int width, int height,
            int tileWidth, int tileHeight,
            int bytesPerPixel) {
        m_factory = factory;
        m_level = level;
        m_width = width;
        m_height = height;
        m_tileWidth = tileWidth;
        m_tileHeight = tileHeight;
        m_bytesPerPixel = bytesPerPixel;
    }

    public String getId() {
        return m_id;
    }

    /**
     * Gets the 2D dimensions of the tiled image.
     * @return
     */
    public Dimension getDimension() {
        return new Dimension(m_width, m_height);
    }

    /**
     * Gets the maximum tile indices.
     *
     * @return
     */
    public int[] getMaxIndex() {
        int maxX = m_width / m_tileWidth;
        if ((m_width % m_tileWidth) != 0) {
            ++maxX;
        }
        int maxY = m_height / m_tileHeight;
        if ((m_height % m_tileHeight) != 0) {
            ++maxY;
        }
        return new int[] { maxX, maxY };
    }

    public int getBytesPerPixel() {
        return m_bytesPerPixel;
    }

    /**
     * Gets the size of the tile.
     *
     * @return
     */
    public int[] getTileSize() {
        return new int[] { m_tileWidth, m_tileHeight };
    }

    public int getPriority(ITileSet tileSet) {
        int priority = 0;
        //TODO
      //  if (m_level == m_mipMap.getLevel()) {
       //     priority = 2;
       // }
        return priority;
    }

    public void putTile(ITileSet tileSet, Tile tile) {
        //TODO
    }

    public Tile getTile(ITileSet tileSet, int index[]) {
        return null;
        //TODO
    }
}
