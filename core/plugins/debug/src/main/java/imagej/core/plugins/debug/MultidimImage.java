package imagej.core.plugins.debug;

import java.util.Arrays;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import imagej.ImageJ;
import imagej.data.ChannelCollection;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.data.DrawingTool;
import imagej.data.DrawingTool.TextJustification;
import imagej.ext.module.ItemIO;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;


@Plugin(menuPath = "Plugins>Sandbox>Multidimensional Test Image")
public class MultidimImage implements ImageJPlugin {

	@Parameter
	ImageJ context;
	
	@Parameter
	DatasetService service;
	

	@Parameter(type=ItemIO.OUTPUT)
	Dataset ds;
	
	@Override
	public void run() {
		long[] dims = new long[]{80,30,4,5,6,7};
		String name = "Multidimensional Example";
		AxisType[] axes = new AxisType[]{Axes.X, Axes.Y, Axes.CHANNEL, Axes.FREQUENCY, Axes.Z, Axes.TIME};
		int bitsPerPixel = 8;
		boolean signed = false;
		boolean floating = false;
		ds = service.create(dims, name, axes, bitsPerPixel, signed, floating);
		long[] pos = new long[dims.length];
		DrawingTool tool = new DrawingTool(ds);
		ChannelCollection channels = new ChannelCollection(Arrays.asList(new Double[]{255.0,255.0,255.0,255.0}));
		tool.setChannels(channels);
		for (int c = 0; c < dims[2]; c++) {
			for (int f = 0; f < dims[3]; f++) {
				for (int z = 0; z < dims[4]; z++) {
					for (int t = 0; t < dims[5]; t++) {
						pos[2] = c;
						pos[3] = f;
						pos[4] = z;
						pos[5] = t;
						tool.setPosition(pos);
						tool.setPreferredChannel(c);
						String label = "c " + c + " f " + f + " z " + z + " t " + t;
						tool.drawText(0, 0, label, TextJustification.LEFT);
					}
				}
			}
		}
	}

}
