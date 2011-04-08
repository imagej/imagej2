
package imagej.envisaje.roimanager;

// Lee's...

/* Represents the assignment of labels of type T to pixels
 * in an image.
 */
public interface Labeling<T>
{
  /* Return the number of dimensions in each coordinate tuple */
  public int getNumberOfDimensions();
  /* Initialize the labeling with the coordinates of pixels and
   * their labels.
   * @param coords: an array of D arrays of N coordinates each
   * where D is the dimensionality of the space and N is the
   * number of pixels that are labeled.
   * @param labels: an array of N labelings giving the label
   * assigned to eacn of the N coordinate tuples.
   */
  public void init(int [][] coords, T[] labels);
  /* a special case where the image dimensions don’t exceed
   * 0x7FFF.
   */
  public void init(short [][] coords, T[] labels);
  public void load(java.io.InputStream stream);
  public void save(java.io.OutputStream stream);
  /* get an iterator over the labels in the labeling */
  public Iterator<T> getLabels();
  /* Produce a cursor that iterates over one label’s pixels */
  public LocalizableCursor<Type<T>> getLocalizableLabelCursor(T label);
  /* Produce a cursor that iterates over all labeled pixels,
   * returning the label of each
   */
  public LocalizableCursor<Type<T>> getLocalizableLabelCursor();
  /* A cursor that can be used to interrogate whether a pixel
   * is or isn’t labeled. getLabel() either returns null (not
   * in label) or <i>label</i> (is in label).
   */
  public LocalizableByDimCursor<Type<T>> getLocalizableByDimLabelCursor(T label);
  /* A cursor that can be used to determine a pixel’s label.
   * An array is returned because a pixel might have more than
   * one label.
   */
  public LocalizableByDimCursor<Type<T[]>> getLocalizableByDimLabelCursor(T label);
  /* Produce a cursor that iterates the pixels of an image
   * for a given label using a LocalizableByDim cursor on that
   * image.
   */
  public LocalizableCursor<I extends Type<I>, C extends LocalizableByDimCursor<I>> getImageCursor(T label, C cursor);
  /* # of pixels with the given label */
  public int getPixelCount(T label);
  /* minimum extent of a given label */
  public int [] getMinimumExtent(T label);
  /* maximum extent of a given label */
  public int [] getMaximumExtent(T label);
  /* Centroid of given label */
  public int [] getCentroid(T label);
}
