package ijx.plugin.readwrite;

import ijx.plugin.api.PlugIn;
import ijx.process.ImageProcessor;
import ijx.process.ColorProcessor;
import ijx.process.ByteProcessor;
import ijx.process.ShortProcessor;
import ijx.io.OpenDialog;
import ijx.io.FileInfo;
import ijx.IJ;
import ijx.ImagePlus;

import ijx.IjxImageStack;
import java.io.*;

/**
 * This plugin opens PxM format images.
 * <p/>
 * The portable graymap format is a lowest common denominator
 * grayscale file format. The definition is as follows:
 * <p/>
 * - A "magic number" for identifying the  file  type.   A  pgm
 * file's magic number is the two characters "P2".
 * - Whitespace (blanks, TABs, CRs, LFs).
 * - A width, formatted as ASCII characters in decimal.
 * - Whitespace.
 * - A height, again in ASCII decimal.
 * - Whitespace.
 * - The maximum gray value, again in ASCII decimal.
 * - Whitespace.
 * - Width * height gray values, each in ASCII decimal, between
 * 0 and the specified maximum value, separated by whi-
 * tespace, starting at the top-left corner of the graymap,
 * proceeding in normal English reading order. A value of 0
 * means black, and the maximum value means white.
 * - Characters from a "#" to the next end-of-line are ignored (comments).
 * - No line should be longer than 70 characters.
 * <p/>
 * Here is an example of a small graymap in this format:
 * P2
 * # feep.pgm
 * 24 7
 * 15
 * 0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0
 * 0  3  3  3  3  0  0  7  7  7  7  0  0 11 11 11 11  0  0 15 15 15 15  0
 * 0  3  0  0  0  0  0  7  0  0  0  0  0 11  0  0  0  0  0 15  0  0 15  0
 * 0  3  3  3  0  0  0  7  7  7  0  0  0 11 11 11  0  0  0 15 15 15 15  0
 * 0  3  0  0  0  0  0  7  0  0  0  0  0 11  0  0  0  0  0 15  0  0  0  0
 * 0  3  0  0  0  0  0  7  7  7  7  0  0 11 11 11 11  0  0 15  0  0  0  0
 * 0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0
 * <p/>
 * There is a  PGM variant that stores the pixel data as raw bytes:
 * <p/>
 * -The "magic number" is "P5" instead of "P2".
 * -The gray values are stored as plain bytes, instead of ASCII decimal.
 * -No whitespace is allowed in the grays section, and only a single
 * character of whitespace (typically a newline) is allowed after the maxval.
 * -The files are smaller and many times faster to read and write.
 * <p/>
 * Kai Barthel Nov 16 2004:
 * Extended to support PPM (portable pixmap) format images (24 bits only).
 * -The "magic numbers" are "P6" (raw) "P3" (ASCII).
 * <p/>
 * Ulf Dittmer April 2005:
 * Extended to support PBM (bitmap) images (P1 and P4)
 * <p/>
 * Jarek Sacha (jarek.at.ieee.org) December 2005:
 * Extended PPM support to 48 bit color images.
 */

public class PGM_Reader extends ImagePlus implements PlugIn {

    private int width, height;
    private boolean rawBits;
    private boolean sixteenBits;
    private boolean isColor;
    private boolean isBlackWhite;
    private int maxValue;

    public void run(String arg) {
        OpenDialog od = new OpenDialog("PBM/PGM/PPM Reader...", arg);
        String directory = od.getDirectory();
        String name = od.getFileName();
        if (name == null)
            return;
        String path = directory + name;

        IJ.showStatus("Opening: " + path);
        IjxImageStack stack;
        try {
            stack = openFile(path);
        }
        catch (IOException e) {
            String msg = e.getMessage();
            IJ.showMessage("PBM/PGM/PPM Reader", msg.equals("") ? "" + e : msg);
            return;
        }
        setStack(name, stack);
        FileInfo fi = new FileInfo();
        fi.fileFormat = FileInfo.PGM;
        fi.directory = directory;
        fi.fileName = name;
        setFileInfo(fi);
        if (arg.equals("")) show();
    }

    public IjxImageStack openFile(String path) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(path));
        try {
			StreamTokenizer tok = new StreamTokenizer(is); //deprecated, but it works
			//Reader r = new BufferedReader(new InputStreamReader(is));
			//StreamTokenizer tok = new StreamTokenizer(r);  // doesn't work
			tok.resetSyntax();
			tok.wordChars(33, 255);
			tok.whitespaceChars(0, ' ');
			tok.parseNumbers();
			tok.eolIsSignificant(true);
			tok.commentChar('#');
			openHeader(tok);
			//IJ.log("PGM_Reader: w="+width+",h="+height+",raw="+rawBits+",16bits="+sixteenBits+",color="+isColor+",b&w="+isBlackWhite+",max="+maxValue);
				
			if (!isColor && sixteenBits) { // 16-bit grayscale
				if (rawBits) {
					ImageProcessor ip = open16bitRawImage(is, width, height);
					IjxImageStack stack = IJ.getFactory().newImageStack(width, height);
					stack.addSlice("", ip);
					return stack;
				} else {
					ImageProcessor ip = open16bitAsciiImage(tok, width, height);
					IjxImageStack stack = IJ.getFactory().newImageStack(width, height);
					stack.addSlice("", ip);
					return stack;
				}
			}
			
			if (!isColor) { // 8-bit grayscale
				byte[] pixels = new byte[width * height];
				ImageProcessor ip = new ByteProcessor(width, height, pixels, null);
				if (rawBits)
					openRawImage(is, width * height, pixels);
				else
					openAsciiImage(tok, width * height, pixels);
				for (int i = pixels.length - 1; i >= 0; i--) {
					if (isBlackWhite) {
						if (rawBits) {
							if (i < (pixels.length / 8)) {
								for (int bit = 7; bit >= 0; bit--) {
									pixels[8 * i + 7 - bit] = (byte) ((pixels[i] & ((int) Math.pow(2, bit))) == 0 ? 255 : 0);
								}
							}
						} else
							pixels[i] = (byte) (pixels[i] == 0 ? 255 : 0);
					} else
						pixels[i] = (byte) (0xff & (255 * (int) (0xff & pixels[i]) / maxValue));
				}
				IjxImageStack stack = IJ.getFactory().newImageStack(width, height);
				stack.addSlice("", ip);
				return stack;
			}
			
			if (!sixteenBits) { // 8-bit color
				int[] pixels = new int[width * height];
				byte[] bytePixels = new byte[3 * width * height];
				ImageProcessor ip = new ColorProcessor(width, height, pixels);
				if (rawBits)
					openRawImage(is, 3 * width * height, bytePixels);
				else
					openAsciiImage(tok, 3 * width * height, bytePixels);
				for (int i = 0; i < width * height; i++) {
					int r = (int) (0xff & bytePixels[i * 3]);
					int g = (int) (0xff & bytePixels[i * 3 + 1]);
					int b = (int) (0xff & bytePixels[i * 3 + 2]);
					r = (r * 255 / maxValue) << 16;
					g = (g * 255 / maxValue) << 8;
					b = (b * 255 / maxValue);
					pixels[i] = 0xFF000000 | r | g | b;
				}
				IjxImageStack stack = IJ.getFactory().newImageStack(width, height);
				stack.addSlice("", ip);
				return stack;
			}
			
			// 16-bit raw color
			short[] red = new short[width*height];
			short[] green = new short[width*height];
			short[] blue = new short[width*height];
			if (rawBits) {
				byte[] bytePixels = new byte[6*width*height];
				openRawImage(is, 6*width*height, bytePixels);
				for (int i=0; i<width*height; i++) {
					int r1 = 0xff & bytePixels[i*6];
					int r2 = 0xff & bytePixels[i*6+1];
					int g1 = 0xff & bytePixels[i*6+2];
					int g2 = 0xff & bytePixels[i*6+3];
					int b1 = 0xff & bytePixels[i*6+ 4];
					int b2 = 0xff & bytePixels[i*6+5];
					red[i] = (short) (0xffff & (r1*256+r2));
					green[i] = (short) (0xffff & (g1*256+g2));
					blue[i] = (short) (0xffff & (b1*256+b2));
				}
			} else {
				ImageProcessor ip = open16bitAsciiImage(tok, 3*width, height);
				short[] pixels = (short[])ip.getPixels();
				for (int i=0; i<width*height; i++) {
					red[i] = (short)(pixels[i*3]&0xffffff);
					green[i] = (short)(pixels[i*3+1]&0xffffff);
					blue[i] = (short)(pixels[i*3+2]&0xffffff);
				}
			}
			IjxImageStack stack = IJ.getFactory().newImageStack(width, height);
			stack.addSlice("red", new ShortProcessor(width, height, red, null));
			stack.addSlice("green", new ShortProcessor(width, height, green, null));
			stack.addSlice("blue", new ShortProcessor(width, height, blue, null));
			return stack;
        } finally {
            if (is!=null) is.close();
        }
   }

    public void openHeader(StreamTokenizer tok) throws IOException {
        String magicNumber = getWord(tok);
        if (magicNumber.equals("P1")) {
            rawBits = false;
            isColor = false;
            isBlackWhite = true;
        } else if (magicNumber.equals("P4")) {
            rawBits = true;
            isColor = false;
            isBlackWhite = true;
        } else if (magicNumber.equals("P2")) {
            rawBits = false;
            isColor = false;
            isBlackWhite = false;
        } else if (magicNumber.equals("P5")) {
            rawBits = true;
            isColor = false;
            isBlackWhite = false;
        } else if (magicNumber.equals("P3")) {
            rawBits = false;
            isColor = true;
            isBlackWhite = false;
        } else if (magicNumber.equals("P6")) {
            rawBits = true;
            isColor = true;
            isBlackWhite = false;
        } else
            throw new IOException("PxM files must start with \"P1\" or \"P2\" or \"P3\" or \"P4\" or \"P5\" or \"P6\"");
        width = getInt(tok);
        height = getInt(tok);
        if (width == -1 || height == -1)
            throw new IOException("Error opening PxM header..");
        if (! isBlackWhite) {
            maxValue = getInt(tok);
            if (maxValue == -1)
                throw new IOException("Error opening PxM header..");
            sixteenBits = maxValue > 255;
        } else
            maxValue = 255;
        if (sixteenBits && maxValue > 65535)
            throw new IOException("The maximum gray value is larger than 65535.");
    }

    public void openAsciiImage(StreamTokenizer tok, int size, byte[] pixels) throws IOException {
        int i = 0;
        int inc = size/20;
        if (inc==0) inc = 1;
        while (tok.nextToken() != tok.TT_EOF) {
            if (tok.ttype == tok.TT_NUMBER) {
                pixels[i++] = (byte) (((int) tok.nval) & 255);
                if (i%inc==0)
                    IJ.showProgress(0.5 + ((double) i / size) / 2.0);
            }
        }
        IJ.showProgress(1.0);
    }

    public void openRawImage(InputStream is, int size, byte[] pixels) throws IOException {
        int count = 0;
        while (count < size && count >= 0)
            count = is.read(pixels, count, size - count);
    }

    public ImageProcessor open16bitRawImage(InputStream is, int width, int height) throws IOException {
        int size = width * height * 2;
        byte[] bytes = new byte[size];
        int count = 0;
        while (count < size && count >= 0)
            count = is.read(bytes, count, size - count);
        short[] pixels = new short[size / 2];
        for (int i = 0, j = 0; i < size / 2; i++, j += 2)
            pixels[i] = (short) (((bytes[j] & 0xff) << 8) | (bytes[j + 1] & 0xff)); //big endian
        return new ShortProcessor(width, height, pixels, null);
    }

    public ImageProcessor open16bitAsciiImage(StreamTokenizer tok, int width, int height) throws IOException {
        int i = 0;
        int size = width * height;
        int inc = size/20; // Progress update interval
        if (inc==0) inc = 1;
        short[] pixels = new short[size];
        while (tok.nextToken() != tok.TT_EOF) {
            if (tok.ttype == tok.TT_NUMBER) {
                pixels[i++] = (short) (((int) tok.nval) & 65535);
                if (i%inc==0)
                    IJ.showProgress(0.5 + ((double) i / size) / 2.0);
            }
        }
        IJ.showProgress(1.0);
        return new ShortProcessor(width, height, pixels, null);
    }

    String getWord(StreamTokenizer tok) throws IOException {
        while (tok.nextToken() != tok.TT_EOF) {
            if (tok.ttype == tok.TT_WORD)
                return tok.sval;
        }
        return null;
    }

    int getInt(StreamTokenizer tok) throws IOException {
        while (tok.nextToken() != tok.TT_EOF) {
            if (tok.ttype == tok.TT_NUMBER)
                return (int) tok.nval;
        }
        return -1;
    }

}
