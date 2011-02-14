/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.display.zoomview;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *  The ZoomTileServer provides tiles at different zoom levels.
 *
 * @author Aivar Grislis
 */
public class ZoomTileServer {
    private final String SEPARATOR = "-";

    /**
     * The width of a tile.<p>
     *
     * Must be divisible by two.
     */
    public static final int TILE_WIDTH = 256;

    /**
     * The height of a tile.<p>
     *
     * Must be divisible by two.
     */
    public static final int TILE_HEIGHT = 256;

    int m_levels;
    Map<String, ZoomView> m_viewerMap = new HashMap<String, ZoomView>();

    /**
     * Initializes the zoom view server.<p>
     * Builds the set of zoom levels.
     *
     * @param tileCache
     * @param factory
     * @param dim
     */
    public void init(TileCache tileCache, ITileFactory factory, int dim[]) {
        System.out.println("ZVS init");
        for (String furtherDims : getFurtherDims(dim)) {
            System.out.println("furtherDims is " + furtherDims);
            ZoomView zoomView = new ZoomView();
            m_levels = 7;
            zoomView.init(tileCache, factory, new Dimension(dim[0], dim[1]), m_levels);
            m_viewerMap.put(furtherDims, zoomView);
            System.out.println("put " + furtherDims + " zoom view " + zoomView);
        }
    }

    /**
     * Tears down the server.
     */
    public void tearDown() {
        for (ZoomView zoomViewer : m_viewerMap.values()) {
            zoomViewer.tearDown();
        }
        m_viewerMap.clear();
    }

    public int getLevels() {
        return m_levels;
    }

    //TODO this is a bit ugly; why does the tileset have to know these things?
    public Dimension getDimensionByLevel(int level) {
        ZoomView zoomView = m_viewerMap.values().toArray(new ZoomView[0])[0];
        ITileSet tileSet = zoomView.getTileSet(level);
        return tileSet.getDimension();
    }

    //TODO this is a bit ugly; why does the tileset have to know these things?
    public int[] getMaxTileIndexByLevel(int level) {
        ZoomView zoomView = m_viewerMap.values().toArray(new ZoomView[0])[0];
        ITileSet tileSet = zoomView.getTileSet(level);
        return tileSet.getMaxIndex();
    }

    /**
     * Serves a tile at a given level.  Index[0] and index[1] are x and y.
     * Remaining indices designate which view to use.
     *
     * @param level
     * @param index
     * @return
     */
    public Tile getTile(int level, int index[]) {
        System.out.println("getFI is >" + getFurtherIndices(index) + "<");
        ZoomView zoomViewer = m_viewerMap.get(getFurtherIndices(index));
        return zoomViewer.getTile(level, index);
    }

    /**
     * Builds a string that describes the index past X and Y.
     *
     * @param index
     * @return string
     */
    String getFurtherIndices(int index[]) {
        StringBuilder stringBuilder = new StringBuilder();
        if (index.length > 2) {
            stringBuilder.append(index[2]);
            for (int i = 3; i < index.length; ++i) {
                stringBuilder.append(SEPARATOR);
                stringBuilder.append(index[i]);
            }
        }
        if (0 == stringBuilder.length()) {
            stringBuilder.append("0");
        }
        return stringBuilder.toString();
    }

    /**
     * Builds an array of strings that describe the dimensions past X and Y.
     *
     * @param dim
     * @return array of strings
     */
    String[] getFurtherDims(int dim[]) {
        Set<String> stringSet = new HashSet<String>();
        for (int i = 2; i < dim.length; ++i) {
            Set<String> newStringSet = new HashSet<String>();
            for (int j = 0; j < dim[i]; ++j) {
                if (stringSet.isEmpty()) {
                    newStringSet.add("" + j);
                }
                else {
                    String tmp = SEPARATOR + j;
                    for (String oldString : stringSet) {
                        newStringSet.add(oldString + tmp);
                    }
                }
            }
            stringSet.clear();
            stringSet = newStringSet;
        }
        if (stringSet.isEmpty()) {
            stringSet.add("0");
        }
        return stringSet.toArray(new String[0]);
    }

    //TODO this is crazy!!!:
    int[] stringToIndexXXX(String indices) {
        String[] indexStrings = indices.split(SEPARATOR);
        int[] index = new int[indexStrings.length];
        for (int i = 0; i < index.length; ++i) {
            index[i] = Integer.parseInt(indexStrings[i]);
        }
        return index;
    }
}
