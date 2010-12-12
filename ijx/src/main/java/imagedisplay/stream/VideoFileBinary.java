package imagedisplay.stream;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

// VideoFileBinary - file format for saving a stream of images
//
// The file format:
// [fileHeader] = [[signature] [frameW] [frameH] [bitDepth]] 4 ints)
// [[time][frameArray]] (long time,
// [[time][frameArray]]
// [...]
//
public class VideoFileBinary
{
  File file = null;
  String fileName = null;
  RandomAccessFile f;
  int frameH;
  int frameW;
  int bitDepth;
  int framePixels = 0;
  int bytesPerFrame;
  long fileSize = 0;
  int totalFrames = 0;
  boolean openedToRecord = false;
  long timeStamp = 0;


  ////////////////////////////////////////////////////////////////////////////
  // Open video file for Record/Write !*!*!*! This OVERWRITEs existing file !!
  //
  public VideoFileBinary(String _fileName, int _frameW, int _frameH, int _bitDepth) {
    fileName = _fileName;
    frameW = _frameW;
    frameH = _frameH;
    framePixels = frameW * frameH;
    bitDepth = _bitDepth;
    if (bitDepth == 8) {
      bytesPerFrame = framePixels;
    }
    if (bitDepth == 16) {
      bytesPerFrame = 2 * framePixels;
    }
    // create/open the file
    try {
      f = new RandomAccessFile(fileName, "rw");
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    // write file header
    try {
      f.seek(0);
      f.writeInt(5446061);
      f.writeInt(frameW);
      f.writeInt(frameH);
      f.writeInt(bitDepth);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    totalFrames = 0;
    openedToRecord = true;

    // seek to the end
    //	try {
    //	  f.seek(  f.length() );
    //	} catch (IOException e) {
    //	  e.printStackTrace();
    //    }
  }


  ////////////////////////////////////////////////////////////////////////////
  // Open video file for Play/Read
  //
  public VideoFileBinary(File _file) { //throws Exception {
    file = _file;
    // create/open the file
    try {
      f = new RandomAccessFile(file, "r");
    }
    catch (Exception e) {
      e.printStackTrace();
      // throw new Exception();
    }
    System.out.println("f: " + f);
    // read file header
    try {
      f.seek(0);
      f.readInt();
      frameW = f.readInt();
      frameH = f.readInt();
      bitDepth = f.readInt();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    framePixels = frameW * frameH;
    if (bitDepth == 8) {
      bytesPerFrame = framePixels;
    }
    if (bitDepth == 16) {
      bytesPerFrame = 2 * framePixels;
    }

    // figure out how many frames there are in the file
    try {
      fileSize = f.length();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    totalFrames = (int) (fileSize - 16) / bytesPerFrame;

    openedToRecord = false;
  }


/////////////////////////////////////////////////////////////////////

  public int writeFrame(byte[] frameArray, long timeStamp) {
    try {
      f.write(frameArray);
      f.writeLong(timeStamp);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    totalFrames++;
    return 0;
  }


/////////////////////////////////////////////////////////////////////

  public byte[] readFrame(int frameNumber) throws Exception {
    byte[] frameArray = new byte[bytesPerFrame];
    if (frameNumber < totalFrames) {
      try {
        // 16 bytes for [fileheader]
        f.seek(16 + (frameNumber * (bytesPerFrame + 8)));
        f.read(frameArray);
        timeStamp = f.readLong();
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }
    else {
      throw new Exception();
    }
    return frameArray;
  }


/////////////////////////////////////////////////////////////////////

  public String getFileName() {
    if (fileName != null) {
      return fileName;
    }
    else {
      String fn = null;
      try {
        fn = file.getCanonicalPath();
      }
      catch (IOException ex) {
        ex.printStackTrace();
      }
      return fn;
    }
  }


  public long getTimeStamp() {
    return timeStamp;
  }


  public int getWidth() {
    return frameW;
  }


  public int getHeight() {
    return frameH;
  }


  public int getBitDepth() {
    return bitDepth;
  }


  public int getTotalFrames() {
    return totalFrames;
  }


  public int close() {
    try {
      f.close();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    f = null;
    return 0;
  }


  //----------------------------------------------------------------------------
  public int test() {
    long off = 0;
    long len = 0;
    try {
      off = f.getFilePointer();
      len = f.length();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println("Filename: " + file + "H, W, bits, pix, bytes: " +
        frameH + ", " + frameW + ", " + bitDepth + ", " + framePixels + ", " +
        bytesPerFrame + ", " + fileSize + " Offset/length: " + off + "/" + len);
    return 0;
  }

  // f.setLength(size);
  //    long l = f.length();
  //    f.skipBytes(int n);
  //    f.seek(5*8);
  //    long offset = f.getFilePointer();
  //    f.writeDouble(47.0001);
  //    f.write(byte[] b, int off, int len)
  //
  //    f.readDouble());
  //    f.read(byte);
  //    byte[] b = f.read(byte[] b, int off, int len)
}
