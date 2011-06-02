package imagej.core.plugins.roi;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.ImgPlus;
import net.imglib2.img.NativeImg;
import net.imglib2.img.NativeImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.basictypeaccess.BitAccess;
import net.imglib2.roi.BinaryMaskRegionOfInterest;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import imagej.data.Dataset;
import imagej.data.roi.BinaryMaskOverlay;
import imagej.data.roi.Overlay;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.util.ColorRGB;

@Plugin(menu = { @Menu(label = "Image", mnemonic = 'i'),
		@Menu(label = "To binary mask", mnemonic = 'b') })

public class ImageToBinaryMask implements ImageJPlugin {
	@Parameter(
			label="Threshold",
			description = "The threshold that separates background (mask) from foreground (region of interest).")
	double threshold;
	@Parameter(
			label = "Input image",
			description = "The image to be converted to a binary mask.")
	private Dataset input;
	
	@Parameter(
			label = "Output mask",
			description = "The overlay that is the result of the operation",
			output = true)
	private Overlay output;
	@Parameter(label = "Overlay color",
			description = "The color used to display the overlay")
	private ColorRGB color = new ColorRGB(255,0,0);

	@Parameter(label = "Overlay alpha", description = "The transparency (transparent = 0) or opacity (opaque=255) of the overlay",min="0", max="255")
	private int alpha = 128;
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		ImgPlus<? extends RealType<?>> imgplus = input.getImgPlus();
		Img<? extends RealType<?>> img = imgplus.getImg();
		long [] dimensions = new long[img.numDimensions()];
		long [] position = new long[img.numDimensions()];
		img.dimensions(dimensions);
		NativeImg<BitType,BitAccess> mask = new ArrayImgFactory<BitType>().createBitInstance(dimensions, 1);
		BitType t = new BitType(mask);
		mask.setLinkedType(t);
		RandomAccess<BitType> raMask = mask.randomAccess();
		Cursor<? extends RealType<?>> c = img.localizingCursor();
		while (c.hasNext()) {
			c.next();
			c.localize(position);
			raMask.setPosition(position);
			raMask.get().set(c.get().getRealDouble() >= threshold);
		}
		output = new BinaryMaskOverlay(new BinaryMaskRegionOfInterest<BitType, Img<BitType>>(mask));
		output.setAlpha(alpha);
		output.setFillColor(color);
	}
}
