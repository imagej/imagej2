package codeblockutil;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class CHeader {
	private CHeader(){}
	public static JComponent makeBasicHeader(
			JComponent factory,
			JComponent[] buttons,
			JComponent search){
		return new BasicHeader(
				factory,
				buttons,
				search);
	}
	private static class BasicHeader extends JPanel{
		private static final long serialVersionUID = 328149080219L;
		protected BasicHeader(JComponent factory, JComponent[] buttons, JComponent search){
			super();
			this.setOpaque(false);
			double[][] size = {{170, TableLayoutConstants.FILL, search.getPreferredSize().width+10},{45}};
			this.setLayout(new TableLayout(size));
			this.add(factory, "0, 0");
			JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.LEADING, 35, 3));
			buttonPane.setOpaque(false);
			for(int j = 0; j<buttons.length; j++){
				buttonPane.add(buttons[j]);
			}
			this.add(buttonPane, "1, 0");
			JPanel searchPane = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 10));
			searchPane.setOpaque(false);
			searchPane.add(search);
			this.add(searchPane, "2 , 0");
			
		}
    	public void paint(Graphics g){
    		Graphics2D g2 = (Graphics2D )g;
    		g2.setPaint(new GradientPaint(0, 0, Color.darkGray, 0, this.getHeight(), Color.DARK_GRAY, false));
    		g2.fillRect(0,0,this.getWidth(), this.getHeight()/2);
    		g2.setPaint(new GradientPaint(0, 0, Color.darkGray, 0, this.getHeight(), Color.black, false));
    		g2.fillRect(0,this.getHeight()/2,this.getWidth(), this.getHeight()/2);
    		super.paint(g);
    	}
	}
}
