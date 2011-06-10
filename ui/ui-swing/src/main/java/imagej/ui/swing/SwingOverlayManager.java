package imagej.ui.swing;

import imagej.ImageJ;
import imagej.data.event.OverlayCreatedEvent;
import imagej.data.event.OverlayDeletedEvent;
import imagej.data.event.OverlayRestructuredEvent;
import imagej.data.roi.Overlay;
import imagej.display.OverlayManager;
import imagej.event.EventSubscriber;
import imagej.event.Events;

import java.awt.Container;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTextArea;

public class SwingOverlayManager extends JFrame {
	private static final long serialVersionUID = -8606439216125437173L;
	private JTextArea text = null;
	
	public SwingOverlayManager() {
		OverlayManager overlayManager = ImageJ.get(OverlayManager.class);
		List<Overlay> overlays = overlayManager.getOverlays();
		
		Container cp = this.getContentPane();
		text = new JTextArea();

		cp.add(text);
		
		for (Overlay overlay : overlays) {
			text.append(overlay.getRegionOfInterest().toString() + "\n");
		}
		
		Events.subscribe(OverlayCreatedEvent.class, new EventSubscriber<OverlayCreatedEvent>(){
			@Override
			public void onEvent(OverlayCreatedEvent event) {
				Overlay overlay = event.getObject();
				text.append(overlay.getRegionOfInterest().toString() + "\n");
			}
		});
		Events.subscribe(OverlayDeletedEvent.class, new EventSubscriber<OverlayDeletedEvent>(){
			@Override
			public void onEvent(OverlayDeletedEvent event) {
				Overlay overlay = event.getObject();
				text.append(overlay.getRegionOfInterest().toString() + "removed\n");
			}
		});
		Events.subscribe(OverlayRestructuredEvent.class, new EventSubscriber<OverlayRestructuredEvent>(){
			@Override
			public void onEvent(OverlayRestructuredEvent event) {
				Overlay overlay = event.getObject();
				text.append(overlay.getRegionOfInterest().toString() + "restructured\n");
			}
		});

		setSize(300, 300);
	}
	
	

}
