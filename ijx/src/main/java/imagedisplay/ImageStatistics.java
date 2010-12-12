package imagedisplay;

public class ImageStatistics {
   public int   max = 0;
   public float mean = 0.0f;
   public int   min = 999999;
   public int   maxInROI = 0;
   public float meanInROI = 0.0f;
   public int   minInROI = 999999;
   public int   nSaturated = 0;

   public ImageStatistics(int max, float mean, int min, int maxInROI,
      float meanInROI, int minInROI) {
      this.max = max;
      this.mean = mean;
      this.min = min;
      this.maxInROI = maxInROI;
      this.meanInROI = meanInROI;
      this.minInROI = minInROI;
   }
}
