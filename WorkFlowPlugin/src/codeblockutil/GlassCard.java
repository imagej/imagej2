package codeblockutil;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;

import codeblockutil.CScrollPane.ScrollPolicy;

/**
 * A GlassCard is used by glass explorers explorers as a
 * mediator to their canvases.
 * 
 * It wraps a button, a scrollpane, and a invoker.
 * 
 * The button uses information about the current color
 * and highlight of the canvas to dipict itself.  The
 * scrollpane takes the canvas and puts it inside a scroll
 * pane so that users can navigate a very large canvas
 * in small space.  The invoker respnds to button presses
 * and invokes the right method in the explorer.
 */
public class GlassCard implements ActionListener, PropertyChangeListener{
	/** the index of the button in the explorer */
	private int index;
	/** the parent explorer */
	private Explorer explorer;
	/** the canvas that is wrapped by this card */
	private Canvas canvas;
	/** The button of this */
	private CButton button;
	/** The scroll that canvas lives in */
	private CScrollPane scroll;
	private final static int SCROLLBAR_WIDTH = 18;
	
	/**
	 * constructor
	 * @param i
	 * @param canvas
	 * @param ex
	 */
	GlassCard(int i, Canvas canvas, GlassExplorer ex){
		this.index= i;
		this.explorer = ex;
		this.canvas = canvas;
		this.button = new GlassButton(canvas.getColor(), canvas.getColor().brighter().brighter().brighter(),canvas.getName());
		//this.button.setMaximumSize(new Dimension(0,10));
		//this.button.setPreferredSize(new Dimension(0,10));
		this.scroll = new CGlassScrollPane(
				canvas.getJComponent(),
				ScrollPolicy.VERTICAL_BAR_AS_NEEDED,
				ScrollPolicy.HORIZONTAL_BAR_NEVER,
				SCROLLBAR_WIDTH, canvas.getColor(), 
				new Color(100,100,100,100));
		canvas.getJComponent().setOpaque(false);
        button.addActionListener(this);
        canvas.getJComponent().addPropertyChangeListener(this);
		this.scroll.setPreferredSize(
    			new Dimension(canvas.getJComponent().getPreferredSize().width + SCROLLBAR_WIDTH, 
    						  canvas.getJComponent().getPreferredSize().height));
	}
	/**
	 * When the user presses the button, the explorer selects the
	 * corresponding canvas
	 */
	public void actionPerformed(ActionEvent e){
		explorer.selectCanvas(index);		
	}
	/**
	 * @return the button
	 */
	JComponent getButton(){
		return button;
	}
	/**
	 * @return the scroll
	 */
	JComponent getScroll(){
		return scroll;
	}
	/**
	 * @return the background color of the glass pane
	 */
	Color getBackgorundColor(){
		return new Color(
				canvas.getColor().getRed(),
				canvas.getColor().getGreen(),
				canvas.getColor().getBlue(),150);
	}
    public void propertyChange(PropertyChangeEvent e){
    	if (e.getPropertyName().equals(Canvas.LABEL_CHANGE)){
    		button.repaint();
    	}
    	if (e.getSource().equals(canvas) && e.getPropertyName().equals("preferredSize")) {
    		this.scroll.setPreferredSize(
    			new Dimension(canvas.getJComponent().getPreferredSize().width + SCROLLBAR_WIDTH, 
    						  canvas.getJComponent().getPreferredSize().height));
    	} 
    }
	private class GlassButton extends CButton{
		private static final long serialVersionUID = 328149080429L;
		//To get the shadow effect the text must be displayed multiple times at
		//multiple locations.  x represents the center, white label.
		// o is color values (0,0,0,0.5f) and ¥ is black.
		//			  o o
		//			o x ¥ o
		//			o ¥ o
		//			  o
		//offsetArrays representing the translation movement needed to get from
		// the center location to a specific offset location given in {{x,y},{x,y}....}
		//..........................................grey points.............................................black points
		private final int[][] shadowPositionArray = {{0,-1},{1,-1}, {-1,0}, {2,0},	{-1,1}, {1,1},  {0,2}, 	{1,0},  {0,1}};
		private final float[] shadowColorArray =	{0.5f,	0.5f,	0.5f, 	0.5f, 	0.5f, 	0.5f,	0.5f,	0,		0};
		private double offsetSize = 1;
		public GlassButton(Color buttonColor, Color selectedColor, String text){
			super(buttonColor, selectedColor, text);
		}
		public void paint(Graphics g){
			// Set up graphics and buffer
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			BufferedImage buffer = GraphicsManager.gc.createCompatibleImage(this.getWidth(), this.getHeight(), Transparency.TRANSLUCENT);
			Graphics2D gb = buffer.createGraphics();
			gb.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			// Set up first layer
			int buttonHeight = this.getHeight()-INSET*2;
			int buttonWidth = this.getWidth()-INSET*2;
			int arc = buttonHeight;
			
			if(this.pressed || this.selected){
				g2.setPaint(new GradientPaint(0, -buttonHeight, Color.darkGray, 0, buttonHeight, canvas.getColor(), false));
				g2.fillRoundRect(INSET, INSET, buttonWidth, buttonHeight, arc, arc);
				g2.setColor(Color.darkGray);
				g2.drawRoundRect(INSET, INSET, buttonWidth, buttonHeight, arc, arc);
			}else{
				//paint highlightlayer
				if(this.focus){
					gb.setColor(Color.yellow);
					gb.setStroke(new BasicStroke(3));
					gb.drawRoundRect(INSET, INSET, buttonWidth, buttonHeight, arc, arc);
					gb.setStroke(new BasicStroke(1));
				}
				// Paint the first layer
				gb.setColor(canvas.getColor().darker());
				gb.fillRoundRect(INSET, INSET, buttonWidth, buttonHeight, arc, arc);
				gb.setColor(Color.darkGray);
				gb.drawRoundRect(INSET, INSET, buttonWidth, buttonHeight, arc, arc);
				
				// set up paint data fields for second layer
				
				int highlightHeight = buttonHeight*2/3;
				int highlightWidth = buttonWidth;
				int highlightArc = highlightHeight;
				
				// Paint the second layer
				gb.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,.8f));
				
				gb.setColor(canvas.getColor());
				gb.setClip(new RoundRectangle2D.Float(INSET,INSET,highlightWidth,highlightHeight,highlightArc,highlightArc));
				gb.fillRoundRect(INSET, INSET, buttonWidth, buttonHeight, arc, arc);
				
				// Blur
				ConvolveOp blurOp = new ConvolveOp(new Kernel(3, 3, BLUR));
				BufferedImage blurredImage = blurOp.filter(buffer, null);
				
				// Draw button
				g2.drawImage(blurredImage, 1, 1, null);
			}
			// Draw the text (if any)
			String text = canvas.getName();
			if(text != null && buttonHeight > 4){
				//Font font = g2.getFont().deriveFont((float)(((float)this.getHeight()) * .6));
				Font font = g2.getFont().deriveFont((float)(this.getHeight()-INSET*2-2)*.7f);
				g2.setFont(font);
				FontMetrics metrics = g2.getFontMetrics();
				Rectangle2D textBounds = metrics.getStringBounds(text,g2);
				float x = (float)((this.getWidth() / 2) - (textBounds.getWidth() / 2));
				float y = (float)((this.getHeight() / 2) + (textBounds.getHeight() / 2)) - metrics.getDescent();
				
				g.setColor(Color.black);
				for (int i = 0; i < shadowPositionArray.length; i++) {
					int dx = shadowPositionArray[i][0];
					int dy = shadowPositionArray[i][1];
					g2.setColor(new Color(0,0,0, shadowColorArray[i]));
					g2.drawString(text, x+(int)((dx)*offsetSize), y+(int)((dy)*offsetSize));
				}
				if(canvas.getHighlight() != null){
					g.setColor(canvas.getHighlight());
				}else{
					g.setColor(Color.white);
				}
				g2.drawString(text, x, y);
			}
			if(canvas.getHighlight() != null){
				g2.setStroke(new BasicStroke(3));
				g2.setColor(canvas.getHighlight());
				g2.drawRoundRect(INSET+1, INSET+1, buttonWidth-2, buttonHeight-2, arc, arc);
				g2.setStroke(new BasicStroke(1));
			}
		}
	}
}