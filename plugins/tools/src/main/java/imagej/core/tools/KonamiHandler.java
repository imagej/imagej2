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

package imagej.core.tools;

import imagej.command.CommandService;
import imagej.display.event.input.KyPressedEvent;
import imagej.tool.AbstractTool;
import imagej.tool.Tool;

import org.scijava.Priority;
import org.scijava.input.KeyCode;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;
import org.scijava.thread.ThreadService;

/**
 * Oh, the nostalgia!
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Tool.class, name = "Konami",
	priority = Priority.FIRST_PRIORITY,
	attrs = { @Attr(name = Tool.ALWAYS_ACTIVE) })
public class KonamiHandler extends AbstractTool implements Runnable {

	private static final KeyCode[] CODE = { KeyCode.UP, KeyCode.UP, KeyCode.DOWN,
		KeyCode.DOWN, KeyCode.LEFT, KeyCode.RIGHT, KeyCode.LEFT, KeyCode.RIGHT,
		KeyCode.B, KeyCode.A };

	private static final String JINGLE =
		"T100 L32 B > C E G B > C E C < B G E C < L8 B";

	private static final String COMMAND = "imagej.core.commands.app.EasterEgg";

	private int index = 0;

	@Override
	public void onKeyDown(final KyPressedEvent evt) {
		if (evt.getCode() == CODE[index]) {
			index++;
			if (index > CODE.length - 2) evt.consume();
			if (index == CODE.length) {
				index = 0;
				final ThreadService threadService =
					getContext().getService(ThreadService.class);
				threadService.run(this);
				final CommandService commandService =
					getContext().getService(CommandService.class);
				commandService.run(COMMAND);
			}
		}
		else index = 0;
	}

	@Override
	public void run() {
		new TunePlayer().play(JINGLE);
	}

}
