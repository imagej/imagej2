package imagej.util;
import ijx.plugin.api.PlugIn;
import ijx.gui.dialog.GenericDialog;
import ijx.Prefs;
import ijx.IJ;


import java.util.*;

/** This plugin implements the Plugins/Utilities/Proxy Settings command. It sets
* 	the JVM proxy properties to allow the Plugins/Utilities/Update ImageJ command
*	and File/Open Samples menu to work on networks behind a proxy server. 
* 
*     @author	Dimiter Prodanov
*/

public class ProxySettings implements PlugIn {
	private static Properties props = System.getProperties();
	private String proxyhost = Prefs.get("proxy.server", "");
	private int proxyport = (int)Prefs.get("proxy.port", 8080);
	private String proxyuser = Prefs.get("proxy.user", "");
	private String proxypass = "";
	private boolean authenticate;
	
	public void run(String arg) {
		if (IJ.getApplet()!=null) return;
		String host = System.getProperty("http.proxyHost");
		if (host!=null) proxyhost = host;
		String port = System.getProperty("http.proxyPort");
		if (port!=null) {
			double portNumber = Tools.parseDouble(port);
			if (!Double.isNaN(portNumber))
				proxyport = (int)portNumber;
		}
		String user = System.getProperty("http.proxyUser");
		if (user!=null) proxyuser = user;
		String pass = System.getProperty("http.proxyPassword");
		if (pass!=null) proxypass = pass;
		if (!showDialog()) return;
		if (!proxyhost.equals(""))
			props.put("proxySet", "true");
		else
			props.put("proxySet", "false");
		props.put("http.proxyHost", proxyhost);
		props.put("http.proxyPort", ""+proxyport);
		props.put("http.proxyUser", proxyuser);
		props.put("http.proxyPassword", proxypass);
		Prefs.set("proxy.server", proxyhost);
		Prefs.set("proxy.port", proxyport);
		Prefs.set("proxy.user", proxyuser);
		if (IJ.debugMode)  {
			IJ.log("proxy set: "+ System.getProperty("proxySet"));
			IJ.log("proxy host: "+ System.getProperty("http.proxyHost"));
			IJ.log("proxy port: "+System.getProperty("http.proxyPort"));
			IJ.log("proxy username: "+ System.getProperty("http.proxyUser"));
			IJ.log("proxy password: "+ System.getProperty("http.proxyPassword"));
		}
	}

	boolean showDialog()   {
		GenericDialog gd=new GenericDialog("Proxy Settings");
		gd.addStringField("Proxy Server:", proxyhost, 15);
		gd.addNumericField("Port:", proxyport , 0);
		gd.addCheckbox(" Authenticate", authenticate);
		gd.showDialog();
		if (gd.wasCanceled())
			return false;
		proxyhost = gd.getNextString();
		proxyport = (int)gd.getNextNumber();
		authenticate = gd.getNextBoolean();
		if (authenticate && !proxyhost.equals(""))  {
			GenericDialog gd2=new GenericDialog("Authentication");
			gd2.setInsets(0,0,12);
			gd2.addMessage("Enter user name and password for proxy server");
			gd2.addStringField("User:", proxyuser, 12);
			gd2.setEchoChar('*');
			gd2.addStringField("Password:", proxypass, 12);
			gd2.showDialog();
			if (gd. wasCanceled())
				return false;
			proxyuser = gd2.getNextString();
			proxypass = gd2.getNextString();
		} else {
			proxyuser = "";
			proxypass = "";
		}
		return true;
	}

}
