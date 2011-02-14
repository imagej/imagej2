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
public class Interpolator {
    private static final int sizeB[] = { 384, 384 };
    private static final int schemeB[][][] = {
        {
            {
                1,2
            },
            {
                3
            },
            {
                4
            }
        }
        ,
        {
            {
                1
            },
            {
                2
            },
            {
                2
            }

        },
        {
            {
                3
            },
            {
                3, 4
            },
            {
                3
            }
        }
    };
    enum Interpolation { A, B, C };
    private final Interpolation m_interpolation;
    private final int m_size[];
    private final int m_scheme[][][];

    public Interpolator(Interpolation interpolation) {
        switch (interpolation) {
            case A:
                m_size = sizeB; //TODO sizeA;
                m_scheme = schemeB; //TODO schemeA;
                break;
            case B:
                m_size = sizeB;
                m_scheme = schemeB;
                break;
            case C:
            default:
                m_size = sizeB; //TODO sizeC;
                m_scheme = schemeB; //TODO schemeC;
                break;
        }
        m_interpolation = interpolation;
    }

    public Tile doInterpolate(Tile higherRes[], Tile lowerRes) {

        // create output tile byte array
        int byteSize = m_size[0] * m_size[1] * 4; // RGBA
        byte output[] = new byte[byteSize];

        int schemeX;
        int schemeMaxX = m_scheme.length;
        int schemeY;
        int schemeMaxY = m_scheme[0].length;

        schemeY = 0;
        for (int y = 0; y < m_size[1]; ++y) {
            schemeX = 0;
            for (int x = 0; x < m_size[0]; ++x) {

                ++schemeX;
                if (schemeX > schemeMaxX) {
                    schemeX = 0;
                }

            }
            ++schemeY;
            if (schemeY > schemeMaxY) {
                schemeY = 0;
            }
        }

        return null;
    }
}
