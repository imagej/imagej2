package imagej.ui.dnd;

import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.data.display.DatasetView;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.display.Display;
import imagej.display.DisplayService;
import net.imglib2.RandomAccess;
import net.imglib2.display.ColorTable;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.scijava.plugin.Plugin;

@Plugin(type = DragAndDropHandler.class)
public class LutDragAndDropHandler extends AbstractDragAndDropHandler {

	private static final int WIDTH = 256;
	private static final int HEIGHT = 32;

	public static final String MIME_TYPE =
		"application/imagej-lut; class=java.util.String; charset=Unicode";

	@Override
	public boolean isCompatible(Display<?> display, DragAndDropData data) {
		if ((display != null) && !(display instanceof ImageDisplay)) return false;
		for (final String mimeType : data.getMimeTypes()) {
			if (MIME_TYPE.equals(mimeType)) return true;
		}
		return false;
	}

	@Override
	public boolean drop(Display<?> display, DragAndDropData data) {
		ColorTable colorTable = (ColorTable) data.getData(MIME_TYPE);
		if (display == null) {
			DatasetService dsSrv = getContext().getService(DatasetService.class);
			Dataset dataset =
				dsSrv.create(new UnsignedByteType(), new long[] { WIDTH, HEIGHT },
					"Lookup table", new AxisType[] { Axes.X, Axes.Y });
			rampFill(dataset);
			// TODO - is this papering over a bug in the dataset/imgplus code?
			if (dataset.getColorTableCount() == 0) dataset.initializeColorTables(1);
			dataset.setColorTable(colorTable, 0);
			DisplayService dispSrv = getContext().getService(DisplayService.class);
			dispSrv.createDisplay(dataset);
			return true;
		}
		if (!(display instanceof ImageDisplay)) return false;
		ImageDisplayService imgSrv =
			getContext().getService(ImageDisplayService.class);
		DatasetView view = imgSrv.getActiveDatasetView((ImageDisplay) display);
		if (view == null) return false;
		int channel = view.getIntPosition(Axes.CHANNEL);
		view.setColorTable(colorTable, channel);
		return true;
	}

	private void rampFill(Dataset dataset) {
		RandomAccess<? extends RealType<?>> accessor =
			dataset.getImgPlus().randomAccess();
		for (int x = 0; x < WIDTH; x++) {
			accessor.setPosition(x, 0);
			for (int y = 0; y < HEIGHT; y++) {
				accessor.setPosition(y, 1);
				accessor.get().setReal(x);
			}
		}
	}
}
