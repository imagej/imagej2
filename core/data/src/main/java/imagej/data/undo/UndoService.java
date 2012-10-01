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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.ops.pointset.PointSetIterator;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import imagej.command.Command;
import imagej.command.CommandInfo;
import imagej.command.CommandService;
import imagej.command.InstantiableCommand;
import imagej.command.DefaultInstantiableCommand;
import imagej.command.InvertibleCommand;
import imagej.command.Unrecordable;
import imagej.data.Dataset;
import imagej.display.Display;
import imagej.display.DisplayService;
import imagej.display.DisplayState;
import imagej.display.SupportsUndo;
import imagej.display.event.DisplayDeletedEvent;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.module.Module;
import imagej.module.ModuleInfo;
import imagej.module.event.ModuleCanceledEvent;
import imagej.module.event.ModuleFinishedEvent;
import imagej.module.event.ModuleStartedEvent;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.service.AbstractService;
import imagej.service.Service;

// TODO
// This service is poorly named (recording something service is better)
// More undoable ops: zoom events, pan events, display creations/deletions,
//   setting of options values, dataset dimensions changing, setImgPlus(), etc.
// Also what about legacy plugin run results? (Multiple things hatched)
// Make friendly for multithreaded access.
// Currently command histories are tied to Displays. Should be made to support
//   histories of arbitrary objects. The objects would need to support some
//   duplicate/save/restore interface.
// Note that undoing a noise reducer plugin fails because that command runs two
//   plugins. They need to be grouped as one undoable command. Or break that
//   command into two separate plugins - one a neigh specifier that sets a value
//   of a new Options plugin. Info could show in menu eventually. But for now
//   just a command with 2d rect and n-d radial options. In the long run we want
//   grouping. (Later note: undo of this command seems to work. Redo will throw
//   up the neigh specification dialog again)
// SplitChannelsContext plugin is sometimes getting run and recorded. Is this a
//   problem? Is it putting useless undo markers in the history? If so we can
//   make it Unrecordable.

// Later TODOs
// Support tools and gestures
// Grouping of many cammands as one undoable block
//   Create a Command that contains a list of commands to run. Record the undos
//   in one and the redos in another. Then add the uber command to the undo stack.

// Note: because initially no image exists a redo gets recorded (load image or
// new image) without a corresponding undo. So in general redoPos == undoPos+1.
// (This may no longer be true)
// TODO - currently while no dataset exists nothing gets recorded. So what
// happens when we run a number of nondisplay oriented plugins. They aren't
// undoable. Should undo steps be associated with the app and not the displays?
// Or have a separate undo history for the app (when no dataset loaded). Then
// user can switch between displays and undo a lot of stuff. And they can record
// and undo app related events.

// Note that when a display is deleted it could invalidate other displays' own
// history (maybe). Have img1. Paste from Img2 into Img1. Delete Img2. Go to
// Img1 and do undo. Now try redo. Is last display state of Img2 kept around as
// a reference in Img1's undo history? However note that even if the undo record
// has a handle on a deleted display the command could fail because the display
// layer does not know about it anymore. Perhaps on display deleted events we
// need to walk all undo histories and trim their list to not include any
// reference to the deleted display

// Original NOTE on this same issue: what if you use ImageCalc to add two images
// (so both displays stored as inputs to plugin in undo/redo stack. Then someone
// deletes one of the displays. Then back to display that may have been a target
// of the calc. Then undo. Should crash.
// On a related topic do we store an undo operation on a Display<?>? Or Object?
// How about a set of inputs like ImageCalc. Then cleanup all undo histories
// that refer to deleted display?????

/**
 * Provides multistep undo/redo support to IJ2.
 * 
 * @author Barry DeZonia
 *
 */
@Plugin(type = Service.class)
public class UndoService extends AbstractService {

	// -- constants --
	
	// TODO we really need max mem for combined histories and max mem of one
	// history. It will work as a test bed for now. Max should be
	// a settable value in some options plugin
	
	private static final int MAX_BYTES = 20 * 1024 * 1024; // temp: 20 meg
	
	// -- Parameters --
	
	@Parameter
	private DisplayService dispService;
	
	@Parameter
	private CommandService commandService;
	
	@Parameter
	private EventService eventService;
	
	// -- working variables --
	
	private Map<Display<?>,CommandHistory> histories =
			new HashMap<Display<?>,CommandHistory>() ;

	// HACK TO GO AWAY SOON
	private Map<ModuleInfo,Boolean> modulesToIgnore =
			new ConcurrentHashMap<ModuleInfo,Boolean>();
	
	// -- service initialization code --
	
	@Override
	public void initialize() {
		subscribeToEvents(eventService);
	}

	// -- public api --
	
	/**
	 * Undoes the previous command associated with the given display.
	 * 
	 * @param interestedParty
	 */
	public void undo(Display<?> interestedParty) {
		CommandHistory history = histories.get(interestedParty);
		if (history != null) history.doUndo();
	}
	
	/**
	 * Redoes the next command associated with the given display.
	 * 
	 * @param interestedParty
	 */
	public void redo(Display<?> interestedParty) {
		CommandHistory history = histories.get(interestedParty);
		if (history != null) history.doRedo();
	}

	/**
	 * Clears the entire undo/redo cache for given display
	 */
	public void clearHistory(Display<?> interestedParty) {
		CommandHistory history = histories.get(interestedParty);
		if (history != null) history.clear();
	}
	
	/**
	 * Clears the entire undo/redo cache for all displays
	 */
	public void clearAllHistory() {
		for (CommandHistory hist : histories.values()) {
			hist.clear();
		}
	}

	/**
	 * Captures a region of a Dataset to a one dimensional Img<DoubleType>.
	 * The region is defined with a PointSet. The data is stored in the order of
	 * iteration of the input PointSet. Using the Img<DoubleType> and the original
	 * PointSet one can easily restore the data using restoreData(). The
	 * Img<DoubleType> will reside completely in memory and is limited to about
	 * two gig of elements.
	 * 
	 * @param source
	 * 	The Dataset to capture from.
	 * @param points
	 *  The set of coordinate points that hold the values to backup.
	 * @return
	 * 	An Img<DoubleType> that contains the backup data.
	 */
	public Img<DoubleType> captureData(Dataset source, PointSet points) {
		return captureData(source, points, new ArrayImgFactory<DoubleType>());
	}
	
	/**
	 * Captures a region of a Dataset to a one dimensional Img<DoubleType>.
	 * The region is defined with a PointSet. The data is stored in the order of
	 * iteration of the input PointSet. Using the Img<DoubleType> and the original
	 * PointSet one can easily restore the data using restoreData(). The
	 * Img<DoubleType> will reside in a structure provided by the user specified
	 * ImgFactory. This allows memory use and element count limitations of the
	 * default implementation to be avoided.
	 * 
	 * @param source
	 * 	The Dataset to capture from.
	 * @param points
	 *  The set of coordinate points that hold the values to backup.
	 * @param factory
	 *  The factory used to make the Img<DoubleType>. This allows API users to
	 *  determine the most efficient way to store backup data.
	 * @return
	 * 	An Img<DoubleType> that contains the backup data.
	 */
	public Img<DoubleType> captureData(Dataset source, PointSet points,
		ImgFactory<DoubleType> factory)
	{
		long numPoints = points.calcSize();
		Img<DoubleType> backup =
				factory.create(new long[]{numPoints}, new DoubleType());
		long i = 0;
		RandomAccess<? extends RealType<?>> dataAccessor =
				source.getImgPlus().randomAccess();
		RandomAccess<DoubleType> backupAccessor = backup.randomAccess();
		PointSetIterator iter = points.createIterator();
		while (iter.hasNext()) {
			long[] pos = iter.next();
			dataAccessor.setPosition(pos);
			double val = dataAccessor.get().getRealDouble();
			backupAccessor.setPosition(i++,0);
			backupAccessor.get().setReal(val);
		}
		return backup;
	}

	/**
	 * Restores a region of a Dataset from a one dimensional Img<DoubleType>.
	 * The region is defined by a PointSet. The data is stored in the order of
	 * iteration of the input PointSet. The Img<DoubleType> should have been
	 * previously recorded by captureData().
	 *
	 * @param target
	 * 	The Dataset to restore data to.
	 * @param points
	 *  The set of coordinate points of the Dataset to restore to.
	 * @param backup
	 * 	An Img<DoubleType> that contains the backup data.
	 */
	public void restoreData(Dataset target, PointSet points,
		Img<DoubleType> backup)
	{
		long i = 0;
		RandomAccess<? extends RealType<?>> dataAccessor =
				target.getImgPlus().randomAccess();
		RandomAccess<DoubleType> backupAccessor = backup.randomAccess();
		PointSetIterator iter = points.createIterator();
		while (iter.hasNext()) {
			long[] pos = iter.next();
			backupAccessor.setPosition(i++,0);
			double val = backupAccessor.get().getRealDouble();
			dataAccessor.setPosition(pos);
			dataAccessor.get().setReal(val);
		}
		target.update();
	}
	
	public InstantiableCommand createFullRestoreCommand(Display<?> display) {
		DisplayState state = display.captureState();
		HashMap<String,Object> inputs = new HashMap<String, Object>();
		inputs.put("display", display);
		inputs.put("state", state);
		return new DefaultInstantiableCommand(
			commandService.getCommand(
				DisplayRestoreState.class), inputs, state.getMemoryUsage());
	}

	// -- protected event handlers --
	
	@EventHandler
	protected void onEvent(ModuleStartedEvent evt) {
		Module module = evt.getModule();
		Object theObject = module.getDelegateObject();
		if (theObject instanceof Unrecordable) return;
		if (theObject instanceof InvertibleCommand) return; // record later
		if (theObject instanceof Command) {
			if (ignoring(module.getInfo())) return;
			Display<?> display = dispService.getActiveDisplay();
			if (!(display instanceof SupportsUndo)) return;
			InstantiableCommand reverseCommand = createFullRestoreCommand(display);
			findHistory(display).addUndo(reverseCommand);
		}
	}
	
	@EventHandler
	protected void onEvent(ModuleCanceledEvent evt) {
		Module module = evt.getModule();
		Object theObject = module.getDelegateObject();
		if (theObject instanceof Unrecordable) return;
		if (theObject instanceof InvertibleCommand) return;
		if (theObject instanceof Command) {
			if (ignoring(module.getInfo())) return;
			Display<?> display = dispService.getActiveDisplay();
			if (!(display instanceof SupportsUndo)) return;
			// remove last undo point
			findHistory(display).removeNewestUndo();
		}
	}
	
	@EventHandler
	protected void onEvent(ModuleFinishedEvent evt) {
		Module module = evt.getModule();
		Object theObject = module.getDelegateObject();
		if (theObject instanceof Unrecordable) return;
		if (theObject instanceof Command) {
			if (ignoring(module.getInfo())) {
				stopIgnoring(module.getInfo());
				return;
			}
			Display<?> display = dispService.getActiveDisplay();
			if (!(display instanceof SupportsUndo)) return;
			if (theObject instanceof InvertibleCommand) {
				InvertibleCommand command = (InvertibleCommand) theObject;
				findHistory(display).addUndo(command.getInverseCommand());
			}
			InstantiableCommand forwardCommand =
					new DefaultInstantiableCommand(
						(CommandInfo<?>)module.getInfo(), module.getInputs(), 0);
			findHistory(display).addRedo(forwardCommand);
		}
	}

	@EventHandler
	protected void onEvent(DisplayDeletedEvent evt) {
		CommandHistory history = histories.get(evt.getObject());
		if (history == null) return;
		history.clear();
		histories.remove(history);
	}
	
	// -- private helpers --

	void ignore(ModuleInfo info) {
		modulesToIgnore.put(info, true);
	}
	
	private boolean ignoring(ModuleInfo info) {
		return modulesToIgnore.get(info) != null;
	}

	private void stopIgnoring(ModuleInfo info) {
		modulesToIgnore.remove(info);
	}
	
	private CommandHistory findHistory(Display<?> disp) {
		CommandHistory h = histories.get(disp);
		if (h == null) {
			h = new CommandHistory(this, commandService, MAX_BYTES);
			histories.put(disp, h);
		}
		return h;
	}
	
}
