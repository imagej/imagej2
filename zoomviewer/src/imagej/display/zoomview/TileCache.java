/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.display.zoomview;

import imagej.display.zoomview.ITileSet;
import imagej.display.zoomview.Tile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Aivar Grislis
 */
public class TileCache {
    private int m_cacheSpace;
    private List<ITileSet> m_tileSets = new ArrayList<ITileSet>();

    /**
     * Starts up a tile cache with a given cache size.
     *
     * @param cacheSpace cache size in megabytes
     */
    public TileCache(int cacheMegabytes) {
        m_cacheSpace = cacheMegabytes * 1024 * 1024;
    }

    /**
     * Adds a tile set.  This tile set will have no cached tiles.
     *
     * @param tileSet
     */
    public void addTileSet(ITileSet tileSet) {
        System.out.println("=============== ADD TILE SET =========");
        m_tileSets.add(tileSet);
    }

    /**
     * Deletes a tile set from the cache.
     *
     * @param tileSet
     */
    public void deleteTileSet(ITileSet tileSet) {
        m_tileSets.remove(tileSet);
        tileSet.flush();
    }

    public void putTile(ITileSet tileSet, Tile tile) {
        // make sure we have enough room in cache
        freeUpSpace(tileSet.getBytesPerTile());

        // save tile in set cache
        tileSet.putTile(tile);
    }

    public Tile getTile(ITileSet tileSet, int index[]) {
        //System.out.print("get tile " + index[0] + " " + index[1]);
        // look for the tile in cache
        Tile tile = tileSet.getCachedTile(index);
        if (null == tile) {
            // make sure we have enough room in cache
            freeUpSpace(tileSet.getBytesPerTile());

            System.out.print(" not in cache");

            // get the tile (and cache it)
            tile = tileSet.getUncachedTile(index);
        }
        return tile;
    }

    /**
     * Calculates the total cache space used by all the tile sets.
     *
     * @return total cache space used
     */
    private int totalSpaceUsed() {
        int space = 0;
        // go through all the tile sets
        for (ITileSet tileSet : m_tileSets) {
            space += tileSet.getBytesUsed();
        }
        return space;
    }

    /**
     * Frees up cache space.
     *
     * @param spaceNeeded number of bytes needed
     */
    private void freeUpSpace(int spaceNeeded) {
        System.out.println("freeUpSpace " + spaceNeeded);
        boolean noLoop = true;
        while (spaceNeeded > 0 && spaceNeeded > m_cacheSpace - totalSpaceUsed()) {
            noLoop = false;
            System.out.println(" freeUpSpace loop: m_cacheSpace " + m_cacheSpace + " totalSpaceUsed " + totalSpaceUsed() + " spaceNEeded " + spaceNeeded);
            // set impossible values for priority
            int lowestPriority = Integer.MAX_VALUE;
            ITileSet lowestPriorityTileSet = null;

            // go through all the tile sets
            for (ITileSet tileSet : m_tileSets) {
                if (tileSet.getBytesUsed() > 0) {
                    if (tileSet.getPriority() < lowestPriority) {
                        // good candidate tile set to free up space
                        lowestPriority = tileSet.getPriority();
                        lowestPriorityTileSet = tileSet;
                    }
                }
            }
            // couldn't find a tile set with cached space to free
            if (null == lowestPriorityTileSet) {
                // shouldn't happen
                System.out.println("in TileCache.freeUpSpace, can't find appropriate tile set.");
                break;
            }
            // request to free up space
            int initialSpace = lowestPriorityTileSet.getBytesUsed();
            lowestPriorityTileSet.freeUpCache(spaceNeeded);
            spaceNeeded -= (initialSpace - lowestPriorityTileSet.getBytesUsed());
        }
        if (noLoop)
          System.out.println("Cache is OK: m_cacheSpace " + m_cacheSpace + " totalSpaceUsed " + totalSpaceUsed() + " spaceNEeded " + spaceNeeded);
    }
}
