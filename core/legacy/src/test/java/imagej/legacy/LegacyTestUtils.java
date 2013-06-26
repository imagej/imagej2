/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

package imagej.legacy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import imagej.data.Dataset;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import javassist.ClassPool;
import net.imglib2.RandomAccess;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.type.numeric.RealType;

import org.scijava.Context;
import org.scijava.util.ClassUtils;

/**
 * Utility methods for unit testing in the {@link imagej.legacy} package.
 * 
 * @author Barry DeZonia
 */
public class LegacyTestUtils {

	public static boolean allNull(final AxisType[] axes) {
		for (final AxisType axis : axes)
			if (axis != null) return false;
		return true;
	}

	public static boolean repeated(final AxisType[] axes) {
		int cCount = 0, zCount = 0, tCount = 0;
		for (final AxisType axis : axes) {
			if (axis == Axes.CHANNEL) cCount++;
			if (axis == Axes.Z) zCount++;
			if (axis == Axes.TIME) tCount++;
		}
		return (cCount > 1 || zCount > 1 || tCount > 1);
	}

	public static void testMetadataSame(final Dataset ds, final ImagePlus imp) {
		final int xIndex = ds.getAxisIndex(Axes.X);
		final int yIndex = ds.getAxisIndex(Axes.Y);
		final int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int tIndex = ds.getAxisIndex(Axes.TIME);
		final Calibration cal = imp.getCalibration();

		assertEquals(ds.getName(), imp.getTitle());
		assertEquals(ds.calibration(xIndex), cal.pixelWidth, 0);
		assertEquals(ds.calibration(yIndex), cal.pixelHeight, 0);
		assertEquals(ds.calibration(cIndex), 1, 0);
		assertEquals(ds.calibration(zIndex), cal.pixelDepth, 0);
		assertEquals(ds.calibration(tIndex), cal.frameInterval, 0);
	}

	public static void testSame(final Dataset ds, final ImagePlus imp) {

		final long[] dimensions = ds.getDims();

		final int xIndex = ds.getAxisIndex(Axes.X);
		final int yIndex = ds.getAxisIndex(Axes.Y);
		final int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int tIndex = ds.getAxisIndex(Axes.TIME);

		assertEquals(dimensions[xIndex], imp.getWidth());
		assertEquals(dimensions[yIndex], imp.getHeight());
		assertEquals(dimensions[cIndex], imp.getNChannels());
		assertEquals(dimensions[zIndex], imp.getNSlices());
		assertEquals(dimensions[tIndex], imp.getNFrames());

		final RandomAccess<? extends RealType<?>> accessor =
			ds.getImgPlus().randomAccess();
		int ij1PlaneNumber = 1;
		final long[] pos = new long[dimensions.length];
		for (int t = 0; t < dimensions[tIndex]; t++) {
			pos[tIndex] = t;
			for (int z = 0; z < dimensions[zIndex]; z++) {
				pos[zIndex] = z;
				for (int c = 0; c < dimensions[cIndex]; c++) {
					pos[cIndex] = c;
					final ImageProcessor proc =
						imp.getStack().getProcessor(ij1PlaneNumber++);
					for (int y = 0; y < dimensions[yIndex]; y++) {
						pos[yIndex] = y;
						for (int x = 0; x < dimensions[xIndex]; x++) {
							pos[xIndex] = x;
							accessor.setPosition(pos);
							final double ij1Value = proc.getf(x, y);
							final double modernValue = accessor.get().getRealDouble();
							if (Math.abs(ij1Value - modernValue) > 0.1) System.out
								.println("x=" + x + " y=" + y + " c=" + c + " z=" + z + " t=" +
									t + " && ij1=" + ij1Value + " modern=" + modernValue);
							assertEquals(ij1Value, modernValue, 0.0001);
						}
					}
				}
			}
		}

		testMetadataSame(ds, imp);
	}

	public static void testColorSame(final Dataset ds, final ImagePlus imp) {

		final long[] dimensions = ds.getDims();

		final int xIndex = ds.getAxisIndex(Axes.X);
		final int yIndex = ds.getAxisIndex(Axes.Y);
		final int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int tIndex = ds.getAxisIndex(Axes.TIME);

		assertEquals(dimensions[xIndex], imp.getWidth());
		assertEquals(dimensions[yIndex], imp.getHeight());
		assertEquals(dimensions[cIndex], 3 * imp.getNChannels());
		assertEquals(dimensions[zIndex], imp.getNSlices());
		assertEquals(dimensions[tIndex], imp.getNFrames());

		final int c = imp.getNChannels();
		final int z = imp.getNSlices();
		final int t = imp.getNFrames();

		final RandomAccess<? extends RealType<?>> accessor =
			ds.getImgPlus().randomAccess();
		final long[] pos = new long[dimensions.length];
		int ijPlaneNumber = 1;
		for (int ti = 0; ti < t; ti++) {
			pos[tIndex] = ti;
			for (int zi = 0; zi < z; zi++) {
				pos[zIndex] = zi;
				for (int ci = 0; ci < c; ci++) {
					final ImageProcessor proc =
						imp.getStack().getProcessor(ijPlaneNumber++);
					for (int y = 0; y < dimensions[yIndex]; y++) {
						pos[yIndex] = y;
						for (int x = 0; x < dimensions[xIndex]; x++) {
							pos[xIndex] = x;

							pos[cIndex] = 3 * ci + 0;
							accessor.setPosition(pos);
							final int r = (int) accessor.get().getRealDouble();

							pos[cIndex] = 3 * ci + 1;
							accessor.setPosition(pos);
							final int g = (int) accessor.get().getRealDouble();

							pos[cIndex] = 3 * ci + 2;
							accessor.setPosition(pos);
							final int b = (int) accessor.get().getRealDouble();

							final int ij1Value = proc.get(x, y);
							final int modernValue = 0xff000000 | (r << 16) | (g << 8) | b;

							assertEquals(ij1Value, modernValue);
						}
					}
				}
			}
		}

		testMetadataSame(ds, imp);
	}

	/**
	 * Makes a fresh class loader for use with ImageJ 1.x.
	 * 
	 * <p>
	 * In particular, this class loader can be used to test ImageJ 1.x classes with and without ij-legacy patching.
	 * </p>
	 * 
	 * @param patchLegacy whether to apply the legacy patches or not
	 * @param patchHeadless whether to apply ij-legacy's headless patches or not
	 * @param classNames names of classes that we want to be found in the class loader
	 * @return a fresh class loader
	 */
	public static ClassLoader getFreshIJClassLoader(final boolean patchLegacy, final boolean patchHeadless, final String... classNames) {
		final URL[] urls = new URL[classNames.length + 3];
		urls[0] = getClassLocation("ij.IJ");
		urls[1] = ClassUtils.getLocation(DefaultLegacyService.class);
		urls[2] = ClassUtils.getLocation(Context.class);
		for (int i = 0; i < classNames.length; i++) urls[i + 3] = getClassLocation(classNames[i]);

		// use the bootstrap class loader as parent so that ij.IJ must resolve
		// via the new class loader
		final ClassLoader parent = ClassLoader.getSystemClassLoader().getParent();
		try {
			assertFalse(parent.loadClass("ij.IJ") != null);
		} catch (ClassNotFoundException e) {
			// ignore
		}
		final ClassLoader loader = new URLClassLoader(urls, parent);
		if (patchLegacy || patchHeadless) {
			final CodeHacker hacker = new CodeHacker(loader, new ClassPool(false));
			if (patchHeadless) {
				new LegacyHeadless(hacker).patch();
			}
			if (patchLegacy) {
				new LegacyInjector().injectHooks(hacker);
			}
			hacker.loadClasses();
		}
		return loader;
	}

	/**
	 * Gets the class location without loading the class.
	 * 
	 * @param className the name of the class
	 * @return the URL of the class path element
	 */
	private static URL getClassLocation(final String className) {
		final String path = "/" + className.replace('.', '/') + ".class";
		final URL classURL = LegacyTestUtils.class.getResource(path);
		final String urlAsString = classURL == null ? "(null)" : classURL.toString();
		final String result;
		if (urlAsString.startsWith("jar:") && urlAsString.endsWith("!" + path)) {
			result = urlAsString.substring(4, urlAsString.length() - 1 - path.length());
		} else if (urlAsString.startsWith("file:") && urlAsString.endsWith(path)) {
			result = urlAsString.substring(0, urlAsString.length() + 1 - path.length());
		} else {
			throw new IllegalArgumentException("Unexpected location for " + path + ": " + classURL);
		}
		try {
			return new URL(result);
		} catch(MalformedURLException e) {
			throw new IllegalArgumentException("Illegal URL: " + urlAsString);
		}
	}

}
