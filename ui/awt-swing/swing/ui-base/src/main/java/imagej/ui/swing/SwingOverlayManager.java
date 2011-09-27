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
import imagej.data.roi.AbstractOverlay;
import imagej.data.roi.Overlay;
import imagej.event.EventService;
import imagej.event.EventSubscriber;
import imagej.ext.display.DisplayService;
import imagej.ext.display.event.DisplayActivatedEvent;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Overlay Manager Swing UI
 * 
 * @author Adam Fraser
 */
public class SwingOverlayManager extends JFrame implements ActionListener {

	private static final long serialVersionUID = -6498169032123522303L;
	private JList olist = null;
	private boolean selecting = false; // flag to prevent event feedback loops
	/** Maintains the list of event subscribers, to avoid garbage collection. */
	private List<EventSubscriber<?>> subscribers;

	private void subscribeToEvents(final EventService eventService) {

		subscribers = new ArrayList<EventSubscriber<?>>();

		final EventSubscriber<OverlayCreatedEvent> creationSubscriber =
			new EventSubscriber<OverlayCreatedEvent>() {

				@Override
				@SuppressWarnings("synthetic-access")
				public void onEvent(final OverlayCreatedEvent event) {
					System.out.println("\tCREATED: " + event.toString());
					olist.updateUI();
				}

			};
		subscribers.add(creationSubscriber);
		eventService.subscribe(OverlayCreatedEvent.class, creationSubscriber);
		//
		final EventSubscriber<OverlayDeletedEvent> deletionSubscriber =
			new EventSubscriber<OverlayDeletedEvent>() {

				@Override
				@SuppressWarnings("synthetic-access")
				public void onEvent(final OverlayDeletedEvent event) {
					System.out.println("\tDELETED: " + event.toString());
					olist.updateUI();
				}

			};
		subscribers.add(deletionSubscriber);
		eventService.subscribe(OverlayDeletedEvent.class, deletionSubscriber);
		//
		// No need to update unless thumbnail will be redrawn.
//		final EventSubscriber<OverlayRestructuredEvent> restructureSubscriber =
//				new EventSubscriber<OverlayRestructuredEvent>() {
//
//					@Override
//					public void onEvent(OverlayRestructuredEvent event) {
//						System.out.println("\tRESTRUCTURED: " + event.toString());
//						olist.updateUI();
//					}
//
//				};
//		subscribers.add(restructureSubscriber);
//		eventService.subscribe(OverlayRestructuredEvent.class, restructureSubscriber);
		//
		/*
		 * Update when a display is activated 
		 */
		final EventSubscriber<DisplayActivatedEvent> displayActivatedSubscriber =
			new EventSubscriber<DisplayActivatedEvent>() {

				@Override
				@SuppressWarnings("synthetic-access")
				public void onEvent(final DisplayActivatedEvent event) {
					olist.updateUI();
				}

			};
		subscribers.add(displayActivatedSubscriber);
		eventService.subscribe(DisplayActivatedEvent.class, displayActivatedSubscriber);

		final EventSubscriber<DataViewSelectionEvent> viewSelectionSubscriber =
			new EventSubscriber<DataViewSelectionEvent>() {

				@Override
				@SuppressWarnings("synthetic-access")
				public void onEvent(final DataViewSelectionEvent event) {
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
			};
		subscribers.add(viewSelectionSubscriber);
		eventService.subscribe(DataViewSelectionEvent.class,
			viewSelectionSubscriber);
		//
	}

	/*
	 * Constructor. Create a JList to list the overlays. 
	 */

	public SwingOverlayManager(final EventService eventService) {
		olist = new JList(new OverlayListModel());
		olist.setCellRenderer(new OverlayRenderer());

		// Populate the list with the current overlays
		final OverlayService om = ImageJ.get(OverlayService.class);
		for (final Overlay overlay : om.getOverlays()) {
			olist.add(olist.getCellRenderer().getListCellRendererComponent(olist,
				overlay, -1, false, false));
		}

		final JScrollPane listScroller = new JScrollPane(olist);
		listScroller.setPreferredSize(new Dimension(250, 80));
		listScroller.setAlignmentX(LEFT_ALIGNMENT);
		final JPanel listPane = new JPanel();
		listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
		listPane.add(listScroller);
		listPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		final JButton delbutton = new JButton("delete selected");
		delbutton.setMnemonic(KeyEvent.VK_DELETE);
		delbutton.setActionCommand("delete");
		delbutton.addActionListener(this);
		final JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(delbutton);

		setSize(300, 300);

		final Container cp = this.getContentPane();
		cp.add(listPane, BorderLayout.CENTER);
		cp.add(buttonPane, BorderLayout.PAGE_END);

		//
		// Listen to list selections
		//
		final ListSelectionListener listSelectionListener =
			new ListSelectionListener() {

				@Override
				@SuppressWarnings("synthetic-access")
				public void valueChanged(final ListSelectionEvent listSelectionEvent) {
					if (selecting) return;
					selecting = true;
					final ImageDisplayService imageDisplayService =
						ImageJ.get(ImageDisplayService.class);
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
		subscribeToEvents(eventService);
//		Events.subscribe(OverlayCreatedEvent.class, creationSubscriber);
//		Events.subscribe(OverlayDeletedEvent.class, deletionSubscriber);
//		// No need to update unless thumbnail will be redrawn.
////		Events.subscribe(OverlayRestructuredEvent.class, restructureSubscriber);
//		Events.subscribe(DisplayActivatedEvent.class, displayActivatedSubscriber);
//		Events.subscribe(DataViewSelectionEvent.class, viewSelectedSubscriber);

	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		if ("delete".equals(e.getActionCommand())) {
			final AbstractOverlay overlay =
				(AbstractOverlay) olist.getSelectedValue();
			overlay.delete();
			System.out.println("\tDelete overlay " +
				overlay.getRegionOfInterest().toString());
		}
	}

	/**
	 * JList synchronized with the overlays in the OverlayService.
	 */
	public class OverlayListModel extends AbstractListModel {

		private static final long serialVersionUID = 7941252533859436640L;
		private final OverlayService om = ImageJ.get(OverlayService.class);
		private final DisplayService dm = ImageJ.get(DisplayService.class);

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
}