package ij.plugin;
import java.io.*;
import java.util.*;
import java.net.URL;
import ij.*;
import ij.io.*;
import ij.process.*;
import ij.util.Tools;
import ij.measure.Calibration;

/** This plugin decodes DICOM files. If 'arg' is empty, it
	displays a file open dialog and opens and displays the 
	image selected by the user. If 'arg' is a path, it opens the 
	specified image and the calling routine can display it using
	"((ImagePlus)IJ.runPlugIn("ij.plugin.DICOM", path)).show()".
	*/

/* RAK (Richard Kirk, rak@cre.canon.co.uk) changes 14/7/99

   InputStream.skip() looped to check the actual number of
   bytes is read.

   Big/little-endian options on element length.

   Explicit check for each known VR to make mistaken identifications
   of explicit VR's less likely.

   Variables b1..b4 renamed as b0..b3.

   Increment of 4 to offset on (7FE0,0010) tag removed.

   Queries on some other unrecognized tags.
   Anyone want to claim them?

   RAK changes 15/7/99

   Bug fix on magic values for explicit VRs with 32-bit lengths.

   Various bits of tidying up, including...
   'location' incremented on read using getByte() or getString().
   simpler debug mode message generation (values no longer reported).

   Added z pixel aspect ratio support for multi-slice DICOM volumes.
   Michael Abramoff, 31-10-2000

   Added DICOM tags to the dictionary (now contains about 2700 tags).
   implemented getDouble() for VR = FD (Floating Double) and getFloat()
   for VR = FL (Floating Single).
   Extended case statement in getHeaderInfo to retrieve FD and FL values.
   Johannes Hermen, Christian Moll, 25-04-2008

   */

public class DICOM extends ImagePlus implements PlugIn {
	private boolean showErrors = true;
	private BufferedInputStream inputStream;
	
	/** Default constructor. */
	public DICOM() {
	}

	/** Constructs a DICOM reader that using an InputStream. Here 
		is an example that shows how to open and display a DICOM:
		<pre>
		DICOM dcm = new DICOM(is);
		dcm.run("Name");
		dcm.show();
		<pre>
	*/
	public DICOM(InputStream is) {
		this(new BufferedInputStream(is));
	}

	/** Constructs a DICOM reader that using an BufferredInputStream. */
	public DICOM(BufferedInputStream bis) {
		inputStream = bis;
	}

	public void run(String arg) {
		OpenDialog od = new OpenDialog("Open Dicom...", arg);
		String directory = od.getDirectory();
		String fileName = od.getFileName();
		if (fileName==null)
			return;
		//IJ.showStatus("Opening: " + directory + fileName);
		DicomDecoder dd = new DicomDecoder(directory, fileName);
		dd.inputStream = inputStream;
		FileInfo fi = null;
		try {fi = dd.getFileInfo();}
		catch (IOException e) {
			String msg = e.getMessage();
			IJ.showStatus("");
			if (msg.indexOf("EOF")<0&&showErrors) {
				IJ.error("DicomDecoder", e.getClass().getName()+"\n \n"+msg);
				return;
			} else if (!dd.dicmFound()&&showErrors) {
				msg = "This does not appear to be a valid\n"
				+ "DICOM file. It does not have the\n"
				+ "characters 'DICM' at offset 128.";
				IJ.error("DicomDecoder", msg);
				return;
			}
		}
		if (fi!=null && fi.width>0 && fi.height>0 && fi.offset>0) {
			FileOpener fo = new FileOpener(fi);
			ImagePlus imp = fo.open(false);
			ImageProcessor ip = imp.getProcessor();
			if (fi.fileType==FileInfo.GRAY16_SIGNED) {
				if (dd.rescaleIntercept!=0.0 && dd.rescaleSlope==1.0)
					ip.add(dd.rescaleIntercept);
			} else if (dd.rescaleIntercept!=0.0 && (dd.rescaleSlope==1.0||fi.fileType==FileInfo.GRAY8)) {
				double[] coeff = new double[2];
				coeff[0] = dd.rescaleIntercept;
				coeff[1] = dd.rescaleSlope;
				imp.getCalibration().setFunction(Calibration.STRAIGHT_LINE, coeff, "gray value");
			}
			if (dd.windowWidth>0.0) {
				double min = dd.windowCenter-dd.windowWidth/2;
				double max = dd.windowCenter+dd.windowWidth/2;
				Calibration cal = imp.getCalibration();
				min = cal.getRawValue(min);
				max = cal.getRawValue(max);
				ip.setMinAndMax(min, max);
				if (IJ.debugMode) IJ.log("window: "+min+"-"+max);
			}
			if (imp.getStackSize()>1)
				setStack(fileName, imp.getStack());
			else
				setProcessor(fileName, imp.getProcessor());
			setCalibration(imp.getCalibration());
			setProperty("Info", dd.getDicomInfo());
			setFileInfo(fi); // needed for revert
			if (arg.equals("")) show();
		} else if (showErrors)
			IJ.error("DicomDecoder","Unable to decode DICOM header.");
		IJ.showStatus("");
	}

	/** Opens the specified file as a DICOM. Does not 
		display a message if there is an error.
		Here is an example:
		<pre>
		DICOM dcm = new DICOM();
		dcm.open(path);
		if (dcm.getWidth()==0)
			IJ.log("Error opening '"+path+"'");
		else
			dcm.show();
		</pre>
	*/
	public void open(String path) {
		showErrors = false;
		run(path);
	}
	
	/** Convert 16-bit signed to unsigned if all pixels>=0. */
	void convertToUnsigned(ImagePlus imp, FileInfo fi) {
		ImageProcessor ip = imp.getProcessor();
		short[] pixels = (short[])ip.getPixels();
		int min = Integer.MAX_VALUE;
		int value;
		for (int i=0; i<pixels.length; i++) {
			value = pixels[i]&0xffff;
			if (value<min)
				min = value;
		}
		if (IJ.debugMode) IJ.log("min: "+(min-32768));
		if (min>=32768) {
			for (int i=0; i<pixels.length; i++)
				pixels[i] = (short)(pixels[i]-32768);
			ip.resetMinAndMax();
			Calibration cal = imp.getCalibration();
			cal.setFunction(Calibration.NONE, null, "Gray Value");
			fi.fileType = FileInfo.GRAY16_UNSIGNED;
		}
	}

}


class DicomDecoder {

	private static final int PIXEL_REPRESENTATION = 0x00280103;
	private static final int TRANSFER_SYNTAX_UID = 0x00020010;
	private static final int SLICE_THICKNESS = 0x00180050;
	private static final int SLICE_SPACING = 0x00180088;
	private static final int SAMPLES_PER_PIXEL = 0x00280002;
	private static final int PHOTOMETRIC_INTERPRETATION = 0x00280004;
	private static final int PLANAR_CONFIGURATION = 0x00280006;
	private static final int NUMBER_OF_FRAMES = 0x00280008;
	private static final int ROWS = 0x00280010;
	private static final int COLUMNS = 0x00280011;
	private static final int PIXEL_SPACING = 0x00280030;
	private static final int BITS_ALLOCATED = 0x00280100;
	private static final int WINDOW_CENTER = 0x00281050;
	private static final int WINDOW_WIDTH = 0x00281051;	
	private static final int RESCALE_INTERCEPT = 0x00281052;
	private static final int RESCALE_SLOPE = 0x00281053;
	private static final int RED_PALETTE = 0x00281201;
	private static final int GREEN_PALETTE = 0x00281202;
	private static final int BLUE_PALETTE = 0x00281203;
	private static final int ICON_IMAGE_SEQUENCE = 0x00880200;
	private static final int ITEM = 0xFFFEE000;
	private static final int ITEM_DELIMINATION = 0xFFFEE00D;
	private static final int SEQUENCE_DELIMINATION = 0xFFFEE0DD;
	private static final int PIXEL_DATA = 0x7FE00010;

	private static final int AE=0x4145, AS=0x4153, AT=0x4154, CS=0x4353, DA=0x4441, DS=0x4453, DT=0x4454,
		FD=0x4644, FL=0x464C, IS=0x4953, LO=0x4C4F, LT=0x4C54, PN=0x504E, SH=0x5348, SL=0x534C, 
		SS=0x5353, ST=0x5354, TM=0x544D, UI=0x5549, UL=0x554C, US=0x5553, UT=0x5554,
		OB=0x4F42, OW=0x4F57, SQ=0x5351, UN=0x554E, QQ=0x3F3F;
		
	private static Properties dictionary;

	private String directory, fileName;
	private static final int ID_OFFSET = 128;  //location of "DICM"
	private static final String DICM = "DICM";
	
	private BufferedInputStream f;
	private int location = 0;
	private boolean littleEndian = true;
	
	private int elementLength;
	private int vr;  // Value Representation
	private static final int IMPLICIT_VR = 0x2D2D; // '--' 
	private byte[] vrLetters = new byte[2];
 	private int previousGroup;
 	private String previousInfo;
 	private StringBuffer dicomInfo = new StringBuffer(1000);
 	private boolean dicmFound; // "DICM" found at offset 128
 	private boolean oddLocations;  // one or more tags at odd locations
 	private boolean bigEndianTransferSyntax = false;
	double windowCenter, windowWidth;
	double rescaleIntercept, rescaleSlope;
	boolean inSequence;
 	BufferedInputStream inputStream;

	public DicomDecoder(String directory, String fileName) {
		this.directory = directory;
		this.fileName = fileName;
		String path = null;
		if (dictionary==null && IJ.getApplet()==null) {
			path = Prefs.getHomeDir()+File.separator+"DICOM_Dictionary.txt";
			File f = new File(path);
			if (f.exists()) try {
				dictionary = new Properties();
				InputStream is = new BufferedInputStream(new FileInputStream(f));
				dictionary.load(is);
				is.close();
				if (IJ.debugMode) IJ.log("DicomDecoder: using "+dictionary.size()+" tag dictionary at "+path);
			} catch (Exception e) {
				dictionary = null;
			}
		}
		if (dictionary==null) {
			DicomDictionary d = new DicomDictionary();
			dictionary = d.getDictionary();
			if (IJ.debugMode) IJ.log("DicomDecoder: "+path+" not found; using "+dictionary.size()+" tag built in dictionary");
		}
	}
  
	String getString(int length) throws IOException {
		byte[] buf = new byte[length];
		int pos = 0;
		while (pos<length) {
			int count = f.read(buf, pos, length-pos);
			pos += count;
		}
		location += length;
		return new String(buf);
	}
  
	int getByte() throws IOException {
		int b = f.read();
		if (b ==-1) throw new IOException("unexpected EOF");
		++location;
		return b;
	}

	int getShort() throws IOException {
		int b0 = getByte();
		int b1 = getByte();
		if (littleEndian)
			return ((b1 << 8) + b0);
		else
			return ((b0 << 8) + b1);
	}
  
	final int getInt() throws IOException {
		int b0 = getByte();
		int b1 = getByte();
		int b2 = getByte();
		int b3 = getByte();
		if (littleEndian)
			return ((b3<<24) + (b2<<16) + (b1<<8) + b0);
		else
			return ((b0<<24) + (b1<<16) + (b2<<8) + b3);
	}

	double getDouble() throws IOException {
		int b0 = getByte();
		int b1 = getByte();
		int b2 = getByte();
		int b3 = getByte();
		int b4 = getByte();
		int b5 = getByte();
		int b6 = getByte();
		int b7 = getByte();
		long res = 0;
		if (littleEndian) {
			res += b0;
			res += ( ((long)b1) << 8);
			res += ( ((long)b2) << 16);
			res += ( ((long)b3) << 24);
			res += ( ((long)b4) << 32);
			res += ( ((long)b5) << 40);
			res += ( ((long)b6) << 48);
			res += ( ((long)b7) << 56);         
		} else {
			res += b7;
			res += ( ((long)b6) << 8);
			res += ( ((long)b5) << 16);
			res += ( ((long)b4) << 24);
			res += ( ((long)b3) << 32);
			res += ( ((long)b2) << 40);
			res += ( ((long)b1) << 48);
			res += ( ((long)b0) << 56);
		}
		return Double.longBitsToDouble(res);
	}
    
	float getFloat() throws IOException {
		int b0 = getByte();
		int b1 = getByte();
		int b2 = getByte();
		int b3 = getByte();
		int res = 0;
		if (littleEndian) {
			res += b0;
			res += ( ((long)b1) << 8);
			res += ( ((long)b2) << 16);
			res += ( ((long)b3) << 24);     
		} else {
			res += b3;
			res += ( ((long)b2) << 8);
			res += ( ((long)b1) << 16);
			res += ( ((long)b0) << 24);
		}
		return Float.intBitsToFloat(res);
	}
  
	byte[] getLut(int length) throws IOException {
		if ((length&1)!=0) { // odd
			String dummy = getString(length);
			return null;
		}
		length /= 2;
		byte[] lut = new byte[length];
		for (int i=0; i<length; i++)
			lut[i] = (byte)(getShort()>>>8);
		return lut;
	}
  
  	int getLength() throws IOException {
		int b0 = getByte();
		int b1 = getByte();
		int b2 = getByte();
		int b3 = getByte();
		
		// We cannot know whether the VR is implicit or explicit
		// without the full DICOM Data Dictionary for public and
		// private groups.
		
		// We will assume the VR is explicit if the two bytes
		// match the known codes. It is possible that these two
		// bytes are part of a 32-bit length for an implicit VR.
		
		vr = (b0<<8) + b1;
		
		switch (vr) {
			case OB: case OW: case SQ: case UN: case UT:
				// Explicit VR with 32-bit length if other two bytes are zero
				if ( (b2 == 0) || (b3 == 0) ) return getInt();
				// Implicit VR with 32-bit length
				vr = IMPLICIT_VR;
				if (littleEndian)
					return ((b3<<24) + (b2<<16) + (b1<<8) + b0);
				else
					return ((b0<<24) + (b1<<16) + (b2<<8) + b3);		
			case AE: case AS: case AT: case CS: case DA: case DS: case DT:  case FD:
			case FL: case IS: case LO: case LT: case PN: case SH: case SL: case SS:
			case ST: case TM:case UI: case UL: case US: case QQ:
				// Explicit vr with 16-bit length
				if (littleEndian)
					return ((b3<<8) + b2);
				else
					return ((b2<<8) + b3);
			default:
				// Implicit VR with 32-bit length...
				vr = IMPLICIT_VR;
				if (littleEndian)
					return ((b3<<24) + (b2<<16) + (b1<<8) + b0);
				else
					return ((b0<<24) + (b1<<16) + (b2<<8) + b3);
		}
	}

	int getNextTag() throws IOException {
		int groupWord = getShort();
		if (groupWord==0x0800 && bigEndianTransferSyntax) {
			littleEndian = false;
			groupWord = 0x0008;
		}
		int elementWord = getShort();
		int tag = groupWord<<16 | elementWord;
		elementLength = getLength();
		
		// hack needed to read some GE files
		// The element length must be even!
		if (elementLength==13 && !oddLocations) elementLength = 10; 
		
		// "Undefined" element length.
		// This is a sort of bracket that encloses a sequence of elements.
		if (elementLength==-1) {
			elementLength = 0;
			inSequence = true;
		}
 		//IJ.log("getNextTag: "+tag+" "+elementLength);
		return tag;
	}
  
	FileInfo getFileInfo() throws IOException {
		long skipCount;
		FileInfo fi = new FileInfo();
		int bitsAllocated = 16;
		fi.fileFormat = fi.RAW;
		fi.fileName = fileName;
		if (directory.indexOf("://")>0) { // is URL
			URL u = new URL(directory+fileName);
			inputStream = new BufferedInputStream(u.openStream());
			fi.inputStream = inputStream;
		} else if (inputStream!=null)
			fi.inputStream = inputStream;
		else
			fi.directory = directory;
		fi.width = 0;
		fi.height = 0;
		fi.offset = 0;
		fi.intelByteOrder = true;
		fi.fileType = FileInfo.GRAY16_UNSIGNED;
		fi.fileFormat = FileInfo.DICOM;
		int samplesPerPixel = 1;
		int planarConfiguration = 0;
		String photoInterpretation = "";
				
		if (inputStream!=null) {
			// Use large buffer to allow URL stream to be reset after reading header
			f = inputStream;
			f.mark(400000);
		} else
			f = new BufferedInputStream(new FileInputStream(directory + fileName));
		if (IJ.debugMode) {
			IJ.log("");
			IJ.log("DicomDecoder: decoding "+fileName);
		}
		
		skipCount = (long)ID_OFFSET;
		while (skipCount > 0) skipCount -= f.skip( skipCount );
		location += ID_OFFSET;
		
		if (!getString(4).equals(DICM)) {
			if (inputStream==null) f.close();
			if (inputStream!=null)
				f.reset();
			else
				f = new BufferedInputStream(new FileInputStream(directory + fileName));
			location = 0;
			if (IJ.debugMode) IJ.log(DICM + " not found at offset "+ID_OFFSET+"; reseting to offset 0");
		} else {
			dicmFound = true;
			if (IJ.debugMode) IJ.log(DICM + " found at offset " + ID_OFFSET);
		}
		
		boolean decodingTags = true;
		boolean signed = false;
		
		while (decodingTags) {
			int tag = getNextTag();
			if ((location&1)!=0) // DICOM tags must be at even locations
				oddLocations = true;
			if (inSequence) {
				addInfo(tag, null);
				continue;
			}
			String s;
			switch (tag) {
				case TRANSFER_SYNTAX_UID:
					s = getString(elementLength);
					addInfo(tag, s);
					if (s.indexOf("1.2.4")>-1||s.indexOf("1.2.5")>-1) {
						f.close();
						String msg = "ImageJ cannot open compressed DICOM images.\n \n";
						msg += "Transfer Syntax UID = "+s;
						throw new IOException(msg);
					}
					if (s.indexOf("1.2.840.10008.1.2.2")>=0)
						bigEndianTransferSyntax = true;
					break;
				case NUMBER_OF_FRAMES:
					s = getString(elementLength);
					addInfo(tag, s);
					double frames = s2d(s);
					if (frames>1.0)
						fi.nImages = (int)frames;
					break;
				case SAMPLES_PER_PIXEL:
					samplesPerPixel = getShort();
					addInfo(tag, samplesPerPixel);
					break;
				case PHOTOMETRIC_INTERPRETATION:
					photoInterpretation = getString(elementLength);
					addInfo(tag, photoInterpretation);
					break;
				case PLANAR_CONFIGURATION:
					planarConfiguration = getShort();
					addInfo(tag, planarConfiguration);
					break;
				case ROWS:
					fi.height = getShort();
					addInfo(tag, fi.height);
					break;
				case COLUMNS:
					fi.width = getShort();
					addInfo(tag, fi.width);
					break;
				case PIXEL_SPACING:
					String scale = getString(elementLength);
					getSpatialScale(fi, scale);
					addInfo(tag, scale);
					break;
				case SLICE_THICKNESS: case SLICE_SPACING:
					String spacing = getString(elementLength);
					fi.pixelDepth = s2d(spacing);
					addInfo(tag, spacing);
					break;
				case BITS_ALLOCATED:
					bitsAllocated = getShort();
					if (bitsAllocated==8)
						fi.fileType = FileInfo.GRAY8;
					else if (bitsAllocated==32)
						fi.fileType = FileInfo.GRAY32_UNSIGNED;
					addInfo(tag, bitsAllocated);
					break;
				case PIXEL_REPRESENTATION:
					int pixelRepresentation = getShort();
					if (pixelRepresentation==1) {
						fi.fileType = FileInfo.GRAY16_SIGNED;
						signed = true;
					}
					addInfo(tag, pixelRepresentation);
					break;
				case WINDOW_CENTER:
					String center = getString(elementLength);
					int index = center.indexOf('\\');
					if (index!=-1) center = center.substring(index+1);
					windowCenter = s2d(center);
					addInfo(tag, center);
					break;
				case WINDOW_WIDTH:
					String width = getString(elementLength);
					index = width.indexOf('\\');
					if (index!=-1) width = width.substring(index+1);
					windowWidth = s2d(width);
					addInfo(tag, width);
					break;
				case RESCALE_INTERCEPT:
					String intercept = getString(elementLength);
					rescaleIntercept = s2d(intercept);
					addInfo(tag, intercept);
					break;
				case RESCALE_SLOPE:
					String slop = getString(elementLength);
					rescaleSlope = s2d(slop);
					addInfo(tag, slop);
					break;
				case RED_PALETTE:
					fi.reds = getLut(elementLength);
					addInfo(tag, elementLength/2);
					break;
				case GREEN_PALETTE:
					fi.greens = getLut(elementLength);
					addInfo(tag, elementLength/2);
					break;
				case BLUE_PALETTE:
					fi.blues = getLut(elementLength);
					addInfo(tag, elementLength/2);
					break;
				case PIXEL_DATA:
					// Start of image data...
					if (elementLength!=0) {
						fi.offset = location;
						addInfo(tag, location);
						decodingTags = false;
					} else
						addInfo(tag, null);
					break;
				case 0x7F880010:
					// What is this? - RAK
					if (elementLength!=0) {
						fi.offset = location+4;
						decodingTags = false;
					}
					break;
				default:
					// Not used, skip over it...
					addInfo(tag, null);
			}
		} // while(decodingTags)
		
		if (fi.fileType==FileInfo.GRAY8) {
			if (fi.reds!=null && fi.greens!=null && fi.blues!=null
			&& fi.reds.length==fi.greens.length
			&& fi.reds.length==fi.blues.length) {
				fi.fileType = FileInfo.COLOR8;
				fi.lutSize = fi.reds.length;
				
			}
		}
				
		if (fi.fileType==FileInfo.GRAY32_UNSIGNED && signed)
			fi.fileType = FileInfo.GRAY32_INT;

		if (samplesPerPixel==3 && photoInterpretation.startsWith("RGB")) {
			if (planarConfiguration==0)
				fi.fileType = FileInfo.RGB;
			else if (planarConfiguration==1)
				fi.fileType = FileInfo.RGB_PLANAR;
		} else if (photoInterpretation.endsWith("1 "))
				fi.whiteIsZero = true;
				
		if (!littleEndian)
			fi.intelByteOrder = false;
		
		if (IJ.debugMode) {
			IJ.log("width: " + fi.width);
			IJ.log("height: " + fi.height);
			IJ.log("images: " + fi.nImages);
			IJ.log("bits allocated: " + bitsAllocated);
			IJ.log("offset: " + fi.offset);
		}
	
		if (inputStream!=null)
			f.reset();
		else
			f.close();
		return fi;
	}
	
	String getDicomInfo() {
		String s = new String(dicomInfo);
		char[] chars = new char[s.length()];
		s.getChars(0, s.length(), chars, 0);
		for (int i=0; i<chars.length; i++) {
			if (chars[i]<' ' && chars[i]!='\n') chars[i] = ' ';
		}
		return new String(chars);
	}

	void addInfo(int tag, String value) throws IOException {
		String info = getHeaderInfo(tag, value);
		if (inSequence && info!=null && vr!=SQ) info = ">" + info;
		if (info!=null &&  tag!=ITEM) {
			int group = tag>>>16;
			//if (group!=previousGroup && (previousInfo!=null&&previousInfo.indexOf("Sequence:")==-1))
			//	dicomInfo.append("\n");
			previousGroup = group;
			previousInfo = info;
			dicomInfo.append(tag2hex(tag)+info+"\n");
		}
		if (IJ.debugMode) {
			if (info==null) info = "";
			vrLetters[0] = (byte)(vr >> 8);
			vrLetters[1] = (byte)(vr & 0xFF);
			String VR = new String(vrLetters);
			IJ.log("(" + tag2hex(tag) + VR
			+ " " + elementLength
			+ " bytes from "
			+ (location-elementLength)+") "
			+ info);
		}
	}

	void addInfo(int tag, int value) throws IOException {
		addInfo(tag, Integer.toString(value));
	}

	String getHeaderInfo(int tag, String value) throws IOException {
		if (tag==ITEM_DELIMINATION || tag==SEQUENCE_DELIMINATION) {
			inSequence = false;
			if (!IJ.debugMode) return null;
		}
		String key = i2hex(tag);
		//while (key.length()<8)
		//	key = '0' + key;
		String id = (String)dictionary.get(key);
		if (id!=null) {
			if (vr==IMPLICIT_VR && id!=null)
				vr = (id.charAt(0)<<8) + id.charAt(1);
			id = id.substring(2);
		}
		if (tag==ITEM)
			return id!=null?id+":":null;
		if (value!=null)
			return id+": "+value;
		switch (vr) {
			case FD:
				if (elementLength==8)
					value = Double.toString(getDouble());
				else
					for (int i=0; i<elementLength; i++) getByte();
				break;
			case FL:
				if (elementLength==4)
					value = Float.toString(getFloat());
				else
					for (int i=0; i<elementLength; i++) getByte();
				break;
			//case UT:
				//throw new IOException("ImageJ cannot read UT (unlimited text) DICOMs");
			case AE: case AS: case AT: case CS: case DA: case DS: case DT:  case IS: case LO: 
			case LT: case PN: case SH: case ST: case TM: case UI:
				value = getString(elementLength);
				break;
			case US:
				if (elementLength==2)
					value = Integer.toString(getShort());
				else {
					value = "";
					int n = elementLength/2;
					for (int i=0; i<n; i++)
						value += Integer.toString(getShort())+" ";
				}
				break;
			case IMPLICIT_VR:
				value = getString(elementLength);
				if (elementLength>44) value=null;
				break;
			case SQ:
				value = "";
				boolean privateTag = ((tag>>16)&1)!=0;
				if (tag!=ICON_IMAGE_SEQUENCE && !privateTag)
					break;
				// else fall through and skip icon image sequence or private sequence
			default:
				long skipCount = (long)elementLength;
				while (skipCount > 0) skipCount -= f.skip(skipCount);
				location += elementLength;
				value = "";
		}
		if (value!=null && id==null && !value.equals(""))
			return "---: "+value;
		else if (id==null)
			return null;
		else
			return id+": "+value;
	}

	static char[] buf8 = new char[8];
	
	/** Converts an int to an 8 byte hex string. */
	String i2hex(int i) {
		for (int pos=7; pos>=0; pos--) {
			buf8[pos] = Tools.hexDigits[i&0xf];
			i >>>= 4;
		}
		return new String(buf8);
	}

	char[] buf10;
	
	String tag2hex(int tag) {
		if (buf10==null) {
			buf10 = new char[11];
			buf10[4] = ',';
			buf10[9] = ' ';
		}
		int pos = 8;
		while (pos>=0) {
			buf10[pos] = Tools.hexDigits[tag&0xf];
			tag >>>= 4;
			pos--;
			if (pos==4) pos--; // skip coma
		}
		return new String(buf10);
	}
	
 	double s2d(String s) {
		Double d;
		try {d = new Double(s);}
		catch (NumberFormatException e) {d = null;}
		if (d!=null)
			return(d.doubleValue());
		else
			return(0.0);
	}
  
	void getSpatialScale(FileInfo fi, String scale) {
		double xscale=0, yscale=0;
		int i = scale.indexOf('\\');
		if (i>0) {
			yscale = s2d(scale.substring(0, i));
			xscale = s2d(scale.substring(i+1));
		}
		if (xscale!=0.0 && yscale!=0.0) {
			fi.pixelWidth = xscale;
			fi.pixelHeight = yscale;
			fi.unit = "mm";
		}
	}
	
	boolean dicmFound() {
		return dicmFound;
	}

}


class DicomDictionary {

	Properties getDictionary() {
		Properties p = new Properties();
		for (int i=0; i<dict.length; i++) {
			p.put(dict[i].substring(0,8), dict[i].substring(9));
		}
		return p;
	}

	String[] dict = {
		"00020002=UIMedia Storage SOP Class UID", 
		"00020003=UIMedia Storage SOP Inst UID",
		"00020010=UITransfer Syntax UID",
		"00020012=UIImplementation Class UID",
		"00020013=SHImplementation Version Name",
		"00020016=AESource Application Entity Title",
		
		"00080005=CSSpecific Character Set",
		"00080008=CSImage Type",
		"00080010=CSRecognition Code",
		"00080012=DAInstance Creation Date",
		"00080013=TMInstance Creation Time",
		"00080014=UIInstance Creator UID",
		"00080016=UISOP Class UID",
		"00080018=UISOP Instance UID",
		"00080020=DAStudy Date",
		"00080021=DASeries Date",
		"00080022=DAAcquisition Date",
		"00080023=DAImage Date",
		"00080024=DAOverlay Date",
		"00080025=DACurve Date",
		"00080030=TMStudy Time",
		"00080031=TMSeries Time",
		"00080032=TMAcquisition Time",
		"00080033=TMImage Time",
		"00080034=TMOverlay Time",
		"00080035=TMCurve Time",
		"00080040=USData Set Type",
		"00080041=LOData Set Subtype",
		"00080042=CSNuclear Medicine Series Type",
		"00080050=SHAccession Number",
		"00080052=CSQuery/Retrieve Level",
		"00080054=AERetrieve AE Title",
		"00080058=AEFailed SOP Instance UID List",
		"00080060=CSModality",
		"00080064=CSConversion Type",
		"00080068=CSPresentation Intent Type",
		"00080070=LOManufacturer",
		"00080080=LOInstitution Name",
		"00080081=STInstitution Address",
		"00080082=SQInstitution Code Sequence",
		"00080090=PNReferring Physician's Name",
		"00080092=STReferring Physician's Address",
		"00080094=SHReferring Physician's Telephone Numbers",
		"00080100=SHCode Value",
		"00080102=SHCoding Scheme Designator",
		"00080104=LOCode Meaning",
		"00080201=SHTimezone Offset From UTC",
		"00081010=SHStation Name",
		"00081030=LOStudy Description",
		"00081032=SQProcedure Code Sequence",
		"0008103E=LOSeries Description",
		"00081040=LOInstitutional Department Name",
		"00081048=PNPhysician(s) of Record",
		"00081050=PNAttending Physician's Name",
		"00081060=PNName of Physician(s) Reading Study",
		"00081070=PNOperator's Name",
		"00081080=LOAdmitting Diagnoses Description",
		"00081084=SQAdmitting Diagnosis Code Sequence",
		"00081090=LOManufacturer's Model Name",
		"00081100=SQReferenced Results Sequence",
		"00081110=SQReferenced Study Sequence",
		"00081111=SQReferenced Study Component Sequence",
		"00081115=SQReferenced Series Sequence",
		"00081120=SQReferenced Patient Sequence",
		"00081125=SQReferenced Visit Sequence",
		"00081130=SQReferenced Overlay Sequence",
		"00081140=SQReferenced Image Sequence",
		"00081145=SQReferenced Curve Sequence",
		"00081150=UIReferenced SOP Class UID",
		"00081155=UIReferenced SOP Instance UID",
		"00082111=STDerivation Description",
		"00082112=SQSource Image Sequence",
		"00082120=SHStage Name",
		"00082122=ISStage Number",
		"00082124=ISNumber of Stages",
		"00082129=ISNumber of Event Timers",
		"00082128=ISView Number",
		"0008212A=ISNumber of Views in Stage",
		"00082130=DSEvent Elapsed Time(s)",
		"00082132=LOEvent Timer Name(s)",
		"00082142=ISStart Trim",
		"00082143=ISStop Trim",
		"00082144=ISRecommended Display Frame Rate",
		"00082200=CSTransducer Position",
		"00082204=CSTransducer Orientation",
		"00082208=CSAnatomic Structure",
		
		"00100010=PNPatient's Name",
		"00100020=LOPatient ID",
		"00100021=LOIssuer of Patient ID",
		"00100030=DAPatient's Birth Date",
		"00100032=TMPatient's Birth Time",
		"00100040=CSPatient's Sex",
		"00101000=LOOther Patient IDs",
		"00101001=PNOther Patient Names",
		"00101005=PNPatient's Maiden Name",
		"00101010=ASPatient's Age",
		"00101020=DSPatient's Size",
		"00101030=DSPatient's Weight",
		"00101040=LOPatient's Address",
		"00102150=LOCountry of Residence",
		"00102152=LORegion of Residence",
		"00102180=SHOccupation",
		"001021A0=CSSmoking Status",
		"001021B0=LTAdditional Patient History",
		"00104000=LTPatient Comments",
		
		"00180010=LOContrast/Bolus Agent",
		"00180015=CSBody Part Examined",
		"00180020=CSScanning Sequence",
		"00180021=CSSequence Variant",
		"00180022=CSScan Options",
		"00180023=CSMR Acquisition Type",
		"00180024=SHSequence Name",
		"00180025=CSAngio Flag",
		"00180030=LORadionuclide",
		"00180031=LORadiopharmaceutical",
		"00180032=DSEnergy Window Centerline",
		"00180033=DSEnergy Window Total Width",
		"00180034=LOIntervention Drug Name",
		"00180035=TMIntervention Drug Start Time",
		"00180040=ISCine Rate",
		"00180050=DSSlice Thickness",
		"00180060=DSkVp",
		"00180070=ISCounts Accumulated",
		"00180071=CSAcquisition Termination Condition",
		"00180072=DSEffective Series Duration",
		"00180073=CSAcquisition Start Condition",
		"00180074=ISAcquisition Start Condition Data",
		"00180075=ISAcquisition Termination Condition Data",
		"00180080=DSRepetition Time",
		"00180081=DSEcho Time",
		"00180082=DSInversion Time",
		"00180083=DSNumber of Averages",
		"00180084=DSImaging Frequency",
		"00180085=SHImaged Nucleus",
		"00180086=ISEcho Numbers(s)",
		"00180087=DSMagnetic Field Strength",
		"00180088=DSSpacing Between Slices",
		"00180089=ISNumber of Phase Encoding Steps",
		"00180090=DSData Collection Diameter",
		"00180091=ISEcho Train Length",
		"00180093=DSPercent Sampling",
		"00180094=DSPercent Phase Field of View",
		"00180095=DSPixel Bandwidth",
		"00181000=LODevice Serial Number",
		"00181004=LOPlate ID",
		"00181010=LOSecondary Capture Device ID",
		"00181012=DADate of Secondary Capture",
		"00181014=TMTime of Secondary Capture",
		"00181016=LOSecondary Capture Device Manufacturer",
		"00181018=LOSecondary Capture Device Manufacturer's Model Name",
		"00181019=LOSecondary Capture Device Software Version(s)",
		"00181020=LOSoftware Versions(s)",
		"00181022=SHVideo Image Format Acquired",
		"00181023=LODigital Image Format Acquired",
		"00181030=LOProtocol Name",
		"00181040=LOContrast/Bolus Route",
		"00181041=DSContrast/Bolus Volume",
		"00181042=TMContrast/Bolus Start Time",
		"00181043=TMContrast/Bolus Stop Time",
		"00181044=DSContrast/Bolus Total Dose",
		"00181045=ISSyringe Counts",
		"00181050=DSSpatial Resolution",
		"00181060=DSTrigger Time",
		"00181061=LOTrigger Source or Type",
		"00181062=ISNominal Interval",
		"00181063=DSFrame Time",
		"00181064=LOFraming Type",
		"00181065=DSFrame Time Vector",
		"00181066=DSFrame Delay",
		"00181070=LORadionuclide Route",
		"00181071=DSRadionuclide Volume",
		"00181072=TMRadionuclide Start Time",
		"00181073=TMRadionuclide Stop Time",
		"00181074=DSRadionuclide Total Dose",
		"00181075=DSRadionuclide Half Life",
		"00181076=DSRadionuclide Positron Fraction",
		"00181080=CSBeat Rejection Flag",
		"00181081=ISLow R-R Value",
		"00181082=ISHigh R-R Value",
		"00181083=ISIntervals Acquired",
		"00181084=ISIntervals Rejected",
		"00181085=LOPVC Rejection",
		"00181086=ISSkip Beats",
		"00181088=ISHeart Rate",
		"00181090=ISCardiac Number of Images",
		"00181094=ISTrigger Window",
		"00181100=DSReconstruction Diameter",
		"00181110=DSDistance Source to Detector",
		"00181111=DSDistance Source to Patient",
		"00181120=DSGantry/Detector Tilt",
		"00181130=DSTable Height",
		"00181131=DSTable Traverse",
		"00181140=CSRotation Direction",
		"00181141=DSAngular Position",
		"00181142=DSRadial Position",
		"00181143=DSScan Arc",
		"00181144=DSAngular Step",
		"00181145=DSCenter of Rotation Offset",
		"00181146=DSRotation Offset",
		"00181147=CSField of View Shape",
		"00181149=ISField of View Dimensions(s)",
		"00181150=ISExposure Time",
		"00181151=ISX-ray Tube Current",
		"00181152=ISExposure",
		"00181153=ISExposure in uAs",
		"00181154=DSAverage Pulse Width",
		"00181155=CSRadiation Setting",
		"00181156=CSRectification Type",
		"0018115A=CSRadiation Mode",
		"0018115E=DSImage Area Dose Product",
		"00181160=SHFilter Type",
		"00181161=LOType of Filters",
		"00181162=DSIntensifier Size",
		"00181164=DSImager Pixel Spacing",
		"00181166=CSGrid",
		"00181170=ISGenerator Power",
		"00181180=SHCollimator/grid Name",
		"00181181=CSCollimator Type",
		"00181182=ISFocal Distance",
		"00181183=DSX Focus Center",
		"00181184=DSY Focus Center",
		"00181190=DSFocal Spot(s)",
		"00181191=CSAnode Target Material",
		"001811A0=DSBody Part Thickness",
		"001811A2=DSCompression Force",
		"00181200=DADate of Last Calibration",
		"00181201=TMTime of Last Calibration",
		"00181210=SHConvolution Kernel",
		"00181242=ISActual Frame Duration",
		"00181243=ISCount Rate",
		"00181250=SHReceiving Coil",
		"00181251=SHTransmitting Coil",
		"00181260=SHPlate Type",
		"00181261=LOPhosphor Type",
		"00181300=ISScan Velocity",
		"00181301=CSWhole Body Technique",
		"00181302=ISScan Length",
		"00181310=USAcquisition Matrix",
		"00181312=CSPhase Encoding Direction",
		"00181314=DSFlip Angle",
		"00181315=CSVariable Flip Angle Flag",
		"00181316=DSSAR",
		"00181318=DSdB/dt",
		"00181400=LOAcquisition Device Processing Description",
		"00181401=LOAcquisition Device Processing Code",
		"00181402=CSCassette Orientation",
		"00181403=CSCassette Size",
		"00181404=USExposures on Plate",
		"00181405=ISRelative X-ray Exposure",
		"00181450=CSColumn Angulation",
		"00181500=CSPositioner Motion",
		"00181508=CSPositioner Type",
		"00181510=DSPositioner Primary Angle",
		"00181511=DSPositioner Secondary Angle",
		"00181520=DSPositioner Primary Angle Increment",
		"00181521=DSPositioner Secondary Angle Increment",
		"00181530=DSDetector Primary Angle",
		"00181531=DSDetector Secondary Angle",
		"00181600=CSShutter Shape",
		"00181602=ISShutter Left Vertical Edge",
		"00181604=ISShutter Right Vertical Edge",
		"00181606=ISShutter Upper Horizontal Edge",
		"00181608=ISShutter Lower Horizontal Edge",
		"00181610=ISCenter of Circular Shutter",
		"00181612=ISRadius of Circular Shutter",
		"00181620=ISVertices of the Polygonal Shutter",
		"00181700=ISCollimator Shape",
		"00181702=ISCollimator Left Vertical Edge",
		"00181704=ISCollimator Right Vertical Edge",
		"00181706=ISCollimator Upper Horizontal Edge",
		"00181708=ISCollimator Lower Horizontal Edge",
		"00181710=ISCenter of Circular Collimator",
		"00181712=ISRadius of Circular Collimator",
		"00181720=ISVertices of the Polygonal Collimator",
		"00185000=SHOutput Power",
		"00185010=LOTransducer Data",
		"00185012=DSFocus Depth",
		"00185020=LOPreprocessing Function",
		"00185021=LOPostprocessing Function",
		"00185022=DSMechanical Index",
		"00185024=DSThermal Index",
		"00185026=DSCranial Thermal Index",
		"00185027=DSSoft Tissue Thermal Index",
		"00185028=DSSoft Tissue-focus Thermal Index",
		"00185029=DSSoft Tissue-surface Thermal Index",
		"00185050=ISDepth of Scan Field",
		"00185100=CSPatient Position",
		"00185101=CSView Position",
		"00185104=SQProjection Eponymous Name Code Sequence",
		"00185210=DSImage Transformation Matrix",
		"00185212=DSImage Translation Vector",
		"00186000=DSSensitivity",
		"00186011=SQSequence of Ultrasound Regions",
		"00186012=USRegion Spatial Format",
		"00186014=USRegion Data Type",
		"00186016=ULRegion Flags",
		"00186018=ULRegion Location Min X0",
		"0018601A=ULRegion Location Min Y0",
		"0018601C=ULRegion Location Max X1",
		"0018601E=ULRegion Location Max Y1",
		"00186020=SLReference Pixel X0",
		"00186022=SLReference Pixel Y0",
		"00186024=USPhysical Units X Direction",
		"00186026=USPhysical Units Y Direction",
		"00181628=FDReference Pixel Physical Value X",
		"0018602A=FDReference Pixel Physical Value Y",
		"0018602C=FDPhysical Delta X",
		"0018602E=FDPhysical Delta Y",
		"00186030=ULTransducer Frequency",
		"00186031=CSTransducer Type",
		"00186032=ULPulse Repetition Frequency",
		"00186034=FDDoppler Correction Angle",
		"00186036=FDSterring Angle",
		"00186038=ULDoppler Sample Volume X Position",
		"0018603A=ULDoppler Sample Volume Y Position",
		"0018603C=ULTM-Line Position X0",
		"0018603E=ULTM-Line Position Y0",
		"00186040=ULTM-Line Position X1",
		"00186042=ULTM-Line Position Y1",
		"00186044=USPixel Component Organization",
		"00186046=ULPixel Component Mask",
		"00186048=ULPixel Component Range Start",
		"0018604A=ULPixel Component Range Stop",
		"0018604C=USPixel Component Physical Units",
		"0018604E=USPixel Component Data Type",
		"00186050=ULNumber of Table Break Points",
		"00186052=ULTable of X Break Points",
		"00186054=FDTable of Y Break Points",
		"00186056=ULNumber of Table Entries",
		"00186058=ULTable of Pixel Values",
		"0018605A=ULTable of Parameter Values",
		"00187000=CSDetector Conditions Nominal Flag",
		"00187001=DSDetector Temperature",
		"00187004=CSDetector Type",
		"00187005=CSDetector Configuration",
		"00187006=LTDetector Description",
		"00187008=LTDetector Mode",
		"0018700A=SHDetector ID",
		"0018700C=DADate of Last Detector Calibration",
		"0018700E=TMTime of Last Detector Calibration",
		"00187010=ISExposures on Detector Since Last Calibration",
		"00187011=ISExposures on Detector Since Manufactured",
		"00187012=DSDetector Time Since Last Exposure",
		"00187014=DSDetector Active Time",
		"00187016=DSDetector Activation Offset From Exposure",
		"0018701A=DSDetector Binning",
		"00187020=DSDetector Element Physical Size",
		"00187022=DSDetector Element Spacing",
		"00187024=CSDetector Active Shape",
		"00187026=DSDetector Active Dimension(s)",
		"00187028=DSDetector Active Origin",
		"00187030=DSField of View Origin",
		"00187032=DSField of View Rotation",
		"00187034=CSField of View Horizontal Flip",
		"00187040=LTGrid Absorbing Material",
		"00187041=LTGrid Spacing Material",
		"00187042=DSGrid Thickness",
		"00187044=DSGrid Pitch",
		"00187046=ISGrid Aspect Ratio",
		"00187048=DSGrid Period",
		"0018704C=DSGrid Focal Distance",
		"00187050=LTFilter Material LT",
		"00187052=DSFilter Thickness Minimum",
		"00187054=DSFilter Thickness Maximum",
		"00187060=CSExposure Control Mode",
		"00187062=LTExposure Control Mode Description",
		"00187064=CSExposure Status",
		"00187065=DSPhototimer Setting",
		
		"0020000D=UIStudy Instance UID",
		"0020000E=UISeries Instance UID",
		"00200010=SHStudy ID",
		"00200011=ISSeries Number",
		"00200012=ISAcquisition Number",
		"00200013=ISImage Number",
		"00200014=ISIsotope Number",
		"00200015=ISPhase Number",
		"00200016=ISInterval Number",
		"00200017=ISTime Slot Number",
		"00200018=ISAngle Number",
		"00200020=CSPatient Orientation",
		"00200022=USOverlay Number",
		"00200024=USCurve Number",
		"00200030=DSImage Position",
		"00200032=DSImage Position (Patient)",
		"00200037=DSImage Orientation (Patient)",
		"00200050=DSLocation",
		"00200052=UIFrame of Reference UID",
		"00200060=CSLaterality",
		"00200070=LOImage Geometry Type",
		"00200080=UIMasking Image UID",
		"00200100=ISTemporal Position Identifier",
		"00200105=ISNumber of Temporal Positions",
		"00200110=DSTemporal Resolution",
		"00201000=ISSeries in Study",
		"00201002=ISImages in Acquisition",
		"00201004=ISAcquisition in Study",
		"00201040=LOPosition Reference Indicator",
		"00201041=DSSlice Location",
		"00201070=ISOther Study Numbers",
		"00201200=ISNumber of Patient Related Studies",
		"00201202=ISNumber of Patient Related Series",
		"00201204=ISNumber of Patient Related Images",
		"00201206=ISNumber of Study Related Series",
		"00201208=ISNumber of Study Related Images",
		"00204000=LTImage Comments",
		
		"00280002=USSamples per Pixel",
		"00280004=CSPhotometric Interpretation",
		"00280006=USPlanar Configuration",
		"00280008=ISNumber of Frames",
		"00280009=ATFrame Increment Pointer",
		"00280010=USRows",
		"00280011=USColumns",
		"00280030=DSPixel Spacing",
		"00280031=DSZoom Factor",
		"00280032=DSZoom Center",
		"00280034=ISPixel Aspect Ratio",
		"00280051=CSCorrected Image",
		"00280100=USBits Allocated",
		"00280101=USBits Stored",
		"00280102=USHigh Bit",
		"00280103=USPixel Representation",
		"00280106=USSmallest Image Pixel Value",
		"00280107=USLargest Image Pixel Value",
		"00280108=USSmallest Pixel Value in Series",
		"00280109=USLargest Pixel Value in Series",
		"00280120=USPixel Padding Value",
		"00280300=CSQuality Control Image",
		"00280301=CSBurned In Annotation",
		"00281040=CSPixel Intensity Relationship",
		"00281041=SSPixel Intensity Relationship Sign",
		"00281050=DSWindow Center",
		"00281051=DSWindow Width",
		"00281052=DSRescale Intercept",
		"00281053=DSRescale Slope",
		"00281054=LORescale Type",
		"00281055=LOWindow Center & Width Explanation",
		"00281101=USRed Palette Color Lookup Table Descriptor",
		"00281102=USGreen Palette Color Lookup Table Descriptor",
		"00281103=USBlue Palette Color Lookup Table Descriptor",
		"00281201=USRed Palette Color Lookup Table Data",
		"00281202=USGreen Palette Color Lookup Table Data",
		"00281203=USBlue Palette Color Lookup Table Data",
		"00282110=CSLossy Image Compression",
		"00283000=SQModality LUT Sequence",
		"00283002=USLUT Descriptor",
		"00283003=LOLUT Explanation",
		"00283004=LOMadality LUT Type",
		"00283006=USLUT Data",
		"00283010=SQVOI LUT Sequence",
		
		"30020011=DSImage Plane Pixel Spacing",
		"30020022=DSRadiation Machine SAD",
		"30020026=DSRT IMAGE SID",
		
		"0032000A=CSStudy Status ID",
		"0032000C=CSStudy Priority ID",
		"00320012=LOStudy ID Issuer",
		"00320032=DAStudy Verified Date",
		"00320033=TMStudy Verified Time",
		"00320034=DAStudy Read Date",
		"00320035=TMStudy Read Time",
		"00321000=DAScheduled Study Start Date",
		"00321001=TMScheduled Study Start Time",
		"00321010=DAScheduled Study Stop Date",
		"00321011=TMScheduled Study Stop Time",
		"00321020=LOScheduled Study Location",
		"00321021=AEScheduled Study Location AE Title(s)",
		"00321030=LOReason for Study",
		"00321032=PNRequesting Physician",
		"00321033=LORequesting Service",
		"00321040=DAStudy Arrival Date",
		"00321041=TMStudy Arrival Time",
		"00321050=DAStudy Completion Date",
		"00321051=TMStudy Completion Time",
		"00321055=CSStudy Component Status ID",
		"00321060=LORequested Procedure Description",
		"00321064=SQRequested Procedure Code Sequence",
		"00321070=LORequested Contrast Agent",
		"00324000=LTStudy Comments",
		
		"00400001=AEScheduled Station AE Title",
		"00400002=DAScheduled Procedure Step Start Date",
		"00400003=TMScheduled Procedure Step Start Time",
		"00400004=DAScheduled Procedure Step End Date",
		"00400005=TMScheduled Procedure Step End Time",
		"00400006=PNScheduled Performing Physician's Name",
		"00400007=LOScheduled Procedure Step Description",
		"00400008=SQScheduled Action Item Code Sequence",
		"00400009=SHScheduled Procedure Step ID",
		"00400010=SHScheduled Station Name",
		"00400011=SHScheduled Procedure Step Location",
		"00400012=LOPre-Medication",
		"00400020=CSScheduled Procedure Step Status",
		"00400100=SQScheduled Procedure Step Sequence",
		"00400220=SQReferenced Standalone SOP Instance Sequence",
		"00400241=AEPerformed Station AE Title",
		"00400242=SHPerformed Station Name",
		"00400243=SHPerformed Location",
		"00400244=DAPerformed Procedure Step Start Date",
		"00400245=TMPerformed Procedure Step Start Time",
		"00400250=DAPerformed Procedure Step End Date",
		"00400251=TMPerformed Procedure Step End Time",
		"00400252=CSPerformed Procedure Step Status",
		"00400253=SHPerformed Procedure Step ID",
		"00400254=LOPerformed Procedure Step Description",
		"00400255=LOPerformed Procedure Type Description",
		"00400260=SQPerformed Action Item Sequence",
		"00400270=SQScheduled Step Attributes Sequence",
		"00400275=SQRequest Attributes Sequence",
		"00400280=STComments on the Performed Procedure Steps",
		"00400293=SQQuantity Sequence",
		"00400294=DSQuantity",
		"00400295=SQMeasuring Units Sequence",
		"00400296=SQBilling Item Sequence",
		"00400300=USTotal Time of Fluoroscopy",
		"00400301=USTotal Number of Exposures",
		"00400302=USEntrance Dose",
		"00400303=USExposed Area",
		"00400306=DSDistance Source to Entrance",
		"00400307=DSDistance Source to Support",
		"00400310=STComments on Radiation Dose",
		"00400312=DSX-Ray Output",
		"00400314=DSHalf Value Layer",
		"00400316=DSOrgan Dose",
		"00400318=CSOrgan Exposed",
		"00400320=SQBilling Procedure Step Sequence",
		"00400321=SQFilm Consumption Sequence",
		"00400324=SQBilling Supplies and Devices Sequence",
		"00400330=SQReferenced Procedure Step Sequence",
		"00400340=SQPerformed Series Sequence",
		"00400400=LTComments on the Scheduled Procedure Step",
		"0040050A=LOSpecimen Accession Number",
		"00400550=SQSpecimen Sequence",
		"00400551=LOSpecimen Identifier",
		"0040059A=SQSpecimen Type Code Sequence",
		"00400555=SQAcquisition Context Sequence",
		"00400556=STAcquisition Context Description",
		"004006FA=LOSlide Identifier",
		"0040071A=SQImage Center Point Coordinates Sequence",
		"0040072A=DSX offset in Slide Coordinate System",
		"0040073A=DSY offset in Slide Coordinate System",
		"0040074A=DSZ offset in Slide Coordinate System",
		"004008D8=SQPixel Spacing Sequence",
		"004008DA=SQCoordinate System Axis Code Sequence",
		"004008EA=SQMeasurement Units Code Sequence",
		"00401001=SHRequested Procedure ID",
		"00401002=LOReason for the Requested Procedure",
		"00401003=SHRequested Procedure Priority",
		"00401004=LOPatient Transport Arrangements",
		"00401005=LORequested Procedure Location",
		"00401006= 1Placer Order Number / Procedure S",
		"00401007= 1Filler Order Number / Procedure S",
		"00401008=LOConfidentiality Code",
		"00401009=SHReporting Priority",
		"00401010=PNNames of Intended Recipients of Results",
		"00401400=LTRequested Procedure Comments",
		"00402001=LOReason for the Imaging Service Request",
		"00402004=DAIssue Date of Imaging Service Request",
		"00402005=TMIssue Time of Imaging Service Request",
		"00402006= 1Placer Order Number / Imaging Service Request S",
		"00402007= 1Filler Order Number / Imaging Service Request S",
		"00402008=PNOrder Entered By",
		"00402009=SHOrder Enterers Location",
		"00402010=SHOrder Callback Phone Number",
		"00402016=LOPlacer Order Number / Imaging Service Request",
		"00402017=LOFiller Order Number / Imaging Service Request",
		"00402400=LTImaging Service Request Comments",
		"00403001=LOConfidentiality Constraint on Patient Data Description",
		"00408302=DSEntrance Dose in mGy",
		"0040A010=CSRelationship Type",
		"0040A027=LOVerifying Organization",
		"0040A030=DTVerification DateTime",
		"0040A032=DTObservation DateTime",
		"0040A040=CSValue Type",
		"0040A043=SQConcept-name Code Sequence",
		"0040A050=CSContinuity Of Content",
		"0040A073=SQVerifying Observer Sequence",
		"0040A075=PNVerifying Observer Name",
		"0040A088=SQVerifying Observer Identification Code Sequence",
		"0040A0B0=USReferenced Waveform Channels",
		"0040A120=DTDateTime",
		"0040A121=DADate",
		"0040A122=TMTime",
		"0040A123=PNPerson Name",
		"0040A124=UIUID",
		"0040A130=CSTemporal Range Type",
		"0040A132=ULReferenced Sample Positions",
		"0040A136=USReferenced Frame Numbers",
		"0040A138=DSReferenced Time Offsets",
		"0040A13A=DTReferenced Datetime",
		"0040A160=UTText Value",
		"0040A168=SQConcept Code Sequence",
		"0040A180=USAnnotation Group Number",
		"0040A195=SQModifier Code Sequence",
		"0040A300=SQMeasured Value Sequence",
		"0040A30A=DSNumeric Value",
		"0040A360=SQPredecessor Documents Sequence",
		"0040A370=SQReferenced Request Sequence",
		"0040A372=SQPerformed Procedure Code Sequence",
		"0040A375=SQCurrent Requested Procedure Evidence Sequence",
		"0040A385=SQPertinent Other Evidence Sequence",
		"0040A491=CSCompletion Flag",
		"0040A492=LOCompletion Flag Description",
		"0040A493=CSVerification Flag",
		"0040A504=SQContent Template Sequence",
		"0040A525=SQIdentical Documents Sequence",
		"0040A730=SQContent Sequence",
		"0040B020=SQAnnotation Sequence",
		"0040DB00=CSTemplate Identifier",
		"0040DB06=DTTemplate Version",
		"0040DB07=DTTemplate Local Version",
		"0040DB0B=CSTemplate Extension Flag",
		"0040DB0C=UITemplate Extension Organization UID",
		"0040DB0D=UITemplate Extension Creator UID",
		"0040DB73=ULReferenced Content Item Identifier",
		
		"00540011=USNumber of Energy Windows",
		"00540012=SQEnergy Window Information Sequence",
		"00540013=SQEnergy Window Range Sequence",
		"00540014=DSEnergy Window Lower Limit",
		"00540015=DSEnergy Window Upper Limit",
		"00540016=SQRadiopharmaceutical Information Sequence",
		"00540017=ISResidual Syringe Counts",
		"00540018=SHEnergy Window Name",
		"00540020=USDetector Vector",
		"00540021=USNumber of Detectors",
		"00540022=SQDetector Information Sequence",
		"00540030=USPhase Vector",
		"00540031=USNumber of Phases",
		"00540032=SQPhase Information Sequence",
		"00540033=USNumber of Frames in Phase",
		"00540036=ISPhase Delay",
		"00540038=ISPause Between Frames",
		"00540039=CSPhase Description",
		"00540050=USRotation Vector",
		"00540051=USNumber of Rotations",
		"00540052=SQRotation Information Sequence",
		"00540053=USNumber of Frames in Rotation",
		"00540060=USR-R Interval Vector",
		"00540061=USNumber of R-R Intervals",
		"00540062=SQGated Information Sequence",
		"00540063=SQData Information Sequence",
		"00540070=USTime Slot Vector",
		"00540071=USNumber of Time Slots",
		"00540072=SQTime Slot Information Sequence",
		"00540073=DSTime Slot Time",
		"00540080=USSlice Vector",
		"00540081=USNumber of Slices",
		"00540090=USAngular View Vector",
		"00540100=USTime Slice Vector",
		"00540101=USNumber of Time Slices",
		"00540200=DSStart Angle",
		"00540202=CSType of Detector Motion",
		"00540210=ISTrigger Vector",
		"00540211=USNumber of Triggers in Phase",
		"00540220=SQView Code Sequence",
		"00540222=SQView Modifier Code Sequence",
		"00540300=SQRadionuclide Code Sequence",
		"00540302=SQAdministration Route Code Sequence",
		"00540304=SQRadiopharmaceutical Code Sequence",
		"00540306=SQCalibration Data Sequence",
		"00540308=USEnergy Window Number",
		"00540400=SHImage ID",
		"00540410=SQPatient Orientation Code Sequence",
		"00540412=SQPatient Orientation Modifier Code Sequence",
		"00540414=SQPatient Gantry Relationship Code Sequence",
		"00540500=CSSlice Progression Direction",
		"00541000=CSSeries Type",
		"00541001=CSUnits",
		"00541002=CSCounts Source",
		"00541004=CSReprojection Method",
		"00541100=CSRandoms Correction Method",
		"00541101=LOAttenuation Correction Method",
		"00541102=CSDecay Correction",
		"00541103=LOReconstruction Method",
		"00541104=LODetector Lines of Response Used",
		"00541105=LOScatter Correction Method",
		"00541200=DSAxial Acceptance",
		"00541201=ISAxial Mash",
		"00541202=ISTransverse Mash",
		"00541203=DSDetector Element Size",
		"00541210=DSCoincidence Window Width",
		"00541220=CSSecondary Counts Type",
		"00541300=DSFrame Reference Time",
		"00541310=ISPrimary (Prompts) Counts Accumulated",
		"00541311=ISSecondary Counts Accumulated",
		"00541320=DSSlice Sensitivity Factor",
		"00541321=DSDecay Factor",
		"00541322=DSDose Calibration Factor",
		"00541323=DSScatter Fraction Factor",
		"00541324=DSDead Time Factor",
		"00541330=USImage Index",
		"00541400=CSCounts Included",
		"00541401=CSDead Time Correction Flag",
		
		"30020002=SHRT Image Label",
		"30020003=LORT Image Name",
		"30020004=STRT Image Description",
		"3002000A=CSReported Values Origin",
		"3002000C=CSRT Image Plane",
		"3002000D=DSX-Ray Image Receptor Translation",
		"3002000E=DSX-Ray Image Receptor Angle",
		"30020011=DSImage Plane Pixel Spacing",
		"30020012=DSRT Image Position",
		"30020020=SHRadiation Machine Name",
		"30020022=DSRadiation Machine SAD",
		"30020026=DSRT Image SID",
		"30020029=ISFraction Number",
		"30020030=SQExposure Sequence",
		"30020032=DSMeterset Exposure",
		
		"300A011E=DSGantry Angle",
		"300A0120=DSBeam Limiting Device Angle",
		"300A0122=DSPatient Support Angle",
		"300A0128=DSTable Top Vertical Position",
		"300A0129=DSTable Top Longitudinal Position",
		"300A012A=DSTable Top Lateral Position",
		"300A00B3=CSPrimary Dosimeter Unit",
		"300A00F0=ISNumber of Blocks",
		
		"300C0006=ISReferenced Beam Number",
		"300C0008=DSStart Cumulative Meterset Weight",
		"300C0022=ISReferenced Fraction Group Number",

		"7FE00010=OXPixel Data",
		
		"FFFEE000=DLItem",
		"FFFEE00D=DLItem Delimitation Item",
		"FFFEE0DD=DLSequence Delimitation Item"
	};

}

