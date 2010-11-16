package ij.io;
import ij.util.Tools;
import java.io.*;
import java.util.*;
import java.net.*;

/**
Decodes single and multi-image TIFF files. The LZW decompression
code was contributed by Curtis Rueden.
*/
public class TiffDecoder {

	// tags
	public static final int NEW_SUBFILE_TYPE = 254;
	public static final int IMAGE_WIDTH = 256;
	public static final int IMAGE_LENGTH = 257;
	public static final int BITS_PER_SAMPLE = 258;
	public static final int COMPRESSION = 259;
	public static final int PHOTO_INTERP = 262;
	public static final int IMAGE_DESCRIPTION = 270;
	public static final int STRIP_OFFSETS = 273;
	public static final int ORIENTATION = 274;
	public static final int SAMPLES_PER_PIXEL = 277;
	public static final int ROWS_PER_STRIP = 278;
	public static final int STRIP_BYTE_COUNT = 279;
	public static final int X_RESOLUTION = 282;
	public static final int Y_RESOLUTION = 283;
	public static final int PLANAR_CONFIGURATION = 284;
	public static final int RESOLUTION_UNIT = 296;
	public static final int SOFTWARE = 305;
	public static final int DATE_TIME = 306;
	public static final int ARTEST = 315;
	public static final int HOST_COMPUTER = 316;
	public static final int PREDICTOR = 317;
	public static final int COLOR_MAP = 320;
	public static final int TILE_WIDTH = 322;
	public static final int SAMPLE_FORMAT = 339;
	public static final int JPEG_TABLES = 347;
	public static final int METAMORPH1 = 33628;
	public static final int METAMORPH2 = 33629;
	public static final int IPLAB = 34122;
	public static final int NIH_IMAGE_HDR = 43314;
	public static final int META_DATA_BYTE_COUNTS = 50838; // private tag registered with Adobe
	public static final int META_DATA = 50839; // private tag registered with Adobe
	
	//constants
	static final int UNSIGNED = 1;
	static final int SIGNED = 2;
	static final int FLOATING_POINT = 3;

	//field types
	static final int SHORT = 3;
	static final int LONG = 4;

	// metadata types
	static final int MAGIC_NUMBER = 0x494a494a;  // "IJIJ"
	static final int INFO = 0x696e666f;  // "info" (Info image property)
	static final int LABELS = 0x6c61626c;  // "labl" (slice labels)
	static final int RANGES = 0x72616e67;  // "rang" (display ranges)
	static final int LUTS = 0x6c757473;  // "luts" (channel LUTs)
	static final int ROI = 0x726f6920;  // "roi " (ROI)
	static final int OVERLAY = 0x6f766572;  // "over" (overlay)
	
	private String directory;
	private String name;
	private String url;
	protected RandomAccessStream in;
	protected boolean debugMode;
	private boolean littleEndian;
	private String dInfo;
	private int ifdCount;
	private int[] metaDataCounts;
	private String tiffMetadata;
		
	public TiffDecoder(String directory, String name) {
		this.directory = directory;
		this.name = name;
	}

	public TiffDecoder(InputStream in, String name) {
		directory = "";
		this.name = name;
		url = "";
		this.in = new RandomAccessStream(in);
	}

	final int getInt() throws IOException {
		int b1 = in.read();
		int b2 = in.read();
		int b3 = in.read();
		int b4 = in.read();
		if (littleEndian)
			return ((b4 << 24) + (b3 << 16) + (b2 << 8) + (b1 << 0));
		else
			return ((b1 << 24) + (b2 << 16) + (b3 << 8) + b4);
	}

	final int getShort() throws IOException {
		int b1 = in.read();
		int b2 = in.read();
		if (littleEndian)
			return ((b2<<8) + b1);
		else
			return ((b1<<8) + b2);
	}

    final long readLong() throws IOException {
    	if (littleEndian)
        	return ((long)getInt()&0xffffffffL) + ((long)getInt()<<32);
        else
			return ((long)getInt()<<32) + ((long)getInt()&0xffffffffL);
        	//return in.read()+(in.read()<<8)+(in.read()<<16)+(in.read()<<24)+(in.read()<<32)+(in.read()<<40)+(in.read()<<48)+(in.read()<<56);
    }

    final double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

	long OpenImageFileHeader() throws IOException {
	// Open 8-byte Image File Header at start of file.
	// Returns the offset in bytes to the first IFD or -1
	// if this is not a valid tiff file.
		int byteOrder = in.readShort();
		if (byteOrder==0x4949) // "II"
			littleEndian = true;
		else if (byteOrder==0x4d4d) // "MM"
			littleEndian = false;
		else {
			in.close();
			return -1;
		}
		int magicNumber = getShort(); // 42
		long offset = ((long)getInt())&0xffffffffL;
		return offset;
	}
		
	int getValue(int fieldType, int count) throws IOException {
		int value = 0;
		int unused;
		if (fieldType==SHORT && count==1) {
			value = getShort();
			unused = getShort();
		} else
			value = getInt();
		return value;
	}	
	
	void getColorMap(long offset, FileInfo fi) throws IOException {
		byte[] colorTable16 = new byte[768*2];
		long saveLoc = in.getLongFilePointer();
		in.seek(offset);
		in.readFully(colorTable16);
		in.seek(saveLoc);
		fi.lutSize = 256;
		fi.reds = new byte[256];
		fi.greens = new byte[256];
		fi.blues = new byte[256];
		int j = 0;
		if (littleEndian) j++;
		int sum = 0;
		for (int i=0; i<256; i++) {
			fi.reds[i] = colorTable16[j];
			sum += fi.reds[i];
			fi.greens[i] = colorTable16[512+j];
			sum += fi.greens[i];
			fi.blues[i] = colorTable16[1024+j];
			sum += fi.blues[i];
			j += 2;
		}
		if (sum!=0)
			fi.fileType = FileInfo.COLOR8;
	}
	
	byte[] getString(int count, long offset) throws IOException {
		count--; // skip null byte at end of string
		if (count<=0)
			return null;
		byte[] bytes = new byte[count];
		long saveLoc = in.getLongFilePointer();
		in.seek(offset);
		in.readFully(bytes);
		in.seek(saveLoc);
		return bytes;
	}

	/** Save the image description in the specified FileInfo. ImageJ
		saves spatial and density calibration data in this string. For
		stacks, it also saves the number of images to avoid having to
		decode an IFD for each image. */
	public void saveImageDescription(byte[] description, FileInfo fi) {
        String id = new String(description);
        if (!id.startsWith("ImageJ"))
			saveMetadata(getName(IMAGE_DESCRIPTION), id);
		if (id.length()<7) return;
		fi.description = id;
        int index1 = id.indexOf("images=");
        if (index1>0) {
            int index2 = id.indexOf("\n", index1);
            if (index2>0) {
                String images = id.substring(index1+7,index2);
                int n = (int)Tools.parseDouble(images, 0.0);
                if (n>1) fi.nImages = n;
            }
        }
	}

	public void saveMetadata(String name, String data) {
		if (data==null) return;
        String str = name+": "+data+"\n";
        if (tiffMetadata==null)
        	tiffMetadata = str;
        else
        	tiffMetadata += str;
	}

	void decodeNIHImageHeader(int offset, FileInfo fi) throws IOException {
		long saveLoc = in.getLongFilePointer();
		
		in.seek(offset+12);
		int version = in.readShort();
		
		in.seek(offset+160);
		double scale = in.readDouble();
		if (version>106 && scale!=0.0) {
			fi.pixelWidth = 1.0/scale;
			fi.pixelHeight = fi.pixelWidth;
		} 

		// spatial calibration
		in.seek(offset+172);
		int units = in.readShort();
		if (version<=153) units += 5;
		switch (units) {
			case 5: fi.unit = "nanometer"; break;
			case 6: fi.unit = "micrometer"; break;
			case 7: fi.unit = "mm"; break;
			case 8: fi.unit = "cm"; break;
			case 9: fi.unit = "meter"; break;
			case 10: fi.unit = "km"; break;
			case 11: fi.unit = "inch"; break;
			case 12: fi.unit = "ft"; break;
			case 13: fi.unit = "mi"; break;
		}

		// density calibration
		in.seek(offset+182);
		int fitType = in.read();
		int unused = in.read();
		int nCoefficients = in.readShort();
		if (fitType==11) {
			fi.calibrationFunction = 21; //Calibration.UNCALIBRATED_OD
			fi.valueUnit = "U. OD";
		} else if (fitType>=0 && fitType<=8 && nCoefficients>=1 && nCoefficients<=5) {
			switch (fitType) {
				case 0: fi.calibrationFunction = 0; break; //Calibration.STRAIGHT_LINE
				case 1: fi.calibrationFunction = 1; break; //Calibration.POLY2
				case 2: fi.calibrationFunction = 2; break; //Calibration.POLY3
				case 3: fi.calibrationFunction = 3; break; //Calibration.POLY4
				case 5: fi.calibrationFunction = 4; break; //Calibration.EXPONENTIAL
				case 6: fi.calibrationFunction = 5; break; //Calibration.POWER
				case 7: fi.calibrationFunction = 6; break; //Calibration.LOG
				case 8: fi.calibrationFunction = 10; break; //Calibration.RODBARD2 (NIH Image)
			}
			fi.coefficients = new double[nCoefficients];
			for (int i=0; i<nCoefficients; i++) {
				fi.coefficients[i] = in.readDouble();
			}
			in.seek(offset+234);
			int size = in.read();
			StringBuffer sb = new StringBuffer();
			if (size>=1 && size<=16) {
				for (int i=0; i<size; i++)
					sb.append((char)(in.read()));
				fi.valueUnit = new String(sb);
			} else
				fi.valueUnit = " ";
		}
			
		in.seek(offset+260);
		int nImages = in.readShort();
		if(nImages>=2 && (fi.fileType==FileInfo.GRAY8||fi.fileType==FileInfo.COLOR8)) {
			fi.nImages = nImages;
			fi.pixelDepth = in.readFloat();	//SliceSpacing
			int skip = in.readShort();		//CurrentSlice
			fi.frameInterval = in.readFloat();
			//ij.IJ.write("fi.pixelDepth: "+fi.pixelDepth);
		}
			
		in.seek(offset+272);
		float aspectRatio = in.readFloat();
		if (version>140 && aspectRatio!=0.0)
			fi.pixelHeight = fi.pixelWidth/aspectRatio;
		
		in.seek(saveLoc);
	}
	
	void dumpTag(int tag, int count, int value, FileInfo fi) {
		String name = getName(tag);
		String cs = (count==1)?"":", count=" + count;
		dInfo += "    " + tag + ", \"" + name + "\", value=" + value + cs + "\n";
		//ij.IJ.log(tag + ", \"" + name + "\", value=" + value + cs + "\n");
	}

	String getName(int tag) {
		String name;
		switch (tag) {
			case NEW_SUBFILE_TYPE: name="NewSubfileType"; break;
			case IMAGE_WIDTH: name="ImageWidth"; break;
			case IMAGE_LENGTH: name="ImageLength"; break;
			case STRIP_OFFSETS: name="StripOffsets"; break;
			case ORIENTATION: name="Orientation"; break;
			case PHOTO_INTERP: name="PhotoInterp"; break;
			case IMAGE_DESCRIPTION: name="ImageDescription"; break;
			case BITS_PER_SAMPLE: name="BitsPerSample"; break;
			case SAMPLES_PER_PIXEL: name="SamplesPerPixel"; break;
			case ROWS_PER_STRIP: name="RowsPerStrip"; break;
			case STRIP_BYTE_COUNT: name="StripByteCount"; break;
			case X_RESOLUTION: name="XResolution"; break;
			case Y_RESOLUTION: name="YResolution"; break;
			case RESOLUTION_UNIT: name="ResolutionUnit"; break;
			case SOFTWARE: name="Software"; break;
			case DATE_TIME: name="DateTime"; break;
			case ARTEST: name="Artest"; break;
			case HOST_COMPUTER: name="HostComputer"; break;
			case PLANAR_CONFIGURATION: name="PlanarConfiguration"; break;
			case COMPRESSION: name="Compression"; break; 
			case PREDICTOR: name="Predictor"; break; 
			case COLOR_MAP: name="ColorMap"; break; 
			case SAMPLE_FORMAT: name="SampleFormat"; break; 
			case JPEG_TABLES: name="JPEGTables"; break; 
			case NIH_IMAGE_HDR: name="NIHImageHeader"; break; 
			case META_DATA_BYTE_COUNTS: name="MetaDataByteCounts"; break; 
			case META_DATA: name="MetaData"; break; 
			default: name="???"; break;
		}
		return name;
	}

	double getRational(long loc) throws IOException {
		long saveLoc = in.getLongFilePointer();
		in.seek(loc);
		int numerator = getInt();
		int denominator = getInt();
		in.seek(saveLoc);
		//System.out.println("numerator: "+numerator);
		//System.out.println("denominator: "+denominator);
		if (denominator!=0)
			return (double)numerator/denominator;
		else
			return 0.0;
	}
	
	FileInfo OpenIFD() throws IOException {
	// Get Image File Directory data
		int tag, fieldType, count, value;
		int nEntries = getShort();
		if (nEntries<1 || nEntries>1000)
			return null;
		ifdCount++;
		FileInfo fi = new FileInfo();
		for (int i=0; i<nEntries; i++) {
			tag = getShort();
			fieldType = getShort();
			count = getInt();
			value = getValue(fieldType, count);
			long lvalue = ((long)value)&0xffffffffL;
			if (debugMode && ifdCount<10) dumpTag(tag, count, value, fi);
			//ij.IJ.write(i+"/"+nEntries+" "+tag + ", count=" + count + ", value=" + value);
			//if (tag==0) return null;
			switch (tag) {
				case IMAGE_WIDTH: 
					fi.width = value;
					fi.intelByteOrder = littleEndian;
					break;
				case IMAGE_LENGTH: 
					fi.height = value;
					break;
 				case STRIP_OFFSETS:
					if (count==1)
						fi.stripOffsets = new int[] {value};
					else {
						long saveLoc = in.getLongFilePointer();
						in.seek(lvalue);
						fi.stripOffsets = new int[count];
						for (int c=0; c<count; c++)
							fi.stripOffsets[c] = getInt();
						in.seek(saveLoc);
					}
					fi.offset = count>0?fi.stripOffsets[0]:value;
					if (count>1 && fi.stripOffsets[count-1]<fi.stripOffsets[0])
						fi.offset = fi.stripOffsets[count-1];
					break;
				case STRIP_BYTE_COUNT:
					if (count==1)
						fi.stripLengths = new int[] {value};
					else {
						long saveLoc = in.getLongFilePointer();
						in.seek(lvalue);
						fi.stripLengths = new int[count];
						for (int c=0; c<count; c++) {
							if (fieldType==SHORT)
								fi.stripLengths[c] = getShort();
							else
								fi.stripLengths[c] = getInt();
						}
						in.seek(saveLoc);
					}
					break;
 				case PHOTO_INTERP:
 					fi.whiteIsZero = value==0;
					break;
				case BITS_PER_SAMPLE:
						if (count==1) {
							if (value==8)
								fi.fileType = FileInfo.GRAY8;
							else if (value==16)
								fi.fileType = FileInfo.GRAY16_UNSIGNED;
							else if (value==32)
								fi.fileType = FileInfo.GRAY32_INT;
							else if (value==12)
								fi.fileType = FileInfo.GRAY12_UNSIGNED;
							else if (value==1)
								fi.fileType = FileInfo.BITMAP;
							else
								error("Unsupported BitsPerSample: " + value);
						} else if (count==3) {
							long saveLoc = in.getLongFilePointer();
							in.seek(lvalue);
							int bitDepth = getShort();
							if (!(bitDepth==8||bitDepth==16))
								error("ImageJ can only open 8 and 16 bit/channel RGB images ("+bitDepth+")");
							if (bitDepth==16)
								fi.fileType = FileInfo.RGB48;
							in.seek(saveLoc);
						}
						break;
				case SAMPLES_PER_PIXEL:
					fi.samplesPerPixel = value;
					if (value==3 && fi.fileType!=FileInfo.RGB48)
						fi.fileType = fi.fileType==FileInfo.GRAY16_UNSIGNED?FileInfo.RGB48:FileInfo.RGB;
					else if (value==4 && fi.fileType==FileInfo.GRAY8)
						fi.fileType = FileInfo.ARGB;
					break;
				case ROWS_PER_STRIP:
					fi.rowsPerStrip = value;
					break;
				case X_RESOLUTION:
					double xScale = getRational(lvalue); 
					if (xScale!=0.0) fi.pixelWidth = 1.0/xScale; 
					break;
				case Y_RESOLUTION:
					double yScale = getRational(lvalue); 
					if (yScale!=0.0) fi.pixelHeight = 1.0/yScale; 
					break;
				case RESOLUTION_UNIT:
					if (value==1&&fi.unit==null)
						fi.unit = " ";
					else if (value==2) {
						if (fi.pixelWidth==1.0/72.0) {
							fi.pixelWidth = 1.0;
							fi.pixelHeight = 1.0;
						} else
							fi.unit = "inch";
					} else if (value==3)
						fi.unit = "cm";
					break;
				case PLANAR_CONFIGURATION:  // 1=chunky, 2=planar
					if (value==2 && fi.fileType==FileInfo.RGB48)
							 fi.fileType = FileInfo.GRAY16_UNSIGNED;
					else if (value==2 && fi.fileType==FileInfo.RGB)
						fi.fileType = FileInfo.RGB_PLANAR;
					else if (value==1 && fi.samplesPerPixel==4)
						fi.fileType = FileInfo.ARGB;
					else if (value!=2 && !((fi.samplesPerPixel==1)||(fi.samplesPerPixel==3))) {
						String msg = "Unsupported SamplesPerPixel: " + fi.samplesPerPixel;
						error(msg);
					}
					break;
				case COMPRESSION:
					if (value==5)  // LZW compression
						fi.compression = FileInfo.LZW;
					else if (value==32773)  // PackBits compression
						fi.compression = FileInfo.PACK_BITS;
					else if (value!=1 && value!=0 && !(value==7&&fi.width<500)) {
						// don't abort with Spot camera compressed (7) thumbnails
						// otherwise, this is an unknown compression type
						fi.compression = FileInfo.COMPRESSION_UNKNOWN;
						error("ImageJ cannot open TIFF files " +
							"compressed in this fashion ("+value+")");
					}
					break;
				case SOFTWARE: case DATE_TIME: case HOST_COMPUTER: case ARTEST:
					if (ifdCount==1) {
						byte[] bytes = getString(count, lvalue);
						String s = bytes!=null?new String(bytes):null;
						saveMetadata(getName(tag), s);
					}
					break;
				case PREDICTOR:
					if (value==2 && fi.compression==FileInfo.LZW)
						fi.compression = FileInfo.LZW_WITH_DIFFERENCING;
					break;
				case COLOR_MAP: 
					if (count==768 && fi.fileType==fi.GRAY8)
						getColorMap(lvalue, fi);
					break;
				case TILE_WIDTH:
					error("ImageJ cannot open tiled TIFFs");
					break;
				case SAMPLE_FORMAT:
					if (fi.fileType==FileInfo.GRAY32_INT && value==FLOATING_POINT)
						fi.fileType = FileInfo.GRAY32_FLOAT;
					if (fi.fileType==FileInfo.GRAY16_UNSIGNED) {
						if (value==SIGNED)
							fi.fileType = FileInfo.GRAY16_SIGNED;
						if (value==FLOATING_POINT)
							error("ImageJ cannot open 16-bit float TIFFs");
					}
					break;
				case JPEG_TABLES:
					if (fi.compression==FileInfo.JPEG)
						error("Cannot open JPEG-compressed TIFFs with separate tables");
					break;
				case IMAGE_DESCRIPTION: 
					if (ifdCount==1) {
						byte[] s = getString(count, lvalue);
						if (s!=null) saveImageDescription(s,fi);
					}
					break;
				case ORIENTATION:
					fi.nImages = 0; // file not created by ImageJ so look at all the IFDs
					break;
				case METAMORPH1: case METAMORPH2:
					if ((name.indexOf(".STK")>0||name.indexOf(".stk")>0) && fi.compression==FileInfo.COMPRESSION_NONE) {
						if (tag==METAMORPH2)
							fi.nImages=count;
						else
							fi.nImages=9999;
					}
					break;
				case IPLAB: 
					fi.nImages=value;
					break;
				case NIH_IMAGE_HDR: 
					if (count==256)
						decodeNIHImageHeader(value, fi);
					break;
 				case META_DATA_BYTE_COUNTS: 
					long saveLoc = in.getLongFilePointer();
					in.seek(lvalue);
					metaDataCounts = new int[count];
					for (int c=0; c<count; c++)
						metaDataCounts[c] = getInt();
					in.seek(saveLoc);
					break;
 				case META_DATA: 
 					getMetaData(value, fi);
 					break;
				default:
					if (tag>10000 && tag<32768 && ifdCount>1)
						return null;
			}
		}
		fi.fileFormat = fi.TIFF;
		fi.fileName = name;
		fi.directory = directory;
		if (url!=null)
			fi.url = url;
		return fi;
	}

	void getMetaData(int loc, FileInfo fi) throws IOException {
		if (metaDataCounts==null || metaDataCounts.length==0)
			return;
		int maxTypes = 10;
		long saveLoc = in.getLongFilePointer();
		in.seek(loc);
		int n = metaDataCounts.length;
		int hdrSize = metaDataCounts[0];
		if (hdrSize<12 || hdrSize>804)
			{in.seek(saveLoc); return;}
		int magicNumber = getInt();
		if (magicNumber!=MAGIC_NUMBER)  // "IJIJ"
			{in.seek(saveLoc); return;}
		int nTypes = (hdrSize-4)/8;
		int[] types = new int[nTypes];
		int[] counts = new int[nTypes];
		
		if (debugMode) dInfo += "Metadata:\n";
		int extraMetaDataEntries = 0;
		for (int i=0; i<nTypes; i++) {
			types[i] = getInt();
			counts[i] = getInt();
			if (types[i]<0xffffff)
				extraMetaDataEntries += counts[i];
			if (debugMode) {
				String id = "";
				if (types[i]==INFO) id = " (Info property)";
				if (types[i]==LABELS) id = " (slice labels)";
				if (types[i]==RANGES) id = " (display ranges)";
				if (types[i]==LUTS) id = " (luts)";
				if (types[i]==ROI) id = " (roi)";
				if (types[i]==OVERLAY) id = " (overlay)";
				dInfo += "   "+i+" "+Integer.toHexString(types[i])+" "+counts[i]+id+"\n";
			}
		}
		fi.metaDataTypes = new int[extraMetaDataEntries];
		fi.metaData = new byte[extraMetaDataEntries][];
		int start = 1;
		int eMDindex = 0;
		for (int i=0; i<nTypes; i++) {
			if (types[i]==INFO)
				getInfoProperty(start, fi);
			else if (types[i]==LABELS)
				getSliceLabels(start, start+counts[i]-1, fi);
			else if (types[i]==RANGES)
				getDisplayRanges(start, fi);
			else if (types[i]==LUTS)
				getLuts(start, start+counts[i]-1, fi);
			else if (types[i]==ROI)
				getRoi(start, fi);
			else if (types[i]==OVERLAY)
				getOverlay(start, start+counts[i]-1, fi);
			else if (types[i]<0xffffff) {
				for (int j=start; j<start+counts[i]; j++) { 
					int len = metaDataCounts[j]; 
					fi.metaData[eMDindex] = new byte[len]; 
					in.readFully(fi.metaData[eMDindex], len); 
					fi.metaDataTypes[eMDindex] = types[i]; 
					eMDindex++; 
				} 
			} else
				skipUnknownType(start, start+counts[i]-1);
			start += counts[i];
		}
		in.seek(saveLoc);
	}

	void getInfoProperty(int first, FileInfo fi) throws IOException {
		int len = metaDataCounts[first];
	    byte[] buffer = new byte[len];
		in.readFully(buffer, len);
		len /= 2;
		char[] chars = new char[len];
		if (littleEndian) {
			for (int j=0, k=0; j<len; j++)
				chars[j] = (char)(buffer[k++]&255 + ((buffer[k++]&255)<<8));
		} else {
			for (int j=0, k=0; j<len; j++)
				chars[j] = (char)(((buffer[k++]&255)<<8) + buffer[k++]&255);
		}
		fi.info = new String(chars);
	}

	void getSliceLabels(int first, int last, FileInfo fi) throws IOException {
		fi.sliceLabels = new String[last-first+1];
	    int index = 0;
	    byte[] buffer = new byte[metaDataCounts[first]];
		for (int i=first; i<=last; i++) {
			int len = metaDataCounts[i];
			if (len>0) {
				if (len>buffer.length)
					buffer = new byte[len];
				in.readFully(buffer, len);
				len /= 2;
				char[] chars = new char[len];
				if (littleEndian) {
					for (int j=0, k=0; j<len; j++)
						chars[j] = (char)(buffer[k++]&255 + ((buffer[k++]&255)<<8));
				} else {
					for (int j=0, k=0; j<len; j++)
						chars[j] = (char)(((buffer[k++]&255)<<8) + buffer[k++]&255);
				}
				fi.sliceLabels[index++] = new String(chars);
				//ij.IJ.log(i+"  "+fi.sliceLabels[i-1]+"  "+len);
			} else
				fi.sliceLabels[index++] = null;
		}
	}

	void getDisplayRanges(int first, FileInfo fi) throws IOException {
		int n = metaDataCounts[first]/8;
		fi.displayRanges = new double[n];
		for (int i=0; i<n; i++)
			fi.displayRanges[i] = readDouble();
	}

	void getLuts(int first, int last, FileInfo fi) throws IOException {
		fi.channelLuts = new byte[last-first+1][];
	    int index = 0;
		for (int i=first; i<=last; i++) {
			int len = metaDataCounts[i];
			fi.channelLuts[index] = new byte[len];
            in.readFully(fi.channelLuts[index], len);
            index++;
		}
	}

	void getRoi(int first, FileInfo fi) throws IOException {
		int len = metaDataCounts[first];
		fi.roi = new byte[len]; 
		in.readFully(fi.roi, len); 
	}

	void getOverlay(int first, int last, FileInfo fi) throws IOException {
		fi.overlay = new byte[last-first+1][];
	    int index = 0;
		for (int i=first; i<=last; i++) {
			int len = metaDataCounts[i];
			fi.overlay[index] = new byte[len];
            in.readFully(fi.overlay[index], len);
            index++;
		}
	}

	void error(String message) throws IOException {
		if (in!=null) in.close();
		throw new IOException(message);
	}
	
	void skipUnknownType(int first, int last) throws IOException {
	    byte[] buffer = new byte[metaDataCounts[first]];
		for (int i=first; i<=last; i++) {
			int len = metaDataCounts[i];
            if (len>buffer.length)
                buffer = new byte[len];
            in.readFully(buffer, len);
		}
	}

	public void enableDebugging() {
		debugMode = true;
	}
	
	
	public FileInfo[] getTiffInfo() throws IOException {
		long ifdOffset;
		Vector info;
				
		if (in==null)
			in = new RandomAccessStream(new RandomAccessFile(new File(directory, name), "r"));
		info = new Vector();
		ifdOffset = OpenImageFileHeader();
		if (ifdOffset<0L) {
			in.close();
			return null;
		}
		if (debugMode) dInfo = "\n  " + name + ": opening\n";
		while (ifdOffset>0L) {
			in.seek(ifdOffset);
			FileInfo fi = OpenIFD();
			if (fi!=null) {
				info.addElement(fi);
				ifdOffset = ((long)getInt())&0xffffffffL;
			} else
				ifdOffset = 0L;
			if (debugMode && ifdCount<10) dInfo += "  nextIFD=" + ifdOffset + "\n";
			if (fi!=null) {
				if (fi.nImages>1) // ignore extra IFDs in ImageJ and NIH Image stacks
					ifdOffset = 0L;
			}
		}
		if (info.size()==0) {
			in.close();
			return null;
		} else {
			FileInfo[] fi = new FileInfo[info.size()];
			info.copyInto((Object[])fi);
			if (debugMode) fi[0].debugInfo = dInfo;
			if (url!=null) {
				in.seek(0);
				fi[0].inputStream = in;
			} else
				in.close();
			if (fi[0].info==null)
				fi[0].info = tiffMetadata;
			if (debugMode) {
				int n = fi.length;
				fi[0].debugInfo += "number of images: "+ n + "\n";
				fi[0].debugInfo += "offset to first image: "+fi[0].getOffset()+ "\n";
				fi[0].debugInfo += "gap between images: "+getGapInfo(fi) + "\n";
				fi[0].debugInfo += "little-endian byte order: "+fi[0].intelByteOrder + "\n";
			}
			return fi;
		}
	}
	
	String getGapInfo(FileInfo[] fi) {
		if (fi.length<2) return "0";
		long minGap = Long.MAX_VALUE;
		long maxGap = -Long.MAX_VALUE;
		for (int i=1; i<fi.length; i++) {
			long gap = fi[i].getOffset()-fi[i-1].getOffset();
			if (gap<minGap) minGap = gap;
			if (gap>maxGap) maxGap = gap;
		}
		long imageSize = fi[0].width*fi[0].height*fi[0].getBytesPerPixel();
		minGap -= imageSize;
		maxGap -= imageSize;
		if (minGap==maxGap)
			return ""+minGap;
		else 
			return "varies ("+minGap+" to "+maxGap+")";
	}

}
