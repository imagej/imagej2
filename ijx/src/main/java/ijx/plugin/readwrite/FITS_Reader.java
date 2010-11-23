package ijx.plugin.readwrite;
import ijx.plugin.api.PlugIn;
import ijx.process.ImageProcessor;
import ijx.measure.Calibration;
import ijx.io.OpenDialog;
import ijx.io.FileInfo;
import ijx.io.FileOpener;
import ijx.ImagePlus;
import ijx.IJ;
import java.io.*;
import java.util.zip.GZIPInputStream;

import ijx.IjxImagePlus;
import ijx.IjxImageStack;

/** Opens and displays FITS images. The FITS format is 
    described at "ftp://nssdc.gsfc.nasa.gov/pub/fits".
*/
public class FITS_Reader extends ImagePlus implements PlugIn {

    public void run(String arg) {
        OpenDialog od = new OpenDialog("Open FITS...", arg);
        String directory = od.getDirectory();
        String fileName = od.getFileName();
        if (fileName==null)
            return;
        IJ.showStatus("Opening: " + directory + fileName);
        FitsDecoder fd = new FitsDecoder(directory, fileName);
        FileInfo fi = null;
        try {fi = fd.getInfo();}
        catch (IOException e) {}
        if (fi!=null && fi.width>0 && fi.height>0 && fi.offset>0) {
            FileOpener fo = new FileOpener(fi);
            IjxImagePlus imp = fo.open(false);
            if(fi.nImages==1) {
              ImageProcessor ip = imp.getProcessor();              
              ip.flipVertical(); // origin is at bottom left corner
              setProcessor(fileName, ip);
            } else {
              IjxImageStack stack = imp.getStack(); // origin is at bottom left corner              
              for(int i=1; i<=stack.getSize(); i++)
                  stack.getProcessor(i).flipVertical();
              setStack(fileName, stack);
            }
            Calibration cal = imp.getCalibration();
            if (fi.fileType==FileInfo.GRAY16_SIGNED && fd.bscale==1.0 && fd.bzero==32768.0)
                cal.setFunction(Calibration.NONE, null, "Gray Value");
            setCalibration(cal);
            setProperty("Info", fd.getHeaderInfo());
			setFileInfo(fi); // needed for File->Revert
            if (arg.equals("")) show();
        } else
            IJ.error("This does not appear to be a FITS file.");
        IJ.showStatus("");
    }

}

class FitsDecoder {
    private String directory, fileName;
    private DataInputStream f;
    private StringBuffer info = new StringBuffer(512);
    double bscale, bzero;

    public FitsDecoder(String directory, String fileName) {
        this.directory = directory;
        this.fileName = fileName;
    }

    FileInfo getInfo() throws IOException {
        FileInfo fi = new FileInfo();
        fi.fileFormat = FileInfo.FITS;
        fi.fileName = fileName;
        fi.directory = directory;
        fi.width = 0;
        fi.height = 0;
        fi.offset = 0;

        InputStream is = new FileInputStream(directory + fileName);
        if (fileName.toLowerCase().endsWith(".gz")) is = new GZIPInputStream(is);
        f = new DataInputStream(is);
        String line = getString(80);
        info.append(line+"\n");
        if (!line.startsWith("SIMPLE"))
            {f.close(); return null;}
        int count = 1;
        while ( true ) {
            count++;
            line = getString(80);
			info.append(line+"\n");
  
            // Cut the key/value pair
			int index = line.indexOf ( "=" );

			// Strip out comments
			int commentIndex = line.indexOf ( "/", index );
			if ( commentIndex < 0 )
				commentIndex = line.length ();
			
			// Split that values
			String key;
			String value;
			if ( index >= 0 ) {
				key = line.substring ( 0, index ).trim ();
				value = line.substring ( index + 1, commentIndex ).trim ();
			} else {
				key = line.trim ();
				value = "";
			}

			// Time to stop ?
			if (key.equals ("END") ) break;

			// Look for interesting information			
            if (key.equals("BITPIX")) {
                int bitsPerPixel = Integer.parseInt ( value );
               if (bitsPerPixel==8)
                    fi.fileType = FileInfo.GRAY8;
                else if (bitsPerPixel==16)
                    fi.fileType = FileInfo.GRAY16_SIGNED;
                else if (bitsPerPixel==32)
                    fi.fileType = FileInfo.GRAY32_INT;
                else if (bitsPerPixel==-32)
                    fi.fileType = FileInfo.GRAY32_FLOAT;
                else if (bitsPerPixel==-64)
                    fi.fileType = FileInfo.GRAY64_FLOAT;
                else {
                    IJ.error("BITPIX must be 8, 16, 32, -32 (float) or -64 (double).");
                    f.close();
                    return null;
                }
            } else if (key.equals("NAXIS1"))
                fi.width = Integer.parseInt ( value );
            else if (key.equals("NAXIS2"))
                fi.height = Integer.parseInt( value );
            else if (key.equals("NAXIS3")) //for multi-frame fits
                fi.nImages = Integer.parseInt ( value );
            else if (key.equals("BSCALE"))
                bscale = parseDouble ( value );
            else if (key.equals("BZERO"))
                bzero = parseDouble ( value );

			if (count>360 && fi.width==0)
				{f.close(); return null;}
        }

        f.close();
        fi.offset = 2880+2880*(((count*80)-1)/2880);
        return fi;
    }

    String getString(int length) throws IOException {
        byte[] b = new byte[length];
        f.readFully(b);
        return new String(b);
    }

    int getInteger(String s) {
        s = s.substring(10, 30);
        s = s.trim();
        return Integer.parseInt(s);
    }

	double parseDouble(String s) throws NumberFormatException {
		Double d = new Double(s);
		return d.doubleValue();
	}

    String getHeaderInfo() {
        return new String(info);
    }

}
