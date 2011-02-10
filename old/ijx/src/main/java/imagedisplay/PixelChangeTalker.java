package imagedisplay;

public interface PixelChangeTalker
  {
    public  void addPixelChangeListener(PixelChangeListener listener);
    
    public  void removePixelChangeListener(PixelChangeListener listener);
    
    public  void firePixelChange(PixelChangeEvent evnt);
  }
