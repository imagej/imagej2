//
// VisADPlugin.java
//

/*
VisAD plugin for ImageJ.
Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.visad;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JPanel;

import visad.DataReferenceImpl;
import visad.DisplayImpl;
import visad.FieldImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.Integer1DSet;
import visad.Integer2DSet;
import visad.MathType;
import visad.RealTupleType;
import visad.RealType;
import visad.ScalarMap;
import visad.TupleType;
import visad.VisADException;
import visad.java3d.DisplayImplJ3D;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

/**
 * Displays an image using VisAD.
 *
 * <dl>
 * <dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.imagejdev.org/trac/java/browser/trunk/projects/ij-visad/src/main/java/imagej/visad/VisADPlugin.java">Trac</a>,
 * <a href="http://dev.imagejdev.org/svn/java/trunk/projects/ij-visad/src/main/java/imagej/visad/VisADPlugin.java">SVN</a></dd>
 * </dl>
 */
public class VisADPlugin implements PlugIn {

	// -- PlugIn methods --

	public void run(String arg) {
		final ImagePlus imp = WindowManager.getCurrentImage();
		final String title = imp.getTitle();

		try {
			final DisplayImpl display = new DisplayImplJ3D(title);
			final DataReferenceImpl ref = new DataReferenceImpl(title);
			final FieldImpl field = createField(imp);
			final List<ScalarMap> maps = identifyMaps(field);
			for (ScalarMap map : maps) display.addMap(map);
			ref.setData(field);
			display.addReference(ref);

			final Frame frame = makeDisplayFrame(display, title);
			WindowManager.addWindow(frame);
			frame.pack();
			frame.setVisible(true);
		}
		catch (RemoteException e) {
			IJ.handleException(e);
		}
		catch (VisADException e) {
			IJ.handleException(e);
		}
	}

	/** Transforms the given image into a VisAD field. */
	private FieldImpl createField(ImagePlus imp) throws VisADException, RemoteException {
		// get dimensional extents
		final int sizeX = imp.getWidth();
		final int sizeY = imp.getHeight();
		final int sizeC = imp.getNChannels();
		final int sizeT = imp.getNFrames();
		final int sizeZ = imp.getNSlices();
		
		// construct MathType: (t -> (z -> (c -> ((x, y) -> value))))
		final RealType xType = RealType.getRealType("x");
		final RealType yType = RealType.getRealType("y");
		final RealTupleType xyType = new RealTupleType(xType, yType);
		final RealType cType = RealType.getRealType("channel");
		final RealType zType = RealType.getRealType("z");
		final RealType tType = RealType.getRealType("time");
		final RealType valueType = RealType.getRealType("value");
		final FunctionType planeType = new FunctionType(xyType, valueType);
		final FunctionType cImageType = new FunctionType(cType, planeType);
		final FunctionType zcImageType = new FunctionType(zType, cImageType);
		final FunctionType tzcImageType = new FunctionType(tType, zcImageType);

		// construct domain sets
		final Integer2DSet planeSet = new Integer2DSet(xyType, sizeX, sizeY);
		final Integer1DSet cSet = new Integer1DSet(tType, sizeC);
		final Integer1DSet zSet = new Integer1DSet(tType, sizeZ);
		final Integer1DSet tSet = new Integer1DSet(tType, sizeT);

		// construct field
		final FieldImpl field = new FieldImpl(tzcImageType, tSet);
		for (int t=0; t<sizeT; t++) {
			final FieldImpl tData = new FieldImpl(zcImageType, zSet);
			for (int z=0; z<sizeZ; z++) {
				final FieldImpl cData = new FieldImpl(cImageType, cSet);
				for (int c=0; c<sizeC; c++) {
					final FlatField imageData = new FlatField(planeType, planeSet);
					final float[][] plane = createPlane(imp, c, z, t);
					imageData.setSamples(plane, false);
					cData.setSample(c, imageData);
				}
				tData.setSample(z, cData);
			}
			field.setSample(t, tData);
		}
		return field;
	}

	private float[][] createPlane(ImagePlus imp, int c, int z, int t) {
		final int sizeX = imp.getWidth();
		final int sizeY = imp.getHeight();
		final float[][] plane = new float[1][sizeX * sizeY];
		final ImageStack stack = imp.getStack();
		final int planeIndex = imp.getStackIndex(c, z, t);
		final ImageProcessor ip = stack.getProcessor(planeIndex);
		for (int y=0; y<sizeY; y++) {
			for (int x=0; x<sizeX; x++) {
				final int index = sizeX * y + x;
				final int value = ip.get(x, y);
				plane[0][index] = value;
			}
		}
		return plane;
	}

	private List<ScalarMap> identifyMaps(FieldImpl field) {
		final MathType fieldType = field.getType();
		final List<RealType> realTypes = extractRealTypes(fieldType);
		final ScalarMap[] guessedMaps = fieldType.guessMaps(true); //TEMP
		final List<ScalarMap> maps = Arrays.asList(guessedMaps); //TEMP
		// TODO: display realTypes in a dialog along with common DisplayRealTypes
		// allow user to map one to the other at-will
		//final List<ScalarMap> maps = new ArrayList<ScalarMap>();
		return maps;
	}

	private List<RealType> extractRealTypes(MathType type) {
		final List<RealType> realTypes = new ArrayList<RealType>();
		extractRealTypes(type, realTypes);
		return realTypes;
	}
	
	private void extractRealTypes(MathType type, List<RealType> realTypes) {
		if (type instanceof RealType) {
			realTypes.add((RealType) type);
		}
		else if (type instanceof FunctionType) {
			final FunctionType functionType = (FunctionType) type;
			extractRealTypes(functionType.getDomain(), realTypes);
			extractRealTypes(functionType.getRange(), realTypes);
		}
		else if (type instanceof TupleType) {
			final TupleType tupleType = (TupleType) type;
			final MathType[] componentTypes = tupleType.getComponents();
			for (MathType componentType : componentTypes) {
				extractRealTypes(componentType, realTypes);
			}
		}
	}

	private Frame makeDisplayFrame(DisplayImpl display, String title) {
		final Frame frame = new Frame(title);
		final JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(display.getComponent(), BorderLayout.CENTER);
		panel.add(display.getWidgetPanel(), BorderLayout.SOUTH);
		frame.add(panel);
		return frame;
	}

}
