package ijx.io;
import java.io.*;

/**Saves an image described by a FileInfo object as an uncompressed, big-endian TIFF file.*/
public class TiffEncoder {
	static final int HDR_SIZE = 8;
	static final int MAP_SIZE = 768; // in 16-bit words
	static final int BPS_DATA_SIZE = 6;
	static final int SCALE_DATA_SIZE = 16;
		
	private FileInfo fi;
	private int bitsPerSample;
	private int photoInterp;
	private int samplesPerPixel;
	private int nEntries;
	private int ifdSize;
	private long imageOffset;
	private int imageSize;
	private long stackSize;
	private byte[] description;
	private int metaDataSize;
	private int nMetaDataTypes;
	private int nMetaDataEntries;
	private int nSliceLabels;
	private int extraMetaDataEntries;
	private int scaleSize;
	private boolean littleEndian = ijx.Prefs.intelByteOrder;
	private byte buffer[] = new byte[8];
		
	public TiffEncoder (FileInfo fi) {
		this.fi = fi;
		fi.intelByteOrder = littleEndian;
		bitsPerSample = 8;
		samplesPerPixel = 1;
		nEntries = 9;
		int bytesPerPixel = 1;
		int bpsSize = 0;
		int colorMapSize = 0;

		switch (fi.fileType) {
			case FileInfo.GRAY8:
				photoInterp = fi.whiteIsZero?0:1;
				break;
			case FileInfo.GRAY16_UNSIGNED:
			case FileInfo.GRAY16_SIGNED:
				bitsPerSample = 16;
				photoInterp = fi.whiteIsZero?0:1;
				bytesPerPixel = 2;
				break;
			case FileInfo.GRAY32_FLOAT:
				bitsPerSample = 32;
				photoInterp = fi.whiteIsZero?0:1;
				bytesPerPixel = 4;
				break;
			case FileInfo.RGB:
				photoInterp = 2;
				samplesPerPixel = 3;
				bytesPerPixel = 3;
				bpsSize = BPS_DATA_SIZE;
				break;
			case FileInfo.RGB48:
				bitsPerSample = 16;
				photoInterp = 2;
				samplesPerPixel = 3;
				bytesPerPixel = 6;
				fi.nImages /= 3;
				bpsSize = BPS_DATA_SIZE;
				break;
			case FileInfo.COLOR8:
				photoInterp = 3;
				nEntries = 10;
				colorMapSize = MAP_SIZE*2;
				break;
			default:
				photoInterp = 0;
		}
		if (fi.unit!=null && fi.pixelWidth!=0 && fi.pixelHeight!=0)
			nEntries += 3; // XResolution, YResolution and ResolutionUnit
		if (fi.fileType==fi.GRAY32_FLOAT)
			nEntries++; // SampleFormat tag
		makeDescriptionString();
		if (description!=null)
			nEntries++;  // ImageDescription tag
		imageSize = fi.width*fi.height*bytesPerPixel;
		stackSize = (long)imageSize*fi.nImages;
		metaDataSize = getMetaDataSize();
		if (metaDataSize>0)
			nEntries += 2; // MetaData & MetaDataCounts
		ifdSize = 2 + nEntries*12 + 4;
		int descriptionSize = description!=null?description.length:0;
		scaleSize = fi.unit!=null && fi.pixelWidth!=0 && fi.pixelHeight!=0?SCALE_DATA_SIZE:0;
		imageOffset = HDR_SIZE+ifdSize+bpsSize+descriptionSize+scaleSize+colorMapSize + nMetaDataEntries*4 + metaDataSize;
		fi.offset = (int)imageOffset;
		//ij.IJ.log(imageOffset+", "+ifdSize+", "+bpsSize+", "+descriptionSize+", "+scaleSize+", "+colorMapSize+", "+nMetaDataEntries*4+", "+metaDataSize);
	}
	
	/** Saves the image as a TIFF file. The OutputStream is not closed.
		The fi.pixels field must contain the image data. If fi.nImages>1
		then fi.pixels must be a 2D array. The fi.offset field is ignored. */
	public void write(OutputStream out) throws IOException {
		writeHeader(out);
		long nextIFD = 0L;
		if (fi.nImages>1)
			nextIFD = imageOffset+stackSize;
        if (nextIFD+fi.nImages*ifdSize>=0xffffffffL)
            nextIFD = 0L;
		writeIFD(out, (int)imageOffset, (int)nextIFD);
		if (fi.fileType==FileInfo.RGB||fi.fileType==FileInfo.RGB48)
			writeBitsPerPixel(out);
		if (description!=null)
			writeDescription(out);
		if (scaleSize>0)
			writeScale(out);
		if (fi.fileType==FileInfo.COLOR8)
			writeColorMap(out);
		if (metaDataSize>0)
			writeMetaData(out);
		new ImageWriter(fi).write(out);
		if (nextIFD>0L) {
			int ifdSize2 = ifdSize;
			if (metaDataSize>0) {
				metaDataSize = 0;
				nEntries -= 2;
				ifdSize2 -= 2*12;
			}
			for (int i=2; i<=fi.nImages; i++) {
				if (i==fi.nImages)
					nextIFD = 0;
				else
					nextIFD += ifdSize2;
				imageOffset += imageSize;
				writeIFD(out, (int)imageOffset, (int)nextIFD);
			}
		}
	}
	
	public void write(DataOutputStream out) throws IOException {
		write((OutputStream)out);
	}

	int getMetaDataSize() {
        //if (stackSize+IMAGE_START>0xffffffffL) return 0;
		nSliceLabels = 0;
		nMetaDataEntries = 0;
		int size = 0;
		int nTypes = 0;
		if (fi.info!=null && fi.info.length()>0) {
			nMetaDataEntries = 1;
			size = fi.info.length()*2;
			nTypes++;
		}
		if (fi.sliceLabels!=null) {
			int max = Math.min(fi.sliceLabels.length, fi.nImages);
			boolean isNonNullLabel = false;
			for (int i=0; i<max; i++) {
				if (fi.sliceLabels[i]!=null && fi.sliceLabels[i].length()>0) {
					isNonNullLabel = true;
					break;
				}
			}
			if (isNonNullLabel) {
				for (int i=0; i<max; i++) {
					nSliceLabels++;
					if (fi.sliceLabels[i]!=null)
						size += fi.sliceLabels[i].length()*2;
				}
				if (nSliceLabels>0) nTypes++;
				nMetaDataEntries += nSliceLabels;
			}
		}

		if (fi.displayRanges!=null) {
			nMetaDataEntries++;
			size += fi.displayRanges.length*8;
			nTypes++;
		}

		if (fi.channelLuts!=null) {
			for (int i=0; i<fi.channelLuts.length; i++) {
                if (fi.channelLuts[i]!=null)
                    size += fi.channelLuts[i].length;
            }
			nTypes++;
			nMetaDataEntries += fi.channelLuts.length;
		}

		if (fi.roi!=null) {
			nMetaDataEntries++;
			size += fi.roi.length;
			nTypes++;
		}

		if (fi.overlay!=null) {
			for (int i=0; i<fi.overlay.length; i++) {
				if (fi.overlay[i]!=null)
					size += fi.overlay[i].length;
			}
			nTypes++;
			nMetaDataEntries += fi.overlay.length;
		}

		if (fi.metaDataTypes!=null && fi.metaData!=null && fi.metaData[0]!=null
		&& fi.metaDataTypes.length==fi.metaData.length) {
			extraMetaDataEntries = fi.metaData.length;
			nTypes += extraMetaDataEntries;
			nMetaDataEntries += extraMetaDataEntries;
			for (int i=0; i<extraMetaDataEntries; i++) {
                if (fi.metaData[i]!=null)
                    size += fi.metaData[i].length;
            }
		}
		if (nMetaDataEntries>0) nMetaDataEntries++; // add entry for header
		int hdrSize = 4 + nTypes*8;
		if (size>0) size += hdrSize;
		nMetaDataTypes = nTypes;
		return size;
	}
	
	/** Writes the 8-byte image file header. */
	void writeHeader(OutputStream out) throws IOException {
		byte[] hdr = new byte[8];
		if (littleEndian) {
			hdr[0] = 73; // "II" (Intel byte order)
			hdr[1] = 73;
			hdr[2] = 42;  // 42 (magic number)
			hdr[3] = 0;
			hdr[4] = 8;  // 8 (offset to first IFD)
			hdr[5] = 0;
			hdr[6] = 0;
			hdr[7] = 0;
		} else {
			hdr[0] = 77; // "MM" (Motorola byte order)
			hdr[1] = 77;
			hdr[2] = 0;  // 42 (magic number)
			hdr[3] = 42;
			hdr[4] = 0;  // 8 (offset to first IFD)
			hdr[5] = 0;
			hdr[6] = 0;
			hdr[7] = 8;
		}
		out.write(hdr);
	}
	
	/** Writes one 12-byte IFD entry. */
	void writeEntry(OutputStream out, int tag, int fieldType, int count, int value) throws IOException {
		writeShort(out, tag);
		writeShort(out, fieldType);
		writeInt(out, count);
		if (count==1 && fieldType==TiffDecoder.SHORT) {
			writeShort(out, value);
			writeShort(out, 0);
		} else
			writeInt(out, value); // may be an offset
	}
	
	/** Writes one IFD (Image File Directory). */
	void writeIFD(OutputStream out, int imageOffset, int nextIFD) throws IOException {	
		int tagDataOffset = HDR_SIZE + ifdSize;
		writeShort(out, nEntries);
		writeEntry(out, TiffDecoder.NEW_SUBFILE_TYPE, 4, 1, 0);
		writeEntry(out, TiffDecoder.IMAGE_WIDTH, 4, 1, fi.width);
		writeEntry(out, TiffDecoder.IMAGE_LENGTH, 4, 1, fi.height);
		if (fi.fileType==FileInfo.RGB||fi.fileType==FileInfo.RGB48) {
			writeEntry(out, TiffDecoder.BITS_PER_SAMPLE,  3, 3, tagDataOffset);
			tagDataOffset += BPS_DATA_SIZE;
		} else
			writeEntry(out, TiffDecoder.BITS_PER_SAMPLE,  3, 1, bitsPerSample);
		writeEntry(out, TiffDecoder.PHOTO_INTERP,     3, 1, photoInterp);
		if (description!=null) {
			writeEntry(out, TiffDecoder.IMAGE_DESCRIPTION, 2, description.length, tagDataOffset);
			tagDataOffset += description.length;
		}
		writeEntry(out, TiffDecoder.STRIP_OFFSETS,    4, 1, imageOffset);
		writeEntry(out, TiffDecoder.SAMPLES_PER_PIXEL,3, 1, samplesPerPixel);
		writeEntry(out, TiffDecoder.ROWS_PER_STRIP,   3, 1, fi.height);
		writeEntry(out, TiffDecoder.STRIP_BYTE_COUNT, 4, 1, imageSize);
		if (fi.unit!=null && fi.pixelWidth!=0 && fi.pixelHeight!=0) {
			writeEntry(out, TiffDecoder.X_RESOLUTION, 5, 1, tagDataOffset);
			writeEntry(out, TiffDecoder.Y_RESOLUTION, 5, 1, tagDataOffset+8);
			tagDataOffset += SCALE_DATA_SIZE;
			int unit = 1;
			if (fi.unit.equals("inch"))
				unit = 2;
			else if (fi.unit.equals("cm"))
				unit = 3;
			writeEntry(out, TiffDecoder.RESOLUTION_UNIT, 3, 1, unit);
		}
		if (fi.fileType==fi.GRAY32_FLOAT) {
			int format = TiffDecoder.FLOATING_POINT;
			writeEntry(out, TiffDecoder.SAMPLE_FORMAT, 3, 1, format);
		}
		if (fi.fileType==FileInfo.COLOR8) {
			writeEntry(out, TiffDecoder.COLOR_MAP, 3, MAP_SIZE, tagDataOffset);
			tagDataOffset += MAP_SIZE*2;
		}
		if (metaDataSize>0) {
			writeEntry(out, TiffDecoder.META_DATA_BYTE_COUNTS, 4, nMetaDataEntries, tagDataOffset);
			writeEntry(out, TiffDecoder.META_DATA, 1, metaDataSize, tagDataOffset+4*nMetaDataEntries);
			tagDataOffset += nMetaDataEntries*4 + metaDataSize;
		}
		writeInt(out, nextIFD);
	}
	
	/** Writes the 6 bytes of data required by RGB BitsPerSample tag. */
	void writeBitsPerPixel(OutputStream out) throws IOException {
		int bitsPerPixel = fi.fileType==FileInfo.RGB48?16:8;
		writeShort(out, bitsPerPixel);
		writeShort(out, bitsPerPixel);
		writeShort(out, bitsPerPixel);
	}

	/** Writes the 16 bytes of data required by the XResolution and YResolution tags. */
	void writeScale(OutputStream out) throws IOException {
		double xscale = 1.0/fi.pixelWidth;
		double yscale = 1.0/fi.pixelHeight;
		double scale = 1000000.0;
		if (xscale>1000.0) scale = 1000.0;
		writeInt(out, (int)(xscale*scale));
		writeInt(out, (int)scale);
		writeInt(out, (int)(yscale*scale));
		writeInt(out, (int)scale);
	}

	/** Writes the variable length ImageDescription string. */
	void writeDescription(OutputStream out) throws IOException {
		out.write(description,0,description.length);
	}

	/** Writes color palette following the image. */
	void writeColorMap(OutputStream out) throws IOException {
		byte[] colorTable16 = new byte[MAP_SIZE*2];
		int j=littleEndian?1:0;
		for (int i=0; i<fi.lutSize; i++) {
			colorTable16[j] = fi.reds[i];
			colorTable16[512+j] = fi.greens[i];
			colorTable16[1024+j] = fi.blues[i];
			j += 2;
		}
		out.write(colorTable16);
	}
	
	/** Writes image metadata ("info" image propery, 
		stack slice labels, channel display ranges, luts, ROIs,
		overlays and extra metadata). */
	void writeMetaData(OutputStream out) throws IOException {
	
		// write byte counts (META_DATA_BYTE_COUNTS tag)
		writeInt(out, 4+nMetaDataTypes*8); // header size	
		if (fi.info!=null && fi.info.length()>0)
			writeInt(out, fi.info.length()*2);
		for (int i=0; i<nSliceLabels; i++) {
			if (fi.sliceLabels[i]==null)
				writeInt(out, 0);
			else
				writeInt(out, fi.sliceLabels[i].length()*2);
		}
		if (fi.displayRanges!=null)
			writeInt(out, fi.displayRanges.length*8);
		if (fi.channelLuts!=null) {
			for (int i=0; i<fi.channelLuts.length; i++)
				writeInt(out, fi.channelLuts[i].length);
		}
		if (fi.roi!=null)
			writeInt(out, fi.roi.length);
		if (fi.overlay!=null) {
			for (int i=0; i<fi.overlay.length; i++)
				writeInt(out, fi.overlay[i].length);
		}
		for (int i=0; i<extraMetaDataEntries; i++)
			writeInt(out, fi.metaData[i].length);	
		
		// write header (META_DATA tag header)
		writeInt(out, TiffDecoder.MAGIC_NUMBER); // "IJIJ"
		if (fi.info!=null) {
			writeInt(out, TiffDecoder.INFO); // type="info"
			writeInt(out, 1); // count
		}
		if (nSliceLabels>0) {
			writeInt(out, TiffDecoder.LABELS); // type="labl"
			writeInt(out, nSliceLabels); // count
		}
		if (fi.displayRanges!=null) {
			writeInt(out, TiffDecoder.RANGES); // type="rang"
			writeInt(out, 1); // count
		}
		if (fi.channelLuts!=null) {
			writeInt(out, TiffDecoder.LUTS); // type="luts"
			writeInt(out, fi.channelLuts.length); // count
		}
		if (fi.roi!=null) {
			writeInt(out, TiffDecoder.ROI); // type="roi "
			writeInt(out, 1); // count
		}
		if (fi.overlay!=null) {
			writeInt(out, TiffDecoder.OVERLAY); // type="over"
			writeInt(out, fi.overlay.length); // count
		}
		for (int i=0; i<extraMetaDataEntries; i++) {
			writeInt(out, fi.metaDataTypes[i]);
			writeInt(out, 1); // count
		}
		
		// write data (META_DATA tag body)
		if (fi.info!=null)
			writeChars(out, fi.info);
		for (int i=0; i<nSliceLabels; i++) {
			if (fi.sliceLabels[i]!=null)
				writeChars(out, fi.sliceLabels[i]);
		}
		if (fi.displayRanges!=null) {
			for (int i=0; i<fi.displayRanges.length; i++)
				writeDouble(out, fi.displayRanges[i]);
		}
		if (fi.channelLuts!=null) {
			for (int i=0; i<fi.channelLuts.length; i++)
				out.write(fi.channelLuts[i]);
		}
		if (fi.roi!=null)
			out.write(fi.roi);
		if (fi.overlay!=null) {
			for (int i=0; i<fi.overlay.length; i++)
				out.write(fi.overlay[i]);
		}
		for (int i=0; i<extraMetaDataEntries; i++)
			out.write(fi.metaData[i]); 
					
	}

	/** Creates an optional image description string for saving calibration data.
		For stacks, also saves the stack size so ImageJ can open the stack without
		decoding an IFD for each slice.*/
	void makeDescriptionString() {
		if (fi.description!=null) {
			if (fi.description.charAt(fi.description.length()-1)!=(char)0)
				fi.description += " ";
			description = fi.description.getBytes();
			description[description.length-1] = (byte)0;
		} else
			description = null;
	}
		
	final void writeShort(OutputStream out, int v) throws IOException {
		if (littleEndian) {
       		out.write(v&255);
        	out.write((v>>>8)&255);
 		} else {
        	out.write((v>>>8)&255);
        	out.write(v&255);
        }
	}

	final void writeInt(OutputStream out, int v) throws IOException {
		if (littleEndian) {
        	out.write(v&255);
        	out.write((v>>>8)&255);
        	out.write((v>>>16)&255);
         	out.write((v>>>24)&255);
		} else {
        	out.write((v>>>24)&255);
        	out.write((v>>>16)&255);
        	out.write((v>>>8)&255);
        	out.write(v&255);
        }
	}

    final void writeLong(OutputStream out, long v) throws IOException {
    	if (littleEndian) {
			buffer[7] = (byte)(v>>>56);
			buffer[6] = (byte)(v>>>48);
			buffer[5] = (byte)(v>>>40);
			buffer[4] = (byte)(v>>>32);
			buffer[3] = (byte)(v>>>24);
			buffer[2] = (byte)(v>>>16);
			buffer[1] = (byte)(v>>> 8);
			buffer[0] = (byte)v;
			out.write(buffer, 0, 8);
        } else {
			buffer[0] = (byte)(v>>>56);
			buffer[1] = (byte)(v>>>48);
			buffer[2] = (byte)(v>>>40);
			buffer[3] = (byte)(v>>>32);
			buffer[4] = (byte)(v>>>24);
			buffer[5] = (byte)(v>>>16);
			buffer[6] = (byte)(v>>> 8);
			buffer[7] = (byte)v;
			out.write(buffer, 0, 8);
        }
     }

    final void writeDouble(OutputStream out, double v) throws IOException {
		writeLong(out, Double.doubleToLongBits(v));
    }
    
	final void writeChars(OutputStream out, String s) throws IOException {
        int len = s.length();
        if (littleEndian) {
			for (int i = 0 ; i < len ; i++) {
				int v = s.charAt(i);
				out.write(v&255); 
				out.write((v>>>8)&255); 
			}
        } else {
			for (int i = 0 ; i < len ; i++) {
				int v = s.charAt(i);
				out.write((v>>>8)&255); 
				out.write(v&255); 
			}
        }
    }

}
