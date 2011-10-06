package imagej.core.plugins.display;

import java.util.Iterator;
import java.util.List;

import net.imglib2.display.RealLUTConverter;
import net.imglib2.type.numeric.RealType;
import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.display.DatasetView;
import imagej.data.display.ImageDisplayService;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Plugin;

/**
 * Plugin that auto-thresholds each channel.
 * 
 * @author Adam Fraser
 */

@Plugin(menu = {
		@Menu(label = "Image"),
		@Menu(label = "Adjust"),
		@Menu(label = "Auto-Contrast", accelerator = "control shift alt L",
			weight = 0) })
public class AutoContrast implements ImageJPlugin {

	static final int AUTO_THRESHOLD = 5000;
	static int autoThreshold;
	
	private DatasetView view = ImageJ.get(ImageDisplayService.class)
		.getActiveDatasetView();

	@Override
	public void run() {
		if (view == null) return;		
		Dataset dataset = view.getData();
		
		Iterator itr = dataset.getImgPlus().getImg().cursor();
		int[] histogram = new int[256];
		int pixelCount = 0;
		while (itr.hasNext()) {
			String v = itr.next().toString();
			// TODO: v needs to be scaled to 0-255 for non 8-bit images
			int val = Integer.valueOf(v);
			histogram[val] += 1;
			pixelCount += 1;
		}
		
		if (autoThreshold<10)
			autoThreshold = AUTO_THRESHOLD;
		else
			autoThreshold /= 2;
		int threshold = pixelCount / autoThreshold;
		int limit = pixelCount / 10;
		int i = -1;
		boolean found = false;
		int count;
		do {
			i++;
			count = histogram[i];
			if (count>limit) count = 0;
			found = count> threshold;
		} while (!found && i<255);
		int hmin = i;
		i = 256;
		do {
			i--;
			count = histogram[i];
			if (count>limit) count = 0;
			found = count > threshold;
		} while (!found && i>0);
		int hmax = i;
		double min;
		double max;
		System.out.println(hmin + "," + hmax);
		if (hmax>=hmin) {
			// XXX
			// Was: min = stats.histMin + hmin * stats.binSize;
			//      max = stats.histMin + hmax * stats.binSize;
			double histMin = 0;
			min = histMin + hmin * 1.0; 
			max = histMin + hmax * 1.0;
			
			// XXX
//			if (RGBImage && roi!=null) 
//				imp.setRoi(roi);
		} else {
			// TODO: respect other image types
			min = 0;
			max = 255;
		}
		
		final List<RealLUTConverter<? extends RealType<?>>> converters =
			view.getConverters();
		for (final RealLUTConverter<? extends RealType<?>> conv : converters) {
			conv.setMin(min);
			conv.setMax(max);
		}
		view.getProjector().map();
		view.update();
		
	}

}
