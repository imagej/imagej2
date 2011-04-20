//
// ExampleGUI.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.roi.ui.jhotdraw;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.Action;
import javax.swing.JToolBar;

import org.jhotdraw.app.Application;
import org.jhotdraw.app.DefaultApplicationModel;
import org.jhotdraw.app.MDIApplication;
import org.jhotdraw.app.View;
import org.jhotdraw.draw.DefaultDrawingEditor;
import org.jhotdraw.draw.DefaultDrawingView;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.action.ButtonFactory;
import org.jhotdraw.draw.tool.CreationTool;
import org.jhotdraw.samples.draw.DrawView;
import org.jhotdraw.util.ResourceBundleUtil;

/**
 * TODO
 * 
 * @author Lee Kamentsky
 */
public class ExampleGUI {

	public static void main(final String[] args) {
		final MDIApplication app = new MDIApplication();
		final DrawingEditor editor = new DefaultDrawingEditor();
		DefaultApplicationModel dam = new DefaultApplicationModel() {
			@Override
			public List<JToolBar> createToolBars(Application app, View p) {
				List<JToolBar> tbList = new ArrayList<JToolBar>(super.createToolBars(app, p));
				tbList.add(ExampleGUI.createToolBar(editor));
				return tbList;
			}

			@Override
			public View createView() {
				DrawView v = new DrawView();
				v.setEditor(editor);
				return v;
			}
			
		};
		app.setModel(dam);
		app.launch(new String [0]);
	}

	public static JToolBar createToolBar(final DrawingEditor editor) {
		final JToolBar toolBar = new JToolBar();
		addButtons(toolBar, editor, ButtonFactory.createDrawingActions(editor),
			ButtonFactory.createSelectionActions(editor));
		return toolBar;
	}

	private static void addButtons(final JToolBar toolBar,
		final DrawingEditor editor, final Collection<Action> drawingActions,
		final Collection<Action> selectionActions)
	{
		final ResourceBundleUtil labels =
			ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");

		ButtonFactory.addSelectionToolTo(toolBar, editor, drawingActions,
			selectionActions);
		toolBar.addSeparator();
		for (final IJHotDrawROIAdapter adapter :
			JHotDrawAdapterFinder.getAllAdapters())
		{
			for (final String typeName : adapter.getROITypeNames()) {
				final CreationTool creationTool =
					new IJCreationTool(adapter, typeName);
				ButtonFactory.addToolTo(toolBar, editor, creationTool, adapter.getIconName(),
					labels);
			}
		}
	}

}
