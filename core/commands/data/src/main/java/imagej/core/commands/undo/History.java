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

package imagej.core.commands.undo;

import imagej.command.Command;
import imagej.command.CommandService;

import java.awt.Toolkit;
import java.util.LinkedList;
import java.util.Map;


/**
 * Package access class used internally by UndoService to record and undo
 * a history of commands.
 * 
 * @author Barry DeZonia
 *
 */
class History {
	private final UndoService undoService;
	private final CommandService commandService;
	private final int maxHistory;
	private final LinkedList<Class<? extends Command>> undoableCommands;
	private final LinkedList<Class<? extends Command>> redoableCommands;
	private final LinkedList<Class<? extends Command>> tmpCommands;
	private final LinkedList<Map<String,Object>> undoableInputs;
	private final LinkedList<Map<String,Object>> redoableInputs;
	private final LinkedList<Map<String,Object>> tmpInputs;

	History(UndoService uSrv, CommandService cSrv, int maxSteps) {
		undoService = uSrv;
		commandService = cSrv;
		maxHistory = maxSteps;
		undoableCommands = new LinkedList<Class<? extends Command>>();
		redoableCommands = new LinkedList<Class<? extends Command>>();
		tmpCommands = new LinkedList<Class<? extends Command>>();
		undoableInputs = new LinkedList<Map<String,Object>>();
		redoableInputs = new LinkedList<Map<String,Object>>();
		tmpInputs = new LinkedList<Map<String,Object>>();
	}
	
	void doUndo() {
		//System.out.println("doUndo() : undoPos = "+undoPos+" redoPos = "+redoPos);
		if (undoableCommands.size() <= 0) {
			// TODO eliminate AWT dependency with a BeepService!
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		Class<? extends Command> command = redoableCommands.removeLast();
		Map<String,Object> inputs = redoableInputs.removeLast();
		tmpCommands.add(command);
		tmpInputs.add(inputs);
		command = undoableCommands.removeLast();
		inputs = undoableInputs.removeLast();
		/* tricky attempt 2
		 * ignore(commandService.run(command, inputs));
		 */
		undoService.ignore(command);
		commandService.run(command, inputs);
	}
	
	void doRedo() {
		//System.out.println("doRedo() : undoPos = "+undoPos+" redoPos = "+redoPos);
		if (tmpCommands.size() <= 0) {
			// TODO eliminate AWT dependency with a BeepService!
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		Class<? extends Command> command = tmpCommands.getLast();
		Map<String,Object> input = tmpInputs.getLast();
		commandService.run(command, input);
	}
	
	void clear() {
		undoableCommands.clear();
		redoableCommands.clear();
		tmpCommands.clear();
		undoableInputs.clear();
		redoableInputs.clear();
		tmpInputs.clear();
	}
	
	void addUndo(Class<? extends Command> command, Map<String,Object> inputs) {
		/*  tricky attempt to make this code ignore prerecorded commands safely
		inputs.put(RECORDED_INTERNALLY, RECORDED_INTERNALLY);
		*/
		undoableCommands.add(command);
		undoableInputs.add(inputs);
		if (undoableCommands.size() > maxHistory) removeOldestUndo();
	}
	
	void addRedo(Class<? extends Command> command, Map<String,Object> inputs) {
		/*  tricky attempt to make this code ignore prerecorded commands safely
		inputs.put(RECORDED_INTERNALLY, RECORDED_INTERNALLY);
		*/
		if (tmpCommands.size() > 0) {
			if (tmpCommands.getLast().equals(command) &&
					tmpInputs.getLast().equals(inputs))
			{
				tmpCommands.removeLast();
				tmpInputs.removeLast();
			}
			else {
				tmpCommands.clear();
				tmpInputs.clear();
			}
		}
		redoableCommands.add(command);
		redoableInputs.add(inputs);
		if (redoableCommands.size() > maxHistory) removeOldestRedo();
	}
	
	void removeNewestUndo() {
		undoableCommands.removeLast();
		undoableInputs.removeLast();
	}

	void removeNewestRedo() {
		redoableCommands.removeLast();
		redoableInputs.removeLast();
	}
	
	void removeOldestUndo() {
		undoableCommands.removeFirst();
		undoableInputs.removeFirst();
	}

	void removeOldestRedo() {
		redoableCommands.removeFirst();
		redoableInputs.removeFirst();
	}

}
