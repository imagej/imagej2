import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import java.util.Random;
import java.awt.Rectangle;

/**
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * <p>
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * <p>
 * This plugin adds Poisson distributed noise to each pixel of an image. This
 * plugin uses a simple way to generate random Poisson-distributed numbers given
 * by Knuth (http://en.wikipedia.org/wiki/Donald_Knuth).
 * <p>
 * Poisson noise or (particularly in electronics) as shot noise is a type of
 * electronic noise that occurs when the finite number of particles that carry
 * energy, such as electrons in an electronic circuit or photons in an optical
 * device, is small enough to give rise to detectable statistical fluctuations
 * in a measurement. It is important in electronics, telecommunications, and
 * fundamental physics.
 * 
 * @author Ignazio Gallo(ignazio.gallo@gmail.com, http://www.dicom.uninsubria.it/~ignazio.gallo/)
 * @since 18/nov/2008
 * 
 * @version 1.0
 */
public class Poisson_Noise implements PlugInFilter {
	static double noiseMean = 2;
	ImagePlus imp;
	Random random = new Random();

	public int setup(String arg, ImagePlus imp) {
		if (imp==null) {
			IJ.noImage();
			return DONE;
		}
		this.imp = imp;
		if (!showDialog())
			return DONE;
		return IJ.setupDialog(imp, DOES_ALL|SUPPORTS_MASKING);
	}

	public void run(ImageProcessor ip) {
		FloatProcessor fp = null;
		for (int i=0; i<ip.getNChannels(); i++) { //grayscale: once. RBG: once per color, i.e., 3 times
			fp = ip.toFloat(i, fp); // convert image or color channel to float
			fp = ip.toFloat(i, fp);
			addNoise(fp);
			ip.setPixels(i, fp); // convert back from float
		}
	}

	public void addNoise(ImageProcessor ip) {
 		int width = ip.getWidth(); // width of the image
		int height = ip.getHeight(); // height of the image
		//double max = ip.getMax();
		float[] pixels = (float[]) ip.getPixels();
		int progress = Math.max(height / 25, 1);
		Rectangle r = ip.getRoi();

		for (int y=r.y; y<(r.y+r.height); y++) {
			if (y % progress==0) IJ.showProgress(y, height);
			for (int x=r.x; x<(r.x+r.width); x++) {
				// Creates additive poisson noise
				double newVal = (pixels[y * width + x]) + poissonValue();
				newVal = Math.max(newVal, 0);
				//if (newVal <= max)
				pixels[x + y * width] = (float)newVal;
			}
		}
		IJ.showProgress(1.0);
		ip.resetMinAndMax();
	}

	/**
	 * Algorithm poisson random number (Knuth). While simple, the complexity is
	 * linear in λ (the mean).
	 * 
	 * @return a random Poisson-distributed number.
	 */
	private int poissonValue() {
		// init:
		double L = Math.exp(-noiseMean);
		int k = 0;
		double p = 1;
		do {
			k++;
			// Generate uniform random number u in [0,1] and let p ← p × u.
			p *= random.nextDouble();
		} while (p >= L);
		return k - 1;
	}

	private boolean showDialog() {
		GenericDialog gd = new GenericDialog(
				"Additive Poisson Noise Parameters");
		gd.addNumericField("Noise Mean (>0):", noiseMean, 2);
		gd.showDialog();
		if (gd.wasCanceled())
			return false;
		noiseMean = Math.max(gd.getNextNumber(), 0.01);
		return true;
	}

}
