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

package imagej.legacy.plugin;

import java.io.File;

import net.imagej.ui.swing.script.TextEditor;

import org.scijava.Context;
import org.scijava.Priority;
import org.scijava.event.EventHandler;
import org.scijava.platform.event.AppQuitEvent;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * The default {@link LegacyEditor} plugin.
 * <p>
 * When the {@link imagej.legacy.DefaultLegacyHooks} are installed, the
 * {@link LegacyEditor} plugin (if any) is given a chance to handle editor
 * requests from ImageJ 1.x. Let's have a sensible default implementation.
 * </p>
 * 
 * @author Johannes Schindelin
 */
@Plugin(type = LegacyEditor.class, priority = Priority.LOW_PRIORITY)
public class DefaultLegacyEditor implements LegacyEditor {

	@Parameter
	private Context context;

	private TextEditor editor;

	private synchronized TextEditor editor() {
		if (editor == null) {
			editor = new TextEditor(context) {
				private static final long serialVersionUID = 1L;

				@Override
				public void dispose() {
					synchronized (DefaultLegacyEditor.this) {
						editor = null;
						super.dispose();
					}
				}
			};
			editor.setVisible(true);
		}
		return editor;
	}

	@EventHandler
	private synchronized void onEvent(final AppQuitEvent event) {
		if (editor != null) {
			editor.dispose();
			editor = null;
		}
	}

	/** @inherit */
	@Override
	public boolean open(File file) {
		editor().open(file);
		return true;
	}

	/** @inherit */
	@Override
	public boolean create(String title, String content) {
		editor().createNewDocument(title, content);
		return true;
	}

}
