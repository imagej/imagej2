package ij.plugin;
import ij.*;
import ij.io.*;
import ij.process.*;
import ijx.IjxImagePlus;
import java.awt.*;
import java.io.*;
import java.awt.image.*;

/** Implements the File/Save As/BMP command. Based on BMPFile class from
   http://www.javaworld.com/javaworld/javatips/jw-javatip60-p2.html */

public class BMP_Writer implements PlugIn {
 //--- Private constants
 private final static int BITMAPFILEHEADER_SIZE = 14;
 private final static int BITMAPINFOHEADER_SIZE = 40;
 //--- Private variable declaration
 //--- Bitmap file header
 private byte bitmapFileHeader [] = new byte [14];
 private byte bfType [] =  {(byte)'B', (byte)'M'};
 private int bfSize = 0;
 private int bfReserved1 = 0;
 private int bfReserved2 = 0;
 private int bfOffBits = BITMAPFILEHEADER_SIZE + BITMAPINFOHEADER_SIZE;
 //--- Bitmap info header
 private byte bitmapInfoHeader [] = new byte [40];
 private int biSize = BITMAPINFOHEADER_SIZE;
 private int biWidth = 0;
 private int padWidth = 0;
 private int biHeight = 0;
 private int biPlanes = 1;
 private int biBitCount = 24;
 private int biCompression = 0;
 private int biSizeImage = 0;
 private int biXPelsPerMeter = 0x0;
 private int biYPelsPerMeter = 0x0;
 private int biClrUsed = 0;
 private int biClrImportant = 0;
 //--- Bitmap raw data
 private int intBitmap [];
 private byte byteBitmap [];
 //--- File section
 private FileOutputStream fo;
 private BufferedOutputStream bfo;
 IjxImagePlus imp;

 public void run(String path) {
   IJ.showProgress(0);
   imp = WindowManager.getCurrentImage();
   if (imp==null)
     {IJ.noImage(); return;}
   try {
     writeImage(imp, path);
   } catch (Exception e) {
     String msg = e.getMessage();
     if (msg==null || msg.equals(""))
   msg = ""+e;
     IJ.error("BMP Writer", "An error occured writing the file.\n \n" + msg);
   }
   IJ.showProgress(1);
   IJ.showStatus("");
 }

 void writeImage(IjxImagePlus imp, String path) throws Exception {
   if(imp.getBitDepth()==24)
     biBitCount = 24;
   else {
     biBitCount = 8;
     LookUpTable lut = imp.createLut();
     biClrUsed=lut.getMapSize(); // 8 bit color image may use less
     bfOffBits+=biClrUsed*4;
   }
   if (path==null || path.equals("")) {
     String prompt = "Save as " + biBitCount + " bit BMP";
     SaveDialog sd = new SaveDialog(prompt, imp.getTitle(), ".bmp");
     if(sd.getFileName()==null)
   return;
     path = sd.getDirectory()+sd.getFileName();
   }
   imp.startTiming();
   saveBitmap (path, imp.getImage(), imp.getWidth(), imp.getHeight() );
 }


 public void saveBitmap (String parFilename, Image parImage, int parWidth, int parHeight) throws Exception {
   fo = new FileOutputStream (parFilename);
   bfo = new BufferedOutputStream(fo);
   save (parImage, parWidth, parHeight);
   bfo.close();
   fo.close ();
 }

 /*
  *   The saveMethod is the main method of the process. This method
  *   will call the convertImage method to convert the memory image to
  *   a byte array; method writeBitmapFileHeader creates and writes
  *   the bitmap file header; writeBitmapInfoHeader creates the
  *   information header; and writeBitmap writes the image.
  *
  */
 private void save (Image parImage, int parWidth, int parHeight) throws Exception {
   convertImage (parImage, parWidth, parHeight);
   writeBitmapFileHeader ();
   writeBitmapInfoHeader ();
   if(biBitCount == 8)
     writeBitmapPalette ();
   writeBitmap ();
 }

 private void writeBitmapPalette() throws Exception {
   LookUpTable lut = imp.createLut();
   byte[] g = lut.getGreens();
   byte[] r = lut.getReds();
   byte[] b = lut.getBlues();
   for(int i = 0;i<lut.getMapSize();i++) {
     bfo.write(b[i]);
     bfo.write(g[i]);
     bfo.write(r[i]);
     bfo.write(0x00);
   }
 }

 /*
  * convertImage converts the memory image to the bitmap format (BRG).
  * It also computes some information for the bitmap info header.
  *
  */
 private boolean convertImage (Image parImage, int parWidth, int parHeight) {
   int pad;
   if(biBitCount == 24)
     intBitmap = (int[]) imp.getProcessor().getPixels();
   else
     byteBitmap = (byte[]) imp.getProcessor().convertToByte(true).getPixels();
   biWidth = parWidth;
   biHeight = parHeight;
   if(biBitCount==24)
     pad = 4 - ((biWidth * 3) % 4);
   else
     pad = 4 - ((biWidth) % 4);
   if (pad == 4)       // <==== Bug correction
     pad = 0;            // <==== Bug correction
   padWidth = biWidth*(biBitCount==24?3:1)+pad;
   return (true);
 }

 /*
  * writeBitmap converts the image returned from the pixel grabber to
  * the format required. Remember: scan lines are inverted in
  * a bitmap file!
  *
  * Each scan line must be padded to an even 4-byte boundary.
  */
 private void writeBitmap () throws Exception {
   int value;
   int i;
   int pad;
   byte rgb [] = new byte [3];
   if(biBitCount==24)
     pad = 4 - ((biWidth * 3) % 4);
   else
     pad = 4 - ((biWidth) % 4);
   if (pad == 4)       // <==== Bug correction
     pad = 0;            // <==== Bug correction

   int counter=0;
   for(int row = biHeight; row>0; row--) {
     if (row%20==0)
   IJ.showProgress((double)(biHeight-row)/biHeight);
     for(int col = 0; col<biWidth; col++) {
   if(biBitCount==24) {
     value = intBitmap [(row-1)*biWidth + col ];
     rgb [0] = (byte) (value & 0xFF);
     rgb [1] = (byte) ((value >> 8) & 0xFF);
     rgb [2] = (byte) ((value >> 16) & 0xFF);
     bfo.write(rgb);
   } else
     bfo.write(byteBitmap [(row-1)*biWidth + col ]);
   ++counter;
     }
     for (i = 1; i <= pad; i++)
   bfo.write (0x00);
     counter += pad;
   }
   // IJ.write("counter of bytes written = " + counter);
 }


 /*
  * writeBitmapFileHeader writes the bitmap file header to the file.
  *
  */
 private void writeBitmapFileHeader() throws Exception {
   fo.write (bfType);
   // calculate bfSize
   bfSize = bfOffBits+padWidth*biHeight;
   fo.write (intToDWord (bfSize));
   fo.write (intToWord (bfReserved1));
   fo.write (intToWord (bfReserved2));
   fo.write (intToDWord (bfOffBits));
   // IJ.write("biClrUsed = " + biClrUsed + " bfSize = " + bfSize + " bfOffBits=" + bfOffBits);
 }

 /*
  *
  * writeBitmapInfoHeader writes the bitmap information header
  * to the file.
  *
  */
 private void writeBitmapInfoHeader () throws Exception {
   fo.write (intToDWord (biSize));
   fo.write (intToDWord (biWidth));
   fo.write (intToDWord (biHeight));
   fo.write (intToWord (biPlanes));
   fo.write (intToWord (biBitCount));
   fo.write (intToDWord (biCompression));
   fo.write (intToDWord (biSizeImage));
   fo.write (intToDWord (biXPelsPerMeter));
   fo.write (intToDWord (biYPelsPerMeter));
   fo.write (intToDWord (biClrUsed));
   fo.write (intToDWord (biClrImportant));
 }

 /*
  *
  * intToWord converts an int to a word, where the return
  * value is stored in a 2-byte array.
  *
  */
 private byte [] intToWord (int parValue) {
   byte retValue [] = new byte [2];
   retValue [0] = (byte) (parValue & 0x00FF);
   retValue [1] = (byte) ((parValue >> 8) & 0x00FF);
   return (retValue);
 }

 /*
  *
  * intToDWord converts an int to a double word, where the return
  * value is stored in a 4-byte array.
  *
  */
 private byte [] intToDWord (int parValue) {
   byte retValue [] = new byte [4];
   retValue [0] = (byte) (parValue & 0x00FF);
   retValue [1] = (byte) ((parValue >> 8) & 0x000000FF);
   retValue [2] = (byte) ((parValue >> 16) & 0x000000FF);
   retValue [3] = (byte) ((parValue >> 24) & 0x000000FF);
   return (retValue);
 }
}
