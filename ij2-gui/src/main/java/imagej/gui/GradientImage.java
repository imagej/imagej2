package imagej.gui;

import imagej.data.Types;
import imagej.dataset.Dataset;
import imagej.dataset.PlanarDatasetFactory;
import imagej.plugin.ij2.IPlugin;
import imagej.plugin.ij2.Parameter;
import imagej.plugin.ij2.Plugin;

@Plugin(
  menuPath="Process>Gradient"
)
public class GradientImage implements IPlugin {

	@Parameter private int width = 512;
	@Parameter	private int height = 512;

	@Parameter(output=true)
	private Dataset dataset;

	@Override
	public void run() {
		
		byte[] data = new byte[ this.width * this.height ];
		
		for(int x = 0; x < width; x++)
			for(int y = 0; y < height; y++)
			{
				int index = y*width+x;
				data[index] = (byte) (x+y);
			}
		
		PlanarDatasetFactory planarDatasetFactory = new PlanarDatasetFactory();
		dataset = planarDatasetFactory.createDataset( Types.findType( "8=bit unsigned"), new int[] { this.width, this.height } );
		dataset.setData( data );
  }
}
