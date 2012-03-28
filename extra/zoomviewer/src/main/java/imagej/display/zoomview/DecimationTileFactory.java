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
