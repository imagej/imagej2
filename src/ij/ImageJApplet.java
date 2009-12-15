package ij;
import ijx.IjxApplication;
import ijx.IjxImagePlus;
import java.applet.Applet;

/**
	Runs ImageJ as an applet and optionally opens up to 
	nine images using URLs passed as a parameters.
	<p>
	Here is an example applet tag that launches ImageJ as an applet
	and passes it the URLs of two images:
	<pre>
	&lt;applet archive="../ij.jar" code="ij.ImageJApplet.class" width=0 height=0&gt;
	&lt;param name=url1 value="http://rsb.info.nih.gov/ij/images/FluorescentCells.jpg"&gt;
	&lt;param name=url2 value="http://rsb.info.nih.gov/ij/images/blobs.gif"&gt;
	&lt;/applet&gt;
	</pre>
	To use plugins, add them to ij.jar and add entries to IJ_Props.txt file (in ij.jar) that will  
	create commands for them in the Plugins menu, or a submenu. There are examples 
	of such entries in IJ.Props.txt, in the "Plugins installed in the Plugins menu" section.
	<p>
	Macros contained in a file named "StartupMacros.txt", in the same directory as the HTML file
	containing the applet tag, will be installed on startup.
*/
public class ImageJApplet extends Applet {

	/** Starts ImageJ if it's not already running. */
    public void init() {
        IjxApplication ij = IJ.getInstance();
     	if (ij==null || (ij!=null && !IJ.getTopComponentFrame().isShowing()))
			new ImageJ(this);
		for (int i=1; i<=9; i++) {
			String url = getParameter("url"+i);
			if (url==null) break;
			IjxImagePlus imp = IJ.getFactory().newImagePlus(url);
			if (imp!=null) imp.show();
		}
    }
    
    public void destroy() {
    	IjxApplication ij = IJ.getInstance();
    	if (ij!=null) ij.quit();
    }

}

