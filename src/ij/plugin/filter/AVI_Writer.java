package ij.plugin.filter;
import ijx.IjxImagePlus;
import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.io.*;
import ij.plugin.Animator;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import com.sun.image.codec.jpeg.*;
import javax.imageio.ImageIO;

/**
This plugin saves stacks in AVI format.
Supported formats:
Uncompressed 8-bit (gray or indexed color), 24-bit (RGB).
JPEG and PNG compression.
16-bit and 32-bit (float) images are converted to 8-bit.
The plugin is based on the FileAvi class written by William Gandler.
The FileAvi class is part of Matthew J. McAuliffe's MIPAV program,
available from http://mipav.cit.nih.gov/.
2008-06-05 Support for jpeg and png-compressed output and composite images by Michael Schmid.
*/
public class AVI_Writer implements IjxPlugInFilter {
    //four-character codes for compression
    // Note: byte sequence in four-cc is reversed - ints in Intel (little endian) byte order.
    // Note that compression codes BI_JPEG=4 and BI_PNG=5 are not understood by avi players
    // (even not by MediaPlayer, even though these codes are specified by Microsoft).
    public final static int  NO_COMPRESSION   = 0;            //no compression, also named BITMAPINFO.BI_RGB
    public final static int  JPEG_COMPRESSION = 0x47504a4d;   //'MJPG' JPEG compression of individual frames
    //public final static int JPEG_COMPRESSION = 0x6765706a;   //'jpeg' JPEG compression of individual frames
    public final static int     PNG_COMPRESSION  = 0x20676e70; //'png ' PNG compression of individual frames
    private final static int FOURCC_00db = 0x62643030;    //'00db' uncompressed frame
    private final static int FOURCC_00dc = 0x63643030;    //'00dc' compressed frame

    //compression options: dialog parameters
    private static int      compressionIndex = 0; //0=none, 1=PNG, 2=JPEG
    private static int      jpegQuality = 90;    //0 is worst, 100 best
    private final static String[] COMPRESSION_STRINGS = new String[] {"Uncompressed", "PNG", "JPEG"};
    private final static int[] COMPRESSION_TYPES = new int[] {NO_COMPRESSION, PNG_COMPRESSION, JPEG_COMPRESSION};

    private IjxImagePlus imp;
    private RandomAccessFile raFile;
    private int             xDim,yDim;      //image size
    private int             zDim;           //number of movie frames (stack size)
    private int             bytesPerPixel;  //8 or 24
    private int             frameDataSize;  //in bytes (uncompressed)
    private int             biCompression;  //compression type (0, 'JPEG, 'PNG')
    private int             linePad;        //padding of data lines in bytes to reach 4*n length
    private byte[]          bufferWrite;    //output buffer for image data
    private BufferedImage   bufferedImage;  //data source for writing compressed images
    private RaOutputStream  raOutputStream; //output stream for writing compressed images
    private JPEGImageEncoder jpegEncoder;
    private long[]          sizePointers =  //a stack of the pointers to the chunk sizes (pointers are
                                new long[5];//  remembered to write the sizes later, when they are known)
    private int             stackPointer;   //points to first free position in sizePointers stack

    public int setup(String arg, IjxImagePlus imp) {
        this.imp = imp;
        return DOES_ALL+NO_CHANGES;
    }

    /** Asks for the compression type and filename; then saves as AVI file */
    public void run(ImageProcessor ip) {
        if (!showDialog(imp)) return;          //compression type dialog
        SaveDialog sd = new SaveDialog("Save as AVI...", imp.getTitle(), ".avi");
        String fileName = sd.getFileName();
        if (fileName == null)
            return;
        String fileDir = sd.getDirectory();
        
        try {
            writeImage(imp, fileDir + fileName, COMPRESSION_TYPES[compressionIndex], jpegQuality);
            IJ.showStatus("");
        } catch (IOException e) {
            IJ.error("AVI Writer", "An error occured writing the file.\n \n" + e);
        }
        IJ.showStatus("");
    }

    private boolean showDialog(IjxImagePlus imp) {
    	String options = Macro.getOptions();
    	if (options!=null && options.indexOf("compression=")==-1)
    		Macro.setOptions("compression=Uncompressed "+options);
  		double fps = getFrameRate(imp);
 		int decimalPlaces = (int) fps == fps?0:1;
        GenericDialog gd = new GenericDialog("Save as AVI...");
        gd.addChoice("Compression:", COMPRESSION_STRINGS, COMPRESSION_STRINGS[compressionIndex]);
        gd.addNumericField("JPEG Quality (0-100):", jpegQuality, 0, 3, "");
		gd.addNumericField("Frame Rate:", fps, decimalPlaces, 3, "fps");
        gd.showDialog();                            // user input (or reading from macro) happens here
        if (gd.wasCanceled())                       // dialog cancelled?
            return false;
        compressionIndex = gd.getNextChoiceIndex();
        jpegQuality = (int)gd.getNextNumber();
        fps = gd.getNextNumber();
        if (fps<=0.5) fps = 0.5;
        if (fps>60.0) fps = 60.0;
		imp.getCalibration().fps = fps;
		return true;
    }

    /** Writes an IjxImagePlus (stack) as AVI file. */
    public void writeImage (IjxImagePlus imp, String path, int compression, int jpegQuality)
            throws IOException {
        if (compression!=NO_COMPRESSION && compression!=JPEG_COMPRESSION && compression!=PNG_COMPRESSION)
            throw new IllegalArgumentException("Unsupported Compression 0x"+Integer.toHexString(compression));
        this.biCompression = compression;
        if (jpegQuality < 0) jpegQuality = 0;
        if (jpegQuality > 100) jpegQuality = 100;
        this.jpegQuality = jpegQuality;
        File file = new File(path);
        raFile = new RandomAccessFile(file, "rw");
        raFile.setLength(0);
        imp.startTiming();

        //  G e t   s t a c k   p r o p e r t i e s
        int[] dimensions = imp.getDimensions();
        boolean isComposite = imp.isComposite();
        xDim = dimensions[0];   //image width
        yDim = dimensions[1];   //image height
        if (isComposite) dimensions[2] = 1; //don't step through the channels of a composite image
        zDim = dimensions[2]*dimensions[3]*dimensions[4]; //number of frames in video
        if (imp.getType()==IjxImagePlus.COLOR_RGB || isComposite || biCompression==JPEG_COMPRESSION)
            bytesPerPixel = 3;  //color and JPEG-compressed files
        else
            bytesPerPixel = 1;  //gray 8, 16, 32 bit and indexed color: all written as 8 bit
        //boolean isColor = imp.getType()==IjxImagePlus.COLOR_RGB || isComposite || imp.getProcessor().isColorLut();
        boolean writeLUT = bytesPerPixel==1; // QuickTime reads the avi palette also for PNG
        linePad = 0;
        int minLineLength = bytesPerPixel*xDim;
        if (biCompression==NO_COMPRESSION && minLineLength%4!=0)
            linePad = 4 - minLineLength%4; //uncompressed lines written must be a multiple of 4 bytes
        frameDataSize = (bytesPerPixel*xDim+linePad)*yDim;
        int microSecPerFrame = (int)((1.0/getFrameRate(imp))*1.0e6);

        //  W r i t e   A V I   f i l e   h e a d e r
        writeString("RIFF");    // signature
        chunkSizeHere();        // size of file (nesting level 0)
        writeString("AVI ");    // RIFF type
        writeString("LIST");    // first LIST chunk, which contains information on data decoding
        chunkSizeHere();        // size of LIST (nesting level 1)
        writeString("hdrl");    // LIST chunk type
        writeString("avih");    // Write the avih sub-CHUNK
        writeInt(0x38);         // length of the avih sub-CHUNK (38H) not including the
                                // the first 8 bytes for avihSignature and the length
        writeInt(microSecPerFrame); // dwMicroSecPerFrame - Write the microseconds per frame
        writeInt(0);            // dwMaxBytesPerSec (maximum data rate of the file in bytes per second)
        writeInt(0);            // dwReserved1 - Reserved1 field set to zero
        writeInt(0x10);         // dwFlags - just set the bit for AVIF_HASINDEX
                                //   10H AVIF_HASINDEX: The AVI file has an idx1 chunk containing
                                //   an index at the end of the file.  For good performance, all
                                //   AVI files should contain an index.
        writeInt(zDim);         // dwTotalFrames - total frame number
        writeInt(0);            // dwInitialFrames -Initial frame for interleaved files.
                                // Noninterleaved files should specify 0.
        writeInt(1);            // dwStreams - number of streams in the file - here 1 video and zero audio.
        writeInt(0);      // dwSuggestedBufferSize 
         writeInt(xDim);         // dwWidth - image width in pixels
        writeInt(yDim);         // dwHeight - image height in pixels
        writeInt(0);            // dwReserved[4]
        writeInt(0);
        writeInt(0);
        writeInt(0);

        //  W r i t e   s t r e a m   i n f o r m a t i o n
        writeString("LIST");    // List of stream headers
        chunkSizeHere();        // size of LIST (nesting level 2)
        writeString("strl");    // LIST chunk type: stream list
        writeString("strh");    // stream header 
        writeInt(56);           // Write the length of the strh sub-CHUNK
        writeString("vids");    // fccType - type of data stream - here 'vids' for video stream
        writeString("DIB ");    // 'DIB ' for Microsoft Device Independent Bitmap.
        writeInt(0);            // dwFlags
        writeInt(0);            // wPriority, wLanguage
        writeInt(0);            // dwInitialFrames
        writeInt(1);            // dwScale
        writeInt((int)getFrameRate(imp)); //  dwRate - frame rate for video streams
        writeInt(0);            // dwStart - this field is usually set to zero
        writeInt(zDim);         // dwLength - playing time of AVI file as defined by scale and rate
                                // Set equal to the number of frames
        writeInt(0);            // dwSuggestedBufferSize for reading the stream.
                                // Typically, this contains a value corresponding to the largest chunk
                                // in a stream.
        writeInt(-1);           // dwQuality - encoding quality given by an integer between
                                // 0 and 10,000.  If set to -1, drivers use the default
                                // quality value.
        writeInt(0);            // dwSampleSize. 0 means that each frame is in its own chunk
        writeShort((short)0);   // left of rcFrame if stream has a different size than dwWidth*dwHeight(unused)
        writeShort((short)0);   // top
        writeShort((short)0);   // right
        writeShort((short)0);   // bottom
        // end of 'strh' chunk, stream format follows
        writeString("strf");    // stream format chunk
        chunkSizeHere();        // size of 'strf' chunk (nesting level 3)
        writeInt(40);           // biSize - Write header size of BITMAPINFO header structure
                                // Applications should use this size to determine which BITMAPINFO header structure is
                                // being used.  This size includes this biSize field.
        writeInt(xDim);         // biWidth - width in pixels
        writeInt(yDim);         // biHeight - image height in pixels. (May be negative for uncompressed
                                // video to indicate vertical flip).
        writeShort(1);          // biPlanes - number of color planes in which the data is stored
        writeShort((short)(8*bytesPerPixel)); // biBitCount - number of bits per pixel #
        writeInt(biCompression); // biCompression - type of compression used (uncompressed: NO_COMPRESSION=0)
        int biSizeImage =       // Image Buffer. Quicktime needs 3 bytes also for 8-bit png
                (biCompression==NO_COMPRESSION)?0:xDim*yDim*bytesPerPixel;
        writeInt(biSizeImage);  // biSizeImage (buffer size for decompressed mage) may be 0 for uncompressed data
        writeInt(0);            // biXPelsPerMeter - horizontal resolution in pixels per meter
        writeInt(0);            // biYPelsPerMeter - vertical resolution in pixels per meter
        writeInt(writeLUT ? 256:0); // biClrUsed (color table size; for 8-bit only)
        writeInt(0);            // biClrImportant - specifies that the first x colors of the color table
                                // are important to the DIB.  If the rest of the colors are not available,
                                // the image still retains its meaning in an acceptable manner.  When this
                                // field is set to zero, all the colors are important, or, rather, their
                                // relative importance has not been computed.
        if (writeLUT)           // write color lookup table
            writeLUT(imp.getProcessor());
        chunkEndWriteSize();    //'strf' chunk finished (nesting level 3)
        
        writeString("strn");    // Use 'strn' to provide a zero terminated text string describing the stream
        writeInt(16);           // length of the strn sub-CHUNK (must be even)
        writeString("ImageJ AVI     \0"); //must be 16 bytes as given above (including the terminating 0 byte)
        chunkEndWriteSize();    // LIST 'strl' finished (nesting level 2)
        chunkEndWriteSize();    // LIST 'hdrl' finished (nesting level 1)
        writeString("JUNK");    // write a JUNK chunk for padding
        chunkSizeHere();        // size of 'strf' chunk (nesting level 1)
        raFile.seek(4096/*2048*/);      // we continue here
        chunkEndWriteSize();    // 'JUNK' finished (nesting level 1)
        
        writeString("LIST");    // the second LIST chunk, which contains the actual data
        chunkSizeHere();        // size of LIST (nesting level 1)
        long moviPointer = raFile.getFilePointer();
        writeString("movi");    // Write LIST type 'movi'

        //  P r e p a r e   f o r   w r i t i n g   d a t a
        if (biCompression == NO_COMPRESSION)
            bufferWrite = new byte[frameDataSize];
        else
            raOutputStream = new RaOutputStream(raFile); //needed for writing compressed formats
        int dataSignature = biCompression==NO_COMPRESSION ? FOURCC_00db : FOURCC_00dc;
        int maxChunkLength = 0;                 // needed for dwSuggestedBufferSize
        int[] dataChunkOffset = new int[zDim];  // remember chunk positions...
        int[] dataChunkLength = new int[zDim];  // ... and sizes for the index

        //  W r i t e   f r a m e   d a t a
        for (int z=0; z<zDim; z++) {
            IJ.showProgress(z, zDim);
            ImageProcessor ip = null;      // get the image to write ...
            if (isComposite) {
                int frame = z/dimensions[3] + 1;
                int slice = z%dimensions[3] + 1;
                //IJ.log("z="+z+"/"+zDim+"; frame="+frame+", slice="+slice);
                imp.setPosition(imp.getChannel(), slice, frame);
                ip = new ColorProcessor(imp.getImage());
            } else
                ip = zDim==1 ? imp.getProcessor() : imp.getStack().getProcessor(z+1);
            int chunkPointer = (int)raFile.getFilePointer();
            writeInt(dataSignature);        // start writing chunk: '00db' or '00dc'
            chunkSizeHere();                // size of '00db' or '00dc' chunk (nesting level 2)
            if (biCompression == NO_COMPRESSION) {
                if (bytesPerPixel==1)
                    writeByteFrame(ip);
                else
                    writeRGBFrame(ip);
            } else
                writeCompressedFrame(ip);
            dataChunkOffset[z] = (int)(chunkPointer - moviPointer);
            dataChunkLength[z] = (int)(raFile.getFilePointer() - chunkPointer - 8); //size excludes '00db' and size fields
            if (maxChunkLength < dataChunkLength[z]) maxChunkLength = dataChunkLength[z];
            chunkEndWriteSize();            // '00db' or '00dc' chunk finished (nesting level 2)
            //if (IJ.escapePressed()) {
            //    IJ.showStatus("Save as Avi INTERRUPTED");
            //    break;
            //}
        }
        chunkEndWriteSize();                // LIST 'movi' finished (nesting level 1)

        //  W r i t e   I n d e x
        writeString("idx1");    // Write the idx1 chunk
        chunkSizeHere();        // size of 'idx1' chunk (nesting level 1)
        for (int z = 0; z < zDim; z++) {
            writeInt(dataSignature);// ckid field: '00db' or '00dc'
            writeInt(0x10);     // flags: select AVIIF_KEYFRAME
                         // AVIIF_KEYFRAME 0x00000010
                         // The flag indicates key frames in the video sequence.
                         // Key frames do not need previous video information to be decompressed.
                         // AVIIF_NOTIME 0x00000100 The CHUNK does not influence video timing (for
                         //   example a palette change CHUNK).
                         // AVIIF_LIST 0x00000001 marks a LIST CHUNK.
                         // AVIIF_TWOCC 2L
                         // AVIIF_COMPUSE 0x0FFF0000 These bits are for compressor use.
             writeInt(dataChunkOffset[z]); // offset to the chunk
                         // offset can be relative to file start or 'movi'
             writeInt(dataChunkLength[z]); // length of the chunk.
        }  // for (z = 0; z < zDim; z++)
        chunkEndWriteSize();    // 'idx1' finished (nesting level 1)
        chunkEndWriteSize();    // 'RIFF' File finished (nesting level 0)
        raFile.close();
        IJ.showProgress(1.0);
    }

    /** Reserve space to write the size of chunk and remember the position
     *  for a later call to chunkEndWriteSize().
     *  Several levels of chunkSizeHere() and chunkEndWriteSize() may be nested.
     */
    private void chunkSizeHere() throws IOException {
        sizePointers[stackPointer] = raFile.getFilePointer();
        writeInt(0);    //for now, write 0 to reserve space for "size" item
        stackPointer++;
    }
    /** At the end of a chunk, calculate its size and write it to the
     *  position remembered previously. Also pads to 2-byte boundaries.
     */
    private void chunkEndWriteSize() throws IOException {
        stackPointer--;
        long position = raFile.getFilePointer();
        raFile.seek(sizePointers[stackPointer]);
        writeInt((int)(position - (sizePointers[stackPointer]+4)));
        raFile.seek(((position+1)/2)*2);    //pad to 2-byte boundary
        //IJ.log("chunk at 0x"+Long.toHexString(sizePointers[stackPointer]-4)+"-0x"+Long.toHexString(position));
    }

    /** Write Grayscale (or indexed color) data. Lines are padded to a length
     *  that is a multiple of 4 bytes. */
    private void writeByteFrame(ImageProcessor ip) throws IOException {
        ip = ip.convertToByte(true);
        byte[] pixels = (byte[])ip.getPixels();
        int width = ip.getWidth();
        int height = ip.getHeight();
        int c, offset, index = 0;
        for (int y=height-1; y>=0; y--) {
            offset = y*width;
            for (int x=0; x<width; x++)
                bufferWrite[index++] = pixels[offset++];
            for (int i = 0; i<linePad; i++)
                bufferWrite[index++] = (byte)0;
        }
        raFile.write(bufferWrite);
    }

    /** Write RGB data. Each 3-byte triplet in the bitmap array represents
     *  blue, green, and red, respectively, for a pixel.  The color bytes are
     *  in reverse order (Windows convention). Lines are padded to a length
     *  that is a multiple of 4 bytes. */
    private void writeRGBFrame(ImageProcessor ip) throws IOException {
        ip = ip.convertToRGB();
        int[] pixels = (int[])ip.getPixels();
        int width = ip.getWidth();
        int height = ip.getHeight();
        int c, offset, index = 0;
        for (int y=height-1; y>=0; y--) {
            offset = y*width;
            for (int x=0; x<width; x++) {
                c = pixels[offset++];
                bufferWrite[index++] = (byte)(c&0xff); // blue
                bufferWrite[index++] = (byte)((c&0xff00)>>8); //green
                bufferWrite[index++] = (byte)((c&0xff0000)>>16); // red
            }
            for (int i = 0; i<linePad; i++)
                bufferWrite[index++] = (byte)0;
        }
        raFile.write(bufferWrite);
    }

    /** Write a frame as jpeg- or png-compressed image */
    private void writeCompressedFrame(ImageProcessor ip) throws IOException {
        BufferedImage bufferedImage = ip.getBufferedImage();
        //IJ.log("BufferdImage Type="+bufferedImage.getType()); // 1=RGB, 13=indexed
        if (biCompression == JPEG_COMPRESSION) {
            jpegEncoder = JPEGCodec.createJPEGEncoder(raOutputStream);
            jpegEncoder.encode(bufferedImage);  
        } else //if (biCompression == PNG_COMPRESSION)
            ImageIO.write(bufferedImage, "png", raOutputStream);
    }

    /** Write the color table entries (for 8 bit grayscale or indexed color).
     *  Byte order or LUT entries: blue byte, green byte, red byte, 0 byte */
    private void writeLUT(ImageProcessor ip) throws IOException {
        IndexColorModel cm = (IndexColorModel)(ip.getCurrentColorModel());
        int mapSize = cm.getMapSize();
        byte[] lutWrite = new byte[4*256];
        for (int i = 0; i<256; i++) {
            if (i<mapSize) {
                lutWrite[4*i] = (byte)cm.getBlue(i);
                lutWrite[4*i+1] = (byte)cm.getGreen(i);
                lutWrite[4*i+2] = (byte)cm.getRed(i);
                lutWrite[4*i+3] = (byte)0;
            }
        }
        raFile.write(lutWrite);
    }

    private double getFrameRate(IjxImagePlus imp) {
        double rate = imp.getCalibration().fps;
        if (rate==0.0)
            rate = Animator.getFrameRate();
        if (rate<=0.5) rate = 0.5;
        if (rate>60.0) rate = 60.0;
        return rate;
    }

    private void writeString(String s) throws IOException {
        byte[] bytes =  s.getBytes("UTF8");
        raFile.write(bytes);
    }

    /** Write 4-byte int with Intel (little-endian) byte order
     * (note: RandomAccessFile.writeInt has other byte order than AVI) */
    private void writeInt(int v) throws IOException {
        raFile.write(v & 0xFF);
        raFile.write((v >>>  8) & 0xFF);
        raFile.write((v >>> 16) & 0xFF);
        raFile.write((v >>> 24) & 0xFF);
        //IJ.log("int: 0x"+Integer.toHexString(v)+"="+v);
    }

    /** Write 2-byte short with Intel (little-endian) byte order
     * (note: RandomAccessFile.writeShort has other byte order than AVI) */
    private void writeShort(int v) throws IOException {
        raFile.write(v & 0xFF);
        raFile.write((v >>> 8) & 0xFF);
    }

    /** An output stream directed to a RandomAccessFile (starting at the current position) */
    class RaOutputStream extends OutputStream {
        RandomAccessFile raFile;
        RaOutputStream (RandomAccessFile raFile) {
            this.raFile = raFile;
        }
        public void write (int b) throws IOException {
            //IJ.log("stream: byte");
            raFile.writeByte(b); //just for completeness, usually not used by image encoders
        }
        public void write (byte[] b) throws IOException {
            //IJ.log("stream: array len="+b.length);
            raFile.write(b);
        }
        public void write (byte[] b, int off, int len) throws IOException {
            //IJ.log("stream: array="+b.length+" off="+off+" len="+len);
            raFile.write(b, off, len);
        }
    }

}
