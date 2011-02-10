package imagedisplay;

import java.util.EventListener;

public interface PixelChangeListener extends EventListener
  {
    public void pixelChanged(PixelChangeEvent evnt);
  }
