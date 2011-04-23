package imagej.display.view;

import imagej.data.Dataset;
import imagej.display.ImageCanvas;
import imagej.display.lut.Lut;
import java.util.ArrayList;
import net.imglib2.converter.Converter;
import net.imglib2.display.ARGBScreenImage;
import net.imglib2.display.RealARGBConverter;
import net.imglib2.display.XYProjector;
import net.imglib2.img.ImgPlus;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;

/**
 *
 * Composite color image with arbitrary number of channels, each with a Lut
 * A View into a a Dataset
 * 
 * @author GBH
 */
public class DatasetView {

	private Dataset dataset;
	private ARGBScreenImage screenImage;
	ArrayList<Converter<? extends RealType<?>, ARGBType>> converters = 
		new ArrayList<Converter<? extends RealType<?>, ARGBType>>();
	private XYProjector<? extends RealType<?>, ARGBType> projector;
	private int positionX;
	private int positionY;
	private final ImgPlus<? extends RealType<?>> img;
	private ImageCanvas imgCanvas;

	
	public 	DatasetView(String name, Dataset dataset, int channelDimIndex, ArrayList<Lut> luts) {

		this.dataset = dataset;

		this.img = dataset.getImage();

		screenImage = new ARGBScreenImage((int) img.dimension(0), (int) img.dimension(1));
		final int min = 0, max = 255;
		if (channelDimIndex < 0) {
			if (luts != null) {
				projector = new XYProjector(img, screenImage,
					new RealLUTConverter(min, max, luts.get(0)));
			} else {
				projector = new XYProjector(img, screenImage,
					new RealARGBConverter(min, max));
			}
		} else {
			for (int i = 0; i < luts.size(); i++) {
				Lut lut = luts.get(i);
				converters.add(new CompositeLUTConverter(min, max, lut));
			}
			projector = new CompositeXYProjector(img, screenImage, converters, channelDimIndex);
		}
		projector.map();
	}

	

	public void setImgCanvas(ImageCanvas imgCanvas) {
		this.imgCanvas = imgCanvas;
	}

	public int getPositionX() {
		return positionX;
	}

	public void setPositionX(int positionX) {
		this.positionX = positionX;
	}

	public int getPositionY() {
		return positionY;
	}

	public void setPositionY(int positionY) {
		this.positionY = positionY;
	}

	//
	public ImgPlus<? extends RealType<?>> getImg() {
		return img;
	}

	public ARGBScreenImage getScreenImage() {
		return screenImage;
	}

	public 	ArrayList<Converter<? extends RealType<?>, ARGBType>>  getConverters() {
		return converters;
	}

	public XYProjector getProjector() {
		return projector;
	}

	void project() {
	}

	void setPosition(int value, int dim) {
		projector.setPosition(value, dim);
		projector.map();
		// tell display components to repaint
		if(imgCanvas!=null)
			imgCanvas.updateImage();
		
		// Dataset emits a DatasetChangedEvent;
	}

}