package ijx.io;
import ijx.process.ImageProcessor;
import ijx.plugin.api.PlugInFilter;
import ijx.io.FileSaver;

import ijx.IjxImagePlus;


/** This plugin saves an image in tiff, gif, jpeg, bmp, png, text or raw format. */
public class Writer implements PlugInFilter {
	private String arg;
    private IjxImagePlus imp;
    
	public int setup(String arg, IjxImagePlus imp) {
		this.arg = arg;
		this.imp = imp;
		return DOES_ALL+NO_CHANGES;
	}

	public void run(ImageProcessor ip) {
		if (arg.equals("tiff"))
			new FileSaver(imp).saveAsTiff();
		else if (arg.equals("gif"))
			new FileSaver(imp).saveAsGif();
		else if (arg.equals("jpeg"))
			new FileSaver(imp).saveAsJpeg();
		else if (arg.equals("text"))
			new FileSaver(imp).saveAsText();
		else if (arg.equals("lut"))
			new FileSaver(imp).saveAsLut();
		else if (arg.equals("raw"))
			new FileSaver(imp).saveAsRaw();
		else if (arg.equals("zip"))
			new FileSaver(imp).saveAsZip();
		else if (arg.equals("bmp"))
			new FileSaver(imp).saveAsBmp();
		else if (arg.equals("png"))
			new FileSaver(imp).saveAsPng();
		else if (arg.equals("pgm"))
			new FileSaver(imp).saveAsPgm();
		else if (arg.equals("fits"))
			new FileSaver(imp).saveAsFits();
	}
	
}


