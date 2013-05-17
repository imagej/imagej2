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

package imagej.command;

import imagej.ValidityProblem;
import imagej.module.DefaultMutableModuleInfo;
import imagej.module.DefaultMutableModuleItem;
import imagej.module.ModuleInfo;
import imagej.module.ModuleItem;
import imagej.module.MutableModuleInfo;
import imagej.module.MutableModuleItem;

import java.lang.reflect.Field;
import java.util.List;

import org.scijava.MenuPath;
import org.scijava.UIDetails;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Helper class for maintaining a {@link DynamicCommand}'s associated
 * {@link ModuleInfo}.
 * <p>
 * The {@link CommandService} has a plain {@link CommandInfo} object in its
 * index, populated from the {@link DynamicCommand}'s @{@link Plugin}
 * annotation. So this class adapts that object, delegating to it for the
 * {@link UIDetails} methods. The plain {@link CommandInfo} cannot be used
 * as-is, however, because we need to override the {@link ModuleInfo} methods as
 * well as provide metadata manipulation functionality such as
 * {@link MutableModuleInfo#addInput(ModuleItem)}.
 * </p>
 * 
 * @author Curtis Rueden
 */
public class DynamicCommandInfo extends DefaultMutableModuleInfo {

	private final CommandInfo info;

	public DynamicCommandInfo(final CommandInfo info,
		final Class<? extends DynamicCommand> moduleClass)
	{
		this.info = info;
		setModuleClass(moduleClass);
		populateItems();
	}

	// -- DynamicCommandInfo methods --

	/**
	 * Gets the mutable input with the given name and type.
	 * <p>
	 * If the input is not mutable (i.e., a {@link MutableModuleItem}), a
	 * {@link ClassCastException} will be thrown. However, this method is always
	 * safe to call for inputs declared using the @{@link Parameter} notation of
	 * {@link Command}s; it is only unsafe when called to retrieve inputs added
	 * dynamically using {@link #addInput(ModuleItem)}, where the
	 * {@link ModuleItem} in question was of unknown (i.e., potentially
	 * non-mutable) origin.
	 * </p>
	 * 
	 * @throws ClassCastException if input is not a {@link MutableModuleItem}.
	 */
	public <T> MutableModuleItem<T> getMutableInput(final String name,
		final Class<T> type)
	{
		return (MutableModuleItem<T>) getInput(name, type);
	}

	/**
	 * Gets the mutable output with the given name and type.
	 * <p>
	 * If the output is not mutable (i.e., a {@link MutableModuleItem}), a
	 * {@link ClassCastException} will be thrown. However, this method is always
	 * safe to call for outputs declared using the @{@link Parameter} notation of
	 * {@link Command}s; it is only unsafe when called to retrieve outputs added
	 * dynamically using {@link #addInput(ModuleItem)}, where the
	 * {@link ModuleItem} in question was of unknown (i.e., potentially
	 * non-mutable) origin.
	 * </p>
	 * 
	 * @throws ClassCastException if output is not a {@link MutableModuleItem}.
	 */
	public <T> MutableModuleItem<T> getMutableOutput(final String name,
		final Class<T> type)
	{
		return (MutableModuleItem<T>) getOutput(name, type);
	}

	// -- Internal methods --

	/**
	 * Gets the {@link Field} corresponding to the given @{@link Parameter}
	 * annotated module input, or null if the input does not exist or was not
	 * declared using the @{@link Parameter} mechanism.
	 */
	protected Field getInputField(final String name) {
		final CommandModuleItem<?> item = info.getInput(name);
		return item == null ? null : item.getField();
	}

	/**
	 * Gets the {@link Field} corresponding to the given @{@link Parameter}
	 * annotated module output, or null if the output does not exist or was not
	 * declared using the @{@link Parameter} mechanism.
	 */
	protected Field getOutputField(final String name) {
		final CommandModuleItem<?> item = info.getOutput(name);
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

	// -- Validated methods --

	@Override
	public boolean isValid() {
		return info.isValid();
	}

	@Override
	public List<ValidityProblem> getProblems() {
		return info.getProblems();
	}

	// -- Helper methods --

	/**
	 * Copies any inputs from the adapted {@link CommandInfo}. This step allows
	 * {@link DynamicCommand}s to mix and match @{@link Parameter} annotations
	 * with inputs dynamically generated at runtime.
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
	private <T> DefaultMutableModuleItem<T> copy(final ModuleItem<T> item) {
		return new DefaultMutableModuleItem<T>(this, item);
	}

}
