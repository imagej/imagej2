/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
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
 * #L%
 */

package net.imagej;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.event.DataViewUpdatedEvent;
import net.imagej.ui.viewer.image.AbstractImageDisplayViewer;
import net.imglib2.AnnotatedSpace;
import net.imglib2.RandomAccessible;
import net.imglib2.display.LinearRange;
import net.imglib2.display.RealARGBColorConverter;
import net.imglib2.meta.ImgPlus;
import net.imglib2.meta.axis.LinearAxis;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.ui.AffineTransformType2D;
import net.imglib2.ui.AffineTransformType3D;
import net.imglib2.ui.InteractiveDisplayCanvasComponent;
import net.imglib2.ui.RenderSource;
import net.imglib2.ui.TransformEventHandler2D;
import net.imglib2.ui.TransformEventHandler3D;
import net.imglib2.ui.overlay.BoxOverlayRenderer;
import net.imglib2.ui.util.Defaults;
import net.imglib2.ui.util.InterpolatingSource;
import net.imglib2.view.Views;

import org.scijava.Priority;
import org.scijava.display.Display;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.options.event.OptionsEvent;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UserInterface;
import org.scijava.ui.awt.AWTInputEventDispatcher;
import org.scijava.ui.swing.SwingUI;
import org.scijava.ui.viewer.DisplayViewer;
import org.scijava.ui.viewer.DisplayWindow;

/**
 * A Swing image display viewer, which displays 2D planes in grayscale or
 * composite color. Intended to be subclassed by a concrete implementation that
 * provides a {@link DisplayWindow} in which the display should be housed.
 *
 * @author Curtis Rueden
 * @author Lee Kamentsky
 * @author Grant Harris
 * @author Barry DeZonia
 */
@Plugin( type = DisplayViewer.class, priority = Priority.VERY_HIGH_PRIORITY )
public class BDVImageDisplayViewer extends
		AbstractImageDisplayViewer
{

	protected AWTInputEventDispatcher dispatcher;

	@Parameter
	private EventService eventService;

	@Parameter
	private ImageDisplayService imageDisplayService;

	private DatasetView activeDatasetView;

	private LinearRange renderConverterRange;

	@EventHandler
	protected void onEvent( final DataViewUpdatedEvent event )
	{
		if ( event.getView() == activeDatasetView )
		{
			final double channelMin = activeDatasetView.getChannelMin( 0 );
			final double channelMax = activeDatasetView.getChannelMax( 0 );
			renderConverterRange.setMin( channelMin );
			renderConverterRange.setMax( channelMax );
			this.getPanel().redraw();
		}
	}

	// -- DisplayViewer methods --

	@SuppressWarnings( "unchecked" )
	@Override
	public void view( final DisplayWindow w, final Display< ? > d )
	{
		super.view( w, d );

		dispatcher = new AWTInputEventDispatcher( getDisplay(), eventService );

		activeDatasetView = imageDisplayService.getActiveDatasetView( ( ImageDisplay ) d );

		final Dataset activeDataset = imageDisplayService.getActiveDataset( ( ImageDisplay ) d );
		if ( activeDataset.getImgPlus().numDimensions() == 2 )
			view2( w, d, ( ImgPlus ) activeDataset.getImgPlus() );
		else if ( activeDataset.getImgPlus().numDimensions() == 3 )
			view3( w, d, ( ImgPlus ) activeDataset.getImgPlus() );

//		updateTitle();
	}

	private < T extends RealType< T > > void view2( final DisplayWindow w, final Display< ? > d, final ImgPlus< T > imgPlus )
	{
		final int width = ( int ) imgPlus.dimension( 0 );
		final int height = ( int ) imgPlus.dimension( 1 );
		final RandomAccessible< T > source = Views.extendZero( imgPlus );
		final AffineTransform2D sourceTransform = new AffineTransform2D();

		final T type = imgPlus.firstElement();
		final RealARGBColorConverter< T > converter = new RealARGBColorConverter.Imp0< T >( type.getMinValue(), type.getMaxValue() );
		converter.setColor( new ARGBType( 0xffffffff ) );
		renderConverterRange = converter;

		final RenderSource< T, AffineTransform2D > renderSource = new InterpolatingSource< T, AffineTransform2D >( source, sourceTransform, converter );
		final BDVDisplayPanel< AffineTransform2D, InteractiveDisplayCanvasComponent< AffineTransform2D > > panel =
				new BDVDisplayPanel< AffineTransform2D, InteractiveDisplayCanvasComponent< AffineTransform2D > >(
						w, d,
						AffineTransformType2D.instance,
						new InteractiveDisplayCanvasComponent< AffineTransform2D >( width, height, TransformEventHandler2D.factory() ),
						Defaults.rendererFactory( AffineTransformType2D.instance, renderSource ) );

		setPanel( panel );
		w.setContent( panel );
		w.pack();
	}

	private static double[] getcalib( final AnnotatedSpace< ? extends LinearAxis > calib )
	{
		final double[] c = new double[ calib.numDimensions() ];
		for ( int d = 0; d < c.length; ++d )
			c[ d ] = calib.axis( d ).scale();
		return c;
	}

	@SuppressWarnings( "unchecked" )
	private < T extends RealType< T > > void view3( final DisplayWindow w, final Display< ? > d, final ImgPlus< T > imgPlus )
	{
		final int width = ( int ) imgPlus.dimension( 0 );
		final int height = ( int ) imgPlus.dimension( 1 );
		final RandomAccessible< T > source = Views.extendZero( imgPlus );

		final AffineTransform3D sourceTransform = new AffineTransform3D();
		if ( imgPlus.axis( 0 ) instanceof LinearAxis )
		{
			final double[] scales = getcalib( ( AnnotatedSpace< ? extends LinearAxis > ) imgPlus );
			for ( int i = 0; i < 3; ++i )
				sourceTransform.set( scales[ i ], i, i );
		}

		final T type = imgPlus.firstElement();
		final RealARGBColorConverter< T > converter = new RealARGBColorConverter.Imp0< T >( type.getMinValue(), type.getMaxValue() );
		converter.setColor( new ARGBType( 0xffffffff ) );
		renderConverterRange = converter;

		final InterpolatingSource< T, AffineTransform3D > renderSource = new InterpolatingSource< T, AffineTransform3D >( source, sourceTransform, converter );

		final BDVDisplayPanel< AffineTransform3D, InteractiveDisplayCanvasComponent< AffineTransform3D > > panel =
				new BDVDisplayPanel< AffineTransform3D, InteractiveDisplayCanvasComponent< AffineTransform3D > >(
						w, d,
						AffineTransformType3D.instance,
						new InteractiveDisplayCanvasComponent< AffineTransform3D >( width, height, TransformEventHandler3D.factory() ),
						Defaults.rendererFactory( AffineTransformType3D.instance, renderSource ) );

		// add box overlay
		final BoxOverlayRenderer box = new BoxOverlayRenderer( width, height );
		box.setSource( imgPlus, renderSource.getSourceTransform() );
		panel.canvas.addTransformListener( box );
		panel.canvas.addOverlayRenderer( box );

		// add KeyHandler for toggling interpolation
		panel.canvas.addHandler( new KeyAdapter()
		{
			@Override
			public void keyPressed( final KeyEvent e )
			{
				if ( e.getKeyCode() == KeyEvent.VK_I )
				{
					renderSource.switchInterpolation();
					panel.requestRepaint();
				}
			}
		} );

		setPanel( panel );
		w.setContent( panel );
		w.pack();
	}

	@Override
	public Dataset capture()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCompatible( final UserInterface ui )
	{
		return ui instanceof SwingUI;
	}

	// -- Disposable methods --

	/**
	 * NB: a reference to the imgCanvas is held, ultimately, by a finalizable
	 * parent of a javax.swing.JViewport. This means that the entire resource
	 * stack is held until finalize executes. This can be troublesome when
	 * resources held by the imgCanvas themselves react to finalization or
	 * reference queueing (e.g. of PhantomReferences). At the point dispose is
	 * called, we know we're trying to release resources associated with this
	 * object, so it makes sense to do as much as we can up front. By clearing
	 * the imgCanvas, we break the strong reference link from Finalizer to the
	 * imgCanvas's resources, allowing them to be garbage collected, etc... and
	 * working around the limitation of swing classes overriding finalize.
	 */
	@Override
	public void dispose()
	{
		super.dispose();

	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent( @SuppressWarnings( "unused" ) final OptionsEvent e )
	{
		updateLabel();
	}
}
