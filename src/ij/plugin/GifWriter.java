package ij.plugin;
import ijx.IjxImagePlus;
import ij.*;
import ij.io.*;
import ij.gui.*;
import ij.process.*;
import ij.plugin.*;
import ijx.IjxImageStack;
import java.io.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.Color;
import java.awt.Point;
import java.io.OutputStream;
import java.io.IOException;


/**
 * This plugin encodes a GIF file consisting of one or more frames.
 *
 * <pre>
 * Extensively Modified for ImagePlus
 * Extended to handle 8 bit Images with more complex Color lookup tables with transparency index
 *
 * Ryan Raz March 2002
 * raz@rraz.ca
 * Version 1.01
 ** Extensively Modified for ImagePlus
 * Extended to handle 8 bit Images with more complex Color lookup tables with transparency index
 *
 * Ryan Raz March 2002
 * ryan@rraz.ca
 * Version 1.01 Please report any bugs
 *
 * Credits for the base conversion codes
 * No copyright asserted on the source code of this class.  May be used
 * for any purpose, however, refer to the Unisys LZW patent for restrictions
 * on use of the associated LZWEncoder class.  Please forward any corrections
 * to kweiner@fmsware.com.
 * </pre>
 *
 * @author Kevin Weiner, FM Software
 * @version 1.0 December 2000
 *
 */

/** Writes a stack as an animated Gif */
public class GifWriter implements PlugIn {
	static int transparentIndex = Prefs.getTransparentIndex();
   
	public void run(String path) {
		IjxImagePlus imp = IJ.getImage();
		if (path.equals("")) {
			SaveDialog sd = new SaveDialog("Save as Gif", imp.getTitle(), ".gif");
			if (sd.getFileName()==null) return;
			path = sd.getDirectory()+sd.getFileName();
		}

		IjxImageStack stack = imp.getStack();
		IjxImagePlus tmp = new ImagePlus();
		int nSlices = stack.getSize();
	    GifEncoder ge = new GifEncoder();
		double fps = imp.getCalibration().fps;
		if (fps==0.0) fps = Animator.getFrameRate();
		if (fps<=0.2) fps = 0.2;
		if (fps>60.0) fps = 60.0;
		ge.setDelay((int)((1.0/fps)*1000.0));
		if (transparentIndex!=-1) {
			ge.transparent = true;
			ge.transIndex = transparentIndex;
		}
	       ge.start(path);
		
 		for (int i=1; i<=nSlices; i++) {
			IJ.showStatus("writing: "+i+"/"+nSlices);
			IJ.showProgress((double)i/nSlices);
			tmp.setProcessor(null, stack.getProcessor(i));
			
			try {
				ge.addFrame(tmp);
			} catch(Exception e)  {
				IJ.showMessage("Save as Gif", ""+e);
				return;
			}

		}
		ge.finish();
		IJ.showStatus("");
		IJ.showProgress(1.0);
	}
	
}


class GifEncoder {

   int width;                 // image size
   int height;
   boolean transparent;  // transparent color if given
   int transIndex;            // transparent index in color table
   int repeat = 0;           // repeat forever
   protected int delay = 50;             // frame delay (hundredths)
   boolean started = false;   // ready to output frames
   OutputStream out;
   IjxImagePlus image;       // current frame
   byte[] pixels;             // BGR byte array from frame
   byte[] indexedPixels;      // converted frame indexed to palette
   int colorDepth;            // number of bit planes
   byte[] colorTab;           // RGB palette
   int lctSize = 7;           // local color table size (bits-1)
   int dispose = 0;          // disposal code (-1 = use default)
   boolean closeStream = false;  // close stream when finished
   boolean firstFrame = true;
   boolean sizeSet = false;   // if false, get size from first frame
   int sample = 2;           // default sample interval for quantizer distance should be small for small icons
   byte[] gct = null;		//Global color table
   boolean GCTextracted = false; // Set if global color table extracted from rgb image 
   boolean GCTloadedExternal = false; // Set if global color table loaded directly from external image 
   int    GCTred =  0;   //Transparent Color
   int  GCTgrn = 0;    // green
   int   GCTbl  =  0;   // blue
   int   GCTcindex = 0;  //index into color table
   boolean GCTsetTransparent = false; //If true then Color table transparency index is set
   boolean GCToverideIndex = false; //If true Transparent index is set to index with closest colors
   boolean GCToverideColor = false; //if true Color at Transparent index is set to GCTred, GCTgrn GCTbl
   
   /**
    * Adds next GIF frame.  The frame is not written immediately, but is
    * actually deferred until the next frame is received so that timing
    * data can be inserted.  Invoking <code>finish()</code> flushes all
    * frames.  If <code>setSize</code> was not invoked, the size of the
    * first image is used for all subsequent frames.
    *
    * @param im  containing frame to write.
    * @return true if successful.
    */
   public boolean addFrame(IjxImagePlus image) {
      if ((image == null) || !started) return false;
      boolean ok = true;
      try {
         if (firstFrame) {
            if (!sizeSet) {
               // use first frame's size
               setSize(image.getWidth(), image.getHeight());
            }
            writeLSD();
            if (repeat>=0) writeNetscapeExt();      // use NS app extension to indicate reps
            firstFrame = false;
         }
      	int bitDepth = image.getBitDepth();
      	// If  indexed byte image then format does not need changing
      	int k;
       	Process8bitCLT(image);
       	writeGraphicCtrlExt();         // write graphic control extension
       	writeImageDesc();              // image descriptor
       	writePalette();                // local color table
       	writePixels();                 // encode and write pixel data
      } catch (IOException e) { ok = false; }
      return ok;
  }
  
 /* 
*	Get Options because options box has been checked
	
	Some of the code being set
		setTransparent(Color.black);
		Dispose = 0;   
    		setDelay(500);   //  time per frame in milliseconds
    		gctused = false; // Set to true to use Global color table
		GCTextracted = false; // Set if global color table extracted from rgb image 
                GCTloadedExternal = false; // Set if global color table loaded directly from external image 
                GCTextracted = false; // Set if global color table extracted from rgb image 
		GCTred =  0;   //Transparent Color
		GCTgrn = 0;    // green
		GCTbl  =  0;   // blue
		GCTcindex = 0;  //index into color table
		autotransparent = false; // Set True if transparency index coming from image 8 bit only
  		GCTsetTransparent = true; //If true then Color table transparency index is set
  		GCToverideIndex = false; //If true Transparent index is set to index with closest colors
		GCToverideColor = false; //if true Color at Transparent index is set to GCTred, GCTgrn GCTbl
   
*/

  
/********************************************************
*    Gets Color lookup Table from 8 bit ImagePlus
*/
void Process8bitCLT(IjxImagePlus image){
       colorDepth = 8;
     	//setTransparent(false);
	    ImageProcessor ip = image.getProcessor();
	    ip = ip.convertToByte(true);       
        ColorModel cm = ip.getColorModel();
        indexedPixels = (byte[])(ip.getPixels());
        IndexColorModel m = (IndexColorModel)cm;
        int mapSize = m.getMapSize();
        if (transIndex>=mapSize) {
			setTransparent(false);
			transIndex = 0;
        }
        int k;
        colorTab = new byte[mapSize*3];
        for (int i = 0; i < mapSize; i++) {
         	k=i*3;
         	colorTab[k] = (byte)m.getRed(i);
         	colorTab[k+1] = (byte)m.getGreen(i);
         	colorTab[k+2] = (byte)m.getBlue(i);
        }
      	m.finalize();
  
 }     

   /**
    * Flushes any pending data and closes output file.
    * If writing to an OutputStream, the stream is not
    * closed.
    */
   public boolean finish() {
      if (!started) return false;
      boolean ok = true;
      started = false;
      try {
         out.write(0x3b);  // gif trailer
         out.flush();
         if (closeStream)
            out.close();
      } catch (IOException e) { ok = false; }

      // reset for subsequent use
      GCTextracted = false; // Set if global color table extracted from rgb image 
      GCTloadedExternal = false; // Set if global color table loaded directly from external image 
      transIndex = 0;
      transparent = false;    
      gct = null;		//Global color table
      out = null;
      image = null;
      pixels = null;
      indexedPixels = null;
      colorTab = null;
      closeStream = false;
      firstFrame = true;

      return ok;
   }
   
   /**
    * Sets the delay time between each frame, or changes it
    * for subsequent frames (applies to last frame added).
    *
    * @param ms int delay time in milliseconds
    */
   public void setDelay(int ms) {
      delay = Math.round(ms / 10.0f);
   }


   /**
    * Sets the GIF frame disposal code for the last added frame
    * and any subsequent frames.  Default is 0 if no transparent
    * color has been set, otherwise 2.
    * @param code int disposal code.
    */
   public void setDispose(int code) {
      if (code >= 0)
         dispose = code;
   }


   /**
    * Sets frame rate in frames per second.  Equivalent to
    * <code>setDelay(1000/fps)</code>.
    *
    * @param fps float frame rate (frames per second)
    */
   public void setFrameRate(float fps) {
      if (fps != 0f) {
         delay = Math.round(100f/fps);
      }
   }


   /**
    * Sets quality of color quantization (conversion of images
    * to the maximum 256 colors allowed by the GIF specification).
    * Lower values (minimum = 1) produce better colors, but slow
    * processing significantly.  10 is the default, and produces
    * good color mapping at reasonable speeds.  Values greater
    * than 20 do not yield significant improvements in speed.
    *
    * @param quality int greater than 0.
    * @return
    */
   public void setQuality(int quality) {
      if (quality < 1) quality = 1;
      sample = quality;
   }
   
   /**
    * Sets the number of times the set of GIF frames
    * should be played.  Default is 1; 0 means play
    * indefinitely.  Must be invoked before the first
    * image is added.
    *
    * @param iter int number of iterations.
    * @return
    */
   public void setRepeat(int iter) {
      if (iter >= 0)
         repeat = iter;
   }


   /**
    * Sets the GIF frame size.  The default size is the
    * size of the first frame added if this method is
    * not invoked.
    *
    * @param w int frame width.
    * @param h int frame width.
    */
   public void setSize(int w, int h) {
      if (started && !firstFrame) return;
      width = w;
      height = h;
      if (width < 1) width = 320;
      if (height < 1) height = 240;
      sizeSet = true;
   }


   /**
    * Sets the transparent color for the last added frame
    * and any subsequent frames.
    * Since all colors are subject to modification
    * in the quantization process, the color in the final
    * palette for each frame closest to the given color
    * becomes the transparent color for that frame.
    * May be set to null to indicate no transparent color.
    *
    * @param c Color to be treated as transparent on display.
    */
   public void setTransparent(boolean c) {
      transparent = c;
   }


   /**
    * Initiates GIF file creation on the given stream.  The stream
    * is not closed automatically.
    *
    * @param os OutputStream on which GIF images are written.
    * @return false if initial write failed.
    */
   public boolean start(OutputStream os) {
      if (os == null) return false;
      boolean ok = true;
      closeStream = false;
      out = os;
      try {
         writeString("GIF89a");        // header
      } catch (IOException e) { ok = false; }
      return started = ok;
   }


   /**
    * Initiates writing of a GIF file with the specified name.
    *
    * @param file String containing output file name.
    * @return false if open or initial write failed.
    */
   public boolean start(String file) {
      boolean ok = true;
      try {
         out = new BufferedOutputStream(new FileOutputStream(file));
         ok = start(out);
         closeStream = true;
      } catch (IOException e) { ok = false; }
      return started = ok;
   }
/**
	Sets Net sample size depending on image size
	
**/
   public void OverRideQuality(int npixs){
        if(npixs>100000) sample = 10;
        else sample = npixs/10000;
        if(sample < 1) sample = 1;

    }
    
   /**
    * Writes Graphic Control Extension
    */
   protected void writeGraphicCtrlExt() throws IOException {
      out.write(0x21);         // extension introducer
      out.write(0xf9);         // GCE label
      out.write(4);            // data block size
      int transp, disp;
      if (!transparent) {
         transp = 0;
         disp = 0;             // dispose = no action
      } else {
         transp = 1;
         disp = 2;             // force clear if using transparent color  
      }
      if (dispose >= 0)
         disp = dispose & 7;   // user override
      disp <<= 2;

      // packed fields
      out.write(  0 |          // 1:3 reserved
               disp |          // 4:6 disposal
               0 |             // 7   user input - 0 = none
               transp);        // 8   transparency flag

      writeShort(delay);       // delay x 1/100 sec
      out.write(transIndex);   // transparent color index
      out.write(0);            // block terminator
  }


   /**
    * Writes Image Descriptor
    */
   protected void writeImageDesc() throws IOException {
      out.write(0x2c);         // image separator
      writeShort(0);           // image position x,y = 0,0
      writeShort(0);
      writeShort(width);       // image size
      writeShort(height);
      // packed fields
      out.write(0x80 |         // 1 local color table  1=yes
        	0 |            // 2 interlace - 0=no
            0 |            // 3 sorted - 0=no
            0 |            // 4-5 reserved
            lctSize);   // size of local color table
   }


   /**
    * Writes Logical Screen Descriptor with global color table
    */
   protected void writeLSDgct() throws IOException {
      // logical screen size
      writeShort(width);
      writeShort(height);
      // packed fields
      out.write((0x80 |       // 1   : global color table flag = 0 (nn
               0x70 |         // 2-4 : color resolution = 7
               0x00 |         // 5   : gct sort flag = 0
               lctSize));        // 6-8 : gct size = 0

      out.write(0);           // background color index
      out.write(0);           // pixel aspect ratio - assume 1:1
   }
   
 /**
    * Writes Logical Screen Descriptor without global color table
    */
   protected void writeLSD() throws IOException {
      // logical screen size
      writeShort(width);
      writeShort(height);
      // packed fields
      out.write((0x00 |       // 1   : global color table flag = 0 (none)
               0x70 |         // 2-4 : color resolution = 7
               0x00 |         // 5   : gct sort flag = 0
               0x00));        // 6-8 : gct size = 0

      out.write(0);           // background color index
      out.write(0);           // pixel aspect ratio - assume 1:1
   }


   /**
    * Writes Netscape application extension to define
    * repeat count.
    */
   protected void writeNetscapeExt() throws IOException {
      out.write(0x21);       // extension introducer
      out.write(0xff);       // app extension label
      out.write(11);         // block size
      writeString("NETSCAPE"+"2.0");    // app id + auth code
      out.write(3);          // sub-block size
      out.write(1);          // loop sub-block id
      writeShort(repeat);    // loop count (extra iterations, 0=repeat forever)
      out.write(0);          // block terminator
   }


   /**
    * Writes color table
    */
   protected void writePalette() throws IOException {
      out.write(colorTab, 0, colorTab.length);
      int n = (3 * 256) - colorTab.length;
      for (int i = 0; i < n; i++)
         out.write(0);
   }


   /**
    * Encodes and writes pixel data
    */
   protected void writePixels() throws IOException {
      LZWEncoder encoder = new LZWEncoder(width, height, indexedPixels, colorDepth);
      encoder.encode(out);
   }


   /**
    *    Write 16-bit value to output stream, LSB first
    */
   protected void writeShort(int value) throws IOException {
      out.write(value & 0xff);
      out.write((value >> 8) & 0xff);
   }


   /**
    * Writes string to output stream
    */
   protected void writeString(String s) throws IOException {
      for (int i = 0; i < s.length(); i++)
         out.write((byte) s.charAt(i));
   }
}


//==============================================================================
//  Adapted from Jef Poskanzer's Java port by way of J. M. G. Elliott.
//  K Weiner 12/00


class LZWEncoder {

  private static final int EOF = -1;

  private int     imgW, imgH;
  private byte[]  pixAry;
  private int     initCodeSize;
  private int     remaining;
  private int     curPixel;


  // GIFCOMPR.C       - GIF Image compression routines
  //
  // Lempel-Ziv compression based on 'compress'.  GIF modifications by
  // David Rowley (mgardi@watdcsu.waterloo.edu)

  // General DEFINEs

  static final int BITS = 12;

  static final int HSIZE = 5003;       // 80% occupancy

  // GIF Image compression - modified 'compress'
  //
  // Based on: compress.c - File compression ala IEEE Computer, June 1984.
  //
  // By Authors:  Spencer W. Thomas      (decvax!harpo!utah-cs!utah-gr!thomas)
  //              Jim McKie              (decvax!mcvax!jim)
  //              Steve Davies           (decvax!vax135!petsd!peora!srd)
  //              Ken Turkowski          (decvax!decwrl!turtlevax!ken)
  //              James A. Woods         (decvax!ihnp4!ames!jaw)
  //              Joe Orost              (decvax!vax135!petsd!joe)

  int n_bits;                         // number of bits/code
  int maxbits = BITS;                 // user settable max # bits/code
  int maxcode;                        // maximum code, given n_bits
  int maxmaxcode = 1 << BITS; // should NEVER generate this code

  int[] htab = new int[HSIZE];
  int[] codetab = new int[HSIZE];

  int hsize = HSIZE;                  // for dynamic table sizing

  int free_ent = 0;                   // first unused entry

  // block compression parameters -- after all codes are used up,
  // and compression rate changes, start over.
  boolean clear_flg = false;

  // Algorithm:  use open addressing double hashing (no chaining) on the
  // prefix code / next character combination.  We do a variant of Knuth's
  // algorithm D (vol. 3, sec. 6.4) along with G. Knott's relatively-prime
  // secondary probe.  Here, the modular division first probe is gives way
  // to a faster exclusive-or manipulation.  Also do block compression with
  // an adaptive reset, whereby the code table is cleared when the compression
  // ratio decreases, but after the table fills.  The variable-length output
  // codes are re-sized at this point, and a special CLEAR code is generated
  // for the decompressor.  Late addition:  construct the table according to
  // file size for noticeable speed improvement on small files.  Please direct
  // questions about this implementation to ames!jaw.

  int g_init_bits;

  int ClearCode;
  int EOFCode;

  // output
  //
  // Output the given code.
  // Inputs:
  //      code:   A n_bits-bit integer.  If == -1, then EOF.  This assumes
  //              that n_bits =< wordsize - 1.
  // Outputs:
  //      Outputs code to the file.
  // Assumptions:
  //      Chars are 8 bits long.
  // Algorithm:
  //      Maintain a BITS character long buffer (so that 8 codes will
  // fit in it exactly).  Use the VAX insv instruction to insert each
  // code in turn.  When the buffer fills up empty it and start over.

  int cur_accum = 0;
  int cur_bits = 0;

  int masks[] = { 0x0000, 0x0001, 0x0003, 0x0007, 0x000F,
              0x001F, 0x003F, 0x007F, 0x00FF,
              0x01FF, 0x03FF, 0x07FF, 0x0FFF,
              0x1FFF, 0x3FFF, 0x7FFF, 0xFFFF };

  // Number of characters so far in this 'packet'
  int a_count;

  // Define the storage for the packet accumulator
  byte[] accum = new byte[256];


  //----------------------------------------------------------------------------
  LZWEncoder(int width, int height, byte[] pixels, int color_depth) {
   imgW = width;
   imgH = height;
   pixAry = pixels;
   initCodeSize = Math.max(2, color_depth);
  }


  // Add a character to the end of the current packet, and if it is 254
  // characters, flush the packet to disk.
  void char_out( byte c, OutputStream outs ) throws IOException
     {
     accum[a_count++] = c;
     if ( a_count >= 254 )
        flush_char( outs );
     }


  // Clear out the hash table

  // table clear for block compress
  void cl_block( OutputStream outs ) throws IOException
     {
     cl_hash( hsize );
     free_ent = ClearCode + 2;
     clear_flg = true;

     output( ClearCode, outs );
     }


  // reset code table
  void cl_hash( int hsize )
     {
     for ( int i = 0; i < hsize; ++i )
        htab[i] = -1;
     }


  void compress( int init_bits, OutputStream outs ) throws IOException
     {
     int fcode;
     int i /* = 0 */;
     int c;
     int ent;
     int disp;
     int hsize_reg;
     int hshift;

     // Set up the globals:  g_init_bits - initial number of bits
     g_init_bits = init_bits;

     // Set up the necessary values
     clear_flg = false;
     n_bits = g_init_bits;
     maxcode = MAXCODE( n_bits );

     ClearCode = 1 << ( init_bits - 1 );
     EOFCode = ClearCode + 1;
     free_ent = ClearCode + 2;

     a_count = 0;  // clear packet

     ent = nextPixel();

     hshift = 0;
     for ( fcode = hsize; fcode < 65536; fcode *= 2 )
        ++hshift;
     hshift = 8 - hshift;         // set hash code range bound

     hsize_reg = hsize;
     cl_hash( hsize_reg );        // clear hash table

     output( ClearCode, outs );

     outer_loop:
     while ( (c = nextPixel()) != EOF )
        {
        fcode = ( c << maxbits ) + ent;
        i = ( c << hshift ) ^ ent;   // xor hashing

        if ( htab[i] == fcode )
           {
           ent = codetab[i];
           continue;
           }
        else if ( htab[i] >= 0 )     // non-empty slot
           {
           disp = hsize_reg - i;  // secondary hash (after G. Knott)
           if ( i == 0 )
              disp = 1;
           do
              {
              if ( (i -= disp) < 0 )
                 i += hsize_reg;

              if ( htab[i] == fcode )
                 {
                 ent = codetab[i];
                 continue outer_loop;
                 }
              }
           while ( htab[i] >= 0 );
           }
        output( ent, outs );
        ent = c;
        if ( free_ent < maxmaxcode )
           {
           codetab[i] = free_ent++;  // code -> hashtable
           htab[i] = fcode;
           }
        else
           cl_block( outs );
        }
     // Put out the final code.
     output( ent, outs );
     output( EOFCode, outs );
     }


  //----------------------------------------------------------------------------
  void encode(OutputStream os) throws IOException
  {
   os.write(initCodeSize);         // write "initial code size" byte

   remaining = imgW * imgH;        // reset navigation variables
   curPixel = 0;

   compress(initCodeSize + 1, os); // compress and write the pixel data

   os.write(0);                    // write block terminator
  }


  // Flush the packet to disk, and reset the accumulator
  void flush_char( OutputStream outs ) throws IOException
     {
     if ( a_count > 0 )
        {
        outs.write( a_count );
        outs.write( accum, 0, a_count );
        a_count = 0;
        }
     }


  final int MAXCODE( int n_bits )
     {
     return ( 1 << n_bits ) - 1;
     }


  //----------------------------------------------------------------------------
  // Return the next pixel from the image
  //----------------------------------------------------------------------------
  private int nextPixel()
  {
   if (remaining == 0)
     return EOF;

   --remaining;

   byte pix = pixAry[curPixel++];

   return pix & 0xff;
  }


  void output( int code, OutputStream outs ) throws IOException
     {
     cur_accum &= masks[cur_bits];

     if ( cur_bits > 0 )
        cur_accum |= ( code << cur_bits );
     else
        cur_accum = code;

     cur_bits += n_bits;

     while ( cur_bits >= 8 )
        {
        char_out( (byte) ( cur_accum & 0xff ), outs );
        cur_accum >>= 8;
        cur_bits -= 8;
        }

     // If the next entry is going to be too big for the code size,
     // then increase it, if possible.
    if ( free_ent > maxcode || clear_flg )
        {
        if ( clear_flg )
           {
           maxcode = MAXCODE(n_bits = g_init_bits);
           clear_flg = false;
           }
        else
           {
           ++n_bits;
           if ( n_bits == maxbits )
              maxcode = maxmaxcode;
           else
              maxcode = MAXCODE(n_bits);
           }
        }

     if ( code == EOFCode )
        {
        // At EOF, write the rest of the buffer.
        while ( cur_bits > 0 )
           {
           char_out( (byte) ( cur_accum & 0xff ), outs );
           cur_accum >>= 8;
           cur_bits -= 8;
           }

        flush_char( outs );
        }
     }
}

