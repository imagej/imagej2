package imagedisplay.zoom;

/**
 ImageCanvas
 */
import imagedisplay.zoom.core.ZoomGraphics;
import imagedisplay.zoom.core.ZoomJPanel;
import java.awt.Color;
import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.io.IOException;
import java.net.MalformedURLException;
import java.awt.Rectangle;


public class MyJPanel
      extends ZoomJPanel
{
   // GBH: Added ROI selection functions
      Rectangle roiRect = null;
      Color roiRectColor = Color.YELLOW;

      public Rectangle getRoiRect() {
         return roiRect;
      }

   BufferedImage img = loadImage(
         //edu.mbl.jif.Constants.testDataPath +
         "images\\PSCollagenDark.gif"); //589x421

   public void paintBackground (ZoomGraphics zg) {
      this.setBackground(Color.GRAY);
      zg.drawImage(img, null, 0, 0);
   };

//   public void paintFront (ZoomGraphics zg) {
//      zg.setColor(Color.RED);
//      zg.fillRect(10, 15, 150, 20);
//      zg.setColor(Color.WHITE);
//      zg.drawString("Happy Programming", 30, 30);
////      zg.setColor(Color.WHITE);
////      zg.drawLine( -2, 0, 2, 0);
////      zg.drawLine(0, -2, 0, 2);
////      zg.drawString("this is zoom center", 10, 5);
//      //zg.drawString("haha! You Found Me :))", -100, -100);
//   };

   private BufferedImage loadImage (String fileName) {
      BufferedImage image = null;
      try {
         URL url = new URL("file:" + fileName);
         image = ImageIO.read(url);
      }
      catch (MalformedURLException mue) {
         System.err.println("malformed url for image: " + mue.getMessage());
      }
      catch (IOException ioe) {
         System.err.println("unable to read image file: " + ioe.getMessage());
      }
      return image;
   }

}
