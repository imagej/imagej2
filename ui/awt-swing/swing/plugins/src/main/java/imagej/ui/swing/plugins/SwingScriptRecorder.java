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

package imagej.ui.swing.plugins;

import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.event.EventSubscriber;
import imagej.event.ImageJEvent;
import imagej.ext.module.Module;
import imagej.ext.module.ModuleInfo;
import imagej.ext.module.ModuleItem;
import imagej.ext.module.event.ModuleEvent;
import imagej.ext.module.event.ModuleExecutedEvent;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.ext.script.CodeGenerator;
import imagej.ext.script.CodeGeneratorJava;
import imagej.ext.script.InvocationObject;
import imagej.ext.script.ParameterObject;
import imagej.ui.DialogPrompt;
import imagej.ui.UIService;
import imagej.ui.swing.StaticSwingUtils;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

/**
 * Script Recorder.
 * 
 * @author Grant Harris
 */
@Plugin(menu = { @Menu(label = "Plugins"), @Menu(label = "Macros"),
	@Menu(label = "Record...", weight = 4) })
public class SwingScriptRecorder implements ImageJPlugin {

	@Parameter(persist = false)
	private EventService eventService;

	@Parameter(persist = false)
	private UIService uiService;

	// private static SwingOutputWindow window;
	JTextArea textArea = new JTextArea();

	@Override
	public void run() {
		createFrame();
	}

	private boolean recording = false;

	boolean isRecording() {
		return recording;
	}

	void setRecording(final boolean t) {
		recording = t;
	}

	void createFrame() {
		final JFrame frame = new JFrame("Recorder");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		textArea.setEditable(false);
		textArea.setRows(20);
		textArea.setColumns(50);
		final java.awt.Font font =
			new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12);
		textArea.setFont(font);
		frame.getContentPane().add(new JScrollPane(textArea), BorderLayout.CENTER);

		final JButton button = new JButton();

		button.setText("Start");
		button.addActionListener(new ActionListener() {

			@Override
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(final ActionEvent e) {
				if (!isRecording()) {
					startRecording();
					button.setText("Stop");
					setRecording(true);
					emitMessage("Started recording...");
				}
				else {
					setRecording(false);
					stopRecording();
					emitMessage("Stopped recording.");
				}
			}

		});
		frame.getContentPane().add(button, BorderLayout.NORTH);
		frame.setBounds(new Rectangle(400, 400, 700, 300));
		frame.setVisible(true);
		StaticSwingUtils.locateUpperRight(frame);
	}

	private void startRecording() {
		eventService.subscribe(this);
	}

	private void stopRecording() {
		eventService.unsubscribe(subscribers);
		promptForGenerate();
	}

	private List<EventSubscriber<?>> subscribers;

	public void append(final String text) {
		textArea.append(text);
		// Make sure the last line is always visible
		textArea.setCaretPosition(textArea.getDocument().getLength());
	}

	public void clear() {
		textArea.setText("");
	}

	@EventHandler
	protected void onEvent(final ImageJEvent evt) {
//		if (evt instanceof ImageJEvent) {
//			emitMessage("Event: " + evt.getClass());
//			showFields(evt);
//		} 
		if (ModuleEvent.class.isAssignableFrom(evt.getClass())) {
//			emitMessage(evt.getClass().getSimpleName());
//			if (evt instanceof ModulePreprocessEvent) {
//				final ModulePreprocessEvent e = (ModulePreprocessEvent) evt;
//				emitMessage("  >> PreProcessor: "
//						+ e.getProcessor().getClass().getSimpleName());
//			}
			if (evt instanceof ModuleExecutedEvent) {
				processModuleExecuted((ModuleExecutedEvent) evt);
			}
		}

//		if (evt instanceof OverlayCreatedEvent) {
//			Overlay overlay = ((OverlayCreatedEvent) evt).getObject();
//			processOverlayCreated(overlay);
//		}

//		if (evt instanceof DisplayEvent) {
//			if (!(evt instanceof MsEvent) && !(evt instanceof KyEvent)) {
//				showClass(evt);
//				showFields(evt);
//				emitMessage("    DisplayName: "
//						+ ((DisplayEvent) evt).getDisplay().getName());
//			}
//		}
//		if (evt instanceof DataViewEvent) {
//			showClass(evt);
//			showFields(evt);
//		}
//		if (evt instanceof ObjectEvent) {
//			showClass(evt);
//			showFields(evt);
//		}
//		if (evt instanceof ToolEvent) {
//			showClass(evt);
//			showFields(evt);
//		}
	}

	ArrayList<InvocationObject> invocationList =
		new ArrayList<InvocationObject>();

	public void processModuleExecuted(final ModuleExecutedEvent evt) {
		final Module module = evt.getModule();
		final ModuleInfo info = module.getInfo();
		final String pluginCalled = info.getDelegateClassName();
		emitMessage("  >> Module: " + pluginCalled);
		final InvocationObject invocation = new InvocationObject(pluginCalled);
		// parameter inputs
		final Iterable<ModuleItem<?>> inputs = info.inputs();
		for (final ModuleItem<?> moduleItem : inputs) {

			final String param = moduleItem.getName();
			final Class<?> type = moduleItem.getType();
			final Object value = module.getInput(param);
			invocation.addParameter(param, type, value);
			emitMessage("    " + param + " = " + value.toString() + "  {" +
				type.getSimpleName() + "}");
		}
		addToInvocationList(invocation);
	}

//	private void processOverlayCreated(Overlay overlay) {
//		Inspector.inspect(overlay);
//		// overlayService.getOverlays(display);
//		if (overlay instanceof RectangleOverlay) {
//			final double[] origin = new double[2];
//			final double[] extent = new double[2];
//			((RectangleRegionOfInterest) overlay.getRegionOfInterest()).getExtent(extent);
//			((RectangleRegionOfInterest) overlay.getRegionOfInterest()).getOrigin(origin);
//			int minX = (int) origin[0];
//			int minY = (int) origin[1];
//			int maxX = (int) extent[0];
//			int maxY = (int) extent[1];
//			
//		}
//	}

	public void addToInvocationList(final InvocationObject invocation) {
		invocationList.add(invocation);
	}

	private void promptForGenerate() {
		final DialogPrompt.Result result =
			uiService.showDialog("Generate Code?", "Code Generator",
				DialogPrompt.MessageType.QUESTION_MESSAGE,
				DialogPrompt.OptionType.YES_NO_OPTION);
		if (result == DialogPrompt.Result.YES_OPTION) {
			System.out.println("That's a YES");
			generateCode();
		}
	}

	private void generateCode() {
		for (final InvocationObject invoked : invocationList) {
			// which script/language to translate to...
			// String scriptLang = Prefs.get(SettingsKeys.SCRIPT_LANG);
			final CodeGenerator cg = new CodeGeneratorJava();

			cg.invokeStatementBegin();
			cg.addModuleCalled(invoked.moduleCalled);

			final List<ParameterObject> params = invoked.parameterObjects;
			for (final ParameterObject parameterObject : params) {
				cg.addArgDelimiter();
				cg.addArgument(parameterObject);
			}
			cg.invokeStatementEnd();
			cg.statementTerminate();
			append("Code generated: \n");
			append(cg.getResult());
		}
	}

//===================================================================================	
	void emitMessage(final String msg) {
		append(msg + "\n");
	}

	public static String timeStamp() {
		final SimpleDateFormat formatter =
			new SimpleDateFormat("mm:ss.SS", Locale.getDefault());
		final Date currentDate = new Date();
		final String dateStr = formatter.format(currentDate);
		return dateStr;
	}

	void showClass(final ImageJEvent evt) {
		emitMessage("[" + timeStamp() + "] " + evt.getClass().getSimpleName());
	}

	public void showFields(final Object evt) {
		try {
			final ArrayList<Field> allFields = getAllFields(evt.getClass());
			for (final Field fld : allFields) {
				fld.setAccessible(true);
				emitMessage("    " + fld.getName() + "= " + fld.get(evt).toString());
				// emitMessage("    " + fld.getName() + "[" + fld.getType() + "] = " +
				// fld.get(evt).toString());
			}
		}
		catch (final Throwable e) {
			System.err.println(e);
		}
	}

	/**
	 * Return the set of fields declared at all level of class hierachy
	 */
	public ArrayList<Field> getAllFields(final Class<?> clazz) {
		return getAllFieldsRec(clazz, new ArrayList<Field>());
	}

	private ArrayList<Field> getAllFieldsRec(final Class<?> clazz,
		final ArrayList<Field> ArrayList)
	{
		final Class<?> superClazz = clazz.getSuperclass();
		if (superClazz != null) {
			getAllFieldsRec(superClazz, ArrayList);
		}
		ArrayList.addAll(Arrays.asList(clazz.getDeclaredFields()));
		return ArrayList;
	}

}
