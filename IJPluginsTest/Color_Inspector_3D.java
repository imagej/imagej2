/*
 * Color_Inspector_3D
 *
 * (C) Author:  Kai Uwe Barthel: barthel (at) fhtw-berlin.de 
 * 
 
 * This ImageJ-PlugIn can display the distribution of the colors of  
 * an image in a 3D color space. 
 * The color distribution can be shown in RGB, YUV, YCbCr, HSB and Lab space. 
 * 
 * Version 1.71			January 25 2005 
 * 		corrected histogram list
 * 
 * Version 1.7			January 20 2005
 * 		Different color quantization methods (uniform, Median Cut, and Xiaolin Wu's scheme)
 * 		Histogram bin size can be chosen
 * 		SHIFT-click in the left window prints the color percentages for histograms
 * 		Distribution can be saved as image
 *
 * Version 1.631		November 1 2004
 * 		Quantization effect for the calculation of the histogram is shown in the image
 * 		histogram bins colors can be chosen as centers and as means of the colors within each bin
 * 		problem for images with 101 colors fixed
 * 
 * Version 1.6		October, 29 2004
 * 		Automatic rotation of the cube
 *  	Histogram functions included 
 * 		corrected frequency weighted display
 * 		
 * Version 1.5
 * 		Added XYZ, Luv, and xyY color space, Lab corrected 
 * 		Device independent color spaces assume D50 illuminant and sRGB data
 * 		Reduced memory consumption
 * 
 * Version 1.41
 * 		new cursors
 * 		display of color values
 * 
 * Version 1.4           September, 26 2004
 * 		corrected z-depth for text 
 * 		display of axes can be switched
 * 		distribution image can be scaled 
 * 		distribution image can shifted (mouse drag + SHIFT-key)
 * 		better display when taking into account frequency 
 * 
 * Version 1.3           September, 20 2004
 * 		Added perspective view, 
 * 		HMMD color space,
 * 		corrected z-depth for colors and lines 
 * 
 * Version 1.2           June, 23 2004
 * 		Added LAB + YCbCr color space  
 * 
 * Version 1.1           June, 21 2004
 * 		Added support for Masks and minor bug fixes and speedups
 * 
 * Version 1.0           June, 16 2004
 * 		First version
 * 
 */
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.NewImage;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;



public final class Color_Inspector_3D implements PlugIn {
	
	private final static String version = "v1.71"; 
	private final static String [] colorSpaceName = { "RGB", "YUV", "YCbCr", "HSB / HSV", "HMMD", "Lab", "Luv", "xyY", "XYZ"} ;
	private final static int NUM_COLOR_SPACES = colorSpaceName.length;
	
	private  final static int OPAQUE = 0xFF000000;
	private  boolean axes = true;
	private  boolean colorMode = true;
	
	private  int mode = 0;
	private  boolean shift = false;
	private  boolean hist = false;
	private  boolean center = true;
	private  boolean pause = false;
	private  boolean move = false;
	
	private  float delta = 1; 
	
	private  int numberOfColors;
	private  int maskSize; // ROI size
	private  ColHash[] colHash; 
	private  float freqFactor;
	
	
	public void run(String arg) {
		
		ImagePlus imp = WindowManager.getCurrentImage();
		
		if (imp==null) {
			IJ.showMessage("Image required");
			return;
		}
		
		final CustomWindow  cw = new CustomWindow();
		
		cw.init(imp);
			
		
		final Frame f = new Frame("Color Inspector 3D (" +  version + ")  Distribution of Image Colors");
		
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				cw.cleanup();
				f.dispose();
			}
		});
		f.setLocation(200,150);
		f.add(cw);
		
		f.pack(); 
		f.setResizable(false);
		Insets ins = f.getInsets();
		
		cw.totalSize.height = cw.H + ins.bottom + ins.top + 65;
		cw.totalSize.width  = cw.leftSize.width + cw.WR + ins.left + ins.right;
		
		f.setSize(cw.totalSize);
		
		f.setVisible(true);
		cw.requestFocus();
		
		cw.addKeyListener ( 
				new KeyAdapter() 
				{ 
					public void keyPressed (KeyEvent e){ 
						if (e.isShiftDown()) 
							shift = true;	
					} 
					public void keyReleased (KeyEvent e){ 
						if (!e.isShiftDown())
							shift = false;
					} 
				}  
		); 
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	class CustomWindow extends JPanel implements 
	MouseListener, MouseMotionListener, ChangeListener, ActionListener, ItemListener {
		
		protected  Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
		protected  Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
		protected  Cursor moveCursor = new Cursor(Cursor.MOVE_CURSOR);
		
		private Picture pic1, pic2;
		
		private JPanel imagePanel;
		
		private JCheckBox checkColor, checkAxes;
		
		private JComboBox displayChoice; 
		private JComboBox colorSpaceChoice; 
		private String [] displayName; 
		private final String [] displayNameLong = 
			{ "All Colors", "Frequency Weighted", "Histogram", "Median Cut", "Wu Quant"	};
		private final String [] displayNameShort =
			{ "All Colors", "Frequency Weighted", "Histogram"};
		private JSlider slider0, slider1, slider2; 
		
		private boolean drag = false; 
		private int checkMove = 0;
		private int xStart, yStart, xAct, yAct;
		
		private int xdiff, ydiff;
		
		private float dx, dy;
		
		private ImageRegion imageRegion1, imageRegion2;
		
		private Dimension totalSize = new Dimension(); 
		private Dimension leftSize  = new Dimension(); 
		private Dimension rightSize = new Dimension(); 
		
		//  do not change sizes !
		private final static int H = 512;
		private final static int WL = 512;
		private final static int WR = 512; 
		
		private TurnThread thread;
		private int qMode;
		
		
		void cleanup() {
			pic1.pixels = null;
			pic1.pixelsAlpha = null;
			pic1.pixelsZ = null;
			pic1 = null;
			
			pic2.pixels = null;
			pic2.pixelsAlpha = null;
			pic2.pixelsZ = null;
			pic2 = null;
			
			colHash = null;
			
			imagePanel = null;
			thread.interrupt();
		}
		
		
		void init(ImagePlus imp) {
			
			setLayout(new BorderLayout());
			
			imagePanel = new JPanel();
			imagePanel.setBackground(Color.lightGray);
			imagePanel.setLayout(new BorderLayout());
			
			// on the left the original image
			imageRegion1 = new ImageRegion();
			
			pic1 = new Picture(imp);  
			
			imageRegion1.setImage(pic1);
			pic1.setImageRegion(imageRegion1);
			
			leftSize.width = Math.min( WL, pic1.getWidth()); 
			float scalew = (float)leftSize.width/pic1.getWidth();
			leftSize.height  = Math.min( H, pic1.getHeight()); 
			float scaleh = (float)leftSize.height/pic1.getHeight();
			
			float scale = Math.min(scaleh, scalew);
			
			leftSize.width = (int)(scale*pic1.getWidth()); 
			leftSize.height= (int)(scale*pic1.getHeight()); 
			
			imageRegion1.setWidth(leftSize.width);
			imageRegion1.setHeight(leftSize.height);
			
			imageRegion1.addMouseMotionListener(this);
			imageRegion1.addMouseListener(this);
			
			// on the right the color distribution 
			imageRegion2 = new ImageRegion();
			imageRegion2.setBackground(Color.lightGray);
			
			imageRegion2.addMouseMotionListener(this);
			imageRegion2.addMouseListener(this);
			
			pic2 = new Picture(WR, H);
			
			imageRegion2.setImage(pic2);
			pic2.setImageRegion(imageRegion2);
			
			pic1.countColors();
			pic1.findUniqueColors();
			pic2.initTextsAndDrawColors();
			
			if (pic1.numberOfColorsOrig <= 256)
				displayName = displayNameShort;
			else
				displayName = displayNameLong;	
			
			imagePanel.add(imageRegion1,BorderLayout.CENTER);
			imagePanel.add(imageRegion2,BorderLayout.EAST);
			
			add(imagePanel, BorderLayout.SOUTH);
			
			imageRegion1.repaint();	
			imageRegion2.repaint();	
			
			JPanel buttonPanel1 = new JPanel();
			buttonPanel1.setLayout(new GridLayout(1,4,5,0));
			
			// display modes
			displayChoice = new JComboBox(displayName);
			displayChoice.addActionListener(this);			
			buttonPanel1.add(displayChoice);
			
			// color model (space)
			colorSpaceChoice = new JComboBox(colorSpaceName);
			colorSpaceChoice.addActionListener(this);	
			buttonPanel1.add(colorSpaceChoice);
			
			JPanel miniPanel = new JPanel();
			miniPanel.setLayout(new GridLayout(1,2));
			
			checkColor = new JCheckBox("Colored");
			checkColor.setSelected(true);
			checkColor.addItemListener(this);
			miniPanel.add(checkColor);
			
			checkAxes = new JCheckBox("Axes + Text");
			checkAxes.setSelected(true);
			checkAxes.addItemListener(this);
			miniPanel.add(checkAxes);
			
			buttonPanel1.add(miniPanel);
			
			JButton button = new JButton("Save Distribution");
			button.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	            	imageRegion2.saveToImage();
	            }
	        });
			buttonPanel1.add(button); 
			
			
			JPanel buttonPanel2 = new JPanel();
			buttonPanel2.setLayout(new GridLayout(1,3,5,0));
						
			Border empty = BorderFactory.createTitledBorder( BorderFactory.createEmptyBorder() );
			
			slider0 = new JSlider(JSlider.HORIZONTAL, 1, 256, 100 );
			slider0.setBorder( new TitledBorder(empty,
					"Number of Colors", TitledBorder.CENTER, TitledBorder.TOP,
					new Font("Sans", Font.PLAIN, 12)));
			slider0.addChangeListener( this );
			slider0.addMouseListener(this);
			slider0.setEnabled(false);
			buttonPanel2.add(slider0); 
			
			slider1 = new JSlider(JSlider.HORIZONTAL, 50, 118, 84 );
			slider1.setBorder( new TitledBorder(empty,
					"Perspective", TitledBorder.CENTER, TitledBorder.TOP,
					new Font("Sans", Font.PLAIN, 12)));
			slider1.addChangeListener( this );
			slider1.addMouseListener(this);
			buttonPanel2.add(slider1); 
			
			slider2 = new JSlider(JSlider.HORIZONTAL, 0, 30, 15 );
			slider2.setBorder( new TitledBorder(empty,
					"Scale", TitledBorder.CENTER, TitledBorder.TOP,
					new Font("Sans", Font.PLAIN, 12)));
			slider2.addChangeListener( this );
			slider2.addChangeListener( this );
			slider2.addMouseListener(this);
			buttonPanel2.add(slider2); 
			
			add(buttonPanel1, BorderLayout.NORTH);
			add(buttonPanel2, BorderLayout.CENTER);
			validate();
			
			thread = new TurnThread(pic2, imageRegion2);
			thread.start();
			super.setCursor(defaultCursor);	
		}
		
		public void stateChanged( ChangeEvent e ){
		    JSlider slider = (JSlider)e.getSource();

		    float sliderValue0, sliderValue1, sliderValue2; 
			
		    if (slider == slider0) {
		    	sliderValue0 = slider0.getValue();
		    	if (qMode == 0) {
		    		delta = (float) (256/Math.pow((6*(sliderValue0+3)), 1/3.));	
		    		pic1.quantize();
		    	}
		    	else if (qMode == 1) {
		    		pic1.quantizeMedianCut((int)sliderValue0);
		    	}
		    	else if (qMode == 3) {
		    		pic1.wu_quant((int)sliderValue0);
		    	}
		    	
		    	pic1.findUniqueColors();
				imageRegion1.repaint();
				pic2.initTextsAndDrawColors();
				pic2.copyColors();
				imageRegion2.repaint();
		    }
		    if (slider == slider1) {
		    	sliderValue1 = 75 - slider1.getValue()/2.f; 
		    	if ( sliderValue1 == 50)
		    		sliderValue1 = 100000;
		    	sliderValue1 = sliderValue1*sliderValue1;
		    	pic2.setD(sliderValue1);
		    }
			if (slider == slider2) {
		    	sliderValue2 = slider2.getValue()-15;
		    	float scale = (float) Math.pow(1.05,sliderValue2);
		    	
		    	pic2.setScale(scale); 
		    }
		    
		    pic2.newDisplayMode();
			imageRegion2.repaint();
			super.requestFocus();
		}
		
		public void actionPerformed(ActionEvent e) {
			JComboBox cb = (JComboBox)e.getSource();
			if (cb == displayChoice) {
				String name = (String)cb.getSelectedItem();
				hist = true;
				center = true;
				
				if (name.equals(displayName[0])) { // all colors 
					pic1.copyOrigPixels();
					hist = false;
					mode = mode & 1;
					slider0.setEnabled(false);
				}
				else if (name.equals(displayName[1])) { // frequency weighted
					pic1.copyOrigPixels();
					hist = false;
					mode = (mode & 1) + 2;
					slider0.setEnabled(false);
				}
				else if (name.equals(displayName[2])) { // histogram AxBxC
					slider0.setEnabled((pic1.numberOfColorsOrig > 256) ? true : false);
					qMode = 0;
					delta = (float) (256/Math.pow((6*(slider0.getValue()+3)), 1/3.));	
					pic1.quantize();
				}
				else if (name.equals(displayName[3])) { // median cut
					slider0.setEnabled((pic1.numberOfColorsOrig > 256) ? true : false);
					qMode = 1;
					pic1.quantizeMedianCut(slider0.getValue()); //
				}
				else if (name.equals(displayName[4])) { //  Wu Quant
					slider0.setEnabled((pic1.numberOfColorsOrig > 256) ? true : false);
					qMode = 3;
					pic1.wu_quant(slider0.getValue()); //
				}
				
				pic1.findUniqueColors();
				imageRegion1.repaint();
				
				pic2.copyColors();
			}
			
			if (cb == colorSpaceChoice) {
				String name = (String)cb.getSelectedItem();
				
				for (int cs = 0; cs < NUM_COLOR_SPACES; cs++) {
					if (name.equals(colorSpaceName[cs])) {
						pic2.setColorSpace(cs);
						pic2.copyColors();
						break;
					}
				}	
			}
				
			pause = false;
			
			pic2.initTextsAndDrawColors();
			imageRegion2.repaint();
			super.requestFocus();
			
			if (!thread.isAlive()) {
				thread = new TurnThread(pic2, imageRegion2);
				thread.start();
			}
		}
		
		public synchronized void itemStateChanged(ItemEvent e) {
			Object source = e.getItemSelectable();
			
			pause = true; 
			
			if (source == checkColor) {
				if (e.getStateChange() == ItemEvent.DESELECTED) {
					colorMode = false;
					mode = (mode & 2) + 1;
				}
				if (e.getStateChange() == ItemEvent.SELECTED) {
					colorMode = true;
					mode = (mode & 2);
				}
			}
			else if (source == checkAxes) {
				if (e.getStateChange() == ItemEvent.DESELECTED) {
					axes = false;
				}
				if (e.getStateChange() == ItemEvent.SELECTED) {
					axes = true;
				}
				imageRegion2.setAxes(axes);
			}
			
			pause = false;
			
			pic2.initTextsAndDrawColors();
			imageRegion2.repaint();
			super.requestFocus();
			
			if (!thread.isAlive()) {
				thread = new TurnThread(pic2, imageRegion2);
				thread.start();
			}
		}
		
		public void mouseClicked(MouseEvent arg0) {
			Object source = arg0.getSource();
			if (source == imageRegion2) {
				move = false;		
			}	
		}
		
		public void mouseEntered(MouseEvent arg0) {
			Object source = arg0.getSource();
			if (source == imageRegion2) {
				super.setCursor(moveCursor);		
			}
			else if (source == imageRegion1) {
				super.setCursor(handCursor);
			}
			
		}
		public void mouseExited(MouseEvent arg0) {
			Object source = arg0.getSource();
			
			if (source == imageRegion1) {
				imageRegion2.setDot(false);
				imageRegion2.setText("", 6);
				imageRegion2.repaint();
			}
			super.setCursor(defaultCursor);
		}
		
		public void mousePressed(MouseEvent arg0) {
			Object source = arg0.getSource();
			
			if (source == imageRegion2) {
				checkMove = 0;
				xStart = arg0.getX();
				yStart = arg0.getY();
				
				drag = true;
				
				dx = dy = 0;
				xdiff = 0;
				ydiff = 0;
				
			}
			else if (source == imageRegion1) {
				int xPos = arg0.getX();
				int yPos = arg0.getY();
				
				if (!shift) {
					if (xPos > 0 && xPos < leftSize.width && yPos > 0 && yPos < leftSize.height) {
						xPos = (xPos*pic1.getWidth() )/leftSize.width;
						yPos = (yPos*pic1.getHeight())/leftSize.height;
						
						int c = pic1.getColor(xPos, yPos);
						
						pic2.showColorDot(c);
					}
				}
				else {
					pic1.printColors();
					shift = false;
				}
					
			}	
		}
		
		public void mouseReleased(MouseEvent arg0) {
			Object source = arg0.getSource();
			drag = false;	
			if (source == imageRegion2) {
				checkMove = 3;
			}
		}
		
		public void mouseDragged(MouseEvent arg0) {
			Object source = arg0.getSource();
			
			if (source == imageRegion2) {
				if (drag == true) {
					checkMove = 0;
					move = false;
					xAct = arg0.getX();
					yAct = arg0.getY();
					xdiff = xAct - xStart;
					ydiff = yAct - yStart;
					
					dx = (5*dx + xdiff)/6.f;
					dy = (5*dy + ydiff)/6.f;
					
					if (shift == false) 
						pic2.setMouseMovement((float)xdiff, (float)ydiff);
					else
						pic2.setMouseMovementOffset(xdiff, ydiff);
					xStart = xAct;
					yStart = yAct;
				}
				
				pic2.newDisplayMode();
				
				imageRegion2.repaint();
			}	
			else if (source == imageRegion1) {
				int xPos = arg0.getX();
				int yPos = arg0.getY();
				
				if (xPos > 0 && xPos < leftSize.width && yPos > 0 && yPos < leftSize.height) {
					xPos = (xPos*pic1.getWidth())/leftSize.width;
					yPos = (yPos*pic1.getHeight())/leftSize.height;
					
					int c = pic1.getColor(xPos, yPos);
					
					pic2.showColorDot(c);
				}
			}
			super.requestFocus();  
			
			if (!thread.isAlive()) {
				thread = new TurnThread(pic2, imageRegion2);
				thread.start();
			}
		}
		
		public void mouseMoved(MouseEvent arg0) {
			Object source = arg0.getSource();
		
			if (source == imageRegion2) {
				if (checkMove > 0 && (dx != 0 || dy != 0))
					move = true;
			}
		}


		//////////////////////////////////////////////////////////////
		
		class TurnThread extends Thread {
			
			private Picture pic;
			private ImageRegion ir;
			
			public TurnThread (Picture picture, ImageRegion imageRegion) { 
				pic = picture; 
				ir = imageRegion;
			}
			
			public void run() {
				long tna = 0, tn = 0; 
				long tm = System.currentTimeMillis();
				float fps = 0;
				int delay;
				
				try {
					while (!interrupted()) {
				
						if (move && !pause) {
							delay = 25;
							
							pic.setMouseMovement(dx, dy);
							pic.newDisplayMode();
							
							if (ir.dot == true) {
								pic.setDot();
								ir.setDot(pic.getX(), pic.getY());
							}
							long dt = (tna == 0 || tn == 0) ? 0 : (tn - tna); 
							if (dt > 0) {
								if (fps == 0)
									fps = 10000.f/dt;
								else
									fps = (2*fps + 10000.f/dt)/3.f;
								ir.setText((Misc.fm(4,((int)fps/10.))+ " fps"),5);
							}
							ir.repaint();
							tna = tn;
							tn = System.currentTimeMillis();
						}
						else { 
							delay = 200;
							ir.setText("", 5);
							ir.repaint();
							fps = 0;
							checkMove--; 
						}
						
						tm += delay;
						
						sleep(2+Math.max(0, tm - System.currentTimeMillis()));
						
					}
				} catch (InterruptedException e){}
			} 
			
		}
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	
	class ImageRegion extends JPanel { 
		private Image image;
		private int width;
		private int height;
		
		private float scale = 1;
		private int xPos;
		private int yPos;
		
		private TextField[] textField = null;
		private Lines[] lines = null;
		
		private boolean dot = false;
		private boolean axes = true;
		
		private Color planeColor = Color.lightGray;
		
		private Font font1 = new Font("Sans", Font.PLAIN, 18);
		private Font font2 = new Font("Sans", Font.PLAIN, 15);
		
		private int plotNumber = 1;
		
		public void setDot(int xPos, int yPos) {
			dot = true;
			this.xPos = xPos;
			this.yPos = yPos;	
		}
		public void setDot(boolean b) {
			dot = b;
		}
		public void setPlaneColor(Color color) {
			planeColor = color; 
		}
		
		public void newText(int n) {
			textField = new TextField[n];
			for (int i = 0; i < n; i++)
				textField[i] = new TextField();
		}
		
		public void setText(String text, int i, int posx, int posy, int z, Color color) {
			textField[i].setText(text);
			textField[i].setXpos(posx);
			textField[i].setYpos(posy);
			textField[i].setColor(color);
		}
		
		public void setText(String text, int i, Color color) {
			textField[i].setText(text);
			textField[i].setColor(color);
		}
		public void setText(String text, int i) {
			textField[i].setText(text);
		}
		
		public void setTextPos(int i, int posx, int posy, int z) {
			textField[i].setXpos(posx);
			textField[i].setYpos(posy);
			textField[i].setZ(z);
		}
		
		public void newLines(int n) {
			lines = new Lines[n];
			for (int i = 0; i < n; i++)
				lines[i] = new Lines();
		}
		
		public void setLine(int i, int x1, int y1, int x2, int y2, int z, Color color) {
			lines[i].setPos(x1, y1, x2, y2, z, color);
		}
		
		public void setImage(Picture pic){
			height = pic.getHeight();
			width = pic.getWidth();
			image = pic.getImage();
		}
		
		public void setImage(Image image){
			this.image = image;
		}
		
		synchronized void saveToImage() {
			
			pause = true;
			
			BufferedImage bufferedImage =  new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			
			paint(bufferedImage.createGraphics());
			
			Graphics2D g2d = bufferedImage.createGraphics();
			g2d.setColor(Color.black);
			g2d.drawString("Color Inspector 3D", width - 180, height - 10); 
			g2d.dispose();
			
			String s = "Color Distribution "+plotNumber;
			ImagePlus plotImage = NewImage.createRGBImage (s, width, height, 1, NewImage.FILL_BLACK);
			ImageProcessor ip = plotImage.getProcessor();
			int[] pixels = (int[]) ip.getPixels();
			bufferedImage.getRGB(0, 0, width, height, pixels, 0, width);
			
			plotImage.show();
			plotImage.updateAndDraw();	
			
			plotNumber++;
			pause = false;
		}
		
		//-------------------------------------------------------------------
		public void paint(Graphics g) {
			
			g.setColor(planeColor);
			g.fillRect(0, 0, width, height);
			
			g.setFont(font1); 
			
			if (textField != null && axes == true)
				for (int i=0; i<textField.length; i++) {
					if (textField[i] != null) 
						if (textField[i].getZ() > 0) {
							g.setColor(textField[i].getColor());
							g.drawString(textField[i].getText(), 
									textField[i].getXpos(), textField[i].getYpos());
						}
				}
			
			if (lines != null  && axes == true)
				for (int i=0; i<lines.length; i++) {
					if (lines[i] != null) 
						if (lines[i].z > 0) {
							g.setColor(lines[i].color);
							g.drawLine(lines[i].x1, lines[i].y1, lines[i].x2, lines[i].y2);
						}		
				}
			
			if (image != null ) {
				int xstart = (int) -((scale - 1)*width/2.); 
				int ystart = (int) -((scale - 1)*height/2.); 
				
				g.drawImage(image, xstart, ystart, (int)(scale*width), (int)(scale*height), this);
			}
			
			if (lines != null  && axes == true)
				for (int i=0; i<lines.length; i++) {
					if (lines[i] != null) 
						if (lines[i].z <= 0) {
							g.setColor(lines[i].color);
							g.drawLine(lines[i].x1, lines[i].y1, lines[i].x2, lines[i].y2);
						}		
				}
			
			if (textField != null && axes == true)
				for (int i=0; i<textField.length; i++) {
					if (textField[i] != null) 
						if (textField[i].getZ() <= 0) {
							if (i > 3)
								g.setFont(font2);
							
							g.setColor(textField[i].getColor());
							g.drawString(textField[i].getText(), 
									textField[i].getXpos(), textField[i].getYpos());
						}
				}
			
			if (dot == true) {				
				g.setColor(Color.orange);
				g.drawLine(xPos-3, yPos-2 ,  xPos-10, yPos-2);
				g.drawLine(xPos-3, yPos-1 ,  xPos-10, yPos-1);
				g.drawLine(xPos-3, yPos+2 ,  xPos-10, yPos+2);
				g.drawLine(xPos-3, yPos+3 ,  xPos-10, yPos+3);
				g.drawLine(xPos+4, yPos-2 ,  xPos+10, yPos-2);
				g.drawLine(xPos+4, yPos-1 ,  xPos+10, yPos-1);
				g.drawLine(xPos+4, yPos+2 ,  xPos+10, yPos+2);
				g.drawLine(xPos+4, yPos+3 ,  xPos+10, yPos+3);
				g.drawLine(xPos-2, yPos-3 ,  xPos-2,  yPos-10);
				g.drawLine(xPos-1, yPos-3 ,  xPos-1,  yPos-10);
				g.drawLine(xPos+2, yPos-3 ,  xPos+2,  yPos-10);
				g.drawLine(xPos+3, yPos-3 ,  xPos+3,  yPos-10);
				g.drawLine(xPos-2, yPos+4 ,  xPos-2,  yPos+10);
				g.drawLine(xPos-1, yPos+4 ,  xPos-1,  yPos+10);
				g.drawLine(xPos+2, yPos+4 ,  xPos+2,  yPos+10);
				g.drawLine(xPos+3, yPos+4 ,  xPos+3,  yPos+10);
				
				g.setColor(Color.black);
				g.drawLine(xPos-3, yPos ,    xPos-12, yPos);
				g.drawLine(xPos-3, yPos+1 ,  xPos-12, yPos+1);
				g.drawLine(xPos+4, yPos ,    xPos+12, yPos);
				g.drawLine(xPos+4, yPos+1 ,  xPos+12, yPos+1);
				g.drawLine(xPos,   yPos-3 ,  xPos,   yPos-12);
				g.drawLine(xPos+1, yPos-3 ,  xPos+1, yPos-12);
				g.drawLine(xPos,   yPos+4 ,  xPos,   yPos+12);
				g.drawLine(xPos+1, yPos+4 ,  xPos+1, yPos+12);
			}
		}
		//-------------------------------------------------------------------
		
		public void update(Graphics g) {
			paint(g);
		}
		public Dimension getPreferredSize() {
			return new Dimension(width, height);
		}
		public int getHeight() {
			return height;
		}
		public void setHeight(int height) {
			this.height = height;
		}
		public int getWidth() {
			return width;
		}
		public void setWidth(int width) {
			this.width = width;
		}
		public void setScale(float scale) {
			this.scale = scale;
		}
		public void setAxes(boolean axes) {
			this.axes = axes;
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private  class Picture {
		private int R, G, B; // color coordinates
		private int X, Y, Z; // screen coordinates
		private int dotR, dotG, dotB;
		
		
		private int numberOfColorsOrig = 0;
		
		private Image  image;    // AWT-Image
		
		final static int RGB = 0;
		final static int YUV = 1;
		final static int YCBCR = 2;
		final static int HSB = 3;
		final static int HMMD = 4;
		final static int LAB = 5;
		final static int LUV = 6;
		final static int XYY = 7;
		final static int XYZ = 8;
		
		//final static int NUM_COLOR_SPACES = 9;
		
		private final static int dm = 18; 
		private final static int dp = 10;
		
		private final  int [][] cubeCornersRGB =  { 
				{-128,-128,-128}, // VUL
				{-128, 127, 127}, // VOR
				{ 127,-128, 127}, // HUR
				{ 127, 127,-128}, // HOL
				{-128,-128, 127}, // VUR
				{-128, 127,-128}, // VOL
				{ 127,-128,-128}, // HUL
				{ 127, 127, 127}  // HOR
		};
		
		private final  int [][] lineEndsYUV =  { 
				{   0,   0,-128}, // Y U
				{-128,   0,-128}, // U L
				{   0,-128,-128}, // V V
				{   0,   0, 127}, // Y 0
				{ 127,   0,-128}, // U L
				{   0, 127,-128}  // V H
		};
		private final  int [][] lineEndsHSB =  { 
				{   0,   0,-128}, // B U
				{   0,   0,-128}, // S M
				{   0,   0, 127}, // B O
				{   0, 127,-128},  
		};
		private final  int [][] lineEndsHMMD =  { 
				{   0,   0,-128}, // B U
				{   0,   0,   0}, // S M
				{   0,   0, 127}, // B O
				{ 127,   0,   0}  // S 
		};
		private final  int [][] lineEndsLAB = lineEndsYUV;
		
		
		private final int [][] textPosRGB = {
				{ -128-dm, -128-dm, -128-dm}, // 0
				{  127+dp, -128-dm, -128-dm}, // R
				{ -128-dm,  127+dp, -128-dm}, // G
				{ -128-dm, -128-dm,  127+dp}  // B
		};
		private final int [][] textPosXYY = textPosRGB;
		private final int [][] textPosXYZ = textPosRGB;
		private final int [][] textPosYUV = { 
				{       0,       0, -128-dm},  // 0
				{  127+dp,       0, -128-dm},  // U 
				{       0,  127+dp, -128-dm},  // V
				{       0,       0,  127+dp}   // Y
		};
		private final int [][] textPosYCbCr = textPosYUV; 
		private final int [][] textPosHSB = { 
				{       0,       0, -128-dm},  // 0
				{  127+dp,       0,  127+dp},  // H 
				{       0, 127+2*dp, -128-dm},  // S
				{       0,       0,  127+dp}   // B
		};
		private final int [][] textPosHMMD = { 
				{       0,       0, -128-dm},  // 0
				{       0,  127+2*dp,       0},  // Diff
				{  127+2*dp,       0,       0},  // Hue 
				{       0,       0,  127+dp}   // Sum
		};
		private final int [][] textPosLAB = { 
				{       0,       0, -128-dm},  // 0
				{  127+dp,       0, -128-dm},  // a 
				{       0,  127+dp, -128-dm},  // b
				{       0,       0,  127+dp}   // L
		};
		private final int [][] textPosLUV = textPosLAB;	
		
		private final  int [][][] textPositions =  { 
				textPosRGB,
				textPosYUV,
				textPosYCbCr,
				textPosHSB,
				textPosHMMD,
				textPosLAB,
				textPosLUV,	
				textPosXYY,
				textPosXYZ
		};
		private final  String [][] letters =  {
				{"0", "R", "G", "B"},
				{"0", "U", "V", "Y"},
				{"0", "Cb","Cr","Y"},
				{"0", "H", "S", "B"},
				{"0", "Hue", "Diff", "Sum"},
				{"0", "a", "b", "L"},
				{"0", "u", "v", "L"},
				{"0", "x", "y", "Y"},
				{"0", "X", "Y", "Z"}
		};
		private final  Color [][] letterCol =  {
				{Color.black, Color.red,  Color.green, Color.blue},
				{Color.black, Color.orange, Color.orange,  Color.orange}
		};
		private final  Color [][] letterGray =  {
				{Color.black, Color.red,  Color.green, Color.blue},
				{Color.black, Color.blue, Color.blue,  Color.blue}
		};
		
		private int colorSpace = RGB; 
		
		private ImageRegion imageRegion = null;
		private int width;   				
		private int height;  		   
		
		
		private int[] pixels = null;		
		private int[] pixelsAlpha = null;
		private int[] pixelsZ = null;		// Z-buffer
		
		private float xs; 
		private float ys; 
		private int xoff;
		private int yoff;
		
		private float xs_xoff; 
		private float ys_yoff; 
		
		private double angleB = -0.6125; // angle for B-rotation
		private double angleR = 2;       // angle for R-rotation
		
		private float d = 33*33;      // perspective
		
		private float cosB = (float)Math.cos(angleB), sinB = (float)Math.sin(angleB);
		private float cosR = (float)Math.cos(angleR), sinR = (float)Math.sin(angleR);
		
		private float cosRsinB = cosR*sinB;  
		private float cosRcosB = cosR*cosB; 
		private float sinRsinB = sinR*sinB;
		private float sinRcosB = sinR*cosB;
		
		private int[] vec = {0,0,0};
		
		private float scale = 1;
		
		private Color cubeBackColor;
		private Color cubeFrontColor;
		
		
		private MemoryImageSource source;
		
		boolean dot = false;
		private int[] pixelsOrig;
		private int numQ;
		
		
		public Picture(ImagePlus imp){
			image = imp.getImage();
			width = imp.getWidth();
			height = imp.getHeight();
			
			maskSize = width*height;
			
			xs_xoff = xs = (float)(width/2.  + 0.5);
			ys_yoff = ys = (float)(height/2. + 0.5);
			
			pixels = new int[width*height];
			pixelsOrig = new int[width*height];
			
			PixelGrabber pg =  new PixelGrabber(image, 0, 0, width, height, pixels, 0, width);
			
			try {
				pg.grabPixels();
			} catch (InterruptedException ex) {IJ.error("error grabbing pixels");}
			
			
			Roi roi = imp.getRoi();
			
			if (roi != null) {
				ImageProcessor mask = roi.getMask();
				ImageProcessor ipMask;
				
				byte[] mpixels = null;
				int mWidth = 0;
				if (mask != null) {
					ipMask = mask.duplicate();
					
					mpixels = (byte[])ipMask.getPixels();
					mWidth = ipMask.getWidth();
				}
				
				Rectangle r = roi.getBoundingRect();
				
				for (int y=0; y<height; y++) {
					int offset = y*width;
					for (int x=0; x< width ; x++) {
						int i = offset + x;
						
						if (x < r.x || x >= r.x+r.width || y < r.y || y >= r.y+r.height) {
							pixels[i] = (63 << 24) |  (pixels[i] & 0xFFFFFF) ;
							maskSize--;
						}
						else if (mask != null) { 
							int mx = x-r.x;
							int my = y-r.y;
							
							if (mpixels[my *mWidth + mx ] == 0) {
								pixels[i] = (63 << 24) |  (pixels[i] & 0xFFFFFF);
								maskSize--;
							}
						}
					}
				}
			}
			
			for (int i = 0; i< pixels.length; i++)
				pixelsOrig[i] = pixels[i];
			
			source = new MemoryImageSource(width, height, pixels, 0, width);
			
			image = Toolkit.getDefaultToolkit().createImage(source);
		}
		
		public Picture(Image image, int width, int height){
			this.image = image;
			this.width  = width;    		// Breite bestimmen
			this.height = height;  			// Hoehe bestimmen
			
			pixels = new int[width*height];
			
			PixelGrabber pg =  new PixelGrabber(image, 0, 0, width, height, pixels, 0, width);
			
			try {
				pg.grabPixels();
			} catch (InterruptedException ex) {}
			
			xs_xoff = xs = (float)(width/2.  + 0.5);
			ys_yoff = ys = (float)(height/2. + 0.5);
		}
		
		public Picture (int width, int height){
			this.width = width;
			this.height = height;
			
			pixels  = new int[width*height];
			pixelsAlpha = new int[width*height];
			pixelsZ  = new int[width*height];
			
			source = new MemoryImageSource(width, height, pixels, 0, width);
			
			image = Toolkit.getDefaultToolkit().createImage(source);
			
			xs_xoff = xs = (float)(width/2.  + 0.5);
			ys_yoff = ys = (float)(height/2. + 0.5);
		}
		
		
		public void initTextsAndDrawColors() {
			imageRegion.newText(7);  
			imageRegion.newLines(30*2 + 2); 
			
			boolean col = ((mode & 1) == 0);
			
			imageRegion.setPlaneColor( (col == true) ? new Color(0xFF777777) : Color.white );
			
			cubeBackColor  = new Color (0, 0, 0, 100);
			cubeFrontColor = (col == true) ? Color.white : Color.blue;
			
			Color color;
			for (int i= 0; i < letters[0].length; i++) {
				if (col == true)
					color = (colorSpace == RGB) ? letterCol[0][i] : letterCol[1][i]; 
				else 
					color = (colorSpace == RGB) ? letterGray[0][i] : letterGray[1][i];
				imageRegion.setText(letters[colorSpace][i], i, color);
			}
			
			String s = maskSize + " Pixels " + numberOfColors + " Colors";
			imageRegion.setText(s,   4, 10, height - 10, 1, (col == true) ? Color.white : Color.black);
			imageRegion.setText("",   5, width - 60, height - 10, 1, (col == true) ? Color.white : Color.black);
			
			imageRegion.setText("",   6, 10, 20, 1, (col == true) ? Color.white : Color.black);
			
			newDisplayMode();
		}
		
		public void setDot() {
			R = dotR;
			G = dotG;
			B = dotB;
			
			xyzPos();
		}
		
		int quant(int x) {
			x -= 128;
			if (x >= 0) {
				x+= (delta/2);
				x = (int) (x / delta);
				x *= delta;
			}
			else {
				x = -x;
				x+= delta/2;
				x = (int) (x / delta);
				x *= delta;
				x = -x;
			}
			x += 128;	
			
			if (x < 0)
				x = 0;
			if (x > 255)
				x = 255;
			
			return x;
		}
 		

		public void setScale(float s) {
			scale = s;
			imageRegion.setScale(scale);
		}
		
		public void setMouseMovement(float dx, float dy) {
			angleB += dx/100.;
			angleR += dy/100.;
			
			cosB = (float)Math.cos(angleB);
			sinB = (float)Math.sin(angleB);
			cosR = (float)Math.cos(angleR); 
			sinR = (float)Math.sin(angleR);
			
			cosRsinB = cosR*sinB;  
			cosRcosB = cosR*cosB; 
			sinRsinB = sinR*sinB;
			sinRcosB = sinR*cosB;	
		}
		
		public void setMouseMovementOffset(int dx, int dy) {
			xoff += dx;
			yoff += dy;
			
			xs_xoff = xs + xoff;
			ys_yoff = ys + yoff;
		}
		
		public void setImageRegion(ImageRegion imageRegion) {
			this.imageRegion = imageRegion;
		}
		
		int getWidth(){
			return width;
		}
		int getHeight(){
			return height;
		}
		Image getImage(){
			return image;
		}
		int[] getPixels(){
			return pixels;
		}
	
		
		
		/*		//Transformation
		 private void xyzPos() {
		 float r_ =  cosB*R - sinB*G;
		 float g_ =  sinB*R + cosB*G;
		 float b_ =                 B;
		 
		 float r__ = r_;
		 float g__ =      cosR*g_ - sinR*b_;
		 float b__ =      sinR*g_ + cosR*b_;
		 
		 float r___ = r__* d   /(b__ + d);
		 float g___ = g__* d   /(b__ + d);
		 
		 X = (int)(r___ + xs);
		 Y = (int)(g___ + ys);
		 Z = (int)b__;		
		 }
		 */		
		
		private final void xyzPosC() {
			//float Y1 =  sinB*R  + cosB*G;
			
			//float Y2 =  cosR*Y1 - sinR*B; // cosRsinB*R  + cosRcosB*G - sinR*B 
			//float z =   sinR*Y1 + cosR*B; // sinRsinB*R  + sinRcosB*G + cosR*B
			
			float Y2 =  cosRsinB*R  + cosRcosB*G - sinR*B;
			float z =   sinRsinB*R  + sinRcosB*G + cosR*B;
			Z = (int) z; 
			
			/*
			float sz = d /(z + d);
			
			X = (int)( (cosB*R - sinB*G)* sz + xs_xoff);
			Y = (int)( Y2* sz + ys_yoff);
			*/
			z += d;
			z = d / z;
			
			X = (int)( (cosB*R - sinB*G)* z + xs_xoff);
			Y = (int)( Y2* z + ys_yoff);
			
		}
		
		private final void xyzPos() {
			float Y1 =  sinB*R  + cosB*G;
			float Y2 =  cosR*Y1 - sinR*B;
			float z =   sinR*Y1 + cosR*B;
			
			float sz = (scale * d /(z + d));
			
			Z = (int) z; 
			X = (int)( (cosB*R - sinB*G)* sz + xs  + scale * xoff);
			Y = (int)( Y2* sz + ys + scale * yoff);
		}
		
		private final void xyzPos(int r, int g, int b) {
			R = r;
			G = g;
			B = b;
			xyzPos();
		}
		
		private final void xyzPos(int[] rgb) {
			R = rgb[0];
			G = rgb[1];
			B = rgb[2];
			xyzPos();
		}
		
		private void rgb2ycbcr(int r, int g, int b, int[] yuv) {
			int rgb [] = {r, g, b};
			rgb2ycbcr(rgb, yuv);
		}
		private void rgb2ycbcr(int[] rgb, ColHash cv) {
			rgb2ycbcr(rgb, vec);
			cv.R = vec[0];
			cv.G = vec[1];
			cv.B = vec[2];
		}
		private void rgb2ycbcr(int[] rgb, int[] yuv) {
			yuv[2] = (int)(0.299*rgb[0]  + 0.587*rgb[1] + 0.114*rgb[2]); // Y
			yuv[0] = (int)(rgb[2] - yuv[2]);  // Cb
			yuv[1] = (int)(rgb[0] - yuv[2]);  // Cr
		}
		
		private void rgb2yuv(int r, int g, int b, int[] yuv) {
			int rgb [] = {r, g, b};
			rgb2yuv(rgb, yuv);
		}
		private void rgb2yuv(int[] rgb, ColHash cv) {
			rgb2yuv(rgb, vec);
			cv.R = vec[0];
			cv.G = vec[1];
			cv.B = vec[2];
		}
		private void rgb2yuv(int[] rgb, int[] yuv) {
			yuv[2] = (int)(0.299*rgb[0]  + 0.587*rgb[1] + 0.114*rgb[2]);
			yuv[0] = (int)((rgb[2] - yuv[2])*0.492f); 
			yuv[1] = (int)((rgb[0] - yuv[2])*0.877f); 
			//yuv[0] = (int)(-0.147*rgb[0] - 0.289*rgb[1] + 0.436*rgb[2]); 
			//yuv[1] = (int)( 0.615*rgb[0] - 0.515*rgb[1] - 0.100*rgb[2]); 
		}
		
		private void rgb2hsb(int[] rgb, ColHash cv) {
			rgb2hsb(rgb[0], rgb[1], rgb[2], vec);
			cv.R = vec[0];
			cv.G = vec[1];
			cv.B = vec[2];
		}
		private void rgb2hsb(int r, int g, int b, int[] hsb) {
			float [] hsbvals = new float[3]; 
			Color.RGBtoHSB(r, g, b, hsbvals);
			
			float phi = (float)(Math.toRadians(360*hsbvals[0]));
			float s   = hsbvals[1]*128;
			float br  = hsbvals[2]*255;
			
			hsb[0] = (int)(s*Math.sin(phi));
			hsb[1] = (int)(s*Math.cos(phi));
			hsb[2] = (int)(br);
			hsbvals = null;
		}
		
		private void rgb2hsb_(int r, int g, int b, int[] hsb) {
			float [] hsbvals = new float[3]; 
			Color.RGBtoHSB(r, g, b, hsbvals);
			
			hsb[0] = (int)(360*hsbvals[0] + 0.5);
			hsb[1] = (int)(hsbvals[1]*100 + 0.5);
			hsb[2] = (int)(hsbvals[2]*100 + 0.5);
			
			hsbvals = null;
		}
		
		private void rgb2hmmd(int[] rgb, ColHash cv) {
			rgb2hmmd(rgb[0], rgb[1], rgb[2], vec);
			cv.R = vec[0];
			cv.G = vec[1];
			cv.B = vec[2];
		}

		private void rgb2hmmd(int r, int g, int b, int[] hmmd) {
			
			float max = (int)Math.max(Math.max(r,g), Math.max(g,b));
			float min = (int)Math.min(Math.min(r,g), Math.min(g,b));
			float diff = (max - min);
			float sum = (float) ((max + min)/2.);
			
			float hue = 0;
			if (diff == 0)
				hue = 0;
			else if (r == max && (g - b) > 0)
				hue = 60*(g-b)/(max-min);
			else if (r == max && (g - b) <= 0)
				hue = 60*(g-b)/(max-min) + 360;
			else if (g == max)
				hue = (float) (60*(2.+(b-r)/(max-min)));
			else if (b == max)
				hue = (float) (60*(4.+(r-g)/(max-min)));
			
			diff /= 2;
			float phi = (float)(Math.toRadians(hue));
			hmmd[0] = (int)(diff*Math.sin(phi));
			hmmd[1] = (int)(diff*Math.cos(phi));
			hmmd[2] = (int)(sum);
		}
		
		private void rgb2hmmd_(int r, int g, int b, int[] hmmd) {
			
			float max = (int)Math.max(Math.max(r,g), Math.max(g,b));
			float min = (int)Math.min(Math.min(r,g), Math.min(g,b));
			float diff = (max - min);
			float sum = (float) ((max + min)/2.);
			
			float hue = 0;
			if (diff == 0)
				hue = 0;
			else if (r == max && (g - b) > 0)
				hue = 60*(g-b)/(max-min);
			else if (r == max && (g - b) <= 0)
				hue = 60*(g-b)/(max-min) + 360;
			else if (g == max)
				hue = (float) (60*(2.+(b-r)/(max-min)));
			else if (b == max)
				hue = (float) (60*(4.+(r-g)/(max-min)));
			
			diff /= 2;
			hmmd[0] = (int)(hue);
			hmmd[1] = (int)(max);
			hmmd[2] = (int)(min);
			hmmd[3] = (int)(diff);
		}
		
		private void rgb2xyy(int[] rgb, ColHash cv) {
			rgb2xyy(rgb[0], rgb[1], rgb[2], vec);
			cv.R = vec[0];
			cv.G = vec[1];
			cv.B = vec[2];
		}
		public void rgb2xyy(int R, int G, int B, int []xyy) {
			//http://www.brucelindbloom.com
			
			float rf, gf, bf;
			float r, g, b, X, Y, Z;
				
			// RGB to XYZ
			r = R/255.f; //R 0..1
			g = G/255.f; //G 0..1
			b = B/255.f; //B 0..1
			
			if (r <= 0.04045)
				r = r/12;
			else
				r = (float) Math.pow((r+0.055)/1.055,2.4);
				
			if (g <= 0.04045)
				g = g/12;
			else
				g = (float) Math.pow((g+0.055)/1.055,2.4);
			
			if (b <= 0.04045)
				b = b/12;
			else
				b = (float) Math.pow((b+0.055)/1.055,2.4);
					
			X =  0.436052025f*r	+ 0.385081593f*g + 0.143087414f *b;
			Y =  0.222491598f*r	+ 0.71688606f *g + 0.060621486f *b;
			Z =  0.013929122f*r	+ 0.097097002f*g + 0.71418547f  *b;
			
			//System.out.println("X: " + X + " Y: " + Y +" Z: " + Z);
			
			float x;
			float y;
			
			float sum = X + Y + Z;
			if (sum != 0) {
				x = X / sum;
				y = Y / sum;
			}
			else {
				float Xr = 0.964221f;  // reference white
				float Yr = 1.0f;
				float Zr = 0.825211f;
				
				x = Xr / (Xr + Yr + Zr);
				y = Yr / (Xr + Yr + Zr);
			}
			
			xyy[2] = (int) (255*Y + .5);
			xyy[0] = (int) (255*x + .5); 
			xyy[1] = (int) (255*y + .5);	
		} 
		
		private void rgb2xyz(int[] rgb, ColHash cv) {
			rgb2xyz(rgb[0], rgb[1], rgb[2], vec);
			cv.R = vec[0];
			cv.G = vec[1];
			cv.B = vec[2];
		}
		public void rgb2xyz(int R, int G, int B, int []xyz) {
			float rf, gf, bf;
			float r, g, b, X, Y, Z;
				
			r = R/255.f; //R 0..1
			g = G/255.f; //G 0..1
			b = B/255.f; //B 0..1
			
			if (r <= 0.04045)
				r = r/12;
			else
				r = (float) Math.pow((r+0.055)/1.055,2.4);
				
			if (g <= 0.04045)
				g = g/12;
			else
				g = (float) Math.pow((g+0.055)/1.055,2.4);
			
			if (b <= 0.04045)
				b = b/12;
			else
				b = (float) Math.pow((b+0.055)/1.055,2.4);
					
			X =  0.436052025f*r	+ 0.385081593f*g + 0.143087414f *b;
			Y =  0.222491598f*r	+ 0.71688606f *g + 0.060621486f *b;
			Z =  0.013929122f*r	+ 0.097097002f*g + 0.71418547f  *b;
			
			xyz[2] = (int) (255*Y + .5);
			xyz[0] = (int) (255*X + .5); 
			xyz[1] = (int) (255*Z + .5);	
		} 
		
		private void rgb2lab(int[] rgb, ColHash cv) {
			rgb2lab(rgb[0], rgb[1], rgb[2], vec);
			cv.R = vec[0];
			cv.G = vec[1];
			cv.B = vec[2];
		}
		public void rgb2lab(int R, int G, int B, int []lab) {
			//http://www.brucelindbloom.com
			
			//System.out.println("R: " + R + " G: " + G +" B: " + B);
			
			float r, g, b, X, Y, Z, fx, fy, fz, xr, yr, zr;
			float Ls, as, bs;
			float eps = 216.f/24389.f;
			float k = 24389.f/27.f;
			
			float Xr = 0.964221f;  // reference white D50
			float Yr = 1.0f;
			float Zr = 0.825211f;
				
			// RGB to XYZ
			r = R/255.f; //R 0..1
			g = G/255.f; //G 0..1
			b = B/255.f; //B 0..1
			
			// assuming sRGB (D65)
			if (r <= 0.04045)
				r = r/12;
			else
				r = (float) Math.pow((r+0.055)/1.055,2.4);
				
			if (g <= 0.04045)
				g = g/12;
			else
				g = (float) Math.pow((g+0.055)/1.055,2.4);
			
			if (b <= 0.04045)
				b = b/12;
			else
				b = (float) Math.pow((b+0.055)/1.055,2.4);
			
			/*
			X_ =  0.412424f * r + 0.357579f * g + 0.180464f  * b;
			Y_ =  0.212656f * r + 0.715158f * g + 0.0721856f * b;
			Z_ = 0.0193324f * r + 0.119193f * g + 0.950444f  * b;
			
			// chromatic adaptation transform from D65 to D50
			X =  1.047835f * X_ + 0.022897f * Y_ - 0.050147f * Z_;
			Y =  0.029556f * X_ + 0.990481f * Y_ - 0.017056f * Z_;
			Z = -0.009238f * X_ + 0.015050f * Y_ + 0.752034f * Z_;
			*/
			
			X =  0.436052025f*r	+ 0.385081593f*g + 0.143087414f *b;
			Y =  0.222491598f*r	+ 0.71688606f *g + 0.060621486f *b;
			Z =  0.013929122f*r	+ 0.097097002f*g + 0.71418547f  *b;

			//System.out.println("X: " + X + " Y: " + Y +" Z: " + Z);
			
			// XYZ to Lab
			xr = X/Xr;
			yr = Y/Yr;
			zr = Z/Zr;
			
			if ( xr > eps )
				fx =  (float) Math.pow(xr, 1/3.);
			else
				fx = (float) ((k * xr + 16.) / 116.);
			
			if ( yr > eps )
				fy =  (float) Math.pow(yr, 1/3.);
			else
				fy = (float) ((k * yr + 16.) / 116.);
			
			if ( zr > eps )
				fz =  (float) Math.pow(zr, 1/3.);
			else
				fz = (float) ((k * zr + 16.) / 116);
			
			
			Ls = ( 116 * fy ) - 16;
			as = 500*(fx-fy);
			bs = 200*(fy-fz);
			
			//System.out.println("L: " + Ls + " a: " + as +" b: " + bs);
			
			lab[2] = (int) (2.55*Ls + .5);
			lab[0] = (int) (as + .5); 
			lab[1] = (int) (bs + .5);	
		} 
		
		private void rgb2luv(int[] rgb, ColHash cv) {
			rgb2luv(rgb[0], rgb[1], rgb[2], vec);
			cv.R = vec[0];
			cv.G = vec[1];
			cv.B = vec[2];
		}
		public void rgb2luv(int R, int G, int B, int []luv) {
			//http://www.brucelindbloom.com
			
			//System.out.println("R: " + R + " G: " + G +" B: " + B);
			
			float rf, gf, bf;
			float r, g, b, X_, Y_, Z_, X, Y, Z, fx, fy, fz, xr, yr, zr;
			float L;
			float eps = 216.f/24389.f;
			float k = 24389.f/27.f;
			
			float Xr = 0.964221f;  // reference white D50
			float Yr = 1.0f;
			float Zr = 0.825211f;
				
			// RGB to XYZ
			
			r = R/255.f; //R 0..1
			g = G/255.f; //G 0..1
			b = B/255.f; //B 0..1
			
			// assuming sRGB (D65)
			if (r <= 0.04045)
				r = r/12;
			else
				r = (float) Math.pow((r+0.055)/1.055,2.4);
				
			if (g <= 0.04045)
				g = g/12;
			else
				g = (float) Math.pow((g+0.055)/1.055,2.4);
			
			if (b <= 0.04045)
				b = b/12;
			else
				b = (float) Math.pow((b+0.055)/1.055,2.4);
			
			/*
			X_ =  0.412424f * r + 0.357579f * g + 0.180464f  * b;
			Y_ =  0.212656f * r + 0.715158f * g + 0.0721856f * b;
			Z_ = 0.0193324f * r + 0.119193f * g + 0.950444f  * b;
			
			// chromatic adaptation transform from D65 to D50
			X =  1.047835f * X_ + 0.022897f * Y_ - 0.050147f * Z_;
			Y =  0.029556f * X_ + 0.990481f * Y_ - 0.017056f * Z_;
			Z = -0.009238f * X_ + 0.015050f * Y_ + 0.752034f * Z_;
			*/
			
			X =  0.436052025f*r	+ 0.385081593f*g + 0.143087414f *b;
			Y =  0.222491598f*r	+ 0.71688606f *g + 0.060621486f *b;
			Z =  0.013929122f*r	+ 0.097097002f*g + 0.71418547f  *b;
			
			//System.out.println("X: " + X + " Y: " + Y +" Z: " + Z);
			
			// XYZ to Luv
			
			float u, v, u_, v_, ur_, vr_;
			
			u_ = 4*X / (X + 15*Y + 3*Z);
			v_ = 9*Y / (X + 15*Y + 3*Z);
			
			ur_ = 4*Xr / (Xr + 15*Yr + 3*Zr);
			vr_ = 9*Yr / (Xr + 15*Yr + 3*Zr);
			
			yr = Y/Yr;
			
			if ( yr > eps )
				L =  (float) (116*Math.pow(yr, 1/3.) - 16);
			else
				L = k * yr;
			
			u = 13*L*(u_ -ur_);
			v = 13*L*(v_ -vr_);
			
			//System.out.println("L: " + Ls + " a: " + as +" b: " + bs);
			
			luv[2] = (int) (2.55*L + .5);
			luv[0] = (int) (u + .5); 
			luv[1] = (int) (v + .5);	
		} 
		
		
		public void copyColors() {
			int rgb[] = {0,0,0};
			ColHash cv;
			for (int i = 0; i < colHash.length; i++ ){
				cv = colHash[i];
				
				if (cv != null) {
					int c = cv.color;
					rgb[0] = ((c >> 16) & 255);
					rgb[1] = ((c >>  8) & 255);
					rgb[2] = ((c      ) & 255);
					
					switch (colorSpace) {
					case RGB:
						cv.R = rgb[0] - 128;
						cv.G = rgb[1] - 128;
						cv.B = rgb[2] - 128;
						break;
					case YUV:	 
						rgb2yuv(rgb, cv); 
						cv.B -= 128;
						break;
					case YCBCR:  
						rgb2ycbcr(rgb, cv); 
						cv.B -= 128;
						break;
					case HSB: 
						rgb2hsb(rgb, cv); 
						cv.B -= 128;
						break;
					case HMMD:
						rgb2hmmd(rgb, cv); 
						cv.B -= 128;
						break;
					case LAB:
						rgb2lab(rgb, cv);
						cv.B -= 128;
						break;
					case LUV: 
						rgb2luv(rgb, cv); 
						cv.B -= 128;
						break;
					case XYY: 
						rgb2xyy(rgb, cv); 
						cv.R -= 128;
						cv.G -= 128;
						cv.B -= 128;
						break;
					case XYZ: 
						rgb2xyz(rgb, cv); 
						cv.R -= 128;
						cv.G -= 128;
						cv.B -= 128;
						break;
					}
				}
			}
		}
		
		private static final int HASH_F = 13;  // 11
		private static final int HASH_P = 101; //101
		
		public synchronized void countColors() {
			
			int hashSize = pixels.length+1;
			if (hashSize % HASH_P == 0)
				hashSize++;
			colHash = new ColHash[hashSize];
			
			if (numberOfColorsOrig == 0) {
				
				for (int i=0; i<pixels.length; i++){
					
					int c  = pixelsOrig[i];
					
					if ( (c & OPAQUE) == OPAQUE ) { // opaque
						
						int hash = (((c & 0xFFffFF) * HASH_F) % hashSize); // hash value
						
						while (colHash[hash] != null && colHash[hash].color != c ){
							hash = (hash + HASH_P) % hashSize;	
						}
						if (colHash[hash] == null) {
							colHash[hash] = new ColHash();
							colHash[hash].color = c;
							numberOfColorsOrig++;
						}
					}
				}
			}
		}
		
		public synchronized void quantizeMedianCut(int numCols) {
			
			for (int i=0; i<pixels.length; i++)
				pixels[i] = pixelsOrig[i];
			
			Median_Cut mc = new Median_Cut(pixels, width, height);
			Image image = mc.convert(numCols);
			
			PixelGrabber pg =  new PixelGrabber(image, 0, 0, width, height, pixels, 0, width);
			
			try {
				pg.grabPixels();
			} catch (InterruptedException ex) {}
			
			int n = 0;
			float e = 0;
			for (int i=0; i<pixels.length; i++) {
				int c  = pixelsOrig[i];
				
				if ( (c & OPAQUE) != OPAQUE ) { // opaque
					pixels[i] = pixelsOrig[i];
				}
				else {
					c = pixels[i];
//					int r = ((c >> 16)& 0xff);
//					int g = ((c >> 8 )& 0xff);
//					int b = ( c       & 0xff);
//					
//					c = pixelsOrig[i];
//					int ro = ((c >> 16)& 0xff);
//					int go = ((c >> 8 )& 0xff);
//					int bo = ( c       & 0xff);
//					
//					int dr = r - ro;
//					int dg = g - go;
//					int db = b - bo;
//					
//					e += dr*dr + dg*dg + db*db;
//					n++;
				}	
			}
//			e /= n;
//			IJ.log("Median Cut MSE: " + e);
			
			image = Toolkit.getDefaultToolkit().createImage(source);
			
			imageRegion.setImage(image);
			
		}

		public synchronized void wu_quant(int numCols) {
				
				for (int i=0; i<pixels.length; i++)
					pixels[i] = pixelsOrig[i];
				
				
				WuCq wq = new WuCq (pixels, width*height, numCols);
					
				wq.main_();
				
				int n = 0;
				float e = 0;
				for (int i=0; i<pixels.length; i++) {
					int c  = pixelsOrig[i];
					
					if ( (c & OPAQUE) != OPAQUE ) { // opaque
						pixels[i] = pixelsOrig[i];
					}
					else {
						c = pixels[i];
//						int r = ((c >> 16)& 0xff);
//						int g = ((c >> 8 )& 0xff);
//						int b = ( c       & 0xff);
//						
//						c = pixelsOrig[i];
//						int ro = ((c >> 16)& 0xff);
//						int go = ((c >> 8 )& 0xff);
//						int bo = ( c       & 0xff);
//						
//						int dr = r - ro;
//						int dg = g - go;
//						int db = b - bo;
//						
//						e += dr*dr + dg*dg + db*db;
//						n++;
					}	
				}
//				e /= n;
//				IJ.log("Wu Quant   MSE: " + e);
				
				image = Toolkit.getDefaultToolkit().createImage(source);
				
				imageRegion.setImage(image);
				
			}
		
		public synchronized void copyOrigPixels() {
			for (int i=0; i<pixels.length; i++)
				pixels[i] = pixelsOrig[i];
			
			image = Toolkit.getDefaultToolkit().createImage(source);
			imageRegion.setImage(image);
		}

		public synchronized void quantize() {
			if (numberOfColorsOrig > 256 ) { // do not do any quantization for palletized images
				
				// perform quantization
				for (int i=0; i<pixels.length; i++){
					int c = pixelsOrig[i];
					
					if ( (c & OPAQUE) == OPAQUE) {
						int rq = quant( (c >> 16) & 255);
						int gq = quant( (c >>  8) & 255);
						int bq = quant( (c      ) & 255);
						
						c = OPAQUE | (rq <<16) | (gq << 8) | bq;
					}
					pixels[i] = c;
					
				}
			}
				
			image = Toolkit.getDefaultToolkit().createImage(source);
			imageRegion.setImage(image);
			
		}
		
		public synchronized void findUniqueColors() { 
			
			long ts = System.currentTimeMillis();
			
			int hashSize = (3*maskSize)+1;
			if (hashSize % HASH_P == 0)
				hashSize++;
			
			colHash = new ColHash[hashSize];

			numberOfColors = 0;
			
			// determine the number of colors 
			for (int i=0; i<pixels.length; i++){
				int c = pixels[i];
				
				if ( (c & OPAQUE) == OPAQUE ) { // opaque
					int hash = (((c & 0xFFffFF) * HASH_F) % hashSize); // hash value
					
					while (colHash[hash] != null && colHash[hash].color != c ){
						hash = (hash + HASH_P) % hashSize;	
					}
					if (colHash[hash] == null) {
						colHash[hash] = new ColHash();
						colHash[hash].color = c;
			
						numberOfColors++;
					}
				}
			}
			
			hashSize = (numberOfColors*7)/6;
			if (hashSize % HASH_P == 0)
				hashSize++;
			ColHash[] colHashtmp = new ColHash[hashSize];
			
			for (int i=0; i<pixels.length; i++){
				int c = pixels[i];
			
				if ( (c & OPAQUE) == OPAQUE ) { // opaque
					
					int hash = (((c & 0xFFffFF) * HASH_F) % hashSize); // hash value
					
					while (colHashtmp[hash] != null && colHashtmp[hash].color != c ){
						hash = (hash + HASH_P) % hashSize;	
					}
					
					if (colHashtmp[hash] == null) {
						colHashtmp[hash] = new ColHash();
						colHashtmp[hash].color = c;
						colHashtmp[hash].R = ((c >> 16) & 255) - 128;
						colHashtmp[hash].G = ((c >>  8) & 255) - 128;
						colHashtmp[hash].B = ((c      ) & 255) - 128;
					}
					colHashtmp[hash].num++;
				}
			}
			
			colHash = null;
			hashSize = numberOfColors;
			colHash = new ColHash[hashSize];
			
			int j = 0;
			for (int i=0; i<colHashtmp.length; i++){
				if (colHashtmp[i] != null) {
					colHash[j] = new ColHash();
					colHash[j].color = colHashtmp[i].color;
					colHash[j].R = colHashtmp[i].R;
					colHash[j].G = colHashtmp[i].G;
					colHash[j].B = colHashtmp[i].B;
					
					colHash[j].num = colHashtmp[i].num;
					j++;
				}
			}
			
			colHashtmp = null;
			
			long te = System.currentTimeMillis();
			
			//IJ.log ("" + (te - ts));
			
			if (numberOfColors <= 256)
				freqFactor = 100000f/maskSize; 
			else 
				freqFactor = 20000000f/maskSize; 
			
		}

		
		private int getNum(int c) {
			
			if ( ((c >> 24) & 0xff) == 255 ) { // opaque

				int hash = (((c & 0xFFffFF) * HASH_F) % colHash.length); // hash value
				
				while (colHash[hash] != null && colHash[hash].color != c ){
					hash = (hash + HASH_P) % colHash.length;	
				}
				return colHash[hash].num;
			}
			else
				return -1;
		}
		
		//------------------------------------------------------------------------------
		public int getColor(int x, int y) {
			int pos = y*width + x; 
			if (pos > 0 && pos < pixels.length)
				return pixels[y*width + x] ;
			else
				return 0;
		}
		
		public void showColorDot(int c) {
			
			int r = ((c >> 16)& 0xff);
			int g = ((c >> 8 )& 0xff);
			int b = ( c       & 0xff);
			
			String s = "RGB(" + Misc.fm(3,r) + "," + Misc.fm(3,g) + "," + Misc.fm(3,b) +")"; 
			
			int[] v = new int[4];
			switch (colorSpace) {
			case RGB:
				break;
			case YUV:	 
				rgb2yuv(r, g, b, v); 
				s += "  YUV(" + Misc.fm(3,v[2]) +"," + Misc.fm(4,v[0]) +"," + Misc.fm(4,v[1]) +")";
				r = v[0] + 128; g = v[1] + 128; b = v[2];
				break;
			case YCBCR:  
				rgb2ycbcr(r, g, b, v); 
				s += "  YCbCr(" + Misc.fm(3,v[2]) +"," + Misc.fm(4,v[0]) +"," + Misc.fm(4,v[1]) +")";
				r = v[0] + 128; g = v[1] + 128; b = v[2];
				break;
			case HSB: 
				rgb2hsb_(r, g, b, v); 
				s += "  HSB(" + Misc.fm(3,v[0]) +"°," + Misc.fm(3,v[1]) +"%," + Misc.fm(3,v[2]) +"%)";
				rgb2hsb(r, g, b, v); 
				r = v[0] + 128; g = v[1] + 128; b = v[2];
				break;
			case HMMD:
				rgb2hmmd_(r, g, b, v); 
				s += " HMMD(" + Misc.fm(3,v[0]) +"," + Misc.fm(3,v[1]) +"," + Misc.fm(3,v[2]) +"," + Misc.fm(3,v[3]) + ")";
				s += " HSD(" + Misc.fm(3,v[0]) +"," + Misc.fm(3,(int)Math.round((v[1] + v[2] + 0.5)/2)) +"," + Misc.fm(3,v[3]) + ")";
				rgb2hmmd(r, g, b, v); 
				r = v[0] + 128; g = v[1] + 128; b = v[2];
				break;
			case LAB: 
				rgb2lab(r, g, b, v); 
				s += "  Lab(" + Misc.fm(3,(int)(v[2]/2.55 + 0.5)) +"," + Misc.fm(4,v[0]) +"," + Misc.fm(4,v[1]) +")";
				r = v[0] + 128; g = v[1] + 128; b = v[2];
				break;
			case LUV: 
				rgb2luv(r, g, b, v); 
				s += "  Luv(" + Misc.fm(3,(int)(v[2]/2.55 + 0.5)) +"," + Misc.fm(4,v[0]) +"," + Misc.fm(4,v[1]) +")";
				r = v[0] + 128; g = v[1] + 128; b = v[2];
				break;			
			case XYY: 
				rgb2xyy(r, g, b, v); 
				s += "  xyY(" + Misc.fm(5,(int)(v[0]/0.255)/1000.) +"," + Misc.fm(5,(int)(v[1]/0.255)/1000.) +"," +Misc.fm(5,( (int)((v[2]+ 0.5)/0.255 ))/1000.) +")"  ;
				r = v[0]; g = v[1]; b = v[2];
				break;
			case XYZ: 
				rgb2xyz(r, g, b, v); 
				s += "  XYZ(" + Misc.fm(5,(int)(v[0]/0.255)/1000.) +"," + Misc.fm(5,(int)(v[1]/0.255)/1000.) +"," +Misc.fm(5,( (int)((v[2]+ 0.5)/0.255 ))/1000.) +")"  ;
				r = v[0]; g = v[1]; b = v[2];
				break;	
			}
			v = null;		
			
			R = dotR = r - 128;
			G = dotG = g - 128;
			B = dotB = b - 128;

			xyzPos();
			imageRegion.setDot(X, Y);
			
			dot = true;
			int num = getNum(c);
			if (num >= 0) {
				int percent = 1000* num / maskSize;
				s += " " + Misc.fm(3,percent/10.) + "% (" + num + "x)" ;
			}
			else
				s += "  ---";
			
			imageRegion.setText(s, 6); 
			imageRegion.repaint();
		}
		
		
		public void printColors() {
			if (hist == true) {
				ResultsTable rt = new ResultsTable();
				
				for (int i = 0; i < colHash.length; i++) {
					int c = colHash[i].color;
					int num = getNum(c);
					
					double percent; 
					if (num >= 0) 
						percent = 100.* num / maskSize;
					else {
						percent = 0;
						num = 0;
					}
					int r = ((c >> 16)& 0xff);
					int g = ((c >> 8 )& 0xff);
					int b = ( c       & 0xff);
					
					rt.incrementCounter();
					rt.addValue("Red   ", r);
					rt.addValue("Green ", g);
					rt.addValue("Blue  ", b);
					rt.addValue("Number", num);
					rt.addValue("%", percent);	
				}
				rt.show("Results"); 
			}	
		}
			
		
		public void newDisplayMode(){
			if (hist == true)
				showColorsHist();
			else {
				if (mode == 2) 
					showColorsAlpha();
				else if (mode == 0)   
					showColorsNoAlpha();
				else if (mode == 3)  
					showNoColorsAlpha();
				else if (mode == 1)  
					showNoColorsNoAlpha();
			}
		}
		
		boolean inside(int[] p, int[] p1, int[] p2, int[] p3) {
			int x  = p[0];
			int y  = p[1];
			int x1 = p1[0];
			int y1 = p1[1];
			int x2 = p2[0];
			int y2 = p2[1];
			int x3 = p3[0];
			int y3 = p3[1];
			
			int a = (x2 - x1) * (y - y1) - (y2 - y1) * (x - x1);
			int b = (x3 - x2) * (y - y2) - (y3 - y2) * (x - x2);
			int c = (x1 - x3) * (y - y3) - (y1 - y3) * (x - x3);
			
			if ((a >= 0 && b >= 0 && c >= 0) || (a <= 0 && b <= 0 && c <= 0))
				return true;
			else
				return false;
		}
		
		private void setTextAndCube() {
			
			// Textpositionen setzen
			for (int i=0; i<textPositions[0].length; i++) {
				xyzPos(textPositions[colorSpace][i]);
				imageRegion.setTextPos(i, X, Y, Z);
			}
			
			int line= 0;
			
			// L and a, b lines 
			if (colorSpace == LAB || colorSpace == LUV) {
				for (int i = 0; i < 3; i++) {
					Color color = (i == 0) ? cubeFrontColor : (((mode & 1) == 0) ? Color.orange : Color.red);
					xyzPos(lineEndsLAB[i]);
					int x1 = X;
					int y1 = Y;
					int z  = (i==0) ? 1: Z;
					xyzPos(lineEndsLAB[i+3]);
					int x2 = X;
					int y2 = Y;
					imageRegion.setLine(line++, x1, y1, x2, y2, z, color );	
				}
			}
			
			if (colorSpace == RGB || colorSpace == LAB || colorSpace == XYY || colorSpace == LUV || colorSpace == XYZ) {
				// determine color of lines
				int corner[][] = new int[8][4]; // 8 x X, Y, Z, order
				
				for (int i=0; i<8; i++) {
					xyzPos(cubeCornersRGB[i]);
					corner[i][0] = X;
					corner[i][1] = Y;
					corner[i][2] = Z;
				}
				
				int[][] cor = new int[3][];
				
				for (int i = 0; i < 4; i++) {
					int k = 0;
					for (int j = 4; j < 8; j++) {
						if (i+j != 7) 
							cor[k++] = corner[j];
					}
					if (corner[i][2]  >= corner[7-i][2] &&
							inside(corner[i], cor[0], cor[1], cor[2]))
						corner[i][3] = 1;  // hidden
				}
				for (int j = 4; j < 8; j++) {
					int k = 0;
					for (int i = 0; i < 4; i++) {
						if (i+j != 7) 
							cor[k++] = corner[i];
					}
					if (corner[j][2]  >= corner[7-j][2]  &&  
							inside(corner[j], cor[0], cor[1], cor[2]))
						corner[j][3] = 1; // hidden
				}
				
				for (int i = 0; i < 4; i++)
					for (int j = 4; j < 8; j++) {
						if (i+j != 7) {
							Color color;
							int z = 1;
							if (corner[i][3] == 1 || corner[j][3] == 1) { // hidden
								color = cubeBackColor;
							}
							else {
								color = cubeFrontColor;
								z = -1;
							}
							imageRegion.setLine(line++, 
									corner[i][0], corner[i][1], corner[j][0], corner[j][1], z, color);
						}
					}
				
				cor = null;
				corner = null;
			}
			//-----------------------------------------------------------------	
			else if ((colorSpace == YCBCR) || (colorSpace == YUV)){
				// Würfellinien eintragen
				int[] yuv_i = new int[3];
				int[] yuv_j = new int[3];
				for (int i = 0; i < 4; i++)
					for (int j = 4; j < 8; j++) {
						if (i+j != 7) {
							Color color = Color.lightGray;  
							if (colorSpace == YCBCR) {
								rgb2ycbcr(cubeCornersRGB[i], yuv_i);
								rgb2ycbcr(cubeCornersRGB[j], yuv_j);
							}
							else {
								rgb2yuv(cubeCornersRGB[i], yuv_i);
								rgb2yuv(cubeCornersRGB[j], yuv_j);
							}
							
							xyzPos(yuv_i);
							int x1 = X;
							int y1 = Y;
							xyzPos(yuv_j);
							int x2 = X;
							int y2 = Y;
							
							imageRegion.setLine(line++, x1, y1, x2, y2, 1, color );
						}
					}
				for (int i = 0; i < 3; i++) {
					Color color = (i == 0) ? cubeFrontColor : (((mode & 1) == 0) ? Color.orange : Color.red);
					xyzPos(lineEndsYUV[i]);
					int x1 = X;
					int y1 = Y;
					int z  = (i==0) ? 1: Z;
					xyzPos(lineEndsYUV[i+3]);
					int x2 = X;
					int y2 = Y;
					imageRegion.setLine(line++, x1, y1, x2, y2, z, color );	
				}
				yuv_i = null;
				yuv_j = null;
			}
			//-----------------------------------------------------------------
			else if (colorSpace == HSB) {
				//B and S lines 
				for (int i = 0; i < 2; i++) {
					Color color = (i == 0) ? cubeFrontColor : (((mode & 1) == 0) ? Color.orange : Color.red);
					xyzPos(lineEndsHSB[i]);
					int x1 = X;
					int y1 = Y;
					int z  = (i==0) ? 1: Z;
					xyzPos(lineEndsHSB[i+2]);
					int x2 = X;
					int y2 = Y;
					
					imageRegion.setLine(line++, x1, y1, x2, y2, z, color );
				}
				int step = 15;
				for (int i=0; i < 360; i+= step) {
					float phi = (float)Math.toRadians(i); 
					
					xyzPos((int)(128*Math.sin(phi)), (int)(128*Math.cos(phi)), 127);
					int x1u = X; 
					int y1u = Y;
					B = -128;
					xyzPos();
					int x1d = X; 
					int y1d = Y;
					
					phi = (float)Math.toRadians(i+step); 
					
					xyzPos((int)(128*Math.sin(phi)), (int)(128*Math.cos(phi)), 127);
					int x2u = X; 
					int y2u = Y;
					int Zu = Z;
					B = -128;
					xyzPos();
					int x2d = X; 
					int y2d = Y;
					
					imageRegion.setLine(line++, x1u, y1u, x2u, y2u, Zu, cubeFrontColor);
					imageRegion.setLine(line++, x1d, y1d, x2d, y2d, Z, cubeFrontColor);
					
					if (i % (2*step) == step) {
						B = 0;
						xyzPos();
						Color color = (Z < 0) ? new Color (0xFFBBBBBB) : new Color (0xFF666666);
						imageRegion.setLine(line++, x2u, y2u, x2d, y2d, Z, color);
					}
				}
			}
			else if (colorSpace == HMMD) {
				//B and S lines 
				for (int i = 0; i < 2; i++) {
					Color color = (i == 0) ? cubeFrontColor : (((mode & 1) == 0) ? Color.orange : Color.red);
					xyzPos(lineEndsHMMD[i]);
					int x1 = X;
					int y1 = Y;
					int z  = 1;
					xyzPos(lineEndsHMMD[i+2]);
					int x2 = X;
					int y2 = Y;
					
					imageRegion.setLine(line++, x1, y1, x2, y2, z, color );
				}
				
				xyzPos(0, 0, 127);
				int x1u = X; 
				int y1u = Y;
				xyzPos(0, 0, -128);
				int x1d = X; 
				int y1d = Y;
				
				int step = 15;
				for (int i=0; i < 360; i+= step) {
					float phi = (float)Math.toRadians(i); 
					
					xyzPos((int)(128*Math.sin(phi)), (int)(128*Math.cos(phi)), 0);
					int x1 = X; 
					int y1 = Y;
					phi = (float)Math.toRadians(i+step); 
					
					xyzPos((int)(128*Math.sin(phi)), (int)(128*Math.cos(phi)), 0);
					int x2 = X; 
					int y2 = Y;
					
					imageRegion.setLine(line++, x1, y1, x2, y2, Z, cubeFrontColor);
					
					if (i % (2*step) == step) {
						Color color;
						B = 0;
						xyzPos();
						if (Z <= 0) 
							color = new Color (0xFFBBBBBB);
						else 
							color = new Color (0xFF666666);
						
						imageRegion.setLine(line++, x1u, y1u, x1, y1, 1, color);
						imageRegion.setLine(line++, x1d, y1d, x1, y1, 1, color);
					}
				}	
			}
		}
		
		/************************************************************/		
		// display routines 
		
		
		public synchronized void showColorsNoAlpha(){
			
			setTextAndCube();
			
			// clear image and z-buffer
			for (int i = pixels.length-1; i >= 0; i--)  {
				pixels[i] = 0; 
				pixelsZ[i] = 1000;
			}
			
			// set colors
			if (numberOfColors > 256) 
			{
				ColHash ch;
				for (int i=colHash.length-1; i>=0; i--){
					ch = colHash[i];
					R = ch.R;
					G = ch.G;
					B = ch.B;
					xyzPosC();
					
					//if (X >= 0 && X < width && Y >= 0 && Y < height) 
					if (((X & ~511) | (Y & ~511)) == 0) // only for w = h = 512 !!!
					{
						//int pos = Y*width + X;
						int pos = (Y<<9) | X;  // only for w = 512 !!!
						
						if (Z < pixelsZ[pos]) {
							pixelsZ[pos] = Z;
							pixels[pos] = ch.color; 
						}
					}
				}
			}
			
			else {
				for (int i=0; i< colHash.length; i++){
					R = colHash[i].R;
					G = colHash[i].G;
					B = colHash[i].B;
					xyzPosC();
					
					for (int y = -4; y <= 4; y++) {
						int Yy = Y+y;
						for (int x = -4; x <= 4; x++) {
							
							if (x*x+y*y <= 16) {
								
								int Xx = X+x;
								
								// if ( X >= 0 && X < width && Y >= 0 && Y < height) {
								if ((Xx & ~511) == 0 && (Yy & ~511) == 0) { // only for w = h = 512
									int pos = Yy*width + Xx;
									if (Z < pixelsZ[pos]) {
										pixelsZ[pos] = Z;
										pixels[pos] = colHash[i].color;
									}
								}
							}
						}
					}
				}
			}
			
			image = Toolkit.getDefaultToolkit().createImage(source);
			
			imageRegion.setImage(image);
			//notifyAll();
		}
		
		
		public synchronized void showColorsHist(){
			
			setTextAndCube();
			
			// clear image and z-buffer
			for (int i = pixels.length-1; i >= 0; i--)  {
				pixels[i] = 0; 
				pixelsZ[i] = 1000;
			}
			
			float rFactor = (float) (40f/Math.pow(maskSize, 1/3.));
			
			
			for (int i=0; i< colHash.length; i++){
				R = colHash[i].R;
				G = colHash[i].G;
				B = colHash[i].B;
				xyzPosC();
				
				int rad = (int)(1.1f*Math.pow(colHash[i].num, 1/3.)*rFactor); 
				
				float sz = d /(Z + d);
				rad = (int) Math.max(Math.round(rad * sz),1);
				
				long r, g, b;
				
				if (colorMode == true) {
					int c = colHash[i].color;
					r = ((c >> 16)& 0xff);
					g = ((c >> 8 )& 0xff);
					b = ( c       & 0xff);
				}
				else 
					r = g = b = 200;
				
				long rr = rad*rad + 1;
				
				for (int y = -rad; y <=  rad; y++) {
					int Yy = Y+y;
					
					for (int x = -rad; x <= rad; x++) {
						long rxy = x*x + y*y;
						
						if (rxy < rr) {
							int Xx = X+x;
							if (((Xx & ~511) | (Yy & ~511)) == 0) {// only for w = h = 512 !!!
								
								int pos = Yy*width + Xx;
								if (Z < pixelsZ[pos]) {
									
									pixelsZ[pos] = Z;
									
									int r_ = (int) (((rr*rr-rxy*rxy)*r) / (rr*rr));
									int g_ = (int) (((rr*rr-rxy*rxy)*g) / (rr*rr));
									int b_ = (int) (((rr*rr-rxy*rxy)*b) / (rr*rr));
									
									pixels[pos] = OPAQUE | (r_ <<16) | (g_ << 8) | b_;
								}
							}
						}
					}
				}
			}
			
			
			image = Toolkit.getDefaultToolkit().createImage(source);
			imageRegion.setImage(image);
		}	
		
		
		public synchronized void showColorsAlpha(){
			
			setTextAndCube();
			
			
			for (int i = pixels.length-1; i >= 0 ; i--)  {
				pixels[i] = 0;
				pixelsAlpha[i] = 0;
			}
			
			if (numberOfColors > 256){
				ColHash ch;
				for (int i=colHash.length-1; i >= 0; i--){
					ch = colHash[i];
					R = ch.R;
					G = ch.G;
					B = ch.B;
					xyzPosC();
					
					
					
					// if ( X >= 0 && X < width && Y >= 0 && Y < height) {
					if ((X & ~511) == 0 && (Y & ~511) == 0) { // only for w = h = 512
						//							int pos = Y*width + X;
						int pos = (Y<<9) | X;  // only for w = 512 !!!
						
						int c_ = pixels[pos];
						int alpha = pixelsAlpha[pos];
						
						int R_ = ((c_ >> 16)& 0xff);
						int G_ = ((c_ >> 8 )& 0xff);
						int B_ = ( c_       & 0xff);
						
						int f = (int) (freqFactor*colHash[i].num);
						f = (f <= 255) ? f : 255;
						
						float zs = (float) (Math.min(1,Math.max(0,(-Z + 128.)/256.))*0.9 + 0.1);
						f = (int) (f*zs) ; 
						
						c_ = colHash[i].color;
						R = ((c_ >> 16)& 0xff);
						G = ((c_ >> 8 )& 0xff);
						B = ( c_       & 0xff);
						
						int ag = alpha+f;
						
						ag = (ag == 0) ? 1 : ag;
						pixelsAlpha[pos] = ag;
						
						R = ((R*f + alpha*R_)/ag); 
						G = ((G*f + alpha*G_)/ag); 
						B = ((B*f + alpha*B_)/ag); 
						
						pixels[pos] = (Math.min( ag, 255)<<24) | (R  << 16) | (G << 8) | B;			
						
					}	
					
				}
			}
					
			
			else {
				for (int i = 0; i < pixels.length; i++) {
					pixelsZ[i] = 1000;
				}
				
				for (int i=0; i< colHash.length; i++){
					R = colHash[i].R;
					G = colHash[i].G;
					B = colHash[i].B;
					xyzPosC();
					
					//int f = colHash[i].freq;  
					int f = (int) (freqFactor*colHash[i].num);
					f = (f <= 255) ? f : 255;
					
					
					for (int y = -4; y <=  4; y++)
						for (int x = -4; x <= 4; x++) {
							
							if (x*x+y*y <= 16) {
								int Yy = Y+y;
								int Xx = X+x;
								
								// if ( X >= 0 && X < width && Y >= 0 && Y < height) {
								if ((Xx & ~511) == 0 && (Yy & ~511) == 0) { // only for w = h = 512
									
									int pos = (Yy)*width + (Xx);
									if (Z < pixelsZ[pos]) {
										pixelsZ[pos] = Z;
										pixels[pos] = (f <<24) | (colHash[i].color & 0xFFffFF);
									}
								}
							}
						}
				}
			}
			
			image = Toolkit.getDefaultToolkit().createImage(source);
			
			imageRegion.setImage(image);
		}
		
		public synchronized void showNoColorsAlpha(){
			
			setTextAndCube();
			
			for (int i = 0; i < pixels.length; i++) {
				pixels[i] = 0;
				pixelsAlpha[i] = 0;
			}
			
			ColHash cv;
			if (numberOfColors > 256){
				for (int i=0; i< colHash.length; i++){
					R = colHash[i].R;
					G = colHash[i].G;
					B = colHash[i].B;
					xyzPosC();
					int pos = X + Y*width;
					
					//	if ( X >= 0 && X < width && Y >= 0 && Y < height) {
					if ((X & ~511) == 0 && (Y & ~511) == 0) { // only for w = h = 512
						int alpha = pixelsAlpha[pos];
						
						int f = (int) (freqFactor*colHash[i].num);
						f = (f <= 255) ? f : 255;
						
						f = (int) (f*(Math.min(1,Math.max(0,(-Z + 128.)/256.))*0.9+ 0.1)) ; 
						
						int ag = alpha+f;
						if (ag == 0)
							ag = (alpha == 0) ? 1 : alpha;
						pixelsAlpha[pos] = ag;
						
						pixels[pos] = (Math.min( ag, 255)<<24) ;
					}
				}	
				
			}
			else {
				for (int i = 0; i < pixels.length; i++) {
					pixelsZ[i] = 1000;
				}
				
				for (int i=0; i< colHash.length; i++){
					R = colHash[i].R;
					G = colHash[i].G;
					B = colHash[i].B;
					xyzPosC();
					
					int f = (int) (freqFactor*colHash[i].num);
					f = (f <= 255) ? f : 255;
					
					for (int y = -4; y <=  4; y++)
						for (int x = -4; x <= 4; x++) {
							
							if (x*x+y*y <= 16) {
								int Yy = Y+y;
								int Xx = X+x;
								
								// if ( X >= 0 && X < width && Y >= 0 && Y < height) {
								if ((Xx & ~511) == 0 && (Yy & ~511) == 0) { // only for w = h = 512
									int pos = (Yy)*width + (Xx);
									if (Z < pixelsZ[pos]) {
										pixelsZ[pos] = Z;
										pixels[pos] = f <<24;
									}
								}
							}
						}
				}
			}
			
			image = Toolkit.getDefaultToolkit().createImage(source);
			
			imageRegion.setImage(image);
		}
		
		public synchronized void showNoColorsNoAlpha(){
			
			setTextAndCube();
			
			for (int i = 0; i < pixels.length; i++) {
				pixels[i] = 0; 
			}
			
			ColHash cv;
			if (numberOfColors > 256){
				for (int i=colHash.length-1; i>=0; i--){
					
						R = colHash[i].R;
						G = colHash[i].G;
						B = colHash[i].B;
						xyzPosC();
						
						// if ( X >= 0 && X < width && Y >= 0 && Y < height) {
						if ((X & ~511) == 0 && (Y & ~511) == 0) { // only for w = h = 512	
							pixels[X + Y*width] = OPAQUE;
						}
					}
				
			}
			else {
				for (int i=0; i< colHash.length; i++){
					
						R = colHash[i].R;
						G = colHash[i].G;
						B = colHash[i].B;
						xyzPosC();
						for (int y = -4; y <=  4; y++)
							for (int x = -4; x <= 4; x++) {
								if (x*x+y*y <= 16) {
									int Yy = Y+y;
									int Xx = X+x;
									
									// if ( X >= 0 && X < width && Y >= 0 && Y < height) {
									if ((Xx & ~511) == 0 && (Yy & ~511) == 0) { // only for w = h = 512
										pixels[(Yy)*width + Xx] = (255 << 24);		
									}
								}
							}
					}
				
			}
			
			image = Toolkit.getDefaultToolkit().createImage(source);
			
			imageRegion.setImage(image);
		}
		
		public void setD(float d) {
			this.d = d;
		}
		
		public void setColorSpace(int colorSpace) {
			this.colorSpace = colorSpace;
		}
		
		public int getX() {
			return X;
		}
		public int getY() {
			return Y;
		}		
	}
	
	private final class ColHash {
		
		int R;
		int G;
		int B;
		
		int color;
		int num;
	}
	///////////////////////////////////////////////////////////////////////////
	
	class Lines {
		int x1;
		int y1;
		int x2;
		int y2;
		int z;
		Color color;
		
		public void setPos(int x1, int y1, int x2, int y2, int z, Color color) {
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
			this.z = z;
			this.color = color;	
		}
		
		public void setColor(Color color) {
			this.color = color;
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	class TextField {
		private String text = "";
		private Color color;
		
		private int xpos;
		private int ypos;
		private int z;
		
		public Color getColor() {
			return color;
		}
		public void setColor(Color color) {
			this.color = color;
		}
		
		public void setText(String text) {
			this.text = text;
		}
		
		public void setXpos(int xpos) {
			this.xpos = xpos;
		}
		
		public void setYpos(int ypos) {
			this.ypos = ypos;
		}
		
		public String getText() {
			return text;
		}
		public int getXpos() {
			return xpos;
		}
		public int getYpos() {
			return ypos;
		}
		public void setZ(int z) {
			this.z = z;
		}
		public int getZ() {
			return z;
		}
	}

	static class Misc {
		
		static String fm(int len, int val) {
			String s = "" + val;
			
			while (s.length() < len) {
				s = " " + s;
			}
			return s;
		}
		static String fm(int len, double val) {
			String s = "" + val;
			
			while (s.length() < len) {
				s = s + " ";
			}
			return s;
		}
		

	}
	
	public class Median_Cut {
		
		static final int MAXCOLORS = 256;  // maximum # of output colors
		static final int HSIZE = 32768;    // size of image histogram
		private int[] hist;          // RGB histogram and reverse color lookup table
		private int[] histPtr;        // points to colors in "hist"
		private Cube[] list;        // list of cubes
		private int[] pixels32;
		private int width, height;
		private IndexColorModel cm;
		
		public Median_Cut(int[] pixels, int width, int height) {
			int color16;
			
			pixels32 = pixels;
			this.width = width;
			this.height = height;
			
			//build 32x32x32 RGB histogram
			hist = new int[HSIZE];
			for (int i = 0; i < width * height; i++) {
				if ((pixels32[i] & 0xFF000000) == 0xFF000000 ) {
					color16 = rgb(pixels32[i]);
					hist[color16]++;
				}
			}
		}
		
		int getColorCount() {
			int count = 0;
			for (int i = 0; i < HSIZE; i++)
				if (hist[i] > 0) count++;
			return count;
		}
		
		
		Color getModalColor() {
			int max = 0;
			int c = 0;
			for (int i = 0; i < HSIZE; i++)
				if (hist[i] > max) {
					max = hist[i];
					c = i;
				}
			return new Color(red(c), green(c), blue(c));
		}
		
		
		// Convert from 24-bit to 15-bit color
		private final int rgb(int c) {
			int r = (c & 0xf80000) >> 19;
			int g = (c & 0xf800) >> 6;
			int b = (c & 0xf8) << 7;
			return b | g | r;
		}
		
		// Get red component of a 15-bit color
		private final int red(int x) {
			return (x & 31) << 3;
		}
		
		// Get green component of a 15-bit color
		private final int green(int x) {
			return (x >> 2) & 0xf8;
		}
		
		// Get blue component of a 15-bit color
		private final int blue(int x) {
			return (x >> 7) & 0xf8;
		}
		
		
		/** Uses Heckbert's median-cut algorithm to divide the color space defined by
		 "hist" into "maxcubes" cubes. The centroids (average value) of each cube
		 are are used to create a color table. "hist" is then updated to function
		 as an inverse color map that is used to generate an 8-bit image. */
		public Image convert(int maxcubes) {
			return convertToByte(maxcubes);
		}
		
		/** This is a version of convert that returns a ByteProcessor. */
		public Image convertToByte(int maxcubes) {
			int lr, lg, lb;
			int i, median, color;
			int count;
			int k, level, ncubes, splitpos;
			int num, width;
			int longdim = 0;  //longest dimension of cube
			Cube cube, cubeA, cubeB;
			
			// Create initial cube
			list = new Cube[MAXCOLORS];
			histPtr = new int[HSIZE];
			ncubes = 0;
			cube = new Cube();
			for (i = 0, color = 0; i <= HSIZE - 1; i++) {
				if (hist[i] != 0) {
					histPtr[color++] = i;
					cube.count = cube.count + hist[i];
				}
			}
			cube.lower = 0;
			cube.upper = color - 1;
			cube.level = 0;
			Shrink(cube);
			list[ncubes++] = cube;
			
			//Main loop
			while (ncubes < maxcubes) {
				
				// Search the list of cubes for next cube to split, the lowest level cube
				level = 255;
				splitpos = -1;
				for (k = 0; k <= ncubes - 1; k++) {
					if (list[k].lower == list[k].upper)
						;  // single color; cannot be split
					else if (list[k].level < level) {
						level = list[k].level;
						splitpos = k;
					}
				}
				if (splitpos == -1)  // no more cubes to split
					break;
				
				// Find longest dimension of this cube
				cube = list[splitpos];
				lr = cube.rmax - cube.rmin;
				lg = cube.gmax - cube.gmin;
				lb = cube.bmax - cube.bmin;
				if (lr >= lg && lr >= lb) longdim = 0;
				if (lg >= lr && lg >= lb) longdim = 1;
				if (lb >= lr && lb >= lg) longdim = 2;
				
				// Sort along "longdim"
				reorderColors(histPtr, cube.lower, cube.upper, longdim);
				quickSort(histPtr, cube.lower, cube.upper);
				restoreColorOrder(histPtr, cube.lower, cube.upper, longdim);
				
				// Find median
				count = 0;
				for (i = cube.lower; i <= cube.upper - 1; i++) {
					if (count >= cube.count / 2) break;
					color = histPtr[i];
					count = count + hist[color];
				}
				median = i;
				
				// Now split "cube" at the median and add the two new
				// cubes to the list of cubes.
				cubeA = new Cube();
				cubeA.lower = cube.lower;
				cubeA.upper = median - 1;
				cubeA.count = count;
				cubeA.level = cube.level + 1;
				Shrink(cubeA);
				list[splitpos] = cubeA;        // add in old slot
				
				cubeB = new Cube();
				cubeB.lower = median;
				cubeB.upper = cube.upper;
				cubeB.count = cube.count - count;
				cubeB.level = cube.level + 1;
				Shrink(cubeB);
				list[ncubes++] = cubeB;        // add in new slot */
			}
			
			// We have enough cubes, or we have split all we can. Now
			// compute the color map, the inverse color map, and return
			// an 8-bit image.
			makeInverseMap(hist, ncubes);
			return makeImage();
		}
		
		void Shrink(Cube cube) {
			// Encloses "cube" with a tight-fitting cube by updating the
			// (rmin,gmin,bmin) and (rmax,gmax,bmax) members of "cube".
			
			int r, g, b;
			int color;
			int rmin, rmax, gmin, gmax, bmin, bmax;
			
			rmin = 255;
			rmax = 0;
			gmin = 255;
			gmax = 0;
			bmin = 255;
			bmax = 0;
			for (int i = cube.lower; i <= cube.upper; i++) {
				color = histPtr[i];
				r = red(color);
				g = green(color);
				b = blue(color);
				if (r > rmax) rmax = r;
				if (r < rmin) rmin = r;
				if (g > gmax) gmax = g;
				if (g < gmin) gmin = g;
				if (b > bmax) bmax = b;
				if (b < bmin) bmin = b;
			}
			cube.rmin = rmin;
			cube.rmax = rmax;
			cube.gmin = gmin;
			cube.gmax = gmax;
			cube.bmin = bmin;
			cube.bmax = bmax;
		}
		
		
		void makeInverseMap(int[] hist, int ncubes) {
			// For each cube in the list of cubes, computes the centroid
			// (average value) of the colors enclosed by that cube, and
			// then loads the centroids in the color map. Next loads
			// "hist" with indices into the color map
			
			int r, g, b;
			int color;
			float rsum, gsum, bsum;
			Cube cube;
			byte[] rLUT = new byte[256];
			byte[] gLUT = new byte[256];
			byte[] bLUT = new byte[256];
			
			for (int k = 0; k <= ncubes - 1; k++) {
				cube = list[k];
				rsum = gsum = bsum = (float) 0.0;
				for (int i = cube.lower; i <= cube.upper; i++) {
					color = histPtr[i];
					r = red(color);
					rsum += (float) r * (float) hist[color];
					g = green(color);
					gsum += (float) g * (float) hist[color];
					b = blue(color);
					bsum += (float) b * (float) hist[color];
				}
				
				// Update the color map
				r = (int) (rsum / (float) cube.count);
				g = (int) (gsum / (float) cube.count);
				b = (int) (bsum / (float) cube.count);
				if (r == 248 && g == 248 && b == 248)
					r = g = b = 255;  // Restore white (255,255,255)
				rLUT[k] = (byte) r;
				gLUT[k] = (byte) g;
				bLUT[k] = (byte) b;
			}
			cm = new IndexColorModel(8, ncubes, rLUT, gLUT, bLUT);
			
			// For each color in each cube, load the corre-
			// sponding slot in "hist" with the centroid of the cube.
			for (int k = 0; k <= ncubes - 1; k++) {
				cube = list[k];
				for (int i = cube.lower; i <= cube.upper; i++) {
					color = histPtr[i];
					hist[color] = k;
				}
			}
		}
		
		
		void reorderColors(int[] a, int lo, int hi, int longDim) {
			// Change the ordering of the 5-bit colors in each word of int[]
			// so we can sort on the 'longDim' color
			
			int c, r, g, b;
			switch (longDim) {
			case 0: //red
				for (int i = lo; i <= hi; i++) {
					c = a[i];
					r = c & 31;
					a[i] = (r << 10) | (c >> 5);
				}
				break;
			case 1: //green
				for (int i = lo; i <= hi; i++) {
					c = a[i];
					r = c & 31;
					g = (c >> 5) & 31;
					b = c >> 10;
					a[i] = (g << 10) | (b << 5) | r;
				}
				break;
			case 2: //blue; already in the needed order
				break;
			}
		}
		
		
		void restoreColorOrder(int[] a, int lo, int hi, int longDim) {
			// Restore the 5-bit colors to the original order
			
			int c, r, g, b;
			switch (longDim) {
			case 0: //red
				for (int i = lo; i <= hi; i++) {
					c = a[i];
					r = c >> 10;
					a[i] = ((c & 1023) << 5) | r;
				}
				break;
			case 1: //green
				for (int i = lo; i <= hi; i++) {
					c = a[i];
					r = c & 31;
					g = c >> 10;
					b = (c >> 5) & 31;
					a[i] = (b << 10) | (g << 5) | r;
				}
				break;
			case 2: //blue
				break;
			}
		}
		
		
		void quickSort(int a[], int lo0, int hi0) {
			// Based on the QuickSort method by James Gosling from Sun's SortDemo applet
			
			int lo = lo0;
			int hi = hi0;
			int mid, t;
			
			if (hi0 > lo0) {
				mid = a[(lo0 + hi0) / 2];
				while (lo <= hi) {
					while ((lo < hi0) && (a[lo] < mid))
						++lo;
					while ((hi > lo0) && (a[hi] > mid))
						--hi;
					if (lo <= hi) {
						t = a[lo];
						a[lo] = a[hi];
						a[hi] = t;
						++lo;
						--hi;
					}
				}
				if (lo0 < hi)
					quickSort(a, lo0, hi);
				if (lo < hi0)
					quickSort(a, lo, hi0);
				
			}
		}
		
		
		Image makeImage() {
			// Generate 8-bit image
			
			Image img8;
			byte[] pixels8;
			int color16;
			pixels8 = new byte[width * height];
			for (int i = 0; i < width * height; i++) {
				color16 = rgb(pixels32[i]);
				pixels8[i] = (byte) hist[color16];
			}
			img8 = Toolkit.getDefaultToolkit().createImage(
					new MemoryImageSource(width, height,
							cm, pixels8, 0, width));
			return img8;
		}
		
		
	} //class MedianCut


	class Cube {      // structure for a cube in color space
		int lower;      // one corner's index in histogram
		int upper;      // another corner's index in histogram
		int count;      // cube's histogram count
		int level;      // cube's level
		int rmin, rmax;
		int gmin, gmax;
		int bmin, bmax;
		
		Cube() {
			count = 0;
		}
		
		public String toString() {
			String s = "lower=" + lower + " upper=" + upper;
			s = s + " count=" + count + " level=" + level;
			s = s + " rmin=" + rmin + " rmax=" + rmax;
			s = s + " gmin=" + gmin + " gmax=" + gmax;
			s = s + " bmin=" + bmin + " bmax=" + bmax;
			return s;
		}
		
	}

	/**
	 * @author barthel
	 *
	 adapted from 
	 
		C Implementation of Wu's Color Quantizer (v. 2)
	(see Graphics Gems vol. II, pp. 126-133)

	Author:	Xiaolin Wu
	Dept. of Computer Science
	Univ. of Western Ontario
	London, Ontario N6A 5B7
	wu@csd.uwo.ca

	Algorithm: Greedy orthogonal bipartition of RGB space for variance
	minimization aided by inclusion-exclusion tricks.
	For speed no nearest neighbor search is done. Slightly
	better performance can be expected by more sophisticated
	but more expensive versions.

	Free to distribute, comments and suggestions are appreciated.
	***********************************************************************/
	public class WuCq {

		private final static int MAXCOLOR	= 512;
		private final static int RED =	2;
		private final static int GREEN = 1;
		private final static int BLUE	= 0;
		
		//	Histogram is in elements 1..HISTSIZE along each axis,
		//	element 0 is for base or marginal value
		//	NB: these must start out 0!

		float[][][]	m2 = new float[33][33][33];
		long [][][]	wt = new long [33][33][33];
		long [][][]	mr = new long [33][33][33];
		long [][][]	mg = new long [33][33][33];
		long [][][]	mb = new long [33][33][33];
		
		int   pixels[];
		int	  size; // image size
		int	  K;    // color look-up table size
		int   Qadd[];
		
		int[]	lut_r = new int[MAXCOLOR], lut_g = new int[MAXCOLOR], lut_b = new int[MAXCOLOR];
		int[]   lut_rgb = new int[MAXCOLOR];
		int	tag[];
		
		WuCq (int[] pixels, int size, int numCols) {
			this.pixels = pixels;
			this.size = size;
			this.K = numCols;
		}
		
		void Hist3d(long vwt[][][], long vmr[][][], long vmg[][][], long vmb[][][], float m2[][][]) 
		{
			int ind, r, g, b;
			int	inr, ing, inb;
			int [] table = new int[256];
			int i;

			for(i=0; i<256; ++i) 
				table[i]= i*i;
			
			Qadd = new int[size];
			
			for(i=0; i<size; ++i){
				int c = pixels[i];
				if ((c & 0xFF000000) == 0xFF000000 ) {
					
					r = ((c >> 16)& 0xff);
					g = ((c >> 8 )& 0xff);
					b = ( c       & 0xff);
					
					inr=(r>>3)+1; 
					ing=(g>>3)+1; 
					inb=(b>>3)+1; 
					Qadd[i]=ind=(inr<<10)+(inr<<6)+inr+(ing<<5)+ing+inb;
					
					vwt[inr][ing][inb]++;
					vmr[inr][ing][inb] += r;
					vmg[inr][ing][inb] += g;
					vmb[inr][ing][inb] += b;
					
					m2[inr][ing][inb] += (float)(table[r]+table[g]+table[b]);
				}
			}
		}
		
		//	At conclusion of the histogram step, we can interpret
		//	wt[r][g][b] = sum over voxel of P(c)
		//	mr[r][g][b] = sum over voxel of r*P(c)  ,  similarly for mg, mb
		//	m2[r][g][b] = sum over voxel of c^2*P(c)
		//	Actually each of these should be divided by 'size' to give the usual
		//	interpretation of P() as ranging from 0 to 1, but we needn't do that here.
		
		//	We now convert histogram into moments so that we can rapidly calculate
		//	the sums of the above quantities over any desired box.

		void M3d(long vwt[][][], long vmr[][][], long vmg[][][], long vmb[][][], float m2[][][]) // compute cumulative moments. 
		{
			int ind1, ind2;
			int i, r, g, b;
			long line, line_r, line_g, line_b;
			long[] area = new long[33], area_r = new long[33], area_g = new long[33], area_b = new long[33];
			float  line2; 
			float[] area2 = new float[33];

			for(r=1; r<=32; ++r){
				for(i=0; i<=32; ++i) 
					area2[i]=area[i]=area_r[i]=area_g[i]=area_b[i]=0;
				for(g=1; g<=32; ++g){
					line2 = line = line_r = line_g = line_b = 0;
					for(b=1; b<=32; ++b){
						ind1 = (r<<10) + (r<<6) + r + (g<<5) + g + b; // [r][g][b] 

						line   += vwt[r][g][b];
						line_r += vmr[r][g][b]; 
						line_g += vmg[r][g][b]; 
						line_b += vmb[r][g][b];
						line2  += m2[r][g][b];
						
						area[b] += line;
						area_r[b] += line_r;
						area_g[b] += line_g;
						area_b[b] += line_b;
						area2[b] += line2;
						
						vwt[r][g][b] = vwt[r-1][g][b] + area[b];
						vmr[r][g][b] = vmr[r-1][g][b] + area_r[b];
						vmg[r][g][b] = vmg[r-1][g][b] + area_g[b];
						vmb[r][g][b] = vmb[r-1][g][b] + area_b[b];
						m2[r][g][b]  = m2[r-1][g][b]  + area2[b];
					}
				}
			}
		}
		
		//	 Compute sum over a box of any given statistic 
		long Vol( Box cube, long mmt[][][]) 
		{
			return(  mmt[cube.r1][cube.g1][cube.b1] 
					-mmt[cube.r1][cube.g1][cube.b0]
					-mmt[cube.r1][cube.g0][cube.b1]
					+mmt[cube.r1][cube.g0][cube.b0]
					-mmt[cube.r0][cube.g1][cube.b1]
					+mmt[cube.r0][cube.g1][cube.b0]
					+mmt[cube.r0][cube.g0][cube.b1]
					-mmt[cube.r0][cube.g0][cube.b0] );
		}

		//	The next two routines allow a slightly more efficient calculation
		//	of Vol() for a proposed subbox of a given box.  The sum of Top()
		//	and Bottom() is the Vol() of a subbox split in the given direction
		//	and with the specified new upper bound.


		long Bottom(Box cube, int dir, long mmt[][][])
		//	Compute part of Vol(cube, mmt) that doesn't depend on r1, g1, or b1 
		//	(depending on dir) 
		{
			switch(dir){
			case RED:
				return( -mmt[cube.r0][cube.g1][cube.b1]
				+mmt[cube.r0][cube.g1][cube.b0]
				+mmt[cube.r0][cube.g0][cube.b1]
				-mmt[cube.r0][cube.g0][cube.b0] );
			case GREEN:
				return( -mmt[cube.r1][cube.g0][cube.b1]
				+mmt[cube.r1][cube.g0][cube.b0]
				+mmt[cube.r0][cube.g0][cube.b1]
				-mmt[cube.r0][cube.g0][cube.b0] );
			case BLUE:
				return( -mmt[cube.r1][cube.g1][cube.b0]
				+mmt[cube.r1][cube.g0][cube.b0]
				+mmt[cube.r0][cube.g1][cube.b0]
				-mmt[cube.r0][cube.g0][cube.b0] );
			default:
				return 0;
			}
		}
		
		long Top(Box cube, int dir, int pos, long mmt[][][])
		//	 Compute remainder of Vol(cube, mmt), substituting pos for 
		//	 r1, g1, or b1 (depending on dir) 
		{
			switch(dir){
			case RED:
				return( mmt[pos][cube.g1][cube.b1] 
				-mmt[pos][cube.g1][cube.b0]
				-mmt[pos][cube.g0][cube.b1]
				+mmt[pos][cube.g0][cube.b0] );
			case GREEN:
				return( mmt[cube.r1][pos][cube.b1] 
				-mmt[cube.r1][pos][cube.b0]
				-mmt[cube.r0][pos][cube.b1]
				+mmt[cube.r0][pos][cube.b0] );
			case BLUE:
				return( mmt[cube.r1][cube.g1][pos]
				-mmt[cube.r1][cube.g0][pos]
				-mmt[cube.r0][cube.g1][pos]
				+mmt[cube.r0][cube.g0][pos] );
			default:
				return 0;
			}
		}
		
		float Var(Box cube)
		//	 Compute the weighted variance of a box 
		//	 NB: as with the raw statistics, this is really the variance * size 
		{
			float dr, dg, db, xx;

			dr = Vol(cube, mr); 
			dg = Vol(cube, mg); 
			db = Vol(cube, mb);
			xx = m2[cube.r1][cube.g1][cube.b1] 
				-m2[cube.r1][cube.g1][cube.b0]
				-m2[cube.r1][cube.g0][cube.b1]
				+m2[cube.r1][cube.g0][cube.b0]
				-m2[cube.r0][cube.g1][cube.b1]
				+m2[cube.r0][cube.g1][cube.b0]
				+m2[cube.r0][cube.g0][cube.b1]
				-m2[cube.r0][cube.g0][cube.b0];

			return( xx - (dr*dr+dg*dg+db*db)/(float)Vol(cube,wt) );    
		}
		
		//	We want to minimize the sum of the variances of two subboxes.
		//	The sum(c^2) terms can be ignored since their sum over both subboxes
		//	is the same (the sum for the whole box) no matter where we split.
		//	The remaining terms have a minus sign in the variance formula,
		//	so we drop the minus sign and MAXIMIZE the sum of the two terms.


		float Maximize(Box cube, int dir, int first, int last, int cut[],
				   long whole_r, long whole_g, long whole_b, long whole_w)
		{
			long half_r, half_g, half_b, half_w;
			long base_r, base_g, base_b, base_w;
			int i;
			float temp, max;

			base_r = Bottom(cube, dir, mr);
			base_g = Bottom(cube, dir, mg);
			base_b = Bottom(cube, dir, mb);
			base_w = Bottom(cube, dir, wt);
			max = (float) 0.0;
			cut[0] = -1;
			for(i=first; i<last; ++i){
				half_r = base_r + Top(cube, dir, i, mr);
				half_g = base_g + Top(cube, dir, i, mg);
				half_b = base_b + Top(cube, dir, i, mb);
				half_w = base_w + Top(cube, dir, i, wt);
				// now half_x is sum over lower half of box, if split at i 
				if (half_w == 0) {        // subbox could be empty of pixels! 
					continue;             // never split into an empty box 
				} else
					temp = ((float)half_r*half_r + (float)half_g*half_g +
							(float)half_b*half_b)/half_w;

				half_r = whole_r - half_r;
				half_g = whole_g - half_g;
				half_b = whole_b - half_b;
				half_w = whole_w - half_w;
				if (half_w == 0) {        // subbox could be empty of pixels! 
					continue;             // never split into an empty box 
				} else
					temp += ((float)half_r*half_r + (float)half_g*half_g +
							(float)half_b*half_b)/half_w;

				if (temp > max) {
					max=temp; 
					cut[0]=i;
					}
			}
			return(max);
		}

		int Cut(Box set1, Box set2)
		{
			int dir;
			int[] cutr = {0}, cutg = {0}, cutb = {0};
			float maxr, maxg, maxb;
			long whole_r, whole_g, whole_b, whole_w;

			whole_r = Vol(set1, mr);
			whole_g = Vol(set1, mg);
			whole_b = Vol(set1, mb);
			whole_w = Vol(set1, wt);

			maxr = Maximize(set1, RED, set1.r0+1, set1.r1, cutr,
				whole_r, whole_g, whole_b, whole_w);
			maxg = Maximize(set1, GREEN, set1.g0+1, set1.g1, cutg,
				whole_r, whole_g, whole_b, whole_w);
			maxb = Maximize(set1, BLUE, set1.b0+1, set1.b1, cutb,
				whole_r, whole_g, whole_b, whole_w);

			if( (maxr>=maxg)&&(maxr>=maxb) ) {
				dir = RED;
				if (cutr[0] < 0) 
					return 0; // can't split the box 
			}
			else
				if( (maxg>=maxr)&&(maxg>=maxb) ) 
					dir = GREEN;
				else
					dir = BLUE; 

			set2.r1 = set1.r1;
			set2.g1 = set1.g1;
			set2.b1 = set1.b1;

			switch (dir){
			case RED:
				set2.r0 = set1.r1 = cutr[0];
				set2.g0 = set1.g0;
				set2.b0 = set1.b0;
				break;
			case GREEN:
				set2.g0 = set1.g1 = cutg[0];
				set2.r0 = set1.r0;
				set2.b0 = set1.b0;
				break;
			case BLUE:
				set2.b0 = set1.b1 = cutb[0];
				set2.r0 = set1.r0;
				set2.g0 = set1.g0;
				break;
			}
			set1.vol=(set1.r1-set1.r0)*(set1.g1-set1.g0)*(set1.b1-set1.b0);
			set2.vol=(set2.r1-set2.r0)*(set2.g1-set2.g0)*(set2.b1-set2.b0);
			return 1;
		}

		void Mark(Box cube, int label, int tag[]) {
			 int r, g, b;

			for(r=cube.r0+1; r<=cube.r1; ++r)
				for(g=cube.g0+1; g<=cube.g1; ++g)
					for(b=cube.b0+1; b<=cube.b1; ++b)
						tag[(r<<10) + (r<<6) + r + (g<<5) + g + b] = label;
		}
		
		void main_()
		{
			Box[]	cube = new Box[MAXCOLOR];
			for (int i = 0; i < MAXCOLOR; i++)
				cube[i] = new Box();

			int		next;
			long    weight;
			float[]	vv = new float[MAXCOLOR];
			float   temp;

			Hist3d(wt, mr, mg, mb, m2); 
			//IJ.log("Histogram done\n");
			
			M3d(wt, mr, mg, mb, m2);    
			//IJ.log("Moments done\n");

			cube[0].r0 = cube[0].g0 = cube[0].b0 = 0;
			cube[0].r1 = cube[0].g1 = cube[0].b1 = 32;
			
			next = 0;
			for(int i=1; i<K; ++i) {
				if (Cut(cube[next], cube[i]) == 1) {
					// volume test ensures we won't try to cut one-cell box 
					vv[next] = (float) ((cube[next].vol>1) ? Var(cube[next]) : 0.0);
					vv[i] = (float) ((cube[i].vol>1) ? Var(cube[i]) : 0.0);
				} 
				else {
					vv[next] = (float) 0.0;   // don't try to split this box again 
					i--;              // didn't create box i 
				}
				next = 0; temp = vv[0];
				for(int k=1; k<=i; ++k)
					if (vv[k] > temp) {
						temp = vv[k]; next = k;
					}
					if (temp <= 0.0) {
						K = i+1;
						IJ.log("Only got " + K + " boxes\n");
						break;
					}
			}
			//IJ.log("Partition done\n");

			tag = new int[33*33*33];
			
			for(int k=0; k<K; ++k){
				Mark(cube[k], k, tag);
				weight = Vol(cube[k], wt);
				if (weight > 0 ) {
					lut_r[k] = (int) (Vol(cube[k], mr) / weight);
					lut_g[k] = (int) (Vol(cube[k], mg) / weight);
					lut_b[k] = (int) (Vol(cube[k], mb) / weight);
				}
				else{
					IJ.log("bogus box " + k);
					lut_r[k] = lut_g[k] = lut_b[k] = 0;		
				}
			}

			for(int i=0; i<size; ++i) {
				Qadd[i] = tag[Qadd[i]];
			}
			
			// no search
			for (int i=0; i<size; i++) {
				int r = lut_r[Qadd[i]]; 
				int g = lut_g[Qadd[i]]; 
				int b = lut_b[Qadd[i]]; 
				
				pixels[i] = (255<<24) | (r  << 16) | (g << 8) | b;	
			}
		}
			
		
		class Box {
			int r0;			 // min value, exclusive 
			int r1;			 // max value, inclusive 
			int g0;  
			int g1;  
			int b0;  
			int b1;
			int vol;	
		}
	}	
	
}
