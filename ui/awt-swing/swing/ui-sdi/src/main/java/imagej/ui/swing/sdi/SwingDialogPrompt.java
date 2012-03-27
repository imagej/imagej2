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

package imagej.ui.swing.sdi;

import imagej.ui.DialogPrompt;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 * TODO
 * 
 * @author Grant Harris
 */
public class SwingDialogPrompt implements DialogPrompt {

	JDialog dialog;
	JOptionPane pane;

	public SwingDialogPrompt(final String message, final String title,
		final MessageType messageType, final OptionType optionType)
	{
		pane =new JOptionPane(message, msgMap.get(messageType), optionMap.get(optionType));
		// pane.set.Xxxx(...); // Configure
		dialog = pane.createDialog(null, title);
	}

	@Override
	public Result prompt() {
		dialog.setVisible(true);
		final Object selectedValue = pane.getValue();
		return resultMap.get(selectedValue);

	}

	/// Translate DialogPrompt types and results to JOptionPane types and results.

	static final Map<DialogPrompt.MessageType, Integer> msgMap =
		new HashMap<DialogPrompt.MessageType, Integer>();
	static final Map<DialogPrompt.OptionType, Integer> optionMap =
		new HashMap<DialogPrompt.OptionType, Integer>();
	static final Map<Integer, DialogPrompt.Result> resultMap =
		new HashMap<Integer, DialogPrompt.Result>();

	static {
		msgMap.put(DialogPrompt.MessageType.ERROR_MESSAGE,
			JOptionPane.ERROR_MESSAGE);
		msgMap.put(DialogPrompt.MessageType.INFORMATION_MESSAGE,
			JOptionPane.INFORMATION_MESSAGE);
		msgMap.put(DialogPrompt.MessageType.PLAIN_MESSAGE,
			JOptionPane.PLAIN_MESSAGE);
		msgMap.put(DialogPrompt.MessageType.WARNING_MESSAGE,
			JOptionPane.WARNING_MESSAGE);
		msgMap.put(DialogPrompt.MessageType.QUESTION_MESSAGE,
			JOptionPane.QUESTION_MESSAGE);
		//
		optionMap.put(DialogPrompt.OptionType.DEFAULT_OPTION,
			JOptionPane.DEFAULT_OPTION);
		optionMap.put(DialogPrompt.OptionType.OK_CANCEL_OPTION,
			JOptionPane.OK_CANCEL_OPTION);
		optionMap.put(DialogPrompt.OptionType.YES_NO_CANCEL_OPTION,
			JOptionPane.YES_NO_CANCEL_OPTION);
		optionMap.put(DialogPrompt.OptionType.YES_NO_OPTION,
			JOptionPane.YES_NO_OPTION);
		//
		resultMap
			.put(JOptionPane.CANCEL_OPTION, DialogPrompt.Result.CANCEL_OPTION);
		resultMap
			.put(JOptionPane.CLOSED_OPTION, DialogPrompt.Result.CLOSED_OPTION);
		resultMap.put(JOptionPane.NO_OPTION, DialogPrompt.Result.NO_OPTION);
		resultMap.put(JOptionPane.OK_OPTION, DialogPrompt.Result.OK_OPTION);
		resultMap.put(JOptionPane.YES_OPTION, DialogPrompt.Result.YES_OPTION);
	}

}
