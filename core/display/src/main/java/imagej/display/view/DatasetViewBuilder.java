package imagej.display.view;

import imagej.data.Dataset;
import imagej.display.lut.ColorTables;

import java.util.ArrayList;

import net.imglib2.display.ColorTable8;
import net.imglib2.img.Axes;
import net.imglib2.img.ImgPlus;

/**
 * Builder of DatasetViews...
 * 
 * @author GBH
 */
public class DatasetViewBuilder {

	/*
	 * Grayscale view - no ColorTable
	 */
	public static DatasetView createView(final String name, final ImgPlus img) {
		Dataset dataset = new Dataset(img);
		return createView(name, new Dataset(img));
	}

	public static DatasetView createView(final String name, final Dataset dataset) {
		final DatasetView view = new DatasetView(name, dataset, -1, null, false);
		return view;
	}
	
	/*
	 * // create an RGB 3-channel Compositeview
	 */
	public static DatasetView createCompositeRGBView(final String name, final ImgPlus img) {
		return createCompositeRGBView(name, new Dataset(img));
	}

	public static DatasetView createCompositeRGBView(final String name, final Dataset dataset) {
		ImgPlus img = dataset.getImgPlus();
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
		final DatasetView view = new DatasetView(name, dataset, channelDimIndex, lutList, true);
		return view;

	}

	// Composite
	//
	public static DatasetView createCompositeView(final String name, final ImgPlus img) {
		Dataset dataset = new Dataset(img);
		return createCompositeView(name, new Dataset(img));
	}

	public static DatasetView createCompositeView(final String name, final Dataset dataset) {
		ImgPlus img = dataset.getImgPlus();
		// create a multichannel Compositeview, up to 6 channels
		int channels = (int) img.dimension(img.getAxisIndex(Axes.CHANNEL));
		ArrayList<ColorTable8> lutList = defaultLutList(channels);
		int channelDimIndex = img.getAxisIndex(Axes.CHANNEL);
		if (channelDimIndex < 0) {
			//"No Channel dimension."
		}
		final DatasetView view = new DatasetView(name, dataset, channelDimIndex, lutList, true);
		return view;
	}

	// Multichannel, not composite
	//
	public static DatasetView createMultichannelView(final String name, final ImgPlus img) {
		Dataset dataset = new Dataset(img);
		return createMultichannelView(name, new Dataset(img));
	}

	public static DatasetView createMultichannelView(final String name, final Dataset dataset) {
		// create a multichannel Compositeview, up to 6 channels
		ImgPlus img = dataset.getImgPlus();
		int channels = (int) img.dimension(img.getAxisIndex(Axes.CHANNEL));
		ArrayList<ColorTable8> lutList = defaultLutList(channels);
		int channelDimIndex = img.getAxisIndex(Axes.CHANNEL);
		if (channelDimIndex < 0) {
			//"No Channel dimension."
		}
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
