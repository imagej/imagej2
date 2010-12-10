package imagej.imglib.process.operation;

import imagej.Dimensions;
import imagej.process.Index;
import imagej.process.Observer;
import imagej.selection.SelectionFunction;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

/**
 * PositionalManyCursorRoiOperation is the prototypical operation that can be done between multiple Images positionally.
 * It is the base class for any operation that manipulates values of Images while referencing position synchronized Images.
 * Any and all input Images can be modified as desired. In addition, the implementor of this abstract interface can create
 * any reference data it needs to decide how to transform values in the Images if desired.
 * 
 * This operation works on user defined N dimensional regions of the input Images. It can be constrained to only
 * apply to sample values that are filtered via user specified SelectionFunctions. It will update an Observer
 * as the iteration takes place if one is attached to this operation.
 *
 * Implementors of the abstract interface define beforeIteration(), insideIteration(), and afterIteration()
 * methods.
 */

public abstract class PositionalManyCursorRoiOperation<T extends RealType<T>>
{
	/** the Images to operate upon */
	private Image<T>[] images;
	
	/** the origins of the N dimensional regions within the Images that the operation will apply to */
	private int[][] origins;

	/** the spans of the N dimensional regions within the Images that the operation will apply to */
	private int[][] spans;
	
	/** an Observer that is interested in our progress through the iteration */
	private Observer observer;

	/** SelectionFunctions that filter which of the samples are of interest */
	private SelectionFunction[] selectors;
	
	/** constructor that takes many Image and region definitions */
	protected PositionalManyCursorRoiOperation(Image<T>[] images, int[][] origins, int[][] spans)
	{
		if ((images.length != origins.length) || (origins.length != spans.length))
			throw new IllegalArgumentException("PositionalManyCursorRoiOperation(): lengths of all input parameters do not match");
		
		this.images = images;
		this.origins = origins.clone();
		this.spans = spans.clone();
		
		this.observer = null;
		this.selectors = new SelectionFunction[images.length];
		
		for (int i = 0; i < this.images.length; i++)
			Dimensions.verifyDimensions(this.images[i].getDimensions(), this.origins[i], this.spans[i]);
		
		for (int i = 1; i < this.spans.length; i++)
			if (Dimensions.getTotalSamples(spans[0]) != Dimensions.getTotalSamples(spans[i]))
				throw new IllegalArgumentException("PositionalManyCursorRoiOperation(): span sizes differ");
	}
	
	public Image<T>[] getImages() { return images; }
	public int[][] getOrigins() { return origins; }
	public int[][] getSpans() { return spans; }

	/** allows (one) Observer to watch the iteration as it takes place. The Observer is updated every time a
	 * sample is loaded (and not just when insideIteration() is invoked).
	 * */
	public void addObserver(Observer o) { this.observer = o; }

	/** allows user to specify which subset of samples will be passed on to insideIteration(). Note that it is
	 * more performant to pass null as a selection function rather than one that accepts all samples. */
	public void setSelectionFunctions(SelectionFunction[] funcs)
	{
		this.selectors = funcs;
		if (funcs != null)
			if (funcs.length != images.length)
				throw new IllegalArgumentException("incorrect number of selectors provided");
	}
	
	/** abstract - implemented by subclass */
	protected abstract void beforeIteration(RealType<T> type);

	/** abstract - implemented by subclass */
	protected abstract void insideIteration(int[][] positions, RealType<T>[] samples);

	/** abstract - implemented by subclass */
	protected abstract void afterIteration();

	/** private helper - determine if current samples are of interest */
	private boolean selected(int[][] positions, RealType<T>[] samples)
	{
		if (this.selectors == null)
			return true;
		
		for (int i = 0; i < samples.length; i++)
			if ((this.selectors[i] != null) && 
					!(this.selectors[i].include(positions[i], samples[i].getRealDouble())))
				return false;
		
		return true;
	}
	
	/** private helper - read the current sample values from the cursors */
	private void collectSamples(LocalizableByDimCursor<T>[] cursors, RealType<T>[] samples)
	{
		for (int i = 0; i < cursors.length; i++)
			samples[i] = cursors[i].getType();
	}

	/** private helper - set each cursor to its given position */
	private void setCursorPositions(LocalizableByDimCursor<T>[] cursors, int[][] positions)
	{
		for (int i = 0; i < cursors.length; i++)
			cursors[i].setPosition(positions[i]);
	}
	/** private helper - return true if every position is valid */
	private boolean areValid(int[][] positions)
	{
		for (int i = 0; i < positions.length; i++)
			if (!Index.isValid(positions[i], origins[i], spans[i]))
				return false;
				
		return true;
	}
	
	/** private helper - move all cursors forward one sample */
	private void increment(int[][] positions)
	{
		for (int i = 0; i < positions.length; i++)
			Index.increment(positions[i], origins[i], spans[i]);
	}
	
	/** private helper - close all cursors */
	private void close(Cursor<T>[] cursors)
	{
		for (int i = 0; i < cursors.length; i++)
			cursors[i].close();
	}
	
	/** private helper - copy current positions */
	private void updatePositionCopies(int[][] positions, int[][] copies)
	{
		int numPositions = positions.length;
		
		int indexColumns = positions[0].length;
		
		for (int index = 0; index < numPositions; index++)
			for (int column = 0; column < indexColumns; column++)
				copies[index][column] = positions[index][column];
	}
	
	@SuppressWarnings("unchecked")
	/** runs the operation. does the iteration and calls subclass methods as appropriate */
	public void execute()
	{
		if (this.observer != null)
			observer.init();

		// create cursors
		LocalizableByDimCursor<T>[] cursors = new LocalizableByDimCursor[images.length];
		for (int i = 0; i < images.length; i++)
			cursors[i] = images[i].createLocalizableByDimCursor();

		// create position indices
		int[][] positions = new int[images.length][];
		for (int i = 0; i < images.length; i++)
			positions[i] = origins[i].clone();
		
		// create position index copies
		int[][] copyOfPositions = new int[images.length][];
		for (int i = 0; i < images.length; i++)
			copyOfPositions[i] = origins[i].clone();
		
		// gather type info to pass along
		RealType<T>[] samples = new RealType[images.length];

		// do the iteration

		beforeIteration(cursors[0].getType());  // pass along type info

		while (areValid(positions))
		{
			setCursorPositions(cursors, positions);
			
			collectSamples(cursors, samples);
		
			updatePositionCopies(positions, copyOfPositions);

			if (selected(copyOfPositions, samples))
				insideIteration(copyOfPositions, samples);

			increment(positions);
			
			if (this.observer != null)
				observer.update();
		}
		
		afterIteration();

		// close the cursors
		
		close(cursors);
		
		if (this.observer != null)
			observer.done();
	}
}
