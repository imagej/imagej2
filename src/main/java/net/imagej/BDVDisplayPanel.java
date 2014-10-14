package net.imagej;

import java.awt.BorderLayout;

import javax.swing.JComponent;

import net.imglib2.concatenate.Concatenable;
import net.imglib2.realtransform.AffineGet;
import net.imglib2.realtransform.AffineSet;
import net.imglib2.ui.AffineTransformType;
import net.imglib2.ui.InteractiveDisplayCanvas;
import net.imglib2.ui.OverlayRenderer;
import net.imglib2.ui.PainterThread;
import net.imglib2.ui.RenderTarget;
import net.imglib2.ui.Renderer;
import net.imglib2.ui.RendererFactory;
import net.imglib2.ui.TransformListener;
import net.imglib2.ui.overlay.BufferedImageOverlayRenderer;

import org.scijava.display.Display;
import org.scijava.ui.swing.viewer.SwingDisplayPanel;
import org.scijava.ui.viewer.DisplayWindow;

public class BDVDisplayPanel<
			A extends AffineSet & AffineGet & Concatenable< AffineGet >,
			C extends JComponent & InteractiveDisplayCanvas< A > >
		extends SwingDisplayPanel
		implements TransformListener< A >, PainterThread.Paintable
{
	private static final long serialVersionUID = 1L;

	private final DisplayWindow window;

	private final Display< ? > display;

	/**
	 * Create an interactive viewer window displaying the specified
	 * <code>interactiveDisplayCanvas</code>, and create a {@link Renderer}
	 * which draws to that canvas.
	 * <p>
	 * A {@link Renderer} is created that paints to a
	 * {@link BufferedImageOverlayRenderer} render target which is displayed on
	 * the canvas as an {@link OverlayRenderer}. A {@link PainterThread} is
	 * created which queues repainting requests from the renderer and
	 * interactive canvas, and triggers {@link #paint() repainting} of the
	 * viewer.
	 *
	 * @param transformType
	 * @param interactiveDisplayCanvas
	 *            the canvas {@link JComponent} which will show the rendered
	 *            images.
	 * @param rendererFactory
	 *            is used to create the {@link Renderer}.
	 */
	public BDVDisplayPanel(
			final DisplayWindow window,
			final Display< ? > display,
			final AffineTransformType< A > transformType,
			final C interactiveDisplayCanvas,
			final RendererFactory< A > rendererFactory )
	{
		this.window = window;
		this.display = display;
		this.transformType = transformType;
		painterThread = new PainterThread( this );
		viewerTransform = transformType.createTransform();
		canvas = interactiveDisplayCanvas;
		canvas.addTransformListener( this );

		final BufferedImageOverlayRenderer target = new BufferedImageOverlayRenderer();
		imageRenderer = rendererFactory.create( target, painterThread );
		canvas.addOverlayRenderer( target );
		target.setCanvasSize( canvas.getWidth(), canvas.getHeight() );

		setLayout( new BorderLayout() );
		add( canvas, BorderLayout.CENTER );

		painterThread.start();
	}

	@Override
	public Display< ? > getDisplay()
	{
		return display;
	}

	@Override
	public DisplayWindow getWindow()
	{
		return window;
	}

	@Override
	public void redoLayout()
	{
		window.pack();
	}

	@Override
	public void setLabel( final String s )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void redraw()
	{
		requestRepaint();
	}

	// ==================================================
	// the following is more or less the same as
	// net.imglib2.ui.viewer.InteractiveRealViewer

	final protected AffineTransformType< A > transformType;

	/**
	 * Transformation set by the interactive viewer.
	 */
	final protected A viewerTransform;

	/**
	 * Canvas used for displaying the rendered {@link #screenImages screen
	 * image}.
	 */
	final protected C canvas;

	/**
	 * Thread that triggers repainting of the display.
	 */
	final protected PainterThread painterThread;

	/**
	 * Paints to a {@link RenderTarget} that is shown in the {@link #display
	 * canvas}.
	 */
	final protected Renderer< A > imageRenderer;

	/**
	 * Render the source using the current viewer transformation and
	 */
	@Override
	public void paint()
	{
		imageRenderer.paint( viewerTransform );
		canvas.repaint();
	}

	@Override
	public void transformChanged( final A transform )
	{
		transformType.set( viewerTransform, transform );
		requestRepaint();
	}

	/**
	 * Request a repaint of the display. Calls {@link Renderer#requestRepaint()}
	 * .
	 */
	public void requestRepaint()
	{
		imageRenderer.requestRepaint();
	}
}
