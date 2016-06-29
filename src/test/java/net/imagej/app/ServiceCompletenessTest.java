/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2015 Board of Regents of the University of
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

package net.imagej.app;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import net.imagej.ImageJService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.InstantiableException;
import org.scijava.plugin.PluginIndex;
import org.scijava.plugin.PluginInfo;
import org.scijava.service.SciJavaService;
import org.scijava.service.Service;

import io.scif.SCIFIOService;

/**
 * Tests that all expected ImageJ services are present.
 *
 * @author Curtis Rueden
 */
public class ServiceCompletenessTest {

	private Context ctx;

	@Before
	public void setUp() {
		ctx = new Context(ImageJService.class);
	}

	@After
	public void tearDown() {
		ctx.dispose();
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testServices() {
		final ArrayList<Class<? extends Service>> services =
			new ArrayList<>();
		services.add(net.imagej.DefaultDatasetService.class);
		services.add(net.imagej.DefaultImgPlusService.class);
		services.add(net.imagej.animation.DefaultAnimationService.class);
		services.add(net.imagej.autoscale.DefaultAutoscaleService.class);
		services.add(net.imagej.display.DefaultImageDisplayService.class);
		services.add(net.imagej.display.DefaultOverlayService.class);
		services.add(net.imagej.display.DefaultWindowService.class);
		services.add(net.imagej.display.DefaultZoomService.class);
		services.add(net.imagej.display.DummyScreenCaptureService.class);
//		services.add(net.imagej.legacy.LegacyService.class);
		services.add(net.imagej.legacy.display.LegacyImageDisplayService.class);
		services.add(net.imagej.lut.DefaultLUTService.class);
		services.add(net.imagej.measure.DefaultMeasurementService.class);
		services.add(net.imagej.measure.DefaultStatisticsService.class);
		services.add(net.imagej.operator.DefaultCalculatorService.class);
		services.add(net.imagej.ops.DefaultNamespaceService.class);
		services.add(net.imagej.ops.DefaultOpMatchingService.class);
		services.add(net.imagej.ops.DefaultOpService.class);
		services.add(net.imagej.render.DummyRenderingService.class);
		services.add(net.imagej.sampler.DefaultSamplerService.class);
		services.add(net.imagej.threshold.DefaultThresholdService.class);
		services.add(net.imagej.types.DefaultDataTypeService.class);
		services.add(net.imagej.ui.DefaultImageJUIService.class);
		services.add(net.imagej.ui.awt.AWTRenderingService.class);
		services.add(net.imagej.ui.awt.AWTScreenCaptureService.class);
		services.add(net.imagej.ui.swing.overlay.JHotDrawService.class);
		services.add(net.imagej.updater.DefaultUpdateService.class);
		services.add(net.imagej.updater.DefaultUploaderService.class);
		for (final Class<? extends Service> c : services) {
			final Service s = ctx.service(c);
			assertSame(c, s.getClass());
		}
	}

	@Test
	public void testMarkerInterfaces() throws InstantiableException {
		final PluginIndex pluginIndex = new PluginIndex();
		final List<PluginInfo<Service>> servicePlugins = //
			pluginIndex.getPlugins(Service.class);
		for (final PluginInfo<Service> info : servicePlugins) {
			final Class<? extends Service> c = info.loadClass();
			final boolean scijava = SciJavaService.class.isAssignableFrom(c);
			final boolean imagej = ImageJService.class.isAssignableFrom(c);
			final boolean scifio = SCIFIOService.class.isAssignableFrom(c);
			assertTrue(c.getName(), scijava ^ imagej ^ scifio);
		}
	}

}
