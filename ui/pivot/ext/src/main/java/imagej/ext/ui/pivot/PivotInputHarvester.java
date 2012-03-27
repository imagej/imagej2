
package imagej.ext.ui.pivot;

import imagej.ext.Priority;
import imagej.ext.module.Module;
import imagej.ext.module.ui.InputHarvester;
import imagej.ext.module.ui.InputPanel;
import imagej.ext.plugin.AbstractInputHarvesterPlugin;
import imagej.ext.plugin.Plugin;
import imagej.ext.plugin.process.PreprocessorPlugin;

import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Sheet;

/**
 * PivotInputHarvester is an {@link InputHarvester} that collects input
 * parameter values from the user using a {@link PivotInputPanel} dialog box.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
@Plugin(type = PreprocessorPlugin.class, priority = Priority.VERY_LOW_PRIORITY)
public class PivotInputHarvester extends AbstractInputHarvesterPlugin {

	@Override
	public PivotInputPanel createInputPanel() {
		return new PivotInputPanel();
	}

	@Override
	public boolean
		harvestInputs(final InputPanel inputPanel, final Module module)
	{
		final Sheet dialog = new Sheet();
		dialog.setTitle(module.getInfo().getLabel());
		dialog.add(((PivotInputPanel) inputPanel).getPanel());
		dialog.open((Display) null);// FIXME
		final boolean success = dialog.getResult();
		return success;
	}

}
