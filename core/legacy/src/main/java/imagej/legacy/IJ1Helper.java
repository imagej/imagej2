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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.legacy;

import ij.Executer;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.Macro;
import ij.WindowManager;
import ij.gui.DialogListener;
import ij.gui.ImageWindow;
import ij.io.Opener;
import ij.plugin.filter.PlugInFilterRunner;
import imagej.data.display.ImageDisplay;
import imagej.platform.event.AppAboutEvent;
import imagej.platform.event.AppOpenFilesEvent;
import imagej.platform.event.AppPreferencesEvent;
import imagej.platform.event.AppQuitEvent;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.FocusEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.scijava.AbstractContextual;
import org.scijava.Context;
import org.scijava.event.EventHandler;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

/**
 * A helper class to interact with ImageJ 1.x.
 * <p>
 * The DefaultLegacyService needs to patch ImageJ 1.x's classes before they are
 * loaded. Unfortunately, this is tricky: if the DefaultLegacyService already
 * uses those classes, it is a matter of luck whether we can get the patches in
 * before those classes are loaded.
 * </p>
 * <p>
 * Therefore, we put as much interaction with ImageJ 1.x as possible into this
 * class and keep a reference to it in the DefaultLegacyService.
 * </p>
 * 
 * @author Johannes Schindelin
 */
public class IJ1Helper extends AbstractContextual {

	/** A reference to the legacy service, just in case we need it */
	private final DefaultLegacyService legacyService;

	@Parameter
	private LogService log;

	public IJ1Helper(final DefaultLegacyService legacyService) {
		setContext(legacyService.getContext());
		this.legacyService = legacyService;
	}

	public void initialize() {
		// initialize legacy ImageJ application
		if (IJ.getInstance() == null) try {
			if (GraphicsEnvironment.isHeadless()) {
				IJ.runPlugIn("ij.IJ.init", null);
			} else {
				new ImageJ(ImageJ.NO_SHOW);
			}
		}
		catch (final Throwable t) {
			log.warn("Failed to instantiate IJ1.", t);
		} else {
			final LegacyImageMap imageMap = legacyService.getImageMap();
			for (int i = 1; i <= WindowManager.getImageCount(); i++) {
				imageMap.registerLegacyImage(WindowManager.getImage(i));
			}
		}
	}

	public void dispose() {
		final ImageJ ij = IJ.getInstance();
		if (ij != null) {
			// close out all image windows, without dialog prompts
			while (true) {
				final ImagePlus imp = WindowManager.getCurrentImage();
				if (imp == null) break;
				imp.changes = false;
				imp.close();
			}

			// close any remaining (non-image) windows
			WindowManager.closeAllWindows();

			// quit legacy ImageJ on the same thread
			ij.run();
		}
	}

	public void setVisible(boolean toggle) {
		final ImageJ ij = IJ.getInstance();
		if (ij != null) {
			if (toggle) ij.pack();
			ij.setVisible(toggle);
		}

		// hide/show the legacy ImagePlus instances
		final LegacyImageMap imageMap = legacyService.getImageMap();
		for (final ImagePlus imp : imageMap.getImagePlusInstances()) {
			final ImageWindow window = imp.getWindow();
			if (window != null) window.setVisible(toggle);
		}
	}

	public void syncActiveImage(final ImageDisplay activeDisplay) {
		final LegacyImageMap imageMap = legacyService.getImageMap();
		final ImagePlus activeImagePlus = imageMap.lookupImagePlus(activeDisplay);
		// NB - old way - caused probs with 3d Project
		// WindowManager.setTempCurrentImage(activeImagePlus);
		// NB - new way - test thoroughly
		if (activeImagePlus == null) WindowManager.setCurrentWindow(null);
		else WindowManager.setCurrentWindow(activeImagePlus.getWindow());
	}

	public void setKeyDown(int keyCode) {
		IJ.setKeyDown(keyCode);
	}

	public void setKeyUp(int keyCode) {
		IJ.setKeyUp(keyCode);
	}

	public boolean hasInstance() {
		return IJ.getInstance() != null;
	}

	public String getVersion() {
		return IJ.getVersion();
	}

	public boolean isMacintosh() {
		return IJ.isMacintosh();
	}

	/**
	 * Delegator for {@link IJ#getClassLoader()}.
	 * 
	 * <p>
	 * This method allows the {@link LegacyExtensions} class to be loaded
	 * without loading any of ImageJ 1.x.
	 * </p>
	 * 
	 * @return ImageJ 1.x' current plugin class loader
	 */
	public static ClassLoader getClassLoader() {
		return IJ.getClassLoader();
	}

	/**
	 * Delegator for {@link IJ#log(String)}.
	 * 
	 * <p>
	 * This method allows the {@link LegacyExtensions} class to be loaded
	 * without loading any of ImageJ 1.x.
	 * </p>
	 */
	public static void log(final String message) {
		IJ.log(message);
	}

	/**
	 * Delegator for {@link IJ#error(String)}.
	 * 
	 * <p>
	 * This method allows the {@link LegacyExtensions} class to be loaded
	 * without loading any of ImageJ 1.x.
	 * </p>
	 */
	public static void error(final String message) {
		IJ.log(message);
	}

	/**
	 * Delegator for {@link IJ#runPlugIn(String, String)}.
	 * <p>
	 * This method allows the {@link DummyLegacyService} class to be loaded
	 * without loading any of ImageJ 1.x.
	 * </p>
	 */
	public static void runIJ1PlugIn(final String className, final String arg) {
		IJ.runPlugIn(className, arg);
	}

	/**
	 * Gets a macro parameter of type <i>boolean</i>.
	 * 
	 * @param label
	 *            the name of the macro parameter
	 * @param defaultValue
	 *            the default value
	 * @return the boolean value
	 */
	public static boolean getMacroParameter(String label, boolean defaultValue) {
		return getMacroParameter(label) != null || defaultValue;
	}

	/**
	 * Gets a macro parameter of type <i>double</i>.
	 * 
	 * @param label
	 *            the name of the macro parameter
	 * @param defaultValue
	 *            the default value
	 * @return the double value
	 */
	public static double getMacroParameter(String label, double defaultValue) {
		String value = Macro.getValue(Macro.getOptions(), label, null);
		return value != null ? Double.parseDouble(value) : defaultValue;
	}

	/**
	 * Gets a macro parameter of type {@link String}.
	 * 
	 * @param label
	 *            the name of the macro parameter
	 * @param defaultValue
	 *            the default value
	 * @return the value
	 */
	public static String getMacroParameter(String label, String defaultValue) {
		return Macro.getValue(Macro.getOptions(), label, defaultValue);
	}

	/**
	 * Gets a macro parameter of type {@link String}.
	 * 
	 * @param label
	 *            the name of the macro parameter
	 * @return the value, <code>null</code> if the parameter was not specified
	 */
	public static String getMacroParameter(String label) {
		return Macro.getValue(Macro.getOptions(), label, null);
	}

	public static class LegacyGenericDialog {
		protected List<Double> numbers;
		protected List<String> strings;
		protected List<Boolean> checkboxes;
		protected List<String> choices;
		protected List<Integer> choiceIndices;
		protected String textArea1, textArea2;
		protected List<String> radioButtons;

		protected int numberfieldIndex = 0, stringfieldIndex = 0, checkboxIndex = 0, choiceIndex = 0, textAreaIndex = 0, radioButtonIndex = 0;
		protected boolean invalidNumber;
		protected String errorMessage;

		public LegacyGenericDialog() {
			if (Macro.getOptions() == null)
				throw new RuntimeException("Cannot instantiate headless dialog except in macro mode");
			numbers = new ArrayList<Double>();
			strings = new ArrayList<String>();
			checkboxes = new ArrayList<Boolean>();
			choices = new ArrayList<String>();
			choiceIndices = new ArrayList<Integer>();
			radioButtons = new ArrayList<String>();
		}

		public void addCheckbox(String label, boolean defaultValue) {
			checkboxes.add(getMacroParameter(label, defaultValue));
		}

		@SuppressWarnings("unused")
		public void addCheckboxGroup(int rows, int columns, String[] labels, boolean[] defaultValues) {
			for (int i = 0; i < labels.length; i++)
				addCheckbox(labels[i], defaultValues[i]);
		}

		@SuppressWarnings("unused")
		public void addCheckboxGroup(int rows, int columns, String[] labels, boolean[] defaultValues, String[] headings) {
			addCheckboxGroup(rows, columns, labels, defaultValues);
		}

		public void addChoice(String label, String[] items, String defaultItem) {
			String item = getMacroParameter(label, defaultItem);
			int index = 0;
			for (int i = 0; i < items.length; i++)
				if (items[i].equals(item)) {
					index = i;
					break;
				}
			choiceIndices.add(index);
			choices.add(items[index]);
		}

		@SuppressWarnings("unused")
		public void addNumericField(String label, double defaultValue, int digits) {
			numbers.add(getMacroParameter(label, defaultValue));
		}

		@SuppressWarnings("unused")
		public void addNumericField(String label, double defaultValue, int digits, int columns, String units) {
			addNumericField(label, defaultValue, digits);
		}

		@SuppressWarnings("unused")
		public void addSlider(String label, double minValue, double maxValue, double defaultValue) {
			numbers.add(getMacroParameter(label, defaultValue));
		}

		public void addStringField(String label, String defaultText) {
			strings.add(getMacroParameter(label, defaultText));
		}

		@SuppressWarnings("unused")
		public void addStringField(String label, String defaultText, int columns) {
			addStringField(label, defaultText);
		}

		@SuppressWarnings("unused")
		public void addTextAreas(String text1, String text2, int rows, int columns) {
			textArea1 = text1;
			textArea2 = text2;
		}

		public boolean getNextBoolean() {
			return checkboxes.get(checkboxIndex++);
		}

		public String getNextChoice() {
			return choices.get(choiceIndex++);
		}

		public int getNextChoiceIndex() {
			return choiceIndices.get(choiceIndex++);
		}

		public double getNextNumber() {
			return numbers.get(numberfieldIndex++);
		}

		/** Returns the contents of the next text field. */
		public String getNextString() {
			return strings.get(stringfieldIndex++);
		}

		public String getNextText()  {
			switch (textAreaIndex++) {
			case 0:
				return textArea1;
			case 1:
				return textArea2;
			}
			return null;
		}

		/** Adds a radio button group. */
		@SuppressWarnings("unused")
		public void addRadioButtonGroup(String label, String[] items, int rows, int columns, String defaultItem) {
			radioButtons.add(getMacroParameter(label, defaultItem));
		}

		public List<String> getRadioButtonGroups() {
			return radioButtons;
		}

		/** Returns the selected item in the next radio button group. */
		public String getNextRadioButton() {
			return radioButtons.get(radioButtonIndex++);
		}

		public boolean invalidNumber() {
			boolean wasInvalid = invalidNumber;
			invalidNumber = false;
			return wasInvalid;
		}

		public void showDialog() {
			if (Macro.getOptions() == null)
				throw new RuntimeException("Cannot run dialog headlessly");
			numberfieldIndex = 0;
			stringfieldIndex = 0;
			checkboxIndex = 0;
			choiceIndex = 0;
			textAreaIndex = 0;
		}

		public boolean wasCanceled() {
			return false;
		}

		public boolean wasOKed() {
			return true;
		}

		public void dispose() {}
		@SuppressWarnings("unused")
		public void addDialogListener(DialogListener dl) {}
		@SuppressWarnings("unused")
		public void addHelp(String url) {}
		@SuppressWarnings("unused")
		public void addMessage(String text) {}
		@SuppressWarnings("unused")
		public void addMessage(String text, Font font) {}
		@SuppressWarnings("unused")
		public void addMessage(String text, Font font, Color color) {}
		@SuppressWarnings("unused")
		public void addPanel(Panel panel) {}
		@SuppressWarnings("unused")
		public void addPanel(Panel panel, int contraints, Insets insets) {}
		@SuppressWarnings("unused")
		public void addPreviewCheckbox(PlugInFilterRunner pfr) {}
		@SuppressWarnings("unused")
		public void addPreviewCheckbox(PlugInFilterRunner pfr, String label) {}
		@SuppressWarnings("unused")
		public void centerDialog(boolean b) {}
		public void enableYesNoCancel() {}
		@SuppressWarnings("unused")
		public void enableYesNoCancel(String yesLabel, String noLabel) {}
		@SuppressWarnings("unused")
		public void focusGained(FocusEvent e) {}
		@SuppressWarnings("unused")
		public void focusLost(FocusEvent e) {}
		public Button[] getButtons() { return null; }
		public Vector<?> getCheckboxes() { return null; }
		public Vector<?> getChoices() { return null; }
		public String getErrorMessage() { return errorMessage; }
		public Insets getInsets() { return null; }
		public Component getMessage() { return null; }
		public Vector<?> getNumericFields() { return null; }
		public Checkbox getPreviewCheckbox() { return null; }
		public Vector<?> getSliders() { return null; }
		public Vector<?> getStringFields() { return null; }
		public TextArea getTextArea1() { return null; }
		public TextArea getTextArea2() { return null; }
		public void hideCancelButton() {}
		@SuppressWarnings("unused")
		public void previewRunning(boolean isRunning) {}
		@SuppressWarnings("unused")
		public void setEchoChar(char echoChar) {}
		@SuppressWarnings("unused")
		public void setHelpLabel(String label) {}
		@SuppressWarnings("unused")
		public void setInsets(int top, int left, int bottom) {}
		@SuppressWarnings("unused")
		public void setOKLabel(String label) {}
		protected void setup() {}
		public void accessTextFields() {}
		public void showHelp() {}
	}

	/**
	 * Replacement for ImageJ 1.x' MacAdapter.
	 * <p>
	 * ImageJ 1.x has a MacAdapter plugin that intercepts MacOSX-specific events
	 * and handles them. The way it does it is deprecated now, however, and
	 * unfortunately incompatible with the way ImageJ 2's platform service does
	 * it.
	 * </p>
	 * <p>
	 * This class implements the same functionality as the MacAdapter, but in a
	 * way that is compatible with ImageJ 2's platform service.
	 * </p>
	 * @author Johannes Schindelin
	 */
	private static class LegacyEventDelegator extends AbstractContextual {

		@Parameter(required = false)
		private LegacyService legacyService;

		// -- MacAdapter re-implementations --

		@EventHandler
		private void onEvent(@SuppressWarnings("unused") final AppAboutEvent event)
		{
			if (isLegacyMode()) {
				IJ.run("About ImageJ...");
			}
		}

		@EventHandler
		private void onEvent(final AppOpenFilesEvent event) {
			if (isLegacyMode()) {
				final List<File> files = new ArrayList<File>(event.getFiles());
				for (final File file : files) {
					new Opener().openAndAddToRecent(file.getAbsolutePath());
				}
			}
		}

		@EventHandler
		private void onEvent(@SuppressWarnings("unused") final AppQuitEvent event) {
			if (isLegacyMode()) {
				new Executer("Quit", null); // works with the CommandListener
			}
		}

		@EventHandler
		private void onEvent(
			@SuppressWarnings("unused") final AppPreferencesEvent event)
		{
			if (isLegacyMode()) {
				IJ.error("The ImageJ preferences are in the Edit>Options menu.");
			}
		}

		private boolean isLegacyMode() {
			// We call setContext() indirectly from DefaultLegacyService#initialize,
			// therefore legacyService might still be null at this point even if the
			// context knows a legacy service now.
			if (legacyService == null) {
				final Context context = getContext();
				if (context != null) legacyService = context.getService(LegacyService.class);
			}
			return legacyService != null && legacyService.isLegacyMode();
		}

	}

	private static LegacyEventDelegator eventDelegator;

	public static void subscribeEvents(final Context context) {
		if (context == null) {
			eventDelegator = null;
		} else {
			eventDelegator = new LegacyEventDelegator();
			eventDelegator.setContext(context);
		}
	}

}
