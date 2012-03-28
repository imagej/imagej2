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

import imagej.display.zoomview.ITileSet;
import imagej.display.zoomview.Tile;

import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.BitSet;
import java.util.UUID;

/**
 * A tile manager that manages a mip-map that is written to a file.
 *
 * @author Aivar Grislis
 */
public class FileTileManager implements ITileManager {
    private final String m_id = UUID.randomUUID().toString();
    private ITileFactory m_factory;
    private int m_level;
    private int m_width;
    private int m_height;
    private int m_tileWidth;
    private int m_tileHeight;
    private int m_bytesPerPixel;
    private int m_tileIntSize;
    private int m_tileByteSize;
    private int m_maxIndex[];
    private BitSet m_tileFlags;
    private RandomAccessFile m_randomAccessFile;

    //TODO
    // should have booleans for each tile to see if it's been written out yet
    // if a new tile is called for, scale it from higher levels.
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
        m_tileIntSize = tileWidth * tileHeight;
        m_tileByteSize = m_tileIntSize * bytesPerPixel;
        m_tileFlags = new BitSet();
        m_randomAccessFile = createTempFile();
    }

    public String getId() {
        return m_id;
    }

    /**
     * Gets the 2D dimensions of the total tiled image.
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
        if (null == m_maxIndex) {
            int maxX = m_width / m_tileWidth;
            if ((m_width % m_tileWidth) != 0) {
                ++maxX;
            }
            int maxY = m_height / m_tileHeight;
            if ((m_height % m_tileHeight) != 0) {
                ++maxY;
            }
            m_maxIndex = new int[] { maxX, maxY };
        }
        return m_maxIndex;
    }

    /**
     * Gets the dimensions of single tile.
     *
     * @return
     */
    public int[] getTileSize() {
        return new int[] { m_tileWidth, m_tileHeight };
    }

    /**
     * Get size of a pixel.
     *
     * @return
     */
    public int getBytesPerPixel() {
        return m_bytesPerPixel;
    }

    /**
     * Gets the priority for a particular tileSet.
     *
     * For now, just sets a middle priority.
     *
     * @param tileSet
     * @return
     */
    public int getPriority(ITileSet tileSet) {
        return 1;
    }

    public void putTile(ITileSet tileSet, Tile tile) {
        if (true) return;
        int bitIndex = getBitIndex(tile);
        // if this tile is not in the file cache, or it has changed
        if (!m_tileFlags.get(bitIndex) || tile.isDirty()) {
            System.out.println("writing tile level " + tileSet.getLevel() + " x " + tile.getIndex()[0] + " y " + tile.getIndex()[1]);
            try {
               // m_randomAccessFile.write(tile.getARGB(), offset(tile), 256*256*4);

              m_randomAccessFile.seek(offset(tile));
                for (int i : tile.getARGB()) {
                    m_randomAccessFile.writeInt(i);
                }

                tile.setDirty(false);
                m_tileFlags.set(bitIndex);
            }
            catch (IOException e) {
                System.out.println("Problem putting tile " + e.getMessage());
            }

        }
    }

    public Tile getTile(ITileSet tileSet, int index[]) {
        Tile tile = null;
        System.out.print("FileTileManager.getTile " + index[0] + " " + index[1]);
        if (m_tileFlags.get(getBitIndex(index))) {
            System.out.print(" from file! ");
            int ARGB[] = new int[m_tileIntSize];
            try {
                m_randomAccessFile.seek(offset(index));
                for (int i = 0; i < m_tileIntSize; ++i) {
                    ARGB[i] = m_randomAccessFile.readInt();
                }
            }
            catch (IOException e) {
                System.out.println("Problem getting tile " + e.getMessage());
            }
            tile = new Tile(index);
            tile.setARGB(ARGB);
            tile.setDirty(false);
        }
        else {
            tile = m_factory.createTile(m_level, index);
        }
        System.out.println();
        return tile;
    }

    private int getBitIndex(Tile tile) {
        return getBitIndex(tile.getIndex());
    }

    private int getBitIndex(int index[]) {
        int horzTiles = getMaxIndex()[0];
        int bitIndex = index[0] * horzTiles + index[1];
        return bitIndex;
    }

    private RandomAccessFile createTempFile() {
        File file = null;
        try {
            // Create temp file.
            file = File.createTempFile("pattern", ".suffix");
            System.out.println("temp file is " + file.getAbsolutePath());

            // Delete temp file when program exits.
            file.deleteOnExit();

        } catch (IOException e) {
            System.out.println("Problem creating temporary file " + e.getMessage());
        }
        RandomAccessFile randomAccessFile = null;
        if (null != file) {
            try {
                randomAccessFile = new RandomAccessFile(file, "rw");
            }
            catch (FileNotFoundException e) {
                System.out.println("Problem with temporary file " + e.getMessage());
            }
        }
        return randomAccessFile;
    }

    private long offset(Tile tile) {
        return offset(tile.getIndex());
    }

    private long offset(int index[]) {
        long offset = 0;
        int maxIndex[] = getMaxIndex();
        // skip to row
        offset += index[1] * maxIndex[0] * m_tileByteSize;
        // skip to column
        offset += index[0] * m_tileByteSize;
        return offset;
    }
}
