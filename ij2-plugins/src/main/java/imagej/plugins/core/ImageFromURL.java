package imagej.plugins.core;

import imagej.plugin.IPlugin;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

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
