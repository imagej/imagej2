package imagej.plugin.display;

import imagej.model.Dataset;

/**
 *
 * @author GBH
 */
public interface DisplayView {
	
/*
 * DisplayView
 * Contains:
  - Dataset
  - Projector
  - Converter
  - ARGBScreenImage
 * 
 (But for now with ImgLib1,Projector/Converter/ARGBScreenImage must stay commented out)
*/

	Dataset getDataSet();



}
