package imagedisplay;

import java.awt.Dimension;
import java.awt.Rectangle;

public class ImageAnalyzer
{

   static int numberFrames = 0;
   static int frame = 0;
   static float[] data;
   static long sum = 0;
   static int imageSize = 0;
   static int max = 0;
   static float mean = 0.0f;
   static int min = 99999;
   static int maxInROI = 0;
   static float meanInROI = 0.0f;
   static int minInROI = 99999;
   static int nSaturated = 0;
   ImageStatistics stats;

   public static ImageStatistics getStats (Object array, Rectangle roi, Dimension frame) {
      if(array == null) return null;
      if (array instanceof byte[]) {
         return getStats((byte[]) array, roi, frame);
      } else if (array instanceof short[]) {
         return getStats((short[]) array, roi, frame);
      } else if (array instanceof int[]) {
         return getStats((int[]) array, roi, frame);
      } else {         return null;
      }
   }


   // for Byte array
   public static ImageStatistics getStats (byte[] array, Rectangle roi, Dimension frame) {
      if(array == null) return null;
      int x = roi.x;
      int y = roi.y;
      int w = roi.width;
      int h = roi.height;
      int frameW = frame.width;
      int frameH = frame.height;
      int numPixels = w * h;
      float average = 0;
      int sum = 0;
      //
      if ((w == 0) || (h == 0)) {
         maxInROI = -1;
         meanInROI = -1.0f;
         minInROI = -1;
      } else {
         maxInROI = 0;
         meanInROI = 0.0f;
         minInROI = 99999;
         int xFinal = x + w;
         int yFinal = y + h;
         for (int nY = y; nY < yFinal; nY++) {
            for (int nX = x; nX < xFinal; nX++) {
               int offset = (frameW * nY) + nX;
               sum = sum + (array[offset] & 0xff);
               if ((array[offset] & 0xff) > maxInROI) {
                  maxInROI = (int) (array[(frameW * nY) + nX] & 0xff);
               }
               if ((array[offset] & 0xff) < minInROI) {
                  minInROI = (int) (array[(frameW * nY) + nX] & 0xff);
               }
            }
         }
         try {
            meanInROI = (float) sum / (float) numPixels;
         }
         catch (Exception ex) {}
      }

      //
      max = 0;
      mean = 0.0f;
      min = 99999;
      nSaturated = 0;
      sum = 0;
      for (int i = 0; i < array.length; i++) {
         sum = sum + (int) (array[i] & 0xff);
         if ((array[i] & 0xff) > max) {
            max = array[i] & 0xff;
         }
         if ((array[i] & 0xff) < min) {
            min = array[i] & 0xff;
         }
         if ((array[i] & 0xff) < min) {
            min = array[i] & 0xff;
         }
      }
      try {
         mean = (float) sum / (float) array.length;
      }
      catch (Exception ex) {}
      return new ImageStatistics(max, mean, min, maxInROI, meanInROI, minInROI);
   }


   // for short array
   public static ImageStatistics getStats (short[] array, Rectangle roi, Dimension frame) {
      if(array == null) return null;
      int x = roi.x;
      int y = roi.y;
      int w = roi.width;
      int h = roi.height;
      int frameW = frame.width;
      int frameH = frame.height;
      int numPixels = w * h;
      float average = 0;
      int sum = 0;
      //
      if ((w == 0) || (h == 0)) {
         maxInROI = -1;
         meanInROI = -1.0f;
         minInROI = -1;
      } else {
         maxInROI = 0;
         meanInROI = 0.0f;
         minInROI = 99999;
         int xFinal = x + w;
         int yFinal = y + h;
         for (int nY = y; nY < yFinal; nY++) {
            for (int nX = x; nX < xFinal; nX++) {
               int offset = (frameW * nY) + nX;
               sum = sum + (array[offset] & 0xffff);
               if ((array[offset] & 0xffff) > maxInROI) {
                  maxInROI = (int) (array[(frameW * nY) + nX] & 0xffff);
               }
               if ((array[offset] & 0xffff) < minInROI) {
                  minInROI = (int) (array[(frameW * nY) + nX] & 0xffff);
               }
            }
         }
         try {
            meanInROI = (float) sum / (float) numPixels;
         }
         catch (Exception ex) {}
      }

      //
      max = 0;
      mean = 0.0f;
      min = 99999;
      nSaturated = 0;
      sum = 0;
      for (int i = 0; i < array.length; i++) {
         sum = sum + (int) (array[i] & 0xffff);
         if ((array[i] & 0xffff) > max) {
            max = array[i] & 0xffff;
         }
         if ((array[i] & 0xffff) < min) {
            min = array[i] & 0xffff;
         }
         if ((array[i] & 0xffff) < min) {
            min = array[i] & 0xffff;
         }
      }
      try {
         mean = (float) sum / (float) array.length;
      }
      catch (Exception ex) {}
      return new ImageStatistics(max, mean, min, maxInROI, meanInROI, minInROI);
   }


// for an Integer array
   public static ImageStatistics getStats (int[] array, Rectangle roi,
         Dimension frame) {
         if(array == null) return null;
         if(frame == null) return null;
      int x = roi.x;
      int y = roi.y;
      int w = roi.width;
      int h = roi.height;
      int frameW = frame.width;
      int frameH = frame.height;
      int numPixels = w * h;
      float average = 0;
      int sum = 0;

      //
      if ((w == 0) || (h == 0)) {
         maxInROI = -1;
         meanInROI = -1.0f;
         minInROI = -1;
      } else {
         maxInROI = 0;
         meanInROI = 0.0f;
         minInROI = 99999;
         int xFinal = x + w;
         int yFinal = y + h;
         for (int nY = y; nY < yFinal; nY++) {
            for (int nX = x; nX < xFinal; nX++) {
               int offset = (frameW * nY) + nX;
               sum = sum + (array[offset] & 0xff);
               if ((array[offset] & 0xff) > maxInROI) {
                  maxInROI = (int) (array[(frameW * nY) + nX] & 0xff);
               }
               if ((array[offset] & 0xff) < minInROI) {
                  minInROI = (int) (array[(frameW * nY) + nX] & 0xff);
               }
            }
         }
         try {
            meanInROI = (float) sum / (float) numPixels;
         }
         catch (Exception ex) {}
      }

      //
      max = 0;
      mean = 0.0f;
      min = 99999;
      sum = 0;
      for (int i = 0; i < array.length; i++) {
         sum = sum + (int) (array[i] & 0xff);
         if ((array[i] & 0xff) > max) {
            max = array[i] & 0xff;
         }
         if ((array[i] & 0xff) < min) {
            min = array[i] & 0xff;
         }
      }
      try {
         mean = (float) sum / (float) array.length;
      }
      catch (Exception ex) {}
      return new ImageStatistics(max, mean, min, maxInROI, meanInROI, minInROI);
   }


   //   public static ImageStatistics getStats(int[] array, Rectangle roi,
   //      Dimension frame) {
   //      int   x = roi.x;
   //      int   y = roi.y;
   //      int   w = roi.width;
   //      int   h = roi.height;
   //      int   xFinal = x + w;
   //      int   yFinal = y + h;
   //      int   frameW = frame.width;
   //      int   frameH = frame.height;
   //      int   numPixels = w * h;
   //      float average = 0;
   //      int   sum = 0;
   //
   //      //
   //      maxInROI = 0;
   //      meanInROI = 0.0f;
   //      minInROI = 999999;
   //      for (int nY = y; nY < yFinal; nY++) {
   //         for (int nX = x; nX < xFinal; nX++) {
   //            sum = sum + array[(frameW * nY) + nX];
   //            if (array[(frameW * nY) + nX] > max) {
   //               maxInROI = array[(frameW * nY) + nX];
   //            }
   //            if (array[(frameW * nY) + nX] < min) {
   //               minInROI = array[(frameW * nY) + nX];
   //            }
   //         }
   //      }
   //      try {
   //         meanInROI = (float) sum / (float) numPixels;
   //      } catch (Exception ex) {}
   //
   //      //
   //      max = 0;
   //      mean = 0.0f;
   //      min = 999999;
   //      sum = 0;
   //      for (int i = 0; i < array.length; i++) {
   //         sum = (sum + array[i]);
   //         if ((array[i]) > max)
   //            max = array[i];
   //         if ((array[i]) < min)
   //            min = array[i];
   //         if ((array[i]) > max)
   //            max = array[i];
   //      }
   //      try {
   //         mean = (float) sum / (float) numPixels;
   //      } catch (Exception ex) {}
   //
   //      //
   //      return new ImageStatistics(max, mean, min, maxInROI, meanInROI, minInROI);
   //   }
   public static ImageStatistics getStats (int[] array) {
            if(array == null) return null;
      float average = 0;
      int sum = 0;
      max = 0;
      mean = 0.0f;
      min = 999999;
      for (int i = 0; i < array.length; i++) {
         sum = (sum + array[i]);
         if ((array[i]) > max) {
            max = array[i];
         }
         if ((array[i]) < min) {
            min = array[i];
         }
         if ((array[i]) > max) {
            max = array[i];
         }
      }
      try {
         mean = (float) sum / (float) array.length;
      }
      catch (Exception ex) {}

      //
      return new ImageStatistics(max, mean, min, maxInROI, meanInROI, minInROI);
   }
}
