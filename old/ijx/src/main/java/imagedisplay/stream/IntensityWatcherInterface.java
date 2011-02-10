/*
 * IntensityWatcherInterface.java
 * Created on March 8, 2007, 5:16 PM
 */

package imagedisplay.stream;

import imagedisplay.PixelChangeListener;
import imagedisplay.RoiChangeListener;


/**
 * @author GBH
 */
public interface IntensityWatcherInterface extends PixelChangeListener, RoiChangeListener {
    
    float getMean();

    void setMeasurementFreq(int freq);

}
