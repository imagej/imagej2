/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.display.view;

import imagej.data.event.DatasetChangedEvent;
import imagej.display.ImageCanvas;
import imagej.event.EventSubscriber;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 *
 * @author GBH
 */
public class ImageDisplayPanel extends JPanel implements EventSubscriber<DatasetChangedEvent>, ImageCanvas {

	protected int maxWidth = 0, maxHeight = 0;
	ImgDisplayController ctrl;
	ImagePanel imgPanel;
	//JPanel controlPanel = new JPanel();
	
	public ImageDisplayPanel(ImgDisplayController ctrl) {
		this.ctrl = ctrl;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		//setLayout(new BorderLayout());
		imgPanel = new ImagePanel();
		add(imgPanel, BorderLayout.CENTER);
		//add(imgPanel, BorderLayout.CENTER);
		//add(controlPanel, BorderLayout.SOUTH);
		//controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
		
	}

	public void setMaxDimension(DatasetView view) {
		int width = (int) view.getImg().dimension(0) + view.getPositionX();
		int height = (int) view.getImg().dimension(1) + view.getPositionY();
		if (width > maxWidth) {
			maxWidth = width;
		}
		if (height > maxHeight) {
			maxHeight = height;
		}
	}

	public void resetSize() {
		this.setPreferredSize(new Dimension(maxWidth, maxHeight));
	}

	@Override
	public void onEvent(DatasetChangedEvent event) {
		imgPanel.repaint();
	}

	@Override
	public int getImageWidth() {
		return maxWidth;
	}

	@Override
	public int getImageHeight() {
		return maxHeight;
	}

	@Override
	public void updateImage() {
		imgPanel.repaint();
	}
	
	public void addToControlPanel(JComponent component) {
		//controlPanel.add(component);
	}

	class ImagePanel extends JPanel {

		@Override
		public void paint(Graphics g) {
			for (final DatasetView view : ctrl.getViews()) {
				System.out.println("Painting in ImagePanel...");
				final Image image = view.getScreenImage().image();
				g.drawImage(image, view.getPositionX(), view.getPositionY(), this);
			}
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(maxWidth, maxHeight);
		}

	};
}
