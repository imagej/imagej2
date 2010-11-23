import ij.plugin.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.image.*;
import java.io.*;
import java.lang.reflect.*;
import ij.*;

	
/** This plugin uses QuickTime for Java to open PICT images on the Mac system 
     clipboard. Only needed on Macs running 32-bit Java and OS X 10.5 and earlier. */
public class MacClipboard extends IjxImagePlus implements PlugIn {
	static java.awt.datatransfer.Clipboard clipboard;
	
	public void run(String arg) {
		Image img = showSystemClipboard();
		if (img!=null) setImage(img);
	}
	
	Image showSystemClipboard() {
		Image img = null;
		if (clipboard==null)
			clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		try {
			Transferable transferable = clipboard.getContents(null);
			img = displayMacImage(transferable);
		} catch (Throwable e) {
			IJ.handleException(e);
		}
		return img;
	}
	
    Image displayMacImage(Transferable t) {
    	Image img = getMacImage(t);
    	if (img!=null) {
			WindowManager.checkForDuplicateName = true;          
			IJ.getFactory().newImagePlus("Clipboard", img).show();
		}
		return img;
    }

	// Mac OS X's data transfer handling is horribly broken. We sometimes
	// need to use the "image/x-pict" MIME type and then Quicktime
	// for Java in order to get the image data.
	Image getMacImage(Transferable t) {
		if (!isQTJavaInstalled())
			return null;
		Image img = null;
		DataFlavor[] d = t.getTransferDataFlavors();
		if (d==null || d.length==0)
			return null;
		try {
			Object is = t.getTransferData(d[0]);
			if (is==null || !(is instanceof InputStream))
				return null;
			img = getImageFromPictStream((InputStream)is);
		} catch (Exception e) {}
		return img;
    }
    
	// Converts a PICT to an AWT image using QuickTime for Java.
	// This code was contributed by Gord Peters.
	Image getImageFromPictStream(InputStream is) {
		try {
			ByteArrayOutputStream baos= new ByteArrayOutputStream();
			// We need to strip the header from the data because a PICT file
			// has a 512 byte header and then the data, but in our case we only
			// need the data. --GP
			byte[] header= new byte[512];
			byte[] buf= new byte[4096];
			int retval= 0, size= 0;
			baos.write(header, 0, 512);
			while ( (retval= is.read(buf, 0, 4096)) > 0)
				baos.write(buf, 0, retval);		
			baos.close();
			size = baos.size();
			//IJ.log("size: "+size); IJ.wait(2000);
			if (size<=0)
				return null;
			byte[] imgBytes= baos.toByteArray();
			// Again with the uglyness.  Here we need to use the Quicktime
			// for Java code in order to create an Image object from
			// the PICT data we received on the clipboard.  However, in
			// order to get this to compile on other platforms, we use
			// the Java reflection API.
			//
			// For reference, here is the equivalent code without
			// reflection:
			//
			//
			// if (QTSession.isInitialized() == false) {
			//     QTSession.open();
			// }
			// QTHandle handle= new QTHandle(imgBytes);
			// GraphicsImporter gi=
			//     new GraphicsImporter(QTUtils.toOSType("PICT"));
			// gi.setDataHandle(handle);
			// QDRect qdRect= gi.getNaturalBounds();
			// GraphicsImporterDrawer gid= new GraphicsImporterDrawer(gi);
			// QTImageProducer qip= new QTImageProducer(gid,
			//                          new Dimension(qdRect.getWidth(),
			//                                        qdRect.getHeight()));
			// return(Toolkit.getDefaultToolkit().createImage(qip));
			//
			// --GP
			//IJ.log("quicktime.QTSession");
			Class c = Class.forName("quicktime.QTSession");
			Method m = c.getMethod("isInitialized", null);
			Boolean b= (Boolean)m.invoke(null, null);			
			if (b.booleanValue() == false) {
				m= c.getMethod("open", null);
				m.invoke(null, null);
			}
			c= Class.forName("quicktime.util.QTHandle");
			Constructor con = c.getConstructor(new Class[] {imgBytes.getClass() });
			Object handle= con.newInstance(new Object[] { imgBytes });
			String s= new String("PICT");
			c = Class.forName("quicktime.util.QTUtils");
			m = c.getMethod("toOSType", new Class[] { s.getClass() });
			Integer type= (Integer)m.invoke(null, new Object[] { s });
			c = Class.forName("quicktime.std.image.GraphicsImporter");
			con = c.getConstructor(new Class[] { type.TYPE });
			Object importer= con.newInstance(new Object[] { type });
			m = c.getMethod("setDataHandle",
			new Class[] { Class.forName("quicktime.util." + "QTHandleRef") });
			m.invoke(importer, new Object[] { handle });
			m = c.getMethod("getNaturalBounds", null);
			Object rect= m.invoke(importer, null);
			c = Class.forName("quicktime.app.view.GraphicsImporterDrawer");
			con = c.getConstructor(new Class[] { importer.getClass() });
			Object iDrawer = con.newInstance(new Object[] { importer });
			m = rect.getClass().getMethod("getWidth", null);
			Integer width= (Integer)m.invoke(rect, null);
			m = rect.getClass().getMethod("getHeight", null);
			Integer height= (Integer)m.invoke(rect, null);
			Dimension d= new Dimension(width.intValue(), height.intValue());
			c = Class.forName("quicktime.app.view.QTImageProducer");
			con = c.getConstructor(new Class[] { iDrawer.getClass(), d.getClass() });
			Object producer= con.newInstance(new Object[] { iDrawer, d });
			if (producer instanceof ImageProducer)
				return(Toolkit.getDefaultToolkit().createImage((ImageProducer)producer));
		} catch (Exception e) {
			IJ.showStatus(""+e);
		}
		return null;
    }

	// Retuns true if QuickTime for Java is installed.
	// This code was contributed by Gord Peters.
	boolean isQTJavaInstalled() {
		boolean isInstalled = false;
		try {
			Class c= Class.forName("quicktime.QTSession");
			isInstalled = true;
		} catch (Exception e) {}
		return isInstalled;
	}

}



