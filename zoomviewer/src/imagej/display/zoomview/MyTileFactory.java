/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
