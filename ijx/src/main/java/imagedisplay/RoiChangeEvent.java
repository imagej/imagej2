package imagedisplay;

public class RoiChangeEvent
      extends java.util.EventObject
{

   public int x, y, w, h;
   public int max = 0;
   public float mean = 0.0f;
   public int min = 999999;

   public RoiChangeEvent (Object source, int x, int y, int w, int h, int min, float mean,
         int max) {
      super(source);
      this.x = x;
      this.y = y;
      this.w = w;
      this.h = h;
      this.min = min;
      this.max = max;
      this.mean = mean;
   }
}

