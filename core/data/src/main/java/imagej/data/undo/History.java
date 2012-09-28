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

package imagej.data.undo;

import imagej.command.CommandService;
import imagej.command.CompleteCommand;

import java.awt.Toolkit;
import java.util.LinkedList;


/**
 * Package access class used internally by UndoService to record and undo
 * a history of commands.
 * 
 * @author Barry DeZonia
 *
 */
class History {

	// -- constants --
	
	private static final int MIN_USAGE = 500;
	
	// -- instance variables --
	
	private final UndoService undoService;
	private final CommandService commandService;
	private final long maxMemUsage;
	private final LinkedList<CompleteCommand> undoableCommands;
	private final LinkedList<CompleteCommand> redoableCommands;
	private final LinkedList<CompleteCommand> transitionCommands;

	// -- constructor --
	
	History(UndoService uSrv, CommandService cSrv, long maxMem) {
		undoService = uSrv;
		commandService = cSrv;
		maxMemUsage = maxMem;
		undoableCommands = new LinkedList<CompleteCommand>();
		redoableCommands = new LinkedList<CompleteCommand>();
		transitionCommands = new LinkedList<CompleteCommand>();
	}
	
	// -- api to be used externally --
	
	void doUndo() {
		//System.out.println("doUndo() : undoPos = "+undoPos+" redoPos = "+redoPos);
		if (undoableCommands.size() <= 0) {
			// TODO eliminate AWT dependency with a BeepService!
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		CompleteCommand command = redoableCommands.removeLast();
		transitionCommands.add(command);
		command = undoableCommands.removeLast();
		/* tricky attempt 2
		 * ignore(commandService.run(command, inputs));
		 */
		undoService.ignore(command.getCommand());
		commandService.run(command.getCommand(), command.getInputs());
	}
	
	void doRedo() {
		//System.out.println("doRedo() : undoPos = "+undoPos+" redoPos = "+redoPos);
		if (transitionCommands.size() <= 0) {
			// TODO eliminate AWT dependency with a BeepService!
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		CompleteCommand command = transitionCommands.getLast();
		commandService.run(command.getCommand(), command.getInputs());
	}
	
	void clear() {
		undoableCommands.clear();
		redoableCommands.clear();
		transitionCommands.clear();
	}
	
	void addUndo(CompleteCommand command) {
		/*  tricky attempt to make this code ignore prerecorded commands safely
		inputs.put(RECORDED_INTERNALLY, RECORDED_INTERNALLY);
		*/
		long additionalSpace = MIN_USAGE + command.getMemoryUsage();
		while (((undoableCommands.size() > 0) || (redoableCommands.size() > 0)) &&
				(spaceUsed() + additionalSpace > maxMemUsage)) {
			if (undoableCommands.size() > 0) removeOldestUndo();
			if (redoableCommands.size() > 0) removeOldestRedo();
			// TODO - what about transitionCommands???
		}
		// at this point we have enough space or no history has been stored
		undoableCommands.add(command);
	}
	
	void addRedo(CompleteCommand command) {
		/*  tricky attempt to make this code ignore prerecorded commands safely
		inputs.put(RECORDED_INTERNALLY, RECORDED_INTERNALLY);
		*/
		if (transitionCommands.size() > 0) {
			if (transitionCommands.getLast().getCommand().equals(command.getCommand()) &&
					transitionCommands.getLast().getInputs().equals(command.getInputs()))
			{
				transitionCommands.removeLast();
			}
			else {
				transitionCommands.clear();
			}
		}
		long additionalSpace = MIN_USAGE + command.getMemoryUsage();
		while (((undoableCommands.size() > 0) || (redoableCommands.size() > 0)) &&
				(spaceUsed() + additionalSpace > maxMemUsage)) {
			if (undoableCommands.size() > 0) removeOldestUndo();
			if (redoableCommands.size() > 0) removeOldestRedo();
			// TODO - what about transitionCommands???
		}
		// at this point we have enough space or no history has been stored
		redoableCommands.add(command);
	}
	
	void removeNewestUndo() {
		undoableCommands.removeLast();
	}

	void removeNewestRedo() {
		redoableCommands.removeLast();
	}
	
	void removeOldestUndo() {
		undoableCommands.removeFirst();
	}

	void removeOldestRedo() {
		redoableCommands.removeFirst();
	}

	long spaceUsed() {
		long used = 0;
		for (CompleteCommand command : undoableCommands) {
			used += command.getMemoryUsage() + MIN_USAGE;
		}
		for (CompleteCommand command : redoableCommands) {
			used += command.getMemoryUsage() + MIN_USAGE;
		}
		for (CompleteCommand command : transitionCommands) {
			used += command.getMemoryUsage() + MIN_USAGE;
		}
		return used;
	}
}
