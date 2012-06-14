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

package imagej.ext.plugin;

import imagej.ImageJ;
import imagej.ext.MenuPath;
import imagej.ext.UIDetails;
import imagej.ext.module.DefaultModuleInfo;
import imagej.ext.module.DefaultModuleItem;
import imagej.ext.module.ModuleInfo;
import imagej.ext.module.ModuleItem;

import java.lang.reflect.Field;

/**
 * Helper class for maintaining a {@link DynamicPlugin}'s associated
 * {@link ModuleInfo}.
 * <p>
 * The {@link PluginService} has a plain {@link PluginInfo} object in its index,
 * populated from the {@link DynamicPlugin}'s @{@link Plugin} annotation. So
 * this class adapts that object, delegating to it for the {@link UIDetails}
 * methods. The plain {@link PluginInfo} cannot be used as-is, however, because
 * we need to override the {@link ModuleInfo} methods as well as provide new
 * functionality such as {@link DefaultModuleInfo#addInput(ModuleItem)}.
 * </p>
 * 
 * @author Curtis Rueden
 */
public class DynamicPluginInfo extends DefaultModuleInfo {

	private PluginModuleInfo<? extends DynamicPlugin> info;

	// -- Internal methods --

	protected void setPluginClass(
		final Class<? extends DynamicPlugin> pluginClass)
	{
		final PluginService pluginService = ImageJ.get(PluginService.class);
		info = pluginService.getRunnablePlugin(pluginClass);
		populateItems();
	}

	/**
	 * Gets the {@link Field} corresponding to the given @{@link Parameter}
	 * annotated module input, or null if the input does not exist or was not
	 * declared using the @{@link Parameter} mechanism.
	 */
	protected Field getInputField(final String name) {
		final PluginModuleItem<?> item = info.getInput(name);
		return item == null ? null : item.getField();
	}

	/**
	 * Gets the {@link Field} corresponding to the given @{@link Parameter}
	 * annotated module output, or null if the output does not exist or was not
	 * declared using the @{@link Parameter} mechanism.
	 */
	protected Field getOutputField(final String name) {
		final PluginModuleItem<?> item = info.getOutput(name);
		return item == null ? null : item.getField();
	}

	// -- ModuleInfo methods --

	@Override
	public boolean canPreview() {
		return info.canPreview();
	}

	@Override
	public boolean canCancel() {
		return info.canCancel();
	}

	@Override
	public String getInitializer() {
		return info.getInitializer();
	}

	// -- UIDetails methods --

	@Override
	public String getTitle() {
		return info.getTitle();
	}

	@Override
	public MenuPath getMenuPath() {
		return info.getMenuPath();
	}

	@Override
	public String getMenuRoot() {
		return info.getMenuRoot();
	}

	@Override
	public String getIconPath() {
		return info.getIconPath();
	}

	@Override
	public double getPriority() {
		return info.getPriority();
	}

	@Override
	public boolean isSelectable() {
		return info.isSelectable();
	}

	@Override
	public String getSelectionGroup() {
		return info.getSelectionGroup();
	}

	@Override
	public boolean isSelected() {
		return info.isSelected();
	}

	@Override
	public boolean isEnabled() {
		return info.isEnabled();
	}

	@Override
	public void setMenuPath(final MenuPath menuPath) {
		info.setMenuPath(menuPath);
	}

	@Override
	public void setMenuRoot(final String menuRoot) {
		info.setMenuRoot(menuRoot);
	}

	@Override
	public void setIconPath(final String iconPath) {
		info.setIconPath(iconPath);
	}

	@Override
	public void setPriority(final double priority) {
		info.setPriority(priority);
	}

	@Override
	public void setEnabled(final boolean enabled) {
		info.setEnabled(enabled);
	}

	@Override
	public void setSelectable(final boolean selectable) {
		info.setSelectable(selectable);
	}

	@Override
	public void setSelectionGroup(final String selectionGroup) {
		info.setSelectionGroup(selectionGroup);
	}

	@Override
	public void setSelected(final boolean selected) {
		info.setSelected(selected);
	}

	// -- BasicDetails methods --

	@Override
	public String getName() {
		return info.getName();
	}

	@Override
	public String getLabel() {
		return info.getLabel();
	}

	@Override
	public String getDescription() {
		return info.getDescription();
	}

	@Override
	public void setName(final String name) {
		info.setName(name);
	}

	@Override
	public void setLabel(final String label) {
		info.setLabel(label);
	}

	@Override
	public void setDescription(final String description) {
		info.setDescription(description);
	}

	// -- Helper methods --

	/**
	 * Copies any inputs from the adapted {@link PluginInfo}. This step allows
	 * {@link DynamicPlugin}s to mix and match @{@link Parameter} annotations with
	 * inputs dynamically generated at runtime.
	 */
	private void populateItems() {
		for (final ModuleItem<?> item : info.inputs()) {
			addInput(copy(item));
		}
		for (final ModuleItem<?> item : info.outputs()) {
			addOutput(copy(item));
		}
	}

	/** Creates a mutable copy of the given module item. */
	private <T> DefaultModuleItem<T> copy(final ModuleItem<T> item) {
		return new DefaultModuleItem<T>(this, item);
	}

}
