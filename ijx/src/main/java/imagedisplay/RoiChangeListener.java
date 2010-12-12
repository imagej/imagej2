package imagedisplay;

import java.util.EventListener;

public interface RoiChangeListener extends EventListener
  {
    public void roiChanged(RoiChangeEvent evnt);
  }
