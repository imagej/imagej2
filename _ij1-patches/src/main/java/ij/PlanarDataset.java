package ij;

import ij.process.ImageProcessor;

public interface PlanarDataset
{
	
// for this interface any planeNumber index ranges on [0..numPlanesPresent-1]
//   TODO - note that growing and shrinking datasets could violate internals of ImgLibProcessors (planePos array) returned by getProcessor
	
	int getPlaneWidth();
	int getPlaneHeight();
	int getPlaneCount();
	Object getPrimitiveArray(int planeNumber);
	void setPrimitiveArray(int planeNumber, Object planeDataReference);
	String getPlaneLabel(int planeNumber);
	void setPlaneLabel(int planeNumber, String label);
	void insertPlaneAt(int planeNumber, Object plane);  // grow the dataset. put new plane at planeNumber index moving other planes as needed
	void deletePlaneAt(int planeNumber);                // shrink the dataset. delete plane at planeNumber index moving other planes as needed
	ImageProcessor getProcessor(int planeNumber);
}
