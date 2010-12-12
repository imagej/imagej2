/*
 * DisplayLiveInterface.java
 * Created on April 21, 2006, 8:58 AM
 */

package imagedisplay.stream;

import java.awt.Rectangle;

/**
 *
 * @author GBH
 */
public interface DisplayLiveInterface {
   
   public void setSize(int w, int h); // +
   public void setScale(float scale); // +
   public void fitToScreen(); // +
   
   public void suspend();
   public void resume();   
   public void setVisible(boolean visible); 
   
   public void setSelectedROI(Rectangle roi);
   public Rectangle getSelectedROI();
   public boolean isROISet();
   // ? onSetRoi(); 
   public void close();

    StreamSource getStreamSource();

    void setStreamSource(StreamSource source);
}
