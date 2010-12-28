package imagedisplay;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

/**
 *
 * @author GBH
 */
public interface SeriesOfImages {

    BufferedImage getAsThumbnail(int n, int sample);

    String getFilename();

    BufferedImage getImage(int n);

    Dimension getImageDimensions();

    double getInterval();

    int getNumImages();

    int getTimeIntervals();

    int getZSections();

}
