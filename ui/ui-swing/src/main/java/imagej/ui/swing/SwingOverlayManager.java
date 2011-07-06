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
import imagej.data.event.OverlayCreatedEvent;
import imagej.data.event.OverlayDeletedEvent;
import imagej.data.event.OverlayRestructuredEvent;
import imagej.data.roi.Overlay;
import imagej.display.OverlayManager;
import imagej.event.EventSubscriber;
import imagej.event.Events;

import java.awt.Component;
import java.awt.Container;
import java.util.Hashtable;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;

/**
 * Overlay Manager Swing UI
 * 
 * @author Adam Fraser
 */
public class SwingOverlayManager extends JFrame {
	private static final long serialVersionUID = -6498169032123522303L;
	private JList olist = null;

	public SwingOverlayManager() {
		olist = new JList(new OverlayListModel());
		olist.setCellRenderer(new OverlayRenderer());
		
		Container cp = this.getContentPane();
		cp.add(olist);
		
		OverlayManager om = ImageJ.get(OverlayManager.class);			
		for (Overlay overlay : om.getOverlays()) {
			olist.add(olist.getCellRenderer().getListCellRendererComponent(olist, overlay, -1, false, false));
		}

		setSize(300, 300);
	}


	/*
	 * http://www.apl.jhu.edu/~hall/java/Swing-Tutorial/Swing-Tutorial-JList.html
	 */
	public class OverlayListModel extends AbstractListModel {
		private static final long serialVersionUID = 7941252533859436640L;
		private OverlayManager om;

		public OverlayListModel() {
			om = ImageJ.get(OverlayManager.class);			
			
			Events.subscribe(OverlayCreatedEvent.class, new EventSubscriber<OverlayCreatedEvent>(){
				@Override
				public void onEvent(OverlayCreatedEvent event) {
					Overlay overlay = event.getObject();
					int index = olist.getComponents().length;
					olist.add(olist.getCellRenderer().getListCellRendererComponent(olist, overlay, index, false, false), 
							index);
					olist.updateUI();
				}
			});
			
			Events.subscribe(OverlayDeletedEvent.class, new EventSubscriber<OverlayDeletedEvent>(){
				@Override
				public void onEvent(OverlayDeletedEvent event) {
					Overlay overlay = event.getObject();
					olist.remove(olist.getCellRenderer().getListCellRendererComponent(olist, overlay, -1, false, false));
					olist.updateUI();
				}
			});
			
			Events.subscribe(OverlayRestructuredEvent.class, new EventSubscriber<OverlayRestructuredEvent>(){
				@Override
				public void onEvent(OverlayRestructuredEvent event) {
					// TODO: update overlay thumbnail icons
//					Overlay overlay = event.getObject();
					olist.updateUI();
				}
			});
		}

		public Object getElementAt(int index) {
			return om.getOverlays().get(index);
		}

		public int getSize() {
			return om.getOverlays().size();
		}
	}

	
	public class OverlayRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 2468086636364454253L;
		private Hashtable iconTable = new Hashtable();

		public Component getListCellRendererComponent(JList list,
				Object value, int index, boolean isSelected, boolean hasFocus) {
			JLabel label = (JLabel) super.getListCellRendererComponent(
					list, value, index, isSelected, hasFocus);
			if (value instanceof Overlay) {
				Overlay overlay = (Overlay) value;
				//TODO: create overlay thumbnail icon from overlay 
				ImageIcon icon = (ImageIcon) iconTable.get(value);
//				if (icon == null) {
//					icon = new ImageIcon(...);
//					iconTable.put(value, icon);
//				}
				label.setIcon(icon);
			} else {
				// Clear old icon; needed in 1st release of JDK 1.2
				label.setIcon(null); 
			}
			return label;
		}
	}

}
