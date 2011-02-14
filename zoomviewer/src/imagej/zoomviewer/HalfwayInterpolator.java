/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.zoomviewer;

import imagej.display.zoomview.Tile;

/**
 *
 * @author Aivar Grislis
 */
public class HalfwayInterpolator {

    public Tile interpolate(int tileX, int tileY) {
        Tile dstTile = new Tile(new int[] { tileX, tileY });
        int dstBytes[] = new int[384*384];
        Tile lowResTile = null; //TODO get tile (tileX, tileY) from layer above
        int lowResBytes[] = lowResTile.getARGB();
        for (int highResTileY : new int[] { 2 * tileY, 2 * tileY + 1 } ) {
            for (int highResTileX : new int[] { 2 * tileX, 2 * tileX + 1 } ) {
                Tile highResTile = null; //TODO get tile (highResX, highResY) from layer below
                int highResBytes[] = highResTile.getARGB();
                int low_0_0 = 0;
                int low_1_0 = 0;
                int low_0_1 = 0;
                int low_1_1 = 0;
                int high_0_0 = 0;
                int high_1_0 = 0;
                int high_2_0 = 0;
                int high_3_0 = 0;
                int high_0_1 = 0;
                int high_1_1 = 0;
                int high_2_1 = 0;
                int high_3_1 = 0;
                int high_0_2 = 0;
                int high_1_2 = 0;
                int high_2_2 = 0;
                int high_3_2 = 0;
                int high_0_3 = 0;
                int high_1_3 = 0;
                int high_2_3 = 0;
                int high_3_3 = 0;
                int dst_0_0 = (low_0_0 +
                        (9 * high_0_0 + 3 * high_1_0 + 3 * high_0_1 + high_1_1) >> 4)
                        >> 1;
                int dst_1_0 = ((low_0_0 + low_1_0) >> 1 +
                        (3 * high_1_0 + 3 * high_2_0 + high_1_1 + high_2_1) >> 3)
                        >> 1;
                int dst_2_0 = (low_1_0 +
                        (3 * high_2_0 + 9 * high_3_0 + high_2_1 + 3 * high_3_1) >> 4)
                        >> 1;
                int dst_0_1 = ((low_0_0 + low_0_1) >> 1 +
                        (3 * high_0_1 + high_1_1 + 3 * high_0_2 + high_1_2) >> 3)
                        >> 1;
                int dst_1_1 = ((low_0_0 + low_1_0 + low_0_1 + low_1_1) >> 2 +
                        ((high_1_1 + high_2_1 + high_1_2 + high_2_2) >> 2)
                        >> 1);
                int dst_2_1 = ((low_1_0 + low_1_1) >> 1 +
                        ((high_2_1 + 3 * high_3_1 + high_2_2 + 3 * high_3_2) >> 3)
                        >> 1);
                int dst_0_2 = (low_0_1 +
                        (3 * high_0_2 + high_1_2 + 9 * high_0_3 + 3 * high_1_3) >> 4)
                        >> 1;
                int dst_1_2 = ((low_0_1 + low_1_1) >> 1 +
                        (high_1_2 + high_2_2 + 3 * high_1_3 + 3 * high_2_3) >> 3)
                        >> 1;
                int dst_2_2 = (low_1_1 +
                        (high_2_2 + 3 * high_3_2 + 3 * high_2_3 + 9 * high_3_3) >> 4)
                        >> 1;

            }
        }
        dstTile.setARGB(dstBytes);
        return dstTile;
    }

}
