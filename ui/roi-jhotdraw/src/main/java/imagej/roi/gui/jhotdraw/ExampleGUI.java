package imagej.roi.gui.jhotdraw;
import imagej.util.Log;

import javax.swing.JToolBar;

import org.jhotdraw.contrib.MDI_DrawApplication;
import org.jhotdraw.framework.DrawingChangeEvent;
import org.jhotdraw.framework.DrawingChangeListener;
import org.jhotdraw.framework.DrawingView;
import org.jhotdraw.framework.Tool;
import org.jhotdraw.framework.ViewChangeListener;
import org.jhotdraw.standard.CreationTool;
import org.jhotdraw.util.UndoableTool;


public class ExampleGUI extends MDI_DrawApplication {

	/* (non-Javadoc)
	 * @see CH.ifa.draw.contrib.MDI_DrawApplication#createTools(javax.swing.JToolBar)
	 */
	@Override
	protected void createTools(JToolBar palette) {
		super.createTools(palette);
		for (IJHotDrawROIAdapter adapter : JHotDrawAdapterFinder.getAllAdapters()) {
			for (String typeName: adapter.getROITypeNames()) {
				CreationTool tCreate = new IJCreationTool(this, adapter, typeName);
				Tool tool = new UndoableTool(tCreate);
				palette.add(createToolButton(adapter.getIconName(), typeName, tool));
			}
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -7254823987203542956L;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ExampleGUI g = new ExampleGUI();
		g.createToolPalette();
		g.addViewChangeListener(new ViewChangeListener() {
			
			@Override
			public void viewSelectionChanged(DrawingView arg0, DrawingView arg1) {
				Log.debug("View selection changed");
				
			}
			
			@Override
			public void viewDestroying(DrawingView arg0) {
				Log.debug("Destroying view");
				
			}
			
			@Override
			public void viewCreated(DrawingView view) {
				Log.debug("Created view");
				view.drawing().addDrawingChangeListener(new DrawingChangeListener() {
					
					@Override
					public void drawingRequestUpdate(DrawingChangeEvent arg0) {
						Log.debug("Drawing request update");
						
					}
					
					@Override
					public void drawingInvalidated(DrawingChangeEvent arg0) {
						Log.debug("Drawing invalidated");
					}

					@Override
					public void drawingTitleChanged(DrawingChangeEvent arg0) {
						Log.debug("Drawing title changed");
					}
				});
			}
		});
		g.open();
	}

}
