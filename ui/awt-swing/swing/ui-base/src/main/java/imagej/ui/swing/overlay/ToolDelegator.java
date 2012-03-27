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

package imagej.ui.swing.overlay;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.event.UndoableEditListener;

import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.event.ToolListener;
import org.jhotdraw.draw.tool.AbstractTool;

public class ToolDelegator extends AbstractTool {

	private static final long serialVersionUID = 1L;

	protected JHotDrawTool selectionTool, creationTool, activeTool;

	public ToolDelegator() {
		selectionTool = new IJDelegationSelectionTool();
		for (final Object listener : listenerList.getListenerList()) {
			if (listener instanceof ToolListener) {
				selectionTool.addToolListener((ToolListener) listener);
			}
			else if (listener instanceof UndoableEditListener) {
				selectionTool.addUndoableEditListener((UndoableEditListener) listener);
			}
		}
		selectionTool.setInputMap(getInputMap());
		selectionTool.setActionMap(getActionMap());
	}

	public void setCreationTool(final JHotDrawTool creationTool) {
		this.creationTool = creationTool;
		if (creationTool == null) return;

		for (final Object listener : listenerList.getListenerList()) {
			if (listener instanceof ToolListener) {
				creationTool.addToolListener((ToolListener) listener);
			}
			else if (listener instanceof UndoableEditListener) {
				creationTool.addUndoableEditListener((UndoableEditListener) listener);
			}
		}
		creationTool.setInputMap(getInputMap());
		creationTool.setActionMap(getActionMap());
	}

	@Override
	public void draw(final Graphics2D graphics) {
		if (activeTool != null) {
			activeTool.draw(graphics);
		}
	}

	@Override
	public void mouseMoved(final MouseEvent event) {
		maybeSwitchTool(event);
		if (activeTool != null) {
			activeTool.mouseMoved(event);
		}
	}

	@Override
	public void mouseClicked(final MouseEvent event) {
		if (activeTool != null) {
			activeTool.mouseClicked(event);
		}
	}

	@Override
	public void mousePressed(final MouseEvent event) {
		maybeSwitchTool(event);
		if (activeTool != null) {
			activeTool.mousePressed(event);
		}
	}

	@Override
	public void mouseReleased(final MouseEvent event) {
		if (activeTool != null) {
			activeTool.mouseReleased(event);
		}
	}

	@Override
	public void mouseDragged(final MouseEvent event) {
		if (activeTool != null) {
			activeTool.mouseDragged(event);
		}
	}

	@Override
	public void mouseEntered(final MouseEvent event) {
		maybeSwitchTool(event);
		if (activeTool != null) {
			activeTool.mouseEntered(event);
		}
	}

	@Override
	public void mouseExited(final MouseEvent event) {
		maybeSwitchTool(event);
		if (activeTool != null) {
			activeTool.mouseExited(event);
		}
	}

	@Override
	public void activate(final DrawingEditor editor) {
		super.activate(editor);
		if (activeTool != null) {
			activeTool.activate(editor);
		}
	}

	@Override
	public void deactivate(final DrawingEditor editor) {
		if (activeTool != null) {
			activeTool.deactivate(editor);
		}
		super.deactivate(editor);
	}

	@Override
	public void addToolListener(final ToolListener listener) {
		super.addToolListener(listener);
		selectionTool.addToolListener(listener);
		if (creationTool != null) {
			creationTool.addToolListener(listener);
		}
	}

	@Override
	public void removeToolListener(final ToolListener listener) {
		super.removeToolListener(listener);
		selectionTool.removeToolListener(listener);
		if (creationTool != null) {
			creationTool.removeToolListener(listener);
		}
	}

	@Override
	public void addUndoableEditListener(final UndoableEditListener listener) {
		super.addUndoableEditListener(listener);
		selectionTool.addUndoableEditListener(listener);
		if (creationTool != null) {
			creationTool.addUndoableEditListener(listener);
		}
	}

	@Override
	public void removeUndoableEditListener(final UndoableEditListener listener) {
		super.removeUndoableEditListener(listener);
		selectionTool.removeUndoableEditListener(listener);
		if (creationTool != null) {
			creationTool.removeUndoableEditListener(listener);
		}
	}

	@Override
	public void setInputMap(final InputMap map) {
		super.setInputMap(map);
		if (selectionTool != null) {
			selectionTool.setInputMap(map);
		}
		if (creationTool != null) {
			creationTool.setInputMap(map);
		}
	}

	@Override
	public void setActionMap(final ActionMap map) {
		super.setActionMap(map);
		if (selectionTool != null) {
			selectionTool.setActionMap(map);
		}
		if (creationTool != null) {
			creationTool.setActionMap(map);
		}
	}

	@Override
	public boolean supportsHandleInteraction() {
		return true;
	}

	protected boolean maybeSwitchTool(final MouseEvent event) {
		if (activeTool != null && activeTool.isConstructing()) return false;
		anchor = new Point(event.getX(), event.getY());
		JHotDrawTool tool = creationTool;
		final DrawingView view = getView();
		if (view != null && view.isEnabled()) {
			if (view.findHandle(anchor) != null ||
				(view.findFigure(anchor) != null && view.findFigure(anchor)
					.isSelectable()))
			{
				tool = selectionTool;
			}
		}

		if (activeTool != tool) {
			if (activeTool != null) {
				activeTool.deactivate(getEditor());
			}
			if (tool != null) {
				tool.activate(editor);
				if (!isActive()) {
					tool.deactivate(editor);
				}
			}
			activeTool = tool;
			return true;
		}
		return false;
	}
}
