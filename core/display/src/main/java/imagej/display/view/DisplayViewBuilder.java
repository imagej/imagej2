package imagej.display.view;

import imagej.data.Dataset;
import imagej.display.lut.Lut;
import imagej.display.lut.LutBuilder;
import java.util.ArrayList;
import net.imglib2.img.Axes;
import net.imglib2.img.ImgPlus;

/**
 *
 * @author GBH
 */
public class DisplayViewBuilder {

	public static DatasetView createCompositeRGBView(final String name,
		final ImgPlus img) {
		// create an RGB 3-channel Compositeview, with channel on axis 2
		ArrayList<Lut> lutList = new ArrayList<Lut>();
		lutList.add(LutBuilder.getInstance().createLUT("red"));
		lutList.add(LutBuilder.getInstance().createLUT("green"));
		lutList.add(LutBuilder.getInstance().createLUT("blue"));
		int channelDimIndex = img.getAxisIndex(Axes.CHANNEL);
		if (channelDimIndex < 0) {
			//"No Channel dimension."
		}
		Dataset dataset = new Dataset(img);
		final DatasetView view = new DatasetView(name, dataset, channelDimIndex, lutList);
		
		return view;
	}

	public static DatasetView createView(final String name,
		final ImgPlus img) {
		Dataset dataset = new Dataset(img);
		final DatasetView view = new DatasetView(name, dataset, -1, null);
		return view;
	}



}
