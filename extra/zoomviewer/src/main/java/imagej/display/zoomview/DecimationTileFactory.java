/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.display.zoomview;

/**
 *
 * @author aivar
 */
public class DecimationTileFactory implements ITileFactory {
    int counter = 0;
    ZoomView m_zoomView;

    public void init(ZoomView zoomView) {
        m_zoomView = zoomView;
    }

    /**
     * Creates a tile on demand.  Index array is organized as follows:
     * <p>
     * index[0] is x index of tile<p>
     * index[1] is y index of tile<p>
     * remainder of index[] if any specifies further dimensions.
     *
     * @param level
     * @param index
     * @return
     */
    //TODO hardcoding 128 & 256 == bad!!
    public Tile createTile(int level, int index[]) {
        final int tileX = index[0];
        final int tileY = index[1];
        final int hiResLevel = level - 1;
        assert(hiResLevel > 0);

        // get upper left tile from higher resolution mipmap level
        int hiResIndex[] = new int[index.length];
        hiResIndex[0] = 2 * tileX;
        hiResIndex[1] = 2 * tileY;
        for (int i = 2; i < index.length; ++i) {
            hiResIndex[i] = index[i];
        }
        int hiResTileARGB[] = m_zoomView.getTile(hiResLevel, hiResIndex).getARGB();

        final int result[] = new int[hiResTileARGB.length];

        for (int y = 0; y < 128; ++y) {
            for (int x = 0; x < 128; ++x) {
                result[y * 256 + x] = hiResTileARGB[2 * y * 256 + 2 * x]; // (2x, 2y)
            }
        }

        // get upper right tile from higher resolution mipmap level
        hiResIndex = new int[index.length];
        hiResIndex[0] = 2 * tileX + 1;
        hiResIndex[1] = 2 * tileY;
        for (int i = 2; i < index.length; ++i) {
            hiResIndex[i] = index[i];
        }
        hiResTileARGB = m_zoomView.getTile(hiResLevel, hiResIndex).getARGB();

        for (int y = 0; y < 128; ++y) {
            for (int x = 0; x < 128; ++x) {
                result[y * 256 + x + 128] = hiResTileARGB[2 * y * 256 + 2 * x]; // (2x, 2y)
            }
        }

        // get lower left tile from higher resolution mipmap level
        hiResIndex = new int[index.length];
        hiResIndex[0] = 2 * tileX;
        hiResIndex[1] = 2 * tileY + 1;
        for (int i = 2; i < index.length; ++i) {
            hiResIndex[i] = index[i];
        }
        hiResTileARGB = m_zoomView.getTile(hiResLevel, hiResIndex).getARGB();

        for (int y = 0; y < 128; ++y) {
            for (int x = 0; x < 128; ++x) {
                result[(y + 128) * 256 + x] = hiResTileARGB[2 * y * 256 + 2 * x]; // (2x, 2y)
            }
        }

        // get lower right tile from higher resolution mipmap level
        hiResIndex = new int[index.length];
        hiResIndex[0] = 2 * tileX + 1;
        hiResIndex[1] = 2 * tileY + 1;
        for (int i = 2; i < index.length; ++i) {
            hiResIndex[i] = index[i];
        }
        hiResTileARGB = m_zoomView.getTile(hiResLevel, hiResIndex).getARGB();

        for (int y = 0; y < 128; ++y) {
            for (int x = 0; x < 128; ++x) {
                result[(y + 128) * 256 + x + 128] = hiResTileARGB[2 * y * 256 + 2 * x]; // (2x, 2y)
            }
        }

        Tile tile = new Tile(index);
        tile.setARGB(result);

        return tile;
    }
}
