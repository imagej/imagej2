package ijx.io;
import ijx.process.ImageProcessor;
import ijx.process.ByteProcessor;
import ijx.process.ShortProcessor;
import ijx.measure.Calibration;
import ijx.IJ;
import java.io.*;


/** Saves an image described by an ImageProcessor object as a tab-delimited text file. */
public class TextEncoder {

	private ImageProcessor ip;
	private Calibration cal;
	private int precision;

	/** Constructs a TextEncoder from an ImageProcessor and optional Calibration. */
	public TextEncoder (ImageProcessor ip, Calibration cal, int precision) {
		this.ip = ip;
		this.cal = cal;
		this.precision = precision;
	}

	/** Saves the image as a tab-delimited text file. */
	public void write(DataOutputStream out) throws IOException {
		PrintWriter pw = new PrintWriter(out);
		boolean calibrated = cal!=null && cal.calibrated();
		if (calibrated)
			ip.setCalibrationTable(cal.getCTable());
		else
			ip.setCalibrationTable(null);
		boolean intData = !calibrated && ((ip instanceof ByteProcessor) || (ip instanceof ShortProcessor));
		int width = ip.getWidth();
		int height = ip.getHeight();
		int inc = height/20;
		if (inc<1) inc = 1;
		//IJ.showStatus("Exporting as text...");
		double value;
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				value = ip.getPixelValue(x,y);
				if (intData)
					pw.print((int)value);
				else
					pw.print(IJ.d2s(value, precision));
				if (x!=(width-1))
					pw.print("\t");
			}
			pw.println();
			if (y%inc==0) IJ.showProgress((double)y/height);
		}
		pw.close();
		IJ.showProgress(1.0);
		//IJ.showStatus("");
	}
	
}
