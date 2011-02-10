package imagedisplay;

import com.sun.media.imageio.plugins.tiff.BaselineTIFFTagSet;
import com.sun.media.imageio.plugins.tiff.TIFFImageReadParam;
import com.sun.media.imageio.plugins.tiff.TIFFImageWriteParam;
//----------------------------------------------------------------
// MultipageTiffFile
// GBH Dec 2003 - Mar 2006
// Incremental Image Read/Write using javax.imageio
//
// static:
// ArrayList MultipageTiffFile.loadImageArrayList(filename);
// MultipageTiffFile.saveImageArrayList(imageArray, filename);
// MultipageTiffFile.appendToSeriesTiffFile(outImage, pathFile);
//----------------------------------------------------------------
// This object may be wrapped in a SeriesOfImages
// or a

import org.w3c.dom.Node;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.event.IIOWriteProgressListener;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/*
 * To enable viewing series images during series acquisition, 
 * requires open, append, close, so that it can be opened, but... timing is tough
 * ? Could write to tempSeriesAcqDir and create multipage file when done ?
 * Then, written files can be viewed... then we need a seriesViewer that looks at dir.
 */// @todo ADD EXCEPTION HANDLING
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/*    File f = new File("c\\myimage.gif ");
ImageInputStream iis = null;
try {
iis = ImageIO.createImageInputStream(f);
}
catch (IIOException iioe1) {
System.out.println("Unable to create an input stream!");
return;
}
reader.setInput (stream);
try {
reader.read(0, param);
}
catch (IIOException iioe2) {
System.out.println("An error occurred during reading : " +
iioe2.getMessage());
Throwable t = iioe2.getCause();
if ((t != null) && (t instanceof IOException)) {
System.out.println("Caused by IOException : " +
t.getMessage());
}
}
}
 */
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
public class MultipageTiffFile {

    public static final String TIFF_EXT = "tif";
    String tiffFilename;
    ImageOutputStream ios;
    ImageWriter writer;
    TIFFImageWriteParam paramWrite;
    private boolean writable = false;
    private boolean sequenceWritable = false;
    ImageInputStream iis;
    ImageReader reader;
    TIFFImageReadParam paramRead;
    private boolean readable = false;
    int nextImg = -1;

    public MultipageTiffFile()
      {
      }

    public MultipageTiffFile(String filename)
      {
        tiffFilename = filenameWithTifExt(filename);
        // check exists...
        File f = new File(tiffFilename);
        if (f.exists()) {
            openRead(tiffFilename);
        } else {
            openWrite(tiffFilename);
        }
      }
    // Open for write to new file - deletes if it already exists.
    public MultipageTiffFile(String filename, boolean overWrite)
      {
        tiffFilename = filenameWithTifExt(filename);
        if (overWrite) {
            if (exists(tiffFilename)) {
                deleteFile(tiffFilename);
            }
        }
        File f = new File(tiffFilename);
        openWrite(tiffFilename);
      }

    public String getFilename()
      {
        return tiffFilename;
      }

    public boolean isReadable()
      {
        return readable;
      }

    public boolean isWritable()
      {
        return writable;
      }

    public boolean canWriteSequence()
      {
        return sequenceWritable;
      }
    ///////////////////////////////////////////////////////////////////
    // Writing
    // Open file for writing
    public void openWrite(String _filename)
      {
        tiffFilename = filenameWithTifExt(_filename);
        try {

            //System.out.println("ext: " + ext);
            Iterator writers = ImageIO.getImageWritersByFormatName(TIFF_EXT);
            writer = (ImageWriter) writers.next();
            File tifFile = new File(tiffFilename);
            ios = ImageIO.createImageOutputStream(tifFile);
            writer.setOutput(ios);
            paramWrite = (TIFFImageWriteParam) writer.getDefaultWriteParam();
            //org.pf.joi.Inspector.inspectWait(paramWrite);
            // param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            // param.setCompressionType("JPEG");
            // param.setCompressionQuality(0.7f);
            //System.out.println("canWrite: " + tifFile.canWrite());

            if (writer.canWriteSequence()) { //	i.e tiff, sff(fax)
                writer.prepareWriteSequence(null);
                sequenceWritable = true;
            } else {
                System.err.println("Cannot WriteSequence");
                sequenceWritable = false;
            }
            writable = true;
            // remove this...
            writer.addIIOWriteProgressListener(new WriteProgressListenerAdapter());
        } catch (Exception e) {
            System.err.println("Error opening to write: " + tiffFilename);
            e.printStackTrace();
            writable = false;
        }
      }
    SeriesFileListener listener;

    public void addSeriesFileListener(SeriesFileListener listener)
      {
        this.listener = listener;

      }
    public void removeSeriesFileListener()
      {
        writer.removeAllIIOWriteProgressListeners();
        this.listener = null;
      }
    
    public class WriteProgressListenerAdapter implements IIOWriteProgressListener {

        @Override
        public void imageStarted(ImageWriter source, int imageIndex)
          {
          }

        @Override
        public void imageProgress(ImageWriter source, float percentageDone)
          {
          }

        @Override
        public void imageComplete(ImageWriter source)
          {
            //System.out.println("ImageComplete event");
          }

        @Override
        public void thumbnailStarted(ImageWriter source, int imageIndex, int thumbnailIndex)
          {
          }

        @Override
        public void thumbnailProgress(ImageWriter source, float percentageDone)
          {
          }

        @Override
        public void thumbnailComplete(ImageWriter source)
          {
          }

        @Override
        public void writeAborted(ImageWriter source)
          {
          }
    }
    //-----------------------------------------------------------
    // appendImage
    //
    //   public void appendImage (BufferedImage image, IIOMetaData streamMeta) {
    //     if (writer == null) {
    //        System.err.println("Image append error: no MultipageTiff writer.");
    //        return;
    //     }
    //     try {
    //        IIOImage iioimg = new IIOImage(image, null, null);
    //        writer.write(streamMeta, iioimg, null);
    //        writer.writeToSequence(iioimg, null);
    //
    //     }
    //     catch (Exception e) {
    //        System.err.println("Image append Error : " + e.getMessage());
    //     }
    //  }
    public void appendImage(BufferedImage image)
      {
        appendImage(image, null, null);
      }

    public void appendImage(BufferedImage image, List thumbnails, IIOMetadata meta)
      {
        if (writer == null) {
            System.err.println("Image append error: no MultipageTiff writer.");
            return;
        }
        try {
            IIOImage iioimg = new IIOImage(image, thumbnails, meta);

            // a test, 7/30/08 {{
            //writer.writeInsert(-1, iioimg, null);}

            writer.writeToSequence(iioimg, null);
            if (listener != null) {
                listener.imageAdded();
            }
        } catch (Exception e) {
            System.err.println("Image append Error : " + e.getMessage());
        }
      }
    ///////////////////////////////////////////////////////////////////
    // Reading
    public void openRead(String _filename)
      {
        tiffFilename = filenameWithTifExt(_filename);
        try {
            Iterator readers = ImageIO.getImageReadersByFormatName(TIFF_EXT);
            reader = (ImageReader) readers.next();
            iis = ImageIO.createImageInputStream(new File(tiffFilename));
            reader.setInput(iis, false);
            paramRead = (TIFFImageReadParam) reader.getDefaultReadParam();
            readable = true;
        } catch (java.util.NoSuchElementException exx) {
            System.err.println("No reader found.");
            exx.printStackTrace();
            readable = false;
        } catch (Exception e) {
            System.err.println("Error opening to read: " + tiffFilename);
            e.printStackTrace();
            readable = false;
        }
      }

    public String getTiffFilename()
      {
        return tiffFilename;
      }

    public int getNumImages()
      {
        try {
            return reader.getNumImages(true);
        } catch (IOException ex) {
            return 0;
        }
      }

    public int getWidth(int n)
      {
        try {
            return reader.getWidth(n);
        } catch (IOException ex) {
            return 0;
        }
      }

    public int getHeight(int n)
      {
        try {
            return reader.getHeight(n);
        } catch (IOException ex) {
            return 0;
        }
      }
    //-----------------------------------------------------------
    // get Image MetaData
    public IIOMetadata getImageMetadata(int n)
      {
        // if null for (n), use (0), if avail.
        try {
            return reader.getImageMetadata(n);
        } catch (IOException ex) {
            return null;
        }
      }
    //-----------------------------------------------------------
    // get Stream MetaData
    public IIOMetadata getStreamMetadata()
      {
        try {
            return reader.getStreamMetadata();
        } catch (IOException ex) {
            return null;
        }
      }
    //-----------------------------------------------------------
    // getImage(n)
    public BufferedImage getImage(int n)
      {
        BufferedImage bImage = null;
        try {
            paramRead.setSourceSubsampling(1, 1, 0, 0);
            bImage = reader.read(n, paramRead);
        } catch (IndexOutOfBoundsException ioobe) {
            System.out.println("getImageFailed");
        //ioobe.printStackTrace();
        } catch (Exception ex) {
            System.out.println("getImage failed: n = " + n + " in " + getFilename());
            ex.printStackTrace();
        }
        return bImage;
      }

    public IIOImage getIIOImage(int n)
      {
        IIOImage image = null;
        try {
            image = new IIOImage(reader.read(n), null, reader.getImageMetadata(n));
        //image.getMetadata();
        } catch (IOException ex) {
        }
        return image;
      }
    //-----------------------------------------------------------
    // getAsThumbnail(n, scale)
    public BufferedImage getAsThumbnail(int n, int subsample)
      {
        BufferedImage bImage = null;
        try {
            paramRead.setSourceSubsampling(subsample, subsample, 0, 0);
            bImage = reader.read(n, paramRead);
        } catch (IndexOutOfBoundsException ioobe) {
            System.out.println("getAsThumbnail failed: n = " + n + " in " + getFilename());
        //ioobe.printStackTrace();
        } catch (Exception ex) {
            System.out.println("getAsThumbnail failed: n = " + n + " in " + getFilename());
        //ex.printStackTrace();
        }
        return bImage;
      }

    public BufferedImage getThumbnail(int n)
      {
        if (reader.readerSupportsThumbnails()) {
            BufferedImage bImage = null;
            try {
                bImage = reader.readThumbnail(n, 0);
            } catch (Exception ex) {
                System.out.println("getAsThumbnail failed: n = " + n + " in " + getFilename());
                ex.printStackTrace();
            }
            return bImage;
        } else {
            return null;
        }
      }
    //-----------------------------------------------------------
    // close the file
    public void close()
      {
        closeWrite();
        closeRead();
      }

    public void closeWrite()
      {
        try {
            if (writer != null) {
                //writer.endWriteSequence();
                writer.dispose();
            }
            if (ios != null) {
                ios.close();
                ios = null;
            }
            if (listener != null) {
                listener = null;
            }
        } catch (IOException ex) {
            System.err.println("Error closing tiff write");
            ex.printStackTrace();
        }
      }

    public void closeRead()
      {
        try {
            if (iis != null) {
                iis.close();
                iis = null;
            }
            if (reader != null) {
                reader.dispose();
            }
            if (listener != null) {
                listener = null;
            }
        } catch (IOException ex) {
            System.err.println("Close");
            ex.printStackTrace();
        }
      }
    //=====================================================================
    // >>> Static utility methods
    public static String filenameWithTifExt(String filename)
      {
        return removeExtension(filename) + "." + TIFF_EXT;
      }
    // loadImageArrayList
    // Load all the pages from a TIFF file into an ArrayList of BufferedImages
    public static ArrayList loadImageArrayList(String filename)
      {
        String tifFilename = filenameWithTifExt(filename);
        if (!(new File(tifFilename)).exists()) {
            System.err.println("File not found in loadImageArrayList()");
            return null;
        } else {
            //System.out.println("File found.");
        }
        ArrayList bImages = new ArrayList();
        try {
            long time = System.currentTimeMillis();

            //String ext = filename.substring(filename.lastIndexOf('.') + 1);
            //System.out.println("ext: " + ext);

            Iterator readers = ImageIO.getImageReadersByFormatName(TIFF_EXT);

            //System.out.println("readers: " + readers);
            //      if (readers != null) {
            //        while (readers.hasNext()) {
            //          Object item = (Object) readers.next();
            //          System.out.println("reader: " + item);
            //        }
            //      }
            ImageReader reader = (ImageReader) readers.next();
            ImageInputStream iis = ImageIO.createImageInputStream(new File(tifFilename));
            reader.setInput(iis, true);
            TIFFImageReadParam param = (TIFFImageReadParam) reader.getDefaultReadParam();
            //param.setTIFFDecompressor();
            //ImageReadParam param = reader.getDefaultReadParam();
            //param.setSourceSubsampling(10, 10, 0, 0);
            //BufferedImage img=null;
            bImages.removeAll(bImages); // ??
            try {
                for (int i = 0; true; i++) {
                    BufferedImage img = reader.read(i, param);
                    //param.setDestinationOffset(new Point(w, 0));
                    //param.setDestination(img);
                    //reader.read(i, param);
                    bImages.add(img);
                }
            } catch (IndexOutOfBoundsException ioobe) {
                System.out.println("done with loadImageArrayList");
            } catch (java.util.NoSuchElementException exx) {
                System.err.println("No reader found.");
            } catch (Exception exxx) {
                System.err.println("Exception in loadImageArrayList");
                exxx.printStackTrace();
            }
            System.out.println("Opened : " + tifFilename);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error in loadImage(): " + e.getMessage());
        }
        return bImages;
      }
    //----------------------------------------------------------------------------
    // saveImageArrayList: Writes the ArrayList of images to a file;
    // If file exists, appends unless overWrite is specified
    public static void saveImageArrayList(ArrayList imageArray, String _filename)
      {
        saveImageArrayList(imageArray, _filename, true);
      }

    public static void saveImageArrayList(ArrayList imageArray, String _filename, boolean overWrite)
      {
        String filename = filenameWithTifExt(_filename);
        try {

            //String ext = filename.substring(filename.lastIndexOf('.') + 1);
            Iterator writers = ImageIO.getImageWritersByFormatName(TIFF_EXT);
            ImageWriter writer = (ImageWriter) writers.next();

            // Overwrite if exists
            if (overWrite) {
                if (exists(filename)) {
                    deleteFile(filename);
                }
            }
            ImageOutputStream ios = ImageIO.createImageOutputStream(new File(filename));
            writer.setOutput(ios);
            //
            if (writer.canWriteSequence()) { //	i.e tiff, sff(fax)

                writer.prepareWriteSequence(writer.getDefaultStreamMetadata(writer.getDefaultWriteParam()));
                ios.seek(ios.length() - 1);
                for (int i = 0; i < imageArray.size(); i++) {
                    IIOImage iioimg = new IIOImage((BufferedImage) imageArray.get(i), null, null);
                    writer.writeToSequence(iioimg, null);
                }
                // IIOMetadata getDefaultStreamMetadata(ImageWriteParam param)
                // writer.replaceStreamMetadata(IIOMetadata);
                writer.endWriteSequence();
            }

            //time = edu.mbl.jif.utils.time.TimerHR.currentTimeMillis() - time;
            ios.close();
            ios = null;
            writer.dispose();
            writers = null;
            System.out.println("Saved : " + filename);
        //System.out.println("Time used to save images : " + time);
        } catch (Exception e) {
            System.err.println("Image Save Error : " + e.getMessage());
        }
      }
    // appendImageToTiffFile (image, file): Append single image to TIFF
//    public static void appendImageToTiffFile(BufferedImage outImage, String pathFile) {
//        ArrayList imgList = new ArrayList();
//        imgList.add(outImage);
//        saveImageArrayList(imgList, pathFile + ".tif", false);
//        imgList = null;
//    }
    public static void appendImageToTiffFile(BufferedImage outImage, String pathFile)
      {
        MultipageTiffFile tif = new MultipageTiffFile();
        tif.openWrite(pathFile);
        tif.appendImage(outImage);
        tif.close();
      }
    //---------------------------------------------------------------------
    public static String removeExtension(String f)
      {
        String ext = null;
        if (f == null) {
            return null;
        }
        int i = f.lastIndexOf('.');
        if (i > 1) {
            ext = f.substring(0, i);
            return ext;
        } else {
            return f;
        }
      }

    static boolean exists(String aFileName)
      {
        File f = new File(aFileName);
        return f.exists();
      } // exists

    static boolean deleteFile(String fileName)
      {
        // A File object to represent the filename
        File f = new File(fileName);

        // Make sure the file or directory exists and isn't write protected
        if (!f.exists()) {
            System.err.println("deleteFile: cannot delete, no such file or directory: " + fileName);
            return false;
        }
        if (!f.canWrite()) {
            System.err.println("deleteFile: cannot delete - write protected: " + fileName);
            return false;
        }

        // If it is a directory, make sure it is empty
        if (f.isDirectory()) {
            String[] files = f.list();
            if (files.length > 0) {
                System.err.println("Delete: directory not empty: " + fileName);
                return false;
            }
        }

        // Attempt to delete it
        boolean success = f.delete();
        if (!success) {
            System.err.println("Delete: Delete failed!!: " + fileName);
            return false;
        //throw new IllegalArgumentException("Delete: deletion failed");
        }
        return success;
      }
    // <<< End of static utility methods
    //======================================================================
    //======================================================================
    // >>>> Testing
    public static void main(String[] args)
      {
        //ImagingUtils.listReadersWriters();

        // OME test Image:
        //"D:\\_TestImages\\OME-TIFF Test Images\\tubhiswt_C1.tiff""
        //"D:\\_TestImages\\meta\\xyzt-200x200x10x15.tif"
        String imageFile =
                //edu.mbl.jif.Constants.testDataPath +
            "meta/ome.tif";

        //testGetImageInfo(imageFile);

        testWriteMeta();

        if (false) {
//            testThumbSample(3);
//            testThumbSample(4);
//            testThumbSample(5);
        }

        // testWriteMultiple();


        if (false) {
            BufferedImage img = ImageFactoryGrayScale.testImageByte();
            appendImageToTiffFile(img, "byterooski");
        //appendImageToTiffFile(ImageFactoryGrayScale.testImageByte(), "byteType");
        //appendImageToTiffFile(ImageFactoryGrayScale.testImageFloat(), "floatType");
        }
      }
//    static void testSequenceReadIncremental() {
//        // Test: read sequence incrementally
//        JFrame f = new JFrame();
//        psj.Image.ImagePanel ip = new psj.Image.ImagePanel();
//        f.getContentPane().add(ip, BorderLayout.CENTER);
//        f.setSize(new Dimension(649, 540));
//        f.setVisible(true);
//        ArrayList img2 = new ArrayList();
//        MultipageTiffFile tif2 = new MultipageTiffFile( //"out.tif");
//                edu.mbl.jif.Constants.testDataPath + "ps/STMPS_04_0621_1451_54_Copy.tif");
//        int n = tif2.getNumImages();
//        ip.setPreferredSize(new Dimension(tif2.getWidth(0), tif2.getHeight(0)));
//        for (int k = 0; k < 10; k++) {
//            for (int i = 0; i < n; i++) {
//                //img2.add(tif2.getImage(i));
//                ip.setImage(tif2.getImage(i));
//                try {
//                    Thread.sleep(10);
//                } catch (InterruptedException ex) {
//                }
//            }
//            for (int i = n - 1; i > 0; i--) {
//                //img2.add(tif2.getImage(i));
//                ip.setImage(tif2.getImage(i));
//                try {
//                    Thread.sleep(10);
//                } catch (InterruptedException ex) {
//                }
//            }
//        }
//        tif2.close();
//    }
    static void testWriteMultiple()
      {
        //testWriteSeries() {
        ArrayList imgs = new ArrayList();
        imgs = MultipageTiffFile.loadImageArrayList("PolStackTest.tif");
        // new FrameImageDisplayTabbed(imgs);
        MultipageTiffFile.saveImageArrayList(imgs, "out.tif", false);
        // Open, append images to a sequence, then close.

        for (Iterator iter = imgs.iterator(); iter.hasNext();) {
            MultipageTiffFile tif = new MultipageTiffFile("outSeq.tif", false);
            Object item = (Object) iter.next();
            tif.appendImage((BufferedImage) item);
            System.out.println("ImgOut");
            tif.close();
        }
//        MultipageTiffFile tif = new MultipageTiffFile("outSeq.tif", false);
//        for (Iterator iter = imgs.iterator(); iter.hasNext();) {
//            Object item = (Object) iter.next();
//            tif.appendImage((BufferedImage) item);
//            System.out.println("ImgOut");
//        }
//        tif.close();
      }
    // Test METADATA ------------------------------------------------------
    static void testWriteMeta()
      {
        MultipageTiffFile tif = new MultipageTiffFile("PolStackTest.tif");
        int n = tif.getNumImages();
        System.out.println("n: " + n);
        TiffMetadata ts = new TiffMetadata(tif.getImageMetadata(0));
        int bps = ts.getNumericTag(BaselineTIFFTagSet.TAG_BITS_PER_SAMPLE);
        System.out.println("bps: " + bps);
        IIOMetadataDisplay.displayIIOMetadataNative(tif.getImageMetadata(0));
        IIOMetadataDisplay.displayIIOMetadataNative(tif.getStreamMetadata());

        //testWriteSeries() {
        //      ArrayList imgs = new ArrayList();
        //      imgs = MultipageTiffFile.loadImageArrayList("PS_JulyTest.tif");
        //      new FrameImageDisplayTabbed(imgs);
        //      MultipageTiffFile.saveImageArrayList(imgs, "out.tif");
        //      //Open, append images to a sequence, then close.
        //      MultipageTiffFile tif = new MultipageTiffFile("outSeq.tif");
        //      for (Iterator iter = imgs.iterator(); iter.hasNext(); ) {
        //         Object item = (Object) iter.next();
        //         IIOMetadata meta = tif.modifyMetadata((BufferedImage) item);
        //
        //         //IIOMetadata meta = tif.createMetadata((BufferedImage) item);
        ////         tif.appendImage((BufferedImage) item, null, meta);
        //         System.out.println("ImgOut");
        //      }
        tif.close();
      }

    public Node getMetadateAsTree(BufferedImage img)
      {
        return null;
      }

    IIOMetadata createMetadata(BufferedImage img)
      {
        ImageTypeSpecifier thisType = new ImageTypeSpecifier(img.getColorModel(),
            img.getSampleModel());
        IIOMetadata imageData = writer.getDefaultImageMetadata(thisType, paramWrite);
        //Verify we're stuck with native tiff metadata format.
        //Not as expected, c.f. the second table in
        //http://java.sun.com/products/java-media/jai/forDevelopers/jai-
        //    imageio - 1_0 - rc - docs / index.html
        System.out.print("Standard metadata format is available:  ");
        System.out.println(String.valueOf(imageData.isStandardMetadataFormatSupported()));
        //Get the name for the native format.
        String imageDataFormat = imageData.getNativeMetadataFormatName();

        //Turn the metadata object into a tree.
        Node iNode = imageData.getAsTree(imageDataFormat);

        // Add ImageDescription field
        String descStr = "PS___\n" + "zeroIntensity=10\n" + "this=that\n" + "swing=0.01\n" +
            "etc. etc. \n" + " \n";

        // use ... addTIFFAsciiField(String tagNumber, String Name, String value){
        //Create the little tree corresponding to the Tiff Tag "ImageDescription".
        IIOMetadataNode fieldNode = new IIOMetadataNode("TIFFField");
        fieldNode.setAttribute("number", "270");
        fieldNode.setAttribute("name", "ImageDescription");
        IIOMetadataNode asciisNode = new IIOMetadataNode("TIFFAsciis");
        fieldNode.appendChild(asciisNode);
        IIOMetadataNode asciiNode = new IIOMetadataNode("TIFFAscii");
        asciiNode.setAttribute("value", descStr);
        asciisNode.appendChild(asciiNode);
        //Add this to the image metadata tree, down one level.
        Node tagSetNode = iNode.getFirstChild();
        tagSetNode.appendChild(fieldNode);
        /*
        <TIFFField number="258"
        name="BitsPerSample">
        <TIFFShorts>
        <TIFFShort value="8"/>
        </TIFFShorts>
        </TIFFField>
         */

        //Convert the tree back to metadata object
        try {
            imageData.setFromTree(imageDataFormat, iNode);
        } catch (IIOInvalidTreeException e) {
            System.out.println(e.getOffendingNode().toString());
        }
        IIOMetadataDisplay.displayIIOMetadataNative(imageData);
        return imageData;
      }

    public IIOMetadata modifyMetadata(BufferedImage img)
      {
        //      ImageTypeSpecifier thisType =
        //            new ImageTypeSpecifier(img.getColorModel(), img.getSampleModel());
        ImageTypeSpecifier thisType = ImageTypeSpecifier.createFromBufferedImageType(img.getType());
        IIOMetadata imageData = null;
        // reader.getDefaultImageMetadata(thisType, reader.getDefaultReadParam());
        IIOMetadataDisplay.displayIIOMetadataNative(imageData);
        TiffMetadata ts = new TiffMetadata(imageData);
        int bps = ts.getNumericTag(BaselineTIFFTagSet.TAG_BITS_PER_SAMPLE);
        //      IIOMetadataNode bitsPerSampleNode =
        //            (IIOMetadataNode)ts.getTiffField(BaselineTIFFTagSet.TAG_BITS_PER_SAMPLE);
        //      bps = ts.getTiffShort (bitsPerSampleNode, 0);
        System.out.println("bps: " + bps);
        return imageData;
      }    // Test Thumbnailing------------------------------------
//    public static void testThumbSample(int sample) {
//        MultipageTiffFile mf = new MultipageTiffFile( //"out.tif");
//                edu.mbl.jif.Constants.testDataPath + "ps/_PS_03_0825_1751_01.tiff");
//        int numPages = mf.getNumImages();
//        
//        //BufferedImage bImage = mf.getImage(0);
//        BufferedImage thumb = mf.getAsThumbnail(0, sample);
//        JFrame f = new JFrame();
//        psj.Image.ImagePanel ip = new psj.Image.ImagePanel();
//        f.getContentPane().add(ip, BorderLayout.CENTER);
//        ip.setImage(thumb);
//        f.setSize(new Dimension(649, 540));
//        f.setVisible(true);
//        mf.close();
//    }
}
