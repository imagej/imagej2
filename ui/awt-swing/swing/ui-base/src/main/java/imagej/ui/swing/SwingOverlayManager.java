//
// SwingOverlayManager.java
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

package imagej.ui.swing;

import imagej.ImageJ;
import imagej.data.display.DataView;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.OverlayService;
import imagej.data.display.event.DataViewSelectionEvent;
import imagej.data.event.OverlayCreatedEvent;
import imagej.data.event.OverlayDeletedEvent;
import imagej.data.overlay.AbstractOverlay;
import imagej.data.overlay.Overlay;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.event.EventSubscriber;
import imagej.ext.KeyCode;
import imagej.ext.display.DisplayService;
import imagej.ext.display.event.DisplayActivatedEvent;
import imagej.ext.display.event.input.KyPressedEvent;
import imagej.util.Prefs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

// TODO
//
// - implement methods that actually do stuff
// - get command accelerators working without needing ALT/OPTION
//     KEY EVENTS SEEM TO ONLY TAKE PLACE IN THE CONTEXT OF DISPLAYS
// - draw overlay labels in left pane
// - FIXME: application menu bar disappears when Overlay Mgr running

/**
 * Overlay Manager Swing UI
 * 
 * @author Barry DeZonia
 * @author Adam Fraser
 */
public class SwingOverlayManager
	extends JFrame
	implements ActionListener, ItemListener
{

	// -- constants --
	
	private static final long serialVersionUID = -6498169032123522303L;

	private static final String ACTION_ADD = "add";
	private static final String ACTION_UPDATE = "update";
	private static final String ACTION_DELETE = "delete";
	private static final String ACTION_RENAME = "rename";
	private static final String ACTION_MEASURE = "measure";
	private static final String ACTION_DESELECT = "deselect";
	private static final String ACTION_PROPERTIES = "properties";
	private static final String ACTION_FLATTEN = "flatten";
	private static final String ACTION_OPEN = "open";
	private static final String ACTION_SAVE = "save";
	private static final String ACTION_FILL = "fill";
	private static final String ACTION_DRAW = "draw";
	private static final String ACTION_AND = "and";
	private static final String ACTION_OR = "or";
	private static final String ACTION_XOR = "xor";
	private static final String ACTION_SPLIT = "split";
	private static final String ACTION_ADD_PARTICLES = "add particles";
	private static final String ACTION_MULTI_MEASURE = "multi measure";
	private static final String ACTION_MULTI_PLOT = "multi plot";
	private static final String ACTION_SORT = "sort";
	private static final String ACTION_SPECIFY = "specify";
	private static final String ACTION_REMOVE_SLICE_INFO = "remove slice info";
	private static final String ACTION_HELP = "help";
	private static final String ACTION_OPTIONS = "options";
	
	private static final String LAST_X = "lastXLocation";
	private static final String LAST_Y = "lastYLocation";

	// -- instance variables --
	
	/** Maintains the list of event subscribers, to avoid garbage collection. */
	@SuppressWarnings("unused")
	private List<EventSubscriber<?>> subscribers = null;
	private ImageJ context = null;
	private JList olist = null;
	private boolean selecting = false; // flag to prevent event feedback loops
	private JPopupMenu popupMenu = null;
	private JCheckBox showAllCheckBox = null;
	private JCheckBox editModeCheckBox = null;
	private boolean shiftDown = false;
	private boolean altDown = false;
	//private FakeDisplay fakeDisplay = new FakeDisplay(Double.class);
	//private AWTKeyEventDispatcher keyEventDispatcher;
	
	// -- constructor --
	
	/**
	 * Creates a JList to list the overlays. 
	 */
	public SwingOverlayManager(final ImageJ context) {
		this.context = context;
		olist = new JList(new OverlayListModel());
		olist.setCellRenderer(new OverlayRenderer());

		// Populate the list with the current overlays
		final OverlayService om = context.getService(OverlayService.class);
		for (final Overlay overlay : om.getOverlays()) {
			olist.add(olist.getCellRenderer().getListCellRendererComponent(olist,
				overlay, -1, false, false));
		}

		final JScrollPane listScroller = new JScrollPane(olist);
		listScroller.setPreferredSize(new Dimension(250, 80));
		listScroller.setAlignmentX(LEFT_ALIGNMENT);
		final JPanel listPanel = new JPanel();
		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
		listPanel.add(listScroller);
		listPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		final JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new GridLayout(9,1,5,0));
		buttonPane.add(getAddButton());
		buttonPane.add(getUpdateButton());
		buttonPane.add(getDeleteButton());
		buttonPane.add(getRenameButton());
		buttonPane.add(getMeasureButton());
		buttonPane.add(getDeselectButton());
		buttonPane.add(getPropertiesButton());
		buttonPane.add(getFlattenButton());
		buttonPane.add(getMoreButton());

		final JPanel boolPane = new JPanel();
		boolPane.setLayout(new BoxLayout(boolPane, BoxLayout.Y_AXIS));
		showAllCheckBox = new JCheckBox("Show All",false);
		editModeCheckBox = new JCheckBox("Edit Mode",false);
		boolPane.add(showAllCheckBox);
		boolPane.add(editModeCheckBox);
		showAllCheckBox.addItemListener(this);
		editModeCheckBox.addItemListener(this);
		
		final JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BorderLayout());
		controlPanel.add(buttonPane,BorderLayout.CENTER);
		controlPanel.add(boolPane,BorderLayout.SOUTH);

		final Container cp = this.getContentPane();
		cp.add(listPanel, BorderLayout.CENTER);
		cp.add(controlPanel, BorderLayout.EAST);

		setTitle("Overlay Manager");
		setupListSelectionListener();
		setupCloseListener();
		// TODO - kill? isn't working
		setupKeyListener();
		restoreLocation();
		
		EventService eventService = context.getService(EventService.class);
		
		subscribers = eventService.subscribe(this);

		pack();
		
		//keyEventDispatcher = new AWTKeyEventDispatcher(fakeDisplay, eventService);
		//addKeyListener(keyEventDispatcher);
	}

	// -- public interface --
	
	@Override
	public void actionPerformed(final ActionEvent e) {
		String command = e.getActionCommand();
		if (command == null) return;
		if (command.equals(ACTION_ADD))
			add();
		else if (command.equals(ACTION_ADD_PARTICLES))
			addParticles();
		else if (command.equals(ACTION_AND))
			and();
		else if (command.equals(ACTION_DELETE))
			delete();
		else if (command.equals(ACTION_DESELECT))
			deselect();
		else if (command.equals(ACTION_DRAW))
			draw();
		else if (command.equals(ACTION_FILL))
			fill();
		else if (command.equals(ACTION_FLATTEN))
			flatten();
		else if (command.equals(ACTION_HELP))
			help();
		else if (command.equals(ACTION_MEASURE))
			measure();
		else if (command.equals(ACTION_MULTI_MEASURE))
			multiMeasure();
		else if (command.equals(ACTION_MULTI_PLOT))
			multiPlot();
		else if (command.equals(ACTION_OPEN))
			open();
		else if (command.equals(ACTION_OPTIONS))
			options();
		else if (command.equals(ACTION_OR))
			or();
		else if (command.equals(ACTION_PROPERTIES))
			properties();
		else if (command.equals(ACTION_REMOVE_SLICE_INFO))
			removeSliceInfo();
		else if (command.equals(ACTION_RENAME))
			rename();
		else if (command.equals(ACTION_SAVE))
			save();
		else if (command.equals(ACTION_SORT))
			sort();
		else if (command.equals(ACTION_SPECIFY))
			specify();
		else if (command.equals(ACTION_SPLIT))
			split();
		else if (command.equals(ACTION_UPDATE))
			update();
		else if (command.equals(ACTION_XOR))
			xor();
	}

	/**
	 * JList synchronized with the overlays in the OverlayService.
	 */
	@SuppressWarnings("synthetic-access")
	public class OverlayListModel extends AbstractListModel {

		private static final long serialVersionUID = 7941252533859436640L;
		private final OverlayService om = context.getService(OverlayService.class);
		private final DisplayService dm = context.getService(DisplayService.class);

		@Override
		public Object getElementAt(final int index) {
			final ImageDisplay display = (ImageDisplay) dm.getActiveDisplay();
			return om.getOverlays(display).get(index);
		}

		@Override
		public int getSize() {
			final ImageDisplay display = (ImageDisplay) dm.getActiveDisplay();
			return om.getOverlays(display).size();
		}

	}

	/**
	 *
	 */
	public class OverlayRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = 2468086636364454253L;
		private final Hashtable<Overlay, ImageIcon> iconTable =
			new Hashtable<Overlay, ImageIcon>();

		@Override
		public Component getListCellRendererComponent(final JList list,
			final Object value, final int index, final boolean isSelected,
			final boolean hasFocus)
		{
			final JLabel label =
				(JLabel) super.getListCellRendererComponent(list, value, index,
					isSelected, hasFocus);
			if (value instanceof Overlay) {
				final Overlay overlay = (Overlay) value;
				// TODO: create overlay thumbnail from overlay
				final ImageIcon icon = iconTable.get(overlay);
//				if (icon == null) {
//					icon = new ImageIcon(...);
//					iconTable.put(overlay, ImageIcon);
//				}
				label.setIcon(icon);
			}
			else {
				// Clear old icon; needed in 1st release of JDK 1.2
				label.setIcon(null);
			}
			return label;
		}

	}

	// -- event handlers --

	@EventHandler
	protected void onEvent(final OverlayCreatedEvent event) {
		System.out.println("\tCREATED: " + event.toString());
		olist.updateUI();
	}

	@EventHandler
	protected void onEvent(final OverlayDeletedEvent event) {
		System.out.println("\tDELETED: " + event.toString());
		olist.updateUI();
	}

	/**
	 * Update when a display is activated.
	 */
	@EventHandler
	protected void onEvent(
		@SuppressWarnings("unused") final DisplayActivatedEvent event)
	{
		olist.updateUI();
	}

	@EventHandler
	protected void onEvent(final DataViewSelectionEvent event) {
		if (selecting) return;
		selecting = true;
		// Select or deselect the corresponding overlay in the list
		final Object overlay = event.getView().getData();
		if (event.isSelected()) {
			final int[] current_sel = olist.getSelectedIndices();
			olist.setSelectedValue(overlay, true);
			final int[] new_sel = olist.getSelectedIndices();
			final int[] sel =
				Arrays.copyOf(current_sel, current_sel.length + new_sel.length);
			System.arraycopy(new_sel, 0, sel, current_sel.length,
				new_sel.length);
			olist.setSelectedIndices(sel);
		}
		else {
			for (final int i : olist.getSelectedIndices()) {
				if (olist.getModel().getElementAt(i) == overlay) {
					olist.removeSelectionInterval(i, i);
				}
			}
		}
		selecting = false;
	}

	// TODO - this may not be best way to do this
	//   Its here to allow acceleration without ALT/OPTION key
	//   Maybe make buttons listen for actions
	@EventHandler
	protected void onKeyPressedEvent(KyPressedEvent ev) {
		System.out.println("key press registered to display "+ev.getDisplay());
		altDown = ev.getModifiers().isAltDown() || ev.getModifiers().isAltGrDown();
		shiftDown = ev.getModifiers().isShiftDown();
		KeyCode key = ev.getCode();
		if (key == KeyCode.T) add();
		if (key == KeyCode.F) flatten();
	}
	
	// No need to update unless thumbnail will be redrawn.
//	@EventHandler
//	protected void onEvent(OverlayRestructuredEvent event) {
//		System.out.println("\tRESTRUCTURED: " + event.toString());
//		olist.updateUI();
//	}

	// -- private helpers that implement overlay interaction commands --
	
	private void add() {
		System.out.println("add");
	}
	
	private void addParticles() {
		System.out.println("add particles");
	}
	
	private void and() {
		System.out.println("and");
	}
	
	private void delete() {
		System.out.println("delete");
		final AbstractOverlay overlay =
			(AbstractOverlay) olist.getSelectedValue();
		if (overlay == null) return;
		overlay.delete();
		System.out.println("\tDelete overlay " +
				overlay.getRegionOfInterest().toString());
	}
	
	private void deselect() {
		System.out.println("deselect");
	}
	
	private void draw() {
		System.out.println("draw");
	}
	
	private void fill() {
		System.out.println("fill");
	}
	
	private void flatten() {
		System.out.println("flatten");
	}
	
	private void help() {
		System.out.println("help");
	}
	
	private void measure() {
		System.out.println("measure");
	}
	
	private void multiMeasure() {
		System.out.println("multi measure");
	}
	
	private void multiPlot() {
		System.out.println("multi plot");
	}
	
	private void open() {
		System.out.println("open");
	}
	
	private void options() {
		System.out.println("options");
	}
	
	private void or() {
		System.out.println("or");
	}
	
	private void properties() {
		System.out.println("properties");
	}
	
	private void removeSliceInfo() {
		System.out.println("remove slice info");
	}
	
	private void rename() {
		System.out.println("rename");
	}
	
	private void save() {
		System.out.println("save");
	}
	
	private void sort() {
		System.out.println("sort");
	}
	
	private void specify() {
		System.out.println("specify");
	}
	
	private void split() {
		System.out.println("split");
	}
	
	private void update() {
		System.out.println("update");
	}
	
	private void xor() {
		System.out.println("xor");
	}
	
	// -- private helpers for hotkey handling --

	// TODO
	// THIS IS NOT WORKING! NO KEY EVENT EVER DETECTED
	private void setupKeyListener() {
		addKeyListener(new KeyListener() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void keyPressed(KeyEvent e) {
				System.out.println("key pressed");
				if (e.getKeyCode() == KeyEvent.VK_T) add();
				if (e.getKeyCode() == KeyEvent.VK_F) flatten();
				if (e.getKeyCode() == KeyEvent.VK_DELETE) delete();
			}
			@Override
			public void keyReleased(KeyEvent e) { /* do nothing */ }
			@Override
			public void keyTyped(KeyEvent e) { /* do nothing */ }
		});
	}
	
	// -- private helpers for frame location --
	
	/** Persists the application frame's current location. */
	private void saveLocation() {
		Prefs.put(getClass(), LAST_X, getLocation().x);
		Prefs.put(getClass(), LAST_Y, getLocation().y);
	}

	/** Restores the application frame's current location. */
	private void restoreLocation() {
		final int lastX = Prefs.getInt(getClass(), LAST_X, 0);
		final int lastY = Prefs.getInt(getClass(), LAST_Y, 0);
		setLocation(lastX, lastY);
	}
	
	private void setupCloseListener() {
		addWindowListener(new WindowListener() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void windowClosing(WindowEvent e) {
				saveLocation();
			}
			@Override
			public void windowOpened(WindowEvent e) { /* do nothing */ }			
			@Override
			public void windowIconified(WindowEvent e) { /* do nothing */ }
			@Override
			public void windowDeiconified(WindowEvent e) { /* do nothing */ }
			@Override
			public void windowDeactivated(WindowEvent e) { /* do nothing */ }
			@Override
			public void windowClosed(WindowEvent e) { /* do nothing */ }
			@Override
			public void windowActivated(WindowEvent e) { /* do nothing */ }
		});
	}

	// -- private helpers for list selection event listening --

	private void setupListSelectionListener() {
		final ListSelectionListener
			listSelectionListener =	new ListSelectionListener() {
				@Override
				@SuppressWarnings("synthetic-access")
				public void valueChanged(final ListSelectionEvent listSelectionEvent) {
					if (selecting) return;
					selecting = true;
					final ImageDisplayService imageDisplayService =
						context.getService(ImageDisplayService.class);
					final ImageDisplay display =
						imageDisplayService.getActiveImageDisplay();
					final JList list = (JList) listSelectionEvent.getSource();
					final Object selectionValues[] = list.getSelectedValues();
					for (final DataView overlayView : display) {
						overlayView.setSelected(false);
						for (final Object overlay : selectionValues) {
							if (overlay == overlayView.getData()) {
								overlayView.setSelected(true);
								break;
							}
						}
					}
					selecting = false;
				}
			};
		olist.addListSelectionListener(listSelectionListener);
	}

	// -- private helpers for constructing popup menu --
	
	private JPopupMenu getPopupMenu() {
		if (popupMenu == null)
			popupMenu = createPopupMenu();
		return popupMenu;
	}

	private JPopupMenu createPopupMenu() {
		JPopupMenu menu = new JPopupMenu();
		menu.add(getOpenMenuItem());
		menu.add(getSaveMenuItem());
		menu.add(getFillMenuItem());
		menu.add(getDrawMenuItem());
		menu.add(getAndMenuItem());
		menu.add(getOrMenuItem());
		menu.add(getXorMenuItem());
		menu.add(getSplitMenuItem());
		menu.add(getAddParticlesMenuItem());
		menu.add(getMultiMeasureMenuItem());
		menu.add(getMultiPlotMenuItem());
		menu.add(getSortMenuItem());
		menu.add(getSpecifyMenuItem());
		menu.add(getRemoveSliceInfoMenuItem());
		menu.add(getHelpMenuItem());
		menu.add(getOptionsMenuItem());
		return menu;
	}
	
	private JMenuItem getAddParticlesMenuItem() {
		JMenuItem item;
		item = new JMenuItem("Add Particles");
		item.setActionCommand(ACTION_ADD_PARTICLES);
		item.addActionListener(this);
		return item;
	}
	
	private JMenuItem getAndMenuItem() {
		JMenuItem item;
		item = new JMenuItem("AND");
		item.setActionCommand(ACTION_AND);
		item.addActionListener(this);
		return item;
	}
	
	private JMenuItem getDrawMenuItem() {
		JMenuItem item;
		item = new JMenuItem("Draw");
		item.setActionCommand(ACTION_DRAW);
		item.addActionListener(this);
		return item;
	}
	
	private JMenuItem getFillMenuItem() {
		JMenuItem item;
		item = new JMenuItem("Fill");
		item.setActionCommand(ACTION_FILL);
		item.addActionListener(this);
		return item;
	}
	
	private JMenuItem getHelpMenuItem() {
		JMenuItem item;
		item = new JMenuItem("Help");
		item.setActionCommand(ACTION_HELP);
		item.addActionListener(this);
		return item;
	}
	
	private JMenuItem getMultiMeasureMenuItem() {
		JMenuItem item;
		item = new JMenuItem("Multi Measure");
		item.setActionCommand(ACTION_MULTI_MEASURE);
		item.addActionListener(this);
		return item;
	}
	
	private JMenuItem getMultiPlotMenuItem() {
		JMenuItem item;
		item = new JMenuItem("Multi Plot");
		item.setActionCommand(ACTION_MULTI_PLOT);
		item.addActionListener(this);
		return item;
	}
	
	private JMenuItem getOpenMenuItem() {
		JMenuItem item;
		item = new JMenuItem("Open...");
		item.setActionCommand(ACTION_OPEN);
		item.addActionListener(this);
		return item;
	}
	
	private JMenuItem getOptionsMenuItem() {
		JMenuItem item;
		item = new JMenuItem("Options...");
		item.setActionCommand(ACTION_OPTIONS);
		item.addActionListener(this);
		return item;
	}
	
	private JMenuItem getOrMenuItem() {
		JMenuItem item;
		item = new JMenuItem("OR (Combine)");
		item.setActionCommand(ACTION_OR);
		item.addActionListener(this);
		return item;
	}
	
	private JMenuItem getRemoveSliceInfoMenuItem() {
		JMenuItem item;
		item = new JMenuItem("Remove Slice Info");
		item.setActionCommand(ACTION_REMOVE_SLICE_INFO);
		item.addActionListener(this);
		return item;
	}
	
	private JMenuItem getSaveMenuItem() {
		JMenuItem item;
		item = new JMenuItem("Save...");
		item.setActionCommand(ACTION_SAVE);
		item.addActionListener(this);
		return item;
	}
	
	private JMenuItem getSortMenuItem() {
		JMenuItem item;
		item = new JMenuItem("Sort");
		item.setActionCommand(ACTION_SORT);
		item.addActionListener(this);
		return item;
	}
	
	private JMenuItem getSpecifyMenuItem() {
		JMenuItem item;
		item = new JMenuItem("Specify...");
		item.setActionCommand(ACTION_SPECIFY);
		item.addActionListener(this);
		return item;
	}
	
	private JMenuItem getSplitMenuItem() {
		JMenuItem item;
		item = new JMenuItem("Split");
		item.setActionCommand(ACTION_SPLIT);
		item.addActionListener(this);
		return item;
	}
	
	private JMenuItem getXorMenuItem() {
		JMenuItem item;
		item = new JMenuItem("XOR");
		item.setActionCommand(ACTION_XOR);
		item.addActionListener(this);
		return item;
	}
	
	// -- private helpers to implement main pane button controls --
	
	private JButton getAddButton() {
		final JButton button = new JButton("Add [t]");
		button.setMnemonic(KeyEvent.VK_T);
		button.setActionCommand(ACTION_ADD);
		button.addActionListener(this);
		return button;
	}
	
	private JButton getDeleteButton() {
		final JButton button = new JButton("Delete");
		button.setMnemonic(KeyEvent.VK_DELETE);
		button.setActionCommand(ACTION_DELETE);
		button.addActionListener(this);
		return button;
	}
	
	private JButton getDeselectButton() {
		final JButton button = new JButton("Deselect");
		button.setActionCommand(ACTION_DESELECT);
		button.addActionListener(this);
		return button;
	}
	
	private JButton getFlattenButton() {
		final JButton button = new JButton("Flatten [f]");
		button.setMnemonic(KeyEvent.VK_F);
		button.setActionCommand(ACTION_FLATTEN);
		button.addActionListener(this);
		return button;
	}
	
	private JButton getMeasureButton() {
		final JButton button = new JButton("Measure");
		button.setActionCommand(ACTION_MEASURE);
		button.addActionListener(this);
		return button;
	}
	
	private JButton getMoreButton() {
		final JButton button = new JButton("More "+'\u00bb');
		button.addMouseListener(new MouseListener() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void mouseClicked(MouseEvent e) {
				getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
			}
			@Override
			public void mouseEntered(MouseEvent evt) { /* do nothing */ }
			@Override
			public void mouseExited(MouseEvent evt) { /* do nothing */ }
			@Override
			public void mousePressed(MouseEvent evt) { /* do nothing */ }
			@Override
			public void mouseReleased(MouseEvent evt) { /* do nothing */ }
			
		});
		return button;
	}
	
	private JButton getPropertiesButton() {
		final JButton button = new JButton("Properties...");
		button.setActionCommand(ACTION_PROPERTIES);
		button.addActionListener(this);
		return button;
	}
	
	private JButton getRenameButton() {
		final JButton button = new JButton("Rename...");
		button.setActionCommand(ACTION_RENAME);
		button.addActionListener(this);
		return button;
	}
	
	private JButton getUpdateButton() {
		final JButton button = new JButton("Update");
		button.setActionCommand(ACTION_UPDATE);
		button.addActionListener(this);
		return button;
	}

	// -- private helpers to change state when checkboxes change --

	// TODO
	
	@Override
	public void itemStateChanged(ItemEvent evt) {
		boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
		if (evt.getSource() == showAllCheckBox) {
			System.out.println("show all is now "+selected);
		}
		if (evt.getSource() == editModeCheckBox) {
			System.out.println("edit mode is now "+selected);
		}
	}

	// -- private class to utilize a fake display capability for key events
	/*
	private class FakeDisplay extends AbstractDisplay<Double> {

		public FakeDisplay(Class<Double> type) {
			super(type);
		}

		@Override
		protected void rebuild() {
			// do nothing
		}
	}
	*/
}
