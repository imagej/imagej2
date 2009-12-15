import java.io.*; 
import java.awt.*; 
import ij.*; 
import ij.gui.*; 
import ij.plugin.*;
import ij.process.*;
import ij.io.*;
import ij.measure.Calibration;

//	This plugin saves Analyze format files.  
//	It appends the '.img' and '.hdr' suffixes to the image and header files
//	respectively. 
//	- Saves in big endian format.   
//	- Will not work on RGB images (unlike associated ReadAnalyze plugin).
//	- Requires ImageJ 1.16 or later 
//
//	Guy Williams, gbw1000@wbic.cam.ac.uk 	23/9/99
 

public final class Analyze_Writer implements PlugIn {

	public void run(String arg) {
		ImagePlus imp = WindowManager.getCurrentImage();
	 	if (imp==null) {
			IJ.noImage(); 
			return; 
		}
		if (imp.getCalibration().isSigned16Bit() && IJ.versionLessThan("1.34e"))
			return;
		String directory = "", name = ""; 
		if ((arg==null) || (arg=="")) {
			SaveDialog sd = new SaveDialog("Save as Analyze", imp.getTitle(), ".img");
			directory = sd.getDirectory();
			name = sd.getFileName();
		} else {
			File file = new File(arg); 
			if (file.isDirectory()) {
				directory = arg;
				name = imp.getTitle();
			} else {
				directory = file.getParent(); 
				name = file.getName(); 
			}	
		}
		if (name == null || name == "") {
			//IJ.error("No filename selected");
			return;
		}
		ImageStack stack = imp.getStack();
		for (int i=1; i<=stack.getSize(); i++) {
			ImageProcessor ip = stack.getProcessor(i);
			ip.flipVertical();
		}
		save(imp, directory, name);
		for (int i=1; i<=stack.getSize(); i++) {
			ImageProcessor ip = stack.getProcessor(i);
			ip.flipVertical();
		}
		IJ.showStatus(""); 
	}

	// Save return false if one of the files already exists and the user pressed Cancel.
	public void save(ImagePlus imp, String directory, String name) {
		if (name == null) return;
		if (name.endsWith(".img")) name = name.substring(0, name.length() - 4); 
		if (name.endsWith(".hdr")) name = name.substring(0, name.length() - 4); 
		if (!directory.endsWith(File.separator)&& !directory.equals("")) directory += File.separator; 
		IJ.showStatus("Saving as Analyze: " + directory + name);

		try {
			String fileName = directory + name + ".hdr";			writeHeader( imp, fileName);			
			fileName = directory + name + ".img";				if (imp.getStackSize() < 2)
				new FileSaver(imp).saveAsRaw(fileName); 
			else
				new FileSaver(imp).saveAsRawStack(fileName); 
		}
		catch (IOException e) {
			IJ.log("Analyze_Writer: "+ e.getMessage());
		}
	} 

	private void writeHeader( ImagePlus imp, String hdrfile ) throws IOException {
		FileOutputStream fileout = new FileOutputStream(hdrfile);
		DataOutputStream output = new DataOutputStream(fileout);
		FileInfo fi = imp.getFileInfo();
		short bitsallocated, datatype;

		switch (fi.fileType) {	
			case FileInfo.GRAY8:
				datatype = 2; 		// DT_UNSIGNED_CHAR 
				bitsallocated = 8;
				break;
			case FileInfo.GRAY16_SIGNED:
			case FileInfo.GRAY16_UNSIGNED:
				datatype = 4; 		// DT_SIGNED_SHORT 
				bitsallocated = 16;
				break;
			case FileInfo.GRAY32_INT:
				datatype = 8; 		// DT_SIGNED_INT
				bitsallocated = 32;
				break; 
			case FileInfo.GRAY32_FLOAT:
				datatype = 16; 		// DT_FLOAT 
				bitsallocated = 32;
				break; 
			default:
				datatype = 0;		// DT_UNKNOWN
				bitsallocated = (short) (fi.getBytesPerPixel() * 8) ; 
		}

		//     header_key  

		writeInt(output, 348); 				// sizeof_hdr
		int i;
		for (i = 0; i < 10; i++) output.write( 0 );	// data_type
		for (i = 0; i < 18; i++) output.write( 0 ); 	// db_name 
		writeInt(output, 16384); 				// extents 
		output.writeShort( 0); 								// session_error
		output.writeByte ( (int) 'r' );				// regular 
		output.writeByte ( 0 );				// hkey_un 

		// image_dimension

		writeShort(output, (short) 4 );				// dim[0] 
		writeShort(output, (short) fi.width );			// dim[1] 
		writeShort(output, (short) fi.height );			// dim[2] 
		writeShort(output, (short) fi.nImages );		// dim[3] 
		writeShort(output, (short) 1 );				// dim[4] 
		for (i = 0; i < 3; i++) output.writeShort( 0 );		// dim[5-7]
		
		output.writeBytes ( "mm\0\0" );			// vox_units
		for (i = 0; i < 8; i++) output.write( 0 );		// cal_units[8] 
		output.writeShort( 0 );				// unused1
		writeShort( output, (short) datatype );		// datatype 
		writeShort( output, (short) bitsallocated );		// bitpix
		output.writeShort( 0 );					// dim_un0
		
		output.writeFloat( 0 );					// pixdim[0] 
		writeFloat(output, (float) fi.pixelWidth );		// pixdim[1] 
		writeFloat(output, (float) fi.pixelHeight );		// pixdim[2] 
		writeFloat(output, (float) fi.pixelDepth ); 		// pixdim[3] 
		for (i = 0; i < 4; i++) output.writeFloat( 0 );	// pixdim[4-7]
		
		output.writeFloat( 0 );		// vox_offset 
		output.writeFloat( 1 );		// roi_scale 
		output.writeFloat( 0 );		// funused1 
		output.writeFloat( 0 );		// funused2 
		output.writeFloat( 0 );		// cal_max 
		output.writeFloat( 0 );		// cal_min 
		output.writeInt( 0 );			// compressed
		output.writeInt( 0 );			// verified  
		ImageStatistics s = imp.getStatistics();
		writeInt(output, (int) s.max );		// glmax 
		writeInt(output, (int) s.min );		// glmin 

		// data_history 

		for (i = 0; i < 80; i++) output.write( 0 );		// descrip  
		for (i = 0; i < 24; i++) output.write( 0 );		// aux_file 
		output.write(0);											// orient 
		for (i = 0; i < 10; i++) output.write( 0 );		// originator 
		for (i = 0; i < 10; i++) output.write( 0 );		// generated 
		for (i = 0; i < 10; i++) output.write( 0 );		// scannum 
		for (i = 0; i < 10; i++) output.write( 0 );		// patient_id  
		for (i = 0; i < 10; i++) output.write( 0 );		// exp_date 
		for (i = 0; i < 10; i++) output.write( 0 );		// exp_time  
		for (i = 0; i < 3; i++)  output.write( 0 );		// hist_un0
		output.writeInt( 0 );		// views 
		output.writeInt( 0 );		// vols_added 
		output.writeInt( 0 );		// start_field  
		output.writeInt( 0 );		// field_skip
		output.writeInt( 0 );		// omax  
		output.writeInt( 0 );		// omin 
		output.writeInt( 0 );		// smax  
		output.writeInt( 0 );		// smin 

		output.close();
		fileout.close();
	}
	
	private void writeInt(DataOutputStream input, int value) throws IOException {
		/*
		byte b1 = (byte) (value & 0xff);
		byte b2 = (byte) ((value >> 8) & 0xff);
		byte b3 = (byte) ((value >> 16) & 0xff);
		byte b4 = (byte) ((value >> 24) & 0xff); 
		input.writeByte(b1);
		input.writeByte(b2);
		input.writeByte(b3);
		input.writeByte(b4);
		*/
		input.writeInt( value );  
	}
	
	private void writeShort(DataOutputStream input, short value) throws IOException {
		/*     byte b1 = (byte) (value & 0xff);
		byte b2 = (byte) ((value >> 8) & 0xff);
		input.writeByte(b1);
		input.writeByte(b2);
		*/
		input.writeShort( value ); 
	}
	
	private void writeFloat(DataOutputStream input, float value) throws IOException {
		writeInt(input, Float.floatToIntBits( value ) ); 
	}

        // adds the specified value to every pixel in the stack
        void add(ImagePlus imp, int value) {
            //IJ.log("add: "+value);
            ImageStack stack = imp.getStack();
            for (int slice=1; slice<=stack.getSize(); slice++) {
                ImageProcessor ip = stack.getProcessor(slice);
                short[] pixels = (short[])ip.getPixels();
                for (int i=0; i<pixels.length; i++)
                    pixels[i] = (short)((pixels[i]&0xffff)+value);
            }
         }

}
