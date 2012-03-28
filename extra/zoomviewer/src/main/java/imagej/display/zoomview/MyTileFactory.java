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

import java.awt.Color;
import java.io.File;

/**
 *
 * @author aivar
 */
public class MyTileFactory implements ITileFactory {
    File m_path;

    MyTileFactory(File path) {
        m_path = path;
    }

    public void init(ZoomView zoomView) {
        //TODO don't care
    }

    public Tile createTile(int level, int index[]) {
        int x = index[0];
        int y = index[1];
        Tile tile = new Tile(index);
        int[] ARGB = new int[256*256];
        //TODO for now, just make a checkerboard
        if (x % 2 ==  y % 2) {
            System.out.println("createTile x y " + x + " " + y + " YELLOW");
            fillARGB(ARGB, Color.YELLOW);
        }
        else {
            System.out.println("createTile BLUE");
            fillARGB(ARGB, Color.BLUE);
        }
        tile.setARGB(ARGB);
        return tile;
    }

    private void fillARGB(int[] ARGB, Color color) {
        for (int i = 0; i < ARGB.length; ++i) {
            ARGB[i] = color.getRGB();
        }
    }
}
