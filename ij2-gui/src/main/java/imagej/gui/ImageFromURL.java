package imagej.gui;

import imagej.plugin.ij2.IPlugin;
import imagej.plugin.ij2.Parameter;
import imagej.plugin.ij2.Plugin;

import java.awt.Image;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;

@Plugin
public class ImageFromURL implements IPlugin {

	@Parameter
	private String url;

	@Parameter(output=true)
	private Image image;

	@Override
	public void run() {
		ImageIcon icon;
		try {
			icon = new ImageIcon(new URL(url));
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);//TEMP
		}
		image = icon.getImage();
	}
}
