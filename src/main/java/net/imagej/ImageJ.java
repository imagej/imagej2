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

package net.imagej;

import io.scif.SCIFIO;
import io.scif.SCIFIOService;

import net.imagej.animation.AnimationService;
import net.imagej.app.ImageJApp;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayService;
import net.imagej.display.ScreenCaptureService;
import net.imagej.display.WindowService;
import net.imagej.lut.LUTService;
import net.imagej.notebook.NotebookService;
import net.imagej.ops.OpService;
import net.imagej.render.RenderingService;
import net.imagej.sampler.SamplerService;
import net.imagej.updater.UpdateService;
import net.imagej.updater.UploaderService;

import org.scijava.AbstractGateway;
import org.scijava.Context;
import org.scijava.Gateway;
import org.scijava.plugin.Plugin;
import org.scijava.service.SciJavaService;

/**
 * Main entry point into ImageJ. This class enables working with ImageJ services
 * in a simple way, while retaining extensibility (i.e., access to third-party
 * services).
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Gateway.class)
public class ImageJ extends AbstractGateway {

	/** SCIFIO gateway instance, for access to SCIFIO services. */
	private SCIFIO scifio;

	// -- Constructors --

	/**
	 * Creates a new ImageJ application context with all ImageJ, SCIFIO and
	 * SciJava services.
	 */
	public ImageJ() {
		this(new Context(SciJavaService.class, SCIFIOService.class,
			ImageJService.class));
	}

	/**
	 * Creates a new ImageJ application context which wraps the given existing
	 * SciJava context.
	 * 
	 * @see Context
	 */
	public ImageJ(final Context context) {
		super(ImageJApp.NAME, context);
		scifio = new SCIFIO(context);
	}

	// -- ImageJ methods - gateways --

	public SCIFIO scifio() {
		return scifio;
	}

	// -- ImageJ methods - services --

	/**
	 * Gets this application context's {@link AnimationService}.
	 *
	 * @return The {@link AnimationService} of this application context.
	 */
	public AnimationService animation() {
		return get(AnimationService.class);
	}

	/**
	 * Gets this application context's {@link DatasetService}.
	 *
	 * @return The {@link DatasetService} of this application context.
	 */
	public DatasetService dataset() {
		return get(DatasetService.class);
	}

	/**
	 * Gets this application context's {@link ImageDisplayService}.
	 *
	 * @return The {@link ImageDisplayService} of this application context.
	 */
	public ImageDisplayService imageDisplay() {
		return get(ImageDisplayService.class);
	}

	/**
	 * Gets this application context's {@link LUTService}.
	 *
	 * @return The {@link LUTService} of this application context.
	 */
	public LUTService lut() {
		return get(LUTService.class);
	}

	/**
	 * Gets this application context's {@link NotebookService}.
	 *
	 * @return The {@link NotebookService} of this application context.
	 */
	public NotebookService notebook() {
		return get(NotebookService.class);
	}

	/**
	 * Gets this application context's {@link OpService}.
	 *
	 * @return The {@link OpService} of this application context.
	 */
	public OpService op() {
		return get(OpService.class);
	}

	/**
	 * Gets this application context's {@link OverlayService}.
	 *
	 * @return The {@link OverlayService} of this application context.
	 */
	public OverlayService overlay() {
		return get(OverlayService.class);
	}

	/**
	 * Gets this application context's {@link RenderingService}.
	 *
	 * @return The {@link RenderingService} of this application context.
	 */
	public RenderingService rendering() {
		return get(RenderingService.class);
	}

	/**
	 * Gets this application context's {@link SamplerService}.
	 *
	 * @return The {@link SamplerService} of this application context.
	 */
	public SamplerService sampler() {
		return get(SamplerService.class);
	}

	/**
	 * Gets this application context's {@link ScreenCaptureService}.
	 *
	 * @return The {@link ScreenCaptureService} of this application context.
	 */
	public ScreenCaptureService screenCapture() {
		return get(ScreenCaptureService.class);
	}

	/**
	 * Gets this application context's {@link UpdateService}.
	 *
	 * @return The {@link UpdateService} of this application context.
	 */
	public UpdateService update() {
		return get(UpdateService.class);
	}

	/**
	 * Gets this application context's {@link UploaderService}.
	 *
	 * @return The {@link UploaderService} of this application context.
	 */
	public UploaderService uploader() {
		return get(UploaderService.class);
	}

	/**
	 * Gets this application context's {@link WindowService}.
	 *
	 * @return The {@link WindowService} of this application context.
	 */
	public WindowService window() {
		return get(WindowService.class);
	}

	// -- Gateway methods --

	@Override
	public String getShortName() {
		return "ij";
	}
}
