package imagedisplay.stream;

//import edu.mbl.jif.utils.prefs.Prefs;
import javax.swing.SwingUtilities;
import java.awt.image.WritableRaster;


/**
 * <p>Title: </p>
 * <p>Description: Camera Streaming callback handler  </p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 *
 */


// Is now Superfluous (2009)

public class StreamHandler
{
   private boolean polView;
   private int framesWaited = 0;
   boolean waitForNextFrame = false;
   VideoFileBinary videoFile = null; // for recording the video stream
   boolean isSuspended = false;
   int framesRecorded = 0;
   Condition isRecording = new Condition(false);
   WritableRaster wr_bImage;
   int iHeight; // Image dimensions (pixels)
   int iSize; // Image dimensions (pixels)
   int iWidth; // Image dimensions (pixels)
   int frames = 0;
   int framesUpdate = 5;
   float framesPerMsec = 0;
   long updateFreq = 250;
   long xTime = 0;

   public StreamHandler () {
   } 


// Outputs
   // display
   // TIFF file
   // Movie - mov, avi.

// Sample


   //-----------------------------------------------------------
   // Suspend camera display
   public synchronized void suspend (boolean setTo) {
      isSuspended = setTo;
   }


   //-----------------------------------------------------------
   // Turn on/off PolView "realtime" retardance image
   public synchronized void setPolView (boolean t) {
      polView = t;
      System.out.println("PolView set: " + t);
   //   recordRawVideo = Prefs.usr.getBoolean("video.record.raw", false);
   }


   //=======================================================================
   //      ****************   CallBack   *********************
   //=======================================================================
   // This is the method that is called as a 'callback' from the C code
   // when the next frame in the stream is ready for display from the camera
   //
   public void callBack () {
      if (isSuspended) {
         return;
      }

      // If grabbing a sample frame for statistics, wait for next frame
      if (waitForNextFrame) {
//         if (QCamJNI.wideDepth) {
//            synchronized (QCamJNI.pixels16) {
//               if (Camera.sampleImageByte != null) {
//                  System.arraycopy(QCamJNI.pixels16, 0, Camera.sampleImageShort, 0,
//                        QCamJNI.pixels16.length);
//               }
//            }
//
//         } else {
//            synchronized (QCamJNI.pixels8) {
//               if (Camera.sampleImageByte != null) {
//                  System.arraycopy(QCamJNI.pixels8, 0, Camera.sampleImageByte, 0,
//                        QCamJNI.pixels8.length);
//               }
//            }
//         }
//         Camera.grabDone.set_true();
         waitForNextFrame = false;
         framesWaited = 0;
         return;
      }
//      if (!Camera.grabDone.is_true()) {
//         if (framesWaited > 0) {
//            waitForNextFrame = true;
//            return;
//         }
//         framesWaited++;
//      }

      // PolView...
      if (false) { //polView) {
         // wr_bImage.setDataElements(0, 0, iWidth, iHeight, PolView.polViewCalc8());
      } else {
         // put the frame in the display
//         if (QCamJNI.wideDepth) {
//            synchronized (QCamJNI.pixels16) {
//               wr_bImage.setDataElements(0, 0, iWidth, iHeight, QCamJNI.pixels16);
//            }
//            //         rOp.filter(wr_bImage, wr_bImage);
//         } else {
//            synchronized (QCamJNI.pixels8) {
//               wr_bImage.setDataElements(0, 0, iWidth, iHeight, QCamJNI.pixels8);
//               //aTx.filter(wr, wr);
//            }
//         }
      }

      // Record frame to video file...
      if (isRecording.is_true()) {
//         synchronized (videoFile) {
//            synchronized (QCamJNI.pixels8) {
//               videoFile.writeFrame((byte[]) wr_bImage.getDataElements(0, 0, iWidth,
//                     iHeight, null), System.currentTimeMillis());
//               if (recordRawVideo) {
//                  videoFile.writeFrame(QCamJNI.pixels8,
//                        System.currentTimeMillis());
//               }
//            }
//            framesRecorded++;
//         }
      }

      // periodically update status indicators
      if (true) {
         frames++;
         if (frames > framesUpdate) {
            updateFPS();
            //updateDisplayLUT();
         }
      }
      SwingUtilities.invokeLater(new Runnable()
      {
         public void run () {
//            repaint();
         }
      });

   }


   public void updateFPS () {
      // periodically measure Frames Per Second & display in frame title
      framesPerMsec = ((float) frames)
            / (float) (System.currentTimeMillis() - xTime);
      framesUpdate = (int) (framesPerMsec * (float) updateFreq);
//      if (PSj.cameraPanel != null) {
//         PSj.cameraPanel.value_FPS.setText(
//               String.valueOf((int) (framesPerMsec * 1000f)));
//      }
//      Camera.setCurrentFPS(framesPerMsec);
      //
//      updateDisplayValues();
      // reset
      xTime = System.currentTimeMillis();
      frames = 0;
      // autoExposure
   }


   //----------------------------------------------------------------
   // Video Recording...
   //
//   boolean recordRawVideo = Prefs.usr.getBoolean("video.record.raw", false);

   public void setVideoFile (VideoFileBinary vf) {
      if (vf != null) {
         framesRecorded = 0;
         videoFile = vf;
         isRecording.set_true();
      } else {
         isRecording.set_false();
         //         messageToDisplay = "";
         synchronized (videoFile) {
            videoFile = vf;
         }
      }
   }
}
