import java.io.*; 
import java.awt.*; 
import ij.*; 
import ij.plugin.*;
import ij.process.*;
import ij.io.*;
import ij.measure.*;

/** This plugin loads Analyze format files.  
    It parses the header file found in '<filename>.hdr' and uses this to 
    appropriately load the raw image data found in '<filename>.img'. 
      - Loads either big or little endian format.   
      - Requires ImageJ 1.16 or later 

    Guy Williams, gbw1000@wbic.cam.ac.uk        23/9/99
*/


public class Analyze_Reader extends ImagePlus implements PlugIn {
  
  public boolean littleEndian = false;

	public void run(String arg) {
		OpenDialog od = new OpenDialog("Open Analyze...", arg);
		String directory = od.getDirectory();
		String name = od.getFileName();
		if (name==null) return;
		IJ.showStatus("Opening: " + directory + name);
		FileInfo fi = load(directory, name);
		FileOpener fo = new FileOpener(fi);  
		ImagePlus imp = fo.open(false);
		if (imp==null) return;
		ImageStack stack = imp.getStack();
		for (int i=1; i<=stack.getSize(); i++) {
			ImageProcessor ip = stack.getProcessor(i);
			ip.flipVertical();
		}
		if (imp.getStackSize()>1)
			setStack(name, stack);
		else
			setProcessor(name, imp.getProcessor());
		setCalibration(imp.getCalibration());
		setFileInfo(fi); // needed for revert
		if (arg.equals("")) show();
	}

	FileInfo load(String directory, String name) {
		FileInfo fi = new FileInfo(); 
		if ((name == null) || (name == "")) return null;
		if (name.endsWith(".img")||name.endsWith(".IMG"))
			name = name.substring(0, name.length()-4 ); 
		if (name.endsWith(".hdr")||name.endsWith(".HDR"))
			name = name.substring(0, name.length()-4 ); 
		IJ.showStatus("Loading Analyze File: " + directory + name);
		try {
			fi = readHeader( directory+name+".hdr" );
		}
		catch (IOException e) { IJ.log("Analyze Reader: "+ e.getMessage()); }
		fi.fileName = name + ".img";
		fi.directory = directory;
		fi.fileFormat = fi.RAW;
		return fi;
	} 
 
  public FileInfo readHeader( String hdrfile ) throws IOException 
    {
    FileInputStream filein = new FileInputStream (hdrfile);
    DataInputStream input = new DataInputStream (filein);
    FileInfo fi = new FileInfo();
    byte[] units = new byte[4]; 

    this.littleEndian = false;     

    int i;
    short bitsallocated, datatype;
// In order to get the sliceSpacing, ImagePlus has been altered

//  header_key  

    input.readInt (); 				// sizeof_hdr
    for (i=0; i<10; i++) input.read();		// data_type
    for (i=0; i<18; i++) input.read(); 		// db_name 
    input.readInt (); 				// extents 
    input.readShort (); 			// session_error
    input.readByte ();				// regular 
    input.readByte (); 				// hkey_un0 

// image_dimension

    short endian = readShort (input);		// dim[0] 
    if ((endian < 0) || (endian > 15)) 
      { littleEndian = true;
        fi.intelByteOrder = true; }  
    fi.width = readShort (input);		// dim[1] 
    fi.height = readShort (input);		// dim[2] 
    fi.nImages = readShort (input);		// dim[3] 
    input.readShort ();				// dim[4] 
    for (i=0; i<3; i++) input.readShort();	// dim[5-7] 
    input.read (units, 0, 4); 			// vox_units 
    fi.unit = new String (units, 0, 4); 
    for (i=0; i<8; i++) input.read();		// cal_units[8] 
    input.readShort();				// unused1
    datatype = readShort( input );		// datatype 
    bitsallocated = readShort( input );		// bitpix
    input.readShort ();				// dim_un0
    input.readFloat ();				// pixdim[0] 
    fi.pixelWidth = (double) readFloat(input);	// pixdim[1] 
    fi.pixelHeight = (double) readFloat(input); // pixdim[2] 
    fi.pixelDepth = (double) readFloat(input); 	// pixdim[3] 
    for (i=0; i<4; i++) input.readFloat();	// pixdim[4-7]  
    fi.offset = (int) readFloat(input);			// vox_offset
    input.readFloat ();				// roi_scale 
    input.readFloat ();				// funused1 
    input.readFloat ();				// funused2 
    input.readFloat ();				// cal_max 
    input.readFloat ();				// cal_min 
    input.readInt ();				// compressed
    input.readInt ();				// verified  
    //   ImageStatistics s = imp.getStatistics();
    readInt (input);	//(int) s.max		// glmax 
    readInt (input);	//(int) s.min		// glmin 

// data_history 

    for (i=0; i<80; i++) input.read();		// descrip  
    for (i=0; i<24; i++) input.read();		// aux_file 
    input.read();				// orient 
    for (i=0; i<10; i++) input.read();		// originator 
    for (i=0; i<10; i++) input.read();		// generated 
    for (i=0; i<10; i++) input.read();		// scannum 
    for (i=0; i<10; i++) input.read();		// patient_id  
    for (i=0; i<10; i++) input.read();		// exp_date 
    for (i=0; i<10; i++) input.read();		// exp_time  
    for (i=0; i<3; i++) input.read();		// hist_un0
    input.readInt ();				// views 
    input.readInt ();				// vols_added 
    input.readInt ();				// start_field  
    input.readInt ();				// field_skip
    input.readInt ();				// omax  
    input.readInt ();				// omin 
    input.readInt ();				// smax  
    input.readInt ();				// smin 

    input.close();
    filein.close();
    
    switch (datatype) {
      
     case 2:
      fi.fileType = FileInfo.GRAY8; 			// DT_UNSIGNED_CHAR 
      bitsallocated = 8;
      break;
     case 4:
      fi.fileType = FileInfo.GRAY16_SIGNED; 		// DT_SIGNED_SHORT 
      bitsallocated = 16;
      break;
     case 8:
      fi.fileType = FileInfo.GRAY32_INT; 		// DT_SIGNED_INT
      bitsallocated = 32;
      break; 
     case 16:
      fi.fileType = FileInfo.GRAY32_FLOAT; 		// DT_FLOAT 
      bitsallocated = 32;
      break; 
     case 128:
      fi.fileType = FileInfo.RGB_PLANAR; 		// DT_RGB
      bitsallocated = 24; 
      break; 
     default:
      fi.fileType = 0;					// DT_UNKNOWN
    }
    
    return (fi);
    }
  
  public int readInt(DataInputStream input) throws IOException 
    {
      if (!littleEndian) return input.readInt(); 
      byte b1 = input.readByte();
      byte b2 = input.readByte();
      byte b3 = input.readByte();
      byte b4 = input.readByte();
      return ( (((b4 & 0xff) << 24) | ((b3 & 0xff) << 16) | ((b2 & 0xff) << 8) | (b1 & 0xff)) );
    }
  
  public short readShort(DataInputStream input) throws IOException
    {
      if (!littleEndian) return input.readShort(); 
      byte b1 = input.readByte();
      byte b2 = input.readByte();
      return ( (short) (((b2 & 0xff) << 8) | (b1 & 0xff)) );
    }
  
  public float readFloat(DataInputStream input) throws IOException 
    {
      if (!littleEndian) return input.readFloat();  
      int orig = readInt(input);
      return (Float.intBitsToFloat(orig));
    }
}

