package imagej;

import mpicbg.imglib.container.array.Array;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

interface PlanarNumericDataset
{
  RealType<?> getType();
  int[] getDimensions();
  int getChannelsPerSample();
  long getTotalSamples();
  long getPlaneCount(int axisNumber);
  double getSample(int[] index, Array<?,?> dest);  // TODO change dest to correct thing when we know
  void  getSamples(int[] index, int axisNumber, int numSamples, Array<?,?> dest);
  void addAxis();
  void insertAxis(int axisNumber);
  void deleteAxis(int axisNumber, int planeToKeep);
  void addPlanes(int axis, Object planes);  // TODO define type so its not an Object
  void insertPlanes(int axis, int insertionSpot, Object planes);  // TODO define type so its not an Object
  void deletePlanes(int axis, int deletionSpot, int planeCount);
  void doOperation(Object operation);  // TODO - change from Object to Operation once its defined
  void setAxisOfInterest(int axisNumber);  // hints at how to organize storage for next set of operations
  Image<?> createPlane(int axisNumber);  // TODO - change from Image<?> to something else if necessary
  void setPlane(int axisNumber, int planeNumber, Object plane);  // TODO - make plane something other than Object
}
