/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.core.plugins.debug;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.display.DatasetView;
import imagej.display.Display;
import imagej.display.DisplayService;
import imagej.display.DisplayView;
import imagej.ext.module.Module;
import imagej.ext.module.ModuleItem;
import java.util.Map;

/**
 *
 * @author GBH
 */
public class Util {
	// -- Helper methods --

	public static Display activeDisplay() {
		return ImageJ.get(DisplayService.class).getActiveDisplay();
	}

	public static boolean setInputFromActive(final Module module) {
		boolean found = false;
		final DisplayService displayService = ImageJ.get(DisplayService.class);

		// assign active display to single Display input
		final String displayInput = getSingleInput(module, Display.class);
		final Display activeDisplay = displayService.getActiveDisplay();
		if (displayInput != null && activeDisplay != null) {
			module.setInput(displayInput, activeDisplay);
			module.setResolved(displayInput, true);
			found = true;
		}

		// assign active dataset view to single DatasetView input
		final String datasetViewInput = getSingleInput(module, DatasetView.class);
		final DatasetView activeDatasetView = displayService.getActiveDatasetView();
		if (datasetViewInput != null && activeDatasetView != null) {
			module.setInput(datasetViewInput, activeDatasetView);
			module.setResolved(datasetViewInput, true);
			found = true;
		}

		// assign active display view to single DisplayView input
		final String displayViewInput = getSingleInput(module, DisplayView.class);
		final DisplayView activeDisplayView =
				activeDisplay == null ? null : activeDisplay.getActiveView();
		if (displayViewInput != null && activeDisplayView != null) {
			module.setInput(displayViewInput, activeDisplayView);
			module.setResolved(displayViewInput, true);
			found = true;
		}

		// assign active dataset to single Dataset input
		final String datasetInput = getSingleInput(module, Dataset.class);
		final Dataset activeDataset = displayService.getActiveDataset();
		if (datasetInput != null && activeDataset != null) {
			module.setInput(datasetInput, activeDataset);
			module.setResolved(datasetInput, true);
			found = true;
		}
		return found;
	}

	public static boolean setInput(final Module module, Object operand) {
		boolean found = false;
		final DisplayService displayService = ImageJ.get(DisplayService.class);

		// assign active display to single Display input
		final String input = getSingleInput(module, operand.getClass());
		if (input != null) {
			module.setInput(input, operand);
			module.setResolved(input, true);
			found = true;
		}

		return found;
	}

	// -- Helper methods --
	private static String getSingleInput(final Module module, final Class<?> type) {
		final Iterable<ModuleItem<?>> inputs = module.getInfo().inputs();
		String result = null;
		for (final ModuleItem<?> item : inputs) {
			final String name = item.getName();
			final boolean resolved = module.isResolved(name);
			if (resolved) {
				continue; // skip resolved inputs
			}
			if (!type.isAssignableFrom(item.getType())) {
				continue;
			}
			if (result != null) {
				return null; // there are multiple matching inputs
			}
			result = name;
		}
		return result;
	}

	public void openInDisplay(final Module module) {
		final Map<String, Object> outputs = module.getOutputs();
		final Dataset dataset = (Dataset) outputs.values();
		final DisplayService displayService = ImageJ.get(DisplayService.class);
		displayService.createDisplay(dataset);
	}

}
