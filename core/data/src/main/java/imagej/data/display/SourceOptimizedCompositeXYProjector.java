/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2013 Stephan Preibisch, Tobias Pietzsch, Barry DeZonia,
 * Stephan Saalfeld, Albert Cardona, Curtis Rueden, Christian Dietz, Jean-Yves
 * Tinevez, Johannes Schindelin, Lee Kamentsky, Larry Lindsey, Grant Harris,
 * Mark Hiner, Aivar Grislis, Martin Horn, Nick Perry, Michael Zinsmaier,
 * Steffen Jaensch, Jan Funke, Mark Longair, and Dimiter Prodanov.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.data.display;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.display.CompositeXYProjector;
import net.imglib2.img.cell.CellImg;
import net.imglib2.type.numeric.ARGBType;

/**
 * As {@link CompositeXYProjector} but uses a source that is both a
 * {@link RandomAccessibleInterval} and an {@link IterableInterval}, and a
 * {@link RandomAccessibleInterval} target. This allows the natural iteration
 * order of the source to be used, instead of that of the target.
 * <p>
 * Furthermore, one source cursor is created for each component being
 * composited. This optimizes for scenarios where each composited component is
 * not stored conveniently relative to each other (for example - in the case of
 * {@link CellImg} instances where each channel is in a different cell).
 * </p>
 * <p>
 * NB: because N cursors are created, one for each composite component, the
 * memory requirements for this projector are higher than
 * {@link CompositeXYProjector}.
 * </p>
 * <p>
 * NB: Because this is an XY projector, the assumption is the provided source's
 * natural iteration order will cover XY first. If that assumption is false, the
 * projector will fail to produce a correct image.
 * </p>
 * 
 * @see CompositeXYProjector for the code upon which this class was based.
 * 
 * @author Mark Hiner <hinerm@gmail.com>
 * @author Stephan Saalfeld
 * @author Curtis Rueden
 * @author Grant Harris
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public class SourceOptimizedCompositeXYProjector< A, S extends RandomAccessibleInterval< A > & IterableInterval< A >, T extends RandomAccessibleInterval< ARGBType > & IterableInterval< ARGBType > > extends CompositeXYProjector< A >
{
  private int dimIndex = 0;
  
	private T raTarget = null;

	private S iraSource = null;

	public SourceOptimizedCompositeXYProjector( S source, T target, ArrayList< Converter< A, ARGBType >> converters, int dimIndex )
	{
		super( source, target, converters, dimIndex );
		this.dimIndex = dimIndex;
		raTarget = target;
		iraSource = source;
	}

	@Override
	public void map()
	{
		boolean noComposite = dimIndex < 0;

		int size = -1;

		if ( !noComposite )
			size = updateCurrentArrays();

		if ( noComposite || size == 1 )
		{
			super.map();
			return;
		}

		// System.out.println("    CompositeXYProjector::map() : call #"+(++calls));
		for ( int d = 2; d < position.length; ++d )
			min[ d ] = max[ d ] = position[ d ];

		min[ 0 ] = raTarget.min( 0 );
		min[ 1 ] = raTarget.min( 1 );
		max[ 0 ] = raTarget.max( 0 );
		max[ 1 ] = raTarget.max( 1 );

		final List< RandomAccess< A >> sourceRandomAccess = new ArrayList< RandomAccess< A >>();

		Cursor< A > leadCursor = iraSource.localizingCursor();

		// Compute how many positions need to be composited in the target
		int steps = 1;
		for ( int i = 0; i < min.length; i++ )
			steps *= ( max[ i ] + 1 - min[ i ] );

		// Generate a RandomAccess for each composite component
		for ( int i = 0; i < size; ++i )
		{
			RandomAccess< A > randomAccess = iraSource.randomAccess();
			min[ dimIndex ] = max[ dimIndex ] = currentPositions[ i ];
			randomAccess.setPosition( min );
			sourceRandomAccess.add( randomAccess );
		}

		// A random access for populating the target
		final RandomAccess< ARGBType > targetCursor = raTarget.randomAccess();

		final ARGBType bi = new ARGBType();

		// Now we will read the composite component values at the first #steps
		// x,y
		// positions.
		while ( leadCursor.hasNext() && ( --steps ) >= 0 )
		{
			int aSum = 0, rSum = 0, gSum = 0, bSum = 0;

			leadCursor.fwd();

			for ( int i = 0; i < size; i++ )
			{
				RandomAccess< A > randomAccess = sourceRandomAccess.get( i );
				randomAccess.setPosition( leadCursor.getLongPosition( 0 ), 0 );
				randomAccess.setPosition( leadCursor.getLongPosition( 1 ), 1 );
				randomAccess.setPosition( currentPositions[ i ], dimIndex );

				currentConverters[ i ].convert( randomAccess.get(), bi );

				// accumulate converted result
				final int value = bi.get();
				final int a = ARGBType.alpha( value );
				final int r = ARGBType.red( value );
				final int g = ARGBType.green( value );
				final int b = ARGBType.blue( value );
				aSum += a;
				rSum += r;
				gSum += g;
				bSum += b;
			}
			if ( aSum > 255 )
				aSum = 255;
			if ( rSum > 255 )
				rSum = 255;
			if ( gSum > 255 )
				gSum = 255;
			if ( bSum > 255 )
				bSum = 255;

			targetCursor.setPosition( leadCursor.getLongPosition( 0 ), 0 );
			targetCursor.setPosition( leadCursor.getLongPosition( 1 ), 1 );
			targetCursor.get().set( ARGBType.rgba( rSum, gSum, bSum, aSum ) );
		}

	}

}
