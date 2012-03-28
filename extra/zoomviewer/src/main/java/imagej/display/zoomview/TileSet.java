/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.display.zoomview;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Describes a set of tiles, could be image data or some zoom level.
 *
 * @author Aivar Grislis
 */
public class TileSet implements ITileSet {
    private final String m_id = UUID.randomUUID().toString();
    private ITileManager m_manager;
    private Dimension m_dimension;
    private int m_maxIndex[];
    private int m_tileSize[];
    private int m_bytesPerPixel;
    private int m_pixelsPerTile;
    private int m_bytesPerTile;
    //TODO zzz private Map<String, Tile> m_tileMap = new HashMap<String, Tile>();
    private Tile m_tile[][];
    int m_tileCount;
    private int[] m_blankARGB = new int[256*256]; //TODO init to zeros or Color.BLACK
    int m_level;

    /**
     * Creates a holder for a set of tiles.  Note that this assumes a fixed size
     * for the tiles and the pixels.
     * 
     * @param manager
     * @param maxIndices gives number of dimensions and extents
     * @param tileSize extents of the tile in each dimension
     * @param bytesPerPixel size of a pixel in bytes
     */
    public void init(ITileManager manager) {
        m_manager = manager;
        m_dimension = manager.getDimension(); //TODO tilesets s/b nd
        m_maxIndex = manager.getMaxIndex();
        int maxIndexX = m_maxIndex[0] + 1;
        int maxIndexY = m_maxIndex[1] + 1;
        System.out.println("Tile set size is " + maxIndexX + " " + maxIndexY);
        m_tile = new Tile[maxIndexX][maxIndexY];
        for (int y = 0; y < maxIndexY; ++y) {
            for (int x = 0; x < maxIndexX; ++x) {
                m_tile[x][y] = null;
            }
        }
        m_tileCount = 0;
        m_bytesPerPixel = manager.getBytesPerPixel();
        m_tileSize = manager.getTileSize();
        m_pixelsPerTile = 1;
        for (int size : m_tileSize) {
            m_pixelsPerTile *= size;
        }
        m_bytesPerTile = m_bytesPerPixel * m_pixelsPerTile;
    }

    /**
     * Gets a unique identifier.
     *
     * @return
     */
    public String getId() {
        return m_id;
    }
    //TODO level s/n/b a concern of a tileset
    public int getLevel() { return m_level; }
    public void setLevel(int level) { m_level = level; }

    /**
     * Gets the dimensions of the tiled image.
     * @return
     */
    public Dimension getDimension() {
        return m_dimension;
    }

    /**
     * Gets the maximum tile indices.
     *
     * @return
     */
    public int[] getMaxIndex() {
        return m_maxIndex;
    }

    /**
     * Gets the size of the tile.
     *
     * @return
     */
    public int[] getTileSize() {
        return null;
    }

    /**
     * Gets the size of the tile in pixels.
     *
     * @return
     */
    public int getPixelsPerTile() {
        return m_pixelsPerTile;
    }

    /**
     * Gets the size of a pixel in bytes.
     *
     * @return
     */
    public int getBytesPerPixel() {
        return m_bytesPerPixel;
    }

    /**
     * Gets the amount of space used by one tile.
     *
     * @return cache space needed for one tile, in bytes
     */
    public int getBytesPerTile() {
        return m_bytesPerTile;
    }

    /**
     * Gets the amount of space used by all the cached tiles.
     *
     * @return bytes cached
     */
    public int getBytesUsed() {
        return m_tileCount * m_bytesPerTile;
    }

    /**
     * Frees up some cache space.
     *
     * @param bytes space needed in bytes
     */
    public void freeUpCache(int bytes) {
        int tiles = (bytes + m_bytesPerTile - 1) / m_bytesPerTile;
       //TODO System.out.println("free up " + bytes + " tiles " + tiles);
        for (int y = 0; y < m_maxIndex[1]; ++y) {
            for (int x = 0; x < m_maxIndex[0]; ++x) {
                if (null != m_tile[x][y]) {
                    // free this tile
                    Tile tile = m_tile[x][y];
                    m_tile[x][y] = null;
                    if (tile.isDirty()) {
                        m_manager.putTile(this, tile);
                    }
                    --m_tileCount;
                    if (--tiles == 0) {
                        return;
                    }
                }
            }
        }
    }

    /**
     * Gets priority of this set.
     *
     * @return priority, zero is lowest
     */
    public int getPriority(){
        return m_manager.getPriority(this);
    }


    /**
     * Puts a tile to the cache.
     *
     * @param tile
     */
    public void putTile(Tile tile) {
        if (true) throw new RuntimeException("PUT TILE IS CURRENTLY NOT USED");
        int x = tile.getIndex()[0];
        int y = tile.getIndex()[1];
        if (x <= m_maxIndex[0] && y <= m_maxIndex[1]) {
            m_tile[x][y] = tile;
            ++m_tileCount;
            System.out.println("PUT TILE BUMP COUNT");
        }
       //TODO zzz int indices[] = tile.getIndex();
        //TODO zzz String indicesString = buildIndicesString(indices);
        //System.out.println("put to map key " + indicesString + " tile " + tile);
        //TODO zzz m_tileMap.put(indicesString, tile);
    }

    /**
     * Gets a tile if cached for this set.
     *
     * @param index indices of this tile
     * @return null or tile
     */
    public Tile getCachedTile(int index[]) {
        //System.out.println("get cached key " + buildIndicesString(index));
        //TODO zzz return m_tileMap.get(buildIndicesString(index));
        Tile tile = null;
        int x = index[0];
        int y = index[1];
        if (x <= m_maxIndex[0] && y <= m_maxIndex[1]) {
            tile = m_tile[x][y];
           //TODO System.out.println("GOT A CACHED TILE");
        }
        else {
            System.out.println("returning blank tile at " + x + " " + y);
            tile = blankTile(index);
        }
        return tile;
    }

    /**
     * Gets a tile that is not in the cache for this set.
     *
     * @param indices
     * @return non-null tile
     */
    public Tile getUncachedTile(int index[]) {
        int x = index[0];
        int y = index[1];
        Tile tile = m_manager.getTile(this, index);
        //TODO zzz String indicesString = buildIndicesString(index);
        //System.out.println("getUncachedTile put to map key " + indicesString + " tile " + tile);
        //TODO zzz m_tileMap.put(indicesString, tile);
        m_tile[index[0]][index[1]] = tile;
        if (x != tile.getIndex()[0] || y != tile.getIndex()[1]) {
            System.out.println("\nMISMATCHED!!!!\n" + index[0] + " " + index[1] + "!="+ tile.getIndex()[0] + " " + tile.getIndex()[1]);
        }
        ++m_tileCount;
        return tile;
    }

    /**
     * Empties the cache.
     */
    public void flush() {
        //TODO zzz
        /*
        for (Tile tile : m_tileMap.values()) {
            m_manager.putTile(this, tile);
        }*/
    }

    /**
     * Builds a unique string with the indices.
     *
     * @param indices, i.e. { 0, 1, 2 }
     * @return string, i.e. "0_1_2_"
     */
    private String buildIndicesString(int index[]) {
        StringBuilder indicesString = new StringBuilder();
        for (int i : index) {
              indicesString.append(i);
              indicesString.append('-');
        }
        return indicesString.toString();
    }

    //TODO stuck here for now; note hardcoded to 256
    Tile blankTile(int[] index) {
        Tile tile = new Tile(index);
        tile.setARGB(m_blankARGB);
        return tile;
    }
}
