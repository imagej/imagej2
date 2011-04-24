/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.display.lut;

import imagej.display.view.GraphicsUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author GBH
 */
public class LutBuilder {

	private LutBuilder() {
	}

	private final static LutBuilder INSTANCE = new LutBuilder();

	public static synchronized LutBuilder getInstance() {
		return LutBuilder.INSTANCE;
	}

	public static String[] lutNames = new String[]{
		"invert", "fire", "grays", "ice", "spectrum", "3-3-2 RGB", "red",
		"green", "blue", "cyan", "magenta", "yellow", "redgreen", "bty"};



	public Lut createLUT(String arg) {
		Lut lut = new Lut();
		int nColors = 0;

		if (arg.equals("invert")) {
			// {invertLut(); return;
		} else if (arg.equals("fire")) {
			nColors = fire(lut.reds, lut.greens, lut.blues);
		} else if (arg.equals("grays")) {
			nColors = grays(lut.reds, lut.greens, lut.blues);
		} else if (arg.equals("ice")) {
			nColors = ice(lut.reds, lut.greens, lut.blues);
		} else if (arg.equals("spectrum")) {
			nColors = spectrum(lut.reds, lut.greens, lut.blues);
		} else if (arg.equals("3-3-2 RGB")) {
			nColors = rgb332(lut.reds, lut.greens, lut.blues);

			// primary colors ...
		} else if (arg.equals("red")) {
			nColors = primaryColor(4, lut.reds, lut.greens, lut.blues);
		} else if (arg.equals("green")) {
			nColors = primaryColor(2, lut.reds, lut.greens, lut.blues);
		} else if (arg.equals("blue")) {
			nColors = primaryColor(1, lut.reds, lut.greens, lut.blues);
		} else if (arg.equals("cyan")) {
			nColors = primaryColor(3, lut.reds, lut.greens, lut.blues);
		} else if (arg.equals("magenta")) {
			nColors = primaryColor(5, lut.reds, lut.greens, lut.blues);
		} else if (arg.equals("yellow")) {
			nColors = primaryColor(6, lut.reds, lut.greens, lut.blues);
			//
		} else if (arg.equals("redgreen")) {
			nColors = redGreen(lut.reds, lut.greens, lut.blues);
		}
		if (nColors > 0) {
			if (nColors < 256) {
				interpolate(lut.reds, lut.greens, lut.blues, nColors);
			}
			return lut;
		} else {
			return null;
		}
	}

	static int grays(byte[] reds, byte[] greens, byte[] blues) {
		for (int i = 0; i < 256; i++) {
			reds[i] = (byte) i;
			greens[i] = (byte) i;
			blues[i] = (byte) i;
		}
		return 256;
	}

	int fire(byte[] reds, byte[] greens, byte[] blues) {
		int[] r = {0, 0, 1, 25, 49, 73, 98, 122, 146, 162, 173, 184, 195, 207, 217, 229, 240, 252, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255};
		int[] g = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 35, 57, 79, 101, 117, 133, 147, 161, 175, 190, 205, 219, 234, 248, 255, 255, 255, 255};
		int[] b = {0, 61, 96, 130, 165, 192, 220, 227, 210, 181, 151, 122, 93, 64, 35, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 98, 160, 223, 255};
		for (int i = 0; i < r.length; i++) {
			reds[i] = (byte) r[i];
			greens[i] = (byte) g[i];
			blues[i] = (byte) b[i];
		}
		return r.length;
	}

	int primaryColor(int color, byte[] reds, byte[] greens, byte[] blues) {
		for (int i = 0; i < 256; i++) {
			if ((color & 4) != 0) {
				reds[i] = (byte) i;
			}
			if ((color & 2) != 0) {
				greens[i] = (byte) i;
			}
			if ((color & 1) != 0) {
				blues[i] = (byte) i;
			}
		}
		return 256;
	}

	int ice(byte[] reds, byte[] greens, byte[] blues) {
		int[] r = {0, 0, 0, 0, 0, 0, 19, 29, 50, 48, 79, 112, 134, 158, 186, 201, 217, 229, 242, 250, 250, 250, 250, 251, 250, 250, 250, 250, 251, 251, 243, 230};
		int[] g = {156, 165, 176, 184, 190, 196, 193, 184, 171, 162, 146, 125, 107, 93, 81, 87, 92, 97, 95, 93, 93, 90, 85, 69, 64, 54, 47, 35, 19, 0, 4, 0};
		int[] b = {140, 147, 158, 166, 170, 176, 209, 220, 234, 225, 236, 246, 250, 251, 250, 250, 245, 230, 230, 222, 202, 180, 163, 142, 123, 114, 106, 94, 84, 64, 26, 27};
		for (int i = 0; i < r.length; i++) {
			reds[i] = (byte) r[i];
			greens[i] = (byte) g[i];
			blues[i] = (byte) b[i];
		}
		return r.length;
	}

	int spectrum(byte[] reds, byte[] greens, byte[] blues) {
		Color c;
		for (int i = 0; i < 256; i++) {
			c = Color.getHSBColor(i / 255f, 1f, 1f);
			reds[i] = (byte) c.getRed();
			greens[i] = (byte) c.getGreen();
			blues[i] = (byte) c.getBlue();
		}
		return 256;
	}

	int rgb332(byte[] reds, byte[] greens, byte[] blues) {
		Color c;
		for (int i = 0; i < 256; i++) {
			reds[i] = (byte) (i & 0xe0);
			greens[i] = (byte) ((i << 3) & 0xe0);
			blues[i] = (byte) ((i << 6) & 0xc0);
		}
		return 256;
	}

	int redGreen(byte[] reds, byte[] greens, byte[] blues) {
		for (int i = 0; i < 128; i++) {
			reds[i] = (byte) (i * 2);
			greens[i] = (byte) 0;
			blues[i] = (byte) 0;
		}
		for (int i = 128; i < 256; i++) {
			reds[i] = (byte) 0;
			greens[i] = (byte) (i * 2);
			blues[i] = (byte) 0;
		}
		return 256;
	}

	// if (nColors<256)
	//	interpolate(lut.reds, lut.greens, lut.blues, nColors);
	void interpolate(byte[] reds, byte[] greens, byte[] blues, int nColors) {
		byte[] r = new byte[nColors];
		byte[] g = new byte[nColors];
		byte[] b = new byte[nColors];
		System.arraycopy(reds, 0, r, 0, nColors);
		System.arraycopy(greens, 0, g, 0, nColors);
		System.arraycopy(blues, 0, b, 0, nColors);
		double scale = nColors / 256.0;
		int i1, i2;
		double fraction;
		for (int i = 0; i < 256; i++) {
			i1 = (int) (i * scale);
			i2 = i1 + 1;
			if (i2 == nColors) {
				i2 = nColors - 1;
			}
			fraction = i * scale - i1;
			//IJ.write(i+" "+i1+" "+i2+" "+fraction);
			reds[i] = (byte) ((1.0 - fraction) * (r[i1] & 255) + fraction * (r[i2] & 255));
			greens[i] = (byte) ((1.0 - fraction) * (g[i1] & 255) + fraction * (g[i2] & 255));
			blues[i] = (byte) ((1.0 - fraction) * (b[i1] & 255) + fraction * (b[i2] & 255));
		}
	}

	static int width = 256;
	static int height = 12;

	public static void main(String[] args) {
		JFrame fr = new JFrame();
		fr.setTitle("LUTs");
		fr.getContentPane().setLayout(new BoxLayout(fr.getContentPane(), BoxLayout.Y_AXIS));
		fr.addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}

		});

		LutBuilder lutBldr = LutBuilder.getInstance();
		String[] names = LutBuilder.lutNames;
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			Lut lut = lutBldr.createLUT(name);
			JComponent panel = createLutStripePanel(lut);
			panel.setPreferredSize(new Dimension(width, height));
			fr.add(panel);

		}
		fr.pack();
		//fr.setSize(wid, ht);
		fr.setVisible(true);
	}

	public static JComponent createLutStripePanel(Lut lut) {
		JPanel panel = new JPanel();

		BufferedImage bi = GraphicsUtilities.createCompatibleImage(width, height);
		// bi.getData().getDataBuffer().
		
		return new ImagePanel(bi);
	}

	static class ImagePanel extends JComponent {

		protected BufferedImage image;

		public ImagePanel() {
		}

		public ImagePanel(BufferedImage img) {
			image = img;
		}

		public void setImage(BufferedImage img) {
			image = img;
		}

		public void paint(Graphics g) {
			Rectangle rect = this.getBounds();
			if (image != null) {
				g.drawImage(image, 0, 0, rect.width, rect.height, null);
			}
		}

	}
}
