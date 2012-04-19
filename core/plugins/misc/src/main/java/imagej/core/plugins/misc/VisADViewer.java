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
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Parameter;
import imagej.util.Log;

import java.awt.BorderLayout;
import java.io.File;
import java.rmi.RemoteException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import loci.formats.gui.BufferedImageReader;
import visad.DataReferenceImpl;
import visad.Display;
import visad.DisplayImpl;
import visad.FunctionType;
import visad.LocalDisplay;
import visad.RealTupleType;
import visad.RealType;
import visad.ScalarMap;
import visad.VisADException;
import visad.java3d.DisplayImplJ3D;

/**
 * Displays all data present in the given dataset, according to their given
 * stage positions.
 * 
 * @author curtis
 */
public class VisADViewer implements ImageJPlugin {

	@Parameter(persist = false)
	protected EventService eventService;

	@Parameter(label = "Image file")
	protected File imageFile;

	// -- Runnable methods --

	@Override
	public void run() {
		try {
			final String title = imageFile.getName();
			final DisplayImpl display = new DisplayImplJ3D(title);

			final RealType xType = RealType.getRealType("X");
			final RealType yType = RealType.getRealType("Y");
			final RealType zType = RealType.getRealType("Z");
			final RealType tType = RealType.getRealType("Time");
			final RealType cType = RealType.getRealType("Channel");
			final RealType vType = RealType.getRealType("value");

			final RealTupleType xyType = new RealTupleType(xType, yType);
			final FunctionType xyvFunc = new FunctionType(xyType, vType);
			final FunctionType cxyvFunc = new FunctionType(cType, xyvFunc);
			final FunctionType zcxyvFunc = new FunctionType(zType, cxyvFunc);
			final FunctionType tzcxyvFunc = new FunctionType(tType, zcxyvFunc);

			final BufferedImageReader reader = new BufferedImageReader();
			reader.setId(imageFile.getAbsolutePath());
			final int seriesCount = reader.getSeriesCount();
			for (int s = 0; s < seriesCount; s++) {
				reader.setSeries(s);
				final int sizeX = reader.getSizeX();
				final int sizeY = reader.getSizeY();
				final int sizeZ = reader.getSizeZ();
				final int sizeT = reader.getSizeT();
				final int sizeC = reader.getSizeC();

				for (int t = 0; t < sizeT; t++) {
					// CTR START HERE
				}
			}

			final ScalarMap xMap = new ScalarMap(xType, Display.XAxis);
			final ScalarMap yMap = new ScalarMap(yType, Display.YAxis);
			final ScalarMap zMap = new ScalarMap(zType, Display.ZAxis);
			final ScalarMap tMap = new ScalarMap(tType, Display.Animation);
			final ScalarMap cMap = new ScalarMap(cType, Display.RGB);

			display.addMap(xMap);
			display.addMap(yMap);
			display.addMap(zMap);
			display.addMap(tMap);
			display.addMap(cMap);

			final DataReferenceImpl ref = new DataReferenceImpl(title);
			ref.setData(data);

			display.addReference(ref);

			show(display);
		}
		catch (final VisADException exc) {
			Log.error(exc);
		}
		catch (final RemoteException exc) {
			Log.error(exc);
		}
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

	public void
		status(final int progress, final int maximum, final String message)
	{
		eventService.publish(progress, maximum, message);
	}

	public void status(final String message) {
		eventService.publish(message);
	}

}
