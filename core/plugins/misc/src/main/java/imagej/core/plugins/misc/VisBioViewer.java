/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.core.plugins.misc;

import imagej.event.EventService;
import imagej.event.StatusEvent;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Parameter;
import imagej.util.Log;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import loci.formats.ChannelMerger;
import loci.formats.FormatException;
import loci.formats.gui.BufferedImageReader;
import visad.DataReferenceImpl;
import visad.Display;
import visad.DisplayImpl;
import visad.FieldImpl;
import visad.FunctionType;
import visad.Integer2DSet;
import visad.LocalDisplay;
import visad.MathType;
import visad.RealTupleType;
import visad.RealType;
import visad.ScalarMap;
import visad.VisADException;
import visad.java3d.DisplayImplJ3D;

/**
 * Displays all data present in the given dataset, according to their given
 * stage positions.
 * 
 * @author Curtis Rueden
 */
public class VisBioViewer implements ImageJPlugin {

	// -- Fields --

	private final RealType xType = RealType.getRealType("X");
	private final RealType yType = RealType.getRealType("Y");
	private final RealType zType = RealType.getRealType("Z");
	private final RealType tType = RealType.getRealType("Time");
	private final List<RealType> cTypes = new ArrayList<RealType>();

	// -- Parameters --

	@Parameter(persist = false)
	protected EventService eventService;

	@Parameter(label = "Image file")
	protected File imageFile;

	@Parameter(label = "Render as volumes")
	protected boolean volume;

	@Parameter(label = "Show tiles only (do not read data)")
	protected boolean showTilesOnly;

	// -- Runnable methods --

	@Override
	public void run() {
		try {
			readAndDisplay();
		}
		catch (final VisADException e) {
			Log.error(e);
		}
		catch (final FormatException e) {
			Log.error(e);
		}
		catch (final IOException e) {
			Log.error(e);
		}
	}

	// -- Helper methods --

	private void readAndDisplay() throws VisADException, FormatException,
		IOException
	{
		final String title = imageFile.getName();
		final DisplayImpl display = new DisplayImplJ3D(title);

		final BufferedImageReader reader =
			new BufferedImageReader(new ChannelMerger());
		reader.setId(imageFile.getAbsolutePath());

		final int seriesCount = reader.getSeriesCount();
		final List<DataReferenceImpl> refs = new ArrayList<DataReferenceImpl>();
		for (int s = 0; s < seriesCount; s++) {
			reader.setSeries(s);
			final FieldImpl data = createData(reader);
			final DataReferenceImpl ref = new DataReferenceImpl(title + ":" + s);
			ref.setData(data);
			refs.add(ref);
		}

		final ScalarMap xMap = new ScalarMap(xType, Display.XAxis);
		final ScalarMap yMap = new ScalarMap(yType, Display.YAxis);
		final ScalarMap zMap = new ScalarMap(zType, Display.ZAxis);
		final ScalarMap tMap = new ScalarMap(tType, Display.Animation);
		final List<ScalarMap> cMaps = new ArrayList<ScalarMap>();
		for (final RealType cType : cTypes) {
			cMaps.add(new ScalarMap(cType, Display.RGB));
		}

		display.addMap(xMap);
		display.addMap(yMap);
		display.addMap(zMap);
		display.addMap(tMap);
		for (final ScalarMap cMap : cMaps) {
			display.addMap(cMap);
		}

		for (final DataReferenceImpl ref : refs) {
			display.addReference(ref);
		}

		show(display);
	}

	private FieldImpl createData(final BufferedImageReader reader)
		throws VisADException, FormatException, IOException
	{
		final List<BufferedImage> planes = readPlanes(reader);

		final int sizeX = reader.getSizeX();
		final int sizeY = reader.getSizeY();
		final int sizeZ = reader.getSizeZ();
		final int sizeT = reader.getSizeT();
		final int sizeC = reader.getSizeC();

		final FunctionType type = makeType(sizeC);
		final Integer2DSet set = null;
		final FieldImpl data = new FieldImpl(type, set);

		return null; // TEMP
	}

	private List<BufferedImage> readPlanes(final BufferedImageReader reader)
		throws FormatException, IOException
	{
		final List<BufferedImage> planes = new ArrayList<BufferedImage>();
		final int imageCount = reader.getImageCount();
		for (int i = 0; i < imageCount; i++) {
			final BufferedImage plane = reader.openImage(i);
			// CTR START HERE - maybe return List<FlatField> instead
			// so we're not screwing around assembling everything at the end
			planes.add(plane);
		}
		return planes;
	}

	/**
	 * Creates {@link MathType}:
	 * <code>(Time -> (Z -> ((X, Y) -> (Channel0, ..., ChannelN))))</code> or
	 * <code>(Time -> ((X, Y, Z) -> (Channel0, ..., ChannelN)))</code>
	 * <p>
	 * This is needed because number of channels may differ per series.
	 * </p>
	 */
	private FunctionType makeType(final int sizeC) throws VisADException {
		while (cTypes.size() < sizeC) {
			cTypes.add(RealType.getRealType("Channel" + cTypes.size()));
		}
		final RealType[] cArray = new RealType[sizeC];
		for (int c = 0; c < sizeC; c++) {
			cArray[c] = cTypes.get(c);
		}
		final RealTupleType channels = new RealTupleType(cArray);
		final FunctionType range;
		if (volume) {
			// ((X, Y, Z) -> (Channel0, ..., ChannelN))
			final RealTupleType xyzType = new RealTupleType(xType, yType, zType);
			range = new FunctionType(xyzType, channels);
		}
		else {
			// (Z -> ((X, Y) -> (Channel0, ..., ChannelN)))
			final RealTupleType xyType = new RealTupleType(xType, yType);
			final FunctionType imageType = new FunctionType(xyType, channels);
			range = new FunctionType(zType, imageType);
		}
		return new FunctionType(tType, range);
	}

	private void show(final LocalDisplay display) throws VisADException,
		RemoteException
	{
		final JFrame frame = new JFrame(display.getName());
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		final JPanel pane = new JPanel();
		pane.setLayout(new BorderLayout());
		frame.setContentPane(pane);
		pane.add(display.getComponent(), BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	}

	private void status(final int progress, final int maximum,
		final String message)
	{
		eventService.publish(new StatusEvent(progress, maximum, message));
	}

	private void status(final String message) {
		eventService.publish(new StatusEvent(message));
	}

}
