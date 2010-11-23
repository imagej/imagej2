package implementation.swing;

import ijx.Menus;
import ijx.Prefs;
import ijx.WindowManager;
import ijx.IJ;
import ijx.IJEventListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import ij.*;
import ijx.gui.dialog.GenericDialog;
import ijx.gui.IjxToolbar;
import ijx.roi.Roi;
import ijx.roi.TextRoi;
import ijx.plugin.frame.Recorder;
import ijx.plugin.frame.Editor;
import ijx.plugin.MacroInstaller;
import ijx.macro.Program;
import ijx.IjxImagePlus;
import ijx.gui.IjxImageCanvas;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

/** The ImageJ toolbar. */
public class ToolbarSwing extends JToolBar implements MouseListener,
        MouseMotionListener, ItemListener, ActionListener, IjxToolbar {
		
	private Dimension ps = new Dimension(SIZE*NUM_BUTTONS, SIZE);
	private boolean[] down;
	private static int current;
	private int previous;
	private int x,y;
	private int xOffset, yOffset;
	private long mouseDownTime;
	private Graphics g;
	private static ToolbarSwing instance;
	private int mpPrevious = RECTANGLE;
	private String[] names = new String[NUM_TOOLS];
	private String[] icons = new String[NUM_TOOLS];
    private JPopupMenu[] menus = new JPopupMenu[NUM_TOOLS];
	private int pc;
	private String icon;
	private MacroInstaller macroInstaller;
	private int startupTime;
	private JPopupMenu rectPopup, ovalPopup, pointPopup, linePopup, switchPopup;
	private JCheckBoxMenuItem rectItem, roundRectItem;
	private JCheckBoxMenuItem ovalItem, brushItem;
	private JCheckBoxMenuItem pointItem, multiPointItem;
	private JCheckBoxMenuItem straightLineItem, polyLineItem, freeLineItem, arrowItem;
	private String currentSet = "Startup Macros";

	private static Color foregroundColor = Prefs.getColor(Prefs.FCOLOR,Color.black);
	private static Color backgroundColor = Prefs.getColor(Prefs.BCOLOR,Color.white);
	private static boolean brushEnabled;
	private static boolean multiPointMode = Prefs.multiPointMode;
	private static boolean roundRectMode;
	private static boolean arrowMode;
	private static int brushSize = (int)Prefs.get(BRUSH_SIZE, 15);
	private static int arcSize = (int)Prefs.get(ARC_SIZE, 20);
	private int lineType = LINE;
	
	private Color gray = IJ.backgroundColor;
	private Color brighter = gray.brighter();
	//private Color darker = gray.darker();
	//private Color evenDarker = darker.darker();
	private Color darker = new Color(175, 175, 175);
	private Color evenDarker = new Color(110, 110, 110);
	private Color triangleColor = new Color(150, 0, 0);
	private Color toolColor = new Color(0, 25, 45);


	public ToolbarSwing() {
		down = new boolean[NUM_TOOLS];
		resetButtons();
		down[0] = true;
		setForeground(Color.black);
		setBackground(gray);
		addMouseListener(this);
		addMouseMotionListener(this);
		instance = this;
		names[NUM_TOOLS-1] = "Switch to alternate macro tool sets";
		icons[NUM_TOOLS-1] = "C900T1c12>T7c12>"; // ">>"
		addPopupMenus();
		if (IJ.isMacOSX() || IJ.isVista()) Prefs.antialiasedTools = true;
	}

	void addPopupMenus() {
		rectPopup = new JPopupMenu();
		if (Menus.getFontSize()!=0)
			rectPopup.setFont(Menus.getFont());
		rectItem = new JCheckBoxMenuItem("Rectangle Tool", !roundRectMode);
		rectItem.addItemListener(this);
		rectPopup.add(rectItem);
		roundRectItem = new JCheckBoxMenuItem("Rounded Rectangle Tool", roundRectMode);
		roundRectItem.addItemListener(this);
		rectPopup.add(roundRectItem);
		add(rectPopup);

		ovalPopup = new JPopupMenu();
		if (Menus.getFontSize()!=0)
			ovalPopup.setFont(Menus.getFont());
		ovalItem = new JCheckBoxMenuItem("Elliptical Selection Tool", !brushEnabled);
		ovalItem.addItemListener(this);
		ovalPopup.add(ovalItem);
		brushItem = new JCheckBoxMenuItem("Selection Brush Tool", brushEnabled);
		brushItem.addItemListener(this);
		ovalPopup.add(brushItem);
		add(ovalPopup);

		pointPopup = new JPopupMenu();
		if (Menus.getFontSize()!=0)
			pointPopup.setFont(Menus.getFont());
		pointItem = new JCheckBoxMenuItem("Point Tool", !multiPointMode);
		pointItem.addItemListener(this);
		pointPopup.add(pointItem);
		multiPointItem = new JCheckBoxMenuItem("Multi-point Tool", multiPointMode);
		multiPointItem.addItemListener(this);
		pointPopup.add(multiPointItem);
		add(pointPopup);

		linePopup = new JPopupMenu();
		if (Menus.getFontSize()!=0)
			linePopup.setFont(Menus.getFont());
		straightLineItem = new JCheckBoxMenuItem("Straight Line", lineType==LINE&&!arrowMode);
		straightLineItem.addItemListener(this);
		linePopup.add(straightLineItem);
		polyLineItem = new JCheckBoxMenuItem("Segmented Line", lineType==POLYLINE);
		polyLineItem.addItemListener(this);
		linePopup.add(polyLineItem);
		freeLineItem = new JCheckBoxMenuItem("Freehand Line", lineType==FREELINE);
		freeLineItem.addItemListener(this);
		linePopup.add(freeLineItem);
		arrowItem = new JCheckBoxMenuItem("Arrow tool", lineType==LINE&&!arrowMode);
		arrowItem.addItemListener(this);
		linePopup.add(arrowItem);
		add(linePopup);

		switchPopup = new JPopupMenu();
		if (Menus.getFontSize()!=0)
			switchPopup.setFont(Menus.getFont());
		add(switchPopup);
	}
	
	/** Returns the ID of the current tool (Toolbar.RECTANGLE,
		Toolbar.OVAL, etc.). */
	public int getToolId() {
		return current;
	}

	/** Returns the ID of the tool whose name (the description displayed in the status bar)
		starts with the specified string, or -1 if the tool is not found. */
	public int getToolId(String name) {
		int tool =  -1;
		for (int i=0; i<=SPARE9; i++) {
			if (names[i]!=null && names[i].startsWith(name)) {
				tool = i;
				break;
			}			
		}
		return tool;
	}

	/** Returns a reference to the ImageJ toolbar. */
	public static ToolbarSwing getInstance() {
		return instance;
	}

	
	private void showMessage(int tool) {
		if (tool>=SPARE1 && tool<=SPARE9 && names[tool]!=null) {
			String name = names[tool];
			int index = name.indexOf("Action Tool");
			if (index!=-1)
				name = name.substring(0, index);
			else {
				index = name.indexOf("Menu Tool");
				if (index!=-1)
					name = name.substring(0, index+4);
			}
			IJ.showStatus(name);
			return;
		}
		String hint = " (right click to switch)";
		switch (tool) {
			case RECTANGLE:
				if (roundRectMode)
					IJ.showStatus("Rectangular or *rounded rectangular* selections"+hint);
				else
					IJ.showStatus("*Rectangular* or rounded rectangular selections"+hint);
				return;
			case OVAL:
				if (brushEnabled)
					IJ.showStatus("Elliptical or *brush* selections"+hint);
				else
					IJ.showStatus("*Elliptical* or brush selections"+hint);
				return;
			case POLYGON:
				IJ.showStatus("Polygon selections");
				return;
			case FREEROI:
				IJ.showStatus("Freehand selections");
				return;
			case LINE:
				if (arrowMode)
					IJ.showStatus("Straight, segmented or freehand lines, or *arrows*"+hint);
				else
					IJ.showStatus("*Straight*, segmented or freehand lines, or arrows"+hint);
				return;
			case POLYLINE:
				IJ.showStatus("Straight, *segmented* or freehand lines, or arrows"+hint);
				return;
			case FREELINE:
				IJ.showStatus("Straight, segmented or *freehand* lines, or arrows"+hint);
				return;
			case POINT:
				if (multiPointMode)
					IJ.showStatus("Point or *multi-point* selections"+hint);
				else
					IJ.showStatus("*Point* or multi-point selections"+hint);
				return;
			case WAND:
				IJ.showStatus("Wand (tracing) tool");
				return;
			case TEXT:
				IJ.showStatus("Text tool");
				TextRoi.recordSetFont();
				return;
			case MAGNIFIER:
				IJ.showStatus("Magnifying glass (or use \"+\" and \"-\" keys)");
				return;
			case HAND:
				IJ.showStatus("Scrolling tool (or press space bar and drag)");
				return;
			case DROPPER:
				IJ.showStatus("Color picker (" + foregroundColor.getRed() + ","
				+ foregroundColor.getGreen() + "," + foregroundColor.getBlue() + ")");
				return;
			case ANGLE:
				IJ.showStatus("Angle tool");
				return;
			default:
				IJ.showStatus("ImageJ "+IJ.getVersion()+" / Java "+System.getProperty("java.version")+(IJ.is64Bit()?" (64-bit)":" (32-bit)"));
				return;
		}
	}


	private void resetButtons() {
		for (int i=0; i<NUM_TOOLS; i++)
			down[i] = false;
	}

	public void paint(Graphics g) {
		if (null==g) return;
		drawButtons(g);
	}
    private void drawButtons(Graphics g) {
        if (Prefs.antialiasedTools) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        for (int i = 0; i < LINE; i++) {
            drawButton(g, i);
        }
        drawButton(g, lineType);
        for (int i = POINT; i < NUM_TOOLS; i++) {
            drawButton(g, i);
        }
    }

    private void fill3DRect(Graphics g, int x, int y, int width, int height, boolean raised) {
        if (null == g) {
            return;
        }
        if (raised) {
            g.setColor(gray);
        } else {
            g.setColor(darker);
        }
        g.fillRect(x + 1, y + 1, width - 2, height - 2);
        g.setColor(raised ? brighter : evenDarker);
        g.drawLine(x, y, x, y + height - 1);
        g.drawLine(x + 1, y, x + width - 2, y);
        g.setColor(raised ? evenDarker : brighter);
        g.drawLine(x + 1, y + height - 1, x + width - 1, y + height - 1);
        g.drawLine(x + width - 1, y, x + width - 1, y + height - 2);
    }

    private void drawButton(Graphics g, int tool) {
        if (g == null) {
            return;
        }
        int index = toolIndex(tool);
        fill3DRect(g, index * SIZE + 1, 1, SIZE, SIZE - 1, !down[tool]);
        g.setColor(toolColor);
        int x = index * SIZE + OFFSET;
        int y = OFFSET;
        if (down[tool]) {
            x++;
            y++;
        }
        this.g = g;
        if (tool >= SPARE1 && tool <= SPARE9 && icons[tool] != null) {
            drawIcon(g, tool, x, y);
            return;
        }
        switch (tool) {
            case RECTANGLE:
                xOffset = x;
                yOffset = y;
                if (roundRectMode) {
                    g.drawRoundRect(x + 1, y + 2, 15, 12, 8, 8);
                } else {
                    g.drawRect(x + 1, y + 2, 15, 12);
                }
                drawTriangle(15, 14);
                return;
            case OVAL:
                xOffset = x;
                yOffset = y;
                if (brushEnabled) {
                    m(9, 2);
                    d(13, 2);
                    d(13, 2);
                    d(15, 5);
                    d(15, 8);
                    d(13, 10);
                    d(10, 10);
                    d(8, 13);
                    d(4, 13);
                    d(2, 11);
                    d(2, 7);
                    d(4, 5);
                    d(7, 5);
                    d(9, 2);
                } else {
                    g.drawOval(x + 1, y + 2, 15, 12);
                }
                drawTriangle(15, 14);
                return;
            case POLYGON:
                xOffset = x + 1;
                yOffset = y + 3;
                m(4, 0);
                d(14, 0);
                d(14, 1);
                d(10, 5);
                d(10, 6);
                d(13, 9);
                d(13, 10);
                d(0, 10);
                d(0, 4);
                d(4, 0);
                return;
            case FREEROI:
                xOffset = x + 1;
                yOffset = y + 3;
                m(3, 0);
                d(5, 0);
                d(7, 2);
                d(9, 2);
                d(11, 0);
                d(13, 0);
                d(14, 1);
                d(15, 2);
                d(15, 4);
                d(14, 5);
                d(14, 6);
                d(12, 8);
                d(11, 8);
                d(10, 9);
                d(9, 9);
                d(8, 10);
                d(5, 10);
                d(3, 8);
                d(2, 8);
                d(1, 7);
                d(1, 6);
                d(0, 5);
                d(0, 2);
                d(1, 1);
                d(2, 1);
                return;
            case LINE:
                xOffset = x;
                yOffset = y;
                if (arrowMode) {
                    m(1, 14);
                    d(14, 1);
                    m(6, 5);
                    d(14, 1);
                    m(10, 9);
                    d(14, 1);
                    m(6, 5);
                    d(10, 9);
                } else {
                    m(0, 12);
                    d(17, 3);
                }
                drawTriangle(12, 14);
                return;
            case POLYLINE:
                xOffset = x;
                yOffset = y;
                m(14, 6);
                d(11, 3);
                d(1, 3);
                d(1, 4);
                d(6, 9);
                d(2, 13);
                drawTriangle(12, 14);
                return;
            case FREELINE:
                xOffset = x;
                yOffset = y;
                m(16, 4);
                d(14, 6);
                d(12, 6);
                d(9, 3);
                d(8, 3);
                d(6, 7);
                d(2, 11);
                d(1, 11);
                drawTriangle(12, 14);
                return;
            case POINT:
                xOffset = x;
                yOffset = y;
                if (multiPointMode) {
                    drawPoint(1, 3);
                    drawPoint(9, 1);
                    drawPoint(15, 5);
                    drawPoint(10, 11);
                    drawPoint(2, 12);
                } else {
                    m(1, 8);
                    d(6, 8);
                    d(6, 6);
                    d(10, 6);
                    d(10, 10);
                    d(6, 10);
                    d(6, 9);
                    m(8, 1);
                    d(8, 5);
                    m(11, 8);
                    d(15, 8);
                    m(8, 11);
                    d(8, 15);
                    m(8, 8);
                    d(8, 8);
                    g.setColor(Roi.getColor());
                    g.fillRect(x + 7, y + 7, 3, 3);
                }
                drawTriangle(14, 14);
                return;
            case WAND:
                xOffset = x + 2;
                yOffset = y + 2;
                dot(4, 0);
                m(2, 0);
                d(3, 1);
                d(4, 2);
                m(0, 0);
                d(1, 1);
                m(0, 2);
                d(1, 3);
                d(2, 4);
                dot(0, 4);
                m(3, 3);
                d(12, 12);
                return;
            case TEXT:
                xOffset = x + 2;
                yOffset = y + 1;
                m(0, 13);
                d(3, 13);
                m(1, 12);
                d(7, 0);
                d(12, 13);
                m(11, 13);
                d(14, 13);
                m(3, 8);
                d(10, 8);
                return;
            case MAGNIFIER:
                xOffset = x + 2;
                yOffset = y + 2;
                m(3, 0);
                d(3, 0);
                d(5, 0);
                d(8, 3);
                d(8, 5);
                d(7, 6);
                d(7, 7);
                d(6, 7);
                d(5, 8);
                d(3, 8);
                d(0, 5);
                d(0, 3);
                d(3, 0);
                m(8, 8);
                d(9, 8);
                d(13, 12);
                d(13, 13);
                d(12, 13);
                d(8, 9);
                d(8, 8);
                return;
            case HAND:
                xOffset = x + 1;
                yOffset = y + 1;
                m(5, 14);
                d(2, 11);
                d(2, 10);
                d(0, 8);
                d(0, 7);
                d(1, 6);
                d(2, 6);
                d(4, 8);
                d(4, 6);
                d(3, 5);
                d(3, 4);
                d(2, 3);
                d(2, 2);
                d(3, 1);
                d(4, 1);
                d(5, 2);
                d(5, 3);
                m(6, 5);
                d(6, 1);
                d(7, 0);
                d(8, 0);
                d(9, 1);
                d(9, 5);
                m(9, 1);
                d(11, 1);
                d(12, 2);
                d(12, 6);
                m(13, 4);
                d(14, 3);
                d(15, 4);
                d(15, 7);
                d(14, 8);
                d(14, 10);
                d(13, 11);
                d(13, 12);
                d(12, 13);
                d(12, 14);
                return;
            case DROPPER:
                xOffset = x;
                yOffset = y;
                g.setColor(foregroundColor);
                //m(0,0); d(17,0); d(17,17); d(0,17); d(0,0);
                m(12, 2);
                d(14, 2);
                m(11, 3);
                d(15, 3);
                m(11, 4);
                d(15, 4);
                m(8, 5);
                d(15, 5);
                m(9, 6);
                d(14, 6);
                m(10, 7);
                d(12, 7);
                d(12, 9);
                m(8, 7);
                d(2, 13);
                d(2, 15);
                d(4, 15);
                d(11, 8);
                g.setColor(backgroundColor);
                m(0, 0);
                d(16, 0);
                d(16, 16);
                d(0, 16);
                d(0, 0);
                return;
            case ANGLE:
                xOffset = x + 1;
                yOffset = y + 2;
                m(0, 11);
                d(11, 0);
                m(0, 11);
                d(15, 11);
                m(10, 11);
                d(10, 8);
                m(9, 7);
                d(9, 6);
                dot(8, 5);
                return;
        }
    }

    void drawTriangle(int x, int y) {
        g.setColor(triangleColor);
        xOffset += x;
        yOffset += y;
        m(0, 0);
        d(4, 0);
        m(1, 1);
        d(3, 1);
        dot(2, 2);
    }

    void drawPoint(int x, int y) {
        g.setColor(toolColor);
        m(x - 2, y);
        d(x + 2, y);
        m(x, y - 2);
        d(x, y + 2);
        g.setColor(Roi.getColor());
        dot(x, y);
    }

    void drawIcon(Graphics g, int tool, int x, int y) {
        if (null == g) {
            return;
        }
        icon = icons[tool];
        this.icon = icon;
        int length = icon.length();
        int x1, y1, x2, y2;
        pc = 0;
        while (true) {
            char command = icon.charAt(pc++);
            if (pc >= length) {
                break;
            }
            switch (command) {
                case 'B':
                    x += v();
                    y += v();
                    break;  // reset base
                case 'R':
                    g.drawRect(x + v(), y + v(), v(), v());
                    break;  // rectangle
                case 'F':
                    g.fillRect(x + v(), y + v(), v(), v());
                    break;  // filled rectangle
                case 'O':
                    g.drawOval(x + v(), y + v(), v(), v());
                    break;  // oval
                case 'o':
                    g.fillOval(x + v(), y + v(), v(), v());
                    break;  // filled oval
                case 'C':
                    g.setColor(new Color(v() * 16, v() * 16, v() * 16));
                    break; // set color
                case 'L':
                    g.drawLine(x + v(), y + v(), x + v(), y + v());
                    break; // line
                case 'D':
                    g.fillRect(x + v(), y + v(), 1, 1);
                    break; // dot
                case 'P': // polyline
                    x1 = x + v();
                    y1 = y + v();
                    while (true) {
                        x2 = v();
                        if (x2 == 0) {
                            break;
                        }
                        y2 = v();
                        if (y2 == 0) {
                            break;
                        }
                        x2 += x;
                        y2 += y;
                        g.drawLine(x1, y1, x2, y2);
                        x1 = x2;
                        y1 = y2;
                    }
                    break;
                case 'T': // text (one character)
                    x2 = x + v();
                    y2 = y + v();
                    int size = v() * 10 + v();
                    char[] c = new char[1];
                    c[0] = pc < icon.length() ? icon.charAt(pc++) : 'e';
                    g.setFont(new Font("SansSerif", Font.BOLD, size));
                    g.drawString(new String(c), x2, y2);
                    break;
                default:
                    break;
            }
            if (pc >= length) {
                break;
            }
        }
        if (menus[tool] != null && menus[tool].getSubElements().length > 0) {
            xOffset = x;
            yOffset = y;
            drawTriangle(14, 14);
        }
    }

    private void m(int x, int y) {
        this.x = xOffset + x;
        this.y = yOffset + y;
    }

    private void d(int x, int y) {
        x += xOffset;
        y += yOffset;
        g.drawLine(this.x, this.y, x, y);
        this.x = x;
        this.y = y;
    }

    private void dot(int x, int y) {
        g.fillRect(x + xOffset, y + yOffset, 1, 1);
    }

    int v() {
        if (pc >= icon.length()) {
            return 0;
        }
        char c = icon.charAt(pc++);
        //IJ.log("v: "+pc+" "+c+" "+toInt(c));
        switch (c) {
            case '0':
                return 0;
            case '1':
                return 1;
            case '2':
                return 2;
            case '3':
                return 3;
            case '4':
                return 4;
            case '5':
                return 5;
            case '6':
                return 6;
            case '7':
                return 7;
            case '8':
                return 8;
            case '9':
                return 9;
            case 'a':
                return 10;
            case 'b':
                return 11;
            case 'c':
                return 12;
            case 'd':
                return 13;
            case 'e':
                return 14;
            case 'f':
                return 15;
            default:
                return 0;
        }
    }


	public boolean setTool(String name) { 
		name = name.toLowerCase(Locale.US);
		boolean ok = true;
		if (name.indexOf("round")!=-1) {
			roundRectMode = true;
			setTool(RECTANGLE);
		} else if (name.indexOf("rect")!=-1) {
			roundRectMode = false;
			setTool(RECTANGLE);
		} else if (name.indexOf("ellip")!=-1 || name.indexOf("oval")!=-1) {
			brushEnabled = false;
			setTool(OVAL);
		} else if (name.indexOf("brush")!=-1) {
			brushEnabled = true;
			setTool(OVAL);
		} else if (name.indexOf("polygon")!=-1)
			setTool(POLYGON);
		else if (name.indexOf("polyline")!=-1)
			setTool(POLYLINE);
		else if (name.indexOf("freeline")!=-1)
			setTool(FREELINE);
		else if (name.indexOf("line")!=-1) {
			arrowMode = false;
			setTool(LINE);
		} else if (name.indexOf("arrow")!=-1) {
			arrowMode = true;
			setTool(LINE);
		} else if (name.indexOf("free")!=-1)
			setTool(FREEROI);
		else if (name.indexOf("multi")!=-1) {
			multiPointMode = true;
			Prefs.multiPointMode = true;
			setTool(POINT);
		} else if (name.indexOf("point")!=-1) {
			multiPointMode = false;
			Prefs.multiPointMode = false;
			setTool(POINT);
		} else if (name.indexOf("wand")!=-1)
			setTool(WAND);
		else if (name.indexOf("text")!=-1)
			setTool(TEXT);
		else if (name.indexOf("hand")!=-1)
			setTool(HAND);
		else if (name.indexOf("zoom")!=-1)
			setTool(MAGNIFIER);
		else if (name.indexOf("dropper")!=-1||name.indexOf("color")!=-1)
			setTool(DROPPER);
		else if (name.indexOf("angle")!=-1)
			setTool(ANGLE);
		else
			ok = false;
		return ok;
	}
	
	/** Returns the name of the current tool. */
	public String getToolName() {
		String name = instance.getName(current);
		if (current>=SPARE1 && current<=SPARE9 && instance.names[current]!=null)
			name = instance.names[current];
		return name!=null?name:"";
	}

	/** Returns the name of the specified tool. */
	String getName(int id) {
		switch (id) {
			case RECTANGLE: return roundRectMode?"roundrect":"rectangle";
			case OVAL: return brushEnabled?"brush":"oval";
			case POLYGON: return "polygon";
			case FREEROI: return "freehand";
			case LINE: return arrowMode?"arrow":"line";
			case POLYLINE: return "polyline";
			case FREELINE: return "freeline";
			case ANGLE: return "angle";
			case POINT: return Prefs.multiPointMode?"multipoint":"point";
			case WAND: return "wand";
			case TEXT: return "text";
			case HAND: return "hand";
			case MAGNIFIER: return "zoom";
			case DROPPER: return "dropper";
			default: return null;
		}
		
	}
	
	public void setTool(int tool) {
		if ((tool==current&&!(tool==RECTANGLE||tool==OVAL||tool==POINT)) || tool<0 || tool>=NUM_TOOLS-1)
			return;
		if (tool==SPARE1||(tool>=SPARE2&&tool<=SPARE8)) {
			if (names[tool]==null)
				names[tool] = "Spare tool"; // enable tool
			if (names[tool].indexOf("Action Tool")!=-1)
				return;
		}
		if (isLine(tool)) lineType = tool;
		setTool2(tool);
	}
		
	private void setTool2(int tool) {
		if (!isValidTool(tool)) return;
		String previousName = getToolName();
		current = tool;
		down[current] = true;
		if (current!=previous)
			down[previous] = false;
		Graphics g = this.getGraphics();
		if (Prefs.antialiasedTools) {
			Graphics2D g2d = (Graphics2D)g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		drawButton(g, previous);
		drawButton(g, current);
		if (null==g) return;
		g.dispose();
		showMessage(current);
		previous = current;
		if (Recorder.record) {
			String name = getName(current);
			if (name!=null) Recorder.record("setTool", name);
		}
		if (IJ.isMacOSX())
			repaint();
		if (!previousName.equals(getToolName()))
			IJ.notifyEventListeners(IJEventListener.TOOL_CHANGED);
	}
	
	boolean isValidTool(int tool) {
		if (tool<0 || tool>=NUM_TOOLS)
			return false;
		if ((tool==SPARE1||(tool>=SPARE2&&tool<=SPARE9)) && names[tool]==null)
			return false;
		return true;
	}

	/**
	* @deprecated
	* replaced by getForegroundColor()
	*/
	public Color getColor() {
		return foregroundColor;
	}

	/**
	* @deprecated
	* replaced by setForegroundColor()
	*/
	public void setColor(Color c) {
		if (c!=null) {
			foregroundColor = c;
			drawButton(this.getGraphics(), DROPPER);
		}
	}
	
	public Color getForegroundColor() {
		return foregroundColor;
	}

	public void setForegroundColor(Color c) {
		if (c!=null) {
			foregroundColor = c;
			repaintTool(DROPPER);
			if (!IJ.isMacro()) setRoiColor(c);
			IJ.notifyEventListeners(IJEventListener.FOREGROUND_COLOR_CHANGED);
		}
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(Color c) {
		if (c!=null) {
			backgroundColor = c;
			repaintTool(DROPPER);
			IJ.notifyEventListeners(IJEventListener.BACKGROUND_COLOR_CHANGED);
		}
	}
	
	private void setRoiColor(Color c) {
		IjxImagePlus imp = WindowManager.getCurrentImage();
		if (imp==null) return;
		Roi roi = imp.getRoi();
		if (roi!=null && (roi.isDrawingTool())) {
			roi.setStrokeColor(c);
			imp.draw();
		}
	}

	/** Returns the size of the brush tool, or 0 if the brush tool is not enabled. */
	public int getBrushSize() {
		if (brushEnabled)
			return brushSize;
		else
			return 0;
	}

	/** Set the size of the brush tool, which must be greater than 4. */
	public void setBrushSize(int size) {
		brushSize = size;
		if (brushSize<5) brushSize = 5;
		Prefs.set(BRUSH_SIZE, brushSize);
	}
		
	/** Returns the rounded rectangle arc size, or 0 if the rounded rectangle tool is not enabled. */
	public int getRoundRectArcSize() {
		if (!roundRectMode)
			return 0;
		else
			return arcSize;
	}

	/** Sets the rounded rectangle arc size (pixels). */
	public void setRoundRectArcSize(int size) {
		if (size<=0)
			roundRectMode = false;
		else {
			arcSize = size;
			Prefs.set(ARC_SIZE, arcSize);
		}
		repaintTool(RECTANGLE);
		IjxImagePlus imp = WindowManager.getCurrentImage();
		Roi roi = imp!=null?imp.getRoi():null;
		if (roi!=null && roi.getType()==Roi.RECTANGLE)
			roi.setRoundRectArcSize(roundRectMode?arcSize:0);
	}

	/** Returns 'true' if the multi-point tool is enabled. */
	public boolean getMultiPointMode() {
		return multiPointMode;
	}

	public int getButtonSize() {
		return SIZE;
	}
	
	static void repaintTool(int tool) {
		if (IJ.getInstance()!=null) {
			ToolbarSwing tb = getInstance();
			Graphics g = tb.getGraphics();
			if (Prefs.antialiasedTools)
				((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			tb.drawButton(g, tool);
			if (g!=null) g.dispose();
		}
		//Toolbar tb = getInstance();
		//tb.repaint(tool * SIZE , 0, SIZE, SIZE);
	}
	
	// Returns the toolbar position index of the specified tool
    int toolIndex(int tool) {
    	switch (tool) {
			case RECTANGLE: return 0;
			case OVAL: return 1;
			case POLYGON: return 2;
			case FREEROI: return 3;
			case LINE: return 4;
			case POLYLINE: return 4;
			case FREELINE: return 4;
			case POINT: return 6;
			case WAND: return 7;
			case TEXT: return 8;
			case MAGNIFIER: return 9;
			case HAND: return 10;
			case DROPPER: return 11;
			case ANGLE: return 5;
			case SPARE1: return 12;
			default: return tool - 2;
		}
    }

	// Returns the tool corresponding to the specified tool position index
    int toolID(int index) {
    	switch (index) {
			case 0: return RECTANGLE;
			case 1: return OVAL;
			case 2: return POLYGON;
			case 3: return FREEROI;
			case 4: return lineType;
			case 5: return ANGLE;
			case 6: return POINT;
			case 7: return WAND;
			case 8: return TEXT;
			case 9: return MAGNIFIER;
			case 10: return HAND;
			case 11: return DROPPER;
			case 12: return SPARE1;
			default: return index + 2;
		}
    }

	public void mousePressed(MouseEvent e) {
		int x = e.getX();
 		int newTool = 0;
		for (int i=0; i<NUM_BUTTONS; i++) {
			if (x>i*SIZE && x<i*SIZE+SIZE)
				newTool = toolID(i);
		}
		if (newTool==SPARE9) {
			showSwitchJPopupMenu(e);
			return;
		}
		if (!isValidTool(newTool)) return;
		if (menus[newTool]!=null && menus[newTool].getSubElements().length>0) {
            menus[newTool].show(e.getComponent(), e.getX(), e.getY());
			return;
		}
		boolean doubleClick = newTool==current && (System.currentTimeMillis()-mouseDownTime)<=DOUBLE_CLICK_THRESHOLD;
 		mouseDownTime = System.currentTimeMillis();
		if (!doubleClick) {
			mpPrevious = current;
			if (isMacroTool(newTool)) {
				String name = names[newTool];
				if (name.indexOf("Unused Tool")!=-1)
					return;
				if (name.indexOf("Action Tool")!=-1) {
					if (e.isPopupTrigger()||e.isMetaDown()) {
						name = name.endsWith(" ")?name:name+" ";
						macroInstaller.runMacroTool(name+"Options");
					} else {
						drawTool(newTool, true);
						IJ.wait(50);
						drawTool(newTool, false);
						runMacroTool(newTool);
					}
					return;
				} else {	
					name = name.endsWith(" ")?name:name+" ";
					macroInstaller.runMacroTool(name+"Selected");
				}
			}
			setTool2(newTool);
			boolean isRightClick = e.isPopupTrigger()||e.isMetaDown();
			if (current==RECTANGLE && isRightClick) {
				rectItem.setState(!roundRectMode);
				roundRectItem.setState(roundRectMode);
				if (IJ.isMacOSX()) IJ.wait(10);
				rectPopup.show(e.getComponent(),x,y);
				mouseDownTime = 0L;
			}
			if (current==OVAL && isRightClick) {
				ovalItem.setState(!brushEnabled);
				brushItem.setState(brushEnabled);
				if (IJ.isMacOSX()) IJ.wait(10);
				ovalPopup.show(e.getComponent(),x,y);
				mouseDownTime = 0L;
			}
			if (current==POINT && isRightClick) {
				pointItem.setState(!multiPointMode);
				multiPointItem.setState(multiPointMode);
				if (IJ.isMacOSX()) IJ.wait(10);
				pointPopup.show(e.getComponent(),x,y);
				mouseDownTime = 0L;
			}
			if (isLine(current) && isRightClick) {
				straightLineItem.setState(lineType==LINE&&!arrowMode);
				polyLineItem.setState(lineType==POLYLINE);
				freeLineItem.setState(lineType==FREELINE);
				arrowItem.setState(lineType==LINE&&arrowMode);
				if (IJ.isMacOSX()) IJ.wait(10);
				linePopup.show(e.getComponent(),x,y);
				mouseDownTime = 0L;
			}
			if (isMacroTool(current) && isRightClick) {
				String name = names[current].endsWith(" ")?names[current]:names[current]+" ";
				macroInstaller.runMacroTool(name+"Options");
			}
		} else {
			if (isMacroTool(current)) {
				String name = names[current].endsWith(" ")?names[current]:names[current]+" ";
				macroInstaller.runMacroTool(name+"Options");
				return;
			}
			IjxImagePlus imp = WindowManager.getCurrentImage();
			switch (current) {
				case RECTANGLE:
					if (roundRectMode)
						showRoundRectDialog();
					break;
				case OVAL:
					showBrushDialog();
					break;
				case MAGNIFIER:
					if (imp!=null) {
						IjxImageCanvas ic = imp.getCanvas();
						if (ic!=null) ic.unzoom();
					}
					break;
				case LINE: case POLYLINE: case FREELINE:
					if (current==LINE && arrowMode) {
						IJ.doCommand("Arrow Tool...");
					} else
						IJ.runPlugIn("ij.plugin.frame.LineWidthAdjuster", "");
					break;
				case POINT:
					if (multiPointMode) {
						if (imp!=null && imp.getRoi()!=null)
							IJ.doCommand("Add Selection...");
					} else
						IJ.doCommand("Point Tool...");
					break;
				case WAND:
					IJ.doCommand("Wand Tool...");
					break;
				case TEXT:
					IJ.doCommand("Fonts...");
					break;
				case DROPPER:
					IJ.doCommand("Color Picker...");
					setTool2(mpPrevious);
					break;
				default:
			}
		}
	}
	
	void showSwitchJPopupMenu(MouseEvent e) {
		String path = IJ.getDirectory("macros")+"toolsets/";
		if (path==null) {
			return;
		}
		boolean applet = IJ.getApplet()!=null;
		File f = new File(path);
		String[] list;
		if (!applet && f.exists() && f.isDirectory()) {
			list = f.list();
			if (list==null) return;
		} else
			list = new String[0];
        boolean stackTools = false;
		for (int i=0; i<list.length; i++) {
            if (list[i].equals("Stack Tools.txt")) {
                stackTools = true;
                break;
            }
		}
		switchPopup.removeAll();
        path = IJ.getDirectory("macros") + "StartupMacros.txt";
		f = new File(path);
		if (!applet && f.exists())
            addItem("Startup Macros");
        else
            addItem("StartupMacros*");
		if (!stackTools) addItem("Stack Tools*");
 		for (int i=0; i<list.length; i++) {
			String name = list[i];
			if (name.endsWith(".txt")) {
				name = name.substring(0, name.length()-4);
                addItem(name);
			} else if (name.endsWith(".ijm")) {
				name = name.substring(0, name.length()-4) + " ";
                addItem(name);
			}
		}
		addItem("Help...");
		add(ovalPopup);
		if (IJ.isMacOSX()) IJ.wait(10);
		switchPopup.show(e.getComponent(), e.getX(), e.getY());
	}
    
    void addItem(String name) {
		JCheckBoxMenuItem item = new JCheckBoxMenuItem(name, name.equals(currentSet));
		item.addItemListener(this);
		switchPopup.add(item);
    }

	void drawTool(int tool, boolean drawDown) {
		down[tool] = drawDown;
		Graphics g = this.getGraphics();
		if (!drawDown && Prefs.antialiasedTools) {
			Graphics2D g2d = (Graphics2D)g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		drawButton(g, tool);
		if (null==g) return;
		g.dispose();
	}

	boolean isLine(int tool) {
		return tool==LINE || tool==POLYLINE || tool==FREELINE;
	}
	
	public void restorePreviousTool() {
		setTool2(mpPrevious);
	}
	
	boolean isMacroTool(int tool) {
		return tool>=SPARE1 && tool<=SPARE9 && names[tool]!=null && macroInstaller!=null;
	}
	
	public void mouseReleased(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
    public void mouseDragged(MouseEvent e) {}
	
	public void itemStateChanged(ItemEvent e) {
		JCheckBoxMenuItem item = (JCheckBoxMenuItem)e.getSource();
		String previousName = getToolName();
		if (item==rectItem || item==roundRectItem) {
			roundRectMode = item==roundRectItem;
			repaintTool(RECTANGLE);
			showMessage(RECTANGLE);
			IjxImagePlus imp = WindowManager.getCurrentImage();
			Roi roi = imp!=null?imp.getRoi():null;
			if (roi!=null && roi.getType()==Roi.RECTANGLE)
				roi.setRoundRectArcSize(roundRectMode?arcSize:0);
			if (!previousName.equals(getToolName()))
				IJ.notifyEventListeners(IJEventListener.TOOL_CHANGED);
		} else if (item==ovalItem || item==brushItem) {
			brushEnabled = item==brushItem;
			repaintTool(OVAL);
			showMessage(OVAL);
			if (!previousName.equals(getToolName()))
				IJ.notifyEventListeners(IJEventListener.TOOL_CHANGED);
		} else if (item==pointItem || item==multiPointItem) {
			multiPointMode = item==multiPointItem;
			Prefs.multiPointMode = multiPointMode;
			repaintTool(POINT);
			showMessage(POINT);
			if (!previousName.equals(getToolName()))
				IJ.notifyEventListeners(IJEventListener.TOOL_CHANGED);
		} else if (item==straightLineItem) {
			lineType = LINE;
			arrowMode = false;
			setTool2(LINE);
			showMessage(LINE);
		} else if (item==polyLineItem) {
			lineType = POLYLINE;
			setTool2(POLYLINE);
			showMessage(POLYLINE);
		} else if (item==freeLineItem) {
			lineType = FREELINE;
			setTool2(FREELINE);
			showMessage(FREELINE);
		} else if (item==arrowItem) {
			lineType = LINE;
			arrowMode = true;
			setTool2(LINE);
			showMessage(LINE);
		} else {
			String label = item.getActionCommand();
			if (!label.equals("Help...")) currentSet = label;
			String path;
			if (label.equals("Help...")) {
				IJ.showMessage("Tool Switcher",
					"Use this drop down menu to switch to macro tool\n"+
					"sets located in the ImageJ/macros/toolsets folder,\n"+
					"or to revert to the ImageJ/macros/StartupMacros\n"+
					"set. The default tool sets, which have names\n"+
					"ending in '*', are loaded from ij.jar.\n"+
					" \n"+
					"Hold the shift key down while selecting a tool\n"+
					"set to view its source code.\n"+
					" \n"+
					"Several example tool sets are available at\n"+
					"<"+IJ.URL+"/macros/toolsets/>."
					);
				return;
			} else if (label.endsWith("*")) {
                // load from ij.jar
                MacroInstaller mi = new MacroInstaller();
                label = label.substring(0, label.length()-1) + ".txt";
                path = "/macros/"+label;
				if (IJ.shiftKeyDown()) {
					String macros = mi.openFromIJJar(path);
                    Editor ed = new Editor();
                    ed.setSize(350, 300);
                    ed.create(label, macros);
                	IJ.setKeyUp(KeyEvent.VK_SHIFT);
				} else
					mi.installFromIJJar(path);
            } else {
                // load from ImageJ/macros/toolsets
                if (label.equals("Startup Macros"))
                    path = IJ.getDirectory("macros")+"StartupMacros.txt";
                else if (label.endsWith(" "))
                    path = IJ.getDirectory("macros")+"toolsets/"+label.substring(0, label.length()-1)+".ijm";
                else
                    path = IJ.getDirectory("macros")+"toolsets/"+label+".txt";
                try {
                    if (IJ.shiftKeyDown()) {
                        IJ.open(path);
                		IJ.setKeyUp(KeyEvent.VK_SHIFT);
                    } else
                        new MacroInstaller().run(path);
                }
                catch(Exception ex) {}
            }
		}
	}

	public void actionPerformed(ActionEvent e) {
		JMenuItem item = (JMenuItem)e.getSource();
		String cmd = e.getActionCommand();
		JPopupMenu popup = (JPopupMenu)item.getParent();
		int tool = -1;
		for (int i=SPARE1; i<NUM_TOOLS; i++) {
			if (popup==menus[i]) {
				tool = i;
				break;
			}
		}
		if (tool==-1) return;
		if (macroInstaller!=null)
			macroInstaller.runMenuTool(names[tool], cmd);
    }

	public Dimension getPreferredSize(){
		return ps;
	}

	public Dimension getMinimumSize(){
		return ps;
	}
	
	public void mouseMoved(MouseEvent e) {
//		int x = e.getX();
//		x=toolID(x/SIZE);
//		showMessage(x);
	}

	/** Adds a tool to the toolbar. The 'toolTip' string is displayed in the status bar
		 when the mouse is over the tool icon. The 'toolTip' string may include icon 
		(http://rsb.info.nih.gov/ij/developer/macro/macros.html#tools).
		Returns the tool ID, or -1 if all tools are in use. */
	public int addTool(String toolTip) {
		int index = toolTip.indexOf('-');
		boolean hasIcon = index>=0 && (toolTip.length()-index)>4;
		int tool =-1;
		if (names[SPARE1]==null)
			tool = SPARE1;
		if (tool==-1) {
			for (int i=SPARE2; i<=SPARE8; i++) {
				if (names[i]==null) {
					tool = i;
					break;
				}			
			}
		}
		if (tool==-1) return -1;
		if (hasIcon) {
			icons[tool] = toolTip.substring(index+1);
			if (index>0 && toolTip.charAt(index-1)==' ')
				names[tool] = toolTip.substring(0, index-1);
			else
				names[tool] = toolTip.substring(0, index);
		} else {
			if (toolTip.endsWith("-"))
				toolTip = toolTip.substring(0, toolTip.length()-1);
			else if (toolTip.endsWith("- "))
				toolTip = toolTip.substring(0, toolTip.length()-2);
			names[tool] = toolTip;
		}
        if (tool==current && (names[tool].indexOf("Action Tool")!=-1||names[tool].indexOf("Unused Tool")!=-1))
        	setTool(RECTANGLE);
        if (names[tool].endsWith(" Menu Tool"))
            installMenu(tool);
		return tool;
	}
    
    void installMenu(int tool) {
        Program pgm = macroInstaller.getProgram();
        Hashtable h = pgm.getMenus();
        if (h==null) return;
        String[] commands = (String[])h.get(names[tool]);
        if (commands==null) return;
		if (menus[tool]==null) {
			menus[tool] = new JPopupMenu("");
			if (Menus.getFontSize()!=0)
				menus[tool].setFont(Menus.getFont());
			add(menus[tool] );
		} else
			menus[tool].removeAll();
        for (int i=0; i<commands.length; i++) {
			if (commands[i].equals("-"))
				menus[tool].addSeparator();
			else {
				JMenuItem mi = new JMenuItem(commands[i]);
				mi.addActionListener(this);
				menus[tool].add(mi);
			}
        }
        if (tool==current) setTool(RECTANGLE);
    }
    
	/** Used by the MacroInstaller class to install macro tools. */
	public void addMacroTool(String name, MacroInstaller macroInstaller, int id) {
	    if (id==0) {
			for (int i=SPARE1; i<NUM_TOOLS-1; i++) {
				names[i] = null;
				icons[i] = null;
				if (menus[i]!=null) menus[i].removeAll();
			}
	    }
		this.macroInstaller = macroInstaller;
        addTool(name);
	}
			
	public void runMacroTool(int id) {
		if (macroInstaller!=null)
			macroInstaller.runMacroTool(names[id]);
	}
	
	void showBrushDialog() {
		GenericDialog gd = new GenericDialog("Selection Brush");
		gd.addCheckbox("Enable Selection Brush", brushEnabled);
		gd.addNumericField("           Size:", brushSize, 0, 4, "pixels");
		gd.showDialog();
		if (gd.wasCanceled()) return;
		brushEnabled = gd.getNextBoolean();
		brushSize = (int)gd.getNextNumber();
		repaintTool(OVAL);
		IjxImagePlus img = WindowManager.getCurrentImage();
		Roi roi = img!=null?img.getRoi():null;
		if (roi!=null && roi.getType()==Roi.OVAL && brushEnabled) img.killRoi();
		Prefs.set(BRUSH_SIZE, brushSize);
	}

	void showRoundRectDialog() {
		GenericDialog gd = new GenericDialog("Rounded Rectangle");
		gd.addNumericField("Corner arc size:", arcSize, 0, 4, "pixels");
		gd.showDialog();
		if (gd.wasCanceled()) return;
		setRoundRectArcSize((int)gd.getNextNumber());
	}

}
