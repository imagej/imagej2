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

package imagej;

import imagej.app.ImageJApp;
import imagej.command.CommandService;
import imagej.data.DatasetService;
import imagej.data.animation.AnimationService;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.InputService;
import imagej.data.display.OverlayService;
import imagej.data.display.ScreenCaptureService;
import imagej.data.display.WindowService;
import imagej.data.lut.LUTService;
import imagej.data.measure.MeasurementService;
import imagej.data.measure.StatisticsService;
import imagej.data.operator.CalculatorService;
import imagej.data.sampler.SamplerService;
import imagej.data.threshold.ThresholdService;
import imagej.display.DisplayService;
import imagej.io.RecentFileService;
import imagej.legacy.LegacyService;
import imagej.menu.MenuService;
import imagej.module.ModuleService;
import imagej.ops.OpService;
import imagej.options.OptionsService;
import imagej.platform.AppEventService;
import imagej.platform.PlatformService;
import imagej.render.RenderingService;
import imagej.script.ScriptService;
import imagej.tool.IconService;
import imagej.tool.ToolService;
import imagej.ui.UIService;
import imagej.updater.core.UploaderService;
import imagej.widget.WidgetService;

import java.util.Collection;

import org.scijava.AbstractGateway;
import org.scijava.Context;
import org.scijava.Gateway;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;

/**
 * Main entry point into ImageJ. This class enables working with ImageJ services
 * in a simple way, while retaining extensibility (i.e., access to third-party
 * services).
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Gateway.class)
public class ImageJ extends AbstractGateway {

	// -- Constructors --

	/** Creates a new ImageJ application context with all available services. */
	public ImageJ() {
		// TODO: Consider whether to restrict available services
		// to SciJava, SCIFIO and ImageJ by default.
		this(new Context());
	}

	/**
	 * Creates a new ImageJ application context.
	 * 
	 * @param empty If true, the context will be empty; otherwise, it will be
	 *          initialized with all available services.
	 */
	public ImageJ(final boolean empty) {
		this(new Context(empty));
	}

	/**
	 * Creates a new ImageJ application context with the specified services (and
	 * any required service dependencies).
	 * <p>
	 * <b>Developer's note:</b> This constructor's argument is raw (i.e.,
	 * {@code Class...} instead of {@code Class<? extends Service>...}) because
	 * otherwise, downstream invocations (e.g.,
	 * {@code new ImageJ(DisplayService.class)}) yield the potentially confusing
	 * warning:
	 * </p>
	 * <blockquote>Type safety: A generic array of Class<? extends Service> is
	 * created for a varargs parameter</blockquote>
	 * <p>
	 * To avoid this, we have opted to use raw types and suppress the relevant
	 * warning here instead.
	 * </p>
	 * 
	 * @param serviceClasses A list of types that implement the {@link Service}
	 *          interface (e.g., {@code DisplayService.class}).
	 * @throws ClassCastException If any of the given arguments do not implement
	 *           the {@link Service} interface.
	 */
	@SuppressWarnings({ "rawtypes" })
	public ImageJ(final Class... serviceClasses) {
		this(new Context(serviceClasses));
	}

	/**
	 * Creates a new ImageJ application context with the specified services (and
	 * any required service dependencies).
	 * 
	 * @param serviceClasses A collection of types that implement the
	 *          {@link Service} interface (e.g., {@code DisplayService.class}).
	 */
	public ImageJ(final Collection<Class<? extends Service>> serviceClasses) {
		this(new Context(serviceClasses));
	}

	/**
	 * Creates a new ImageJ application context which wraps the given existing
	 * SciJava context.
	 * 
	 * @see Context
	 */
	public ImageJ(final Context context) {
		super(ImageJApp.NAME, context);
	}

	// -- ImageJ methods - services --

	public AnimationService animation() {
		return get(AnimationService.class);
	}

	public AppEventService appEvent() {
		return get(AppEventService.class);
	}

	public CalculatorService calculator() {
		return get(CalculatorService.class);
	}

	public CommandService command() {
		return get(CommandService.class);
	}

	public DatasetService dataset() {
		return get(DatasetService.class);
	}

	public DisplayService display() {
		return get(DisplayService.class);
	}

	public IconService icon() {
		return get(IconService.class);
	}

	public ImageDisplayService imageDisplay() {
		return get(ImageDisplayService.class);
	}

	public InputService input() {
		return get(InputService.class);
	}

	public LegacyService legacy() {
		return get(LegacyService.class);
	}

	public LUTService lut() {
		return get(LUTService.class);
	}

	public MeasurementService measurement() {
		return get(MeasurementService.class);
	}

	public MenuService menu() {
		return get(MenuService.class);
	}

	public ModuleService module() {
		return get(ModuleService.class);
	}

	public OpService op() {
		return get(OpService.class);
	}

	public OptionsService options() {
		return get(OptionsService.class);
	}

	public OverlayService overlay() {
		return get(OverlayService.class);
	}

	public PlatformService platform() {
		return get(PlatformService.class);
	}

	public RecentFileService recentFile() {
		return get(RecentFileService.class);
	}

	public RenderingService rendering() {
		return get(RenderingService.class);
	}

	public SamplerService sampler() {
		return get(SamplerService.class);
	}

	public ScreenCaptureService screenCapture() {
		return get(ScreenCaptureService.class);
	}

	public ScriptService script() {
		return get(ScriptService.class);
	}

	public StatisticsService statistics() {
		return get(StatisticsService.class);
	}

	public ThresholdService threshold() {
		return get(ThresholdService.class);
	}

	public ToolService tool() {
		return get(ToolService.class);
	}

	public UIService ui() {
		return get(UIService.class);
	}

	public UploaderService uploader() {
		return get(UploaderService.class);
	}

	public WidgetService widget() {
		return get(WidgetService.class);
	}

	public WindowService window() {
		return get(WindowService.class);
	}

}
