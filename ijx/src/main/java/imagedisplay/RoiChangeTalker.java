package imagedisplay;

public interface RoiChangeTalker
  {
    public  void addRoiChangeListener(RoiChangeListener listener);
    
    public  void removeRoiChangeListener(RoiChangeListener listener);
    
    public  void fireRoiChange(RoiChangeEvent evnt);
  }
