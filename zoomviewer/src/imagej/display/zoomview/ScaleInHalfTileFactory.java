/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.display.zoomview;

/**
 * Creates a tile for a level of the mipmap by averaging down four higher
 * resolution tiles.
 *
 * @author aivar
 */
public class ScaleInHalfTileFactory implements ITileFactory {
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
        System.out.println("SCALE TILE " + level + " " + index[0] + " " + index[1]);
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
                final int argb1 = hiResTileARGB[2 * y * 256 + 2 * x]; // (2x, 2y)
                final int argb2 = hiResTileARGB[2 * y * 256 + 2 * x + 1]; // (2x+1, 2y)
                final int argb3 = hiResTileARGB[(2 * y + 1) * 256 + 2 * x]; // (2x, 2y+1)
                final int argb4 = hiResTileARGB[(2 * y + 1) * 256 + 2 * x + 1]; // (2x+1, 2y+1)
                result[y * 256 + x] = average(argb1, argb2, argb3, argb4); // (x, y)
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
                final int argb1 = hiResTileARGB[2 * y * 256 + 2 * x]; // (2x, 2y)
                final int argb2 = hiResTileARGB[2 * y * 256 + 2 * x + 1]; // (2x+1, 2y)
                final int argb3 = hiResTileARGB[(2 * y + 1) * 256 + 2 * x]; // (2x, 2y+1)
                final int argb4 = hiResTileARGB[(2 * y + 1) * 256 + 2 * x + 1]; // (2x+1, 2y+1)
                result[y * 256 + x + 128] = average(argb1, argb2, argb3, argb4); // (x, y)
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
                final int argb1 = hiResTileARGB[2 * y * 256 + 2 * x]; // (2x, 2y)
                final int argb2 = hiResTileARGB[2 * y * 256 + 2 * x + 1]; // (2x+1, 2y)
                final int argb3 = hiResTileARGB[(2 * y + 1) * 256 + 2 * x]; // (2x, 2y+1)
                final int argb4 = hiResTileARGB[(2 * y + 1) * 256 + 2 * x + 1]; // (2x+1, 2y+1)
                result[(y + 128) * 256 + x] = average(argb1, argb2, argb3, argb4); // (x, y)
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
                final int argb1 = hiResTileARGB[2 * y * 256 + 2 * x]; // (2x, 2y)
                final int argb2 = hiResTileARGB[2 * y * 256 + 2 * x + 1]; // (2x+1, 2y)
                final int argb3 = hiResTileARGB[(2 * y + 1) * 256 + 2 * x]; // (2x, 2y+1)
                final int argb4 = hiResTileARGB[(2 * y + 1) * 256 + 2 * x + 1]; // (2x+1, 2y+1)
                result[(y + 128) * 256 + x + 128] = average(argb1, argb2, argb3, argb4); // (x, y)
            }
        }

        Tile tile = new Tile(index);
        tile.setARGB(result);
        
        return tile;
    }

    int average(final int argb1, final int argb2, final int argb3, final int argb4) {
        final int a = (getA(argb1) + getA(argb2) + getA(argb3) + getA(argb4)) >> 2;
        final int r = (getR(argb1) + getR(argb2) + getR(argb3) + getR(argb4)) >> 2;
        final int g = (getG(argb1) + getG(argb2) + getG(argb3) + getG(argb4)) >> 2;
        final int b = (getB(argb1) + getB(argb2) + getB(argb3) + getB(argb4)) >> 2;
        int result = (r << 16) | (g << 8) | b; //TODO transparency goofs things up
        result |= 0xff000000;
        return result;
    }

    int getA(final int ARGB) {
        return (ARGB & 0xff000000) >> 24;
    }

    int getR(final int ARGB) {
        return (ARGB & 0xff0000) >> 16;
    }

    int getG(final int ARGB) {
        return (ARGB & 0xff00) >> 8;
    }

    int getB(final int ARGB) {
        return ARGB & 0xff;
    }
}
