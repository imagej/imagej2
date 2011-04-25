package imagej.display.view;

import imagej.data.Dataset;
import imagej.display.ImageCanvas;
import imagej.display.lut.ColorTable8;
import imagej.display.lut.ColorTables;
import java.util.ArrayList;
import net.imglib2.converter.Converter;
import net.imglib2.display.ARGBScreenImage;
import net.imglib2.display.XYProjector;
import net.imglib2.img.Axes;
import net.imglib2.img.ImgPlus;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;

/**
 * Builder of DatasetViews...
 * 
 * @author GBH
 */
public class DatasetViewBuilder2 {

//		public DatasetView2(final String name, final Dataset dataset,
//		final int channelDimIndex, final ArrayList<ColorTable8> luts, boolean composite) {
//
//		this.dataset = dataset;
//		this.img = dataset.getImage();
//		this.channelDimIndex = channelDimIndex;
//		this.luts = luts;
	
	public static DatasetView2 createView(final String name, final ImgPlus img, boolean composite) {
		return createView(name, img, (int) img.dimension(0), (int) img.dimension(1), composite);
		
	}
	public static DatasetView2 createView(final String name, final 
			ImgPlus imgIn, int scrnImgW, int scrnImgH, boolean composite) {
		
		Dataset dataset = null;
		ARGBScreenImage screenImage;
		ArrayList<Converter<? extends RealType<?>, ARGBType>> converters =
				new ArrayList<Converter<? extends RealType<?>, ARGBType>>();
		XYProjector<? extends RealType<?>, ARGBType> projector = null;
		int positionX;
		int positionY;
		ImgPlus<? extends RealType<?>> imgP;
		ImageCanvas imgCanvas;
		ArrayList<ColorTable8> luts;
		int channelDimIndex = -1;
		
		screenImage = new ARGBScreenImage(scrnImgW, scrnImgH);
		
		final int min = 0, max = 255;
		
//
//		if (channelDimIndex < 0) { // No channels
//			if (luts != null) { // single channel, use lut for display.
//				projector =
//					new XYProjector(img, screenImage, new RealLUTConverter(min, max,
//					luts.get(0)));
//			} else {
//				projector =
//					new XYProjector(img, screenImage, new RealARGBConverter(min, max));
//			}
//		} else {
//
//			if (composite) { // multichannel composite
//				for (int i = 0; i < luts.size(); i++) {
//					ColorTable8 lut = luts.get(i);
//					converters.add(new CompositeLUTConverter(min, max, lut));
//				}
//				projector = new CompositeXYProjector(img, screenImage, converters, channelDimIndex);
//			} else { // multichannel with sep. ColorTable8 for each
//				projector = new LutXYProjector(img, screenImage, new RealLUTConverter(min, max, luts.get(0)));
//			}
//		}
//		projector.map();
//	}
		
		return new DatasetView2( name, dataset, converters, projector, screenImage, channelDimIndex, composite);
	}

	/*
	 * // create an RGB 3-channel Compositeview
	 */

	public static DatasetView createCompositeRGBView(final String name, final ImgPlus img) {
		ArrayList<ColorTable8> lutList = new ArrayList<ColorTable8>();
		lutList.add(ColorTables.RED);
		lutList.add(ColorTables.GREEN);
		lutList.add(ColorTables.BLUE);
		int channelDimIndex = img.getAxisIndex(Axes.CHANNEL);
		if (channelDimIndex < 0) {
			System.err.println("No Channel dimension.");
		}
		int channels = (int) img.dimension(channelDimIndex);
		if (channels != 3) {
			System.err.println("Creating RBG composite, but not 3 channels");
		}
		Dataset dataset = new Dataset(img);
		final DatasetView view = new DatasetView(name, dataset, channelDimIndex, lutList, true);

		return view;
	}

	/*
	 * Grayscale view
	 */
	public static DatasetView createView(final String name, final ImgPlus img) {
		Dataset dataset = new Dataset(img);
		final DatasetView view = new DatasetView(name, dataset, -1, null, false);
		return view;
	}

	public static DatasetView createCompositeView(final String name, final ImgPlus img) {
		// create a multichannel Compositeview, up to 6 channels
		int channels = (int) img.dimension(img.getAxisIndex(Axes.CHANNEL));
		ArrayList<ColorTable8> lutList = defaultLutList(channels);
		int channelDimIndex = img.getAxisIndex(Axes.CHANNEL);
		if (channelDimIndex < 0) {
			//"No Channel dimension."
		}
		Dataset dataset = new Dataset(img);
		final DatasetView view = new DatasetView(name, dataset, channelDimIndex, lutList, true);
		return view;
	}

	public static DatasetView createMultichannelView(final String name, final ImgPlus img) {
		// create a multichannel Compositeview, up to 6 channels
		int channels = (int) img.dimension(img.getAxisIndex(Axes.CHANNEL));
		ArrayList<ColorTable8> lutList = defaultLutList(channels);
		int channelDimIndex = img.getAxisIndex(Axes.CHANNEL);
		if (channelDimIndex < 0) {
			//"No Channel dimension."
		}
		Dataset dataset = new Dataset(img);
		final DatasetView view = new DatasetView(name, dataset, channelDimIndex, lutList, false);
		return view;
	}

	private static ArrayList<ColorTable8> defaultLutList(int channels) {
		ArrayList<ColorTable8> lutList = new ArrayList<ColorTable8>();
		lutList.add(ColorTables.RED);
		if (channels > 1) {
			lutList.add(ColorTables.GREEN);
		}
		if (channels > 2) {
			lutList.add(ColorTables.BLUE);
		}
		if (channels > 3) {
			lutList.add(ColorTables.CYAN);
		}
		if (channels > 4) {
			lutList.add(ColorTables.MAGENTA);
		}
		if (channels > 5) {
			lutList.add(ColorTables.YELLOW);
		}
		return lutList;
	}

}
